<script setup lang="ts">
import {
  ArrowDown,
  Plus,
  Refresh,
  View,
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { type RealmOption, realmApi } from '@/api/realm'
import { type SubjectPayload, type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

import SubjectMembersTab from './components/SubjectMembersTab.vue'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const loading = ref(false)
const optionLoading = ref(false)
const saveLoading = ref(false)

// Detail & Drawers state
const detailVisible = ref(false)
const currentDetail = ref<SubjectRecord>()
const activeDetailTab = ref('basic')

// Add / Edit Subject Drawer
const isSubjectDrawerOpen = ref(false)
const subjectDrawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const subjectFormRef = ref<FormInstance>()

const subjectForm = reactive<any>({
  subjectTypeId: '',
  realmId: '',
  code: '',
  name: '',
  rootSubjectId: null,
  parentSubjectId: null,
  isRoot: 0,
  builtIn: 0,
  description: '',
  status: 1,
})

// Open App Drawer
const isOpenAppDrawerOpen = ref(false)
const openAppForm = reactive({
  subjectId: '',
  subjectName: '',
  appId: '1',
  instanceName: '租户A商城实例',
  instanceCode: 'tenant_a_mall',
  expireTime: '',
  status: 1,
  initClient: true,
  initResource: true,
  initRole: true
})

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  realmId: undefined as string | undefined,
  subjectTypeId: undefined as string | undefined,
  status: undefined as number | undefined,
  parentSubjectId: undefined as string | undefined,
})

const tableData = ref<SubjectRecord[]>([])
const total = ref(0)
const realmOptions = ref<RealmOption[]>([])
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])

const availableSubjectOptions = computed(() =>
  subjectOptions.value.filter((item) => item.id !== editingId.value),
)

const permissionsList = ref([
  {
    id: 1,
    subjectName: '租户A',
    appInstanceName: '租户A商城实例',
    clientName: '商城平台端',
    roles: ['商城管理员', '商城运营'],
    roleType: '自定义',
    status: '启用',
    authTime: '2026-05-01'
  },
  {
    id: 2,
    subjectName: '租户A',
    appInstanceName: '租户A支付实例',
    clientName: '支付平台端',
    roles: ['支付管理员'],
    roleType: '自定义',
    status: '启用',
    authTime: '2026-05-03'
  },
  {
    id: 3,
    subjectName: '支付服务商A',
    appInstanceName: '服务商支付实例',
    clientName: '支付商户端',
    roles: [] as string[],
    roleType: '-',
    status: '无',
    authTime: '-'
  }
])

const handleAdjustRole = (row: any) => {
  ElMessage.info(`调整角色: ${row.appInstanceName}`)
}

