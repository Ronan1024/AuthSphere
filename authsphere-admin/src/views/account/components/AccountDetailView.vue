<script setup lang="ts">
import { ref } from 'vue'
import { ArrowDown, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { accountApi, type AccountRecord } from '@/api/account'
import AccountSecurityTab from './AccountSecurityTab.vue'
import AccountThirdPartyTab from './AccountThirdPartyTab.vue'
import AccountSubjectTab from './AccountSubjectTab.vue'
import AccountClientRoleTab from './AccountClientRoleTab.vue'
import AccountLoginLogTab from './AccountLoginLogTab.vue'
import AccountActionLogTab from './AccountActionLogTab.vue'

const props = defineProps<{
  account: AccountRecord
}>()

const emit = defineEmits(['back', 'refresh', 'command'])

const activeTab = ref('basic')
const ACCOUNT_STATUS_LOCKED = 2
const ACCOUNT_STATUS_DISABLED = 3

const isEditing = ref(false)
const saving = ref(false)
const editForm = ref({
  username: '',
  nickname: '',
  mobile: '',
  email: ''
})

const handleBack = () => {
  if (isEditing.value) {
    isEditing.value = false
    return
  }
  emit('back')
}

const startEdit = () => {
  editForm.value = {
    username: props.account?.username || '',
    nickname: props.account?.nickname || '',
    mobile: props.account?.mobile || '',
    email: props.account?.email || ''
  }
  isEditing.value = true
  activeTab.value = 'basic' // Ensure we are on basic tab
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveEdit = async () => {
  if (!props.account?.id) return
  saving.value = true
  try {
    await accountApi.update(props.account.id, {
      username: editForm.value.username,
      nickname: editForm.value.nickname,
      mobile: editForm.value.mobile,
      email: editForm.value.email,
      realmId: props.account.realmId // realmId usually doesn't change, pass the current one
    })
    ElMessage.success('账号信息已更新')
    isEditing.value = false
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    saving.value = false
  }
}

const handleCommand = (command: string, row: AccountRecord) => {
  emit('command', command, row)
}
</script>

<template>
  <div class="account-detail-page">
    <!-- 顶部导航 -->
    <div class="page-header mb-4 flex-center">
      <el-button link :icon="ArrowLeft" @click="handleBack" class="mr-4 text-lg">返回</el-button>
      <h1 class="page-title">账号详情</h1>
    </div>

    <!-- 顶部名片 -->
    <el-card shadow="never" class="profile-card mb-4">
      <div class="profile-header">
        <div class="profile-left">
          <el-avatar :size="64" class="member-avatar">{{ account?.nickname?.substring(0, 1) || '李' }}</el-avatar>
          <div class="profile-info">
            <div class="name-row">
              <h2>{{ account?.username }}</h2>
              <span class="status-pill green" v-if="account?.status !== ACCOUNT_STATUS_DISABLED">启用</span>
              <span class="status-pill red" v-else>禁用</span>
            </div>
            <div class="meta-row">
              <span>{{ account?.realmName }} ({{ account?.realmCode || 'tenant_realm' }})</span>
              <span class="divider-dot">|</span>
              <span>账号ID：ACC-{{ account?.id?.slice(0, 5) }}</span>
              <span class="divider-dot">|</span>
              <span>最近登录：{{ account?.lastLoginTime || '2026-06-17 09:32' }}</span>
            </div>
          </div>
        </div>
        <div class="profile-actions">
          <template v-if="!isEditing">
            <el-button plain @click="handleCommand('reset-password', account)">重置密码</el-button>
            <el-button v-if="account?.status === ACCOUNT_STATUS_LOCKED" plain @click="handleCommand('unlock', account)">解锁</el-button>
            <el-button v-else-if="account?.status !== ACCOUNT_STATUS_DISABLED" plain @click="handleCommand('lock-account', account)">锁定</el-button>
            <el-button
              :type="account?.status === ACCOUNT_STATUS_DISABLED ? 'success' : 'danger'"
              plain
              @click="handleCommand(account?.status === ACCOUNT_STATUS_DISABLED ? 'enable' : 'disable', account)"
            >
              {{ account?.status === ACCOUNT_STATUS_DISABLED ? '启用' : '禁用' }}
            </el-button>
            <el-button type="primary" @click="startEdit">编辑</el-button>
          </template>
          <template v-else>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveEdit">保存更改</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="tabs-card">
      <!-- 页签区 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="基础信息" name="basic" />
        <el-tab-pane label="关联主体" name="subject" />
        <el-tab-pane label="可访问客户端" name="clients" />
        <el-tab-pane label="安全设置" name="security" />
        <el-tab-pane label="操作日志" name="action-log" />
        <el-tab-pane label="登录日志" name="login-log" />
      </el-tabs>

      <!-- 动态内容区 -->
      <div class="tab-content-area mt-4">
        <div v-if="activeTab === 'basic'">
          <!-- Metrics Row Inside Content -->
          <div class="content-metrics-row mb-6">
            <div class="c-metric-card">
              <span class="c-metric-val">{{ account?.subjectMemberCount ?? 2 }}</span>
              <span class="c-metric-lbl">关联主体</span>
            </div>
            <div class="c-metric-card">
              <span class="c-metric-val">3</span>
              <span class="c-metric-lbl">可访问客户端</span>
            </div>
            <div class="c-metric-card">
              <span class="c-metric-val">4</span>
              <span class="c-metric-lbl">已分配角色</span>
            </div>
            <div class="c-metric-card">
              <span class="c-metric-val">0</span>
              <span class="c-metric-lbl">当前锁定风险</span>
            </div>
          </div>

          <!-- Cards Grid -->
          <div class="basic-info-layout">
            <div class="info-card-panel">
              <h3>基础信息</h3>
              <div class="panel-rows">
                <div class="panel-row">
                  <label>用户名</label>
                  <div v-if="isEditing" class="edit-input-wrapper">
                    <el-input v-model="editForm.username" size="default" />
                  </div>
                  <span v-else>{{ account?.username }}</span>
                </div>
                <div class="panel-row">
                  <label>所属身份域</label>
                  <span>{{ account?.realmName }}</span>
                </div>
                <div class="panel-row">
                  <label>手机号</label>
                  <div v-if="isEditing" class="edit-input-wrapper">
                    <el-input v-model="editForm.mobile" size="default" />
                  </div>
                  <span>{{ account?.mobile || '-' }}</span>
                </div>
                <div class="panel-row">
                  <label>邮箱</label>
                  <div v-if="isEditing" class="edit-input-wrapper">
                    <el-input v-model="editForm.email" size="default" />
                  </div>
                  <span>{{ account?.email || '-' }}</span>
                </div>
                <div class="panel-row">
                  <label>状态</label>
                  <span class="status-pill green" v-if="account?.status !== ACCOUNT_STATUS_DISABLED">启用</span>
                  <span class="status-pill red" v-else>禁用</span>
                </div>
              </div>
            </div>

            <div class="info-card-panel">
              <h3>安全状态</h3>
              <div class="panel-rows">
                <div class="panel-row">
                  <label>密码状态</label>
                  <span>正常</span>
                </div>
                <div class="panel-row">
                  <label>登录失败次数</label>
                  <span>0 / 5</span>
                </div>
                <div class="panel-row">
                  <label>是否锁定</label>
                  <span>否</span>
                </div>
                <div class="panel-row">
                  <label>最近改密</label>
                  <span>2026-05-10 14:20</span>
                </div>
                <div class="panel-row">
                  <label>MFA</label>
                  <span class="status-pill orange">未绑定</span>
                </div>
              </div>
            </div>
          </div>
          <AccountClientRoleTab :account="account" class="mt-6" />
        </div>

        <AccountSubjectTab v-else-if="activeTab === 'subject'" :account="account" />
        <AccountThirdPartyTab v-else-if="activeTab === 'clients'" :account="account" />
        <AccountSecurityTab v-else-if="activeTab === 'security'" :account="account" />
        <AccountActionLogTab v-else-if="activeTab === 'action-log'" :account="account" />
        <AccountLoginLogTab v-else-if="activeTab === 'login-log'" :account="account" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

.account-detail-page {
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
  align-items: center;
  border-left: 3px solid var(--primary-color);
  padding-left: 10px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.profile-card {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.member-avatar {
  background-color: var(--bg-color);
  color: var(--primary-color);
  border: 1px solid rgba(14, 165, 233, 0.2);
  font-weight: 800;
  font-size: 28px;
  font-family: var(--font-family-display);
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name-row h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.divider-dot {
  color: #cbd5e1;
}

.profile-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tabs-card {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  min-height: 500px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--border-light);
}

.detail-tabs :deep(.el-tabs__item) {
  font-family: var(--font-family-display);
  font-weight: 600;
  color: var(--text-muted);
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

/* Metric row */
.content-metrics-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.c-metric-card {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: baseline;
  gap: 12px;
  background: #ffffff;
}

.c-metric-val {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.c-metric-lbl {
  font-size: 13px;
  color: var(--text-muted);
}

/* Details Panel layout */
.basic-info-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-card-panel {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 20px;
  background: #ffffff;
}

.info-card-panel h3 {
  margin: 0 0 16px 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.panel-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.panel-row label {
  color: var(--text-muted);
}

.panel-row span {
  font-weight: 600;
  color: var(--text-main);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

.status-pill.green {
  color: var(--success-color);
  background: var(--success-bg);
}

.status-pill.orange {
  color: #c2410c;
  background: #ffedd5;
}

.status-pill.red {
  color: var(--danger-color);
  background: #fee2e2;
}

.edit-input-wrapper {
  flex-grow: 1;
  max-width: 260px;
}

/* Utilities */
.mb-6 { margin-bottom: 24px; }
.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
.mt-6 { margin-top: 24px; }
.mr-4 { margin-right: 16px; }
.flex-center { display: flex; align-items: center; }
.text-lg { font-size: 16px; }

:deep(.el-card__body) {
  padding: 20px;
}
</style>
