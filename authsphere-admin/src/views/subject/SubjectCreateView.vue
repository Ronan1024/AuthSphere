<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

import { type RealmOption, realmApi } from '@/api/realm'
import { type SubjectPayload, type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const router = useRouter()
const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const optionsLoading = ref(false)

// Select Options
const realmOptions = ref<RealmOption[]>([])
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])

// Form Model
const form = reactive({
  name: '',
  code: '',
  subjectTypeId: '',
  status: 1, // 1: 启用, 2: 禁用
  description: '',
  parentSubjectId: '',
  realmId: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入主体编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线 and 下划线',
      trigger: 'blur',
    }
  ],
  subjectTypeId: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  realmId: [{ required: true, message: '请选择所属身份域', trigger: 'change' }],
}

const goBack = () => {
  router.push('/subjects')
}

const normalizeList = <T>(payload: unknown): T[] => {
  if (Array.isArray(payload)) {
    return payload as T[]
  }
  if (payload && typeof payload === 'object') {
    const data = (payload as { data?: unknown }).data
    if (Array.isArray(data)) {
      return data as T[]
    }
    if (data && typeof data === 'object' && Array.isArray((data as { records?: unknown }).records)) {
      return (data as { records: T[] }).records
    }
    if (Array.isArray((payload as { records?: unknown }).records)) {
      return (payload as { records: T[] }).records
    }
  }
  return []
}

const loadRealmOptions = async () => {
  const realms = await realmApi.list()
  const realmList = normalizeList<RealmOption>(realms)
  if (realmList.length > 0) {
    return realmList
  }
  const realmPage = await realmApi.page({ page: 1, size: 100 })
  return normalizeList<RealmOption>(realmPage)
}

const loadSubjectTypeOptions = async () => {
  const subjectTypes = await subjectTypeApi.list()
  const subjectTypeList = normalizeList<SubjectTypeRecord>(subjectTypes)
  if (subjectTypeList.length > 0) {
    return subjectTypeList
  }
  const subjectTypePage = await subjectTypeApi.page({ page: 1, size: 100 })
  return normalizeList<SubjectTypeRecord>(subjectTypePage)
}

const loadSubjectOptions = async () => {
  const subjects = await subjectApi.list()
  const subjectList = normalizeList<SubjectRecord>(subjects)
  if (subjectList.length > 0) {
    return subjectList
  }
  const subjectPage = await subjectApi.page({ page: 1, size: 100 })
  return normalizeList<SubjectRecord>(subjectPage)
}

