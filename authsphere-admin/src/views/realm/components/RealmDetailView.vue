<script setup lang="ts">
import { ref } from 'vue'
import { 
  ArrowLeft, Edit,
  Promotion, Monitor, Key, Link, Clock, Document,
  User, OfficeBuilding, Right, Lock, EditPen, UserFilled,
  Connection, Stamp
} from '@element-plus/icons-vue'
import type { RealmRecord } from '@/api/realm'
import type { PasswordPolicyRecord } from '@/api/passwordPolicy'

const props = defineProps<{
  realm: RealmRecord
  passwordPolicy?: PasswordPolicyRecord
}>()

const emit = defineEmits(['back', 'edit'])

const activeTab = ref('basic')

const mockAssociationObjects = [
  { type: '登录入口', name: '平台后台登录入口', realmCode: 'enterprise_realm', status: 1, desc: '平台管理后台默认登录入口' },
  { type: '主体', name: '企业主体 (示例: 企业AAA)', realmCode: 'enterprise_realm', status: 1, desc: '企业客户主体, 用于企业后台系统访问' },
  { type: '主体', name: '商户主体 (示例: 商户A)', realmCode: 'enterprise_realm', status: 1, desc: '商户主体, 用于商户后台系统访问' },
  { type: '主体', name: '支付商户主体 (示例: 支付商户P1)', realmCode: 'enterprise_realm', status: 1, desc: '支付商户主体, 用于支付商户后台访问' }
]

</script>

