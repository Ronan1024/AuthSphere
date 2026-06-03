<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  EditPen, SwitchButton, Back, 
  Select, User, Box, Monitor,
  Document, Menu as IconMenu, Lock, ArrowDown,
  CopyDocument, Clock, 
  Right, List, InfoFilled, Connection, TrendCharts
} from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('basic')

const handleBack = () => {
  router.push('/applications/instances')
}

// Table Data for Client Preview
const clientData = [
  {
    name: '商城后台管理系统',
    clientId: 'mall-admin-web',
    type: 'Web 应用',
    domain: '企业AAA',
    status: 1
  },
  {
    name: '商城移动端 APP',
    clientId: 'mall-mobile-app',
    type: '移动应用',
    domain: '企业AAA',
    status: 1
  },
  {
    name: '商城开放 API 服务',
    clientId: 'mall-open-api',
    type: 'API 服务',
    domain: '企业AAA',
    status: 1
  }
]
</script>

<template>
  <div class="app-container">
    <!-- Header Section -->
    <div class="header-section">
      <div class="header-top">
        <div class="header-title">
          <h2>企业AAA商城实例</h2>
          <p class="subtitle">应用实例的详细信息页，提供实例的基本信息、关系预览及能力概览等内容。</p>
        </div>
        <div class="header-actions">
          <el-button plain :icon="EditPen">编辑实例</el-button>
          <el-dropdown trigger="click">
            <el-button plain class="btn-dropdown">
              <el-icon class="mr-1"><SwitchButton /></el-icon> 启用/禁用 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>禁用实例</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button plain :icon="Back" @click="handleBack">返回列表</el-button>
        </div>
      </div>

      <!-- Top Stats Cards -->
      <div class="stats-cards-grid">
        <!-- Status Card -->
        <div class="stat-card">
          <div class="stat-icon-wrapper bg-green-50 text-green-500">
            <el-icon><Select /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">实例状态</div>
            <div class="stat-value text-green-600">启用</div>
            <div class="stat-sub-info">
              <span class="status-dot green-dot"></span> 运行中
            </div>
          </div>
        </div>

        <!-- Subject Card -->
        <div class="stat-card">
          <div class="stat-icon-wrapper bg-blue-50 text-blue-500">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">所属主体</div>
            <div class="stat-value text-blue-600">企业AAA</div>
            <div class="stat-sub-info text-gray-500">主体编码: ent_aaa</div>
          </div>
        </div>

        <!-- App Card -->
        <div class="stat-card">
          <div class="stat-icon-wrapper bg-purple-50 text-purple-500">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">所属应用</div>
            <div class="stat-value text-purple-600">商城应用</div>
            <div class="stat-sub-info text-gray-500">应用编码: mall_app</div>
          </div>
        </div>

        <!-- Client Stats Card -->
        <div class="stat-card">
          <div class="stat-icon-wrapper bg-orange-50 text-orange-500">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">客户端数量</div>
            <div class="stat-value text-orange-500">3</div>
            <div class="stat-sub-info text-gray-500">已启用客户端数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Tabs -->
    <div class="main-content-section">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="basic">
          <template #label>
            <div class="tab-label-content">
              <el-icon><Document /></el-icon> 基本信息
            </div>
          </template>
          
          <div class="tab-pane-content dual-layout">
            <!-- Left Column -->
            <div class="left-column">
              <!-- Basic Info Panel -->
              <div class="panel-card">
                <div class="panel-header">
                  <div class="panel-title-with-line">
                    <span class="vertical-line"></span> 基本信息
                  </div>
                </div>
                <div class="panel-body">
                  <el-descriptions :column="2" border class="custom-descriptions">
                    <el-descriptions-item label="实例名称:">企业AAA商城实例</el-descriptions-item>
                    <el-descriptions-item label="实例编码:">ent_aaa_mall</el-descriptions-item>
                    <el-descriptions-item label="所属主体:">企业AAA</el-descriptions-item>
                    <el-descriptions-item label="所属应用:">商城应用</el-descriptions-item>
                    <el-descriptions-item label="数据隔离根:">企业AAA</el-descriptions-item>
                    <el-descriptions-item label="开通方式:">平台开通</el-descriptions-item>
                    <el-descriptions-item label="默认访问入口:">
                      <a href="#" class="link-with-icon">https://mall.ent-aaa.com <el-icon><TopRight /></el-icon></a>
                    </el-descriptions-item>
                    <el-descriptions-item label="状态:">
                      <span class="status-tag status-green">启用</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间:">2026-06-01 10:20:30</el-descriptions-item>
                    <el-descriptions-item label="更新时间:">2026-06-03 15:48:12</el-descriptions-item>
                    <el-descriptions-item label="描述:" :span="2">企业AAA商城业务后台应用实例</el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>

              <!-- Default Clients Preview Panel -->
              <div class="panel-card mt-6">
                <div class="panel-header">
                  <div class="panel-title-with-line">
                    <span class="vertical-line"></span> 默认客户端预览
                  </div>
                </div>
                <div class="panel-body">
                  <el-table :data="clientData" style="width: 100%" class="inner-table">
                    <el-table-column prop="name" label="客户端名称" min-width="160" />
                    <el-table-column prop="clientId" label="client_id" min-width="160" />
                    <el-table-column prop="type" label="客户端类型" min-width="120" />
                    <el-table-column prop="domain" label="默认身份域" min-width="120" />
                    <el-table-column label="状态" width="100">
                      <template #default="{ row }">
                        <div class="status-cell text-green" v-if="row.status === 1">
                          <span class="dot"></span> 启用
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="inner-pagination-wrapper">
                    <span class="total-text">共 3 条</span>
                    <el-pagination
                      background
                      layout="prev, pager, next, sizes"
                      :total="3"
                      :page-sizes="[10, 20]"
                      :default-page-size="10"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Right Column -->
            <div class="right-column">
              <!-- Relationship Preview -->
              <div class="panel-card">
                <div class="panel-header">
                  <div class="panel-title-with-icon">
                    <el-icon class="text-blue-500"><Connection /></el-icon> 关系预览
                  </div>
                </div>
                <div class="panel-body">
                  <div class="horizontal-flow">
                    <div class="flow-node">
                      <div class="node-icon bg-blue-50 text-blue-500"><el-icon><User /></el-icon></div>
                      <div class="node-label">主体</div>
                      <div class="node-value">企业AAA</div>
                    </div>
                    <div class="flow-arrow"><el-icon><Right /></el-icon></div>

                    <div class="flow-node">
                      <div class="node-icon bg-purple-50 text-purple-500"><el-icon><Box /></el-icon></div>
                      <div class="node-label">应用</div>
                      <div class="node-value">商城应用</div>
                    </div>
                    <div class="flow-arrow"><el-icon><Right /></el-icon></div>

                    <div class="flow-node">
                      <div class="node-icon bg-green-50 text-green-500"><el-icon><CopyDocument /></el-icon></div>
                      <div class="node-label">应用实例</div>
                      <div class="node-value">企业AAA商城实例</div>
                    </div>
                    <div class="flow-arrow"><el-icon><Right /></el-icon></div>

                    <div class="flow-node">
                      <div class="node-icon bg-orange-50 text-orange-500"><el-icon><Monitor /></el-icon></div>
                      <div class="node-label">默认客户端</div>
                      <div class="node-value">3 个客户端</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Capabilities Stats -->
              <div class="panel-card mt-6">
                <div class="panel-header">
                  <div class="panel-title-with-icon">
                    <el-icon class="text-blue-500"><TrendCharts /></el-icon> 当前能力概览
                  </div>
                </div>
                <div class="panel-body">
                  <div class="capability-grid">
                    <div class="cap-card bg-blue-50">
                      <div class="cap-label text-blue-600">已启用菜单</div>
                      <div class="cap-value-row text-blue-600">
                        <span class="cap-num">24</span>
                        <el-icon class="cap-icon"><List /></el-icon>
                      </div>
                    </div>
                    <div class="cap-card bg-green-50">
                      <div class="cap-label text-green-600">已启用权限</div>
                      <div class="cap-value-row text-green-600">
                        <span class="cap-num">86</span>
                        <el-icon class="cap-icon"><Lock /></el-icon>
                      </div>
                    </div>
                    <div class="cap-card bg-purple-50">
                      <div class="cap-label text-purple-600">客户端</div>
                      <div class="cap-value-row text-purple-600">
                        <span class="cap-num">3</span>
                        <el-icon class="cap-icon"><Monitor /></el-icon>
                      </div>
                    </div>
                    <div class="cap-card bg-orange-50">
                      <div class="cap-label text-orange-600">已分配角色</div>
                      <div class="cap-value-row text-orange-600">
                        <span class="cap-num">8</span>
                        <el-icon class="cap-icon"><User /></el-icon>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Info Panel -->
              <div class="panel-card mt-6 bg-gray-50 border-none">
                <div class="panel-header">
                  <div class="panel-title-with-icon text-gray-700">
                    <el-icon class="text-blue-500"><InfoFilled /></el-icon> 说明
                  </div>
                </div>
                <div class="panel-body">
                  <ul class="info-list">
                    <li>本页用于查看和维护当前应用实例的详细信息及其关联关系。</li>
                    <li>菜单资源的管理请前往【菜单资源】标签页进行配置。</li>
                    <li>权限资源的管理请前往【权限资源】标签页进行配置。</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="menus">
          <template #label><div class="tab-label-content"><el-icon><IconMenu /></el-icon> 菜单资源</div></template>
          <div class="placeholder-content"><el-empty description="当前面板内容正在开发中..." /></div>
        </el-tab-pane>
        
        <el-tab-pane name="permissions">
          <template #label><div class="tab-label-content"><el-icon><Lock /></el-icon> 权限资源</div></template>
          <div class="placeholder-content"><el-empty description="当前面板内容正在开发中..." /></div>
        </el-tab-pane>

        <el-tab-pane name="clients">
          <template #label><div class="tab-label-content"><el-icon><Monitor /></el-icon> 应用客户端</div></template>
          <div class="placeholder-content"><el-empty description="当前面板内容正在开发中..." /></div>
        </el-tab-pane>

        <el-tab-pane name="logs">
          <template #label><div class="tab-label-content"><el-icon><Clock /></el-icon> 操作日志</div></template>
          <div class="placeholder-content"><el-empty description="当前面板内容正在开发中..." /></div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.app-container {
  padding: 24px;
  background-color: #f1f5f9;
  min-height: calc(100vh - 60px);
}

