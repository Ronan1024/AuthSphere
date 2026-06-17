<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { 
  Delete, 
  Plus, 
  Lock, 
  Monitor, 
  Setting, 
  Link, 
  Compass
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { AppClientRecord } from '@/api/appClient'
import type { AppPayload, AppRecord } from '@/api/app'

const props = defineProps<{
  visible: boolean
  mode: 'create' | 'edit'
  loading?: boolean
  initialApp?: AppRecord | null
  initialClients?: AppClientRecord[]
}>()

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void
  (event: 'submit', payload: AppPayload): void
}>()

const formRef = ref<FormInstance>()

const appTypeOptions = [
  { label: 'IAM 认证中心', value: 'IAM' },
  { label: '商城应用', value: 'MALL' },
  { label: '支付中心', value: 'PAYMENT' },
  { label: '仓储系统', value: 'WAREHOUSE' },
  { label: '物流服务', value: 'LOGISTICS' },
  { label: '自定义业务', value: 'CUSTOM' }
]

const clientTypeOptions = [
  { label: 'ADMIN_WEB (管理后台)', value: 1 },
  { label: 'MERCHANT_WEB (商家后台)', value: 2 },
  { label: 'MINI_PROGRAM (微信小程序)', value: 3 },
  { label: 'H5 (轻量移动网页)', value: 4 },
  { label: 'OPEN_API (开放API接口)', value: 5 },
  { label: 'SERVICE (服务间通信)', value: 6 }
]

const formData = reactive<AppPayload>({
  appName: '',
  appCode: '',
  appType: '',
  entryUrl: '',
  icon: '',
  status: 1,
  description: '',
  clients: []
})

const dialogTitle = computed(() => props.mode === 'create' ? '新增应用' : '编辑应用')

const rules: FormRules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { max: 64, message: '长度不能超过 64 个字符', trigger: 'blur' }
  ],
  appCode: [
    { required: true, message: '请输入应用编码', trigger: 'blur' },
    { pattern: /^[a-z0-9_]+$/, message: '只能由小写字母、数字、下划线组成', trigger: 'blur' },
    { max: 64, message: '长度不能超过 64 个字符', trigger: 'blur' }
  ],
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择应用状态', trigger: 'change' }]
}

const resetForm = () => {
  formData.id = props.initialApp?.id
  formData.appName = props.initialApp?.appName || ''
  formData.appCode = props.initialApp?.appCode || ''
  formData.appType = props.initialApp?.appType || ''
  formData.entryUrl = props.initialApp?.entryUrl || ''
  formData.icon = props.initialApp?.icon || ''
  formData.status = props.initialApp?.status || 1
  formData.description = props.initialApp?.description || ''
  formData.clients = (props.initialClients || []).map(client => ({
    id: client.id,
    clientCode: client.clientCode,
    clientName: client.clientName,
    clientType: Number(client.clientType),
    defaultRealmId: client.defaultRealmId || null,
    defaultEntryUrl: client.defaultEntryUrl || '',
    status: client.status,
    description: client.description || ''
  }))
}

watch(() => props.visible, visible => {
  if (visible) {
    resetForm()
    window.requestAnimationFrame(() => formRef.value?.clearValidate())
  }
})