<template>
  <div class="realm-detail-layout">
    <!-- Top Header -->
    <div class="detail-header-bar">
      <div class="header-left">
        <el-button :icon="ArrowLeft" plain class="back-btn" @click="emit('back')">返回</el-button>
        <div class="header-title-wrapper">
          <div class="title-row">
            <h2>{{ realm.name }} <span>({{ realm.code }})</span></h2>
            <div class="status-badge" :class="realm.status === 1 ? 'is-active' : ''">
              <span class="dot"></span> {{ realm.status === 1 ? '启用' : '禁用' }}
            </div>
          </div>
          <div class="subtitle-row">身份域ID: {{ realm.id || '20001' }}</div>
        </div>
      </div>
      <div class="header-right">
        <!-- Actions if needed -->
      </div>
    </div>

    <div class="detail-content-wrapper">
      <!-- Left Sidebar: Overview -->
      <div class="detail-sidebar">
        <div class="sidebar-section">
          <h3 class="section-title">身份域概览</h3>
          <div class="overview-list">
            <div class="overview-item">
              <el-icon class="item-icon"><Promotion /></el-icon>
              <span class="item-label">身份域编码</span>
              <span class="item-value">{{ realm.code }}</span>
            </div>
            <div class="overview-item">
              <el-icon class="item-icon"><Monitor /></el-icon>
              <span class="item-label">身份域类型</span>
              <span class="item-value">企业客户身份域</span>
            </div>
            <div class="overview-item">
              <el-icon class="item-icon"><Key /></el-icon>
              <span class="item-label">账号唯一性范围</span>
              <span class="item-value">Realm 内唯一</span>
            </div>
            <div class="overview-item">
              <el-icon class="item-icon"><Link /></el-icon>
              <span class="item-label">默认状态</span>
              <span class="item-value status-green"><span class="dot"></span> 启用</span>
            </div>
            <div class="overview-item">
              <el-icon class="item-icon"><Clock /></el-icon>
              <span class="item-label">创建时间</span>
              <span class="item-value">2024-11-18 11:09:54</span>
            </div>
            <div class="overview-item">
              <el-icon class="item-icon"><Clock /></el-icon>
              <span class="item-label">更新时间</span>
              <span class="item-value">2025-05-26 09:20:40</span>
            </div>
            <div class="overview-item align-start">
              <el-icon class="item-icon"><Document /></el-icon>
              <span class="item-label">说明</span>
              <span class="item-value desc-text">企业/商户/支付商户后台统一账号空间</span>
            </div>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon bg-blue"><el-icon><UserFilled /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">账号数</div>
              <div class="stat-value">18,642</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon bg-green"><el-icon><OfficeBuilding /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">关联主体</div>
              <div class="stat-value">326</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon bg-purple"><el-icon><Right /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">登录入口</div>
              <div class="stat-value">3</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon bg-orange"><el-icon><Lock /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">启用登录方式</div>
              <div class="stat-value">2</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Main Content: Tabs -->
      <div class="detail-main">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <!-- Block 1: 基础资料 -->
            <div class="content-block">
              <div class="block-header">
                <h3>基础资料</h3>
                <el-button plain size="small" :icon="EditPen">编辑</el-button>
              </div>
              <div class="info-grid">
                <div class="info-item"><span>身份域名称：</span><strong>{{ realm.name }}</strong></div>
                <div class="info-item"><span>状态：</span><strong class="status-green"><span class="dot"></span> 启用</strong></div>
                <div class="info-item"><span>身份域编码：</span><strong>{{ realm.code }}</strong></div>
                <div class="info-item"><span>排序：</span><strong>1</strong></div>
                <div class="info-item"><span>身份域类型：</span><strong>企业客户身份域</strong></div>
                <div class="info-item"><span>备注：</span><strong>无</strong></div>
                <div class="info-item"><span>账号唯一性范围：</span><strong>Realm 内唯一</strong></div>
              </div>
            </div>

            <!-- Block 2: 使用说明 -->
            <div class="content-block">
              <div class="block-header">
                <h3>使用说明 / 设计定位</h3>
                <el-button plain size="small" :icon="EditPen">编辑</el-button>
              </div>
              <div class="desc-content">
                <p>企业客户身份域是为企业、商户、支付商户等后台系统提供的统一账号空间。该身份域承载所有企业相关账号，在此身份域下创建的账号可用于访问平台及其关联应用系统。多个主体 (企业、商户、支付商户等) 可共享同一身份域，实现统一认证与授权，便于集中管理与权限控制。</p>
              </div>
            </div>

            <!-- Block 3: 关联规则 -->
            <div class="content-block">
              <div class="block-header">
                <h3>关联规则 <span class="header-sub">（登录到主体的关系链路）</span></h3>
              </div>
              <div class="flowchart-container">
                <!-- Step 1 -->
                <div class="flow-node">
                  <div class="node-icon bg-light-blue"><el-icon><Right /></el-icon></div>
                  <div class="node-text">
                    <h4>登录入口</h4>
                    <p>用户发起登录</p>
                  </div>
                </div>
                <div class="flow-arrow"><el-icon><Right /></el-icon></div>
                <!-- Step 2 -->
                <div class="flow-node highlight">
                  <div class="node-icon bg-blue"><el-icon><Lock /></el-icon></div>
                  <div class="node-text">
                    <h4>身份域 (Realm)</h4>
                    <p>统一账号空间</p>
                  </div>
                </div>
                <div class="flow-arrow"><el-icon><Right /></el-icon></div>
                <!-- Step 3 -->
                <div class="flow-node">
                  <div class="node-icon bg-green"><el-icon><User /></el-icon></div>
                  <div class="node-text">
                    <h4>账号 (Account)</h4>
                    <p>用户登录身份</p>
                  </div>
                </div>
                <div class="flow-arrow"><el-icon><Right /></el-icon></div>
                <!-- Step 4 -->
                <div class="flow-node">
                  <div class="node-icon bg-purple"><el-icon><Connection /></el-icon></div>
                  <div class="node-text">
                    <h4>主体成员 (SubjectMember)</h4>
                    <p>账号与主体的关联</p>
                  </div>
                </div>
                <div class="flow-arrow"><el-icon><Right /></el-icon></div>
                <!-- Step 5 -->
                <div class="flow-node">
                  <div class="node-icon bg-orange"><el-icon><Stamp /></el-icon></div>
                  <div class="node-text">
                    <h4>主体 (Subject)</h4>
                    <p>企业/商户/支付商户</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Block 4: 关联对象概览 -->
            <div class="content-block borderless">
              <div class="block-header">
                <h3>关联对象概览 <span class="header-sub">（当前身份域下的主要关联对象）</span></h3>
              </div>
              <el-table :data="mockAssociationObjects" style="width: 100%">
                <el-table-column prop="type" label="对象类型" width="120" />
                <el-table-column prop="name" label="名称 / 标识" min-width="200" />
                <el-table-column prop="realmCode" label="默认身份域" width="160" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <span class="status-green"><span class="dot"></span> 启用</span>
                  </template>
                </el-table-column>
                <el-table-column prop="desc" label="说明" min-width="250" />
                <el-table-column label="操作" width="80" fixed="right">
                  <template #default>
                    <el-button link type="primary">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination-right mt-4">
                <span class="total-text">共 4 条</span>
                <el-pagination layout="prev, pager, next, sizes" :total="4" :page-sizes="[10, 20]" />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="登录方式" name="login-methods"><el-empty description="开发中" /></el-tab-pane>
          <el-tab-pane label="账号策略" name="account-policy"><el-empty description="开发中" /></el-tab-pane>
          <el-tab-pane label="安全策略" name="security-policy"><el-empty description="开发中" /></el-tab-pane>
          <el-tab-pane label="关联主体与入口" name="associations"><el-empty description="开发中" /></el-tab-pane>
          <el-tab-pane label="操作日志" name="logs"><el-empty description="开发中" /></el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<style scoped>
