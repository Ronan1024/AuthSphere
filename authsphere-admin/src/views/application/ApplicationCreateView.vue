<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, UploadFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()

const formData = reactive({
  name: '',
  code: '',
  url: '',
  icon: '',
  description: '',
  sort: 0,
  status: 1, // 1: 启用, 0: 禁用
  isBuiltIn: 0, // 0: 否, 1: 是
  tags: [] as string[],
  extConfigs: [] as { key: string; value: string }[]
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
  status: [
    { required: true, message: '请选择应用状态', trigger: 'change' }
  ]
}

const tagOptions = ref([
  { value: '内部系统', label: '内部系统' },
  { value: '核心业务', label: '核心业务' },
  { value: '工具类', label: '工具类' }
])

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

const addExtConfig = () => {
  formData.extConfigs.push({ key: '', value: '' })
}

const removeExtConfig = (index: number) => {
  formData.extConfigs.splice(index, 1)
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success('保存成功')
      router.push('/applications')
    }
  })
}
</script>

<template>
  <div class="application-create-page">
    <div class="page-header">
      <div class="header-left">
        <h1>新增应用</h1>
        <p>创建一个新的应用，定义应用的基本信息和访问配置。</p>
      </div>
      <div class="header-right">
        <el-button :icon="ArrowLeft" plain @click="handleBack">返回应用列表</el-button>
      </div>
    </div>

    <div class="form-container">
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-position="top"
        class="create-app-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">
            <span class="title-bar"></span>基本信息
          </div>
          <div class="form-grid-3">
            <el-form-item label="应用名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入应用名称" maxlength="64" show-word-limit />
            </el-form-item>
            <el-form-item label="应用编码" prop="code">
              <el-input v-model="formData.code" placeholder="请输入应用编码 (由小写字母、数字、下划线组成)" maxlength="64" show-word-limit />
            </el-form-item>
            <el-form-item label="应用入口地址" prop="url">
              <el-input v-model="formData.url" placeholder="请输入应用默认访问地址，如 https://xxx.example.com" maxlength="255" show-word-limit />
            </el-form-item>
          </div>

          <div class="form-grid-3 mt-4">
            <el-form-item label="应用图标">
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
                    <span>点击上传</span>
                  </div>
                </el-upload>
                <div class="upload-tip">
                  <p>支持 jpg、png、svg 格式</p>
                  <p>建议尺寸 128x128px，大小不超过 2MB</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="应用描述" prop="description" class="col-span-1">
              <el-input 
                v-model="formData.description" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入应用描述 (可选)" 
                maxlength="255" 
                show-word-limit 
              />
            </el-form-item>
            <el-form-item label="应用排序" prop="sort">
              <el-input-number v-model="formData.sort" :min="0" :max="9999" controls-position="right" class="w-full" />
              <div class="item-tip">数字越小排序越靠前，默认为 0</div>
            </el-form-item>
          </div>
        </div>

        <el-divider />

        <!-- 应用状态 -->
        <div class="form-section">
          <div class="section-title">
            <span class="title-bar"></span>应用状态
          </div>
          <div class="form-grid-3">
            <el-form-item label="应用状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
              <div class="item-tip mt-2 w-full">启用后，该应用可被主体开通并使用</div>
            </el-form-item>
            <el-form-item label="是否内置应用" prop="isBuiltIn">
              <el-radio-group v-model="formData.isBuiltIn">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
              <div class="item-tip mt-2 w-full">内置应用不可删除，且部分字段不可修改</div>
            </el-form-item>
          </div>
        </div>

        <el-divider />

        <!-- 扩展信息 -->
        <div class="form-section">
          <div class="section-title">
            <span class="title-bar"></span>扩展信息 <span class="title-optional">(可选)</span>
          </div>
          <div class="form-grid-3">
            <el-form-item label="应用标签" prop="tags">
              <el-select
                v-model="formData.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入标签"
                class="w-full"
              >
                <el-option
                  v-for="item in tagOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <div class="item-tip mt-2 w-full">为应用添加标签，便于分类管理</div>
            </el-form-item>
            <el-form-item label="扩展配置" class="col-span-2">
              <div class="ext-config-wrapper">
                <el-button plain :icon="Plus" @click="addExtConfig">添加配置项</el-button>
                <div class="item-tip mt-2">配置键值对形式的扩展信息</div>
                
                <div v-for="(item, index) in formData.extConfigs" :key="index" class="ext-config-item mt-3">
                  <el-input v-model="item.key" placeholder="键 (Key)" style="width: 200px" />
                  <span class="separator">:</span>
                  <el-input v-model="item.value" placeholder="值 (Value)" style="width: 300px" />
                  <el-button link type="danger" @click="removeExtConfig(index)">删除</el-button>
                </div>
              </div>
            </el-form-item>
          </div>
        </div>
      </el-form>
    </div>

    <div class="page-footer">
      <el-button @click="handleBack" size="large" class="footer-btn">取消</el-button>
      <el-button type="primary" @click="submitForm" size="large" class="footer-btn">保存并继续</el-button>
    </div>
  </div>
</template>

<style scoped>
.application-create-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 120px);
  background-color: #f9fafb;
}

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
  color: #6b7280;
  font-size: 13px;
}

.form-container {
  background: #ffffff;
  border-radius: 8px;
  padding: 32px 40px;
  border: 1px solid #eaeaea;
  margin-bottom: 80px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 24px;
}

.title-bar {
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #2563eb;
  border-radius: 2px;
  margin-right: 8px;
}

.title-optional {
  font-weight: normal;
  color: #9ca3af;
  margin-left: 8px;
  font-size: 14px;
}

.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0 40px;
}

.col-span-1 {
  grid-column: span 1;
}

.col-span-2 {
  grid-column: span 2;
}

.mt-4 {
  margin-top: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-3 {
  margin-top: 12px;
}

.w-full {
  width: 100%;
}

.item-tip {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.4;
}

/* Upload styles */
.upload-wrapper {
  display: flex;
  gap: 16px;
  align-items: center;
}

.icon-uploader {
  width: 128px;
  height: 128px;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  background-color: #f9fafb;
}

.icon-uploader:hover {
  border-color: #2563eb;
}

.uploader-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #9ca3af;
}

.upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.uploaded-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.upload-tip {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  color: #6b7280;
  font-size: 13px;
}

.upload-tip p {
  margin: 0;
}

/* Ext Config */
.ext-config-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.separator {
  color: #9ca3af;
  font-weight: 600;
}

/* Footer */
.page-footer {
  position: fixed;
  bottom: 0;
  left: 240px; /* Adjust based on sidebar width */
  right: 0;
  height: 72px;
  background: #ffffff;
  border-top: 1px solid #eaeaea;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  z-index: 10;
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.05);
}

.footer-btn {
  width: 140px;
}

/* Ensure padding on el-form-item doesn't break grid */
.create-app-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.create-app-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  padding-bottom: 8px;
}

.create-app-form :deep(.el-divider) {
  margin: 32px 0;
  border-color: #f3f4f6;
}
</style>
