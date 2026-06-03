<script setup lang="ts">
import {
  ArrowDown,
  ArrowLeft,
  Avatar,
  Calendar,
  CircleCheck,
  Connection,
  Delete,
  DocumentCopy,
  Download,
  Edit,
  Key,
  Link,
  Lock,
  Monitor,
  MoreFilled,
  OfficeBuilding,
  Plus,
  Refresh,
  Search,
  Setting,
  SwitchButton,
  User,
  UserFilled,
  View,
  Warning,
} from '@element-plus/icons-vue'
import { ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { type PageResult as RealmPageResult, type RealmRecord, realmApi } from '@/api/realm'
import { type PageResult, type SubjectPayload, type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

import SubjectMembersTab from './components/SubjectMembersTab.vue'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const loading = ref(false)
const optionLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const createMode = ref(false)
const saveLoading = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const formRef = ref<FormInstance>()
const currentDetail = ref<SubjectRecord>()
const selectedSubject = ref<SubjectRecord>()

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  realmId: undefined as string | undefined,
  subjectTypeId: undefined as string | undefined,
  isRoot: undefined as number | undefined,
  status: undefined as number | undefined,
})

const tableData = ref<SubjectRecord[]>([])
const total = ref(0)
const realmOptions = ref<RealmRecord[]>([])
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])

const availableSubjectOptions = computed(() =>
  subjectOptions.value.filter((item) => item.id !== editingId.value),
)

const currentPageEnabledCount = computed(() =>
  tableData.value.filter((item) => item.status === STATUS_NORMAL).length,
)
const currentPageDisabledCount = computed(() =>
  tableData.value.filter((item) => item.status === STATUS_DISABLED).length,
)
const currentPageRootCount = computed(() =>
  tableData.value.filter((item) => Number(item.isRoot) === 1).length,
)
const currentPageOrphanCount = computed(() =>
  tableData.value.filter((item) => !item.parentSubjectId && Number(item.isRoot) !== 1).length,
)

const statCards = computed(() => [
  {
    label: '总主体数',
    value: total.value,
    icon: View,
    tone: 'blue',
    suffix: '个',
  },
  {
    label: '启用主体数',
    value: currentPageEnabledCount.value,
    icon: CircleCheck,
    tone: 'green',
    suffix: '当前页',
  },
  {
    label: '禁用主体数',
    value: currentPageDisabledCount.value,
    icon: Warning,
    tone: 'orange',
    suffix: '当前页',
  },
  {
    label: '数据根主体数',
    value: currentPageRootCount.value,
    icon: Link,
    tone: 'purple',
    suffix: '当前页',
  },
  {
    label: '无父级主体数',
    value: currentPageOrphanCount.value,
    icon: MoreFilled,
    tone: 'slate',
    suffix: '当前页',
  },
])

const subjectCategoryOptions = computed(() => {
  const categories = new Set<string>()
  subjectTypeOptions.value.forEach((item) => {
    if (item.category) {
      categories.add(item.category)
    }
  })
  return Array.from(categories)
})

const relationPreview = computed(() => {
  const target = selectedSubject.value
  if (!target) {
    return []
  }
  const relations: Array<{ label: string; value: string; type: 'parent' | 'root' | 'child' }> = []
  if (target.parentSubjectId) {
    relations.push({
      label: '上级主体',
      value: target.parentSubjectName || target.parentSubjectCode || String(target.parentSubjectId),
      type: 'parent',
    })
  }
  if (target.rootSubjectId) {
    relations.push({
      label: '数据根',
      value: target.rootSubjectName || target.rootSubjectCode || String(target.rootSubjectId),
      type: 'root',
    })
  }
  subjectOptions.value
    .filter((item) => item.parentSubjectId === target.id)
    .slice(0, 4)
    .forEach((item) => {
      relations.push({
        label: '下级主体',
        value: item.name,
        type: 'child',
      })
    })
  return relations
})

const defaultForm = (): SubjectPayload => ({
  subjectTypeId: '',
  realmId: '',
  code: '',
  name: '',
  rootSubjectId: null,
  parentSubjectId: null,
  isRoot: 0,
  builtIn: 0,
  description: '',
})

const form = reactive<SubjectPayload>(defaultForm())

const rules: FormRules<SubjectPayload> = {
  subjectTypeId: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
  realmId: [{ required: true, message: '请选择身份域', trigger: 'change' }],
  code: [
    { required: true, message: '请输入主体编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线和下划线',
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
  isRoot: [{ required: true, message: '请选择是否数据隔离根主体', trigger: 'change' }],
  builtIn: [{ required: true, message: '请选择是否系统内置主体', trigger: 'change' }],
}

const getErrorMessage = (error: unknown, fallback = '操作失败') => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === 'string' && message) {
      return message
    }
  }
  return fallback
}

const normalizePage = (result: PageResult<SubjectRecord>) => {
  tableData.value = result.records ?? []
  total.value = Number(result.total ?? 0)
  if (!selectedSubject.value || !tableData.value.some((item) => item.id === selectedSubject.value?.id)) {
    selectedSubject.value = tableData.value[0]
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await subjectApi.page({
      page: query.page,
      size: query.size,
      name: query.name || undefined,
      code: query.code || undefined,
      realmId: query.realmId,
      subjectTypeId: query.subjectTypeId,
      isRoot: query.isRoot,
      status: query.status,
    })
    normalizePage(result)
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体列表失败'))
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  optionLoading.value = true
  try {
    const [realmPage, subjectTypes, subjects] = await Promise.all([
      realmApi.page({ page: 1, size: 100 }) as Promise<RealmPageResult<RealmRecord>>,
      subjectTypeApi.list(),
      subjectApi.list(),
    ])
    realmOptions.value = realmPage.records ?? []
    subjectTypeOptions.value = subjectTypes ?? []
    subjectOptions.value = subjects ?? []
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体表单选项失败'))
  } finally {
    optionLoading.value = false
  }
}

const handleOptionsVisible = (visible: boolean) => {
  if (visible && (!realmOptions.value.length || !subjectTypeOptions.value.length)) {
    fetchOptions()
  }
}

const resetQuery = () => {
  query.page = 1
  query.name = ''
  query.code = ''
  query.realmId = undefined
  query.subjectTypeId = undefined
  query.isRoot = undefined
  query.status = undefined
  fetchData()
}

const selectSubject = (row: SubjectRecord) => {
  selectedSubject.value = row
}

const assignForm = (value: SubjectPayload) => {
  Object.assign(form, value)
}

const toForm = (record: SubjectRecord): SubjectPayload => ({
  subjectTypeId: record.subjectTypeId,
  realmId: record.realmId,
  code: record.code,
  name: record.name,
  rootSubjectId: record.rootSubjectId || null,
  parentSubjectId: record.parentSubjectId || null,
  isRoot: Number(record.isRoot ?? 0),
  builtIn: Number(record.builtIn ?? 0),
  description: record.description || '',
})

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = undefined
  assignForm(defaultForm())
  createMode.value = true
  fetchOptions()
}

