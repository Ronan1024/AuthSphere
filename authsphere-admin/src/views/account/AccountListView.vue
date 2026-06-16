<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { Search, ArrowDown, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { accountApi, type AccountRecord } from '@/api/account'
import { realmApi, type RealmRecord } from '@/api/realm'

import CreateAccountDialog from './components/CreateAccountDialog.vue'
import AccountDetailView from './components/AccountDetailView.vue'
import ResetPasswordDialog from './components/ResetPasswordDialog.vue'
import BindThirdPartyDialog from './components/BindThirdPartyDialog.vue'
import LockAccountDialog from './components/LockAccountDialog.vue'

const ACCOUNT_STATUS_ENABLED = 1
const ACCOUNT_STATUS_LOCKED = 2
const ACCOUNT_STATUS_DISABLED = 3

const query = reactive({
  keyword: '',
  realmId: undefined as string | undefined,
  status: undefined as number | undefined,
  lockStatus: '',
  createTimeRange: [],
  loginTimeRange: [],
  page: 1,
  size: 10
})

const total = ref(0)
const loading = ref(false)
const tableData = ref<AccountRecord[]>([])
const realmOptions = ref<RealmRecord[]>([])

const selectedRows = ref<any[]>([])
const currentDetail = ref<AccountRecord | null>(null)

const createDrawerRef = ref()
const resetPasswordRef = ref()
const bindThirdPartyRef = ref()
const lockAccountRef = ref()

const maskMobile = (mobile?: string) => {
  if (!mobile || mobile.length < 7) return mobile || '-'
  return `${mobile.slice(0, 3)}****${mobile.slice(-4)}`
}

const maskEmail = (email?: string) => {
  if (!email) return '-'
  const [name, domain] = email.split('@')
  if (!domain) return email
  return `${name.slice(0, 2)}***@${domain}`
}

const getAccountDisplayName = (row: AccountRecord) => row.nickname || row.username

const loadRealms = async () => {
  try {
    const result = await realmApi.page({ page: 1, size: 100, status: 1 })
    realmOptions.value = result.records ?? []
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '身份域加载失败')
  }
}

