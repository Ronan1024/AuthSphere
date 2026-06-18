<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { AccountRecord } from '@/api/account'

const props = defineProps<{
  account: AccountRecord
}>()

const roleList = ref([
  {
    id: 1,
    subjectName: '租户A',
    appInstanceName: '租户A商城实例',
    clientName: '商城平台端',
    roles: ['商城管理员', '商城运营'],
    roleType: '自定义',
    status: '启用',
    authTime: '2026-05-01'
  },
  {
    id: 2,
    subjectName: '租户A',
    appInstanceName: '租户A支付实例',
    clientName: '支付平台端',
    roles: ['支付管理员'],
    roleType: '自定义',
    status: '启用',
    authTime: '2026-05-03'
  },
  {
    id: 3,
    subjectName: '支付服务商A',
    appInstanceName: '服务商支付实例',
    clientName: '支付商户端',
    roles: [] as string[],
    roleType: '-',
    status: '无',
    authTime: '-'
  }
])

const dialogVisible = ref(false)
const isEditMode = ref(false)
const editingRowId = ref<number | null>(null)

const selectedSubject = ref('tenant_a')
const appInstanceName = ref('租户A商城实例')
const clientName = ref('商城平台端 PC')

const subjects = ref([
  { code: 'tenant_a', name: '租户A', desc: 'tenant_a / 主体管理员' },
  { code: 'provider_a', name: '支付服务商A', desc: 'provider_a / 成员' }
])

const rolesOptions = ref([
  { name: '商城管理员', desc: '拥有商品、订单、商户、系统设置资源', selected: true },
  { name: '商城运营', desc: '拥有商品、订单运营相关资源', selected: true },
  { name: '财务人员', desc: '拥有对账、退款审核、导出资源', selected: false }
])

const handleAssign = () => {
  isEditMode.value = false
  editingRowId.value = null
  selectedSubject.value = 'tenant_a'
  appInstanceName.value = '租户A商城实例'
  clientName.value = '商城平台端 PC'
  rolesOptions.value = [
    { name: '商城管理员', desc: '拥有商品、订单、商户、系统设置资源', selected: false },
    { name: '商城运营', desc: '拥有商品、订单运营相关资源', selected: false },
    { name: '财务人员', desc: '拥有对账、退款审核、导出资源', selected: false }
  ]
  dialogVisible.value = true
}

const handleAdjust = (row: any) => {
  isEditMode.value = true
  editingRowId.value = row.id
  
  const sub = subjects.value.find(s => s.name === row.subjectName)
  selectedSubject.value = sub ? sub.code : 'tenant_a'
  
  appInstanceName.value = row.appInstanceName
  clientName.value = row.clientName
  
  rolesOptions.value = [
    { name: '商城管理员', desc: '拥有商品、订单、商户、系统设置资源', selected: row.roles.includes('商城管理员') },
    { name: '商城运营', desc: '拥有商品、订单运营相关资源', selected: row.roles.includes('商城运营') },
    { name: '财务人员', desc: '拥有对账、退款审核、导出资源', selected: row.roles.includes('财务人员') || row.roles.includes('支付管理员') || row.roles.includes('财务人员') }
  ]
  
  if (row.roles.includes('支付管理员')) {
    rolesOptions.value[2].name = '支付管理员'
    rolesOptions.value[2].desc = '拥有支付对账、商户入驻、退款审核资源'
  } else {
    rolesOptions.value[2].name = '财务人员'
    rolesOptions.value[2].desc = '拥有对账、退款审核、导出资源'
  }
  
  dialogVisible.value = true
}

const toggleRoleSelection = (role: any) => {
  role.selected = !role.selected
}