const openEdit = async (row: SubjectRecord) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  loading.value = true
  try {
    const detail = await subjectApi.detail(row.id)
    assignForm(toForm(detail))
    dialogVisible.value = true
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体详情失败'))
  } finally {
    loading.value = false
  }
}

const route = useRoute()
const router = useRouter()

const openDetail = (row: SubjectRecord) => {
  router.push({ path: '/subjects', query: { subjectId: row.id } })
}

const goBack = () => {
  router.push('/subjects')
}

watch(
  () => route.query.subjectId,
  async (newId) => {
    if (newId && typeof newId === 'string') {
      loading.value = true
      try {
        currentDetail.value = await subjectApi.detail(newId)
        detailVisible.value = true
      } catch (error) {
        showErrorMessage(getErrorMessage(error, '获取主体详情失败'))
        currentDetail.value = undefined
        detailVisible.value = false
      } finally {
        loading.value = false
      }
    } else {
      currentDetail.value = undefined
      detailVisible.value = false
    }
  },
  { immediate: true },
)

const buildPayload = (): SubjectPayload => ({
  ...form,
  rootSubjectId: form.rootSubjectId || null,
  parentSubjectId: form.parentSubjectId || null,
  isRoot: Number(form.isRoot),
  builtIn: Number(form.builtIn),
})

const submitForm = async () => {
  if (!formRef.value) {
    showErrorMessage('表单尚未初始化，请重新打开后再保存')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项和格式校验')
    return
  }

  saveLoading.value = true
  try {
    const payload = buildPayload()
    if (dialogMode.value === 'create') {
      await subjectApi.create(payload)
    } else if (editingId.value) {
      await subjectApi.update(editingId.value, payload)
    }
    showSuccessMessage(dialogMode.value === 'create' ? '主体已创建' : '主体已更新')
    dialogVisible.value = false
    createMode.value = false
    fetchData()
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存主体失败'))
  } finally {
    saveLoading.value = false
  }
}

const cancelCreate = () => {
  createMode.value = false
  assignForm(defaultForm())
}

