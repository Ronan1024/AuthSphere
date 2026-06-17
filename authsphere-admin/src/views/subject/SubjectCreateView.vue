<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, CircleCheck, InfoFilled } from '@element-plus/icons-vue'

import { type RealmRecord, realmApi } from '@/api/realm'
import { type SubjectPayload, type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import { type AccountRecord, accountApi } from '@/api/account'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const router = useRouter()
const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const optionsLoading = ref(false)

// Select Options
const realmOptions = ref<RealmRecord[]>([])
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])
const accountOptions = ref<AccountRecord[]>([])

// Form Model
const form = reactive({
  name: '',
  code: '',
  subjectTypeId: '',
  status: 1, // 1: 启用, 2: 禁用
  description: '',
  parentSubjectId: '',
  realmId: '',
  rootSubjectId: '',
  
  // Management capability state (frontend mock config)
  allowCreateChild: 1, // 1: 允许, 0: 不允许
  allowCreateTypes: ['商户'], // mock checkboxes
  allowCreateMembers: 1, // 1: 允许, 0: 不允许
  defaultRelation: '管理员',

  // Initial Admin state (frontend mock config)
  adminAddMode: 'existing', // existing: 选择已有账号, new: 新建账号
  adminUsername: '',
  adminMemberType: '主体管理员',
  adminIsDefault: '1' // 1: 是, 0: 否
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入主体编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线和下划线',
      trigger: 'blur',
    }
  ],
  subjectTypeId: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  realmId: [{ required: true, message: '请选择所属身份域', trigger: 'change' }],
}

// Compute dynamic path preview
const pathPreview = computed(() => {
  const code = form.code || ''
  if (!form.parentSubjectId) {
    return `/${code}`
  }
  const parent = subjectOptions.value.find(s => s.id === form.parentSubjectId)
  const parentCode = parent ? parent.code : 'parent'
  return `/${parentCode}/${code}`
})

const goBack = () => {
  router.push('/subjects')
}

const fetchOptions = async () => {
  optionsLoading.value = true
  try {
    const [realmPage, subjectTypes, subjects, accountsPage] = await Promise.all([
      realmApi.page({ page: 1, size: 100 }),
      subjectTypeApi.list(),
      subjectApi.list(),
      accountApi.page({ page: 1, size: 100 })
    ])
    realmOptions.value = realmPage.records ?? []
    subjectTypeOptions.value = subjectTypes ?? []
    subjectOptions.value = subjects ?? []
    accountOptions.value = accountsPage.records ?? []
  } catch (error) {
    showErrorMessage('获取配置选项失败')
  } finally {
    optionsLoading.value = false
  }
}

