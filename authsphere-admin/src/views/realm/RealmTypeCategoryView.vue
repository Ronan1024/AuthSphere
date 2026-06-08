<script setup lang="ts">
import { Plus, Search, Refresh, ArrowLeft, Connection, Warning } from '@element-plus/icons-vue'
import { type FormInstance, type FormRules, ElMessageBox, ElMessage } from 'element-plus'
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

import {
  type PageResult,
  type TypeCategoryPayload,
  type TypeCategoryRecord,
  typeCategoryApi,
} from '@/api/typeCategory'
import { realmApi, type RealmRecord } from '@/api/realm'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const router = useRouter()
const loading = ref(false)
const drawerVisible = ref(false)
const saveLoading = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const originalStatus = ref(STATUS_NORMAL)
const formStatus = ref(STATUS_NORMAL)
const formRef = ref<FormInstance>()

// View state: 'list' or 'detail'
const currentView = ref<'list' | 'detail'>('list')
const selectedCategory = ref<TypeCategoryRecord | null>(null)

// References mapping: Key is typeCategoryId, value is array of RealmRecords
const realmRefsMap = ref<Record<string, RealmRecord[]>>({})
const realmsLoading = ref(false)

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
}

const getErrorMessage = (error: unknown, fallback = '操作失败') => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const activeRefsCount = ref(0)
const disabledRefsCount = ref(0)
const refRealmsList = ref<RealmRecord[]>([])

const fetchReferencedRealms = async (categoryId: string) => {
  realmsLoading.value = true
  try {
    const result = await realmApi.page({ page: 1, size: 500, typeCategoryId: categoryId })
    refRealmsList.value = result.records || []
    activeRefsCount.value = refRealmsList.value.filter(r => r.status === STATUS_NORMAL).length
    disabledRefsCount.value = refRealmsList.value.filter(r => r.status === STATUS_DISABLED).length
  } catch (error) {
    console.error('获取关联身份域失败', error)
  } finally {
    realmsLoading.value = false
  }
}

const getRefsCount = (row: TypeCategoryRecord) => {
  return row.referenceCount ?? 0
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
    // If we're editing details view category, update the detail state
    if (currentView.value === 'detail' && selectedCategory.value?.id === editingId.value) {
      const updatedList = await typeCategoryApi.list()
      const updated = updatedList.find(c => c.id === editingId.value)
      if (updated) selectedCategory.value = updated
    }
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
      `${action}后将影响身份域创建/编辑时的类型选择。确认${action}该类型？`,
      `${action}身份域类型`,
      {
        type: row.status === STATUS_NORMAL ? 'warning' : 'info',
        confirmButtonText: action,
        cancelButtonText: '取消',
      },
    )
    await typeCategoryApi.toggleStatus(row.id)
    showSuccessMessage(`身份域类型已${action}`)
    fetchData()
    if (currentView.value === 'detail' && selectedCategory.value?.id === row.id) {
      selectedCategory.value.status = row.status === STATUS_NORMAL ? STATUS_DISABLED : STATUS_NORMAL
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showErrorMessage(getErrorMessage(error, `${action}身份域类型失败`))
  }
}

// Custom Delete Dialog state
const deleteModalVisible = ref(false)
const deletingCategory = ref<TypeCategoryRecord | null>(null)
const deleteLoading = ref(false)

const openDeleteConfirm = (row: TypeCategoryRecord) => {
  deletingCategory.value = row
  deleteModalVisible.value = true
}

const confirmDelete = async () => {
  if (!deletingCategory.value) return
  deleteLoading.value = true
  try {
    await typeCategoryApi.delete(deletingCategory.value.id)
    showSuccessMessage('身份域类型已成功删除')
    deleteModalVisible.value = false
    deletingCategory.value = null
    fetchData()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '删除身份域类型失败'))
  } finally {
    deleteLoading.value = false
  }
}

const showDetail = (row: TypeCategoryRecord) => {
  selectedCategory.value = row
  currentView.value = 'detail'
  fetchReferencedRealms(row.id)
}

