<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  ArrowLeft, Edit, Delete, VideoPause, 
  Menu as IconMenu, Lock, Box, Monitor, Clock,
  DocumentCopy, TopRight,
  ElementPlus
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appApi, type AppPayload, type AppRecord } from '@/api/app'
import { appClientApi, type AppClientRecord } from '@/api/appClient'

const router = useRouter()
const route = useRoute()
const activeTab = ref('clients')
const loading = ref(true)

// Import sub components
import ClientTab from './components/ClientTab.vue'
import ApplicationFormDialog from './components/ApplicationFormDialog.vue'

// App Data
const appInfo = ref<AppRecord | null>(null)
const editDialogVisible = ref(false)
const submitLoading = ref(false)
const selectedClients = ref<AppClientRecord[]>([])

const fetchAppDetail = async () => {
  const appId = route.params.id as string
  if (!appId) return

  loading.value = true
  try {
    const data = await appApi.detail(appId)
    appInfo.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '获取应用详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/applications')
}

const handleOpenEdit = async () => {
  if (!appInfo.value) return
  submitLoading.value = true
  try {
    selectedClients.value = await appClientApi.listByApp(appInfo.value.id)
    editDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取应用客户端失败')
  } finally {
    submitLoading.value = false
  }
}

const submitEditForm = async (payload: AppPayload) => {
  if (!appInfo.value) return
  submitLoading.value = true
  try {
    await appApi.update(appInfo.value.id, payload)
    ElMessage.success('应用已更新')
    editDialogVisible.value = false
    await fetchAppDetail()
  } catch (error: any) {
    ElMessage.error(error.message || '更新应用失败')
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async () => {
  if (!appInfo.value) return
  const enabled = appInfo.value.status === 1
  const action = enabled ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}应用「${appInfo.value.appName}」？`, '提示', { type: 'warning' })
    if (enabled) {
      await appApi.disable(appInfo.value.id)
    } else {
      await appApi.enable(appInfo.value.id)
    }
    ElMessage.success(`应用已${action}`)
    fetchAppDetail()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || `${action}应用失败`)
  }
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('复制成功')
}

onMounted(() => {
  fetchAppDetail()
})
</script>

<template>
  <div class="application-detail-page animate-fade-in" v-loading="loading">
    <div class="page-top-nav">
      <el-button :icon="ArrowLeft" link @click="handleBack" class="btn-nav-back">返回应用列表</el-button>
    </div>

    <!-- Top Header Card -->
    <div class="detail-header-card" v-if="appInfo">
      <div class="header-top-section">
        <div class="header-left-info">
          <!-- App Icon -->
          <div class="app-icon-large-container">
            <div class="app-icon-large">
              <el-icon><ElementPlus /></el-icon>
            </div>
          </div>
          
          <!-- Title & Subtitle -->
          <div class="app-title-area">
            <div class="title-wrapper">
              <h1>{{ appInfo.appName }}</h1>
              <div class="status-tag-pill" :class="appInfo.status === 1 ? 'status-green' : 'status-red'">
                <span class="status-pill-dot"></span>
                {{ appInfo.status === 1 ? '已启用' : '已禁用' }}
              </div>
            </div>
            <p class="subtitle">{{ appInfo.description || '暂无该应用的详细描述信息。' }}</p>
          </div>
        </div>

        <!-- Actions -->
        <div class="header-actions">
          <el-button type="primary" :icon="Edit" @click="handleOpenEdit" class="btn-action-edit">编辑</el-button>
          <el-button :icon="VideoPause" plain @click="toggleStatus" class="btn-action-status">
            {{ appInfo.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </div>
      </div>

      <!-- Meta Info Grid -->
      <div class="meta-info-panel">
        <div class="meta-item">
          <span class="meta-label">应用唯一编码</span>
          <div class="meta-value copyable" @click="copyToClipboard(appInfo.appCode)">
            <span class="value-text">{{ appInfo.appCode }}</span>
            <el-icon class="copy-icon"><DocumentCopy /></el-icon>
          </div>
        </div>
        <div class="meta-item">
          <span class="meta-label">默认入口地址</span>
          <div class="meta-value">
            <a :href="appInfo.entryUrl || '#'" target="_blank" class="entry-link">{{ appInfo.entryUrl || '-' }}</a>
            <el-icon class="link-icon" v-if="appInfo.entryUrl"><TopRight /></el-icon>
          </div>
        </div>
        <div class="meta-item">
          <span class="meta-label">创建时间</span>
          <div class="meta-value-plain">{{ appInfo.createTime }}</div>
        </div>
        <div class="meta-item">
          <span class="meta-label">更新时间</span>
          <div class="meta-value-plain">{{ appInfo.updateTime }}</div>
        </div>
      </div>

      <!-- Tabs Navigation -->
      <div class="header-tabs-row">
        <el-tabs v-model="activeTab" class="custom-icon-tabs">
          <el-tab-pane name="clients">
            <template #label>
              <div class="tab-label-content">
                <el-icon><Monitor /></el-icon>
                <span>应用客户端</span>
                <span class="badge" :class="activeTab === 'clients' ? 'badge-active' : 'badge-gray'" v-if="appInfo.clientSize !== undefined">
                  {{ appInfo.clientSize }}
                </span>
              </div>
            </template>
          </el-tab-pane>

          <el-tab-pane name="instances">
            <template #label>
              <div class="tab-label-content">
                <el-icon><Box /></el-icon>
                <span>应用实例</span>
                <span class="badge" :class="activeTab === 'instances' ? 'badge-active' : 'badge-gray'" v-if="appInfo.instanceSize !== undefined">
                  {{ appInfo.instanceSize }}
                </span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="logs">
            <template #label>
              <div class="tab-label-content">
                <el-icon><Clock /></el-icon>
                <span>操作日志</span>
              </div>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Main Content Tab Panes -->
    <div class="tab-content-wrapper">
      <div v-if="activeTab === 'clients'" class="tab-pane-content">
        <ClientTab />
      </div>

      <div v-else class="placeholder-content">
        <el-empty description="该模块内容正在火热开发中..." :image-size="100" />
      </div>
    </div>

    <ApplicationFormDialog
      v-model:visible="editDialogVisible"
      mode="edit"
      :loading="submitLoading"
      :initial-app="appInfo"
      :initial-clients="selectedClients"
      @submit="submitEditForm"
    />
  </div>
</template>

<style scoped>
.application-detail-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.page-top-nav {
  margin-bottom: 16px;
}

.btn-nav-back {
  font-weight: 550;
  color: #64748b;
  font-size: 13.5px;
}

.btn-nav-back:hover {
  color: #1e293b;
}

/* Header Card */
.detail-header-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  padding: 32px 32px 0 32px;
  margin-bottom: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.02), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
}

.header-top-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.header-left-info {
  display: flex;
  align-items: center;
}

.app-icon-large-container {
  padding: 2px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  border-radius: 16px;
  margin-right: 24px;
  flex-shrink: 0;
}

.app-icon-large {
  width: 76px;
  height: 76px;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.app-title-area {
  max-width: 480px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-wrapper h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.status-tag-pill {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-pill-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-green {
  background-color: #d1fae5;
  color: #065f46;
}
.status-green .status-pill-dot {
  background-color: #10b981;
}

.status-red {
  background-color: #fee2e2;
  color: #991b1b;
}
.status-red .status-pill-dot {
  background-color: #ef4444;
}

.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 13.5px;
  line-height: 1.5;
}

.meta-info-panel {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding: 20px 24px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 28px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-label {
  font-size: 12.5px;
  color: #94a3b8;
  font-weight: 500;
}

.meta-value {
  font-size: 14.5px;
  color: #1e293b;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.meta-value-plain {
  font-size: 13.5px;
  color: #475569;
  font-weight: 500;
}

.entry-link {
  color: #6366f1;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.15s;
}

.entry-link:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.copy-icon {
  color: #94a3b8;
  font-size: 13px;
  transition: color 0.2s;
}

.copyable:hover .copy-icon {
  color: #6366f1;
}

.link-icon {
  color: #6366f1;
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 12px;
  margin-left: auto;
  align-items: center;
}

.btn-action-edit {
  font-weight: 600;
  border-radius: 8px;
}

.btn-action-status {
  font-weight: 600;
  border-radius: 8px;
}

/* Tabs Styling */
.header-tabs-row {
  margin-top: auto;
}

.custom-icon-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-icon-tabs :deep(.el-tabs__active-bar) {
  background-color: #6366f1;
  height: 3px;
  border-radius: 99px;
}

.custom-icon-tabs :deep(.el-tabs__item) {
  font-size: 14.5px;
  color: #64748b;
  padding: 0 24px;
  height: 58px;
  line-height: 58px;
  font-weight: 550;
  transition: all 0.2s;
}

.custom-icon-tabs :deep(.el-tabs__item:hover) {
  color: #1e293b;
}

.custom-icon-tabs :deep(.el-tabs__item.is-active) {
  color: #6366f1;
  font-weight: 700;
}

.tab-label-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-label-content .el-icon {
  font-size: 18px;
}

.badge {
  display: inline-block;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  border-radius: 99px;
  font-size: 11px;
  font-weight: 600;
}

.badge-gray {
  background-color: #f1f5f9;
  color: #64748b;
}

.badge-active {
  background-color: #eeebff;
  color: #6366f1;
}

.tab-content-wrapper {
  margin-top: 12px;
}

.placeholder-content {
  background: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 60px;
}
</style>
