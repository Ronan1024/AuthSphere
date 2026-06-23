<script setup lang="ts">
import {
  Delete,
  Edit,
  Plus,
  Refresh,
  Search,
  SwitchButton,
} from '@element-plus/icons-vue'
import {
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from 'element-plus'
import { reactive, ref } from 'vue'

const categoryOptions = ref<string[]>([])

import {
  type PageResult,
  type SubjectTypePayload,
  type SubjectTypeRecord,
  subjectTypeApi,
} from '@/api/subjectType'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const loading = ref(false)
const dialogVisible = ref(false)
const saveLoading = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const formRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  size: 10,
  code: '',
  name: '',
  category: '',
  status: undefined as number | undefined,
})

const tableData = ref<SubjectTypeRecord[]>([])
const total = ref(0)

const defaultForm = (): SubjectTypePayload => ({
  code: '',
  name: '',
  category: '',
  canHaveMembers: true,
  canOpenApp: true,
  canAssignRole: true,
  canBeRoot: false,
  builtIn: false,
  status: STATUS_NORMAL,
  description: '',
})

const form = reactive<SubjectTypePayload>(defaultForm())

const rules: FormRules<SubjectTypePayload> = {
  code: [
    { required: true, message: '请输入主体类型编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线和下划线',
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入主体类型名称', trigger: 'blur' }],
  canHaveMembers: [{ required: true, message: '请选择是否允许拥有成员', trigger: 'change' }],
  canOpenApp: [{ required: true, message: '请选择是否允许开通应用', trigger: 'change' }],
  canAssignRole: [{ required: true, message: '请选择是否允许分配角色', trigger: 'change' }],
  canBeRoot: [{ required: true, message: '请选择是否可作为数据隔离根', trigger: 'change' }],
  builtIn: [{ required: true, message: '请选择是否系统内置', trigger: 'change' }],
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

const normalizePage = (result: PageResult<SubjectTypeRecord>) => {
  tableData.value = result.records ?? []
  total.value = Number(result.total ?? 0)
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await subjectTypeApi.page({
      page: query.page,
      size: query.size,
      code: query.code || undefined,
      name: query.name || undefined,
      category: query.category || undefined,
      status: query.status,
    })
    normalizePage(result)
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体类型失败'))
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.page = 1
  query.code = ''
  query.name = ''
  query.category = ''
  query.status = undefined
  fetchData()
}

const assignForm = (value: SubjectTypePayload) => {
  Object.assign(form, value)
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = undefined
  assignForm(defaultForm())
  dialogVisible.value = true
}

const handleCategoryBlur = (e: FocusEvent) => {
  const target = e.target as HTMLInputElement
  if (target && target.value) {
    const val = target.value.trim()
    if (val) {
      form.category = val
      if (!categoryOptions.value.includes(val)) {
        categoryOptions.value.push(val)
      }
    }
  }
}

const getCategoryClass = (category?: string) => {
  if (!category) return 'tag-default'
  if (category.includes('组织') || category.includes('tenant') || category.includes('organization')) {
    return 'tag-tenant'
  }
  if (category.includes('个人') || category.includes('employee') || category.includes('account') || category.includes('user')) {
    return 'tag-personal'
  }
  if (category.includes('服务') || category.includes('service')) {
    return 'tag-service'
  }
  return 'tag-default'
}

const openEdit = async (row: SubjectTypeRecord) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  loading.value = true
  try {
    const detail = await subjectTypeApi.detail(row.id)
    assignForm({
      code: detail.code,
      name: detail.name,
      category: detail.category || '',
      canHaveMembers: detail.canHaveMembers,
      canOpenApp: detail.canOpenApp,
      canAssignRole: detail.canAssignRole,
      canBeRoot: detail.canBeRoot,
      builtIn: detail.builtIn,
      status: detail.status,
      description: detail.description || '',
    })

    if (detail.category && !categoryOptions.value.includes(detail.category)) {
      categoryOptions.value.push(detail.category)
    }

    dialogVisible.value = true
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体类型详情失败'))
  } finally {
    loading.value = false
  }
}

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
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await subjectTypeApi.create(payload)
    } else if (editingId.value) {
      await subjectTypeApi.update(editingId.value, payload)
    }
    showSuccessMessage(dialogMode.value === 'create' ? '主体类型已创建' : '主体类型已更新')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存主体类型失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: SubjectTypeRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响主体创建和主体授权时的类型选择。确认${action}？`,
    `${action}主体类型`,
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
    await subjectTypeApi.toggleStatus(row.id)
    showSuccessMessage(`主体类型已${action}`)
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, `${action}主体类型失败`))
  }
}

const removeSubjectType = async (row: SubjectTypeRecord) => {
  if (row.builtIn) {
    showErrorMessage('系统内置主体类型不能删除')
    return
  }

  await ElMessageBox.confirm(
    `删除后主体类型「${row.name}」将不可恢复。确认删除？`,
    '删除主体类型',
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
    await subjectTypeApi.remove(row.id)
    showSuccessMessage('主体类型已删除')
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '删除主体类型失败'))
  }
}

const fetchCategories = async () => {
  try {
    const res = await subjectTypeApi.categoryList() as any
    const rawList = Array.isArray(res) ? res : (res?.data && Array.isArray(res.data) ? res.data : [])
    const cleanList = rawList.filter(
      (item: any): item is string => typeof item === 'string' && item.trim() !== ''
    )
    categoryOptions.value = Array.from(new Set(cleanList))
  } catch (error) {
    console.error('获取分类列表失败', error)
    categoryOptions.value = []
  }
}

fetchData()
fetchCategories()
</script>

<template>
  <section class="subject-type-page">
    <div class="page-heading">
      <div class="heading-text">
        <h1>主体类型</h1>
        <p>维护员工、客户、伙伴、服务账号等主体类型，定义主体是否能拥有成员、开通应用、分配角色和作为数据隔离根。</p>
      </div>
      <div class="heading-actions">
        <el-button type="primary" :icon="Plus" class="primary-action" @click="openCreate">新增主体类型</el-button>
        <el-button :icon="Refresh" class="refresh-action" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- Filters Card -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-grid">
        <div class="filter-row" style="grid-template-columns: repeat(4, 1fr) auto;">
          <div class="filter-col">
            <span class="filter-label">类型名称</span>
            <el-input
              v-model="query.name"
              placeholder="请输入名称"
              clearable
              class="input-styled"
              @keyup.enter="fetchData"
            />
          </div>
          <div class="filter-col">
            <span class="filter-label">类型编码</span>
            <el-input
              v-model="query.code"
              placeholder="请输入编码"
              clearable
              class="input-styled"
              @keyup.enter="fetchData"
            />
          </div>
          <div class="filter-col">
            <span class="filter-label">主体分类</span>
            <el-input
              v-model="query.category"
              placeholder="请输入分类"
              clearable
              class="input-styled"
              @keyup.enter="fetchData"
            />
          </div>
          <div class="filter-col">
            <span class="filter-label">状态</span>
            <el-select v-model="query.status" placeholder="全部状态" clearable class="select-styled">
              <el-option label="启用" :value="STATUS_NORMAL" />
              <el-option label="禁用" :value="STATUS_DISABLED" />
            </el-select>
          </div>
          <div class="filter-actions">
            <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Table Card -->
    <el-card shadow="never" class="table-card">
      <div class="table-header-row">
        <div>
          <span class="table-title">主体类型列表</span>
          <span class="table-desc">主体类型用于约束主体能力边界，不直接保存账号登录凭证。</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" row-key="id" class="custom-table" border empty-text="暂无主体类型">
        <el-table-column prop="name" label="类型名称" min-width="120" />
        <el-table-column prop="code" label="编码" min-width="120">
          <template #default="{ row }">
            <code class="code-text">{{ row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" min-width="110">
          <template #default="{ row }">
            <span v-if="row.category" class="badge" :class="getCategoryClass(row.category)">
              {{ row.category }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="成员" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.canHaveMembers ? 'text-success' : 'text-danger'">
              {{ row.canHaveMembers ? '允许' : '禁止' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="下级主体" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.canAssignRole ? 'text-success' : 'text-danger'">
              {{ row.canAssignRole ? '允许' : '禁止' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="签约应用" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.canOpenApp ? 'text-success' : 'text-danger'">
              {{ row.canOpenApp ? '允许' : '禁止' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="数据边界" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.canBeRoot ? 'success' : 'info'" size="default">
              {{ row.canBeRoot ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="系统内置" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.builtIn"
              disabled
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === STATUS_NORMAL"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              type="danger"
              :icon="Delete"
              :disabled="row.builtIn"
              @click="removeSubjectType(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination-footer">
        <span class="total-text">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          layout="prev, pager, next"
          :total="total"
          @change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增主体类型' : '编辑主体类型'"
      width="760px"
      class="subject-type-dialog"
      destroy-on-close
    >
      <el-form
        id="subject-type-form"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submitForm"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型名称" prop="name">
              <el-input v-model="form.name" placeholder="员工主体" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型编码" prop="code">
              <el-input v-model="form.code" :disabled="dialogMode === 'edit'" placeholder="employee" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="主体分类" prop="category" class="category-form-item">
              <el-select
                v-model="form.category"
                class="category-select"
                filterable
                allow-create
                default-first-option
                popper-class="subject-type-select-popper"
                placeholder="请选择或输入主体分类"
                style="width: 100%;"
                @blur="handleCategoryBlur"
              >
                <el-option
                  v-for="item in categoryOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                >
                  <div class="category-option">
                    <span class="badge" :class="getCategoryClass(item)">{{ item }}</span>
                    <span class="category-option-hint">主体分类</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="dialogMode === 'edit'" :span="6">
            <el-form-item label="系统内置" prop="builtIn">
              <el-switch v-model="form.builtIn" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="dialogMode === 'edit' ? 6 : 12">
            <el-form-item label="启用状态" prop="status">
              <el-switch
                v-model="form.status"
                :active-value="STATUS_NORMAL"
                :inactive-value="STATUS_DISABLED"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>

        <div class="switch-grid">
          <label>
            <span>
              <strong>允许拥有成员</strong>
              <small>开启后该主体类型可以维护下级成员或子主体。</small>
            </span>
            <el-switch v-model="form.canHaveMembers" />
          </label>
          <label>
            <span>
              <strong>允许开通应用</strong>
              <small>开启后该主体类型可以作为应用访问和授权入口。</small>
            </span>
            <el-switch v-model="form.canOpenApp" />
          </label>
          <label>
            <span>
              <strong>允许分配角色</strong>
              <small>开启后该主体类型可以绑定角色并参与权限计算。</small>
            </span>
            <el-switch v-model="form.canAssignRole" />
          </label>
          <label>
            <span>
              <strong>数据隔离根主体</strong>
              <small>开启后该主体类型可作为数据范围和隔离边界根节点。</small>
            </span>
            <el-switch v-model="form.canBeRoot" />
          </label>
        </div>
      </el-form>

      <template #footer>
        <el-button native-type="button" @click="dialogVisible = false">取消</el-button>
        <el-button
          form="subject-type-form"
          native-type="submit"
          type="primary"
          :loading="saveLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

.subject-type-page {
  --primary-color: #2563eb;      /* Primary Blue */
  --primary-hover: #1d4ed8;
  --secondary-color: #3b82f6;    /* Sky/Accent Blue */
  --success-color: #16a34a;      /* Success Green */
  --bg-color: #f8fafc;           /* Light slate bg */
  --text-main: #0f172a;          /* Charcoal Slate Text */
  --text-muted: #64748b;
  --border-light: #eaecf0;
  --font-family-display: 'Lexend', system-ui, -apple-system, sans-serif;
  --font-family-body: 'Source Sans 3', system-ui, -apple-system, sans-serif;

  font-family: var(--font-family-body);
}

.page-heading,
.table-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
}

.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.page-heading h1 {
  margin: 0;
  color: var(--text-main);
  font-size: 22px;
  font-weight: 700;
  font-family: var(--font-family-display);
}

.page-heading p,
.section-intro span {
  margin: 8px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.section-card {
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.section-intro {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-intro strong {
  display: block;
  color: var(--text-main);
  font-size: 15px;
  font-weight: 700;
  font-family: var(--font-family-display);
}

.section-intro span {
  display: block;
}

.table-toolbar {
  margin-bottom: 16px;
}

.filter-row,
.toolbar-actions,
.capability-tags {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-row {
  flex-wrap: wrap;
}

.filter-input {
  width: 200px;
}

.status-filter {
  width: 140px;
}

.capability-tags {
  flex-wrap: wrap;
  gap: 6px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
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
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: var(--bg-color);
  color: var(--text-main);
  font-size: 14px;
  transition: all 0.2s ease;
}

.switch-grid label:hover {
  border-color: var(--secondary-color);
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.03);
}

.switch-grid label > span {
  min-width: 0;
}

.switch-grid strong,
.switch-grid small {
  display: block;
}

.switch-grid strong {
  color: var(--text-main);
  font-weight: 600;
  font-family: var(--font-family-display);
}

.switch-grid small {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.5;
}

/* Align styles with RealmListView */
.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.heading-text h1 {
  margin: 0 0 8px;
  color: #101828;
  font-size: 26px;
  font-weight: 700;
  line-height: 36px;
}

.heading-text p {
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 22px;
}

.heading-actions {
  display: flex;
  gap: 12px;
  padding-top: 6px;
}

.heading-actions :deep(.el-button) {
  height: 38px;
  border-radius: 6px;
  font-weight: 600;
}

.heading-actions .primary-action {
  color: #fff;
  background: #2563eb;
  border-color: #2563eb;
  padding: 0 20px;
}

.heading-actions .primary-action:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.filter-card {
  margin-bottom: 24px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
}

.filter-card :deep(.el-card__body) {
  padding: 24px;
}

.filter-grid {
  display: flex;
  flex-direction: column;
}

.filter-row {
  display: grid;
  gap: 24px;
  align-items: flex-end;
}

.filter-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  color: #344054;
  font-size: 13px;
  font-weight: 600;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.filter-actions :deep(.el-button) {
  height: 36px;
  border-radius: 6px;
  padding: 0 18px;
  font-weight: 600;
}

.filter-actions :deep(.el-button--primary) {
  background: #2563eb;
  border-color: #2563eb;
}

.filter-actions :deep(.el-button--primary:hover) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.table-card {
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
}

.table-card :deep(.el-card__body) {
  padding: 24px;
}

.table-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.table-title {
  display: block;
  font-size: 16px;
  color: #0f172a;
  font-weight: 700;
}

.table-desc {
  display: block;
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.custom-table :deep(th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569;
}

.code-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #475569;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.table-pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  color: #64748b;
  font-size: 13px;
}

/* Badges for custom type categories matching Realm style */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.tag-tenant {
  background-color: #eff6ff;
  color: #2563eb;
}

.tag-personal {
  background-color: #f3e8ff;
  color: #a855f7;
}

.tag-service {
  background-color: #ffedd5;
  color: #ea580c;
}

.tag-default {
  background-color: #f1f5f9;
  color: #64748b;
}

.category-form-item :deep(.el-form-item__content) {
  display: block;
}

.category-select :deep(.el-select__selected-item),
.category-select :deep(.el-select__placeholder) {
  font-size: 14px;
  font-weight: 600;
}

.category-select :deep(.el-select__selected-item) {
  color: #0f172a;
}

.category-select :deep(.el-select__placeholder) {
  color: #94a3b8;
}

.category-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.category-option-hint {
  flex-shrink: 0;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
}

</style>
