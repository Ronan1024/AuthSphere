<script setup lang="ts">
import { computed, ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, Edit, Plus } from '@element-plus/icons-vue'

import { type RealmOption, realmApi } from '@/api/realm'
import { type SubjectPayload, type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectMemberRecord, subjectMemberApi } from '@/api/subjectMember'
import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const route = useRoute()
const router = useRouter()
const subjectId = ref<string>(route.params.id as string)

const loading = ref(false)
const optionLoading = ref(false)
const saveLoading = ref(false)
const subject = ref<SubjectRecord>()
const editingBasic = ref(false)
const subjectFormRef = ref<FormInstance>()
const realmOptions = ref<RealmOption[]>([])
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])

// Metric counts (Mock or fetched)
const stats = reactive({
  type: '租户',
  memberCount: 6,
  appCount: 3,
  childCount: 18
})

const subjectForm = reactive<SubjectPayload>({
  subjectTypeId: '',
  realmId: '',
  code: '',
  name: '',
  rootSubjectId: null,
  parentSubjectId: null,
  isRoot: false,
  builtIn: false,
  description: '',
})

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

const availableSubjectOptions = computed(() =>
  subjectOptions.value.filter((item) => item.id !== subjectId.value),
)

// Tables Data
const membersTable = ref<SubjectMemberRecord[]>([])
const appsTable = ref([
  {
    id: '1',
    instanceName: '租户A商城',
    instanceCode: 'tenant_a_mall',
    appName: '商城应用',
    clients: '商城平台端 PC、商家后台 PC',
    scope: '按客户端资源范围',
    status: 1
  },
  {
    id: '2',
    instanceName: '租户A支付',
    instanceCode: 'tenant_a_payment',
    appName: '支付应用',
    clients: '支付平台端 PC',
    scope: '全部资源',
    status: 1
  }
])

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

const fetchSubjectDetails = async () => {
  if (!subjectId.value) return
  loading.value = true
  try {
    const data = await subjectApi.detail(subjectId.value)
    subject.value = data
    stats.type = data.subjectTypeName || '租户'
    stats.memberCount = data.memberCount ?? 0
    stats.appCount = data.appCount ?? 0
    stats.childCount = data.childCount ?? 0

    // Load members
    const membersData = await subjectMemberApi.page({
      page: 1,
      size: 50,
      subjectId: subjectId.value
    })
    membersTable.value = membersData.records ?? []
    if (data.memberCount == null) {
      stats.memberCount = membersData.total || membersTable.value.length
    }
  } catch (error: any) {
    showErrorMessage(error.message || '获取主体详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/subjects')
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
    const [realms, subjectTypes, subjects] = await Promise.all([
      loadRealmOptions(),
      loadSubjectTypeOptions(),
      loadSubjectOptions(),
    ])
    realmOptions.value = realms
    subjectTypeOptions.value = subjectTypes
    subjectOptions.value = subjects
  } catch (error: any) {
    showErrorMessage(error.message || '获取表单选项失败')
  } finally {
    optionLoading.value = false
  }
}

const handleEdit = async () => {
  if (!subject.value) return
  Object.assign(subjectForm, {
    subjectTypeId: subject.value.subjectTypeId || '',
    realmId: subject.value.realmId || '',
    code: subject.value.code || '',
    name: subject.value.name || '',
    rootSubjectId: subject.value.rootSubjectId || null,
    parentSubjectId: subject.value.parentSubjectId || null,
    isRoot: Boolean(subject.value.isRoot),
    builtIn: Boolean(subject.value.builtIn),
    description: subject.value.description || '',
  })
  editingBasic.value = true
  if (!realmOptions.value.length || !subjectTypeOptions.value.length || !subjectOptions.value.length) {
    await fetchOptions()
  }
}

const cancelEdit = () => {
  editingBasic.value = false
}