const toggleStatus = async (row: SubjectRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响该主体进入系统和参与授权边界计算。确认${action}？`,
    `${action}主体`,
    {
      type: row.status === STATUS_NORMAL ? 'warning' : 'info',
      appendTo: 'body',
      modalClass: 'app-confirm-overlay',
      customClass: 'app-confirm-box',
      confirmButtonText: action,
      cancelButtonText: '取消',
    },
  )

  try {
    await subjectApi.toggleStatus(row.id)
    showSuccessMessage(`主体已${action}`)
    fetchData()
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, `${action}主体失败`))
  }
}

const removeSubject = async (row: SubjectRecord) => {
  if (Number(row.builtIn) === 1) {
    showErrorMessage('系统内置主体不能删除')
    return
  }

  await ElMessageBox.confirm(
    `删除后主体「${row.name}」将不可恢复。确认删除？`,
    '删除主体',
    {
      type: 'warning',
      appendTo: 'body',
      modalClass: 'app-confirm-overlay',
      customClass: 'app-confirm-box',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    },
  )

  try {
    await subjectApi.remove(row.id)
    showSuccessMessage('主体已删除')
    fetchData()
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '删除主体失败'))
  }
}

// 主体详情功能增强属性与方法
const activeDetailTab = ref('basic')

const copyText = (text?: string) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    showSuccessMessage('复制成功')
  }).catch(() => {
    showErrorMessage('复制失败')
  })
}

const mockOperations = computed(() => [
  {
    time: '2024-05-28 15:42:10',
    operator: 'admin',
    type: '编辑主体',
    content: `更新主体描述信息为: ${currentDetail.value?.description || '企业 AAA 总部主体'}`,
    source: '控制台',
  },
  {
    time: '2024-05-25 10:15:33',
    operator: 'admin',
    type: '新增应用实例',
    content: '新增应用实例: 商城系统 (v2.0)',
    source: '控制台',
  },
  {
    time: '2024-05-24 09:30:22',
    operator: 'admin',
    type: '新增关系',
    content: `新增关系: ${currentDetail.value?.name || '企业 AAA'} 管理 支付商户 (8个)`,
    source: '控制台',
  },
])

const openCreateWithParent = (parentId: string) => {
  dialogMode.value = 'create'
  editingId.value = undefined
  const formObj = defaultForm()
  formObj.parentSubjectId = parentId
  assignForm(formObj)
  createMode.value = true
  fetchOptions()
}

const addTagPrompt = () => {
  ElMessageBox.prompt('请输入标签名称', '添加标签', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\\S+/,
    inputErrorMessage: '标签不能为空',
  }).then(({ value }) => {
    showSuccessMessage(`添加标签「\${value}」成功(前端模拟)`)
  }).catch(() => {})
}

const showFeaturePlaceholder = (featureName: string) => {
  ElMessageBox.alert(`\${featureName}功能正在接入中，敬请期待！`, '系统提示', {
    confirmButtonText: '确定',
    type: 'info',
  })
}

fetchData()
fetchOptions()
</script>

<template>
  <section class="subject-page">
    <template v-if="createMode">
      <div class="create-layout">
        <main class="create-main">
          <el-form
            id="subject-create-form"
            ref="formRef"
            v-loading="optionLoading"
            :model="form"
            :rules="rules"
            label-position="top"
            class="wizard-card single-page-form"
            @submit.prevent="submitForm"
          >
            <div class="form-section">
              <h2>主体基本信息</h2>
              <el-row :gutter="32">
                <el-col :span="12">
                  <el-form-item label="主体名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入主体名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主体编码" prop="code">
                    <el-input v-model="form.code" placeholder="请输入主体编码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="32">
                <el-col :span="12">
                  <el-form-item label="主体类型" prop="subjectTypeId">
                    <el-select
                      v-model="form.subjectTypeId"
                      filterable
                      placeholder="请选择主体类型"
                      popper-class="subject-select-popper"
                      @visible-change="handleOptionsVisible"
                    >
                      <el-option
                        v-for="item in subjectTypeOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      >
                        <span>{{ item.name }}</span>
                        <small class="option-code">{{ item.code }}</small>
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主体描述">
                    <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <el-divider />

            <div class="form-section">
              <h2>关系与归属</h2>
              <el-row :gutter="32">
                <el-col :span="12">
                  <el-form-item label="关系来源主体">
                    <el-select
                      v-model="form.parentSubjectId"
                      clearable
                      filterable
                      placeholder="请选择上级主体"
                      popper-class="subject-select-popper"
                      @visible-change="handleOptionsVisible"
                    >
                      <el-option
                        v-for="item in availableSubjectOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      >
                        <span>{{ item.name }}</span>
                        <small class="option-code">{{ item.code }}</small>
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="数据隔离根主体">
                    <el-select
                      v-model="form.rootSubjectId"
                      clearable
                      filterable
                      placeholder="请选择数据隔离根"
                      popper-class="subject-select-popper"
                      @visible-change="handleOptionsVisible"
                    >
                      <el-option
                        v-for="item in availableSubjectOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      >
                        <span>{{ item.name }}</span>
                        <small class="option-code">{{ item.code }}</small>
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="switch-grid">
                <label>
                  <span>
                    <strong>当前主体是否作为数据隔离根</strong>
                    <small>开启后该主体作为数据权限和隔离边界的根节点。</small>
                  </span>
                  <el-switch v-model="form.isRoot" :active-value="1" :inactive-value="0" />
                </label>
              </div>
              <div class="wizard-relation-preview">
                <div>{{ form.parentSubjectId ? '上级主体' : '未选择上级主体' }}</div>
                <span>→</span>
                <div>当前主体</div>
                <span>→</span>
                <div>{{ form.rootSubjectId || Number(form.isRoot) === 1 ? '数据隔离根已配置' : '未配置数据隔离根' }}</div>
              </div>
            </div>

            <el-divider />

            <div class="form-section">
              <h2>身份域与配置</h2>
              <el-row :gutter="32">
                <el-col :span="12">
                  <el-form-item label="默认身份域" prop="realmId">
                    <el-select
                      v-model="form.realmId"
                      filterable
                      placeholder="请选择身份域"
                      popper-class="subject-select-popper"
                      @visible-change="handleOptionsVisible"
                    >
                      <el-option
                        v-for="item in realmOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      >
                        <span>{{ item.name }}</span>
                        <small class="option-code">{{ item.code }}</small>
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="switch-grid">
                <label>
                  <span>
                    <strong>系统内置主体</strong>
                    <small>内置主体不允许删除，适用于初始化或平台级主体。</small>
                  </span>
                  <el-switch v-model="form.builtIn" :active-value="1" :inactive-value="0" />
                </label>
              </div>
            </div>

            <div class="wizard-footer">
              <el-button @click="cancelCreate">取消</el-button>
              <el-button
                type="primary"
                :loading="saveLoading"
                @click="submitForm"
              >
                确认创建
              </el-button>
            </div>
          </el-form>
        </main>

        <aside class="create-summary">
          <h3>创建概要</h3>
          <dl>
            <dt>主体名称</dt><dd>{{ form.name || '-' }}</dd>
            <dt>主体编码</dt><dd>{{ form.code || '-' }}</dd>
            <dt>主体类型</dt><dd>{{ subjectTypeOptions.find((item) => item.id === form.subjectTypeId)?.name || '-' }}</dd>
            <dt>默认身份域</dt><dd>{{ realmOptions.find((item) => item.id === form.realmId)?.name || '-' }}</dd>
            <dt>是否数据隔离根</dt><dd>{{ Number(form.isRoot) === 1 ? '是' : '否' }}</dd>
            <dt>系统内置</dt><dd>{{ Number(form.builtIn) === 1 ? '是' : '否' }}</dd>
          </dl>
          <div class="summary-tip">
            创建后可在主体详情中继续维护成员、关系、身份域与更多配置信息。
          </div>
        </aside>
      </div>
    </template>

    <template v-else-if="detailVisible && currentDetail">
      <div class="subject-detail-view">
        <!-- Header -->
        <div class="detail-header-card">
          <div class="header-main-row">
            <div class="left-section">
              <el-button class="back-btn" @click="goBack" circle>
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <div class="subject-icon-box">
                <span>{{ currentDetail.name?.slice(0, 1) || '-' }}</span>
              </div>
              <div class="title-and-status">
                <div class="title-row">
                  <h1>{{ currentDetail.name }}</h1>
                  <el-tag :type="currentDetail.status === STATUS_NORMAL ? 'success' : 'danger'" class="status-tag" size="small">
                    {{ currentDetail.status === STATUS_NORMAL ? '启用' : '禁用' }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div class="right-actions">
              <el-button type="default" @click="openEdit(currentDetail)">编辑</el-button>
              <el-button :type="currentDetail.status === STATUS_NORMAL ? 'danger' : 'success'" plain @click="toggleStatus(currentDetail)">
                {{ currentDetail.status === STATUS_NORMAL ? '禁用' : '启用' }}
              </el-button>
              <el-dropdown trigger="click">
                <el-button>更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :disabled="Number(currentDetail.builtIn) === 1" @click="removeSubject(currentDetail)">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div class="header-meta-row">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ currentDetail.subjectTypeName || currentDetail.subjectTypeCode || '企业主体' }} ({{ currentDetail.subjectTypeCode?.toUpperCase() || 'ENTERPRISE' }})
            </span>
            <span class="meta-item">
              <el-icon><Postcard /></el-icon>
              主体编码: <strong>{{ currentDetail.code }}</strong>
              <el-button link type="primary" class="copy-btn" @click="copyText(currentDetail.code)">
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
            </span>
            <span class="meta-item">
              <el-icon><OfficeBuilding /></el-icon>
              主体分类: {{ subjectTypeOptions.find(t => t.id === currentDetail?.subjectTypeId)?.category || '企业类' }}
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              创建时间: {{ currentDetail.createTime || '2024-05-21 11:20:33' }}
            </span>
            <span class="meta-item">
              <el-icon><UserFilled /></el-icon>
              创建人: admin
            </span>
          </div>
        </div>

        <!-- Navigation Tabs -->
        <el-tabs v-model="activeDetailTab" class="detail-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <!-- 3-Column Layout -->
            <div class="detail-grid">
              <!-- Left: Basic Info Card -->
              <div class="info-card">
                <div class="card-header">
                  <h3>基本信息</h3>
                </div>
                <div class="card-body">
                  <div class="attribute-list">
                    <div class="attr-item">
                      <span class="attr-label">主体名称</span>
                      <strong class="attr-value">{{ currentDetail.name }}</strong>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">主体编码</span>
                      <span class="attr-value">
                        {{ currentDetail.code }}
                        <el-button link type="primary" class="copy-btn" @click="copyText(currentDetail.code)">
                          <el-icon><DocumentCopy /></el-icon>
                        </el-button>
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">主体类型</span>
                      <span class="attr-value type-link">
                        {{ currentDetail.subjectTypeName || currentDetail.subjectTypeCode || '企业主体' }} ({{ currentDetail.subjectTypeCode?.toUpperCase() || 'ENTERPRISE' }})
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">主体分类</span>
                      <span class="attr-value">{{ subjectTypeOptions.find(t => t.id === currentDetail?.subjectTypeId)?.category || '企业类' }}</span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">主体别名</span>
                      <span class="attr-value">—</span>
                    </div>
                    <div class="attr-item description-row">
                      <span class="attr-label">主体描述</span>
                      <span class="attr-value description-text">{{ currentDetail?.description || '暂无描述信息。' }}</span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">主体状态</span>
                      <span class="attr-value status-dot-wrapper">
                        <span class="status-dot green" v-if="currentDetail.status === STATUS_NORMAL"></span>
                        <span class="status-dot red" v-else></span>
                        {{ currentDetail.status === STATUS_NORMAL ? '启用' : '禁用' }}
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">是否内置主体</span>
                      <span class="attr-value">{{ Number(currentDetail.builtIn) === 1 ? '是' : '否' }}</span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">排序号</span>
                      <span class="attr-value">10</span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">标签</span>
                      <span class="attr-value tags-row">
                        <el-tag size="small" type="primary" effect="light">总部</el-tag>
                        <el-button link type="primary" size="small" class="add-tag-btn" @click="addTagPrompt">+ 添加标签</el-button>
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Middle: Realm & Data Isolation Card -->
              <div class="info-card">
                <div class="card-header">
                  <h3>身份域与数据隔离</h3>
                </div>
                <div class="card-body">
                  <div class="attribute-list">
                    <div class="attr-item">
                      <span class="attr-label">默认身份域</span>
                      <span class="attr-value link-value">
                        {{ currentDetail.realmName || '企业客户身份域' }} ({{ currentDetail.realmCode || 'realm_ent' }})
                        <el-button link type="primary" class="arrow-link" @click="showFeaturePlaceholder('查看身份域')">查看身份域 ↗</el-button>
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">身份域状态</span>
                      <span class="attr-value status-dot-wrapper">
                        <span class="status-dot green"></span>
                        启用
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">数据隔离根主体</span>
                      <span class="attr-value link-value">
                        {{ Number(currentDetail.isRoot) === 1 ? currentDetail.name : (currentDetail.rootSubjectName || '企业 AAA') }} 
                        ({{ Number(currentDetail.isRoot) === 1 ? currentDetail.code : (currentDetail.rootSubjectCode || 'ent_aaa') }})
                        <el-button link type="primary" class="arrow-link" @click="showFeaturePlaceholder('查看数据隔离根主体')">查看</el-button>
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">是否数据隔离根</span>
                      <span class="attr-value status-dot-wrapper">
                        <span class="status-dot green" v-if="Number(currentDetail.isRoot) === 1"></span>
                        <span class="status-dot grey" v-else></span>
                        {{ Number(currentDetail.isRoot) === 1 ? '是' : '否' }}
                      </span>
                    </div>
                    <div class="attr-item description-row">
                      <span class="attr-label">数据隔离范围</span>
                      <span class="attr-value description-text">本主体及所有下级主体的数据，均属于此隔离范围。</span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">上级数据根主体</span>
                      <span class="attr-value link-value">
                        {{ currentDetail.parentSubjectName || '平台主体' }} ({{ currentDetail.parentSubjectCode || 'platform' }})
                        <el-button link type="primary" class="arrow-link" @click="showFeaturePlaceholder('查看上级数据根主体')">查看</el-button>
                      </span>
                    </div>
                    <div class="attr-item">
                      <span class="attr-label">继承自</span>
                      <span class="attr-value">—</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Right: Statistics & Quick Actions -->
              <div class="right-column">
                <div class="info-card stats-card">
                  <div class="card-header">
                    <h3>统计信息</h3>
                  </div>
                  <div class="card-body grid-body">
                    <div class="stat-item">
                      <span class="stat-icon-wrapper blue">
                        <el-icon><User /></el-icon>
                      </span>
                      <div class="stat-info">
                        <span class="stat-label">直接下级主体</span>
                        <strong class="stat-number">{{ subjectOptions.filter(item => item.parentSubjectId === currentDetail?.id).length || 8 }}</strong>
                      </div>
                    </div>
                    <div class="stat-item">
                      <span class="stat-icon-wrapper blue-light">
                        <el-icon><UserFilled /></el-icon>
                      </span>
                      <div class="stat-info">
                        <span class="stat-label">所有下级主体</span>
                        <strong class="stat-number">32</strong>
                      </div>
                    </div>
                    <div class="stat-item">
                      <span class="stat-icon-wrapper orange">
                        <el-icon><Avatar /></el-icon>
                      </span>
                      <div class="stat-info">
                        <span class="stat-label">成员账号</span>
                        <strong class="stat-number">126</strong>
                      </div>
                    </div>
                    <div class="stat-item">
                      <span class="stat-icon-wrapper orange-light">
                        <el-icon><Monitor /></el-icon>
                      </span>
                      <div class="stat-info">
                        <span class="stat-label">应用实例</span>
                        <strong class="stat-number">6</strong>
                      </div>
                    </div>
                    <div class="stat-item">
                      <span class="stat-icon-wrapper green">
                        <el-icon><CircleCheck /></el-icon>
                      </span>
                      <div class="stat-info">
                        <span class="stat-label">角色数量</span>
                        <strong class="stat-number">24</strong>
                      </div>
                    </div>
                    <div class="stat-item">
                      <span class="stat-icon-wrapper green-light">
                        <el-icon><Lock /></el-icon>
                      </span>
                      <div class="stat-info">
                        <span class="stat-label">权限数量</span>
                        <strong class="stat-number">156</strong>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="info-card actions-card">
                  <div class="card-header">
                    <h3>快捷操作</h3>
                  </div>
                  <div class="card-body action-grid">
                    <el-button class="action-btn" type="default" plain :icon="Connection" @click="activeDetailTab = 'relation'">
                      查看关系视图
                    </el-button>
                    <el-button class="action-btn" type="default" plain :icon="Plus" @click="openCreateWithParent(currentDetail?.id)">
                      新增下级主体
                    </el-button>
                    <el-button class="action-btn" type="default" plain :icon="User" @click="showFeaturePlaceholder('新增成员账号')">
                      新增成员账号
                    </el-button>
                    <el-button class="action-btn" type="default" plain :icon="Key" @click="showFeaturePlaceholder('分配权限')">
                      分配权限
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Relationships Section (3 small side-by-side tables) -->
            <div class="relation-section-grid">
              <!-- Left: Parent Relationship -->
              <div class="relation-table-card">
                <div class="relation-card-header">
                  <h4>上级关系（被管理/监督）</h4>
                  <el-button link type="primary" class="view-all-link" @click="activeDetailTab = 'relation'">查看全部 ↗</el-button>
                </div>
                <el-table :data="currentDetail.parentSubjectId ? [{ name: currentDetail.parentSubjectName, code: currentDetail.parentSubjectCode, type: '管理 (MANAGE)', scope: 'IAM', isMain: '是', status: '启用' }] : [{ name: '平台主体', code: 'platform', type: '管理 (MANAGE)', scope: 'IAM', isMain: '是', status: '启用' }]" size="small" class="mini-table">
                  <el-table-column label="上级主体" min-width="120">
                    <template #default="{ row }">
                      <div class="relation-subject-cell">
                        <span class="mini-avatar">B</span>
                        <div>
                          <strong>{{ row.name }}</strong>
                          <span>{{ row.code }}</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="关系类型" width="100" />
                  <el-table-column prop="scope" label="作用域" width="70" />
                  <el-table-column prop="isMain" label="是否主关系" width="80" align="center" />
                  <el-table-column prop="status" label="状态" width="70">
                    <template #default="{ row }">
                      <el-tag size="small" type="success" effect="light">{{ row.status }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="relation-count-footer">
                  共 1 条
                </div>
              </div>

              <!-- Middle: Lower Relationship -->
              <div class="relation-table-card">
                <div class="relation-card-header">
                  <h4>下级关系（管理/服务/监督过滤）</h4>
                  <el-button link type="primary" class="view-all-link" @click="activeDetailTab = 'relation'">查看全部 ↗</el-button>
                </div>
                <el-table :data="[
                  { name: '商户主体', code: 'MERCHANT', type: '管理', rel: 'MANAGE', scope: 'MALL', count: 12, status: '启用', avatar: '商' },
                  { name: '支付商户', code: 'PAY_MERCHANT', type: '管理', rel: 'MANAGE', scope: 'PAYMENT', count: 8, status: '启用', avatar: '支' },
                  { name: '门店主体', code: 'STORE', type: '管理', rel: 'MANAGE', scope: 'MALL', count: 10, status: '启用', avatar: '门' },
                  { name: '应用实例', code: 'APP_INSTANCE', type: '拥有', rel: 'OWN', scope: 'IAM', count: 6, status: '启用', avatar: '应' }
                ]" size="small" class="mini-table">
                  <el-table-column label="下级主体" min-width="120">
                    <template #default="{ row }">
                      <div class="relation-subject-cell">
                        <span class="mini-avatar green-avatar">{{ row.avatar }}</span>
                        <div>
                          <strong>{{ row.name }}</strong>
                          <span>{{ row.code }}</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="关系类型" width="110">
                    <template #default="{ row }">
                      {{ row.type }} ({{ row.rel }})
                    </template>
                  </el-table-column>
                  <el-table-column prop="scope" label="作用域" width="80" />
                  <el-table-column prop="count" label="数量" width="60" align="center" />
                  <el-table-column prop="status" label="状态" width="70">
                    <template #default="{ row }">
                      <el-tag size="small" type="success" effect="light">{{ row.status }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="relation-count-footer">
                  共 4 条
                </div>
              </div>

              <!-- Right: Other/Related Relationship -->
              <div class="relation-table-card">
                <div class="relation-card-header">
                  <h4>相关关系（除主关系外）</h4>
                  <el-button link type="primary" class="view-all-link" @click="activeDetailTab = 'relation'">查看全部 ↗</el-button>
                </div>
                <el-table :data="[
                  { name: '服务商 A', code: 'provider_a', type: '服务 (SERVICE)', scope: 'PAYMENT', isMain: '否', status: '启用', avatar: '服' },
                  { name: '支付平台', code: 'pay_platform', type: '监督 (SUPERVISE)', scope: 'PAYMENT', isMain: '否', status: '启用', avatar: '支' }
                ]" size="small" class="mini-table">
                  <el-table-column label="关联主体" min-width="120">
                    <template #default="{ row }">
                      <div class="relation-subject-cell">
                        <span class="mini-avatar orange-avatar">{{ row.avatar }}</span>
                        <div>
                          <strong>{{ row.name }}</strong>
                          <span>{{ row.code }}</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="关系类型" width="120" />
                  <el-table-column prop="scope" label="作用域" width="95" />
                  <el-table-column prop="isMain" label="主关系" width="70" align="center" />
                  <el-table-column prop="status" label="状态" width="70">
                    <template #default="{ row }">
                      <el-tag size="small" type="success" effect="light">{{ row.status }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="relation-count-footer">
                  共 2 条
                </div>
              </div>
            </div>

            <!-- bottom: Recent Operations Table -->
            <div class="info-card operation-log-card">
              <div class="card-header logs-header">
                <h3>最近操作记录</h3>
                <el-button link type="primary" @click="activeDetailTab = 'operations'">查看全部 ↗</el-button>
              </div>
              <el-table :data="mockOperations" size="default" class="logs-table">
                <el-table-column prop="time" label="操作时间" width="180" />
                <el-table-column prop="operator" label="操作人" width="120" />
                <el-table-column prop="type" label="操作类型" width="140">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.type === '编辑主体' ? 'primary' : 'success'">{{ row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="content" label="操作内容" min-width="300" />
                <el-table-column prop="source" label="来源" width="120" />
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="关系视图" name="relation">
            <div class="tab-placeholder-card">
              <el-icon><Connection /></el-icon>
              <h4>关系视图数据呈现</h4>
              <p>以当前主体为节点的数据路由关系拓扑图正在规划设计中，可通过上述列表直观检索管理上下级关系信息。</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="成员管理" name="members">
            <SubjectMembersTab
              v-if="activeDetailTab === 'members' && currentDetail"
              :subject-id="currentDetail.id"
              :realm-code="currentDetail.realmCode"
            />
          </el-tab-pane>
          <el-tab-pane label="应用实例" name="apps">
            <div class="tab-placeholder-card">
              <el-icon><Monitor /></el-icon>
              <h4>授权接入的应用实例</h4>
              <p>当前主体关联配置的企业级应用、业务系统和第三方组件实例入口列表。</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="权限授权" name="perms">
            <div class="tab-placeholder-card">
              <el-icon><Key /></el-icon>
              <h4>已授角色与策略清单</h4>
              <p>为该主体配置的角色映射、权限矩阵及属性条件控制策略明细。</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="审计日志" name="audit">
            <div class="tab-placeholder-card">
              <el-icon><CircleCheck /></el-icon>
              <h4>审计合规性检查记录</h4>
              <p>全周期审计安全合规日志，包含与该安全实体绑定的授权鉴权凭证下发追踪信息。</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="操作记录" name="operations">
            <div class="tab-placeholder-card">
              <el-icon><Calendar /></el-icon>
              <h4>完备的历史操作溯源</h4>
              <p>追踪与本安全主体相关的全量操作记录审计，包括主体参数更新、关系变更和状态切换日志。</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <template v-else>
    <div class="metrics-grid">
      <div v-for="item in statCards" :key="item.label" class="metric-card">
        <span class="metric-icon" :class="`metric-${item.tone}`">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <div>
          <span class="metric-label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.suffix }}</small>
        </div>
      </div>
    </div>

    <section class="filter-panel">
      <div class="filter-grid">
        <label>
          <span>主体名称</span>
          <el-input
            v-model="query.name"
            clearable
            placeholder="请输入主体名称"
            @keyup.enter="fetchData"
          />
        </label>
        <label>
          <span>主体编码</span>
          <el-input
            v-model="query.code"
            clearable
            placeholder="请输入主体编码"
            @keyup.enter="fetchData"
          />
        </label>
        <label>
          <span>主体类型</span>
          <el-select
            v-model="query.subjectTypeId"
            clearable
            filterable
            placeholder="请选择主体类型"
            popper-class="subject-select-popper"
            @visible-change="handleOptionsVisible"
          >
            <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </label>
        <label>
          <span>主体分类</span>
          <el-select disabled clearable placeholder="后端暂未提供分类筛选">
            <el-option v-for="item in subjectCategoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </label>
        <label>
          <span>默认身份域</span>
          <el-select
            v-model="query.realmId"
            clearable
            filterable
            placeholder="请选择身份域"
            popper-class="subject-select-popper"
            @visible-change="handleOptionsVisible"
          >
            <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </label>
        <label>
          <span>主体状态</span>
          <el-select v-model="query.status" clearable placeholder="请选择状态">
            <el-option label="启用" :value="STATUS_NORMAL" />
            <el-option label="禁用" :value="STATUS_DISABLED" />
          </el-select>
        </label>
        <label>
          <span>是否数据根</span>
          <el-select v-model="query.isRoot" clearable placeholder="全部">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </label>
        <label>
          <span>关系类型</span>
          <el-select disabled clearable placeholder="后端暂未提供关系筛选" />
        </label>
      </div>
      <div class="filter-actions">
        <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </section>

    <section class="subject-table-shell">
      <div class="action-bar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增主体</el-button>
          <el-button :icon="CircleCheck" disabled>批量启用</el-button>
          <el-button :icon="Warning" disabled>批量禁用</el-button>
          <el-button :icon="Download" disabled>导出</el-button>
        </div>
        <div class="right-actions">
          <el-button :icon="Setting" circle />
          <el-button :icon="Refresh" circle @click="fetchData" />
        </div>
      </div>

      <div class="table-and-preview">
        <div class="table-area">
          <el-table
            v-loading="loading"
            :data="tableData"
            row-key="id"
            empty-text="暂无主体"
            @row-click="selectSubject"
          >
            <el-table-column type="selection" width="42" />
            <el-table-column label="主体名称" min-width="170">
              <template #default="{ row }">
                <div class="subject-name-cell">
                  <span class="subject-avatar">{{ row.name?.slice(0, 1) || '-' }}</span>
                  <div>
                    <strong>{{ row.name }}</strong>
                    <span>{{ row.code }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="主体类型" min-width="130">
              <template #default="{ row }">
                <span>{{ row.subjectTypeName || row.subjectTypeCode || row.subjectTypeId }}</span>
              </template>
            </el-table-column>
            <el-table-column label="默认身份域" min-width="140">
              <template #default="{ row }">
                <span>{{ row.realmName || row.realmCode || row.realmId }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数据根主体" min-width="140">
              <template #default="{ row }">
                <span>{{ Number(row.isRoot) === 1 ? row.name : row.rootSubjectName || row.rootSubjectCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="是否数据根" width="110">
              <template #default="{ row }">
                <el-tag :type="Number(row.isRoot) === 1 ? 'success' : 'info'">
                  {{ Number(row.isRoot) === 1 ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="父级关系" min-width="150">
              <template #default="{ row }">
                <span>{{ row.parentSubjectName || row.parentSubjectCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="关系数" width="90" align="center">
              <template #default="{ row }">
                {{ subjectOptions.filter((item) => item.parentSubjectId === row.id).length }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === STATUS_NORMAL ? 'success' : 'danger'">
                  {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="异常标识" width="110">
              <template #default="{ row }">
                <el-tag v-if="!row.parentSubjectId && Number(row.isRoot) !== 1" type="warning">无关系</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="160" />
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openDetail(row)">详情</el-button>
                <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
                <el-button link type="primary" @click.stop="selectSubject(row)">关系</el-button>
                <el-dropdown trigger="click" @click.stop>
                  <el-button link type="primary" :icon="MoreFilled" />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="toggleStatus(row)">
                        {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        :disabled="Number(row.builtIn) === 1"
                        divided
                        @click="removeSubject(row)"
                      >
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.size"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @change="fetchData"
            />
          </div>
        </div>

        <aside class="relation-preview">
          <div class="preview-header">
            <strong>关系预览</strong>
            <el-button :icon="Refresh" text @click="fetchOptions" />
          </div>
          <template v-if="selectedSubject">
            <div class="preview-subject">
              <span class="subject-avatar large">{{ selectedSubject.name?.slice(0, 1) || '-' }}</span>
              <div>
                <strong>{{ selectedSubject.name }}</strong>
                <span>{{ selectedSubject.code }}</span>
              </div>
            </div>
            <div class="preview-meta">
              <span>{{ selectedSubject.subjectTypeName || selectedSubject.subjectTypeCode }}</span>
              <span>{{ selectedSubject.realmName || selectedSubject.realmCode }}</span>
            </div>
            <div class="relation-list">
              <div v-if="!relationPreview.length" class="empty-preview">
                暂无直接关系
              </div>
              <div v-for="item in relationPreview" :key="`${item.type}-${item.value}`" class="relation-item">
                <span :class="`relation-dot ${item.type}`" />
                <div>
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.value }}</span>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="empty-preview">
            选择一条主体记录查看关系
          </div>
        </aside>
      </div>
    </section>
    </template>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增主体' : '编辑主体'"
      width="820px"
      class="subject-dialog"
      destroy-on-close
    >
      <el-form
        id="subject-form"
        ref="formRef"
        v-loading="optionLoading"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submitForm"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="主体名称" prop="name">
              <el-input v-model="form.name" placeholder="SaaS运营方" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主体编码" prop="code">
              <el-input v-model="form.code" :disabled="dialogMode === 'edit'" placeholder="saas_platform" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份域" prop="realmId">
              <el-select
                v-model="form.realmId"
                filterable
                placeholder="请选择身份域"
                popper-class="subject-select-popper"
                @visible-change="handleOptionsVisible"
              >
                <el-option
                  v-for="item in realmOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <span>{{ item.name }}</span>
                  <small class="option-code">{{ item.code }}</small>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主体类型" prop="subjectTypeId">
              <el-select
                v-model="form.subjectTypeId"
                filterable
                placeholder="请选择主体类型"
                popper-class="subject-select-popper"
                @visible-change="handleOptionsVisible"
              >
                <el-option
                  v-for="item in subjectTypeOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <span>{{ item.name }}</span>
                  <small class="option-code">{{ item.code }}</small>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="父级主体">
              <el-select
                v-model="form.parentSubjectId"
                clearable
                filterable
                placeholder="请选择父级主体"
                popper-class="subject-select-popper"
                @visible-change="handleOptionsVisible"
              >
                <el-option
                  v-for="item in availableSubjectOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <span>{{ item.name }}</span>
                  <small class="option-code">{{ item.code }}</small>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据隔离根主体">
              <el-select
                v-model="form.rootSubjectId"
                clearable
                filterable
                placeholder="请选择数据隔离根"
                popper-class="subject-select-popper"
                @visible-change="handleOptionsVisible"
              >
                <el-option
                  v-for="item in availableSubjectOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <span>{{ item.name }}</span>
                  <small class="option-code">{{ item.code }}</small>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>

        <div class="switch-grid">
          <label>
            <span>
              <strong>数据隔离根主体</strong>
              <small>开启后该主体作为数据权限和隔离边界的根节点。</small>
            </span>
            <el-switch v-model="form.isRoot" :active-value="1" :inactive-value="0" />
          </label>
          <label>
            <span>
              <strong>系统内置主体</strong>
              <small>内置主体不允许删除，适用于初始化或平台级主体。</small>
            </span>
            <el-switch v-model="form.builtIn" :active-value="1" :inactive-value="0" />
          </label>
        </div>
      </el-form>

      <template #footer>
        <el-button native-type="button" @click="dialogVisible = false">取消</el-button>
        <el-button form="subject-form" native-type="submit" type="primary" :loading="saveLoading">
          保存
        </el-button>
      </template>
    </el-dialog>

  </section>
</template>

<style scoped>
.subject-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(160px, 1fr));
  gap: 12px;
}

.metric-card {
  display: flex;
  gap: 14px;
  align-items: center;
  min-height: 86px;
  padding: 16px 18px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.04);
}

.metric-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  color: #ffffff;
  font-size: 20px;
}

.metric-blue {
  background: #2f7df6;
}

.metric-green {
  background: #17b26a;
}

.metric-orange {
  background: #f79009;
}

.metric-purple {
  background: #7a5af8;
}

.metric-slate {
  background: #667085;
}

.metric-card div {
  display: grid;
  gap: 2px;
}

.metric-card strong {
  color: #101828;
  font-size: 24px;
  line-height: 1.1;
}

.metric-card small,
.metric-label {
  color: #667085;
  font-size: 12px;
}

.filter-panel,
.subject-table-shell {
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.04);
}

.filter-panel {
  padding: 22px 24px 18px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(210px, 1fr));
  gap: 18px 28px;
}

.filter-grid label {
  display: grid;
  grid-template-columns: 82px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  min-width: 0;
}

.filter-grid label > span {
  color: #344054;
  font-size: 13px;
  white-space: nowrap;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.subject-table-shell {
  overflow: hidden;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #eef2f7;
}

.left-actions,
.right-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.table-and-preview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  min-height: 420px;
}

.table-area {
  min-width: 0;
}

.subject-name-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}

.subject-name-cell > div {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.subject-name-cell strong {
  overflow: hidden;
  color: #111827;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.subject-avatar {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.subject-avatar.large {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 16px;
}

.subject-name-cell span,
.option-code {
  color: #6b7280;
  font-size: 12px;
}

.option-code {
  float: right;
  margin-left: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid #eef2f7;
}

.relation-preview {
  min-width: 0;
  padding: 16px;
  border-left: 1px solid #eef2f7;
  background: #ffffff;
}

.preview-header,
.preview-subject {
  display: flex;
  align-items: center;
}

.preview-header {
  justify-content: space-between;
  margin-bottom: 14px;
  color: #101828;
}

.preview-subject {
  gap: 10px;
  padding: 12px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #f8fafc;
}

.preview-subject div {
  display: grid;
  gap: 3px;
}

.preview-subject strong {
  color: #101828;
}

.preview-subject span,
.preview-meta,
.relation-item span,
.empty-preview {
  color: #667085;
  font-size: 12px;
}

.preview-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 12px 0 16px;
}

.preview-meta span {
  padding: 3px 8px;
  border-radius: 999px;
  background: #eef4ff;
  color: #3538cd;
}

.relation-list {
  display: grid;
  gap: 10px;
}

.relation-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f4f7;
}

.relation-item strong,
.relation-item span {
  display: block;
}

.relation-item strong {
  margin-bottom: 3px;
  color: #344054;
  font-size: 13px;
}

.relation-dot {
  width: 8px;
  height: 8px;
  margin-top: 5px;
  border-radius: 50%;
}

.relation-dot.parent {
  background: #2f7df6;
}

.relation-dot.root {
  background: #17b26a;
}

.relation-dot.child {
  background: #f79009;
}

.empty-preview {
  display: grid;
  place-items: center;
  min-height: 120px;
  border: 1px dashed #d0d5dd;
  border-radius: 8px;
  background: #f9fafb;
}

.create-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}

.create-main,
.create-summary,
.wizard-card {
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.04);
}

.create-main {
  overflow: hidden;
}

.single-page-form {
  min-height: 560px;
  margin: 0;
  padding: 32px;
  border-width: 0;
  border-radius: 0;
  box-shadow: none;
}

.form-section {
  margin-bottom: 32px;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.form-section h2 {
  margin: 0 0 24px;
  padding-left: 12px;
  border-left: 4px solid #2f7df6;
  color: #101828;
  font-size: 18px;
  font-weight: 600;
}

.wizard-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin: 40px -32px -32px;
  padding: 20px 32px;
  border-top: 1px solid #eef2f7;
  background: #f8fafc;
}

.wizard-relation-preview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 32px minmax(0, 1fr) 32px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  margin-top: 24px;
  padding: 18px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fbff;
  color: #344054;
  text-align: center;
}

.wizard-relation-preview div {
  padding: 10px;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  background: #eff6ff;
}

.wizard-relation-preview span {
  color: #667085;
  font-size: 20px;
}

.create-summary {
  position: sticky;
  top: 16px;
  padding: 24px;
}

.create-summary h3 {
  margin: 0 0 20px;
  color: #101828;
  font-size: 16px;
}

.create-summary dl {
  display: grid;
  gap: 8px;
  margin: 0;
}

.create-summary dt {
  color: #667085;
  font-size: 12px;
}

.create-summary dd {
  margin: 0 0 10px;
  color: #101828;
  font-size: 14px;
}

.summary-tip {
  margin-top: 20px;
  padding: 14px;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: #eff6ff;
  color: #344054;
  font-size: 13px;
  line-height: 1.7;
}

.switch-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.switch-grid label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
  color: #374151;
  font-size: 14px;
}

.switch-grid label > span {
  min-width: 0;
}

.switch-grid strong,
.switch-grid small {
  display: block;
}

.switch-grid strong {
  color: #1f2937;
  font-weight: 600;
}

.switch-grid small {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 1440px) {
  .metrics-grid {
    grid-template-columns: repeat(3, minmax(160px, 1fr));
  }

  .filter-grid {
    grid-template-columns: repeat(2, minmax(210px, 1fr));
  }
}

@media (max-width: 1100px) {
  .table-and-preview {
    grid-template-columns: 1fr;
  }

  .relation-preview {
    border-top: 1px solid #eef2f7;
    border-left: 0;
  }

  .create-layout {
    grid-template-columns: 1fr;
  }
}

/* 主体详情页面高级视觉样式 */
.subject-detail-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 4px;
}

.detail-header-card {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.header-main-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border-color: #e4e7ec;
  color: #475467;
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: #eff6ff;
  border-color: #2563eb;
  color: #2563eb;
}

.subject-icon-box {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.2);
}

.title-and-status {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-row h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #101828;
}

.status-tag {
  border-radius: 4px;
  font-weight: 600;
}

.right-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.header-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #f2f4f7;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #4b5563;
  font-size: 13px;
}

.meta-item .el-icon {
  color: #6b7280;
  font-size: 14px;
}

.copy-btn {
  margin-left: 4px;
  padding: 0;
  height: auto;
  color: #98a2b3;
  transition: color 0.2s;
}

.copy-btn:hover {
  color: #2563eb;
}

.detail-tabs {
  margin-top: 6px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 600;
  color: #667085;
  height: 44px;
  line-height: 44px;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: #2563eb;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  align-items: stretch;
  margin-top: 8px;
}

.info-card {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 22px 24px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.03);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.info-card .card-header {
  border-bottom: 1px solid #f2f4f7;
  padding-bottom: 14px;
  margin-bottom: 18px;
}

.info-card .card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #101828;
}

.attribute-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.attr-item {
  display: flex;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid #f8fafc;
  line-height: 1.6;
}

.attr-item:last-child {
  border-bottom: none;
}

.attr-item.description-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 12px 0;
}

.attr-label {
  width: 110px;
  flex-shrink: 0;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.attr-value {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
  word-break: break-all;
}

.attr-value.type-link {
  color: #2563eb;
  font-weight: 600;
}

.description-row .attr-value {
  text-align: left;
  justify-content: flex-start;
  width: 100%;
}

.description-text {
  color: #475467;
  word-break: break-all;
}

.tags-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.add-tag-btn {
  font-size: 12px;
  font-weight: 600;
}

.link-value {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.arrow-link {
  font-size: 12px;
  font-weight: 600;
  padding: 0;
}

.status-dot-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.green {
  background: #17b26a;
}

.status-dot.red {
  background: #f04438;
}

.status-dot.grey {
  background: #98a2b3;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.grid-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: none !important;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background: #f1f5f9;
}

.stat-icon-wrapper {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 16px;
  color: #ffffff;
}

.stat-icon-wrapper.blue {
  background: #2f7df6;
  box-shadow: 0 4px 10px rgba(47, 125, 246, 0.2);
}

.stat-icon-wrapper.blue-light {
  background: #60a5fa;
  box-shadow: 0 4px 10px rgba(96, 165, 250, 0.2);
}

.stat-icon-wrapper.orange {
  background: #f79009;
  box-shadow: 0 4px 10px rgba(247, 144, 9, 0.2);
}

.stat-icon-wrapper.orange-light {
  background: #fb923c;
  box-shadow: 0 4px 10px rgba(251, 146, 60, 0.2);
}

.stat-icon-wrapper.green {
  background: #17b26a;
  box-shadow: 0 4px 10px rgba(23, 178, 106, 0.2);
}

.stat-icon-wrapper.green-light {
  background: #34d399;
  box-shadow: 0 4px 10px rgba(52, 211, 153, 0.2);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  color: #64748b;
  font-size: 11px;
}

.stat-number {
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.action-btn {
  width: 100% !important;
  margin: 0 !important;
  height: 38px;
  border-radius: 8px;
  border: 1px solid #d0d5dd !important;
  background: #ffffff !important;
  color: #344054 !important;
  font-weight: 600;
  transition: all 0.2s;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.action-btn:hover {
  background: #eff6ff !important;
  border-color: #2563eb !important;
  color: #2563eb !important;
}

.relation-section-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 12px;
}

.relation-table-card {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.03);
}

.relation-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.relation-card-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #101828;
}

.view-all-link {
  font-size: 12px;
  font-weight: 600;
}

.mini-table {
  border: 1px solid #f2f4f7;
  border-radius: 8px;
  overflow: hidden;
}

.relation-subject-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.relation-subject-cell div {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.relation-subject-cell strong {
  color: #1d2939;
  font-size: 12px;
  font-weight: 600;
}

.relation-subject-cell span {
  color: #667085;
  font-size: 10px;
}

.mini-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50% !important;
  background: #eff6ff !important;
  color: #2563eb !important;
  font-weight: bold;
  font-size: 11px;
  border: none !important;
}

.mini-avatar.green-avatar {
  background: #ecfdf5 !important;
  color: #10b981 !important;
}

.mini-avatar.orange-avatar {
  background: #fff7ed !important;
  color: #f97316 !important;
}

.relation-count-footer {
  text-align: right;
  font-size: 11px;
  color: #98a2b3;
  margin-top: 8px;
}

.operation-log-card {
  margin-top: 20px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logs-table {
  border: 1px solid #f2f4f7;
  border-radius: 8px;
}

.tab-placeholder-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #eef2f7;
  text-align: center;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.03);
}

.tab-placeholder-card .el-icon {
  font-size: 42px;
  color: #cbd5e1;
  margin-bottom: 14px;
}

.tab-placeholder-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #334155;
}

.tab-placeholder-card p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  max-width: 420px;
  line-height: 1.6;
}

@media (max-width: 1400px) {
  .detail-grid {
    grid-template-columns: 1fr 1fr;
  }

  .right-column {
    grid-column: span 2;
  }

  .relation-section-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .right-column {
    grid-column: span 1;
  }
}

</style>
