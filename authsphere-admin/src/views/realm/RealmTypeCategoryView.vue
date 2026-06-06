<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
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
const drawerVisible = ref(false)
const saveLoading = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const originalStatus = ref(STATUS_NORMAL)
const formStatus = ref(STATUS_NORMAL)
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
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === 'string' && message) return message
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
      code: query.code.trim() || undefined,
      name: query.name.trim() || undefined,
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
  Object.assign(query, { page: 1, code: '', name: '', status: undefined })
  fetchData()
}

const openCreate = () => {
  drawerMode.value = 'create'
  editingId.value = undefined
  Object.assign(form, defaultForm())
  originalStatus.value = STATUS_NORMAL
  formStatus.value = STATUS_NORMAL
  drawerVisible.value = true
}

const openEdit = (row: TypeCategoryRecord) => {
  drawerMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    description: row.description || '',
    systemBuiltin: row.systemBuiltin,
    editable: row.editable,
  })
  originalStatus.value = row.status
  formStatus.value = row.status
  drawerVisible.value = true
}

const syncCreatedStatus = async () => {
  if (formStatus.value === STATUS_NORMAL) return
  const list = await typeCategoryApi.list()
  const created = list.find(item => item.code === form.code)
  if (created) await typeCategoryApi.toggleStatus(created.id)
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项和格式校验')
    return
  }

  saveLoading.value = true
  try {
    if (drawerMode.value === 'create') {
      await typeCategoryApi.create({ ...form })
      await syncCreatedStatus()
    } else if (editingId.value) {
      await typeCategoryApi.update(editingId.value, { ...form })
      if (formStatus.value !== originalStatus.value) {
        await typeCategoryApi.toggleStatus(editingId.value)
      }
    }
    showSuccessMessage(drawerMode.value === 'create' ? '身份域类型已创建' : '身份域类型已更新')
    drawerVisible.value = false
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存身份域类型失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: TypeCategoryRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  try {
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
    await typeCategoryApi.toggleStatus(row.id)
    showSuccessMessage(`身份域类型已${action}`)
    fetchData()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showErrorMessage(getErrorMessage(error, `${action}身份域类型失败`))
  }
}

fetchData()
</script>