const submitSubjectForm = async () => {
  if (!subject.value || !subjectFormRef.value) return
  const valid = await subjectFormRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项')
    return
  }

  saveLoading.value = true
  try {
    await subjectApi.update(subject.value.id, {
      ...subjectForm,
      rootSubjectId: subjectForm.rootSubjectId || null,
      parentSubjectId: subjectForm.parentSubjectId || null,
      isRoot: Boolean(subjectForm.isRoot),
      builtIn: Boolean(subjectForm.builtIn),
    })
    showSuccessMessage('基础信息已更新')
    editingBasic.value = false
    await fetchSubjectDetails()
  } catch (error: any) {
    showErrorMessage(error.message || '保存基础信息失败')
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async () => {
  if (!subject.value) return
  const action = subject.value.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}主体「${subject.value.name}」吗？`, `${action}主体`, {
    confirmButtonText: action,
    cancelButtonText: '取消',
    type: 'warning'
  })

  try {
    await subjectApi.toggleStatus(subjectId.value)
    showSuccessMessage(`主体已${action}`)
    fetchSubjectDetails()
  } catch (error: any) {
    showErrorMessage(error.message || '操作失败')
  }
}

const removeMember = async (row: SubjectMemberRecord) => {
  await ElMessageBox.confirm(`确定要移除成员「${row.displayName || row.username}」吗？`, '移除成员', {
    confirmButtonText: '移除',
    cancelButtonText: '取消',
    type: 'warning'
  })

  try {
    await subjectMemberApi.remove(row.id)
    showSuccessMessage('成员已移除')
    fetchSubjectDetails()
  } catch (error: any) {
    showErrorMessage(error.message || '移除成员失败')
  }
}

onMounted(() => {
  fetchSubjectDetails()
})
</script>

<template>
  <section class="subject-detail-page" v-loading="loading">
    <!-- Breadcrumb -->
    <div class="top-breadcrumb">
      <span class="crumb-back" @click="goBack"><el-icon><ArrowLeft /></el-icon> 返回列表</span>
      <span class="divider">/</span>
      <span class="crumb-curr">主体详情 - {{ subject?.name || '...' }}</span>
    </div>

    <!-- Header area -->
    <div class="detail-header-row" v-if="subject">
      <h1>{{ subject.name }}</h1>
      <div class="header-actions">
        <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button
          :type="subject.status === 1 ? 'danger' : 'success'"
          plain
          @click="toggleStatus"
        >
          {{ subject.status === 1 ? '禁用' : '启用' }}
        </el-button>
      </div>
    </div>

    <!-- Metrics Cards Row -->
    <div class="metrics-cards-row">
      <div class="metric-card">
        <span class="metric-label">主体类型</span>
        <strong class="metric-value">{{ stats.type }}</strong>
      </div>
      <div class="metric-card">
        <span class="metric-label">成员账号</span>
        <strong class="metric-value">{{ stats.memberCount }}</strong>
      </div>
      <div class="metric-card">
        <span class="metric-label">开通应用</span>
        <strong class="metric-value">{{ stats.appCount }}</strong>
      </div>
      <div class="metric-card">
        <span class="metric-label">下级主体</span>
        <strong class="metric-value">{{ stats.childCount }}</strong>
      </div>
    </div>

    <!-- Layout Grid -> Single Card -->
    <div class="info-details-card full-width-card" v-if="subject">
      <div class="card-header-flex">
        <div class="header-titles">
          <span class="eyebrow-text">BASIC</span>
          <h3>基础信息</h3>
          <p class="subtitle-text">只展示主体自身归属和基础能力，不在这里配置客户端和角色。</p>
        </div>
        <div class="basic-actions">
          <el-button v-if="!editingBasic" type="primary" class="edit-basic-btn" @click="handleEdit">编辑基础信息</el-button>
          <template v-else>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" :loading="saveLoading" @click="submitSubjectForm">保存</el-button>
          </template>
        </div>
      </div>

      <el-form
        v-if="editingBasic"
        ref="subjectFormRef"
        v-loading="optionLoading"
        :model="subjectForm"
        :rules="rules"
        label-position="top"
      >
        <div class="edit-form-grid">
          <el-form-item label="主体名称" prop="name">
            <el-input v-model="subjectForm.name" placeholder="请输入主体名称" />
          </el-form-item>
          <el-form-item label="主体编码" prop="code">
            <el-input v-model="subjectForm.code" placeholder="请输入主体编码" />
          </el-form-item>
          <el-form-item label="主体类型" prop="subjectTypeId">
            <el-select v-model="subjectForm.subjectTypeId" placeholder="请选择主体类型" style="width: 100%">
              <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属身份域" prop="realmId">
            <el-select v-model="subjectForm.realmId" placeholder="请选择所属身份域" style="width: 100%">
              <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="父级主体">
            <el-select v-model="subjectForm.parentSubjectId" placeholder="请选择父级主体" clearable style="width: 100%">
              <el-option v-for="item in availableSubjectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据边界">
            <el-switch v-model="subjectForm.isRoot" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </div>
        <el-form-item label="描述">
          <el-input v-model="subjectForm.description" type="textarea" :rows="4" placeholder="请输入描述" />
        </el-form-item>
      </el-form>

      <div v-else class="info-grid-2col">
        <div class="info-cell">
          <label class="info-label">主体名称</label>
          <span class="info-value font-semibold">{{ subject.name }}</span>
        </div>
        <div class="info-cell">
          <label class="info-label">主体编码</label>
          <span class="info-value code-font">{{ subject.code }}</span>
        </div>
        <div class="info-cell">
          <label class="info-label">主体类型</label>
          <span class="info-value">{{ subject.subjectTypeName || '-' }}{{ subject.subjectTypeCode ? ' / ' + subject.subjectTypeCode : '' }}</span>
        </div>
        <div class="info-cell">
          <label class="info-label">所属身份域</label>
          <span class="info-value">{{ subject.realmName || '-' }}{{ subject.realmCode ? ' ' + subject.realmCode : '' }}</span>
        </div>
        <div class="info-cell">
          <label class="info-label">父级主体</label>
          <span class="info-value">{{ subject.parentSubjectName || '-' }}{{ subject.parentSubjectCode ? ' ' + subject.parentSubjectCode : '' }}</span>
        </div>
        <div class="info-cell">
          <label class="info-label">状态</label>
          <span class="info-value">
            <span class="status-pill green" v-if="subject.status === 1">
              <span class="dot"></span>启用
            </span>
            <span class="status-pill red" v-else>
              <span class="dot"></span>禁用
            </span>
          </span>
        </div>
        <div class="info-cell">
          <label class="info-label">创建时间</label>
          <span class="info-value">{{ subject.createTime ? subject.createTime.slice(0, 16).replace('T', ' ') : '-' }}</span>
        </div>
        <div class="info-cell">
          <label class="info-label">数据边界</label>
          <span class="info-value">{{ subject.isRoot ? '开启，当前主体作为边界' : '关闭' }}</span>
        </div>
        <div class="info-cell full-width-cell">
          <label class="info-label">描述</label>
          <span class="info-value">{{ subject.description || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- Section 3: 成员账号 -->
    <div class="full-section-card" id="members-section">
      <div class="section-card-header flex-header">
        <h2>成员账号</h2>
        <el-button type="primary" :icon="Plus" @click="router.push('/subjects/create')">添加成员</el-button>
      </div>
      <div class="section-card-body">
        <el-table :data="membersTable" class="detail-premium-table">
          <el-table-column prop="username" label="账号" min-width="120" />
          <el-table-column prop="memberTypeName" label="成员类型" min-width="120">
            <template #default="{ row }">
              {{ row.memberType === 2 ? '主体管理员' : '成员' }}
            </template>
          </el-table-column>
          <el-table-column label="默认主体" width="100">
            <template #default="{ row }">
              {{ row.memberType === 2 ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="status-tag green" v-if="row.memberStatus === 1">启用</span>
              <span class="status-tag red" v-else>禁用</span>
            </template>
          </el-table-column>
          <el-table-column prop="joinedAt" label="最近登录" min-width="160">
            <template #default="{ row }">
              {{ row.createTime?.slice(0, 16) || '2026-06-16 18:20' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="right">
            <template #default="{ row }">
              <div class="row-link-actions">
                <span class="link-btn-act" @click="router.push('/permission/roles')">应用角色</span>
                <span class="link-btn-act danger" @click="removeMember(row)">移除</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Section 4: 应用权限 -->
    <div class="full-section-card" id="apps-section">
      <div class="section-card-header">
        <h2>应用权限</h2>
        <p class="subtitle">主体创建完成后，在这里开通应用、生成应用实例，并维护该主体可用的客户端和资源范围。</p>
      </div>
      <div class="section-card-body">
        <el-table :data="appsTable" class="detail-premium-table">
          <el-table-column prop="instanceName" label="应用实例" min-width="150">
            <template #default="{ row }">
              <div class="instance-name-col">
                <strong>{{ row.instanceName }}</strong>
                <small class="code-font">{{ row.instanceCode }}</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appName" label="应用" min-width="120" />
          <el-table-column prop="clients" label="已启用客户端" min-width="220" />
          <el-table-column prop="scope" label="资源范围" min-width="140" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="status-tag green" v-if="row.status === 1">启用</span>
              <span class="status-tag red" v-else>禁用</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="right">
            <template #default="{ row }">
              <div class="row-link-actions">
                <span class="link-btn-act" @click="router.push(`/instances/detail/${row.id}`)">实例详情</span>
                <span class="link-btn-act" @click="router.push('/permission/roles')">资源范围</span>
                <span class="link-btn-act danger">停用</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Footer Controls -->
    <div class="detail-footer-controls">
      <el-button @click="goBack">返回列表</el-button>
      <el-button type="primary">开通应用</el-button>
    </div>

  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

.subject-detail-page {
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
  gap: 20px;
  font-family: var(--font-family-body);
}

.top-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
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

/* Header styling */
.detail-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-header-row h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* Metric count cards */
.metrics-cards-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.metric-label {
  font-size: 13px;
  color: var(--text-muted);
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

/* Details Grid -> Single Card Styles */
.info-details-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  padding-bottom: 20px;
  margin-bottom: 24px;
}

.header-titles {
  display: flex;
  flex-direction: column;
}

.eyebrow-text {
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.08em;
  margin-bottom: 6px;
}

.card-header-flex h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  font-family: var(--font-family-display);
}

.subtitle-text {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.edit-basic-btn {
  font-family: var(--font-family-display);
  font-weight: 500;
}

.basic-actions {
  display: flex;
  gap: 12px;
}

.edit-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.info-grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  row-gap: 20px;
  column-gap: 48px;
}

.info-cell {
  display: flex;
  align-items: center;
  font-size: 14px;
  padding: 4px 0;
}

.info-cell.full-width-cell {
  grid-column: span 2;
  align-items: flex-start;
}

.info-label {
  width: 140px;
  flex-shrink: 0;
  color: #64748b;
}

.info-value {
  color: #0f172a;
  font-weight: 500;
  line-height: 1.6;
}

.font-semibold {
  font-weight: 600;
}

.code-font {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill.green {
  background-color: #f0fdf4;
  color: #166534;
}

.status-pill.red {
  background-color: #fef2f2;
  color: #991b1b;
}

.status-pill .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-pill.green .dot {
  background-color: #22c55e;
}

.status-pill.red .dot {
  background-color: #ef4444;
}

/* Full Width Card Sections */
.full-section-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
  overflow: hidden;
}

.section-card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
}

.section-card-header.flex-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-card-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.section-card-header .subtitle {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: var(--text-muted);
}

.section-card-body {
  padding: 24px;
}

.detail-premium-table {
  border: 1px solid rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  overflow: hidden;
}

.status-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-tag.green {
  color: var(--success-color);
  background: var(--success-bg);
}

.status-tag.red {
  color: var(--danger-color);
  background: #fee2e2;
}

.row-link-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.link-btn-act {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-color);
  cursor: pointer;
  transition: color 0.2s ease;
}

.link-btn-act:hover {
  color: var(--primary-hover);
}

.link-btn-act.danger {
  color: var(--danger-color);
}

.instance-name-col {
  display: flex;
  flex-direction: column;
}

.instance-name-col strong {
  font-size: 14px;
  color: var(--text-main);
}

.instance-name-col small {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

.detail-footer-controls {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
  margin-bottom: 30px;
}

@media (max-width: 900px) {
  .edit-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
