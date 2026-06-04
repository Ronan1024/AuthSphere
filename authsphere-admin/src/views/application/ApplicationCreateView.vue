<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  UploadFilled, 
  Plus, 
  Delete, 
  InfoFilled, 
  Monitor,
  Link,
  Lock,
  Compass,
  Setting,
  Connection
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appApi } from '@/api/app'
import { realmApi, type RealmRecord } from '@/api/realm'
import { appClientApi } from '@/api/appClient'

const router = useRouter()
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  name: '',
  code: '',
  type: '',
  url: '',
  icon: '',
  description: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { max: 64, message: '长度不能超过 64 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入应用编码', trigger: 'blur' },
    { pattern: /^[a-z0-9_]+$/, message: '只能由小写字母、数字、下划线组成', trigger: 'blur' },
    { max: 64, message: '长度不能超过 64 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择应用类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择应用状态', trigger: 'change' }
  ]
}

const appTypeOptions = [
  { label: 'IAM 认证中心', value: 'IAM' },
  { label: '商城应用', value: 'MALL' },
  { label: '支付中心', value: 'PAYMENT' },
  { label: '仓储系统', value: 'WAREHOUSE' },
  { label: '物流服务', value: 'LOGISTICS' },
  { label: '自定义业务', value: 'CUSTOM' }
]

interface ClientFormItem {
  clientCode: string
  clientName: string
  clientType: number
  defaultRealmId: string | number | null
  defaultEntryUrl: string
  status: number
  description: string
}

const clientList = ref<ClientFormItem[]>([])
const realms = ref<RealmRecord[]>([])

const fetchRealms = async () => {
  try {
    const res = await realmApi.page({ page: 1, size: 100 })
    realms.value = res.records || []
  } catch (e: any) {
    console.error('获取身份域列表失败', e)
  }
}

const addClient = () => {
  clientList.value.push({
    clientCode: '',
    clientName: '',
    clientType: 1,
    defaultRealmId: realms.value[0]?.id || null,
    defaultEntryUrl: '',
    status: 1,
    description: ''
  })
}

const removeClient = (index: number) => {
  ElMessageBox.confirm(
    '确定要删除该客户端配置吗？删除后此客户端将不会被创建。',
    '提示',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      buttonSize: 'default'
    }
  ).then(() => {
    clientList.value.splice(index, 1)
    ElMessage.success('已移除客户端配置项')
  }).catch(() => {})
}

const handleBack = () => {
  router.push('/applications')
}

const handleAvatarSuccess = (res: any, file: any) => {
  formData.icon = URL.createObjectURL(file.raw)
}

const beforeAvatarUpload = (file: any) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/svg+xml'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('上传应用图标只能是 JPG/PNG/SVG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传应用图标大小不能超过 2MB!')
  }
  return isValidType && isLt2M
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // Validate clients
  for (let i = 0; i < clientList.value.length; i++) {
    const c = clientList.value[i]
    if (!c.clientCode) {
      ElMessage.warning(`请输入第 ${i + 1} 个客户端的编号`)
      return
    }
    if (!/^[a-z0-9_]+$/.test(c.clientCode)) {
      ElMessage.warning(`第 ${i + 1} 个客户端编号只能由小写字母、数字、下划线组成`)
      return
    }
    if (!c.clientName) {
      ElMessage.warning(`请输入第 ${i + 1} 个客户端的名称`)
      return
    }
    if (!c.defaultRealmId) {
      ElMessage.warning(`请选择第 ${i + 1} 个客户端的默认身份域`)
      return
    }
  }

  submitLoading.value = true
  try {
    // Create App with Clients nested in payload
    await appApi.create({
      appName: formData.name,
      appCode: formData.code,
      appType: formData.type,
      status: formData.status,
      icon: formData.icon || undefined,
      description: formData.description || undefined,
      clients: clientList.value.map(c => ({
        clientCode: c.clientCode,
        clientName: c.clientName,
        clientType: c.clientType,
        status: c.status,
        description: c.description || undefined,
        defaultRealmId: c.defaultRealmId,
        defaultEntryUrl: c.defaultEntryUrl || undefined
      }))
    })

    ElMessage.success('应用创建成功')
    router.push('/applications')
  } catch (error: any) {
    ElMessage.error(error.message || '创建应用失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await fetchRealms()
  // Automatically seed one default client if list is empty
  if (clientList.value.length === 0) {
    addClient()
  }
})
</script>

