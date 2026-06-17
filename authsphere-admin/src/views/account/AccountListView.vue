<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowDown, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

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

const openCreate = () => {
  router.push('/accounts/create')
}
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
          <p class="subtitle">查询、查看、编辑、重置密码、禁用、解锁</p>
        </div>
        <div class="header-actions">
          <el-button plain @click="() => {}"><el-icon class="mr-1"><Download /></el-icon>导出</el-button>
          <el-button type="primary" @click="openCreate">新增账号</el-button>
        </div>
      </div>

      <!-- 筛选与操作区 -->
      <el-card shadow="never" class="account-filter-card mb-4">
        <div class="filter-flex-row">
          <div class="filter-item">
            <label>关键词</label>
            <el-input v-model="query.keyword" placeholder="用户名 / 手机号 / 邮箱" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="filter-item">
            <label>所属身份域</label>
            <el-select v-model="query.realmId" placeholder="全部身份域" clearable>
              <el-option v-for="realm in realmOptions" :key="realm.id" :label="realm.name" :value="realm.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>账号状态</label>
            <el-select v-model="query.status" placeholder="全部" clearable>
              <el-option label="启用" :value="ACCOUNT_STATUS_ENABLED" />
              <el-option label="禁用" :value="ACCOUNT_STATUS_DISABLED" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>主体关系</label>
            <el-select v-model="query.lockStatus" placeholder="全部" clearable>
              <el-option label="全部" value="" />
              <el-option label="已加入" value="normal" />
              <el-option label="未加入" value="locked" />
            </el-select>
          </div>
          <div class="filter-buttons">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- 数据表格区 -->
      <el-card shadow="never" class="account-table-card">
        <el-table v-loading="loading" :data="tableData" style="width: 100%">
          <el-table-column label="账号" min-width="140">
            <template #default="{ row }">
              <div class="account-user-col">
                <span class="username-text" @click="openDetail(row)">{{ row.username }}</span>
                <span class="acc-code-badge">ACC-{{ row.id.slice(0, 5) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="登录凭证" min-width="160">
            <template #default="{ row }">
              <div class="credentials-stack">
                <span class="mobile-text">{{ maskMobile(row.mobile) }}</span>
                <small class="email-text" v-if="row.email">{{ maskEmail(row.email) }}</small>
                <small class="email-text" v-else>-</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="所属身份域" min-width="160">
            <template #default="{ row }">
              <div class="realm-stack">
                <span class="realm-name-text">{{ row.realmName }}</span>
                <span class="realm-code-badge">{{ row.realmCode || 'tenant_realm' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="关联主体" min-width="150">
            <template #default="{ row }">
              <div class="relation-subject-stack" v-if="row.subjectMemberCount > 0">
                <span class="subject-outline-badge">
                  {{ row.realmCode === 'platform_realm' ? '平台主体' : row.realmCode === 'merchant_realm' ? '商户W' : '租户A' }}
                </span>
                <span class="subject-count-badge">{{ row.subjectMemberCount }}个主体</span>
              </div>
              <span v-else class="unjoined-text">未加入主体</span>
            </template>
          </el-table-column>
          <el-table-column label="客户端角色" min-width="130">
            <template #default="{ row }">
              <div class="client-role-badge" v-if="row.username === 'lisi'">3个客户端</div>
              <div class="client-role-badge" v-else-if="row.username === 'wangwu'">2个客户端</div>
              <div class="client-role-badge" v-else-if="row.username === 'old_admin'">1个客户端</div>
              <span v-else class="unjoined-text">无</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="status-pill green" v-if="row.status === ACCOUNT_STATUS_ENABLED">启用</span>
              <span class="status-pill orange" v-else-if="row.status === ACCOUNT_STATUS_LOCKED">锁定</span>
              <span class="status-pill red" v-else>禁用</span>
            </template>
          </el-table-column>
          <el-table-column label="最近登录" min-width="160">
            <template #default="{ row }">
              <span class="time-text">{{ row.lastLoginTime || '2026-06-17 09:32' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right" align="right">
            <template #default="{ row }">
              <div class="row-actions">
                <span class="link-btn" @click="openDetail(row)">详情</span>
                <span class="link-btn" @click="openDetail(row)">编辑</span>
                <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
                  <span class="link-more-btn">更多 <el-icon><ArrowDown /></el-icon></span>
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
      <AccountDetailView :account="currentDetail" @back="closeDetail" @refresh="refreshCurrent" @command="handleCommand" />
    </template>

    <!-- Drawers & Dialogs -->
    <CreateAccountDialog ref="createDrawerRef" @success="handleSearch" />
    <ResetPasswordDialog ref="resetPasswordRef" @success="refreshCurrent" />
    <BindThirdPartyDialog ref="bindThirdPartyRef" />
    <LockAccountDialog ref="lockAccountRef" @success="refreshCurrent" />
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

.account-page {
  --primary-color: #0369A1;      /* Security Blue */
  --primary-hover: #0284c7;
  --secondary-color: #0EA5E9;    /* Sky Blue */
  --success-color: #16A34A;      /* Protected Green */
  --success-bg: #dcfce7;
  --success-border: #bbf7d0;
  --danger-color: #dc2626;
  --bg-color: #F0F9FF;           /* Theme Background */
  --text-main: #0C4A6E;          /* Deep Navy Text */
  --text-muted: #475569;
  --border-light: rgba(226, 232, 240, 0.8);
  --font-family-display: 'Lexend', system-ui, -apple-system, sans-serif;
  --font-family-body: 'Source Sans 3', system-ui, -apple-system, sans-serif;

  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-family-body);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.subtitle {
  margin: 6px 0 0 0;
  font-size: 14px;
  color: var(--text-muted);
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* Filters Grid styling */
.account-filter-card {
  border-radius: 10px;
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.filter-flex-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr) auto;
  gap: 16px;
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  font-size: 13px;
  color: var(--text-main);
  font-weight: 600;
  font-family: var(--font-family-display);
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

/* Table styling */
.account-table-card {
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.account-user-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username-text {
  font-weight: 700;
  color: var(--text-main);
  cursor: pointer;
  transition: color 0.15s;
}

.username-text:hover {
  color: var(--primary-color);
}

.acc-code-badge {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  background: #f1f5f9;
  border: 1px solid rgba(226, 232, 240, 0.8);
  color: var(--text-muted);
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  width: max-content;
}

.credentials-stack {
  display: flex;
  flex-direction: column;
}

.mobile-text {
  color: var(--text-main);
  font-weight: 500;
}

.email-text {
  color: var(--text-muted);
  font-size: 12px;
}

.realm-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.realm-name-text {
  color: var(--text-main);
}

.realm-code-badge {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  background: #fafcff;
  border: 1px solid rgba(14, 165, 233, 0.1);
  color: var(--secondary-color);
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  width: max-content;
}

.relation-subject-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.subject-outline-badge {
  color: var(--primary-color);
  background: var(--bg-color);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: 600;
  width: max-content;
}

.subject-count-badge {
  background: #f1f5f9;
  color: var(--text-muted);
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  width: max-content;
}

.unjoined-text {
  color: var(--text-muted);
  font-size: 12px;
}

.client-role-badge {
  color: var(--success-color);
  background: var(--success-bg);
  border: 1px solid var(--success-border);
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  width: max-content;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid transparent;
}

.status-pill.green {
  color: var(--success-color);
  background: var(--success-bg);
  border-color: var(--success-border);
}

.status-pill.orange {
  color: #c2410c;
  background: #ffedd5;
  border-color: #fed7aa;
}

.status-pill.red {
  color: var(--danger-color);
  background: #fee2e2;
  border-color: #fecaca;
}

.time-text {
  color: var(--text-muted);
  font-size: 13px;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
}

.link-btn {
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 13px;
  transition: color 0.2s ease;
}

.link-btn:hover {
  color: var(--primary-hover);
}

.link-more-btn {
  cursor: pointer;
  color: var(--text-muted);
  font-weight: 600;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s ease;
}

.link-more-btn:hover {
  color: var(--primary-color);
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding: 16px 0 0 0;
  border-top: 1px solid rgba(241, 245, 249, 0.8);
}

.total-text {
  font-size: 13px;
  color: var(--text-muted);
}

.mb-4 { margin-bottom: 16px; }
.mr-1 { margin-right: 4px; }

:deep(.el-card__body) {
  padding: 20px;
}

@media (max-width: 1400px) {
  .filter-flex-row {
    grid-template-columns: 1fr 1fr;
  }
  .filter-buttons {
    grid-column: span 2;
    justify-content: flex-end;
  }
}
</style>
