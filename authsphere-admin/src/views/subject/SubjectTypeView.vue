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
      description: detail.description || '',
    })
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

fetchData()
</script>

<template>
  <section class="subject-type-page">
    <div class="page-heading">
      <div>
        <h1>主体类型</h1>
        <p>维护员工、客户、伙伴、服务账号等主体类型，定义主体是否能拥有成员、开通应用、分配角色和作为数据隔离根。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增主体类型</el-button>
    </div>

    <el-card shadow="never" class="section-card">
      <div class="section-intro">
        <div>
          <strong>主体类型列表</strong>
          <span>主体类型用于约束主体能力边界，不直接保存账号登录凭证。</span>
        </div>
      </div>

      <div class="table-toolbar">
        <div class="filter-row">
          <el-input
            v-model="query.name"
            class="filter-input"
            clearable
            placeholder="类型名称"
            :prefix-icon="Search"
            @keyup.enter="fetchData"
          />
          <el-input
            v-model="query.code"
            class="filter-input"
            clearable
            placeholder="类型编码"
            @keyup.enter="fetchData"
          />
          <el-input
            v-model="query.category"
            class="filter-input"
            clearable
            placeholder="主体分类"
            @keyup.enter="fetchData"
          />
          <el-select v-model="query.status" clearable placeholder="全部状态" class="status-filter">
            <el-option label="启用" :value="STATUS_NORMAL" />
            <el-option label="禁用" :value="STATUS_DISABLED" />
          </el-select>
        </div>
        <div class="toolbar-actions">
          <el-button @click="resetQuery">重置</el-button>
          <el-button :icon="Refresh" @click="fetchData">查询</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" row-key="id" empty-text="暂无主体类型">
        <el-table-column prop="name" label="类型名称" min-width="150" />
        <el-table-column prop="code" label="类型编码" min-width="140" />
        <el-table-column prop="category" label="主体分类" min-width="130" show-overflow-tooltip />
        <el-table-column label="能力" min-width="280">
          <template #default="{ row }">
            <div class="capability-tags">
              <el-tag v-if="row.canHaveMembers" type="success">成员</el-tag>
              <el-tag v-if="row.canOpenApp" type="success">应用</el-tag>
              <el-tag v-if="row.canAssignRole" type="success">角色</el-tag>
              <el-tag v-if="row.canBeRoot" type="warning">隔离根</el-tag>
              <el-tag
                v-if="!row.canHaveMembers && !row.canOpenApp && !row.canAssignRole && !row.canBeRoot"
                type="info"
              >
                未配置
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="系统内置" width="110">
          <template #default="{ row }">
            <el-tag :type="row.builtIn ? 'warning' : 'info'">
              {{ row.builtIn ? '内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === STATUS_NORMAL ? 'success' : 'info'">
              {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
            </el-button>
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
            <el-form-item label="主体分类" prop="category">
              <el-input v-model="form.category" placeholder="account / organization / service" />
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
          <label>
            <span>
              <strong>系统内置</strong>
              <small>系统预置类型建议开启，内置类型不允许删除。</small>
            </span>
            <el-switch v-model="form.builtIn" />
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

.page-heading,
.table-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
}

.page-heading {
  padding: 24px;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 4px 30px rgba(3, 105, 161, 0.03);
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
</style>
