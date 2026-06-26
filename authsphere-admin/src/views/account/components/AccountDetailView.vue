<script setup lang="ts">
import { ref } from 'vue'
import { ArrowDown, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { accountApi, type AccountRecord } from '@/api/account'
import AccountSecurityTab from './AccountSecurityTab.vue'
import AccountThirdPartyTab from './AccountThirdPartyTab.vue'
import AccountSubjectTab from './AccountSubjectTab.vue'
import AccountClientRoleTab from './AccountClientRoleTab.vue'
import AccountLoginLogTab from './AccountLoginLogTab.vue'
import AccountActionLogTab from './AccountActionLogTab.vue'

const associatedSubjects = ref([
  {
    id: '1',
    subjectName: '租户A',
    subjectCode: 'tenant_a',
    subjectType: '租户',
    realmName: '租户身份域',
    memberType: '主体管理员',
    isDefault: true,
    status: '启用',
    joinedTime: '2026-05-01'
  },
  {
    id: '2',
    subjectName: '支付服务商A',
    subjectCode: 'provider_a',
    subjectType: '服务商',
    realmName: '服务商身份域',
    memberType: '成员',
    isDefault: false,
    status: '启用',
    joinedTime: '2026-06-02'
  }
])

const handleSetDefault = (row: any) => {
  associatedSubjects.value.forEach(sub => {
    sub.isDefault = sub.id === row.id
  })
  ElMessage.success('已设为默认主体')
}

const handleRemoveSubject = (row: any) => {
  ElMessageBox.confirm(`确定要移除与主体 ${row.subjectName} 的关联吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    associatedSubjects.value = associatedSubjects.value.filter(sub => sub.id !== row.id)
    ElMessage.success('已移除主体关联')
  }).catch(() => {})
}

const joinDialogVisible = ref(false)
const joinForm = ref({
  subjectId: '',
  memberType: 'admin', // 'admin' | 'member'
  isDefault: 'no', // 'yes' | 'no'
  remark: ''
})

const subjectOptions = [
  { id: '1', labelName: '租户A tenant_a', subjectName: '租户A', subjectCode: 'tenant_a', subjectType: '租户', realmName: '租户身份域' },
  { id: '2', labelName: '支付服务商A provider_a', subjectName: '支付服务商A', subjectCode: 'provider_a', subjectType: '服务商', realmName: '服务商身份域' },
  { id: '3', labelName: '电商分拨中心B distribution_b', subjectName: '电商分拨中心B', subjectCode: 'distribution_b', subjectType: '分部', realmName: '租户身份域' }
]

const handleJoinSubjectClick = () => {
  joinForm.value = {
    subjectId: '1',
    memberType: 'admin',
    isDefault: 'no',
    remark: '租户A后台管理员。'
  }
  joinDialogVisible.value = true
}

const handleSaveJoinSubject = () => {
  const selectedSub = subjectOptions.find(sub => sub.id === joinForm.value.subjectId)
  if (!selectedSub) {
    ElMessage.error('请选择主体')
    return
  }

  if (associatedSubjects.value.some(sub => sub.subjectCode === selectedSub.subjectCode)) {
    ElMessage.warning('该账号已加入此主体')
    joinDialogVisible.value = false
    return
  }

  const isDefaultBool = joinForm.value.isDefault === 'yes'

  if (isDefaultBool) {
    associatedSubjects.value.forEach(sub => {
      sub.isDefault = false
    })
  }

  associatedSubjects.value.push({
    id: String(associatedSubjects.value.length + 1),
    subjectName: selectedSub.subjectName,
    subjectCode: selectedSub.subjectCode,
    subjectType: selectedSub.subjectType,
    realmName: selectedSub.realmName,
    memberType: joinForm.value.memberType === 'admin' ? '主体管理员' : '成员',
    isDefault: isDefaultBool,
    status: '启用',
    joinedTime: new Date().toISOString().substring(0, 10)
  })

  ElMessage.success('成功加入主体')
  joinDialogVisible.value = false
}

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

const onlineSessions = ref([
  { client: '商城平台端', subject: '租户A', ipDevice: '10.1.2.8 / Chrome', lastActive: '5 分钟前' },
  { client: '支付平台端', subject: '租户A', ipDevice: '10.1.2.8 / Chrome', lastActive: '22 分钟前' }
])

const handleOffline = (row: any) => {
  ElMessageBox.confirm('确定要强制下线该会话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    onlineSessions.value = onlineSessions.value.filter(s => s.client !== row.client)
    ElMessage.success('会话已强制下线')
  }).catch(() => {})
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
        <el-tab-pane label="登录日志" name="login-log" />
      </el-tabs>

      <!-- 动态内容区 -->
      <div class="tab-content-area mt-4">
        <div v-if="activeTab === 'basic'">
          <!-- Metrics Row Inside Content -->
          <div class="content-metrics-row mb-6">
            <div class="c-metric-card">
              <span class="c-metric-val">{{ account?.subjectMemberCount ?? 0 }}</span>
              <span class="c-metric-lbl">关联主体</span>
            </div>
            <div class="c-metric-card">
              <span class="c-metric-val">{{ account?.externalIdentityCount ?? 0 }}</span>
              <span class="c-metric-lbl">第三方绑定</span>
            </div>
            <div class="c-metric-card">
              <span
                class="c-metric-val"
                :class="account?.status === ACCOUNT_STATUS_LOCKED ? 'text-warning' : account?.status === ACCOUNT_STATUS_DISABLED ? 'text-danger' : ''"
              >
                {{ account?.status === ACCOUNT_STATUS_LOCKED ? '锁定' : account?.status === ACCOUNT_STATUS_DISABLED ? '禁用' : '启用' }}
              </span>
              <span class="c-metric-lbl">账号状态</span>
            </div>
            <div class="c-metric-card">
              <span class="c-metric-val metric-time">{{ account?.lastLoginTime || '-' }}</span>
              <span class="c-metric-lbl">最近登录</span>
            </div>
          </div>

          <!-- Cards Grid -->
          <div class="basic-info-layout">
            <div class="info-card-panel">
              <div class="panel-header">
                <h3>账号资料</h3>
                <p>来自 `account` 表的基础信息</p>
              </div>
              <div class="panel-rows">
                <div class="panel-row">
                  <label>账号 ID</label>
                  <span class="mono-text">{{ account?.id || '-' }}</span>
                </div>
                <div class="panel-row">
                  <label>用户名</label>
                  <div v-if="isEditing" class="edit-input-wrapper">
                    <el-input v-model="editForm.username" size="default" />
                  </div>
                  <span v-else>{{ account?.username }}</span>
                </div>
                <div class="panel-row">
                  <label>昵称</label>
                  <div v-if="isEditing" class="edit-input-wrapper">
                    <el-input v-model="editForm.nickname" size="default" />
                  </div>
                  <span v-else>{{ account?.nickname || '-' }}</span>
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
                  <span class="status-pill green" v-if="account?.status === 1">启用</span>
                  <span class="status-pill orange" v-else-if="account?.status === ACCOUNT_STATUS_LOCKED">锁定</span>
                  <span class="status-pill red" v-else>禁用</span>
                </div>
              </div>
            </div>

            <div class="info-card-panel">
              <div class="panel-header">
                <h3>凭证状态</h3>
                <p>按账号状态展示当前可判断的登录信息</p>
              </div>
              <div class="panel-rows">
                <div class="panel-row">
                  <label>登录凭证</label>
                  <span>-</span>
                </div>
                <div class="panel-row">
                  <label>锁定状态</label>
                  <span>{{ account?.status === ACCOUNT_STATUS_LOCKED ? '已锁定' : '正常' }}</span>
                </div>
                <div class="panel-row">
                  <label>创建时间</label>
                  <span>{{ account?.createTime || '-' }}</span>
                </div>
                <div class="panel-row">
                  <label>更新时间</label>
                  <span>{{ account?.updateTime || '-' }}</span>
                </div>
                <div class="panel-row">
                  <label>最近登录</label>
                  <span>{{ account?.lastLoginTime || '-' }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 账号详情 / 关联主体 Card -->
          <div class="subject-association-card mt-6">
            <div class="card-header-flex">
              <div class="header-left-group">
                <span class="card-title">账号详情 / 关联主体</span>
                <span class="card-subtitle">账号加入主体后，才可以代表该主体进入业务系统</span>
              </div>
              <el-button type="primary" @click="handleJoinSubjectClick">加入主体</el-button>
            </div>

            <el-table :data="associatedSubjects" style="width: 100%; margin-top: 16px;">
              <el-table-column label="主体" min-width="150">
                <template #default="{ row }">
                  <div class="subject-name-cell">
                    <strong>{{ row.subjectName }}</strong>
                    <span class="code-inline-badge">{{ row.subjectCode }}</span>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column label="主体类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.subjectType === '租户' ? 'primary' : 'info'" size="default">
                    {{ row.subjectType }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column prop="realmName" label="身份域" min-width="120" />
              <el-table-column prop="memberType" label="成员类型" width="120" />
              
              <el-table-column label="是否默认" width="100">
                <template #default="{ row }">
                  <el-tag type="success" v-if="row.isDefault" size="default">默认</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <span class="status-pill green" v-if="row.status === '启用'">启用</span>
                  <span class="status-pill red" v-else>禁用</span>
                </template>
              </el-table-column>
              
              <el-table-column prop="joinedTime" label="加入时间" width="140" />
              
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <div class="action-buttons-flex">
                    <span class="action-btn-link" v-if="!row.isDefault" @click="handleSetDefault(row)">设为默认</span>
                    <span class="action-btn-link disabled-action" v-else>默认主体</span>
                    <span class="action-btn-link">成员角色</span>
                    <span class="action-btn-link danger" @click="handleRemoveSubject(row)">移除</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <AccountClientRoleTab :account="account" class="mt-6" />

          <!-- 在线会话 Card -->
          <div class="online-sessions-card mt-6">
            <div class="card-header-flex">
              <div class="header-left-group">
                <span class="card-title">在线会话</span>
                <span class="card-subtitle">禁用账号后应自动下线全部会话</span>
              </div>
            </div>

            <el-table :data="onlineSessions" style="width: 100%; margin-top: 16px;" class="custom-session-table">
              <el-table-column prop="client" label="客户端" min-width="150" />
              <el-table-column prop="subject" label="主体" min-width="120" />
              <el-table-column prop="ipDevice" label="登录 IP / 设备" min-width="180" />
              <el-table-column prop="lastActive" label="最近活跃" width="150" />
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <div class="action-buttons-flex">
                    <span class="action-btn-link danger" @click="handleOffline(row)">下线</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <AccountLoginLogTab v-else-if="activeTab === 'login-log'" :account="account" />
      </div>
    </el-card>

    <!-- 加入主体 Dialog -->
    <el-dialog v-model="joinDialogVisible" width="580px" destroy-on-close class="join-subject-dialog">
      <template #header>
        <div class="custom-dialog-header">
          <span class="dialog-title">加入主体</span>
          <span class="dialog-account-info">账号：<strong>{{ account?.username }}</strong></span>
        </div>
      </template>

      <div class="join-dialog-body">
        <el-form :model="joinForm" label-position="top">
          <!-- 选择主体 -->
          <el-form-item label="选择主体 *" class="form-item-bold-label">
            <el-select v-model="joinForm.subjectId" placeholder="请选择主体" style="width: 100%">
              <el-option
                v-for="item in subjectOptions"
                :key="item.id"
                :label="item.labelName"
                :value="item.id"
              />
            </el-select>
            <div class="field-sub-tip">只展示与账号身份域匹配或允许跨域加入的主体。</div>
          </el-form-item>

          <!-- 成员类型 -->
          <el-form-item label="成员类型 *" class="form-item-bold-label">
            <div class="member-type-cards">
              <div 
                class="type-card" 
                :class="{ active: joinForm.memberType === 'admin' }"
                @click="joinForm.memberType = 'admin'"
              >
                <div class="card-radio-header">
                  <span class="card-title">主体管理员</span>
                </div>
                <div class="card-desc">可维护主体成员、应用开通和授权入口</div>
              </div>

              <div 
                class="type-card" 
                :class="{ active: joinForm.memberType === 'member' }"
                @click="joinForm.memberType = 'member'"
              >
                <div class="card-radio-header">
                  <span class="card-title">普通成员</span>
                </div>
                <div class="card-desc">仅作为主体成员，后续再分配客户端角色</div>
              </div>
            </div>
          </el-form-item>

          <!-- 是否设为默认主体 -->
          <el-form-item label="是否设为默认主体" class="form-item-bold-label">
            <el-select v-model="joinForm.isDefault" style="width: 100%">
              <el-option label="是" value="yes" />
              <el-option label="否" value="no" />
            </el-select>
          </el-form-item>

          <!-- 备注 -->
          <el-form-item label="备注" class="form-item-bold-label">
            <el-input
              v-model="joinForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注"
            />
          </el-form-item>
        </el-form>

        <!-- 保存后的警告横幅 -->
        <div class="dialog-save-warning-banner">
          保存后只写入主体成员关系，不会自动生成账号角色。应用权限需要到“客户端角色”中按客户端分配。
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="joinDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveJoinSubject">保存</el-button>
        </div>
      </template>
    </el-dialog>
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

.c-metric-val.metric-time {
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}

.c-metric-lbl {
  font-size: 13px;
  color: var(--text-muted);
}

.text-warning {
  color: #ea580c;
}

.text-danger {
  color: #dc2626;
}

/* Details Panel layout */
.basic-info-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.info-card-panel {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 20px;
  background: #ffffff;
}

.panel-header {
  margin-bottom: 20px;
}

.info-card-panel h3 {
  margin: 0 0 16px 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.panel-header h3 {
  margin-bottom: 6px;
}

.panel-header p {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
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

.mono-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .content-metrics-row,
  .basic-info-layout {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .content-metrics-row,
  .basic-info-layout {
    grid-template-columns: 1fr;
  }

  .panel-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
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

/* Associated subjects card styling */
.subject-association-card {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: #ffffff;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 16px;
}

.header-left-group {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.card-subtitle {
  font-size: 13px;
  color: var(--text-muted);
}

.subject-name-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.code-inline-badge {
  font-family: monospace;
  font-size: 11px;
  background: #f1f5f9;
  border: 1px solid var(--border-light);
  border-radius: 4px;
  padding: 1px 6px;
  color: var(--text-muted);
}

.action-buttons-flex {
  display: flex;
  gap: 12px;
}

.action-buttons-flex .action-btn-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  cursor: pointer;
}

.action-buttons-flex .action-btn-link.disabled-action {
  color: #94a3b8;
  cursor: not-allowed;
  font-weight: 500;
}

.action-buttons-flex .action-btn-link.danger {
  color: var(--el-color-danger);
}

/* Join Subject Dialog styles */
.custom-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 24px;
}

.custom-dialog-header .dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.custom-dialog-header .dialog-account-info {
  font-size: 14px;
  color: var(--text-muted);
}

.form-item-bold-label :deep(.el-form-item__label) {
  font-weight: 600 !important;
  color: var(--text-main) !important;
  font-size: 14px;
  margin-bottom: 6px !important;
}

.field-sub-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
  line-height: 1.4;
}

.member-type-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  width: 100%;
}

.type-card {
  border: 1.5px solid var(--border-light);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  background: #ffffff;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.type-card:hover {
  border-color: var(--primary-hover);
}

.type-card.active {
  border-color: var(--primary-color) !important;
  background-color: var(--bg-color) !important; /* light sky-blue background */
}

.type-card .card-radio-header {
  display: flex;
  align-items: center;
}

.type-card .card-title {
  font-weight: 700;
  font-size: 15px;
  color: var(--text-main);
}

.type-card .card-desc {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.dialog-save-warning-banner {
  background-color: #fff5eb;
  border: 1px solid #ffd8a8;
  border-radius: 8px;
  padding: 12px 18px;
  font-size: 13px;
  color: #c2410c;
  line-height: 1.5;
  margin-top: 20px;
  font-weight: 500;
}

.online-sessions-card {
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: #ffffff;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}
</style>
