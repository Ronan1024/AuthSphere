<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, Edit, Delete, VideoPause, 
  InfoFilled, Menu as IconMenu, Lock, Box, Monitor, Clock,
  DocumentCopy, TopRight,
  EditPen, Setting, ElementPlus
} from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('permissions')

// Import sub components
import PermissionTab from './components/PermissionTab.vue'
import MenuTab from './components/MenuTab.vue'
import ClientTab from './components/ClientTab.vue'

// Mock App Data
const appInfo = {
  id: '1',
  name: '统一门户系统',
  description: '企业统一门户与集成平台',
  code: 'unified-portal',
  url: 'https://portal.example.com',
  status: 1,
  isBuiltIn: false,
  sort: 100,
  creator: '张管理员',
  createTime: '2024-05-20 10:30:00',
  updateTime: '2024-05-20 16:45:00',
  remark: '-'
}

const handleBack = () => {
  router.push('/applications')
}
</script>

<template>
  <div class="application-detail-page">
    <!-- Top Header Card -->
    <div class="detail-header-card">
      <div class="header-main-row">
        <!-- App Icon -->
        <div class="app-icon-large bg-blue">
          <el-icon><ElementPlus /></el-icon>
        </div>
        
        <!-- Title & Subtitle -->
        <div class="app-title-area">
          <div class="title-wrapper">
            <h1>{{ appInfo.name }}</h1>
            <div class="status-tag status-green">启用</div>
          </div>
          <p class="subtitle">{{ appInfo.description }}</p>
        </div>

        <!-- Meta Info Grid -->
        <div class="meta-info-grid">
          <div class="meta-item">
            <span class="meta-label">应用编码</span>
            <div class="meta-value">
              {{ appInfo.code }}
              <el-icon class="copy-icon"><DocumentCopy /></el-icon>
            </div>
          </div>
          <div class="meta-item">
            <span class="meta-label">默认入口地址</span>
            <div class="meta-value">
              <a :href="appInfo.url" target="_blank">{{ appInfo.url }}</a>
              <el-icon class="link-icon"><TopRight /></el-icon>
            </div>
          </div>
          <div class="meta-item">
            <span class="meta-label">创建时间</span>
            <div class="meta-value">{{ appInfo.createTime }}</div>
          </div>
          <div class="meta-item">
            <span class="meta-label">更新时间</span>
            <div class="meta-value">{{ appInfo.updateTime }}</div>
          </div>
        </div>

        <!-- Actions -->
        <div class="header-actions">
          <el-button :icon="Edit" plain>编辑</el-button>
          <el-button :icon="VideoPause" plain>禁用</el-button>
          <el-button type="danger" :icon="Delete" plain>删除</el-button>
        </div>
      </div>

      <!-- Tabs Navigation -->
      <div class="header-tabs-row">
        <el-tabs v-model="activeTab" class="custom-icon-tabs">
          <el-tab-pane name="basic">
            <template #label>
              <div class="tab-label-content">
                <el-icon><InfoFilled /></el-icon>
                <span>基本信息</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="menus">
            <template #label>
              <div class="tab-label-content">
                <el-icon><IconMenu /></el-icon>
                <span>菜单资源</span>
                <span class="badge badge-gray">18</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="permissions">
            <template #label>
              <div class="tab-label-content">
                <el-icon><Lock /></el-icon>
                <span>权限资源</span>
                <span class="badge badge-gray">36</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="instances">
            <template #label>
              <div class="tab-label-content">
                <el-icon><Box /></el-icon>
                <span>应用实例</span>
                <span class="badge badge-gray">8</span>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane name="clients">
            <template #label>
              <div class="tab-label-content">
                <el-icon><Monitor /></el-icon>
                <span>应用客户端</span>
                <span class="badge badge-gray">6</span>
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
      <div v-show="activeTab === 'basic'" class="basic-info-layout">
        
        <!-- Top Half: Basic Info Fields -->
        <el-card shadow="never" class="info-card">
          <h3 class="card-title">基本信息</h3>
          <div class="info-grid">
            <!-- Left Column -->
            <div class="info-column">
              <div class="info-row">
                <span class="label">应用名称</span>
                <span class="value">{{ appInfo.name }}</span>
              </div>
              <div class="info-row">
                <span class="label">应用编码</span>
                <span class="value">{{ appInfo.code }}</span>
              </div>
              <div class="info-row">
                <span class="label">默认入口地址</span>
                <span class="value link-value">
                  <a :href="appInfo.url" target="_blank">{{ appInfo.url }}</a>
                  <el-icon class="link-icon"><TopRight /></el-icon>
                </span>
              </div>
              <div class="info-row align-center">
                <span class="label">应用图标</span>
                <span class="value">
                  <div class="small-app-icon bg-blue"><el-icon><ElementPlus /></el-icon></div>
                </span>
              </div>
              <div class="info-row">
                <span class="label">应用描述</span>
                <span class="value">提供统一门户、消息中心、应用集成、单点登录等基础能力。</span>
              </div>
            </div>
            <!-- Right Column -->
            <div class="info-column">
              <div class="info-row">
                <span class="label">应用状态</span>
                <span class="value text-green"><span class="dot"></span> 启用</span>
              </div>
              <div class="info-row">
                <span class="label">是否内置</span>
                <span class="value">{{ appInfo.isBuiltIn ? '是' : '否' }}</span>
              </div>
              <div class="info-row">
                <span class="label">排序</span>
                <span class="value">{{ appInfo.sort }}</span>
              </div>
              <div class="info-row">
                <span class="label">创建人</span>
                <span class="value">{{ appInfo.creator }}</span>
              </div>
              <div class="info-row">
                <span class="label">备注</span>
                <span class="value">{{ appInfo.remark }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Bottom Half: Quick Actions & Stats -->
        <div class="bottom-layout">
          <!-- Quick Actions -->
          <el-card shadow="never" class="quick-action-card">
            <h3 class="card-title">快速操作</h3>
            <div class="action-grid">
              <div class="action-item">
                <div class="action-icon bg-light-blue"><el-icon><EditPen /></el-icon></div>
                <div class="action-text">
                  <h4>编辑应用</h4>
                  <p>修改应用基本信息</p>
                </div>
              </div>
              <div class="action-item">
                <div class="action-icon bg-light-purple"><el-icon><IconMenu /></el-icon></div>
                <div class="action-text">
                  <h4>菜单管理</h4>
                  <p>管理应用菜单资源</p>
                </div>
              </div>
              <div class="action-item">
                <div class="action-icon bg-light-blue"><el-icon><Lock /></el-icon></div>
                <div class="action-text">
                  <h4>权限管理</h4>
                  <p>管理应用权限资源</p>
                </div>
              </div>
              <div class="action-item">
                <div class="action-icon bg-light-purple"><el-icon><Box /></el-icon></div>
                <div class="action-text">
                  <h4>实例管理</h4>
                  <p>查看应用实例列表</p>
                </div>
              </div>
              <div class="action-item">
                <div class="action-icon bg-light-blue"><el-icon><Monitor /></el-icon></div>
                <div class="action-text">
                  <h4>客户端管理</h4>
                  <p>查看应用客户端列表</p>
                </div>
              </div>
              <div class="action-item">
                <div class="action-icon bg-light-purple"><el-icon><Clock /></el-icon></div>
                <div class="action-text">
                  <h4>操作日志</h4>
                  <p>查看操作历史记录</p>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Statistics -->
          <el-card shadow="never" class="stats-card">
            <h3 class="card-title">统计信息</h3>
            <div class="stats-list">
              <div class="stat-row">
                <span class="stat-label">应用实例数量</span>
                <span class="stat-value">8</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">应用客户端数量</span>
                <span class="stat-value">6</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">菜单资源数量</span>
                <span class="stat-value">18</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">权限资源数量</span>
                <span class="stat-value">36</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">最近30天操作次数</span>
                <span class="stat-value">42</span>
              </div>
            </div>
          </el-card>
        </div>

      </div>

      <div v-show="activeTab === 'permissions'" class="tab-pane-content">
        <PermissionTab />
      </div>

      <div v-show="activeTab === 'menus'" class="tab-pane-content">
        <MenuTab />
      </div>

      <div v-show="activeTab === 'clients'" class="tab-pane-content">
        <ClientTab />
      </div>

      <div v-show="activeTab !== 'basic' && activeTab !== 'permissions' && activeTab !== 'menus' && activeTab !== 'clients'" class="placeholder-content">
        <el-empty description="该模块内容正在火热开发中..." />
      </div>
    </div>
  </div>
</template>

<style scoped>
.application-detail-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

/* Header Card */
.detail-header-card {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #eaeaea;
  padding: 32px 32px 0 32px;
  margin-bottom: 24px;
}

.header-main-row {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.app-icon-large {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
  flex-shrink: 0;
  margin-right: 24px;
}

.bg-blue { background-color: #2563eb; }

.app-title-area {
  flex-shrink: 0;
  margin-right: 48px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-wrapper h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-green {
  background-color: #dcfce7;
  color: #16a34a;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.meta-info-grid {
  display: flex;
  flex-grow: 1;
  gap: 40px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-label {
  font-size: 13px;
  color: #9ca3af;
}

.meta-value {
  font-size: 14px;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-value a {
  color: #111827;
  text-decoration: none;
}

.copy-icon, .link-icon {
  color: #9ca3af;
  cursor: pointer;
}

.header-actions {
  display: flex;
  gap: 12px;
  margin-left: 40px;
}

/* Tabs */
.header-tabs-row {
  margin-top: auto;
}

.custom-icon-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-icon-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  color: #4b5563;
  padding: 0 24px;
  height: 56px;
  line-height: 56px;
}

.custom-icon-tabs :deep(.el-tabs__item.is-active) {
  color: #2563eb;
  font-weight: 600;
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
  padding: 0 8px;
  height: 20px;
  line-height: 20px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.badge-gray {
  background-color: #f3f4f6;
  color: #6b7280;
}

.custom-icon-tabs :deep(.el-tabs__item.is-active) .badge-gray {
  background-color: #eff6ff;
  color: #2563eb;
}

/* Main Content */
.card-title {
  margin: 0 0 24px 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.info-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
}

.info-grid {
  display: flex;
  gap: 80px;
}

.info-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-row.align-center {
  align-items: center;
}

.info-row .label {
  width: 120px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-row .value {
  color: #111827;
  flex-grow: 1;
}

.link-value a {
  color: #2563eb;
  text-decoration: none;
  margin-right: 6px;
}

.link-value .link-icon {
  color: #2563eb;
}

.small-app-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.text-green { color: #16a34a; }
.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
  margin-right: 4px;
}

/* Bottom Layout */
.bottom-layout {
  display: flex;
  gap: 24px;
}

.quick-action-card {
  flex: 2;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 32px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 8px;
  background-color: #f9fafb;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-item:hover {
  background-color: #f3f4f6;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.bg-light-blue { background-color: #eff6ff; color: #2563eb; }
.bg-light-purple { background-color: #f5f3ff; color: #7c3aed; }

.action-text h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 500;
  color: #111827;
}

.action-text p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.stats-card {
  flex: 1;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 32px;
}

.stats-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #f3f4f6;
}

.stat-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #111827;
}

.placeholder-content {
  background: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 60px;
}
</style>