const loadAccounts = async () => {
  loading.value = true
  try {
    const status = query.lockStatus === 'locked' ? ACCOUNT_STATUS_LOCKED : query.status
    const result = await accountApi.page({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      realmId: query.realmId,
      status,
    })
    tableData.value = result.records ?? []
    total.value = Number(result.total ?? 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadAccounts()
}

const resetQuery = () => {
  query.keyword = ''
  query.realmId = undefined
  query.status = undefined
  query.lockStatus = ''
  query.createTimeRange = []
  query.loginTimeRange = []
  handleSearch()
}

const openCreate = () => createDrawerRef.value?.open(realmOptions.value)
const openDetail = async (row: AccountRecord) => {
  try {
    currentDetail.value = await accountApi.detail(row.id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号详情加载失败')
  }
}
const closeDetail = () => currentDetail.value = null

const refreshCurrent = async () => {
  await loadAccounts()
  if (currentDetail.value) {
    currentDetail.value = await accountApi.detail(currentDetail.value.id)
  }
}

const handleCommand = (command: string, row: AccountRecord) => {
  if (command === 'reset-password') {
    resetPasswordRef.value?.open(row)
  } else if (command === 'bind-third-party') {
    bindThirdPartyRef.value?.open(row)
  } else if (command === 'lock-account') {
    lockAccountRef.value?.open(row)
  } else if (command === 'disable') {
    ElMessageBox.confirm(`确定要禁用账号 ${row.username} 吗？`, '提示', { type: 'warning' }).then(() => {
      return accountApi.toggleStatus(row.id)
    }).then(() => {
      ElMessage.success('已禁用')
      refreshCurrent()
    }).catch(() => {})
  } else if (command === 'enable') {
    ElMessageBox.confirm(`确定要启用账号 ${row.username} 吗？`, '提示', { type: 'success' }).then(() => {
      return accountApi.toggleStatus(row.id)
    }).then(() => {
      ElMessage.success('已启用')
      refreshCurrent()
    }).catch(() => {})
  } else if (command === 'unlock') {
    ElMessageBox.confirm(`确定要解锁账号 ${row.username} 吗？`, '提示', { type: 'success' }).then(() => {
      return accountApi.unlock(row.id)
    }).then(() => {
      ElMessage.success('已解锁')
      refreshCurrent()
    }).catch(() => {})
  }
}

watch(() => [query.page, query.size], loadAccounts)
onMounted(() => {
  loadRealms()
  loadAccounts()
})
</script>

<template>
  <section class="account-page">
    <template v-if="!currentDetail">
      <div class="page-header mb-4">
        <div class="header-titles">
          <h1>账号列表</h1>
          <p class="subtitle">管理系统中的所有账号，支持账号的新增、编辑、启用/禁用、锁定/解锁、密码重置等操作。</p>
        </div>
      </div>

    <!-- 筛选与操作区 -->
    <el-card shadow="never" class="account-filter-card mb-4">
      <el-form :inline="true" :model="query" class="compact-filter-form">
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="搜索账号/手机号/邮箱/用户名" clearable style="width: 260px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="所属身份域">
          <el-select v-model="query.realmId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="realm in realmOptions" :key="realm.id" :label="realm.name" :value="realm.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="ACCOUNT_STATUS_ENABLED" />
            <el-option label="禁用" :value="ACCOUNT_STATUS_DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="锁定状态">
          <el-select v-model="query.lockStatus" placeholder="全部" clearable style="width: 100px">
            <el-option label="正常" value="normal" />
            <el-option label="锁定" value="locked" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker v-model="query.createTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" />
        </el-form-item>
        <el-form-item label="最近登录时间">
          <el-date-picker v-model="query.loginTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="toolbar flex justify-end mb-4">
      <el-button plain><el-icon class="mr-1"><Download /></el-icon>导出</el-button>
      <el-dropdown class="ml-2 mr-2">
        <el-button>批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>批量导出</el-dropdown-item>
            <el-dropdown-item>批量禁用</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button type="primary" @click="openCreate">新建账号</el-button>
    </div>

    <!-- 数据表格区 -->
    <el-card shadow="never" class="account-table-card">
      <el-table v-loading="loading" :data="tableData" style="width: 100%" @selection-change="(val: any[]) => selectedRows = val">
        <el-table-column type="selection" width="55" />
        <el-table-column label="账号昵称" min-width="140">
          <template #default="{ row }">
            <div class="account-cell">
              <el-avatar :size="24" class="account-avatar mr-2">{{ getAccountDisplayName(row).substring(0, 1) }}</el-avatar>
              <span>{{ getAccountDisplayName(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="手机号" min-width="120">
          <template #default="{ row }">{{ maskMobile(row.mobile) }}</template>
        </el-table-column>
        <el-table-column label="邮箱" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ maskEmail(row.email) }}</template>
        </el-table-column>
        <el-table-column prop="realmName" label="所属身份域" min-width="130" show-overflow-tooltip />
        <el-table-column label="加入主体数" width="100">
          <template #default="{ row }">{{ row.subjectMemberCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="第三方绑定" width="110">
          <template #default="{ row }">{{ row.externalIdentityCount ?? 0 }}</template>
        </el-table-column>
        
        <el-table-column label="账号状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === ACCOUNT_STATUS_DISABLED ? 'danger' : 'success'" effect="light" size="small" round>
              {{ row.status === ACCOUNT_STATUS_DISABLED ? '禁用' : row.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="锁定状态" width="90">
          <template #default="{ row }">
            <span class="status-text" :class="row.status === ACCOUNT_STATUS_LOCKED ? 'text-red' : 'text-green'">
              {{ row.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '正常' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="lastLoginTime" label="最近登录时间" width="160" class-name="text-secondary text-sm" />
        <el-table-column prop="createTime" label="创建时间" width="160" class-name="text-secondary text-sm" />
        
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
                <el-button link type="primary">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="row.status !== ACCOUNT_STATUS_DISABLED" command="disable">禁用账号</el-dropdown-item>
                    <el-dropdown-item v-else command="enable">启用账号</el-dropdown-item>
                    <el-dropdown-item v-if="row.status === ACCOUNT_STATUS_LOCKED" command="unlock">解锁账号</el-dropdown-item>
                    <el-dropdown-item v-else-if="row.status !== ACCOUNT_STATUS_DISABLED" command="lock-account">锁定账号</el-dropdown-item>
                    <el-dropdown-item command="reset-password">重置密码</el-dropdown-item>
                    <el-dropdown-item command="bind-third-party">绑定第三方</el-dropdown-item>
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
          :page-sizes="[10, 20, 50, 100]"
          layout="prev, pager, next, sizes, jumper"
          :total="total"
        />
      </div>
    </el-card>
    </template>

    <template v-else>
      <AccountDetailView :account="currentDetail" @back="closeDetail" @refresh="refreshCurrent" />
    </template>

    <!-- Drawers & Dialogs -->
    <CreateAccountDialog ref="createDrawerRef" @success="handleSearch" />
    <ResetPasswordDialog ref="resetPasswordRef" @success="refreshCurrent" />
    <BindThirdPartyDialog ref="bindThirdPartyRef" />
    <LockAccountDialog ref="lockAccountRef" @success="refreshCurrent" />
  </section>
</template>

<style scoped>
.account-page {
  display: flex;
  flex-direction: column;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.subtitle {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.compact-filter-form {
  padding: 0;
}

.compact-filter-form :deep(.el-form-item) {
  margin-bottom: 16px;
  margin-right: 16px;
}

.account-filter-card {
  border: 1px solid #eaeaea;
  border-radius: 4px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.account-table-card {
  border: 1px solid #eaeaea;
  border-radius: 4px;
}

.account-cell {
  display: flex;
  align-items: center;
}

.account-avatar {
  background-color: #ffedd5;
  color: #ea580c;
  font-weight: 600;
  font-size: 12px;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.row-actions :deep(.el-button) {
  margin-left: 0;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #eaeaea;
}

/* Utilities */
.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
.mr-1 { margin-right: 4px; }
.mr-2 { margin-right: 8px; }
.ml-2 { margin-left: 8px; }
.w-240 { width: 240px; }
.w-120 { width: 120px; }
.w-100 { width: 100px; }
.flex-grow { flex-grow: 1; }
.text-green { color: #16a34a; font-weight: 500; }
.text-red { color: #dc2626; font-weight: 500; }
.text-secondary { color: #6b7280; }
.text-sm { font-size: 13px; }
.total-text { font-size: 13px; color: #6b7280; }

.flex { display: flex; }
.justify-end { justify-content: flex-end; }

:deep(.el-card__body) {
  padding: 0;
}
.account-table-card :deep(.el-card__body) {
  padding: 16px 20px 0;
}
.account-filter-card :deep(.el-card__body) {
  padding: 16px 20px 0;
}
</style>
