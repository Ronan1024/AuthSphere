<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Refresh, Download, ArrowDown, ArrowUp, Monitor, Iphone, Coin, Tools } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AppRecord } from '@/api/app'
import type { RealmRecord } from '@/api/realm'

const router = useRouter()
const isExpanded = ref(false)

const query = reactive({
  page: 1,
  size: 10,
  appName: '',
  appCode: '',
  appType: '',
  status: undefined as number | undefined,
  realmId: undefined as number | string | undefined,
  createTimeRange: [] as any[]
})

const loading = ref(false)
const tableData = ref<AppRecord[]>([])
const total = ref(0)
const realmOptions = ref<RealmRecord[]>([])

type MockAppRecord = AppRecord & {
  realmId?: string
}

const mockRealms: RealmRecord[] = [
  {
    id: 'realm_platform',
    code: 'platform_realm',
    name: '平台身份域',
    realmTypeId: 'platform',
    registerEnabled: false,
    ssoEnabled: true,
    status: 1
  },
  {
    id: 'realm_tenant_a',
    code: 'tenant_a_realm',
    name: '租户身份域 (租户A)',
    realmTypeId: 'tenant',
    registerEnabled: true,
    ssoEnabled: true,
    status: 1
  },
  {
    id: 'realm_tenant_b',
    code: 'tenant_b_realm',
    name: '租户身份域 (租户B)',
    realmTypeId: 'tenant',
    registerEnabled: true,
    ssoEnabled: false,
    status: 1
  }
]

const mockApplications: MockAppRecord[] = [
  {
    id: '10001',
    appName: '认证授权中心',
    appCode: 'admin-console',
    appType: 'WEB',
    entryUrl: 'http://localhost:5173',
    status: 1,
    realmId: 'realm_platform',
    clientSize: 3,
    instanceSize: 1,
    createTime: '2026-06-10 09:20:00',
    description: 'IAM 管理后台，用于维护身份域、账号、主体和授权策略。'
  },
  {
    id: '10002',
    appName: '商城应用',
    appCode: 'ecommerce',
    appType: 'WEB',
    entryUrl: 'https://mall.example.com',
    status: 1,
    realmId: 'realm_tenant_a',
    clientSize: 4,
    instanceSize: 2,
    createTime: '2026-06-11 14:05:00',
    description: '包含平台端、商家端、消费者 H5 与开放 API。'
  },
  {
    id: '10003',
    appName: '消费者移动端',
    appCode: 'mobile-app',
    appType: 'MOBILE',
    entryUrl: 'app://mall/home',
    status: 1,
    realmId: 'realm_tenant_a',
    clientSize: 2,
    instanceSize: 2,
    createTime: '2026-06-12 10:35:00',
    description: '小程序与 H5 移动登录入口测试数据。'
  },
  {
    id: '10004',
    appName: '开放平台',
    appCode: 'open-platform',
    appType: 'API',
    entryUrl: 'https://open.example.com',
    status: 1,
    realmId: 'realm_platform',
    clientSize: 2,
    instanceSize: 1,
    createTime: '2026-06-13 16:12:00',
    description: '面向第三方系统的 API 接入应用。'
  },
  {
    id: '10005',
    appName: '报表系统',
    appCode: 'report-system',
    appType: 'WEB',
    entryUrl: 'https://report.example.com',
    status: 2,
    realmId: 'realm_tenant_b',
    clientSize: 1,
    instanceSize: 1,
    createTime: '2026-06-14 11:40:00',
    description: '租户 B 的数据报表系统，当前禁用。'
  },
  {
    id: '10006',
    appName: '客户自有登录系统',
    appCode: 'customer-login',
    appType: 'THIRD_PARTY',
    entryUrl: 'https://customer.example.com/login',
    status: 1,
    realmId: 'realm_tenant_a',
    clientSize: 1,
    instanceSize: 1,
    createTime: '2026-06-15 08:30:00',
    description: '用于测试外部登录页跳转和 API_ONLY 接入。'
  }
]