const closeDetail = () => {
  selectedCategory.value = null
  currentView.value = 'list'
}

const navigateToRealms = () => {
  router.push('/realms')
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <section class="realm-type-page">
    <!-- Breadcrumb & Nav Tabs -->
    <div class="page-heading">
      <div class="heading-text">
        <h1 v-if="currentView === 'list'">身份域类型</h1>
        <h1 v-else-if="currentView === 'detail'">{{ selectedCategory?.name }} <span class="header-code-pill">{{ selectedCategory?.code }}</span></h1>
        <p v-if="currentView === 'list'">维护身份域的业务分类，新增身份域时从这里选择类型。</p>
        <p v-else-if="currentView === 'detail'">类型编码 {{ selectedCategory?.code }}，被 {{ selectedCategory ? getRefsCount(selectedCategory) : 0 }} 个身份域引用。</p>
      </div>
      <div class="heading-actions">
        <template v-if="currentView === 'list'">
          <el-button type="primary" :icon="Plus" class="primary-action" @click="openCreate">新增类型</el-button>
          <el-button :icon="Refresh" class="refresh-action" @click="fetchData">刷新</el-button>
        </template>
        <template v-else>
          <el-button :icon="ArrowLeft" class="refresh-action" @click="closeDetail">返回列表</el-button>
          <el-tooltip
            :disabled="!selectedCategory?.systemBuiltin"
            content="系统内置，无法进行编辑"
            placement="top"
          >
            <span style="display: inline-block;">
              <el-button type="primary" :disabled="selectedCategory?.editable === false || selectedCategory?.systemBuiltin" @click="selectedCategory && openEdit(selectedCategory)">编辑</el-button>
            </span>
          </el-tooltip>
          <el-button
            v-if="selectedCategory"
            :type="selectedCategory.status === STATUS_NORMAL ? 'danger' : 'success'"
            plain
            @click="toggleStatus(selectedCategory)"
          >
            {{ selectedCategory.status === STATUS_NORMAL ? '禁用' : '启用' }}
          </el-button>
        </template>
      </div>
    </div>


    <!-- Main Views Container -->
    <div class="page-body-container">
      <!-- LIST VIEW -->
      <div v-if="currentView === 'list'" class="list-layout">
        <!-- Search Filter Card -->
        <el-card shadow="never" class="filter-card">
          <div class="filter-grid">
            <div class="filter-row">
              <div class="filter-col">
                <span class="filter-label">类型名称</span>
                <el-input v-model="query.name" placeholder="请输入类型名称" clearable class="input-styled" @keyup.enter="fetchData" />
              </div>
              <div class="filter-col">
                <span class="filter-label">类型编码</span>
                <el-input v-model="query.code" placeholder="请输入类型编码" clearable class="input-styled" @keyup.enter="fetchData" />
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

        <!-- Table List Card -->
        <el-card shadow="never" class="table-card">
          <div class="table-header-row">
            <div>
              <span class="table-title">类型列表</span>
              <span class="table-desc">包含类型名称、编码、描述、排序、状态和关联引用数。</span>
            </div>

          </div>

          <el-table v-loading="loading" :data="tableData" row-key="id" class="custom-table" border>
            <el-table-column label="类型名称" min-width="150">
              <template #default="{ row }">
                <span class="strong-text">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="类型编码" min-width="150">
              <template #default="{ row }">
                <code class="code-text">{{ row.code }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
            <el-table-column label="排序" width="90">
              <template #default="{ $index }">
                <span>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="引用身份域" width="130">
              <template #default="{ row }">
                <span class="ref-count-pill" @click="showDetail(row)">
                  {{ getRefsCount(row.id) }} 个
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <span class="status-badge" :class="row.status === STATUS_NORMAL ? 'enabled' : 'disabled'">
                  <span class="dot"></span>
                  {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button link type="primary" @click="showDetail(row)">详情</el-button>
                  <el-tooltip
                    :disabled="!row.systemBuiltin"
                    content="系统内置，无法进行编辑"
                    placement="top"
                  >
                    <span style="display: inline-block;">
                      <el-button link type="primary" :disabled="row.editable === false || row.systemBuiltin" @click="openEdit(row)">编辑</el-button>
                    </span>
                  </el-tooltip>
                  <el-button link type="primary" @click="toggleStatus(row)">
                    {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
                  </el-button>
                  <!-- Delete button shown only if status is disabled AND reference count is 0 -->
                  <el-button
                    v-if="row.status === STATUS_DISABLED && getRefsCount(row) === 0"
                    link
                    type="danger"
                    @click="openDeleteConfirm(row)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- Table Footer Pagination -->
          <div class="table-pagination-footer">
            <span class="total-text">共 {{ total }} 条</span>
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
      </div>

      <!-- DETAILS VIEW -->
      <div v-else-if="currentView === 'detail' && selectedCategory" class="detail-layout">
        <div class="detail-grid">
          <!-- Left Column: Basic Information Card -->
          <el-card shadow="never" class="detail-info-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">基础信息</span>
                <span class="card-desc">详细展现类型属性配置及其生命周期。</span>
              </div>
            </template>

            <div class="property-list">
              <div class="property-row">
                <span class="property-label">类型名称</span>
                <span class="property-value strong-text">{{ selectedCategory.name }}</span>
              </div>
              <div class="property-row">
                <span class="property-label">类型编码</span>
                <span class="property-value"><code class="code-text">{{ selectedCategory.code }}</code></span>
              </div>
              <div class="property-row">
                <span class="property-label">排序值</span>
                <span class="property-value">2</span>
              </div>
              <div class="property-row">
                <span class="property-label">状态</span>
                <span class="property-value">
                  <span class="status-badge" :class="selectedCategory.status === STATUS_NORMAL ? 'enabled' : 'disabled'">
                    <span class="dot"></span>
                    {{ selectedCategory.status === STATUS_NORMAL ? '启用' : '禁用' }}
                  </span>
                </span>
              </div>
              <div class="property-row">
                <span class="property-label">是否系统内置</span>
                <span class="property-value">{{ selectedCategory.systemBuiltin ? '是' : '否' }}</span>
              </div>
              <div class="property-row">
                <span class="property-label">创建时间</span>
                <span class="property-value">{{ selectedCategory.createTime || '-' }}</span>
              </div>
              <div class="property-row full-width">
                <span class="property-label">描述</span>
                <span class="property-value desc-box">{{ selectedCategory.description || '暂无描述信息。' }}</span>
              </div>
            </div>

            <!-- Operations Muted Tip -->
            <div class="warning-tip-box">
              <div class="tip-title">操作说明</div>
              <div class="tip-content">
                禁用后：新增身份域时不可再选择该类型；已有关联身份域不受影响，但会显示类型已禁用提示。
                若要删除，请先确保该类型状态为“禁用”，并且没有被任何身份域关联引用。
              </div>
            </div>
          </el-card>

          <!-- Right Column: Usage Statistics & Referenced Realms List -->
          <el-card shadow="never" class="detail-usage-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">引用分析</span>
                <span class="card-desc">展示当前类型在身份域中的引用分布。</span>
              </div>
            </template>

            <div class="stats-mini-cards">
              <div class="stat-card">
                <span class="stat-number">{{ activeRefsCount + disabledRefsCount }}</span>
                <span class="stat-label">总引用数量</span>
              </div>
              <div class="stat-card">
                <span class="stat-number">{{ disabledRefsCount }}</span>
                <span class="stat-label">已禁用引用</span>
              </div>
            </div>

            <div class="ref-realms-section">
              <div class="section-title">关联身份域列表</div>
              <div v-if="refRealmsList.length === 0" class="empty-refs-placeholder">
                暂无关联身份域
              </div>
              <div v-else class="refs-list">
                <div v-for="realm in refRealmsList" :key="realm.id" class="ref-item">
                  <div class="ref-info">
                    <span class="ref-realm-name">{{ realm.name }}</span>
                    <code class="ref-realm-code">{{ realm.code }}</code>
                  </div>
                  <span class="status-badge" :class="realm.status === STATUS_NORMAL ? 'enabled' : 'disabled'">
                    <span class="dot"></span>
                    {{ realm.status === STATUS_NORMAL ? '启用' : '禁用' }}
                  </span>
                </div>
              </div>
            </div>

            <el-button type="primary" plain class="action-btn-block" @click="navigateToRealms">
              查看并配置身份域
            </el-button>
          </el-card>
        </div>
      </div>
    </div>

    <!-- Create & Edit Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerMode === 'create' ? '新增身份域类型' : '编辑身份域类型'"
      size="480px"
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
          <div class="field-hint">全局唯一，且创建后不可修改。只能包含小写字母、数字和下划线/中横线。</div>
        </el-form-item>
        <el-form-item label="排序" prop="sortNo">
          <el-input-number :model-value="drawerMode === 'create' ? 5 : 2" :min="1" controls-position="right" style="width: 100%; text-align: left;" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formStatus">
            <el-radio :label="STATUS_NORMAL">启用</el-radio>
            <el-radio :label="STATUS_DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入类型描述，比如使用该空间的人群分类..." />
        </el-form-item>

        <div class="drawer-tips">
          <span class="tips-title">校验规则提示</span>
          <p>已被身份域关联引用的类型无法直接删除。编辑时，类型编码将锁定为只读，确保引用的连贯一致。</p>
        </div>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button form="type-category-form" native-type="submit" type="primary" :loading="saveLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- Custom Delete Confirmation Dialog -->
    <el-dialog
      v-model="deleteModalVisible"
      title="确认删除身份域类型"
      width="480px"
      align-center
      class="custom-delete-dialog"
    >
      <div class="delete-confirm-content">
        <div class="warn-icon-wrapper">
          <el-icon class="warn-icon"><Warning /></el-icon>
        </div>
        <div class="confirm-message-container">
          <div class="confirm-title">确认删除身份域类型？</div>
          <div class="confirm-desc">
            将删除类型「{{ deletingCategory?.name }} - <code class="code-highlight">{{ deletingCategory?.code }}</code>」。删除后数据将不可恢复。
          </div>
        </div>

        <div class="confirm-check-alert">
          <div class="alert-title">安全校验通过</div>
          <p>当前引用数量为 0，且该类型已处于“禁用”状态，符合系统物理删除规则。</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteModalVisible = false">取消</el-button>
          <el-button type="danger" :loading="deleteLoading" @click="confirmDelete">确认删除</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.realm-type-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding-bottom: 24px;
}

/* Page Header and Layout */
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
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-code-pill {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 14px;
  background: #f1f5f9;
  color: #64748b;
  padding: 2px 10px;
  border-radius: 4px;
  font-weight: normal;
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

/* Page Navigation Tabs */
.page-navigation-tabs {
  display: flex;
  align-items: center;
  height: 52px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 24px;
  padding: 0 16px;
  gap: 8px;
}

.nav-tab {
  height: 34px;
  padding: 0 18px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #64748b;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-tab:hover:not(.disabled) {
  color: #2563eb;
  background: #f8fafc;
}

.nav-tab.active {
  background: #eff6ff;
  color: #2563eb;
  box-shadow: inset 0 0 0 1px #bfdbfe;
}

.nav-tab.disabled {
  color: #94a3b8;
  cursor: not-allowed;
  opacity: 0.6;
}

/* Page Body Container */
.page-body-container {
  display: flex;
  flex-direction: column;
}

/* Filter Card */
.filter-card {
  margin-bottom: 24px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
}

.filter-card :deep(.el-card__body) {
  padding: 24px;
}

.filter-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr auto;
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

/* Table Card */
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

.table-note {
  font-size: 13px;
  color: #ef4444;
  background: #fef2f2;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: 500;
}

/* Table Styles */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.strong-text {
  font-weight: 700;
  color: #0f172a;
}

.code-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: #475569;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.ref-count-pill {
  color: #2563eb;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.ref-count-pill:hover {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
}

.status-badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-badge.enabled {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.enabled .dot {
  background: #16a34a;
}

.status-badge.disabled {
  background: #fee2e2;
  color: #dc2626;
}

.status-badge.disabled .dot {
  background: #dc2626;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons :deep(.el-button) {
  padding: 0;
  margin: 0;
  font-weight: 600;
}

.table-pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  color: #64748b;
  font-size: 13px;
}

/* Detail Layout */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

.detail-info-card,
.detail-usage-card {
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-header .card-title {
  font-size: 16px;
  color: #0f172a;
  font-weight: 700;
}

.card-header .card-desc {
  font-size: 13px;
  color: #64748b;
}

/* Property List Table */
.property-list {
  display: grid;
  grid-template-columns: 140px 1fr;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
}

.property-row {
  display: contents;
}

.property-label {
  background: #f8fafc;
  color: #64748b;
  font-weight: 700;
  border-bottom: 1px solid #e5e7eb;
  padding: 14px 16px;
  font-size: 13px;
}

.property-value {
  border-bottom: 1px solid #e5e7eb;
  padding: 14px 16px;
  font-size: 13px;
  color: #334155;
  background: #ffffff;
}

.property-list .property-row:last-child .property-label,
.property-list .property-row:last-child .property-value {
  border-bottom: none;
}

.desc-box {
  line-height: 20px;
}

.warning-tip-box {
  padding: 16px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  font-size: 13px;
  color: #64748b;
  line-height: 22px;
}

.warning-tip-box .tip-title {
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}

/* Usage Statistics Card */
.stats-mini-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  line-height: 36px;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.ref-realms-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.ref-realms-section .section-title {
  background: #f8fafc;
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  border-bottom: 1px solid #e5e7eb;
}

.empty-refs-placeholder {
  padding: 32px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  background: #ffffff;
}

.refs-list {
  background: #ffffff;
}

.ref-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.ref-item:last-child {
  border-bottom: none;
}

.ref-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ref-realm-name {
  font-weight: 700;
  font-size: 13px;
  color: #0f172a;
}

.ref-realm-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  color: #64748b;
}

.action-btn-block {
  width: 100%;
  height: 40px;
  font-weight: 600;
  border-radius: 6px;
}

/* Custom Dialog / Modals */
.delete-confirm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 12px 0;
}

.warn-icon-wrapper {
  width: 56px;
  height: 56px;
  background: #fee2e2;
  color: #dc2626;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  margin-bottom: 16px;
}

.confirm-message-container {
  margin-bottom: 20px;
}

.confirm-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.confirm-desc {
  font-size: 14px;
  color: #64748b;
  margin-top: 8px;
  line-height: 20px;
}

.code-highlight {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  background: #f1f5f9;
  color: #475569;
  padding: 2px 6px;
  border-radius: 4px;
}

.confirm-check-alert {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 12px 16px;
  text-align: left;
  width: 100%;
}

.confirm-check-alert .alert-title {
  font-weight: 700;
  color: #9a3412;
  font-size: 13px;
  margin-bottom: 4px;
}

.confirm-check-alert p {
  margin: 0;
  font-size: 12px;
  color: #c2410c;
  line-height: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer :deep(.el-button) {
  height: 38px;
  border-radius: 6px;
  font-weight: 600;
  min-width: 90px;
}

/* Drawer Tip Section */
.drawer-tips {
  margin-top: 24px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  padding: 14px 16px;
  border-radius: 8px;
  font-size: 12px;
  color: #64748b;
  line-height: 20px;
}

.drawer-tips .tips-title {
  font-weight: 700;
  color: #334155;
  display: block;
  margin-bottom: 4px;
}

.drawer-tips p {
  margin: 0;
}

.drawer-footer :deep(.el-button) {
  height: 38px;
  border-radius: 6px;
  font-weight: 600;
  min-width: 90px;
}

.field-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  line-height: 16px;
}
</style>
