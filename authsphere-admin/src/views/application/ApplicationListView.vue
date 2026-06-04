<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Monitor, Iphone, Coin, Platform } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appApi, type AppPayload, type AppRecord } from '@/api/app'
import { appClientApi, type AppClientRecord } from '@/api/appClient'
import ApplicationFormDialog from './components/ApplicationFormDialog.vue'

const router = useRouter()
const query = reactive({
  page: 1,
  size: 10,
  appName: '',
  appCode: '',
  appType: '',
  status: undefined as number | undefined
})

const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const selectedApp = ref<AppRecord | null>(null)
const selectedClients = ref<AppClientRecord[]>([])
const tableData = ref<AppRecord[]>([])
const total = ref(0)

const appTypeOptions = [
  { label: 'IAM', value: 'IAM' },
  { label: '商城', value: 'MALL' },
  { label: '支付', value: 'PAYMENT' },
  { label: '仓储', value: 'WAREHOUSE' },
  { label: '物流', value: 'LOGISTICS' },
  { label: '自定义', value: 'CUSTOM' }
]

const appTypeText = (type?: string) => appTypeOptions.find(item => item.value === type)?.label || type || '-'

const getTypeClass = (type?: string) => {
  const classMap: Record<string, string> = {
    IAM: 'bg-blue',
    MALL: 'bg-green',
    PAYMENT: 'bg-purple',
    WAREHOUSE: 'bg-orange',
    LOGISTICS: 'bg-teal',
    CUSTOM: 'bg-gray'
  }
  return type ? classMap[type] || 'bg-gray' : 'bg-gray'
}

const getAppIcon = (type?: string) => {
  if (type === 'MALL') return Iphone
  if (type === 'PAYMENT') return Coin
  if (type === 'WAREHOUSE' || type === 'LOGISTICS') return Platform
  return Monitor
}

const getStatusClass = (status: number) => {
  if (status === 1) return 'text-green'
  if (status === 2) return 'text-orange'
  return 'text-gray'
}

const getStatusText = (status: number) => {
  if (status === 1) return '已启用'
  if (status === 2) return '已禁用'
  return '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await appApi.page({
      page: query.page,
      size: query.size,
      appName: query.appName || undefined,
      appCode: query.appCode || undefined,
      appType: query.appType || undefined,
      status: query.status
    })
    tableData.value = result.records || []
    total.value = result.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取应用列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.page = 1
  query.appName = ''
  query.appCode = ''
  query.appType = ''
  query.status = undefined
  fetchData()
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleOpenCreate = () => {
  router.push('/applications/create')
}

const handleOpenEdit = async (row: AppRecord) => {
  submitLoading.value = true
  try {
    selectedApp.value = await appApi.detail(row.id)
    selectedClients.value = await appClientApi.listByApp(row.id)
    editDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取应用编辑信息失败')
  } finally {
    submitLoading.value = false
  }
}

const submitCreateForm = async (payload: AppPayload) => {
  submitLoading.value = true
  try {
    await appApi.create(payload)
    ElMessage.success('应用创建成功')
    createDialogVisible.value = false
    handleSearch()
  } catch (error: any) {
    ElMessage.error(error.message || '创建应用失败')
  } finally {
    submitLoading.value = false
  }
}

