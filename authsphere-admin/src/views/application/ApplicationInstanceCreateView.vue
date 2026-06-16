<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Lock, InfoFilled, User, Box, CopyDocument, Monitor, Key, Connection, Opportunity } from '@element-plus/icons-vue'

const router = useRouter()

const form = reactive({
  // Section 1
  subject: '',
  app: '',
  name: '企业AAA商城实例',
  code: 'ent_aaa_mall',
  status: 1, // 1: 启用, 0: 禁用
  openMethod: 'platform', // platform: 平台开通, self: 主体自助
  description: '',
  
  // Section 2
  dataIsolationSubject: '',
  subjectCode: 'ent_aaa',
  appCode: 'app_mall',
  accessPath: '',
  tags: [],

  // Section 3
  syncMenu: true,
  syncPermission: true,
  createClient: true,
  clientName: '商城管理后台',
  clientId: 'mall_admin_console',
  clientType: 'web'
})

const handleCancel = () => {
  router.push('/applications/instances')
}

const handleSubmit = () => {
  // Mock submit
  router.push('/applications/instances')
}
</script>

<template>
  <div class="app-container">
    <div class="main-layout">
      <!-- Left Column: Form Area -->
      <div class="form-area">
        <!-- Header -->
        <div class="page-header">
          <div class="header-icon">
            <div class="plus-box">
              <el-icon><Plus /></el-icon>
            </div>
          </div>
          <div class="header-content">
            <h2>新增应用实例</h2>
            <p class="subtitle">为指定主体开通一个应用实例，并初始化默认可用能力。</p>
          </div>
        </div>

        <el-form :model="form" label-position="top" class="custom-form">
          <!-- Section 1: Basic Info -->
          <div class="form-section">
            <div class="section-title">
              <span class="step-num">1</span>基本信息
            </div>
            
            <div class="form-row-3">
              <el-form-item label="所属主体 *" class="required-label">
                <el-select v-model="form.subject" placeholder="请选择主体" class="w-full">
                  <el-option label="企业AAA" value="aaa" />
                  <el-option label="企业BBB" value="bbb" />
                </el-select>
              </el-form-item>
              <el-form-item label="选择应用 *" class="required-label">
                <el-select v-model="form.app" placeholder="请选择应用" class="w-full">
                  <el-option label="商城应用" value="mall" />
                  <el-option label="支付中心" value="pay" />
                </el-select>
              </el-form-item>
              <el-form-item label="实例名称 *" class="required-label">
                <el-input v-model="form.name" placeholder="请输入实例名称" />
              </el-form-item>
            </div>

            <div class="form-row-3">
              <el-form-item label="实例编码 *" class="required-label">
                <el-input v-model="form.code" placeholder="请输入实例编码" />
              </el-form-item>
              <el-form-item label="状态 *" class="required-label">
                <el-radio-group v-model="form.status">
                  <el-radio :value="1">启用</el-radio>
                  <el-radio :value="0">禁用</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="开通方式 *" class="required-label">
                <el-radio-group v-model="form.openMethod">
                  <el-radio value="platform">平台开通</el-radio>
                  <el-radio value="self">主体自助</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>

            <el-form-item label="描述" class="full-width">
              <el-input 
                type="textarea" 
                v-model="form.description" 
                :rows="3"
                placeholder="请输入实例描述（选填）"
                maxlength="200"
                show-word-limit
                class="custom-textarea"
              />
            </el-form-item>
          </div>

          <div class="section-divider"></div>

          <!-- Section 2: Ownership and Isolation -->
          <div class="form-section">
            <div class="section-title">
              <span class="step-num">2</span>归属与隔离
            </div>
            
            <div class="form-row-3">
              <el-form-item label="数据隔离根主体 *" class="required-label">
                <el-select v-model="form.dataIsolationSubject" placeholder="请选择数据隔离根主体" class="w-full">
                  <el-option label="企业AAA" value="aaa" />
                </el-select>
              </el-form-item>
              <el-form-item label="归属主体编码">
                <el-input v-model="form.subjectCode" disabled class="readonly-input">
                  <template #suffix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="应用编码">
                <el-input v-model="form.appCode" disabled class="readonly-input">
                  <template #suffix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-row-2">
              <el-form-item label="实例访问路径 / 默认入口 *" class="required-label">
                <el-input v-model="form.accessPath" placeholder="例如：https://mall.ent-aaa.com" />
              </el-form-item>
              <el-form-item label="标签">
                <el-select
                  v-model="form.tags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  :reserve-keyword="false"
                  placeholder="选择或输入标签，按回车添加"
                  class="w-full"
                >
                </el-select>
              </el-form-item>
            </div>
          </div>

          <div class="section-divider"></div>

          <!-- Section 3: Initial Resource Config -->
          <div class="form-section">
            <div class="section-title">
              <span class="step-num">3</span>初始资源配置
            </div>
            
            <div class="form-row-3 resource-config-row">
              <el-form-item>
                <template #label>
                  <div class="label-with-icon">
                    初始化菜单资源
                    <el-tooltip content="将默认应用的菜单同步到当前实例中" placement="top">
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
                <el-checkbox v-model="form.syncMenu">默认同步应用菜单资源</el-checkbox>
              </el-form-item>
              <el-form-item>
                <template #label>
                  <div class="label-with-icon">
                    初始权限资源
                    <el-tooltip content="将默认应用的权限点同步到当前实例中" placement="top">
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
                <el-checkbox v-model="form.syncPermission">默认同步应用权限资源</el-checkbox>
              </el-form-item>
              <el-form-item>
                <template #label>
                  <div class="label-with-icon">
                    默认创建客户端
                    <el-tooltip content="为该实例自动创建一个默认客户端，用于接口调用和SSO登录" placement="top">
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
                <el-switch v-model="form.createClient" />
              </el-form-item>
            </div>

            <!-- Client config sub-form when switch is on -->
            <div class="client-config-box" v-if="form.createClient">
              <div class="form-row-3">
                <el-form-item label="客户端名称 *" class="required-label">
                  <el-input v-model="form.clientName" />
                </el-form-item>
                <el-form-item label="client_id *" class="required-label">
                  <el-input v-model="form.clientId" />
                </el-form-item>
                <el-form-item label="客户端类型 *" class="required-label">
                  <el-select v-model="form.clientType" class="w-full">
                    <el-option label="Web 应用" value="web" />
                    <el-option label="原生移动应用" value="mobile" />
                    <el-option label="M2M 服务间调用" value="m2m" />
                  </el-select>
                </el-form-item>
              </div>
            </div>

          </div>
        </el-form>
      </div>

      <!-- Right Column: Sidebar -->
      <div class="sidebar-area">
        <!-- Preview Card -->
        <div class="side-card">
          <div class="side-card-title">
            <el-icon class="text-blue-500"><Connection /></el-icon>
            关系预览
          </div>
          
          <div class="preview-flow">
            <!-- Node 1 -->
            <div class="flow-node">
              <div class="node-icon bg-blue-100 text-blue-500"><el-icon><User /></el-icon></div>
              <div class="node-text">主体: 企业AAA</div>
            </div>
            <div class="flow-arrow"><el-icon><TopRight class="rotate-45"/></el-icon></div>
            
            <!-- Node 2 -->
            <div class="flow-node">
              <div class="node-icon bg-green-100 text-green-500"><el-icon><Box /></el-icon></div>
              <div class="node-text">应用: 商城应用</div>
            </div>
            <div class="flow-arrow"><el-icon><TopRight class="rotate-45"/></el-icon></div>

            <!-- Node 3 -->
            <div class="flow-node">
              <div class="node-icon bg-purple-100 text-purple-500"><el-icon><CopyDocument /></el-icon></div>
              <div class="node-text">应用实例: 企业AAA商城实例</div>
            </div>
            <div class="flow-arrow"><el-icon><TopRight class="rotate-45"/></el-icon></div>

            <!-- Node 4 -->
            <div class="flow-node">
              <div class="node-icon bg-orange-100 text-orange-500"><el-icon><Key /></el-icon></div>
              <div class="node-text">数据隔离根: 企业AAA</div>
            </div>
            <div class="flow-arrow"><el-icon><TopRight class="rotate-45"/></el-icon></div>

            <!-- Node 5 -->
            <div class="flow-node">
              <div class="node-icon bg-blue-50 text-blue-600"><el-icon><Monitor /></el-icon></div>
              <div class="node-text">初始客户端: 商城管理后台</div>
            </div>
          </div>
        </div>

        <!-- Info Card -->
        <div class="side-card">
          <div class="side-card-title">
            <el-icon class="text-blue-500"><Opportunity /></el-icon>
            创建后将自动完成
          </div>
          
          <div class="auto-task-list">
            <div class="auto-task-item">
              <span class="task-num">1</span>
              <span class="task-text">创建应用实例记录</span>
            </div>
            <div class="auto-task-item">
              <span class="task-num">2</span>
              <span class="task-text">初始化默认菜单与权限资源</span>
            </div>
            <div class="auto-task-item">
              <span class="task-num">3</span>
              <span class="task-text">可继续配置客户端与访问入口</span>
            </div>
          </div>

          <div class="side-card-footer">
            <el-icon><InfoFilled /></el-icon>
            创建完成后，可在 <a href="#" class="link">应用实例详情</a> 中继续配置。
          </div>
        </div>
      </div>
    </div>

    <!-- Sticky Footer -->
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button @click="handleCancel">保存草稿</el-button>
      <el-button type="primary" @click="handleSubmit">创建应用实例</el-button>
    </div>
  </div>
