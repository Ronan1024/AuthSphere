<script setup lang="ts">
import {
  Delete,
  Edit,
  Plus,
  Refresh,
  SwitchButton,
  View,
} from '@element-plus/icons-vue'
import {
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from 'element-plus'
import { computed, reactive, ref } from 'vue'

import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import {
  type PageResult,
  type SubjectTypeRelationPayload,
  type SubjectTypeRelationRecord,
  subjectTypeRelationApi,
} from '@/api/subjectTypeRelation'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const RELATION_OPTIONS = [
  { label: '管理', value: 1 },
  { label: '拥有', value: 2 },
  { label: '归属', value: 3 },
  { label: '服务', value: 4 },
  { label: '绑定', value: 5 },
]

const loading = ref(false)
const typeLoading = ref(false)
const globalRelationLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const saveLoading = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const detailRecord = ref<SubjectTypeRelationRecord>()
const formRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  size: 10,
  parentTypeId: undefined as string | undefined,
  childTypeId: undefined as string | undefined,
  relationType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const tableData = ref<SubjectTypeRelationRecord[]>([])
const total = ref(0)
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const globalRelationData = ref<SubjectTypeRelationRecord[]>([])

const enabledSubjectTypeOptions = computed(() =>
  subjectTypeOptions.value.filter((item) => Number(item.status) === STATUS_NORMAL),
)

interface RelationPathNode {
  id: string
  name: string
  code: string
  edge?: {
    id?: string
    relationName: string
    allowCreate: boolean
    allowManage: boolean
    status: number
    active?: boolean
  }
}

const relationTypeMap = computed(() => {
  const map = new Map<string, { name: string; code: string }>()
  subjectTypeOptions.value.forEach((item) => {
    map.set(String(item.id), {
      name: item.name || '-',
      code: item.code || '-',
    })
  })
  globalRelationData.value.forEach((item) => {
    map.set(String(item.parentTypeId), {
      name: item.parentTypeName || map.get(String(item.parentTypeId))?.name || '-',
      code: item.parentTypeCode || map.get(String(item.parentTypeId))?.code || '-',
    })
    map.set(String(item.childTypeId), {
      name: item.childTypeName || map.get(String(item.childTypeId))?.name || '-',
      code: item.childTypeCode || map.get(String(item.childTypeId))?.code || '-',
    })
  })
  return map
})

const buildRelationPath = (
  parentTypeId?: string | number,
  childTypeId?: string | number,
  relationType?: number,
  allowCreate = false,
  allowManage = false,
  status = STATUS_NORMAL,
  activeRelationId?: string,
) => {
  if (!parentTypeId || !childTypeId) {
    return []
  }

  const incomingRelations: SubjectTypeRelationRecord[] = []
  const visited = new Set<string>()
  let currentTypeId = String(parentTypeId)

  while (currentTypeId && !visited.has(currentTypeId)) {
    visited.add(currentTypeId)
    const incoming = globalRelationData.value.find((item) => String(item.childTypeId) === currentTypeId)
    if (!incoming) {
      break
    }
    incomingRelations.unshift(incoming)
    currentTypeId = String(incoming.parentTypeId)
  }

  const firstTypeId = incomingRelations.length
    ? String(incomingRelations[0].parentTypeId)
    : String(parentTypeId)
  const firstType = relationTypeMap.value.get(firstTypeId)
  const nodes: RelationPathNode[] = [{
    id: firstTypeId,
    name: firstType?.name || '-',
    code: firstType?.code || '-',
  }]

  incomingRelations.forEach((item) => {
    const childType = relationTypeMap.value.get(String(item.childTypeId))
    nodes.push({
      id: String(item.childTypeId),
      name: childType?.name || item.childTypeName || '-',
      code: childType?.code || item.childTypeCode || '-',
      edge: {
        id: item.id,
        relationName: relationLabel(item.relationType),
        allowCreate: item.allowCreate,
        allowManage: item.allowManage,
        status: Number(item.status),
        active: item.id === activeRelationId,
      },
    })
  })

  const childType = relationTypeMap.value.get(String(childTypeId))
  nodes.push({
    id: String(childTypeId),
    name: childType?.name || '-',
    code: childType?.code || '-',
    edge: {
      id: activeRelationId,
      relationName: relationLabel(relationType),
      allowCreate,
      allowManage,
      status,
      active: true,
    },
  })

  return nodes
}

const formRelationPath = computed(() =>
  buildRelationPath(
    form.parentTypeId,
    form.childTypeId,
    form.relationType,
    form.allowCreate,
    form.allowManage,
    STATUS_NORMAL,
    editingId.value,
  ),
)

const detailRelationPath = computed(() => {
  const item = detailRecord.value
  if (!item) {
    return []
  }
  return buildRelationPath(
    item.parentTypeId,
    item.childTypeId,
    item.relationType,
    item.allowCreate,
    item.allowManage,
    Number(item.status),
    item.id,
  )
})

const detailDescriptions = computed(() => {
  const item = detailRecord.value
  if (!item) {
    return []
  }
  return [
    { label: '上级主体类型', value: `${item.parentTypeName || '-'}（${item.parentTypeCode || '-'}）` },
    { label: '下级主体类型', value: `${item.childTypeName || '-'}（${item.childTypeCode || '-'}）` },
    { label: '关系类型', value: relationLabel(item.relationType) },
    { label: '状态', value: Number(item.status) === STATUS_NORMAL ? '启用' : '禁用' },
    { label: '允许创建', value: item.allowCreate ? '是' : '否' },
    { label: '允许管理', value: item.allowManage ? '是' : '否' },
    { label: '更新时间', value: item.updateTime || '-' },
    { label: '描述', value: item.description || '-' },
  ]
})

const defaultForm = (): SubjectTypeRelationPayload => ({
  parentTypeId: '',
  childTypeId: '',
  relationType: 1,
  allowCreate: false,
  allowManage: false,
  description: '',
})

const form = reactive<SubjectTypeRelationPayload>(defaultForm())

const rules: FormRules<SubjectTypeRelationPayload> = {
  parentTypeId: [{ required: true, message: '请选择上级主体类型', trigger: 'change' }],
  childTypeId: [{ required: true, message: '请选择下级主体类型', trigger: 'change' }],
  relationType: [{ required: true, message: '请选择关系类型', trigger: 'change' }],
  allowCreate: [{ required: true, message: '请选择是否允许创建', trigger: 'change' }],
  allowManage: [{ required: true, message: '请选择是否允许管理', trigger: 'change' }],
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

const relationLabel = (value?: number) => {
  return RELATION_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

const normalizePage = (result: PageResult<SubjectTypeRelationRecord>) => {
  tableData.value = result.records ?? []
  total.value = Number(result.total ?? 0)
}

const fetchSubjectTypes = async () => {
  if (typeLoading.value) {
    return
  }
  typeLoading.value = true
  try {
    subjectTypeOptions.value = await subjectTypeApi.list()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体类型列表失败'))
  } finally {
    typeLoading.value = false
  }
}

const handleSubjectTypeDropdownVisible = (visible: boolean) => {
  if (visible) {
    fetchSubjectTypes()
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await subjectTypeRelationApi.page({
      page: query.page,
      size: query.size,
      parentTypeId: query.parentTypeId || undefined,
      childTypeId: query.childTypeId || undefined,
      relationType: query.relationType,
      status: query.status,
    })
    normalizePage(result)
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取关系规则失败'))
  } finally {
    loading.value = false
  }
}

const fetchGlobalRelations = async () => {
  if (globalRelationLoading.value) {
    return
  }
  globalRelationLoading.value = true
  try {
    const result = await subjectTypeRelationApi.page({
      page: 1,
      size: 100,
    })
    globalRelationData.value = result.records ?? []
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取全局关系规则失败'))
  } finally {
    globalRelationLoading.value = false
  }
}

const resetQuery = () => {
  query.page = 1
  query.parentTypeId = undefined
  query.childTypeId = undefined
  query.relationType = undefined
  query.status = undefined
  fetchData()
}

const assignForm = (value: SubjectTypeRelationPayload) => {
  Object.assign(form, value)
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = undefined
  assignForm(defaultForm())
  dialogVisible.value = true
  fetchSubjectTypes()
  fetchGlobalRelations()
}

const openEdit = (row: SubjectTypeRelationRecord) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  assignForm({
    parentTypeId: row.parentTypeId,
    childTypeId: row.childTypeId,
    relationType: row.relationType,
    allowCreate: row.allowCreate,
    allowManage: row.allowManage,
    description: row.description || '',
  })
  dialogVisible.value = true
  fetchSubjectTypes()
  fetchGlobalRelations()
}

const openDetail = (row: SubjectTypeRelationRecord) => {
  detailRecord.value = row
  detailVisible.value = true
  fetchGlobalRelations()
}

const submitForm = async () => {
  if (!formRef.value) {
    showErrorMessage('表单尚未初始化，请重新打开后再保存')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项')
    return
  }

  if (String(form.parentTypeId) === String(form.childTypeId)) {
    showErrorMessage('上级主体类型和下级主体类型不能相同')
    return
  }

  saveLoading.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await subjectTypeRelationApi.create(payload)
    } else if (editingId.value) {
      await subjectTypeRelationApi.update(editingId.value, payload)
    }
    showSuccessMessage(dialogMode.value === 'create' ? '关系规则已创建' : '关系规则已更新')
    dialogVisible.value = false
    fetchData()
    fetchGlobalRelations()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存关系规则失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: SubjectTypeRelationRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响主体关系创建和管理校验。确认${action}？`,
    `${action}关系规则`,
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
    await subjectTypeRelationApi.toggleStatus(row.id)
    showSuccessMessage(`关系规则已${action}`)
    fetchData()
    fetchGlobalRelations()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, `${action}关系规则失败`))
  }
}

const removeRelation = async (row: SubjectTypeRelationRecord) => {
  await ElMessageBox.confirm(
    `删除后「${row.parentTypeName || row.parentTypeCode} - ${relationLabel(row.relationType)} - ${row.childTypeName || row.childTypeCode}」规则将不可恢复。确认删除？`,
    '删除关系规则',
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
    await subjectTypeRelationApi.remove(row.id)
    showSuccessMessage('关系规则已删除')
    fetchData()
    fetchGlobalRelations()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '删除关系规则失败'))
  }
}

fetchSubjectTypes()
fetchData()
fetchGlobalRelations()
</script>

<template>
  <section class="subject-relation-page">
    <div class="page-heading">
      <div>
        <h1>关系规则</h1>
        <p>配置主体类型之间允许建立的父子关系、关系语义，以及创建和管理权限边界。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增关系规则</el-button>
    </div>

    <el-card shadow="never" class="section-card">
      <div class="section-intro">
        <div>
          <strong>主体类型关系规则</strong>
          <span>同一上级类型、下级类型和关系类型只能配置一条规则。</span>
        </div>
      </div>

      <div class="table-toolbar">
        <div class="filter-row">
          <el-select
            v-model="query.parentTypeId"
            clearable
            filterable
            :loading="typeLoading"
            placeholder="上级主体类型"
            class="type-filter"
            empty-text="暂无主体类型"
            popper-class="subject-type-select-popper"
            @visible-change="handleSubjectTypeDropdownVisible"
          >
            <el-option
              v-for="item in subjectTypeOptions"
              :key="item.id"
              :label="`${item.name}（${item.code}）`"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="query.childTypeId"
            clearable
            filterable
            :loading="typeLoading"
            placeholder="下级主体类型"
            class="type-filter"
            empty-text="暂无主体类型"
            popper-class="subject-type-select-popper"
            @visible-change="handleSubjectTypeDropdownVisible"
          >
            <el-option
              v-for="item in subjectTypeOptions"
              :key="item.id"
              :label="`${item.name}（${item.code}）`"
              :value="item.id"
            />
          </el-select>
          <el-select v-model="query.relationType" clearable placeholder="关系类型" class="status-filter">
            <el-option
              v-for="item in RELATION_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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

      <el-table v-loading="loading" :data="tableData" row-key="id" empty-text="暂无关系规则">
        <el-table-column label="上级主体类型" min-width="180">
          <template #default="{ row }">
            <div class="type-cell">
              <strong>{{ row.parentTypeName || '-' }}</strong>
              <span>{{ row.parentTypeCode || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="关系类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ relationLabel(row.relationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下级主体类型" min-width="180">
          <template #default="{ row }">
            <div class="type-cell">
              <strong>{{ row.childTypeName || '-' }}</strong>
              <span>{{ row.childTypeCode || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="能力" min-width="160">
          <template #default="{ row }">
            <div class="capability-tags">
              <el-tag :type="row.allowCreate ? 'success' : 'info'">
                {{ row.allowCreate ? '允许创建' : '禁止创建' }}
              </el-tag>
              <el-tag :type="row.allowManage ? 'success' : 'info'">
                {{ row.allowManage ? '允许管理' : '禁止管理' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === STATUS_NORMAL ? 'success' : 'info'">
              {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row)">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" :icon="Delete" @click="removeRelation(row)">删除</el-button>
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
      :title="dialogMode === 'create' ? '新增关系规则' : '编辑关系规则'"
      width="720px"
      destroy-on-close
    >
      <div v-if="formRelationPath.length" class="detail-section">
        <div class="detail-section__title">全局关系路径预览</div>
        <div v-loading="globalRelationLoading" class="path-graph path-graph--form">
          <template v-for="(node, index) in formRelationPath" :key="`${node.id}-${index}`">
            <div v-if="node.edge" class="path-edge">
              <span>{{ node.edge.relationName }}</span>
            </div>
            <div
              class="path-node"
              :class="{ 'path-node--active': node.edge?.active }"
            >
              <strong>{{ node.name }}</strong>
              <span>{{ node.code }}</span>
              <div v-if="node.edge" class="path-node__tags">
                <el-tag size="small" :type="Number(node.edge.status) === STATUS_NORMAL ? 'success' : 'info'">
                  {{ Number(node.edge.status) === STATUS_NORMAL ? '启用' : '禁用' }}
                </el-tag>
                <el-tag size="small" :type="node.edge.allowCreate ? 'success' : 'info'">
                  {{ node.edge.allowCreate ? '创建' : '不可创建' }}
                </el-tag>
                <el-tag size="small" :type="node.edge.allowManage ? 'success' : 'info'">
                  {{ node.edge.allowManage ? '管理' : '不可管理' }}
                </el-tag>
              </div>
            </div>
          </template>
        </div>
      </div>

      <el-form
        id="subject-relation-form"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submitForm"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级主体类型" prop="parentTypeId">
              <el-select
                v-model="form.parentTypeId"
                class="full-field"
                filterable
                :loading="typeLoading"
                placeholder="请选择上级主体类型"
                empty-text="暂无可用主体类型"
                popper-class="subject-type-select-popper"
                @visible-change="handleSubjectTypeDropdownVisible"
              >
                <el-option
                  v-for="item in enabledSubjectTypeOptions"
                  :key="item.id"
                  :label="`${item.name}（${item.code}）`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下级主体类型" prop="childTypeId">
              <el-select
                v-model="form.childTypeId"
                class="full-field"
                filterable
                :loading="typeLoading"
                placeholder="请选择下级主体类型"
                empty-text="暂无可用主体类型"
                popper-class="subject-type-select-popper"
                @visible-change="handleSubjectTypeDropdownVisible"
              >
                <el-option
                  v-for="item in enabledSubjectTypeOptions"
                  :key="item.id"
                  :label="`${item.name}（${item.code}）`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关系类型" prop="relationType">
              <el-select v-model="form.relationType" class="full-field" placeholder="请选择关系类型">
                <el-option
                  v-for="item in RELATION_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>

        <div class="switch-grid">
          <label>
            <span>允许创建</span>
            <el-switch v-model="form.allowCreate" />
          </label>
          <label>
            <span>允许管理</span>
            <el-switch v-model="form.allowManage" />
          </label>
        </div>
      </el-form>

      <template #footer>
        <el-button native-type="button" @click="dialogVisible = false">取消</el-button>
        <el-button
          form="subject-relation-form"
          native-type="submit"
          type="primary"
          :loading="saveLoading"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="关系规则详情" size="640px">
      <template v-if="detailRecord">
        <div class="detail-section">
          <div class="detail-section__title">全局关系路径</div>
          <div v-loading="globalRelationLoading" class="path-graph">
            <template v-for="(node, index) in detailRelationPath" :key="`${node.id}-${index}`">
              <div v-if="node.edge" class="path-edge">
                <span>{{ node.edge.relationName }}</span>
              </div>
              <div
                class="path-node"
                :class="{ 'path-node--active': node.edge?.active }"
              >
                <strong>{{ node.name }}</strong>
                <span>{{ node.code }}</span>
                <div v-if="node.edge" class="path-node__tags">
                  <el-tag size="small" :type="Number(node.edge.status) === STATUS_NORMAL ? 'success' : 'info'">
                    {{ Number(node.edge.status) === STATUS_NORMAL ? '启用' : '禁用' }}
                  </el-tag>
                  <el-tag size="small" :type="node.edge.allowCreate ? 'success' : 'info'">
                    {{ node.edge.allowCreate ? '创建' : '不可创建' }}
                  </el-tag>
                  <el-tag size="small" :type="node.edge.allowManage ? 'success' : 'info'">
                    {{ node.edge.allowManage ? '管理' : '不可管理' }}
                  </el-tag>
                </div>
              </div>
            </template>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section__title">基础信息</div>
          <el-descriptions :column="1" border>
            <el-descriptions-item
              v-for="item in detailDescriptions"
              :key="item.label"
              :label="item.label"
            >
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.subject-relation-page {
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

.relation-tree {
  margin-bottom: 18px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfcfe;
}

.relation-tree--detail {
  margin-bottom: 0;
}

.relation-tree--form {
  max-height: 280px;
  margin-bottom: 0;
  overflow: auto;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 18px;
}

.detail-section__title {
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.path-graph {
  display: flex;
  gap: 12px;
  align-items: stretch;
  overflow: auto;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfcfe;
}

.path-graph--form {
  max-height: 240px;
}

.path-node {
  min-width: 190px;
  padding: 14px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #ffffff;
}

.path-node--active {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.14);
}

.path-node strong,
.path-node span {
  display: block;
}

.path-node strong {
  overflow: hidden;
  color: #111827;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.path-node span {
  margin-top: 5px;
  color: #6b7280;
  font-size: 12px;
}

.path-node__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.path-edge {
  position: relative;
  display: flex;
  flex: 0 0 92px;
  align-items: center;
  justify-content: center;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.path-edge::before,
.path-edge::after {
  position: absolute;
  top: 50%;
  width: 24px;
  height: 1px;
  background: #93c5fd;
  content: "";
}

.path-edge::before {
  left: 0;
}

.path-edge::after {
  right: 0;
}

.path-edge span {
  z-index: 1;
  padding: 2px 8px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #eff6ff;
}

.relation-tree__header {
  display: flex;
  gap: 10px;
  align-items: baseline;
  margin-bottom: 14px;
}

.relation-tree__header strong {
  color: #111827;
  font-size: 15px;
}

.relation-tree__header span {
  color: #6b7280;
  font-size: 13px;
}

.relation-tree__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.relation-tree__parent {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.relation-tree__children {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.relation-tree__edge {
  display: grid;
  grid-template-columns: 112px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.relation-line {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.relation-line::before,
.relation-line::after {
  position: absolute;
  top: 50%;
  width: 22px;
  height: 1px;
  background: #93c5fd;
  content: "";
}

.relation-line::before {
  left: 0;
}

.relation-line::after {
  right: 0;
}

.relation-line span {
  z-index: 1;
  padding: 2px 8px;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #eff6ff;
}

.relation-node {
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #ffffff;
}

.relation-node strong,
.relation-node span {
  display: block;
}

.relation-node strong {
  overflow: hidden;
  color: #111827;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.relation-node span {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.relation-node--parent {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.relation-node--child {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.relation-node--active {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.14);
}

.relation-node__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
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

.type-filter {
  width: 220px;
}

.status-filter {
  width: 140px;
}

.type-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.type-cell strong {
  color: #111827;
  font-weight: 600;
}

.type-cell span {
  color: #6b7280;
  font-size: 12px;
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

.full-field {
  width: 100%;
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

@media (max-width: 960px) {
  .relation-tree__parent,
  .relation-tree__edge {
    grid-template-columns: 1fr;
  }

  .relation-line {
    justify-content: flex-start;
  }

  .relation-line::before,
  .relation-line::after {
    display: none;
  }

  .relation-node--child {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