.realm-detail-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* Header Bar */
.detail-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.back-btn {
  font-weight: 500;
}

.header-title-wrapper {
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
  font-size: 20px;
  color: #111827;
  font-weight: 600;
}

.title-row h2 span {
  font-weight: normal;
  color: #6b7280;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 12px;
}

.status-badge.is-active {
  color: #16a34a;
  background: #f0fdf4;
}

.status-badge .dot, .status-green .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.subtitle-row {
  font-size: 13px;
  color: #9ca3af;
}

.status-green {
  color: #16a34a;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* Layout */
.detail-content-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Sidebar */
.detail-sidebar {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  background: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 24px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.overview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.overview-item.align-start {
  align-items: flex-start;
}

.item-icon {
  font-size: 16px;
  color: #9ca3af;
  margin-right: 12px;
  flex-shrink: 0;
}

.item-label {
  color: #6b7280;
  width: 120px;
  flex-shrink: 0;
}

.item-value {
  color: #111827;
  flex-grow: 1;
  word-break: break-all;
}

.desc-text {
  line-height: 1.5;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.bg-blue { background: #eff6ff; color: #3b82f6; }
.bg-green { background: #f0fdf4; color: #10b981; }
.bg-purple { background: #f5f3ff; color: #8b5cf6; }
.bg-orange { background: #fff7ed; color: #f97316; }
.bg-light-blue { background: #f0f9ff; color: #0ea5e9; }

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

/* Main Content */
.detail-main {
  flex-grow: 1;
  min-width: 0;
  background: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 0 24px 24px;
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eaeaea;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 24px;
  height: 56px;
  line-height: 56px;
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  font-weight: 600;
  color: #2563eb;
}

/* Content Blocks */
.content-block {
  padding: 24px 0;
  border-bottom: 1px solid #f3f4f6;
}

.content-block.borderless {
  border-bottom: none;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.block-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.header-sub {
  font-weight: normal;
  color: #6b7280;
  font-size: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 40px;
}

.info-item {
  display: flex;
  font-size: 14px;
}

.info-item span {
  width: 140px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-item strong {
  color: #111827;
  font-weight: 500;
}

.desc-content p {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

/* Flowchart */
.flowchart-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  padding: 24px 32px;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 120px;
}

.flow-node.highlight .node-icon {
  background: #e0e7ff;
  color: #4f46e5;
  box-shadow: 0 0 0 4px #e0e7ff50;
}

.node-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 12px;
}

.node-text h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.node-text p {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.flow-arrow {
  color: #cbd5e1;
  font-size: 20px;
}

/* Table Pagination */
.pagination-right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}
</style>