</template>

<style scoped>
.app-container {
  padding: 0;
  background-color: #f1f5f9; /* Outer background to show card outlines */
  min-height: calc(100vh - 60px);
  position: relative;
  display: flex;
  flex-direction: column;
}

.main-layout {
  display: flex;
  gap: 24px;
  padding: 24px;
  flex: 1;
  padding-bottom: 100px; /* Space for sticky footer */
}

/* Left Column */
.form-area {
  flex: 1;
  background-color: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 40px;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.plus-box {
  width: 48px;
  height: 48px;
  background-color: #eff6ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  font-size: 24px;
}

.header-content h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

/* Form Styles */
.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  padding-bottom: 8px;
}

.required-label :deep(.el-form-item__label::after) {
  content: ' *';
  color: #ef4444;
}

.required-label :deep(.el-form-item__label::before) {
  display: none; /* Hide default el-form red star if any */
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 24px;
}

.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background-color: #3b82f6;
  color: #fff;
  border-radius: 50%;
  font-size: 14px;
  margin-right: 12px;
}

.form-row-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 16px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 16px;
}

.w-full {
  width: 100%;
}

.full-width {
  width: 100%;
}

.custom-textarea :deep(.el-textarea__inner) {
  background-color: #f9fafb;
}

.section-divider {
  height: 1px;
  background-color: #f3f4f6;
  margin: 0 -32px 32px -32px;
}

