<script setup lang="ts">
import { ref } from 'vue'
import { ArrowDown, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { accountApi, type AccountRecord } from '@/api/account'
import AccountSecurityTab from './AccountSecurityTab.vue'
import AccountThirdPartyTab from './AccountThirdPartyTab.vue'
import AccountSubjectTab from './AccountSubjectTab.vue'
import AccountLoginLogTab from './AccountLoginLogTab.vue'
import AccountActionLogTab from './AccountActionLogTab.vue'

const props = defineProps<{
  account: AccountRecord
}>()

const emit = defineEmits(['back', 'refresh'])

const activeTab = ref('basic')
const ACCOUNT_STATUS_ENABLED = 1
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
</script>

<template>
  <div class="account-detail-page">
    <!-- 顶部导航 -->
    <div class="page-header mb-6 flex-center">
      <el-button link :icon="ArrowLeft" @click="handleBack" class="mr-4 text-lg">返回</el-button>
      <h1 class="page-title">账号详情</h1>
    </div>

    <!-- 顶部名片 -->
    <el-card shadow="never" class="profile-card mb-4">
      <div class="profile-header">
        <div class="profile-left">
          <el-avatar :size="64" class="member-avatar">{{ account?.nickname?.substring(0, 1) || 'U' }}</el-avatar>
          <div class="profile-info">
            <div class="name-row">
              <h2>{{ account?.nickname }}</h2>
              <el-tag :type="account?.status === ACCOUNT_STATUS_DISABLED ? 'danger' : 'success'" effect="light" size="small" round>
                {{ account?.status === ACCOUNT_STATUS_DISABLED ? '禁用' : account?.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '启用' }}
              </el-tag>
              <el-tag :type="account?.status === ACCOUNT_STATUS_LOCKED ? 'danger' : 'success'" effect="plain" size="small" round>
                {{ account?.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '正常' }}
              </el-tag>
            </div>
            <div class="meta-row">
              <span>{{ account?.username }}</span>
              <span class="divider"></span>
              <span>{{ account?.mobile || '-' }}</span>
              <span class="divider"></span>
              <span>{{ account?.email || '-' }}</span>
            </div>
          </div>
        </div>
        <div class="profile-actions">
          <template v-if="!isEditing">
            <el-button type="primary" plain @click="startEdit">编辑基本信息</el-button>
            <el-dropdown trigger="click" class="ml-2">
              <el-button>
                更多操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>重置密码</el-dropdown-item>
                  <el-dropdown-item>锁定账号</el-dropdown-item>
                  <el-dropdown-item divided type="danger">禁用账号</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
        <el-tab-pane label="基本信息" name="basic" />
        <el-tab-pane label="登录与安全" name="security" />
        <el-tab-pane label="第三方账号" name="third-party" />
        <el-tab-pane label="加入的主体" name="subject" />
        <el-tab-pane label="登录日志" name="login-log" />
        <el-tab-pane label="操作日志" name="action-log" />
      </el-tabs>

      <!-- 动态内容区 -->
      <div class="tab-content-area mt-6">
        <div v-if="activeTab === 'basic'" class="basic-info-layout">
          <div class="section-title">基本信息</div>
          <div class="info-grid">
            <div class="info-item"><span>账号ID</span><strong>{{ account?.id }}</strong></div>
            <div class="info-item"><span>账号状态</span><strong class="text-green">{{ account?.status === ACCOUNT_STATUS_DISABLED ? '禁用' : account?.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '启用' }}</strong></div>
            <div class="info-item">
              <span>用户名</span>
              <div v-if="isEditing" class="edit-input-wrapper"><el-input v-model="editForm.username" size="small" /></div>
              <strong v-else>{{ account?.username }}</strong>
            </div>
            <div class="info-item"><span>锁定状态</span><strong>{{ account?.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '正常' }}</strong></div>
            <div class="info-item">
              <span>昵称</span>
              <div v-if="isEditing" class="edit-input-wrapper"><el-input v-model="editForm.nickname" size="small" /></div>
              <strong v-else>{{ account?.nickname || '-' }}</strong>
            </div>
            <div class="info-item"><span>最近登录时间</span><strong>{{ account?.lastLoginTime || '-' }}</strong></div>
            <div class="info-item">
              <span>手机号</span>
              <div v-if="isEditing" class="edit-input-wrapper"><el-input v-model="editForm.mobile" size="small" /></div>
              <strong v-else>{{ account?.mobile || '-' }}</strong>
            </div>
            <div class="info-item"><span>加入主体数</span><strong>{{ account?.subjectMemberCount ?? 0 }}</strong></div>
            <div class="info-item">
              <span>邮箱</span>
              <div v-if="isEditing" class="edit-input-wrapper"><el-input v-model="editForm.email" size="small" /></div>
              <strong v-else>{{ account?.email || '-' }}</strong>
            </div>
            <div class="info-item"><span>第三方绑定数</span><strong>{{ account?.externalIdentityCount ?? 0 }}</strong></div>
            <div class="info-item"><span>所属身份域</span><strong>{{ account?.realmName || '-' }}</strong></div>
            <div class="info-item"><span>创建时间</span><strong>{{ account?.createTime || '-' }}</strong></div>
            <div class="info-item"><span>更新时间</span><strong>{{ account?.updateTime || '-' }}</strong></div>
          </div>
        </div>

        <AccountSecurityTab v-else-if="activeTab === 'security'" :account="account" />
        <AccountThirdPartyTab v-else-if="activeTab === 'third-party'" :account="account" />
        <AccountSubjectTab v-else-if="activeTab === 'subject'" :account="account" />
        <AccountLoginLogTab v-else-if="activeTab === 'login-log'" :account="account" />
        <AccountActionLogTab v-else-if="activeTab === 'action-log'" :account="account" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.account-detail-page {
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.profile-card {
  border: 1px solid #eef2f7;
  border-radius: 8px;
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
  background-color: #ffedd5;
  color: #ea580c;
  font-weight: 600;
  font-size: 28px;
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
  font-weight: 600;
  color: #111827;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 14px;
}

.divider {
  width: 1px;
  height: 12px;
  background-color: #d1d5db;
}

.profile-actions {
  display: flex;
  align-items: center;
}

.tabs-card {
  border: 1px solid #eef2f7;
  border-radius: 8px;
  min-height: 500px;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eef2f7;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px 40px;
  max-width: 1000px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
}

.info-item span {
  width: 120px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-item strong {
  color: #111827;
  font-weight: 400;
  flex-grow: 1;
}

.edit-input-wrapper {
  flex-grow: 1;
  max-width: 300px;
}

/* Utilities */
.mb-6 { margin-bottom: 24px; }
.mb-4 { margin-bottom: 16px; }
.mt-6 { margin-top: 24px; }
.ml-2 { margin-left: 8px; }
.mr-4 { margin-right: 16px; }
.flex-center { display: flex; align-items: center; }
.text-lg { font-size: 16px; }
.text-green { color: #16a34a !important; font-weight: 500 !important; }
</style>