const handleRemoveRole = (row: any) => {
  ElMessageBox.confirm(`确定要移除在 ${row.appInstanceName} 下的客户端角色吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    permissionsList.value = permissionsList.value.filter(item => item.id !== row.id)
    ElMessage.success('已移除')
  }).catch(() => {})
}

const handleAssignRole = () => {
  ElMessage.success('调起客户端角色分配向导...')
}

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
      status: query.status,
      parentSubjectId: query.parentSubjectId,
    })
    tableData.value = result.records ?? []
    total.value = Number(result.total ?? 0)
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体列表失败'))
  } finally {
    loading.value = false
  }
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
  optionLoading.value = true
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
      showErrorMessage(getErrorMessage(realmsResult.reason, '获取身份域选项失败'))
    }

    if (subjectTypesResult.status === 'fulfilled') {
      subjectTypeOptions.value = subjectTypesResult.value ?? []
    } else {
      subjectTypeOptions.value = []
      showErrorMessage(getErrorMessage(subjectTypesResult.reason, '获取主体类型选项失败'))
    }

    if (subjectsResult.status === 'fulfilled') {
      subjectOptions.value = subjectsResult.value ?? []
    } else {
      subjectOptions.value = []
      showErrorMessage(getErrorMessage(subjectsResult.reason, '获取父级主体选项失败'))
    }
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体表单选项失败'))
  } finally {
    optionLoading.value = false
  }
}

const resetQuery = () => {
  query.page = 1
  query.name = ''
  query.code = ''
  query.realmId = undefined
  query.subjectTypeId = undefined
  query.status = undefined
  query.parentSubjectId = undefined
  fetchData()
}

const getSubjectTypePillText = (row: SubjectRecord) => {
  const typeCode = row.subjectTypeCode?.toLowerCase() || ''
  if (typeCode.includes('tenant')) return '租户主体'
  if (typeCode.includes('merchant')) return '商户主体'
  if (typeCode.includes('provider') || typeCode.includes('service')) return '服务商主体'
  return '消费者主体'
}

const getSubjectTypePillClass = (row: SubjectRecord) => {
  const typeCode = row.subjectTypeCode?.toLowerCase() || ''
  if (typeCode.includes('tenant')) return 'pill-blue'
  if (typeCode.includes('merchant')) return 'pill-orange'
  if (typeCode.includes('provider') || typeCode.includes('service')) return 'pill-indigo'
  return 'pill-gray'
}

const openCreate = () => {
  subjectDrawerMode.value = 'create'
  editingId.value = undefined
  Object.assign(subjectForm, {
    subjectTypeId: '',
    realmId: '',
    code: '',
    name: '',
    rootSubjectId: null,
    parentSubjectId: null,
    isRoot: 0,
    builtIn: 0,
    description: '',
    status: 1,
  })
  isSubjectDrawerOpen.value = true
  fetchOptions()
}

const openEdit = async (row: SubjectRecord) => {
  subjectDrawerMode.value = 'edit'
  editingId.value = row.id
  loading.value = true
  try {
    const detail = await subjectApi.detail(row.id)
    Object.assign(subjectForm, {
      subjectTypeId: detail.subjectTypeId,
      realmId: detail.realmId,
      code: detail.code,
      name: detail.name,
      rootSubjectId: detail.rootSubjectId || null,
      parentSubjectId: detail.parentSubjectId || null,
      isRoot: Number(detail.isRoot ?? 0),
      builtIn: Number(detail.builtIn ?? 0),
      description: detail.description || '',
    })
    isSubjectDrawerOpen.value = true
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体详情失败'))
  } finally {
    loading.value = false
  }
}

const openOpenAppDrawer = (row: SubjectRecord) => {
  openAppForm.subjectId = row.id
  openAppForm.subjectName = row.name
  openAppForm.instanceName = `${row.name}商城实例`
  openAppForm.instanceCode = `${row.code}_mall`
  isOpenAppDrawerOpen.value = true
}

const submitOpenApp = () => {
  saveLoading.value = true
  setTimeout(() => {
    saveLoading.value = false
    isOpenAppDrawerOpen.value = false
    ElMessage.success('应用开通成功！已经为您自动初始化客户端、复制资源模板和配置角色。')
  }, 800)
}

const route = useRoute()
const router = useRouter()

const openDetail = (row: SubjectRecord) => {
  router.push({ path: `/subjects/detail/${row.id}` })
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
        activeDetailTab.value = 'basic'
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

const submitSubjectForm = async () => {
  if (!subjectFormRef.value) return
  const valid = await subjectFormRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项')
    return
  }

  saveLoading.value = true
  try {
    const payload = {
      ...subjectForm,
      rootSubjectId: subjectForm.rootSubjectId || null,
      parentSubjectId: subjectForm.parentSubjectId || null,
      isRoot: Number(subjectForm.isRoot),
      builtIn: Number(subjectForm.builtIn),
    }
    if (subjectDrawerMode.value === 'create') {
      await subjectApi.create(payload)
    } else if (editingId.value) {
      await subjectApi.update(editingId.value, payload)
    }
    showSuccessMessage(subjectDrawerMode.value === 'create' ? '主体已创建' : '主体已更新')
    isSubjectDrawerOpen.value = false
    fetchData()
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存主体失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: SubjectRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响该主体进入系统和参与授权边界计算。确认${action}？`,
    `${action}主体`,
    {
      type: row.status === STATUS_NORMAL ? 'warning' : 'info',
      confirmButtonText: action,
      cancelButtonText: '取消',
    },
  )

  try {
    await subjectApi.toggleStatus(row.id)
    showSuccessMessage(`主体已${action}`)
    if (currentDetail.value?.id === row.id) {
      currentDetail.value.status = currentDetail.value.status === STATUS_NORMAL ? STATUS_DISABLED : STATUS_NORMAL
    }
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
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    },
  )

  try {
    await subjectApi.remove(row.id)
    showSuccessMessage('主体已删除')
    if (currentDetail.value?.id === row.id) {
      goBack()
    } else {
      fetchData()
      fetchOptions()
    }
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '删除主体失败'))
  }
}

const copyText = (text?: string) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('复制成功')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const mockOperations = ref([
  {
    time: '2026-06-08 15:42:10',
    operator: 'admin',
    type: '编辑主体',
    content: '更新主体描述信息为: 租户A 作为业务主体，承载商城、支付、仓储等应用实例和数据边界。',
    source: '控制台',
  },
  {
    time: '2026-06-08 10:15:33',
    operator: 'admin',
    type: '开通应用实例',
    content: '新增应用实例: 租户A商城实例 (tenant_a_mall)',
    source: '控制台',
  },
  {
    time: '2026-06-08 09:30:22',
    operator: 'admin',
    type: '新增关系',
    content: '绑定父级主体关系: 平台主体 (platform) 为上级管理主体',
    source: '控制台',
  },
])

onMounted(() => {
  fetchData()
  fetchOptions()
})
</script>