const saveDraft = () => {
  showSuccessMessage('保存成功（草稿已保存）')
  router.push('/subjects')
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
      rootSubjectId: form.rootSubjectId || null,
      isRoot: form.rootSubjectId ? 0 : 1, // If rootSubjectId is set, it is not root
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
  <section class="subject-create-page" v-loading="optionsLoading">
    <div class="top-breadcrumb">
      <span class="crumb-back" @click="goBack"><el-icon><ArrowLeft /></el-icon> 返回主体列表</span>
      <span class="divider">/</span>
      <span class="crumb-curr">新增主体</span>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="create-form-container"
    >
      <!-- Section 1: 新增主体 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>新增主体</h2>
            <p>创建业务主体，用于承载成员账号、应用开通和数据边界。</p>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <el-form-item label="主体名称 *" prop="name">
            <el-input v-model="form.name" placeholder="请输入主体名称，例如：租户A" />
          </el-form-item>
          <el-form-item label="主体编码 *" prop="code">
            <el-input v-model="form.code" placeholder="请输入主体编码，例如：tenant_a" />
          </el-form-item>
          <el-form-item label="主体类型 *" prop="subjectTypeId">
            <el-select v-model="form.subjectTypeId" placeholder="请选择主体类型">
              <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态 *" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </el-form-item>
          <div class="span-2col">
            <el-form-item label="主体说明">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="请输入主体说明，用于描述主体的业务用途、隔离范围等。"
              />
            </el-form-item>
          </div>
        </div>
      </div>

      <!-- Section 2: 归属关系 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>归属关系</h2>
            <p>主体层级只表达管理归属，不代表自动继承角色权限。</p>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <el-form-item label="父级主体" prop="parentSubjectId">
            <el-select v-model="form.parentSubjectId" placeholder="请选择父级主体，不选为根" clearable>
              <el-option v-for="item in subjectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属身份域 *" prop="realmId">
            <el-select v-model="form.realmId" placeholder="请选择所属身份域">
              <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据隔离根主体" prop="rootSubjectId">
            <el-select v-model="form.rootSubjectId" placeholder="请选择数据隔离根主体" clearable>
              <el-option v-for="item in subjectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="主体路径预览">
            <el-input v-model="pathPreview" disabled class="code-preview" />
          </el-form-item>
        </div>
      </div>

      <!-- Section 3: 管理能力 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>管理能力</h2>
            <p>用于控制该主体后续是否可以创建成员账号和下级主体。</p>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <el-form-item label="允许创建下级主体">
            <div class="pill-selector">
              <div
                class="pill-option"
                :class="{ active: form.allowCreateChild === 1 }"
                @click="form.allowCreateChild = 1"
              >允许</div>
              <div
                class="pill-option"
                :class="{ active: form.allowCreateChild === 0 }"
                @click="form.allowCreateChild = 0"
              >不允许</div>
            </div>
          </el-form-item>

          <el-form-item label="可创建主体类型">
            <el-checkbox-group v-model="form.allowCreateTypes" class="custom-checkbox-group">
              <el-checkbox-button label="商户">商户</el-checkbox-button>
              <el-checkbox-button label="消费者">消费者</el-checkbox-button>
              <el-checkbox-button label="内部组织">内部组织</el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="允许创建成员账号">
            <div class="pill-selector">
              <div
                class="pill-option"
                :class="{ active: form.allowCreateMembers === 1 }"
                @click="form.allowCreateMembers = 1"
              >允许</div>
              <div
                class="pill-option"
                :class="{ active: form.allowCreateMembers === 0 }"
                @click="form.allowCreateMembers = 0"
              >不允许</div>
            </div>
          </el-form-item>

          <el-form-item label="默认成员关系">
            <el-select v-model="form.defaultRelation" placeholder="请选择">
              <el-option label="管理员" value="管理员" />
              <el-option label="普通成员" value="普通成员" />
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- Section 4: 初始管理员 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>初始管理员</h2>
            <p>可暂不添加，保存后在主体详情中继续维护成员账号。</p>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <el-form-item label="添加方式">
            <el-select v-model="form.adminAddMode">
              <el-option label="选择已有账号" value="existing" />
              <el-option label="新建账号" value="new" />
            </el-select>
          </el-form-item>

          <el-form-item label="账号">
            <el-select
              v-model="form.adminUsername"
              placeholder="搜索或选择已有用户账号"
              clearable
              filterable
            >
              <el-option
                v-for="item in accountOptions"
                :key="item.id"
                :label="`${item.username} / ${item.mobile || '无手机号'}`"
                :value="item.username"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="成员类型">
            <el-select v-model="form.adminMemberType">
              <el-option label="主体管理员" value="主体管理员" />
              <el-option label="普通成员" value="普通成员" />
            </el-select>
          </el-form-item>

          <el-form-item label="是否默认主体">
            <el-select v-model="form.adminIsDefault">
              <el-option label="是" value="1" />
              <el-option label="否" value="0" />
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- Action Row -->
      <div class="form-action-row">
        <el-button @click="goBack">取消</el-button>
        <el-button @click="saveDraft">保存为草稿</el-button>
        <el-button type="primary" :loading="saveLoading" @click="submitForm">保存并进入主体详情</el-button>
      </div>
    </el-form>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

.subject-create-page {
  --primary-color: #0369A1;      /* Security Blue */
  --primary-hover: #0284c7;
  --secondary-color: #0EA5E9;    /* Sky Blue */
  --success-color: #16A34A;      /* Protected Green */
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

.top-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 12px;
  color: var(--text-muted);
  font-family: var(--font-family-display);
}

.crumb-back {
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s ease;
}

.crumb-back:hover {
  color: var(--primary-color);
}

.crumb-curr {
  color: var(--text-main);
  font-weight: 500;
}

.create-form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
  overflow: hidden;
}

.section-card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title-wrap h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.header-title-wrap p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-muted);
}

.section-card-body {
  padding: 24px;
}

.grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}

.span-2col {
  grid-column: span 2;
}

/* Custom form components styling */
.code-preview :deep(.el-input__inner) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: var(--text-muted);
  background: #f8fafc;
}

.pill-selector {
  display: flex;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
  width: max-content;
}

.pill-option {
  padding: 8px 24px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  background: #ffffff;
  transition: all 0.2s ease;
  user-select: none;
}

.pill-option:not(:last-child) {
  border-right: 1px solid var(--border-light);
}

.pill-option.active {
  background: var(--bg-color);
  color: var(--primary-color);
}

.custom-checkbox-group :deep(.el-checkbox-button__inner) {
  border: 1px solid var(--border-light);
  border-radius: 8px !important;
  margin-right: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  padding: 8px 16px;
  background: #ffffff;
  box-shadow: none !important;
  transition: all 0.2s ease;
}

.custom-checkbox-group :deep(.el-checkbox-button.is-checked .el-checkbox-button__inner) {
  background-color: var(--bg-color);
  border-color: var(--secondary-color);
  color: var(--primary-color);
}

.form-action-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
  margin-bottom: 30px;
}
</style>