const appTypeOptions = [
  { label: 'Web 应用', value: 'WEB' },
  { label: '移动应用', value: 'MOBILE' },
  { label: 'API 服务', value: 'API' },
  { label: '三方应用', value: 'THIRD_PARTY' }
]

const getAppTypeLabel = (type?: string) => {
  if (type === 'WEB') return 'Web 应用'
  if (type === 'MOBILE') return '移动应用'
  if (type === 'API') return 'API 服务'
  if (type === 'THIRD_PARTY') return '三方应用'
  return type || 'Web 应用'
}

const getAppTypeClass = (type?: string) => {
  if (type === 'WEB') return 'tag-web'
  if (type === 'MOBILE') return 'tag-mobile'
  if (type === 'API') return 'tag-api'
  return 'tag-web'
}

const getAppIconClass = (type?: string) => {
  if (type === 'WEB') return 'icon-web'
  if (type === 'MOBILE') return 'icon-mobile'
  if (type === 'API') return 'icon-api'
  return 'icon-web'
}

const getAppIcon = (type?: string) => {
  if (type === 'MOBILE') return Iphone
  if (type === 'API') return Coin
  if (type === 'THIRD_PARTY') return Tools
  return Monitor
}

const getMockRealmText = (row: AppRecord) => {
  const realmId = (row as MockAppRecord).realmId
  return mockRealms.find(item => item.id === realmId)?.name || '平台身份域'
}

const getStatusText = (status: number) => {
  return status === 1 ? '启用' : '禁用'
}

const fetchData = async () => {
  loading.value = true
  try {
    const nameKeyword = query.appName.trim().toLowerCase()
    const codeKeyword = query.appCode.trim().toLowerCase()
    const [startDate, endDate] = query.createTimeRange || []
    const filtered = mockApplications.filter(item => {
      const matchedName = !nameKeyword || item.appName.toLowerCase().includes(nameKeyword)
      const matchedCode = !codeKeyword || item.appCode.toLowerCase().includes(codeKeyword)
      const matchedType = !query.appType || item.appType === query.appType
      const matchedStatus = typeof query.status !== 'number' || item.status === query.status
      const matchedRealm = !query.realmId || item.realmId === query.realmId
      const createdAt = item.createTime ? new Date(item.createTime.replace(/-/g, '/')).getTime() : 0
      const matchedStart = !startDate || createdAt >= new Date(startDate).setHours(0, 0, 0, 0)
      const matchedEnd = !endDate || createdAt <= new Date(endDate).setHours(23, 59, 59, 999)
      return matchedName && matchedCode && matchedType && matchedStatus && matchedRealm && matchedStart && matchedEnd
    })
    total.value = filtered.length
    const start = (query.page - 1) * query.size
    tableData.value = filtered.slice(start, start + query.size)
  } finally {
    window.setTimeout(() => {
      loading.value = false
    }, 120)
  }
}

const fetchRealms = async () => {
  realmOptions.value = mockRealms
}