watch(() => [props.initialApp, props.initialClients], () => {
  if (props.visible) {
    resetForm()
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const addClient = () => {
  formData.clients = [
    ...(formData.clients || []),
    {
      clientCode: '',
      clientName: '',
      clientType: 1,
      status: 1,
      defaultEntryUrl: '',
      defaultRealmId: null,
      description: ''
    }
  ]
}

const removeClient = (index: number) => {
  ElMessageBox.confirm(
    '确定要移除该客户端配置吗？',
    '提示',
    {
      confirmButtonText: '确定移除',
      cancelButtonText: '取消',
      type: 'warning',
      buttonSize: 'default'
    }
  ).then(() => {
    formData.clients = (formData.clients || []).filter((_, currentIndex) => currentIndex !== index)
    ElMessage.success('已移除客户端配置项')
  }).catch(() => {})
}

const validateClients = () => {
  const codes = new Set<string>()
  for (const [index, client] of (formData.clients || []).entries()) {
    const rowNumber = index + 1
    if (!client.clientName?.trim()) {
      ElMessage.warning(`请填写第 ${rowNumber} 个客户端名称`)
      return false
    }
    if (!client.clientCode?.trim()) {
      ElMessage.warning(`请填写第 ${rowNumber} 个客户端编码`)
      return false
    }
    if (!/^[a-z0-9_]+$/.test(client.clientCode)) {
      ElMessage.warning(`第 ${rowNumber} 个客户端编码只能由小写字母、数字、下划线组成`)
      return false
    }
    if (codes.has(client.clientCode)) {
      ElMessage.warning(`客户端编码「${client.clientCode}」重复`)
      return false
    }
    codes.add(client.clientCode)
  }
  return true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !validateClients()) return

  emit('submit', {
    id: formData.id,
    appName: formData.appName.trim(),
    appCode: formData.appCode.trim(),
    appType: formData.appType,
    entryUrl: formData.entryUrl?.trim() || undefined,
    icon: formData.icon?.trim() || undefined,
    status: formData.status,
    description: formData.description?.trim() || undefined,
    clients: (formData.clients || []).map(client => ({
      id: client.id,
      clientName: client.clientName.trim(),
      clientCode: client.clientCode.trim(),
      clientType: Number(client.clientType),
      defaultRealmId: client.defaultRealmId || null,
      defaultEntryUrl: client.defaultEntryUrl?.trim() || undefined,
      status: client.status,
      description: client.description?.trim() || undefined
    }))
  })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="880px"
    destroy-on-close
    align-center
    class="premium-dialog"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="110px"
      label-position="top"
      class="application-form"
    >
      <div class="form-section">
        <div class="section-title">
          <span class="title-bar-gradient"></span>
          <span>基本信息</span>
        </div>
        <div class="basic-grid">
          <el-form-item label="应用名称" prop="appName">
            <el-input v-model="formData.appName" placeholder="请输入应用名称" maxlength="64" show-word-limit />
          </el-form-item>
          <el-form-item label="应用编码" prop="appCode">
            <el-input v-model="formData.appCode" placeholder="如 unified_portal" maxlength="64" show-word-limit />
          </el-form-item>
          <el-form-item label="应用类型" prop="appType">
            <el-select v-model="formData.appType" placeholder="请选择应用类型" style="width: 100%">
              <el-option v-for="item in appTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="应用状态" prop="status">
            <el-radio-group v-model="formData.status" class="dialog-radio-group">
              <el-radio :value="1" class="dialog-status-radio enabled">
                <span class="status-dot enabled"></span> 启用
              </el-radio>
              <el-radio :value="2" class="dialog-status-radio disabled">
                <span class="status-dot disabled"></span> 禁用
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        
        <div class="basic-grid mt-8">
          <el-form-item label="入口地址">
            <el-input v-model="formData.entryUrl" placeholder="请输入默认入口地址 (选填)" maxlength="255" />
          </el-form-item>
          <el-form-item label="图标标识">
            <el-input v-model="formData.icon" placeholder="请输入图标标识或资源地址 (选填)" maxlength="255" />
          </el-form-item>
        </div>

        <el-form-item label="应用描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入应用描述 (选填)"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </div>

      <div class="form-section last-section">
        <div class="section-header">
          <div class="section-title">
            <span class="title-bar-gradient client-bar"></span>
            <span>应用客户端配置</span>
          </div>
          <el-button type="primary" :icon="Plus" @click="addClient" class="btn-add-client-mini">新增客户端</el-button>
        </div>
        
        <el-empty v-if="!formData.clients?.length" description="暂无客户端，可按需新增多个访问入口" :image-size="80" />
        
        <div v-else class="client-list">
          <div v-for="(client, index) in formData.clients" :key="client.id || index" class="client-row">
            <div class="client-row-header">
              <div class="client-badge">
                <span class="badge-num">#{{ index + 1 }}</span>
                <span>客户端通道</span>
              </div>
              <el-button link type="danger" :icon="Delete" @click="removeClient(index)" class="btn-delete-row">移除</el-button>
            </div>
            
            <div class="client-grid">
              <el-form-item label="客户端名称">
                <template #label>
                  <span class="dialog-field-label"><el-icon><Monitor /></el-icon> 名称</span>
                </template>
                <el-input v-model="client.clientName" placeholder="请输入客户端名称" maxlength="64" />
              </el-form-item>
              <el-form-item label="客户端编码">
                <template #label>
                  <span class="dialog-field-label"><el-icon><Lock /></el-icon> 编码</span>
                </template>
                <el-input v-model="client.clientCode" placeholder="如 portal_web" maxlength="64" />
              </el-form-item>
              <el-form-item label="客户端类型">
                <template #label>
                  <span class="dialog-field-label"><el-icon><Setting /></el-icon> 类型</span>
                </template>
                <el-select v-model="client.clientType" placeholder="请选择客户端类型" style="width: 100%">
                  <el-option v-for="item in clientTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <template #label>
                  <span class="dialog-field-label"><el-icon><Compass /></el-icon> 启用状态</span>
                </template>
                <el-switch 
                  v-model="client.status" 
                  :active-value="1" 
                  :inactive-value="2" 
                  active-text="开启" 
                  inactive-text="关闭" 
                  inline-prompt
                />
              </el-form-item>
            </div>
            
            <el-form-item label="默认入口">
              <template #label>
                <span class="dialog-field-label"><el-icon><Link /></el-icon> 默认入口</span>
              </template>
              <el-input v-model="client.defaultEntryUrl" placeholder="请输入客户端默认入口地址 (选填)" maxlength="255" />
            </el-form-item>
            <el-form-item label="客户端说明">
              <el-input
                v-model="client.description"
                type="textarea"
                :rows="2"
                placeholder="请输入客户端说明 (选填)"
                maxlength="255"
                show-word-limit
              />
            </el-form-item>
          </div>
        </div>
      </div>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" class="btn-dialog-cancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit" class="btn-dialog-submit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* Dialog wrapper */
:deep(.premium-dialog) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

:deep(.premium-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.premium-dialog .el-dialog__title) {
  font-size: 17px;
  font-weight: 650;
  color: #0f172a;
}

:deep(.premium-dialog .el-dialog__body) {
  padding: 20px 24px;
}

:deep(.premium-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f1f5f9;
  background: #f8fafc;
}

.application-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 12px;
}

/* Custom Scrollbar */
.application-form::-webkit-scrollbar {
  width: 6px;
}
.application-form::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
.application-form::-webkit-scrollbar-track {
  background: transparent;
}

.form-section {
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px dashed #e2e8f0;
}

.form-section.last-section {
  border-bottom: 0;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 650;
}

.title-bar-gradient {
  display: inline-block;
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #6366f1 0%, #4f46e5 100%);
  border-radius: 2px;
}

.title-bar-gradient.client-bar {
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
}

.basic-grid,
.client-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0 24px;
}

.mt-8 {
  margin-top: 8px;
}

/* Radio Group */
.dialog-radio-group {
  display: flex;
  gap: 16px;
  margin-top: 4px;
}

.dialog-status-radio {
  display: flex;
  align-items: center;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 0 16px !important;
  margin-right: 0 !important;
  transition: all 0.2s;
}

.dialog-status-radio:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.dialog-radio-group :deep(.el-radio.is-checked) {
  border-color: #6366f1;
  background: #f5f3ff;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}
.status-dot.enabled { background: #10b981; }
.status-dot.disabled { background: #94a3b8; }

/* Client configurations list */
.client-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.client-row {
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  transition: all 0.2s;
}

.client-row:hover {
  border-color: #cbd5e1;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.client-row-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e2e8f0;
}

.client-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 650;
  color: #1e293b;
}

.badge-num {
  font-size: 10px;
  font-weight: 700;
  color: #ffffff;
  background-color: #3b82f6;
  padding: 1px 6px;
  border-radius: 999px;
}

.btn-delete-row {
  font-size: 12.5px;
  font-weight: 600;
  color: #ef4444;
}

.dialog-field-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 600 !important;
  color: #475569 !important;
}

/* Add Client Button */
.btn-add-client-mini {
  font-weight: 600;
  border-radius: 6px;
}

/* Form labels and inputs */
.application-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.application-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
  font-size: 13px;
  padding-bottom: 4px;
}

.application-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 6px;
}

.application-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.application-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #6366f1 inset !important;
}

.application-form :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 6px;
}

.application-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #6366f1 inset !important;
}

/* Footer elements */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-dialog-cancel {
  font-weight: 600;
  border-radius: 6px;
  border-color: #cbd5e1;
}

.btn-dialog-submit {
  font-weight: 600;
  border-radius: 6px;
  min-width: 100px;
}

@media (max-width: 760px) {
  .basic-grid,
  .client-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