<template>
  <section class="subject-page">
    <!-- View 1: Detail View -->
    <template v-if="detailVisible && currentDetail">
      <div class="subject-detail-view">
        <div class="top-breadcrumb">
          <span class="crumb-back" @click="goBack">← 返回主体列表</span>
          <span class="divider">/</span>
          <span class="crumb-curr">主体详情 - {{ currentDetail.name }}</span>
        </div>

        <!-- Hero block -->
        <div class="premium-hero-card">
          <div class="hero-left-section">
            <div class="subject-icon-box">
              <span>{{ currentDetail.name?.slice(0, 1) || '租' }}</span>
            </div>
            <div class="subject-meta-info">
              <div class="title-row">
                <h2>{{ currentDetail.name }}</h2>
                <span class="custom-badge" :class="currentDetail.status === STATUS_NORMAL ? 'green' : 'red'">
                  {{ currentDetail.status === STATUS_NORMAL ? '启用' : '禁用' }}
                </span>
              </div>
              <div class="meta-row">
                <span>主体编码：<code class="code-text">{{ currentDetail.code }}</code></span>
                <span class="dot-separator">|</span>
                <span>主体类型：{{ currentDetail.subjectTypeName || '租户' }}</span>
                <span class="dot-separator">|</span>
                <span>所属身份域：{{ currentDetail.realmCode || 'tenant_realm' }}</span>
                <span class="dot-separator">|</span>
                <span>父级主体：{{ currentDetail.parentSubjectName || '平台主体' }}</span>
              </div>
            </div>
          </div>
          <div class="hero-right-actions">
            <el-button @click="openEdit(currentDetail)">编辑</el-button>
            <el-button type="primary" @click="openOpenAppDrawer(currentDetail)">开通应用</el-button>
            <el-button :type="currentDetail.status === STATUS_NORMAL ? 'danger' : 'success'" plain @click="toggleStatus(currentDetail)">
              {{ currentDetail.status === STATUS_NORMAL ? '禁用' : '启用' }}
            </el-button>
          </div>
        </div>

        <!-- Tabs Navigation -->
        <div class="detail-tabs-wrapper">
          <el-tabs v-model="activeDetailTab" class="custom-detail-tabs">
            <!-- Tab 1: Basic Info -->
            <el-tab-pane label="基础信息" name="basic">
              <div>
                <!-- Left: Attributes -->
                <div class="pane-card">
                  <div class="pane-header">
                    <h3>基础信息</h3>
                    <el-button size="small" @click="openEdit(currentDetail!)">编辑基础信息</el-button>
                  </div>
                  <div class="info-grid-2col">
                    <div class="info-item">
                      <label>主体名称</label>
                      <span>{{ currentDetail.name }}</span>
                    </div>
                    <div class="info-item">
                      <label>主体编码</label>
                      <span class="code-text">{{ currentDetail.code }}</span>
                    </div>
                    <div class="info-item">
                      <label>主体类型</label>
                      <span>{{ currentDetail.subjectTypeName || '租户' }}</span>
                    </div>
                    <div class="info-item">
                      <label>所属身份域</label>
                      <span>{{ currentDetail.realmCode || 'tenant_realm' }}</span>
                    </div>
                    <div class="info-item">
                      <label>父级主体</label>
                      <span>{{ currentDetail.parentSubjectName || '平台主体' }}</span>
                    </div>
                    <div class="info-item">
                      <label>状态</label>
                      <span class="custom-badge" :class="currentDetail.status === STATUS_NORMAL ? 'green' : 'red'">
                        {{ currentDetail.status === STATUS_NORMAL ? '启用' : '禁用' }}
                      </span>
                    </div>
                    <div class="info-item full-width">
                      <label>备注</label>
                      <span class="desc-text">{{ currentDetail.description || '租户A 作为业务主体，承载商城、支付、仓储等应用实例和数据边界。' }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Bottom Split: Members & Apps Summary -->
              <div class="basic-grid-layout" style="margin-top: 16px;">
                <div class="pane-card">
                  <div class="pane-header">
                    <h3>成员账号</h3>
                    <el-button type="primary" size="small" @click="activeDetailTab = 'members'">添加成员</el-button>
                  </div>
                  <div class="summary-list-box">
                    <div class="summary-list-item">
                      <div class="item-left">
                        <strong>lisi</strong>
                        <small>成员类型：管理员 · 状态：启用</small>
                      </div>
                      <div class="item-ops">
                        <span class="link-btn-text" @click="activeDetailTab = 'members'">角色</span>
                        <span class="link-btn-text danger" @click="activeDetailTab = 'members'">移除</span>
                      </div>
                    </div>
                    <div class="summary-list-item">
                      <div class="item-left">
                        <strong>tenant_ops</strong>
                        <small>成员类型：成员 · 状态：启用</small>
                      </div>
                      <div class="item-ops">
                        <span class="link-btn-text" @click="activeDetailTab = 'members'">角色</span>
                        <span class="link-btn-text danger" @click="activeDetailTab = 'members'">移除</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="pane-card">
                  <div class="pane-header">
                    <h3>应用权限</h3>
                    <el-button type="primary" size="small" @click="handleAssignRole">授权客户端</el-button>
                  </div>
                  <el-table :data="permissionsList" style="width: 100%" class="custom-permissions-table">
                    <el-table-column prop="subjectName" label="主体" min-width="90" />
                    <el-table-column prop="appInstanceName" label="应用实例" min-width="130" />
                    <el-table-column prop="clientName" label="客户端" min-width="110" />
                    <el-table-column label="角色" min-width="140">
                      <template #default="{ row }">
                        <div class="roles-tags-flex" v-if="row.roles && row.roles.length > 0">
                          <el-tag v-for="role in row.roles" :key="role" size="small" class="role-badge-tag" style="margin-right: 4px; margin-bottom: 4px;">
                            {{ role }}
                          </el-tag>
                        </div>
                        <span class="status-pill orange-light" v-else>待分配</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="roleType" label="角色类型" width="85" />
                    <el-table-column label="状态" width="70">
                      <template #default="{ row }">
                        <span class="status-pill green-pill" v-if="row.status === '启用'">启用</span>
                        <span class="status-pill gray-pill" v-else>无</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="authTime" label="授权时间" width="100" />
                    <el-table-column label="操作" width="130" fixed="right" align="right">
                      <template #default="{ row }">
                        <div class="row-actions-flex">
                          <template v-if="row.roles && row.roles.length > 0">
                            <span class="action-btn-link" @click="handleAdjustRole(row)">调整角色</span>
                            <span class="action-btn-link danger" @click="handleRemoveRole(row)">移除</span>
                          </template>
                          <template v-else>
                            <span class="action-btn-link" @click="handleAssignRole">分配角色</span>
                          </template>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </el-tab-pane>

            <!-- Tab 2: Members Tab Component -->
            <el-tab-pane label="成员账号" name="members">
              <SubjectMembersTab
                v-if="activeDetailTab === 'members'"
                :subject-id="currentDetail.id"
                :realm-code="currentDetail.realmCode"
              />
            </el-tab-pane>

            <!-- Tab 3: Open Apps -->
            <el-tab-pane label="应用权限" name="apps">
              <div class="pane-card" style="min-height: 400px;">
                <div class="pane-header">
                  <h3>应用权限</h3>
                  <el-button type="primary" @click="handleAssignRole">授权客户端</el-button>
                </div>
                <el-table :data="permissionsList" style="width: 100%" class="custom-permissions-table">
                  <el-table-column prop="subjectName" label="主体" min-width="120" />
                  <el-table-column prop="appInstanceName" label="应用实例" min-width="150" />
                  <el-table-column prop="clientName" label="客户端" min-width="130" />
                  <el-table-column label="角色" min-width="180">
                    <template #default="{ row }">
                      <div class="roles-tags-flex" v-if="row.roles && row.roles.length > 0">
                        <el-tag v-for="role in row.roles" :key="role" size="default" class="role-badge-tag" style="margin-right: 6px; margin-bottom: 6px;">
                          {{ role }}
                        </el-tag>
                      </div>
                      <span class="status-pill orange-light" v-else>待分配</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="roleType" label="角色类型" width="100" />
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                      <span class="status-pill green-pill" v-if="row.status === '启用'">启用</span>
                      <span class="status-pill gray-pill" v-else>无</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="authTime" label="授权时间" width="140" />
                  <el-table-column label="操作" width="160" fixed="right" align="right">
                    <template #default="{ row }">
                      <div class="row-actions-flex">
                        <template v-if="row.roles && row.roles.length > 0">
                          <span class="action-btn-link" @click="handleAdjustRole(row)">调整角色</span>
                          <span class="action-btn-link danger" @click="handleRemoveRole(row)">移除</span>
                        </template>
                        <template v-else>
                          <span class="action-btn-link" @click="handleAssignRole">分配角色</span>
                        </template>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>

            <!-- Tab 4: Sub subjects Tree-Table -->
            <el-tab-pane label="子主体" name="relation">
              <div class="pane-card" style="min-height: 400px;">
                <div class="pane-header">
                  <h3>子主体</h3>
                  <el-button type="primary" @click="openCreate">新增子主体</el-button>
                </div>

                <div class="tree-table-shell">
                  <div class="tree-row head">
                    <div></div>
                    <div>主体名称</div>
                    <div>主体类型</div>
                    <div>状态</div>
                    <div>操作</div>
                  </div>
                  
                  <!-- Row 1 -->
                  <div class="tree-row">
                    <div class="toggle-icon">▾</div>
                    <div><strong>{{ currentDetail.name }}</strong> <code class="code-text">{{ currentDetail.code }}</code></div>
                    <div><span class="custom-badge blue">租户</span></div>
                    <div><span class="custom-badge green">启用</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text" @click="openCreate">新增子级</span>
                    </div>
                  </div>

                  <!-- Row 2 -->
                  <div class="tree-row indent">
                    <div class="toggle-icon"></div>
                    <div class="nested-text">├─ 商户W <code class="code-text">merchant_w</code></div>
                    <div><span class="custom-badge orange">商户</span></div>
                    <div><span class="custom-badge green">启用</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text">详情</span>
                      <span class="link-btn-text">编辑</span>
                    </div>
                  </div>

                  <!-- Row 3 -->
                  <div class="tree-row indent">
                    <div class="toggle-icon"></div>
                    <div class="nested-text">├─ 私域商户M <code class="code-text">merchant_m</code></div>
                    <div><span class="custom-badge orange">商户</span></div>
                    <div><span class="custom-badge green">启用</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text">详情</span>
                      <span class="link-btn-text">编辑</span>
                    </div>
                  </div>

                  <!-- Row 4 -->
                  <div class="tree-row indent">
                    <div class="toggle-icon"></div>
                    <div class="nested-text">└─ 普通消费者主体 <code class="code-text">consumer_libai</code></div>
                    <div><span class="custom-badge gray">消费者</span></div>
                    <div><span class="custom-badge gray">普通</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text">详情</span>
                      <span class="link-btn-text">编辑</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Tab 5: Audit Log -->
            <el-tab-pane label="审计日志" name="audit">
              <div class="pane-card" style="min-height: 400px;">
                <div class="pane-header">
                  <h3>审计合规性检查记录</h3>
                </div>
                <el-table :data="mockOperations" size="default" class="premium-table">
                  <el-table-column prop="time" label="操作时间" width="180" />
                  <el-table-column prop="operator" label="操作人" width="120" />
                  <el-table-column prop="type" label="操作类型" width="140">
                    <template #default="{ row }">
                      <el-tag size="small" :type="row.type.includes('编辑') ? 'primary' : 'success'">{{ row.type }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="content" label="操作内容" min-width="300" />
                  <el-table-column prop="source" label="来源" width="120" />
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </template>

    <!-- View 2: List View -->
    <template v-else>
      <div class="page-heading">
        <div class="heading-text">
          <h1>主体管理</h1>
          <p>主体用于定义业务归属、父子管理关系和应用开通边界，账号加入主体后才可代表该主体操作。</p>
        </div>
        <div class="heading-actions">
          <el-button type="primary" :icon="Plus" class="primary-action" @click="openCreate">新增主体</el-button>
          <el-button :icon="Refresh" class="refresh-action" @click="fetchData" :loading="loading">刷新</el-button>
        </div>
      </div>

      <!-- Filters -->
      <el-card shadow="never" class="filter-card">
        <div class="filter-grid">
          <div class="filter-row" style="grid-template-columns: repeat(4, 1fr) auto;">
            <div class="filter-col">
              <span class="filter-label">主体名称 / 编码</span>
              <el-input v-model="query.name" placeholder="租户A / tenant_a" clearable @keyup.enter="fetchData" />
            </div>
            <div class="filter-col">
              <span class="filter-label">所属身份域</span>
              <el-select v-model="query.realmId" placeholder="全部身份域" clearable popper-class="account-realm-select-popper">
                <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="filter-col">
              <span class="filter-label">主体类型</span>
              <el-select v-model="query.subjectTypeId" placeholder="全部类型" clearable popper-class="subject-type-select-popper">
                <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="filter-col">
              <span class="filter-label">父级主体</span>
              <el-select v-model="query.parentSubjectId" placeholder="平台主体" clearable popper-class="subject-select-popper">
                <el-option v-for="item in subjectOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="filter-actions">
              <el-button type="primary" @click="fetchData" :loading="loading">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Main Table -->
      <el-card shadow="never" class="table-card">
        <div class="table-header-row">
          <div>
            <span class="table-title">主体列表</span>
          </div>
        </div>

        <el-table v-loading="loading" :data="tableData" row-key="id" class="custom-table" border>
          <el-table-column label="主体名称" min-width="180">
            <template #default="{ row }">
              <div class="subject-title-cell">
                <span class="subject-name-link" @click="openDetail(row)">{{ row.name }}</span>
                <span class="subject-code-sub">{{ row.code }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="主体类型" min-width="140">
            <template #default="{ row }">
              <span class="subject-type-pill" :class="getSubjectTypePillClass(row)">
                {{ getSubjectTypePillText(row) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="所属身份域" min-width="180">
            <template #default="{ row }">
              <div class="realm-cell">
                <span class="realm-name-bold">{{ row.realmName || '-' }}</span>
                <span class="realm-code-sub">{{ row.realmCode || '-' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="父级主体" min-width="140">
            <template #default="{ row }">
              <span class="parent-text">{{ row.parentName || row.parentSubjectName || '无' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="成员" width="100" align="center">
            <template #default="{ row }">
              <span class="count-text">{{ row.memberCount ?? 0 }}</span>
            </template>
          </el-table-column>

          <el-table-column label="签约应用" width="100" align="center">
            <template #default="{ row }">
              <span class="count-text">{{ row.contractCount ?? 0 }}</span>
            </template>
          </el-table-column>

          <el-table-column label="客户端入口" width="110" align="center">
            <template #default="{ row }">
              <span class="count-text">{{ row.clientCount ?? 0 }}</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="status-badge" :class="row.status === STATUS_NORMAL ? 'enabled' : 'disabled'">
                <span class="dot"></span>
                {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="创建时间" width="160" />

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <span class="link-btn" @click="openDetail(row)">详情</span>
                <span class="link-btn" @click="openEdit(row)">编辑</span>
                <span class="link-btn" @click="openOpenAppDrawer(row)">开通应用</span>
                <el-dropdown trigger="click">
                  <span class="link-more-btn">更多 <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="toggleStatus(row)">
                        {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        :disabled="Number(row.builtIn) === 1"
                        class="danger-item"
                        divided
                        @click="removeSubject(row)"
                      >
                        删除主体
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-container">
          <div class="footer-note-text">
            共 {{ total }} 条记录。
          </div>
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="prev, pager, next, sizes, jumper"
            :total="total"
            @change="fetchData"
          />
        </div>
      </el-card>
    </template>

    <!-- Dialog 1: Create / Edit Subject Dialog -->
    <el-dialog
      v-model="isSubjectDrawerOpen"
      :title="subjectDrawerMode === 'create' ? '新增主体' : '编辑主体'"
      width="800px"
      destroy-on-close
      class="custom-subject-dialog"
    >
      <el-form
        ref="subjectFormRef"
        v-loading="optionLoading"
        :model="subjectForm"
        :rules="rules"
        label-position="top"
        class="modal-form"
      >
        <div class="form-grid">
          <!-- Row 1 -->
          <el-form-item label="主体名称 *" prop="name" class="form-item">
            <el-input v-model="subjectForm.name" placeholder="租户A" />
          </el-form-item>

          <el-form-item label="主体编码 *" prop="code" class="form-item">
            <el-input v-model="subjectForm.code" :disabled="subjectDrawerMode === 'edit'" placeholder="tenant_a" />
          </el-form-item>

          <!-- Row 2 -->
          <el-form-item label="主体类型 *" prop="subjectTypeId" class="form-item">
            <el-select v-model="subjectForm.subjectTypeId" placeholder="请选择主体类型" class="w-full" popper-class="subject-type-select-popper">
              <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <div class="helper-text">由主体类型决定是否可签约应用、是否可创建下级主体。</div>
          </el-form-item>

          <el-form-item label="所属身份域 *" prop="realmId" class="form-item">
            <el-select v-model="subjectForm.realmId" placeholder="请选择所属身份域" class="w-full" popper-class="account-realm-select-popper">
              <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <div class="helper-text">主体的默认账号体系和应用签约范围来源。</div>
          </el-form-item>

          <!-- Row 3 -->
          <el-form-item label="父级主体" prop="parentSubjectId" class="form-item">
            <el-select v-model="subjectForm.parentSubjectId" placeholder="平台主体" clearable class="w-full" popper-class="subject-select-popper">
              <el-option v-for="item in availableSubjectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="状态" prop="status" class="form-item">
            <div class="switch-container">
              <el-switch
                v-model="subjectForm.status"
                :active-value="1"
                :inactive-value="2"
                active-text="启用"
                inactive-text="禁用"
                inline-prompt
                class="custom-switch"
              />
            </div>
          </el-form-item>

          <!-- Row 4: Remarks -->
          <el-form-item label="备注" prop="description" class="form-item span-full">
            <el-input
              v-model="subjectForm.description"
              type="textarea"
              :rows="4"
              placeholder="只保存主体基础信息；签约应用、客户端入口和账号角色在详情页处理。"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="modal-footer">
          <el-button class="btn-cancel" @click="isSubjectDrawerOpen = false">取消</el-button>
          <el-button type="primary" class="btn-submit" :loading="saveLoading" @click="submitSubjectForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Drawer 2: Open App Drawer (Design Drawer Layout) -->
    <el-drawer
      v-model="isOpenAppDrawerOpen"
      title="开通应用"
      size="640px"
      destroy-on-close
    >
      <div class="drawer-main-content">
        <div class="drawer-form-section">
          <h4>开通信息</h4>
          <div class="form-grid-2col">
            <el-form-item label="所属主体">
              <el-input v-model="openAppForm.subjectName" disabled />
            </el-form-item>
            <el-form-item label="应用 *">
              <el-select v-model="openAppForm.appId">
                <el-option label="商城应用" value="1" />
                <el-option label="支付应用" value="2" />
                <el-option label="仓储应用" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="实例名称 *">
              <el-input v-model="openAppForm.instanceName" />
            </el-form-item>
            <el-form-item label="实例编码 *">
              <el-input v-model="openAppForm.instanceCode" />
            </el-form-item>
            <el-form-item label="到期时间">
              <el-date-picker v-model="openAppForm.expireTime" type="date" placeholder="永不过期" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="openAppForm.status">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="2" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="drawer-form-section" style="margin-top: 20px;">
          <h4>自动初始化</h4>
          <div class="check-cards-list">
            <div class="check-card-item">
              <div class="card-left-text">
                <strong>生成实例客户端</strong>
                <small>根据应用客户端模板生成商城平台端、商家后台等入口</small>
              </div>
              <el-checkbox v-model="openAppForm.initClient" />
            </div>
            <div class="check-card-item">
              <div class="card-left-text">
                <strong>复制资源模板</strong>
                <small>生成客户端菜单、按钮、接口资源池</small>
              </div>
              <el-checkbox v-model="openAppForm.initResource" />
            </div>
            <div class="check-card-item">
              <div class="card-left-text">
                <strong>初始化默认角色</strong>
                <small>创建管理员、运营等角色，后续在客户端详情授权</small>
              </div>
              <el-checkbox v-model="openAppForm.initRole" />
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="drawer-footer-actions">
          <el-button @click="isOpenAppDrawerOpen = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="submitOpenApp">确认开通</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
/* Unified Color Palette matching Identity Domain and SaaS style */
.subject-page {
  --primary-color: #2563eb;      /* Royal Blue */
  --primary-hover: #1d4ed8;
  --secondary-color: #3b82f6;
  --success-color: #16a34a;
  --success-bg: #dcfce7;
  --success-border: #bbf7d0;
  --danger-color: #dc2626;
  --bg-color: #f8fafc;
  --text-main: #101828;
  --text-muted: #667085;
  --border-light: #eaecf0;
  
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Breadcrumbs Styling */
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
  transition: color 0.2s ease;
}

.crumb-back:hover {
  color: var(--primary-color);
}

.crumb-curr {
  color: var(--text-main);
  font-weight: 500;
}

/* Premium Hero block */
.premium-hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 30px rgba(3, 105, 161, 0.03);
}

.hero-left-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.subject-icon-box {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: var(--bg-color);
  border: 1px solid rgba(14, 165, 233, 0.2);
  color: var(--primary-color);
  font-weight: 800;
  font-size: 22px;
  font-family: var(--font-family-display);
}

.subject-meta-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-row h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.dot-separator {
  color: #cbd5e1;
}

.hero-right-actions {
  display: flex;
  gap: 10px;
}

/* Detail Tabs Custom Styling */
.detail-tabs-wrapper :deep(.el-tabs__header) {
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 10px 10px 0 0;
  padding: 0 16px;
}

.detail-tabs-wrapper :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.detail-tabs-wrapper :deep(.el-tabs__item) {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: var(--font-family-display);
  transition: color 0.2s ease;
}

.detail-tabs-wrapper :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.detail-tabs-wrapper :deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
  height: 2px;
}

/* Basic Split Grid */
.basic-grid-layout {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.pane-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.01);
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  padding-bottom: 12px;
}

.pane-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

/* Info Grid */
.info-grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  border: 1px solid rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  padding: 10px 12px;
  background: #fafcff;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  display: block;
  font-size: 11px;
  color: #64748b;
  margin-bottom: 4px;
}

.info-item span {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
}

.desc-text {
  font-weight: normal !important;
  color: var(--text-muted) !important;
  line-height: 1.5;
}

/* Note Box */
.note-box {
  background: var(--bg-color);
  border: 1px dashed rgba(14, 165, 233, 0.3);
  border-radius: 10px;
  padding: 12px 14px;
  color: var(--text-main);
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 20px;
}

/* Visual Flow Chart */
.visual-flow-container {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}

.flow-step {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 10px 6px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(3, 105, 161, 0.02);
  transition: all 0.2s ease;
}

.flow-step:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.05);
  border-color: var(--secondary-color);
}

.flow-step span {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.flow-step small {
  display: block;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-main);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #475569;
  font-size: 10px;
  font-weight: 700;
}

.bg-purple { background: #f3e8ff; color: #7c3aed; }
.bg-green { background: var(--success-bg); color: var(--success-color); }
.bg-orange { background: #ffedd5; color: #ea580c; }
.bg-blue { background: var(--bg-color); color: var(--primary-color); }

.flow-arrow {
  text-align: center;
  color: var(--secondary-color);
  font-weight: bold;
}

/* Summary List Items */
.summary-list-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  padding: 10px 14px;
  transition: all 0.2s ease;
}

.summary-list-item:hover {
  border-color: var(--secondary-color);
  background: var(--bg-color);
}

.item-left {
  display: flex;
  flex-direction: column;
}

.item-left strong {
  font-size: 14px;
  color: var(--text-main);
}

.item-left small {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.link-btn-text {
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 13px;
  margin-left: 12px;
  transition: color 0.2s ease;
}

.link-btn-text:hover {
  color: var(--primary-hover);
}

.link-btn-text.danger {
  color: var(--danger-color);
}

/* App summary cards */
.summary-app-card {
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.2s ease;
}

.summary-app-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.04);
  background: var(--bg-color);
}

.summary-app-card.full-card {
  margin-bottom: 12px;
}

.app-left h4 {
  margin: 0;
  font-size: 14px;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.app-left p {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.pills-row {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.pill-badge {
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 11px;
  color: var(--text-muted);
  transition: all 0.2s ease;
}

.pill-badge:hover {
  border-color: var(--secondary-color);
  color: var(--primary-color);
}

.app-right-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* Apps Grid */
.apps-grid-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* Tree Table */
.tree-table-shell {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
}

.tree-row {
  display: grid;
  grid-template-columns: 32px 1.5fr 1fr 1fr 120px;
  align-items: center;
  height: 48px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  font-size: 13px;
  padding: 0 16px;
  transition: background 0.2s ease;
}

.tree-row:hover {
  background: var(--bg-color);
}

.tree-row:last-child {
  border-bottom: none;
}

.tree-row.head {
  height: 38px;
  background: #fafcff;
  color: var(--text-main);
  font-weight: 700;
  font-family: var(--font-family-display);
}

.tree-row.indent {
  padding-left: 36px;
}

.toggle-icon {
  color: var(--text-muted);
  font-weight: bold;
}

.nested-text {
  color: var(--text-muted);
}

/* Badges styling */
.custom-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid transparent;
  font-family: var(--font-family-display);
}

.custom-badge.green {
  color: var(--success-color);
  background: var(--success-bg);
  border-color: var(--success-border);
}

.custom-badge.red {
  color: var(--danger-color);
  background: #fee2e2;
  border-color: #fecaca;
}

.custom-badge.blue {
  color: var(--primary-color);
  background: var(--bg-color);
  border-color: rgba(14, 165, 233, 0.2);
}

.custom-badge.orange {
  color: #c2410c;
  background: #ffedd5;
  border-color: #fed7aa;
}

.custom-badge.gray {
  color: var(--text-muted);
  background: #f1f5f9;
  border-color: #e2e8f0;
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

.heading-actions .refresh-action {
  border-radius: 6px;
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
}

/* Main Table card */
.table-card {
  border-radius: 8px;
  border: 1px solid #eaecf0;
}

.table-card :deep(.el-card__body) {
  padding: 24px;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.custom-table :deep(th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569;
}

.subject-name-link {
  font-weight: 700;
  color: var(--text-main);
  cursor: pointer;
  transition: color 0.15s;
}

.subject-name-link:hover {
  color: var(--primary-color);
}

.code-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  background: #f1f5f9;
  color: var(--text-muted);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.link-btn {
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 13px;
  transition: color 0.2s ease;
}

.link-btn:hover {
  color: var(--primary-hover);
}

.link-more-btn {
  cursor: pointer;
  color: var(--text-muted);
  font-weight: 600;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s ease;
}

.link-more-btn:hover {
  color: var(--primary-color);
}

.danger-item {
  color: var(--danger-color) !important;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.footer-note-text {
  font-size: 13px;
  color: #64748b;
}

/* Custom Table Layout Styles */
.subject-title-cell,
.realm-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.subject-code-sub,
.realm-code-sub {
  font-size: 12px;
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.realm-name-bold {
  font-weight: 600;
  color: var(--text-main);
}

.subject-type-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 999px;
  border: 1px solid transparent;
}

.subject-type-pill.pill-blue {
  background-color: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

.subject-type-pill.pill-orange {
  background-color: #fff7ed;
  border-color: #fed7aa;
  color: #c2410c;
}

.subject-type-pill.pill-indigo {
  background-color: #e0e7ff;
  border-color: #c7d2fe;
  color: #4338ca;
}

.subject-type-pill.pill-gray {
  background-color: #f8fafc;
  border-color: #e2e8f0;
  color: #475569;
}

.parent-text {
  color: #334155;
  font-weight: 500;
}

.count-text {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

/* Drawer styles */
.drawer-main-content {
  padding: 10px 0;
}

.drawer-form-section {
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 16px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.01);
}

.drawer-form-section h4 {
  margin: 0 0 14px;
  font-size: 14px;
  color: var(--text-main);
  font-weight: 700;
  border-left: 3px solid var(--primary-color);
  padding-left: 8px;
  font-family: var(--font-family-display);
}

.form-grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.full-row-item {
  grid-column: 1 / -1;
}

.dashed-note-box {
  background: var(--bg-color);
  border: 1px dashed rgba(14, 165, 233, 0.3);
  border-radius: 10px;
  padding: 12px;
  color: var(--text-main);
  font-size: 13px;
  line-height: 1.6;
}

.drawer-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
}

/* Check cards */
.check-cards-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.check-card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 12px;
  transition: all 0.2s ease;
}

.check-card-item:hover {
  border-color: var(--primary-color);
  background: var(--bg-color);
}

.card-left-text {
  display: flex;
  flex-direction: column;
}

.card-left-text strong {
  font-size: 13px;
  color: var(--text-main);
}

.card-left-text small {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

@media (max-width: 1400px) {
  .filter-flex-row {
    grid-template-columns: 1fr 1fr;
  }
  .filter-buttons {
    grid-column: span 2;
    justify-content: flex-end;
  }
}

/* App Permissions Custom Table Styles */
.custom-permissions-table {
  margin-top: 12px;
}

.roles-tags-flex {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-badge-tag {
  background: #e0f2fe !important;
  color: #0369a1 !important;
  border: 1px solid #bae6fd !important;
  border-radius: 6px !important;
  font-weight: 600;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
}

.status-pill.green-pill {
  color: var(--success-color);
  background: var(--success-bg);
}

.status-pill.gray-pill {
  color: #64748b;
  background: #f1f5f9;
}

.status-pill.orange-light {
  color: #c2410c;
  background: #ffedd5;
  border: 1px solid #fed7aa;
}

.row-actions-flex {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.action-btn-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-color);
  cursor: pointer;
  transition: color 0.15s ease;
}

.action-btn-link:hover {
  color: var(--primary-hover);
}

.action-btn-link.danger {
  color: #dc2626;
}

.action-btn-link.danger:hover {
  color: #b91c1c;
}

/* Dialog Style Customization */
.custom-subject-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin-right: 0;
  border-bottom: 1px solid #f1f5f9;
}
.custom-subject-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}
.custom-subject-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  font-size: 20px;
}
.custom-subject-dialog :deep(.el-dialog__body) {
  padding: 24px;
}
.custom-subject-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}
.modal-form {
  padding: 0;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 24px;
  row-gap: 16px;
}
.form-item {
  margin-bottom: 0;
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
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #f1f5f9;
  padding-top: 20px;
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

.switch-container {
  height: 40px;
  display: flex;
  align-items: center;
}

.custom-switch :deep(.el-switch__core) {
  height: 24px;
  border-radius: 12px;
  min-width: 60px;
}

.custom-switch :deep(.el-switch__inner) {
  padding: 0 8px;
}

/* Status Badge matching RealmListView */
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
</style>