const handleReset = () => {
  query.page = 1
  query.appName = ''
  query.appCode = ''
  query.appType = ''
  query.status = undefined
  query.realmId = undefined
  query.createTimeRange = []
  fetchData()
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleOpenCreate = () => {
  router.push('/applications/create')
}

const toggleStatus = async (row: AppRecord) => {
  const enabled = row.status === 1
  const action = enabled ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}应用「${row.appName}」？`, '提示', { type: 'warning' })
    const target = mockApplications.find(item => item.id === row.id)
    if (target) {
      target.status = enabled ? 2 : 1
    }
    ElMessage.success(`应用已${action}`)
    fetchData()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || `${action}应用失败`)
  }
}

const handleExport = () => {
  ElMessage.success('数据导出成功')
}

onMounted(() => {
  fetchRealms()
  fetchData()
})
</script>

<template>
  <div class="application-list-page">
    <!-- Header layout -->
    <div class="page-header">
      <div class="header-left">
        <h1>应用管理</h1>
        <p>应用是系统的核心访问资源的载体，用于定义应用的基本信息、访问配置和安全策略。</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleOpenCreate">新增应用</el-button>
        <el-button :icon="Refresh" @click="fetchData" class="btn-secondary">刷新</el-button>
        <el-button :icon="Download" @click="handleExport" class="btn-secondary">导出</el-button>
      </div>
    </div>

    <!-- Filters layout -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-grid">
        <div class="filter-row-layout">
          <div class="filter-col">
            <span class="filter-label">应用名称</span>
            <el-input v-model="query.appName" placeholder="请输入应用名称" clearable class="input-styled" @keyup.enter="handleSearch" />
          </div>
          <div class="filter-col">
            <span class="filter-label">应用编码</span>
            <el-input v-model="query.appCode" placeholder="请输入应用编码" clearable class="input-styled" @keyup.enter="handleSearch" />
          </div>
          <div class="filter-col">
            <span class="filter-label">应用类型</span>
            <el-select v-model="query.appType" placeholder="全部" clearable class="select-styled">
              <el-option v-for="item in appTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
          <div class="filter-col">
            <span class="filter-label">状态</span>
            <el-select v-model="query.status" placeholder="全部" clearable class="select-styled">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </div>
        </div>

        <div class="filter-row-layout mt-16" v-if="isExpanded">
          <div class="filter-col">
            <span class="filter-label">所属身份域</span>
            <el-select v-model="query.realmId" placeholder="全部" clearable class="select-styled">
              <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>
          <div class="filter-col date-col">
            <span class="filter-label">创建时间</span>
            <el-date-picker
              v-model="query.createTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="date-picker-styled"
            />
          </div>
          <div class="filter-actions-col">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset" class="btn-secondary">重置</el-button>
            <el-button link class="btn-toggle-expand" @click="isExpanded = !isExpanded">
              收起
              <el-icon class="el-icon--right"><ArrowUp /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- Search Controls row shown ONLY when collapsed -->
        <div class="filter-actions-row mt-16" v-else>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset" class="btn-secondary">重置</el-button>
          <el-button link class="btn-toggle-expand" @click="isExpanded = !isExpanded">
            展开
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Table Card -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" border class="app-table">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="应用名称" min-width="180">
          <template #default="{ row }">
            <div class="app-name-cell">
              <div class="app-icon" :class="getAppIconClass(row.appType)">
                <el-icon><component :is="getAppIcon(row.appType)" /></el-icon>
              </div>
              <span class="app-name-txt">{{ row.appName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="appCode" label="应用编码" min-width="150" />
        <el-table-column label="应用类型" min-width="120">
          <template #default="{ row }">
            <span class="type-tag" :class="getAppTypeClass(row.appType)">
              {{ getAppTypeLabel(row.appType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="所属身份域" min-width="180">
          <template #default="{ row }">
            {{ getMockRealmText(row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-cell" :class="row.status === 1 ? 'status-enabled' : 'status-disabled'">
              <span class="dot"></span> {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" class="row-action-btn" @click="router.push(`/applications/detail/${row.id}`)">详情</el-button>
              <el-button link type="primary" class="row-action-btn" @click="router.push(`/applications/detail/${row.id}`)">编辑</el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary" class="row-action-btn">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <span class="total-text">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.application-list-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}



/* Page headings */
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
  color: #4b5563;
  font-size: 13px;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* Collapsible filters styling */
.filter-card {
  margin-bottom: 24px;
  border: 1px solid #eaecf0;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(16 24 40 / 4%);
}

.filter-card :deep(.el-card__body) {
  padding: 28px 24px 18px;
}

.filter-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-row-layout {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px;
}

.filter-col {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.second-filter-row {
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr) minmax(0, 1fr);
}

.date-col {
  grid-column: span 2;
}

.filter-label {
  width: auto;
  color: #344054;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.input-styled, .select-styled {
  width: 100%;
}

.input-styled :deep(.el-input__wrapper),
.select-styled :deep(.el-input__wrapper) {
  min-height: 34px;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 0 0 1px #d0d5dd inset;
}

.input-styled :deep(.el-input__wrapper.is-focus),
.select-styled :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1677ff inset;
}

.date-picker-styled {
  width: 100% !important;
  min-height: 34px;
  border-radius: 4px;
  background: #fff !important;
  box-shadow: 0 0 0 1px #d0d5dd inset !important;
}

.date-picker-styled :deep(.el-input__wrapper) {
  box-shadow: none !important;
}

.filter-actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
  margin-top: 16px;
}

.filter-actions-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.btn-toggle-expand {
  font-size: 13px;
  color: #2563eb;
}

/* Secondary Button style */
.btn-secondary {
  background-color: #ffffff !important;
  border: 1px solid #e5e7eb !important;
  color: #4b5563 !important;
  border-radius: 6px;
}
.btn-secondary:hover {
  border-color: #2563eb !important;
  color: #2563eb !important;
}

/* Primary buttons style */
:deep(.el-button--primary:not(.is-link)) {
  background-color: #2563eb !important;
  border-color: #2563eb !important;
  border-radius: 6px;
}
:deep(.el-button--primary:not(.is-link):hover),
:deep(.el-button--primary:not(.is-link):focus),
:deep(.el-button--primary:not(.is-link):active) {
  background-color: #1d4ed8 !important;
  border-color: #1d4ed8 !important;
}

/* Table styling */
.table-card {
  border: 1px solid #eaecf0;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(16 24 40 / 4%);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.app-table {
  border: none;
  background: #fff;
}

.app-table :deep(.el-table__inner-wrapper::before),
.app-table :deep(.el-table__border-left-patch) {
  display: none;
}

.app-table :deep(th.el-table__cell) {
  height: 54px;
  padding: 0;
  color: #344054;
  background: #f8fafc !important;
  border-right: none;
  border-bottom: none;
  font-size: 14px;
  font-weight: 600;
}

.app-table :deep(td.el-table__cell) {
  height: 66px;
  padding: 0;
  color: #344054;
  border-right: none;
  border-bottom: 1px solid #eaecf0;
  font-size: 14px;
}

.app-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f8fbff;
}

.app-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #ffffff;
}

/* Icons colored backgrounds matching design graph */
.icon-web {
  background-color: #3b82f6;
}
.icon-mobile {
  background-color: #f97316;
}
.icon-api {
  background-color: #a855f7;
}

.app-name-txt {
  font-weight: 500;
  color: #111827;
}

/* Type Tags matching Element Plus style in mockup */
.type-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}

.tag-web {
  background-color: #eff6ff;
  color: #2563eb;
  border: 1px solid #dbeafe;
}

.tag-mobile {
  background-color: #f0fdf4;
  color: #16a34a;
  border: 1px solid #dcfce7;
}

.tag-api {
  background-color: #faf5ff;
  color: #9333ea;
  border: 1px solid #f3e8ff;
}

/* Status colors */
.status-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 24px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-cell .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #9ca3af;
}

.status-enabled {
  background-color: #f0fdf4;
  color: #16a34a;
  border: 1px solid #dcfce7;
}
.status-enabled .dot {
  background-color: #16a34a;
}

.status-disabled {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fee2e2;
}
.status-disabled .dot {
  background-color: #dc2626;
}

.row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

/* Scoped row-action-btn overrides to prevent primary deep leakage */
.row-action-btn {
  color: #2563eb !important;
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
  height: auto !important;
  font-weight: 400 !important;
}
.row-action-btn:hover {
  color: #1d4ed8 !important;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0 0 0;
  border-top: none;
}

.total-text {
  font-size: 13px;
  color: #4b5563;
}

.mb-24 { margin-bottom: 24px; }
.mt-16 { margin-top: 16px; }
</style>