const fetchOptions = async () => {
  optionsLoading.value = true
  try {
    const [realmsResult, subjectTypesResult, subjectsResult] = await Promise.allSettled([
      loadRealmOptions(),
      loadSubjectTypeOptions(),
      loadSubjectOptions(),
    ])

    if (realmsResult.status === 'fulfilled') {
      realmOptions.value = realmsResult.value ?? []
    } else {
      realmOptions.value = []
      showErrorMessage(realmsResult.reason instanceof Error ? realmsResult.reason.message : '获取身份域选项失败')
    }

    if (subjectTypesResult.status === 'fulfilled') {
      subjectTypeOptions.value = subjectTypesResult.value ?? []
    } else {
      subjectTypeOptions.value = []
      showErrorMessage(subjectTypesResult.reason instanceof Error ? subjectTypesResult.reason.message : '获取主体类型选项失败')
    }

    if (subjectsResult.status === 'fulfilled') {
      subjectOptions.value = subjectsResult.value ?? []
    } else {
      subjectOptions.value = []
      showErrorMessage(subjectsResult.reason instanceof Error ? subjectsResult.reason.message : '获取父级主体选项失败')
    }
  } catch (error) {
    showErrorMessage('获取配置选项失败')
  } finally {
    optionsLoading.value = false
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项')
    return
  }

  saveLoading.value = true
  try {
    const payload: SubjectPayload = {
      name: form.name,
      code: form.code,
      subjectTypeId: form.subjectTypeId,
      realmId: form.realmId,
      parentSubjectId: form.parentSubjectId || null,
      rootSubjectId: null,
      isRoot: form.parentSubjectId ? 0 : 1,
      builtIn: 0,
      description: form.description
    }

    await subjectApi.create(payload)
    showSuccessMessage('主体创建成功')
    router.push('/subjects')
  } catch (error: any) {
    showErrorMessage(error.message || '保存主体失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  fetchOptions()
})
</script>

<template>
  <div class="page-container" v-loading="optionsLoading">
    <div class="modal-card">
      <!-- Header -->
      <div class="modal-header">
        <h2 class="modal-title">新增主体</h2>
        <button class="close-btn" @click="goBack" aria-label="关闭">
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <!-- Form Body -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="modal-form"
      >
        <div class="form-grid">
          <!-- Row 1 -->
          <el-form-item label="主体名称 *" prop="name" class="form-item">
            <el-input v-model="form.name" placeholder="租户A" />
          </el-form-item>

          <el-form-item label="主体编码 *" prop="code" class="form-item">
            <el-input v-model="form.code" placeholder="tenant_a" />
          </el-form-item>

          <!-- Row 2 -->
          <el-form-item label="主体类型 *" prop="subjectTypeId" class="form-item">
            <el-select v-model="form.subjectTypeId" placeholder="请选择主体类型" class="w-full" popper-class="subject-type-select-popper">
              <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <div class="helper-text">由主体类型决定是否可签约应用、是否可创建下级主体。</div>
          </el-form-item>

          <el-form-item label="所属身份域 *" prop="realmId" class="form-item">
            <el-select v-model="form.realmId" placeholder="请选择所属身份域" class="w-full" popper-class="account-realm-select-popper">
              <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <div class="helper-text">主体的默认账号体系和应用签约范围来源。</div>
          </el-form-item>

          <!-- Row 3 -->
          <el-form-item label="父级主体" prop="parentSubjectId" class="form-item">
            <el-select v-model="form.parentSubjectId" placeholder="平台主体" clearable class="w-full" popper-class="subject-select-popper">
              <el-option v-for="item in subjectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="状态" prop="status" class="form-item">
            <el-select v-model="form.status" placeholder="启用" class="w-full">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </el-form-item>

          <!-- Row 4: Remarks -->
          <el-form-item label="备注" prop="description" class="form-item span-full">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="只保存主体基础信息；签约应用、客户端入口和账号角色在详情页处理。"
            />
          </el-form-item>
        </div>

        <!-- Footer Actions -->
        <div class="modal-footer">
          <el-button class="btn-cancel" @click="goBack">取消</el-button>
          <el-button type="primary" class="btn-submit" :loading="saveLoading" @click="submitForm">保存</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
  background-color: #f8fafc;
}

.modal-card {
  width: 100%;
  max-width: 800px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background-color: #f1f5f9;
  color: #64748b;
}

.modal-form {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 24px;
  row-gap: 16px;
}

.form-item {
  margin-bottom: 0; /* Let grid manage spacing */
}

.span-full {
  grid-column: span 2;
}

.w-full {
  width: 100%;
}

.helper-text {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

/* Custom Input & Select Styling overrides to match premium look */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  padding-bottom: 6px !important;
  font-size: 14px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 8px;
  padding: 8px 12px;
  transition: all 0.2s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #3b82f6 inset, 0 0 0 3px rgba(59, 130, 246, 0.15);
}

:deep(.el-select) {
  --el-select-border-color-hover: #cbd5e1;
  --el-select-input-focus-border-color: #3b82f6;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.btn-cancel {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  border-color: #e2e8f0;
  color: #475569;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}

.btn-submit {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  background-color: #2563eb;
  border-color: #2563eb;
  transition: all 0.2s ease;
}

.btn-submit:hover {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}
</style>
