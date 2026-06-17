<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, VideoPause, Plus } from '@element-plus/icons-vue'

import { type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectMemberRecord, subjectMemberApi } from '@/api/subjectMember'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const route = useRoute()
const router = useRouter()
const subjectId = ref<string>(route.params.id as string)

const loading = ref(false)
const subject = ref<SubjectRecord>()

// Metric counts (Mock or fetched)
const stats = reactive({
  type: '租户',
  memberCount: 6,
  appCount: 3,
  childCount: 18
})

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

const fetchSubjectDetails = async () => {
  if (!subjectId.value) return
  loading.value = true
  try {
    const data = await subjectApi.detail(subjectId.value)
    subject.value = data
    stats.type = data.subjectTypeName || '租户'

    // Load members
    const membersData = await subjectMemberApi.page({
      page: 1,
      size: 50,
      subjectId: subjectId.value
    })
    membersTable.value = membersData.records ?? []
    stats.memberCount = membersData.total || membersTable.value.length
  } catch (error: any) {
    showErrorMessage(error.message || '获取主体详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/subjects')
}

const handleEdit = () => {
  // Simple edit router push or mock edit
  router.push(`/subjects?editId=${subjectId.value}`)
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
    type: 'danger'
  })

  try {
    await subjectMemberApi.remove(row.id)
    showSuccessMessage('成员已移除')
    fetchSubjectDetails()
  } catch (error: any) {
    showErrorMessage(error.message || '移除成员失败')
  }
}

const scrollToSection = (id: string) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
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

    <!-- Layout Grid -->
    <div class="details-grid-row" v-if="subject">
      <!-- Card Left: 基础信息 -->
      <div class="info-details-card">
        <h3>基础信息</h3>
        <div class="info-list-rows">
          <div class="info-list-row">
            <label>主体编码</label>
            <span class="code-font">{{ subject.code }}</span>
          </div>
          <div class="info-list-row">
            <label>所属身份域</label>
            <span>{{ subject.realmName || subject.realmCode || '租户身份域' }}</span>
          </div>
          <div class="info-list-row">
            <label>父级主体</label>
            <span>{{ subject.parentSubjectName || '-' }}</span>
          </div>
          <div class="info-list-row">
            <label>主体路径</label>
            <span class="code-font">/{{ subject.parentSubjectCode || 'platform' }}/{{ subject.code }}</span>
          </div>
          <div class="info-list-row status-highlight-row">
            <label>状态</label>
            <span class="status-badge" :class="subject.status === 1 ? 'enabled' : 'disabled'">
              {{ subject.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Card Right: 配置入口 -->
      <div class="info-details-card">
        <h3>配置入口</h3>
        <div class="entry-points-list">
          <div class="entry-point-item">
            <div class="entry-meta">
              <strong>成员账号</strong>
              <p>添加账号、设置管理员、移除成员</p>
            </div>
            <span class="entry-link" @click="scrollToSection('members-section')">去维护</span>
          </div>

          <div class="entry-point-item">
            <div class="entry-meta">
              <strong>应用权限</strong>
              <p>在主体详情中开通应用和维护客户端</p>
            </div>
            <span class="entry-link" @click="scrollToSection('apps-section')">去维护</span>
          </div>

          <div class="entry-point-item">
            <div class="entry-meta">
              <strong>角色授权</strong>
              <p>按应用实例和客户端分配角色</p>
            </div>
            <span class="entry-link" @click="router.push('/permission/roles')">去授权</span>
          </div>
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

/* Details Grid */
.details-grid-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-details-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.info-details-card h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.info-list-rows {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-list-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.info-list-row label {
  color: var(--text-muted);
}

.info-list-row span {
  font-weight: 600;
  color: var(--text-main);
}

.code-font {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.status-highlight-row {
  background: #f0fdf4;
  border-radius: 8px;
  padding: 8px 12px;
  margin-top: 6px;
}

.status-highlight-row label {
  color: #166534;
  font-weight: 600;
}

.status-badge {
  font-weight: 700 !important;
}

.status-badge.enabled {
  color: var(--success-color);
}

.status-badge.disabled {
  color: var(--danger-color);
}

/* Configuration entries style */
.entry-points-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.entry-point-item {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
}

.entry-point-item:hover {
  border-color: var(--secondary-color);
  background: var(--bg-color);
}

.entry-meta strong {
  display: block;
  font-size: 14px;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.entry-meta p {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: var(--text-muted);
}

.entry-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-color);
  cursor: pointer;
  transition: color 0.2s ease;
}

.entry-link:hover {
  color: var(--primary-hover);
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
</style>