<template>
  <section class="type-category-page">
    <div class="realm-type-heading">
      <div>
        <h1>身份域类型</h1>
        <p>管理身份域类型的定义，用于分类和标识不同业务场景下的身份域，支持用户自定义新增、编辑和禁用。</p>
      </div>
      <el-button type="primary" :icon="Plus" class="create-button" @click="openCreate">新增身份域类型</el-button>
    </div>

    <el-card shadow="never" class="realm-type-filter-card">
      <div class="realm-type-filter-grid">
        <label class="realm-type-filter-item">
          <span>类型名称</span>
          <el-input v-model="query.name" clearable placeholder="请输入类型名称" @keyup.enter="fetchData" />
        </label>
        <label class="realm-type-filter-item">
          <span>类型编码</span>
          <el-input v-model="query.code" clearable placeholder="请输入类型编码" @keyup.enter="fetchData" />
        </label>
        <label class="realm-type-filter-item">
          <span>状态</span>
          <el-select v-model="query.status" clearable placeholder="全部">
            <el-option label="启用" :value="STATUS_NORMAL" />
            <el-option label="禁用" :value="STATUS_DISABLED" />
          </el-select>
        </label>
        <div class="realm-type-filter-actions">
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="realm-type-table-card">
      <el-table v-loading="loading" :data="tableData" row-key="id" empty-text="暂无身份域类型">
        <el-table-column prop="name" label="类型名称" min-width="140" />
        <el-table-column prop="code" label="类型编码" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
        <el-table-column label="属性" width="130">
          <template #default="{ row }">
            <span class="property-text">{{ row.systemBuiltin ? '系统内置' : '自定义' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === STATUS_NORMAL ? 'success' : 'danger'" effect="light">
              {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="toggleStatus(row)">
              {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="realm-type-table-footer">
        <span>共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next, jumper"
          @change="fetchData"
        />
      </div>
    </el-card>

    <el-drawer
      v-model="drawerVisible"
      :title="drawerMode === 'create' ? '新增身份域类型' : '编辑身份域类型'"
      size="420px"
      class="type-category-drawer"
      destroy-on-close
    >
      <el-form
        id="type-category-form"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="drawer-form"
        @submit.prevent="submitForm"
      >
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型编码" prop="code">
          <el-input v-model="form.code" :disabled="drawerMode === 'edit'" maxlength="50" show-word-limit placeholder="请输入类型编码" />
          <div class="field-hint">英文、数字或下划线，创建后不可修改。</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入描述信息" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formStatus">
            <el-radio :label="STATUS_NORMAL">启用</el-radio>
            <el-radio :label="STATUS_DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="系统属性">
          <div class="switch-row">
            <span>系统内置</span>
            <el-switch v-model="form.systemBuiltin" />
          </div>
          <div class="switch-row">
            <span>允许编辑</span>
            <el-switch v-model="form.editable" />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button form="type-category-form" native-type="submit" type="primary" :loading="saveLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.type-category-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #172033;
}

.realm-type-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.realm-type-heading h1 {
  margin: 0 0 10px;
  color: #101828;
  font-size: 26px;
  line-height: 36px;
}

.realm-type-heading p {
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 22px;
}

.create-button {
  min-width: 160px;
  height: 40px;
  margin-top: 8px;
}

.realm-type-filter-card,
.realm-type-table-card {
  border: 1px solid #e4eaf2;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(31 56 88 / 4%);
}

.realm-type-filter-card :deep(.el-card__body) {
  padding: 26px 24px;
}

.realm-type-filter-grid {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(220px, 1fr) minmax(180px, .8fr) auto;
  gap: 32px;
  align-items: center;
}

.realm-type-filter-item {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  color: #344054;
  font-size: 14px;
  font-weight: 500;
}

.realm-type-filter-item :deep(.el-input__wrapper),
.realm-type-filter-item :deep(.el-select__wrapper) {
  min-height: 36px;
  box-shadow: 0 0 0 1px #d9e1ec inset;
}

.realm-type-filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.realm-type-filter-actions :deep(.el-button) {
  height: 36px;
  margin: 0;
  padding: 0 20px;
}

.realm-type-table-card :deep(.el-card__body) {
  padding: 0;
}

.realm-type-table-card :deep(.el-table__header th) {
  height: 52px;
  color: #344054;
  background: #f8fafc;
  font-weight: 600;
}

.realm-type-table-card :deep(.el-table__row td) {
  height: 58px;
  color: #344054;
}

.realm-type-table-card :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.property-text {
  color: #667085;
}

.realm-type-table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 14px 24px;
  color: #344054;
  border-top: 1px solid #eaecf0;
  font-size: 14px;
}

:deep(.type-category-drawer .el-drawer__header) {
  height: 70px;
  margin: 0;
  padding: 0 28px;
  color: #172033;
  border-bottom: 1px solid #e4eaf2;
  font-size: 18px;
  font-weight: 600;
}

:deep(.type-category-drawer .el-drawer__body) {
  padding: 24px 28px;
}

:deep(.type-category-drawer .el-drawer__footer) {
  padding: 18px 28px;
  border-top: 1px solid #e4eaf2;
}

.drawer-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.drawer-form :deep(.el-form-item__label) {
  color: #344054;
  font-weight: 600;
}

.drawer-form :deep(.el-input__wrapper),
.drawer-form :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #d9e1ec inset;
}

.field-hint {
  margin-top: 6px;
  color: #7b8798;
  font-size: 12px;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 0;
  color: #475467;
}

.drawer-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.drawer-footer :deep(.el-button) {
  min-width: 80px;
  height: 38px;
  margin: 0;
}

@media (max-width: 1100px) {
  .realm-type-filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