<template>
  <div class="application-create-page">
    <div class="page-header">
      <div class="header-left">
        <div class="title-with-badge">
          <h1>新增应用</h1>
          <span class="premium-badge">Create Application</span>
        </div>
        <p>配置并发布新的应用服务，定义应用的基础通信凭证与受保护的客户端端口。</p>
      </div>
      <div class="header-right">
        <el-button :icon="ArrowLeft" plain @click="handleBack" class="btn-back">返回应用列表</el-button>
      </div>
    </div>

    <div class="form-container">
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-position="top"
        class="create-app-form animate-fade-in"
      >
        <!-- 基本信息 Section -->
        <div class="form-section">
          <div class="section-title">
            <span class="title-bar-gradient"></span>
            <div class="title-text">
              <h3>基本信息</h3>
              <span>定义应用在系统全局的唯一性标志与名称。</span>
            </div>
          </div>
          
          <div class="form-grid-3">
            <el-form-item label="应用名称" prop="name" required>
              <el-input v-model="formData.name" placeholder="例如: 商城主应用" maxlength="64" show-word-limit />
            </el-form-item>
            <el-form-item label="应用唯一编码" prop="code" required>
              <el-input v-model="formData.code" placeholder="例如: mall_main (小写字母/数字/下划线)" maxlength="64" show-word-limit />
            </el-form-item>
            <el-form-item label="应用所属类型" prop="type" required>
              <el-select v-model="formData.type" placeholder="请选择应用类型" style="width: 100%">
                <el-option
                  v-for="item in appTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-grid-3 mt-16">
            <el-form-item label="应用图标" class="col-span-1">
              <div class="upload-wrapper">
                <el-upload
                  class="icon-uploader"
                  action="#"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="formData.icon" :src="formData.icon" class="uploaded-icon" />
                  <div v-else class="uploader-placeholder">
                    <el-icon class="upload-icon"><UploadFilled /></el-icon>
                    <span>上传/拖拽图标</span>
                  </div>
                </el-upload>
                <div class="upload-tip">
                  <span class="formats">支持 JPEG / PNG / SVG</span>
                  <span class="size">建议 128×128px，不超过 2MB</span>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="应用描述" prop="description" class="col-span-2">
              <el-input 
                v-model="formData.description" 
                type="textarea" 
                :rows="4" 
                placeholder="请详细描述该应用的职责和业务范围 (选填)" 
                maxlength="255" 
                show-word-limit 
              />
            </el-form-item>
          </div>

          <div class="form-grid-3 mt-24">
            <el-form-item label="应用初始状态" prop="status" required class="col-span-3">
              <el-radio-group v-model="formData.status" class="custom-radio-group">
                <el-radio :value="1" class="status-radio-card enabled">
                  <span class="indicator-dot-pulse enabled"></span>
                  <div class="radio-label">
                    <strong class="label-title">启用应用</strong>
                    <span class="label-desc">创建后允许客户端进行通信与登录鉴权，立即上线服务</span>
                  </div>
                </el-radio>
                <el-radio :value="2" class="status-radio-card disabled">
                  <span class="indicator-dot-pulse disabled"></span>
                  <div class="radio-label">
                    <strong class="label-title">禁用应用</strong>
                    <span class="label-desc">创建后暂停一切通信，无法登录和调用接口，进入维护状态</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>

        <div class="divider-wrapper">
          <el-divider>
            <span class="divider-badge"><el-icon><Connection /></el-icon> 客户端配置</span>
          </el-divider>
        </div>

        <!-- 客户端配置 Section -->
        <div class="form-section">
          <div class="section-header">
            <div class="section-title">
              <span class="title-bar-gradient client-bar"></span>
              <div class="title-text">
                <h3>应用客户端端口配置</h3>
                <span>应用可暴露多套不同入口的客户端进行权限受控配置（如：管理端网页、小程序端、API调用等）。</span>
              </div>
            </div>
            <el-button type="primary" :icon="Plus" @click="addClient" class="btn-add-client-gradient">添加客户端</el-button>
          </div>

          <div v-if="clientList.length === 0" class="empty-clients-placeholder">
            <div class="empty-content">
              <el-icon class="empty-icon"><Plus /></el-icon>
              <h4>暂无已配置的客户端</h4>
              <p>为了保证应用可被正常访问，建议至少配置一个登录端或接口客户端。</p>
              <el-button type="primary" size="small" @click="addClient">立即添加</el-button>
            </div>
          </div>

          <div v-else class="clients-list">
            <div 
              v-for="(client, index) in clientList" 
              :key="index" 
              class="client-card"
            >
              <div class="client-card-header">
                <div class="client-badge-container">
                  <span class="client-badge-num">#{{ index + 1 }}</span>
                  <span class="client-badge-title">客户端通道</span>
                </div>
                <el-button 
                  type="danger" 
                  link 
                  :icon="Delete"
                  @click="removeClient(index)"
                  class="btn-delete-client"
                  v-if="clientList.length > 1"
                >
                  删除此通道
                </el-button>
              </div>

              <div class="form-grid-3">
                <el-form-item label="客户端编号" required>
                  <template #label>
                    <span class="field-label-with-icon"><el-icon><Lock /></el-icon> 客户端编号</span>
                  </template>
                  <el-input v-model="client.clientCode" placeholder="如: web_portal_client" maxlength="64" />
                </el-form-item>
                <el-form-item label="客户端名称" required>
                  <template #label>
                    <span class="field-label-with-icon"><el-icon><Monitor /></el-icon> 客户端名称</span>
                  </template>
                  <el-input v-model="client.clientName" placeholder="如: Web 网页端" maxlength="64" />
                </el-form-item>
                <el-form-item label="客户端类型" required>
                  <template #label>
                    <span class="field-label-with-icon"><el-icon><Setting /></el-icon> 客户端类型</span>
                  </template>
                  <el-select v-model="client.clientType" placeholder="选择接口或登录端类型" style="width: 100%">
                    <el-option label="ADMIN_WEB (管理后台)" :value="1" />
                    <el-option label="MERCHANT_WEB (商家后台)" :value="2" />
                    <el-option label="MINI_PROGRAM (微信小程序)" :value="3" />
                    <el-option label="H5 (轻量移动网页)" :value="4" />
                    <el-option label="OPEN_API (开放API接口)" :value="5" />
                    <el-option label="SERVICE (服务间通信)" :value="6" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="form-grid-3 mt-16">
                <el-form-item label="默认身份空间 (Realm)" required>
                  <template #label>
                    <span class="field-label-with-icon"><el-icon><Compass /></el-icon> 默认身份空间 (Realm)</span>
                  </template>
                  <el-select v-model="client.defaultRealmId" placeholder="该应用所属的身份隔离域" style="width: 100%">
                    <el-option
                      v-for="realm in realms"
                      :key="realm.id"
                      :label="realm.name"
                      :value="realm.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="默认跳转/入口地址">
                  <template #label>
                    <span class="field-label-with-icon"><el-icon><Link /></el-icon> 默认跳转/入口地址</span>
                  </template>
                  <el-input v-model="client.defaultEntryUrl" placeholder="如: /platform 或 https://x.com" maxlength="255" />
                </el-form-item>
                <el-form-item label="启用状态">
                  <template #label>
                    <span class="field-label-with-icon"><el-icon><InfoFilled /></el-icon> 启用状态</span>
                  </template>
                  <div class="switch-container">
                    <span class="switch-custom-label">启用此端口</span>
                    <el-switch 
                      v-model="client.status" 
                      :active-value="1" 
                      :inactive-value="2" 
                      active-text="已开启" 
                      inactive-text="已关闭" 
                      inline-prompt 
                    />
                  </div>
                </el-form-item>
              </div>

              <div class="mt-16">
                <el-form-item label="端说明描述">
                  <el-input 
                    v-model="client.description" 
                    type="textarea" 
                    :rows="2" 
                    placeholder="客户端的使用人群及权限定义场景概述 (选填)" 
                    maxlength="200" 
                    show-word-limit 
                  />
                </el-form-item>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </div>

    <!-- 浮动玻璃拟态页脚 -->
    <div class="page-footer">
      <div class="footer-inner">
        <el-button @click="handleBack" size="large" class="btn-cancel">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitLoading" 
          @click="submitForm" 
          size="large" 
          class="btn-submit"
        >
          保存应用并初始化客户端
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Page & layout */
.application-create-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 120px);
  background-color: #f6f8fa;
  padding: 0 0 120px 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.title-with-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.title-with-badge h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.02em;
}

