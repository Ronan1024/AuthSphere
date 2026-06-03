<script setup lang="ts">
import {
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
  type TypeCategoryPayload,
  type TypeCategoryRecord,
  typeCategoryApi,
} from '@/api/typeCategory'
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
  status: undefined as number | undefined,
})

const tableData = ref<TypeCategoryRecord[]>([])
const total = ref(0)

const defaultForm = (): TypeCategoryPayload => ({
  code: '',
  name: '',
  description: '',
  systemBuiltin: false,
  editable: true,
})

const form = reactive<TypeCategoryPayload>(defaultForm())

const rules: FormRules<TypeCategoryPayload> = {
  code: [
    { required: true, message: '请输入类型编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线和下划线',
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  systemBuiltin: [{ required: true, message: '请选择是否系统内置', trigger: 'change' }],
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

const normalizePage = (result: PageResult<TypeCategoryRecord>) => {
  tableData.value = result.records ?? []
  total.value = Number(result.total ?? 0)
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await typeCategoryApi.page({
      page: query.page,
      size: query.size,
      code: query.code || undefined,
      name: query.name || undefined,
      status: query.status,
    })
    normalizePage(result)
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取身份域类型失败'))
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.page = 1
  query.code = ''
  query.name = ''
  query.status = undefined
  fetchData()
}

const assignForm = (value: TypeCategoryPayload) => {
  Object.assign(form, value)
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = undefined
  assignForm(defaultForm())
  dialogVisible.value = true
}

const openEdit = (row: TypeCategoryRecord) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  assignForm({
    code: row.code,
    name: row.name,
    description: row.description || '',
    systemBuiltin: row.systemBuiltin,
    editable: row.editable,
  })
  dialogVisible.value = true
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
    if (dialogMode.value === 'create') {
      await typeCategoryApi.create({ ...form })
    } else if (editingId.value) {
      await typeCategoryApi.update(editingId.value, { ...form })
    }
    showSuccessMessage(dialogMode.value === 'create' ? '身份域类型已创建' : '身份域类型已更新')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存身份域类型失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: TypeCategoryRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响身份域创建和编辑时的类型选择。确认${action}？`,
    `${action}身份域类型`,
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
    await typeCategoryApi.toggleStatus(row.id)
    showSuccessMessage(`身份域类型已${action}`)
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, `${action}身份域类型失败`))
  }
}

fetchData()
</script>

<template>
  <section class="type-category-page">
    <div class="page-heading">
      <div>
        <h1>身份域类型</h1>
        <p>维护身份域的类型分类，用于区分内部员工域、外部伙伴域、客户域等身份空间。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增类型</el-button>
    </div>

    <el-card shadow="never" class="section-card">
      <div class="section-intro">
        <div>
          <strong>类型分类列表</strong>
          <span>类型分类用于身份域创建和编辑时选择，不直接承载账号、主体或权限数据。</span>
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

      <el-table v-loading="loading" :data="tableData" row-key="id" empty-text="暂无身份域类型">
        <el-table-column prop="name" label="类型名称" min-width="160" />
        <el-table-column prop="code" label="类型编码" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="系统内置" width="110">
          <template #default="{ row }">
            <el-tag :type="row.systemBuiltin ? 'warning' : 'info'">
              {{ row.systemBuiltin ? '内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="允许编辑" width="110">
          <template #default="{ row }">
            <el-tag :type="row.editable ? 'success' : 'info'">
              {{ row.editable ? '允许' : '禁止' }}
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
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
      :title="dialogMode === 'create' ? '新增身份域类型' : '编辑身份域类型'"
      width="640px"
      class="type-category-dialog"
      destroy-on-close
    >
      <el-form
        id="type-category-form"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submitForm"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型名称" prop="name">
              <el-input v-model="form.name" placeholder="内部员工域" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型编码" prop="code">
              <el-input v-model="form.code" :disabled="dialogMode === 'edit'" placeholder="internal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>

        <div class="switch-grid">
          <label>
            <span>系统内置</span>
            <el-switch v-model="form.systemBuiltin" />
          </label>
          <label>
            <span>允许编辑</span>
            <el-switch v-model="form.editable" />
          </label>
        </div>
      </el-form>

      <template #footer>
        <el-button native-type="button" @click="dialogVisible = false">取消</el-button>
        <el-button
          form="type-category-form"
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
.type-category-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.page-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p,
.section-intro span {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.section-card {
  border-radius: 8px;
}

.section-intro {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-intro strong {
  display: block;
  color: #111827;
  font-size: 15px;
}

.section-intro span {
  display: block;
}

.table-toolbar {
  margin-bottom: 16px;
}

.filter-row,
.toolbar-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-input {
  width: 220px;
}

.status-filter {
  width: 140px;
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
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
  color: #374151;
  font-size: 14px;
}
</style>