.readonly-input :deep(.el-input__wrapper) {
  background-color: #f9fafb;
}

.label-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.info-icon {
  color: #9ca3af;
  font-size: 14px;
  cursor: help;
}

.resource-config-row {
  margin-bottom: 8px; /* Tighter gap before the client box */
}

/* Right Column */
.sidebar-area {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.side-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 24px;
}

.text-blue-500 { color: #3b82f6; }

/* Relationship Flow */
.preview-flow {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flow-node {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.node-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bg-blue-100 { background-color: #eff6ff; }
.bg-green-100 { background-color: #dcfce7; }
.text-green-500 { color: #22c55e; }
.bg-purple-100 { background-color: #f3e8ff; }
.text-purple-500 { color: #a855f7; }
.bg-orange-100 { background-color: #ffedd5; }
.text-orange-500 { color: #f97316; }
.bg-blue-50 { background-color: #e0f2fe; }
.text-blue-600 { color: #2563eb; }

.node-text {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.flow-arrow {
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cbd5e1;
  font-size: 16px;
  margin-left: -200px; /* Quick hack to align arrow under icon roughly */
  width: 36px;
}
.rotate-45 {
  transform: rotate(90deg); /* Arrow down */
}

/* Auto Task List */
.auto-task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.auto-task-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.task-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background-color: #3b82f6;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.task-text {
  font-size: 14px;
  color: #374151;
}

.side-card-footer {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.side-card-footer .el-icon {
  margin-top: 2px;
}

.link {
  color: #3b82f6;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

/* Sticky Footer */
.form-footer {
  position: fixed;
  bottom: 0;
  left: 240px; /* Assuming side menu width */
  right: 0;
  height: 64px;
  background-color: #fff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  z-index: 100;
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.05);
}
</style>