.premium-badge {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #4f46e5;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 9999px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.header-left p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.btn-back {
  font-weight: 500;
  border-color: #cbd5e1;
  color: #475569;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background-color: #f1f5f9;
  border-color: #94a3b8;
  color: #0f172a;
}

.form-container {
  background: #ffffff;
  border-radius: 16px;
  padding: 40px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.02), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
}

/* Sections */
.form-section {
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.section-header .section-title {
  margin-bottom: 0;
}

.title-bar-gradient {
  display: inline-block;
  width: 5px;
  height: 20px;
  background: linear-gradient(180deg, #6366f1 0%, #4f46e5 100%);
  border-radius: 4px;
}

.title-bar-gradient.client-bar {
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
}

.title-text h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 650;
  color: #0f172a;
}

.title-text span {
  font-size: 13px;
  color: #64748b;
  font-weight: 400;
}

.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0 28px;
}

.col-span-1 {
  grid-column: span 1;
}

.col-span-2 {
  grid-column: span 2;
}

.col-span-3 {
  grid-column: span 3;
}

.mt-16 {
  margin-top: 16px;
}

.mt-24 {
  margin-top: 24px;
}

/* Custom Dividers */
.divider-wrapper {
  margin: 44px 0;
}

.divider-wrapper :deep(.el-divider) {
  border-color: #f1f5f9;
}