/* Header Section */
.header-section {
  margin-bottom: 24px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-title h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-dropdown {
  padding: 8px 15px;
}

.mr-1 { margin-right: 4px; }

/* Stats Cards Grid */
.stats-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background-color: #fff;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
  border: 4px solid #fff;
  box-shadow: 0 0 0 1px rgba(0,0,0,0.05);
}

.bg-green-50 { background-color: #f0fdf4; }
.text-green-500 { color: #22c55e; }
.text-green-600 { color: #16a34a; }

.bg-blue-50 { background-color: #eff6ff; }
.text-blue-500 { color: #3b82f6; }
.text-blue-600 { color: #2563eb; }

.bg-purple-50 { background-color: #faf5ff; }
.text-purple-500 { color: #a855f7; }
.text-purple-600 { color: #9333ea; }

.bg-orange-50 { background-color: #fff7ed; }
.text-orange-500 { color: #f97316; }
.text-orange-600 { color: #ea580c; }

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
}

.stat-sub-info {
  font-size: 13px;
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.text-gray-500 { color: #6b7280; }

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}
.green-dot { background-color: #22c55e; }

/* Main Content Tabs */
.main-content-section {
  background-color: transparent;
}

.custom-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  background-color: transparent;
  border-bottom: 1px solid #e2e8f0;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

.tab-label-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-pane-content {
  padding-top: 24px;
}

.placeholder-content {
  padding: 60px 0;
  background-color: #fff;
  border-radius: 12px;
  margin-top: 24px;
}

/* Dual Layout */
.dual-layout {
  display: flex;
  gap: 24px;
}

.left-column {
  flex: 5;
  min-width: 0; /* prevent flex overflow */
}

.right-column {
  flex: 3;
  min-width: 0;
}

/* Panel Card */
.panel-card {
  background-color: #fff;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.mt-6 { margin-top: 24px; }
.bg-gray-50 { background-color: #f8fafc; }
.border-none { border: none; box-shadow: none; }

.panel-header {
  margin-bottom: 20px;
}

.panel-title-with-line {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.vertical-line {
  width: 4px;
  height: 16px;
  background-color: #3b82f6;
  border-radius: 2px;
  margin-right: 10px;
}

.panel-title-with-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.text-gray-700 { color: #374151; }

/* Custom Descriptions */
.custom-descriptions :deep(.el-descriptions__table) {
  border: none;
}

.custom-descriptions :deep(.el-descriptions__cell) {
  border: none;
  padding: 12px 16px;
}

.custom-descriptions :deep(.el-descriptions__label) {
  width: 140px;
  background-color: transparent;
  color: #6b7280;
  font-weight: 400;
  justify-content: flex-end;
}

.custom-descriptions :deep(.el-descriptions__content) {
  color: #111827;
  font-weight: 500;
}

.link-with-icon {
  color: #3b82f6;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.link-with-icon:hover {
  text-decoration: underline;
}

.status-tag {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  border: 1px solid currentColor;
}
.status-green { color: #16a34a; background-color: #f0fdf4; border-color: #bbf7d0;}

/* Inner Table */
.inner-table {
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  overflow: hidden;
}

.inner-table :deep(th.el-table__cell) {
  background-color: #f8fafc;
  color: #4b5563;
  font-weight: 500;
  border-bottom: 1px solid #f1f5f9;
}

.inner-table :deep(td.el-table__cell) {
  border-bottom: 1px solid #f1f5f9;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.status-cell .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
}
.text-green { color: #16a34a; }

.inner-pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}

/* Horizontal Flow Preview */
.horizontal-flow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100px;
}

.node-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.node-label {
  font-size: 13px;
  color: #6b7280;
}

.node-value {
  font-size: 13px;
  color: #111827;
  font-weight: 500;
  text-align: center;
}

.flow-arrow {
  color: #94a3b8;
  font-size: 20px;
  margin-bottom: 40px; /* Aligned roughly with icons */
}

/* Capability Grid */
.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.cap-card {
  padding: 16px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cap-label {
  font-size: 13px;
  font-weight: 500;
}

.cap-value-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.cap-num {
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
}

.cap-icon {
  font-size: 20px;
  opacity: 0.8;
}

/* Info List */
.info-list {
  margin: 0;
  padding-left: 20px;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.8;
}

.info-list li {
  margin-bottom: 4px;
}
</style>