const submitEditForm = async (payload: AppPayload) => {
  if (!selectedApp.value) return
  submitLoading.value = true
  try {
    await appApi.update(selectedApp.value.id, payload)
    ElMessage.success('应用已更新')
    editDialogVisible.value = false
    await fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '更新应用失败')
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row: AppRecord) => {
  const enabled = row.status === 1
  const action = enabled ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}应用「${row.appName}」？`, '提示', { type: 'warning' })
    if (enabled) {
      await appApi.disable(row.id)
    } else {
      await appApi.enable(row.id)
    }
    ElMessage.success(`应用已${action}`)
    fetchData()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || `${action}应用失败`)
  }
}

fetchData()
</script>

<template>
  <div class="application-list-page">
    <div class="page-header">
      <div class="header-left">
        <h1>应用列表</h1>
        <p>管理身份域下的所有应用，支持应用的创建、配置与生命周期管理。</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleOpenCreate">新建应用</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="app-filter-form">
        <el-form-item label="应用名称">
          <el-input v-model="query.appName" placeholder="请输入应用名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="应用编码">
          <el-input v-model="query.appCode" placeholder="请输入应用编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="应用类型">
          <el-select v-model="query.appType" placeholder="全部类型" clearable style="width: 160px">
            <el-option v-for="item in appTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="应用状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="已启用" :value="1" />
            <el-option label="已禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column label="应用名称" min-width="260">
          <template #default="{ row }">
            <div class="app-name-cell">
              <div class="app-icon" :class="getTypeClass(row.appType)">
                <el-icon><component :is="getAppIcon(row.appType)" /></el-icon>
              </div>
              <div class="app-info">
                <strong>{{ row.appName }}</strong>
                <span>{{ row.description || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="appCode" label="应用编码" min-width="150" />
        <el-table-column label="应用类型" min-width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :class="'tag-' + getTypeClass(row.appType)">
              {{ appTypeText(row.appType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应用客户端" min-width="180">
          <template #default="{ row }">
            <div class="client-tags-container">
              <template v-if="row.clientName && row.clientName.length">
                <el-tag
                  v-for="name in row.clientName"
                  :key="name"
                  size="small"
                  type="info"
                  class="client-tag"
                >
                  {{ name }}
                </el-tag>
              </template>
              <span v-else class="text-gray-muted">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="应用状态" width="100">
          <template #default="{ row }">
            <div class="status-cell" :class="getStatusClass(row.status)">
              <span class="dot"></span> {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="router.push(`/applications/detail/${row.id}`)">详情</el-button>
              <el-button link type="primary" @click="handleOpenEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="toggleStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <span class="total-text">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <ApplicationFormDialog
      v-model:visible="createDialogVisible"
      mode="create"
      :loading="submitLoading"
      @submit="submitCreateForm"
    />
    <ApplicationFormDialog
      v-model:visible="editDialogVisible"
      mode="edit"
      :loading="submitLoading"
      :initial-app="selectedApp"
      :initial-clients="selectedClients"
      @submit="submitEditForm"
    />
  </div>
</template>

<style scoped>
.application-list-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.header-left p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.filter-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  margin-bottom: 24px;
  padding: 24px 24px 6px 24px;
}

.app-filter-form :deep(.el-form-item) {
  margin-bottom: 18px;
  margin-right: 24px;
}

.app-filter-form :deep(.el-form-item__label) {
  font-weight: 400;
  color: #4b5563;
}

.form-actions {
  width: 100%;
  margin-top: 4px;
  display: flex;
}

.tabs-wrapper {
  margin-bottom: 16px;
}

.app-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eaeaea;
}

.app-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  color: #4b5563;
  padding: 0 24px;
}

.app-tabs :deep(.el-tabs__item.is-active) {
  color: #2563eb;
  font-weight: 600;
}

.table-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.app-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.app-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  color: #ffffff;
}

.bg-blue { background-color: #3b82f6; }
.bg-green { background-color: #10b981; }
.bg-purple { background-color: #8b5cf6; }
.bg-orange { background-color: #f97316; }
.bg-teal { background-color: #14b8a6; }
.bg-gray { background-color: #64748b; }

.tag-bg-blue { color: #3b82f6; border-color: #bfdbfe; background-color: #eff6ff; }
.tag-bg-green { color: #10b981; border-color: #bbf7d0; background-color: #f0fdf4; }
.tag-bg-purple { color: #8b5cf6; border-color: #ddd6fe; background-color: #f5f3ff; }
.tag-bg-orange { color: #f97316; border-color: #fed7aa; background-color: #fff7ed; }
.tag-bg-teal { color: #14b8a6; border-color: #99f6e4; background-color: #f0fdfa; }
.tag-bg-gray { color: #64748b; border-color: #cbd5e1; background-color: #f8fafc; }

.app-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.app-info strong {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.app-info span {
  font-size: 12px;
  color: #6b7280;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.status-cell .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
}

.text-green { color: #16a34a; }
.text-orange { color: #ea580c; }
.text-gray { color: #9ca3af; }

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.client-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.client-tag {
  background-color: #f1f5f9;
  border-color: #cbd5e1;
  color: #475569;
  font-weight: 500;
  border-radius: 4px;
}

.text-gray-muted {
  color: #94a3b8;
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-top: 1px solid #eaeaea;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}
</style>