.divider-badge {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #475569;
  padding: 4px 14px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Avatar/Icon Uploader Custom Styling */
.upload-wrapper {
  display: flex;
  gap: 16px;
  align-items: center;
}

.icon-uploader {
  width: 100px;
  height: 100px;
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-uploader:hover {
  border-color: #6366f1;
  background-color: #f5f3ff;
}

.uploader-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 500;
  gap: 6px;
}

.upload-icon {
  font-size: 26px;
  color: #94a3b8;
  transition: color 0.2s;
}

.icon-uploader:hover .upload-icon {
  color: #6366f1;
}

.uploaded-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 8px;
}

.upload-tip {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.upload-tip .formats {
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.upload-tip .size {
  color: #94a3b8;
  font-size: 12px;
}

/* Custom Styled Status Radio Group */
.custom-radio-group {
  display: flex;
  gap: 20px;
  width: 100%;
}

.custom-radio-group :deep(.el-radio) {
  margin-right: 0;
  height: auto;
  padding: 20px 24px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  flex: 1;
  display: flex;
  align-items: flex-start;
  background: #ffffff;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-radio-group :deep(.el-radio__input) {
  margin-top: 4px;
}

.custom-radio-group :deep(.el-radio:hover) {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.custom-radio-group :deep(.el-radio.is-checked) {
  border-color: #6366f1;
  background: #f5f3ff;
}

.custom-radio-group :deep(.el-radio.is-checked.disabled) {
  border-color: #94a3b8;
  background: #f8fafc;
}

.status-radio-card {
  position: relative;
}

.indicator-dot-pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  position: absolute;
  right: 24px;
  top: 24px;
}

.indicator-dot-pulse.enabled { 
  background-color: #10b981; 
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.5);
  animation: pulse-green 2s infinite;
}

.indicator-dot-pulse.disabled { 
  background-color: #94a3b8; 
}

@keyframes pulse-green {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  }
  70% {
    transform: scale(1);
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

.radio-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 10px;
  white-space: normal;
}

.radio-label .label-title {
  font-size: 14.5px;
  color: #0f172a;
  font-weight: 600;
}

.radio-label .label-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

/* Add Client Button */
.btn-add-client-gradient {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.2);
  transition: all 0.2s;
}

