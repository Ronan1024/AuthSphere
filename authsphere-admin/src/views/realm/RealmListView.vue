<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Plus, Search, Refresh, Setting, ArrowDown, OfficeBuilding, User, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

import { realmApi, type RealmRecord } from '@/api/realm'
import { typeCategoryApi, type TypeCategoryRecord } from '@/api/typeCategory'

import CreateRealmDrawer from './components/CreateRealmDrawer.vue'
import RealmDetailView from './components/RealmDetailView.vue'

const loading = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const createDrawerRef = ref<InstanceType<typeof CreateRealmDrawer>>()

const selectedRealm = ref<RealmRecord>()

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

const tableData = ref<RealmRecord[]>([])
const total = ref(0)
const typeCategoryOptions = ref<TypeCategoryRecord[]>([])

// Mock Account Counts & unique scope for display
const getMockData = (id: string) => {
  const num = parseInt(id.replace(/\D/g, '') || '0')
  return {
    accountCount: (num % 5000) + 38,
    uniqueScope: 'Realm 内唯一'
  }
}

const typeCategoryText = (typeCategoryId?: string | number | null) => {
  if (!typeCategoryId) return '-'
  const match = typeCategoryOptions.value.find(item => String(item.id) === String(typeCategoryId))
  return match ? match.name : String(typeCategoryId)
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await realmApi.page({
      page: query.page,
      size: query.size,
      name: query.keyword, // Use keyword for name search temporarily
      status: query.status
    })
    tableData.value = result.records || []
    total.value = result.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取身份域列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTypeCategories = async () => {
  try {
    const result = await typeCategoryApi.list()
    typeCategoryOptions.value = Array.isArray(result) ? result : []
  } catch (error) {}
}

const openCreateDialog = () => {
  createDrawerRef.value?.open()
}

const openDetail = (row: RealmRecord) => {
  selectedRealm.value = row
}

const closeDetail = () => {
  selectedRealm.value = undefined
  fetchData()
}

const toggleStatus = async (row: RealmRecord) => {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}该身份域？`, '提示', { type: 'warning' })
  try {
    await realmApi.toggleStatus(row.id)
    ElMessage.success(`身份域已${action}`)
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || `${action}失败`)
  }
}

const init = () => {
  fetchTypeCategories()
  fetchData()
}

init()
</script>

<template>
  <section class="realm-page">
    <template v-if="selectedRealm">
      <RealmDetailView 
        :realm="selectedRealm" 
        @back="closeDetail" 
        @edit="() => {}"
      />
    </template>

    <template v-else>
      <div class="page-heading">
        <div class="heading-text">
          <h1>身份域列表</h1>
          <p>统一账号空间的划分与管理，定义不同身份领域的登录策略、安全策略和账号唯一性规则。</p>
        </div>
      </div>

      <el-card shadow="never" class="realm-table-card">
        <div class="table-toolbar">
          <div class="toolbar-left">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增身份域</el-button>
            <el-dropdown class="ml-2">
              <el-button>批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>导出列表</el-dropdown-item>
                  <el-dropdown-item>批量启用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="toolbar-right">
            <el-input
              v-model="query.keyword"
              class="filter-input"
              clearable
              placeholder="搜索身份域名称 / 编码"
              :prefix-icon="Search"
              @keyup.enter="fetchData"
            />
            <el-button class="icon-btn" :icon="Setting">列设置</el-button>
            <el-button class="icon-btn" :icon="Refresh" @click="fetchData">刷新</el-button>
          </div>
        </div>

        <el-table v-loading="loading" :data="tableData" row-key="id">
          <el-table-column label="身份域名称" min-width="180">
            <template #default="{ row }">
              <div class="realm-name-cell">
                <div class="realm-icon">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div class="realm-info">
                  <strong>{{ row.name }}</strong>
                  <span>{{ row.description || '无描述' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="code" label="身份域编码" min-width="140" />
          <el-table-column label="身份域类型" min-width="120">
            <template #default="{ row }">
              <el-tag size="small" type="primary" effect="plain">{{ typeCategoryText(row.typeCategoryId) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="账号唯一性范围" min-width="140">
            <template #default="{ row }">
              <el-tag size="small" type="success" effect="plain">{{ getMockData(row.id).uniqueScope }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="账号数量" min-width="100">
            <template #default="{ row }">
              {{ getMockData(row.id).accountCount.toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column label="登录方式" min-width="120">
            <template #default>
              <div class="login-methods-icons">
                <el-icon class="text-primary"><User /></el-icon>
                <el-icon class="text-primary"><Iphone /></el-icon>
                <el-icon class="text-primary"><Message /></el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="status-dot" :class="row.status === 1 ? 'is-active' : ''"></span>
              {{ row.status === 1 ? '启用' : '禁用' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button link type="primary" @click="openDetail(row)">编辑</el-button>
                <el-button link type="primary" @click="openDetail(row)">详情</el-button>
                <el-dropdown trigger="click">
                  <el-button link type="primary">
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
            layout="sizes, prev, pager, next"
            :total="total"
            @change="fetchData"
          />
        </div>
      </el-card>
    </template>

    <CreateRealmDrawer ref="createDrawerRef" @success="fetchData" />
  </section>
</template>

<style scoped>
.realm-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-heading {
  margin-bottom: 24px;
}

.heading-text h1 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.heading-text p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.realm-table-card {
  border: 1px solid #eaeaea;
  border-radius: 4px;
}

.realm-table-card :deep(.el-card__body) {
  padding: 0;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eaeaea;
}

.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-input {
  width: 240px;
}

.icon-btn {
  padding: 8px 12px;
}

.realm-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.realm-icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background: #fdf2f8;
  color: #db2777;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.realm-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.realm-info strong {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.realm-info span {
  font-size: 12px;
  color: #6b7280;
}

.login-methods-icons {
  display: flex;
  gap: 8px;
  font-size: 16px;
}

.text-primary {
  color: #000000;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #9ca3af;
  margin-right: 6px;
  vertical-align: middle;
}

.status-dot.is-active {
  background-color: #16a34a;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #eaeaea;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}

.ml-2 { margin-left: 8px; }
</style>