const handleRemove = (row: any) => {
  ElMessageBox.confirm(
    `确定要移除该账号在 ${row.subjectName} / ${row.appInstanceName} 下的授权客户端吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    roleList.value = roleList.value.filter(item => item.id !== row.id)
    ElMessage.success('移除成功')
  }).catch(() => {})
}

const submitForm = () => {
  const activeSubject = subjects.value.find(s => s.code === selectedSubject.value)
  if (!activeSubject) return
  
  const selectedRoles = rolesOptions.value.filter(r => r.selected).map(r => r.name)
  
  if (isEditMode.value && editingRowId.value !== null) {
    const idx = roleList.value.findIndex(item => item.id === editingRowId.value)
    if (idx !== -1) {
      roleList.value[idx] = {
        ...roleList.value[idx],
        subjectName: activeSubject.name,
        appInstanceName: appInstanceName.value,
        clientName: clientName.value,
        roles: selectedRoles,
        roleType: '自定义',
        status: selectedRoles.length ? '启用' : '无'
      }
      ElMessage.success('授权更新成功')
    }
  } else {
    const newId = roleList.value.length ? Math.max(...roleList.value.map(item => item.id)) + 1 : 1
    roleList.value.push({
      id: newId,
      subjectName: activeSubject.name,
      appInstanceName: appInstanceName.value,
      clientName: clientName.value,
      roles: selectedRoles,
      roleType: '自定义',
      status: selectedRoles.length ? '启用' : '无',
      authTime: new Date().toISOString().substring(0, 10)
    })
    ElMessage.success('授权客户端成功')
  }
  dialogVisible.value = false
}
</script>

<template>
  <div class="account-client-role-card mt-6">
    <div class="card-header-flex">
      <div class="header-left-group">
        <span class="card-title">授权客户端</span>
        <span class="card-subtitle">为账号授权可访问的客户端及角色权限</span>
      </div>
      <el-button type="primary" @click="handleAssign">授权客户端</el-button>
    </div>

    <!-- 表格数据区 -->
    <el-table :data="roleList" style="width: 100%; margin-top: 16px;" class="custom-role-table">
      <el-table-column prop="subjectName" label="主体" min-width="120" />
      <el-table-column prop="appInstanceName" label="应用实例" min-width="150" />
      <el-table-column prop="clientName" label="客户端" min-width="130" />
      
      <el-table-column label="角色" min-width="200">
        <template #default="{ row }">
          <div class="roles-tags-flex" v-if="row.roles && row.roles.length > 0">
            <el-tag 
              v-for="role in row.roles" 
              :key="role" 
              size="default" 
              class="role-badge-tag"
            >
              {{ role }}
            </el-tag>
          </div>
          <span v-else class="status-pill orange-light">待分配</span>
        </template>
      </el-table-column>

      <el-table-column prop="roleType" label="角色类型" width="100" />
      
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span class="status-pill green-pill" v-if="row.status === '启用'">启用</span>
          <span class="status-pill gray-pill" v-else>无</span>
        </template>
      </el-table-column>

      <el-table-column prop="authTime" label="授权时间" width="140" />

      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <div class="row-actions-flex">
            <template v-if="row.roles && row.roles.length > 0">
              <span class="action-btn-link" @click="handleAdjust(row)">调整角色</span>
              <span class="action-btn-link danger" @click="handleRemove(row)">移除</span>
            </template>
            <template v-else>
              <span class="action-btn-link" @click="handleAssign">分配角色</span>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <div class="dialogs-wrapper">
    <el-dialog 
      v-model="dialogVisible" 
      width="920px"
      destroy-on-close
      class="wizard-dialog"
      :show-close="true"
    >
      <template #header>
        <span class="dialog-title-text">{{ isEditMode ? '调整客户端角色' : '分配客户端角色' }}</span>
      </template>

      <div class="wizard-container">
        <div class="wizard-left-sidebar">
          <div 
            v-for="sub in subjects" 
            :key="sub.code" 
            class="subject-sidebar-item"
            :class="{ active: selectedSubject === sub.code }"
            @click="selectedSubject = sub.code"
          >
            <strong class="sub-title">{{ sub.name }}</strong>
            <span class="sub-desc">{{ sub.desc }}</span>
          </div>
        </div>

        <div class="wizard-right-content">
          <h3 class="panel-section-title">上下文选择</h3>

          <div class="context-grid-row">
            <div class="form-item-block">
              <label>应用实例 <span class="required-star">*</span></label>
              <el-select v-model="appInstanceName" placeholder="请选择应用实例" style="width: 100%;">
                <el-option label="租户A商城实例" value="租户A商城实例" />
                <el-option label="租户A支付实例" value="租户A支付实例" />
                <el-option label="服务商支付实例" value="服务商支付实例" />
              </el-select>
            </div>
            <div class="form-item-block">
              <label>客户端 <span class="required-star">*</span></label>
              <el-select v-model="clientName" placeholder="请选择客户端" style="width: 100%;">
                <el-option label="商城平台端 PC" value="商城平台端 PC" />
                <el-option label="支付平台端 PC" value="支付平台端 PC" />
                <el-option label="支付商户端" value="支付商户端" />
              </el-select>
            </div>
          </div>

          <div class="roles-selection-block">
            <label class="block-label">选择角色 <span class="required-star">*</span></label>
            
            <div class="roles-cards-list">
              <div 
                v-for="role in rolesOptions" 
                :key="role.name"
                class="role-card-item" 
                :class="{ selected: role.selected }"
                @click="toggleRoleSelection(role)"
              >
                <div class="role-card-left">
                  <span class="role-card-title">{{ role.name }}</span>
                  <p class="role-card-desc">{{ role.desc }}</p>
                </div>
                <div class="role-card-badge" :class="role.selected ? 'selected' : 'unselected'">
                  {{ role.selected ? '已选' : '未选' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="wizard-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存角色</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.account-client-role-card {
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 12px;
  background: #ffffff;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
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
  color: #0C4A6E;
}

.card-subtitle {
  font-size: 13px;
  color: #475569;
}

.roles-tags-flex {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-badge-tag {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border: 1px solid var(--el-color-primary-light-8);
  border-radius: 4px;
  font-weight: 500;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

.status-pill.green-pill {
  color: var(--el-color-success);
  background: var(--el-color-success-light-9);
  border: 1px solid var(--el-color-success-light-8);
}

.status-pill.gray-pill {
  color: #64748b;
  background: #f1f5f9;
  border: 1px solid #eaeaea;
}

.status-pill.orange-light {
  color: #c2410c;
  background: #ffedd5;
  border: 1px solid #fed7aa;
}

.row-actions-flex {
  display: flex;
  gap: 12px;
}

.action-btn-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  cursor: pointer;
  transition: color 0.15s ease;
}

.action-btn-link:hover {
  color: var(--el-color-primary-dark-2);
}

.action-btn-link.danger {
  color: var(--el-color-danger);
}

.action-btn-link.danger:hover {
  color: var(--el-color-danger-dark-2);
}

.mb-4 {
  margin-bottom: 16px;
}

:deep(.el-card__body) {
  padding: 0;
}

:deep(.wizard-dialog .el-dialog__body) {
  padding: 0 !important;
}

:deep(.wizard-dialog .el-dialog__header) {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #eaeaea;
}

:deep(.wizard-dialog .el-dialog__footer) {
  padding: 16px 24px !important;
  border-top: 1px solid #eaeaea;
  margin: 0 !important;
}

.dialog-title-text {
  font-size: 16px;
  font-weight: 700;
  color: #172033;
}

.wizard-container {
  display: flex;
  margin: 0;
  height: 480px;
  background: #ffffff;
}

.wizard-left-sidebar {
  width: 250px;
  background-color: #fafcff;
  border-right: 1px solid #eaeaea;
  display: flex;
  flex-direction: column;
}

.subject-sidebar-item {
  padding: 16px 20px;
  border-left: 3px solid transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.2s ease;
}

.subject-sidebar-item:hover {
  background-color: rgba(22, 119, 255, 0.04);
}

.subject-sidebar-item.active {
  background-color: var(--el-color-primary-light-9);
  border-left-color: var(--el-color-primary);
}

.subject-sidebar-item .sub-title {
  color: #172033;
  font-size: 14px;
  font-weight: 700;
}

.subject-sidebar-item .sub-desc {
  color: #64748b;
  font-size: 11px;
}

.wizard-right-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #172033;
}

.context-grid-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-item-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item-block label {
  font-size: 13px;
  font-weight: 600;
  color: #172033;
}

.required-star {
  color: var(--el-color-danger);
}

.roles-selection-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.block-label {
  font-size: 13px;
  font-weight: 600;
  color: #172033;
}

.roles-cards-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.role-card-item {
  border: 1px solid #eaeaea;
  border-radius: 6px;
  padding: 12px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
  background-color: #ffffff;
}

.role-card-item:hover {
  border-color: var(--el-color-primary-light-3);
  background-color: var(--el-color-primary-light-9);
}

.role-card-item.selected {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  box-shadow: 0 0 0 1px var(--el-color-primary);
}

.role-card-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-card-title {
  font-weight: 700;
  font-size: 14px;
  color: #172033;
}

.role-card-desc {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.role-card-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.role-card-badge.selected {
  color: var(--el-color-success);
  background-color: var(--el-color-success-light-9);
  border: 1px solid var(--el-color-success-light-8);
}

.role-card-badge.unselected {
  color: #64748b;
  background-color: #f1f5f9;
  border: 1px solid #eaeaea;
}

.warning-tip-box {
  background-color: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 4px;
  padding: 10px 16px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.5;
}

.wizard-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>



























































