.btn-add-client-gradient:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.3);
}

/* Empty placeholder */
.empty-clients-placeholder {
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  padding: 48px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f8fafc;
}

.empty-content {
  text-align: center;
  max-width: 320px;
}

.empty-icon {
  font-size: 32px;
  color: #94a3b8;
  margin-bottom: 12px;
}

.empty-content h4 {
  margin: 0 0 6px 0;
  font-size: 15px;
  color: #334155;
  font-weight: 600;
}

.empty-content p {
  margin: 0 0 16px 0;
  font-size: 12.5px;
  color: #64748b;
  line-height: 1.5;
}

/* Clients List & Card styling */
.clients-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.client-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 28px;
  background: #ffffff;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.01), 0 2px 4px -1px rgba(0, 0, 0, 0.01);
}

.client-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.04), 0 8px 8px -6px rgba(0, 0, 0, 0.04);
}

.client-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 14px;
  border-bottom: 1px dashed #e2e8f0;
}

.client-badge-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.client-badge-num {
  font-size: 11px;
  font-weight: 700;
  color: #ffffff;
  background-color: #3b82f6;
  padding: 2px 8px;
  border-radius: 9999px;
}

.client-badge-title {
  font-size: 13.5px;
  font-weight: 650;
  color: #1e293b;
}

.btn-delete-client {
  font-size: 13px;
  font-weight: 600;
  color: #ef4444;
}

.btn-delete-client:hover {
  color: #dc2626;
}

.field-label-with-icon {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600 !important;
  color: #475569 !important;
}

.switch-container {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 40px;
}

.switch-custom-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* Global Form item label formatting overrides */
.create-app-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.create-app-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
  font-size: 13.5px;
  padding-bottom: 8px;
  display: inline-flex !important;
  align-items: center;
  min-height: 28px;
}

/* Custom form components size overrides */
.create-app-form :deep(.el-input__wrapper),
.create-app-form :deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 8px;
  padding: 6px 12px;
  transition: all 0.2s;
  height: 40px;
  box-sizing: border-box;
}

.create-app-form :deep(.el-input__wrapper:hover),
.create-app-form :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.create-app-form :deep(.el-input__wrapper.is-focus),
.create-app-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #6366f1 inset !important;
}

.create-app-form :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 8px;
  padding: 10px 14px;
  transition: all 0.2s;
}

.create-app-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

.create-app-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #6366f1 inset !important;
}

/* Fixed Footer Glassmorphism Effect */
.page-footer {
  position: fixed;
  bottom: 0;
  left: 240px; /* Aligns with navigation sidebar */
  right: 0;
  height: 80px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  box-shadow: 0 -8px 30px rgba(0, 0, 0, 0.04);
  transition: left 0.3s;
}

.footer-inner {
  display: flex;
  gap: 16px;
  width: 100%;
  max-width: 1200px;
  padding: 0 40px;
  justify-content: flex-end;
}

.btn-cancel {
  width: 120px;
  font-weight: 600;
  border-color: #e2e8f0;
  color: #475569;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background-color: #f8fafc;
  color: #1e293b;
  border-color: #cbd5e1;
}

.btn-submit {
  min-width: 220px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
  transition: all 0.2s ease;
}

.btn-submit:hover {
  background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.35);
}

.btn-submit:active {
  transform: translateY(1px);
}

/* Animations */
.animate-fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

