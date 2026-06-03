<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Back, Message } from '@element-plus/icons-vue'

const router = useRouter()

const formData = reactive({
  name: '',
  code: '',
  identifier: '',
  type: '',
  authMethod: 'authorization_code',
  status: 1,
  description: '',
  instanceId: '',
  callbackUrl: '',
  accessTokenLifespan: 3600,
  refreshTokenLifespan: 604800,
  autoApprove: false,
  pkceSupport: true
})

const scopeTableData = ref([])

const handleBack = () => {
  router.back()
}

const handleSave = () => {
  // Mock save action
}
</script>

<template>
  <div class="client-create-page">
    <!-- Top Header -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><Back /></el-icon>
      </div>
      <div class="header-titles">
        <h2>添加客户端</h2>
        <p>为应用「统一门户系统」添加新的客户端</p>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="main-content-scrollable">
      
      <!-- Top Grid: Left (Basic Info) & Right (Instance + Callback) -->
      <div class="top-grid-layout">
        
        <!-- Left Column: Basic Info -->
        <div class="left-column">
          <el-card shadow="never" class="form-card basic-info-card">
            <template #header>
              <div class="card-header">基本信息</div>
            </template>
            <el-form :model="formData" label-width="120px" class="custom-form" label-position="left">
              <el-form-item label="客户端名称" required>
                <el-input v-model="formData.name" placeholder="请输入客户端名称" maxlength="50" show-word-limit />
              </el-form-item>
              
              <el-form-item label="客户端编码" required>
                <el-input v-model="formData.code" placeholder="请输入客户端编码（由英文、数字、中划线组成）" maxlength="50" show-word-limit />
              </el-form-item>
              
              <el-form-item label="客户端标识" required>
                <el-input v-model="formData.identifier" placeholder="请输入客户端标识（简称，字母、数字或下划线）" maxlength="50" show-word-limit />
              </el-form-item>
              
              <el-form-item label="类型" required>
                <el-select v-model="formData.type" placeholder="请选择客户端类型" style="width: 100%">
                  <el-option label="Web 应用" value="web" />
                  <el-option label="移动应用" value="mobile" />
                  <el-option label="API 服务" value="api" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="授权方式" required>
                <el-radio-group v-model="formData.authMethod">
                  <el-radio label="authorization_code">授权码模式</el-radio>
                  <el-radio label="client_credentials">客户端凭证</el-radio>
                  <el-radio label="password">密码模式</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="状态" required>
                <el-radio-group v-model="formData.status" class="status-radio-group">
                  <el-radio :label="1"><span class="text-green"><span class="dot dot-green"></span>启用</span></el-radio>
                  <el-radio :label="0"><span class="text-red"><span class="dot dot-red"></span>禁用</span></el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="描述">
                <el-input 
                  v-model="formData.description" 
                  type="textarea" 
                  :rows="4" 
                  placeholder="请输入描述信息（选填）" 
                  maxlength="200" 
                  show-word-limit 
                />
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- Right Column: Instance & Callback -->
        <div class="right-column">
          <!-- Instance Card -->
          <el-card shadow="never" class="form-card mb-20">
            <template #header>
              <div class="card-header">应用实例</div>
            </template>
            <el-form :model="formData" label-width="100px" class="custom-form" label-position="left">
              <el-form-item label="选择实例" required>
                <div class="field-with-help">
                  <el-select v-model="formData.instanceId" placeholder="请选择应用实例" style="width: 100%">
                    <el-option label="北京研发中心实例" value="inst_bj" />
                    <el-option label="上海数据中心实例" value="inst_sh" />
                  </el-select>
                  <div class="help-text">客户端将隶属于所选的应用实例</div>
                </div>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- Callback URL Card -->
          <el-card shadow="never" class="form-card flex-grow-card">
            <template #header>
              <div class="card-header">回调地址</div>
            </template>
            <el-form :model="formData" label-width="100px" class="custom-form callback-form" label-position="left">
              <el-form-item label="回调地址">
                <div class="field-with-help">
                  <el-input 
                    v-model="formData.callbackUrl" 
                    type="textarea" 
                    :rows="4" 
                    placeholder="请输入回调地址，多个地址请换行输入" 
                    maxlength="1000" 
                    show-word-limit 
                  />
                  <div class="help-text callback-help-list">
                    <ul>
                      <li>授权码模式必填</li>
                      <li>支持的回调地址示例：https://example.com/callback</li>
                      <li>支持通配符地址：https://*.example.com/*</li>
                    </ul>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </div>

      <!-- Full Width: Client Configuration -->
      <el-card shadow="never" class="form-card mb-20">
        <template #header>
          <div class="card-header">客户端配置</div>
        </template>
        <div class="config-grid">
          <el-form :model="formData" label-width="160px" class="custom-form" label-position="left">
            <div class="config-row">
              <div class="config-col">
                <el-form-item label="访问令牌有效期">
                  <div class="field-with-help">
                    <el-input v-model="formData.accessTokenLifespan" type="number">
                      <template #append>秒</template>
                    </el-input>
                    <div class="help-text">设置访问令牌 (Access Token) 的有效期，默认3600秒 (1小时)</div>
                  </div>
                </el-form-item>
              </div>
              <div class="config-col">
                <el-form-item label="刷新令牌有效期">
                  <div class="field-with-help">
                    <el-input v-model="formData.refreshTokenLifespan" type="number">
                      <template #append>秒</template>
                    </el-input>
                    <div class="help-text">设置刷新令牌 (Refresh Token) 的有效期，默认604800秒 (7天)</div>
                  </div>
                </el-form-item>
              </div>
            </div>
            
            <div class="config-row">
              <div class="config-col">
                <el-form-item label="自动批准">
                  <div class="field-with-help">
                    <el-switch v-model="formData.autoApprove" />
                    <div class="help-text">开启后，用户访问时无需手动授权，直接自动批准</div>
                  </div>
                </el-form-item>
              </div>
              <div class="config-col">
                <el-form-item label="PKCE 支持">
                  <div class="field-with-help">
                    <el-switch v-model="formData.pkceSupport" />
                    <div class="help-text">开启后，支持 PKCE 机制，增强客户端安全性</div>
                  </div>
                </el-form-item>
              </div>
            </div>
          </el-form>
        </div>
      </el-card>

      <!-- Full Width: Scopes -->
      <el-card shadow="never" class="form-card mb-20">
        <template #header>
          <div class="card-header-with-action">
            <div class="header-titles-inline">
              <span class="card-header-text">权限范围 (Scope)</span>
              <span class="card-header-sub">选择客户端可访问的权限范围</span>
            </div>
            <el-button type="primary" class="add-scope-btn">+ 选择权限范围</el-button>
          </div>
        </template>
        
        <el-table :data="scopeTableData" class="custom-table" border style="width: 100%">
          <template #empty>
            <div class="custom-empty">
              <el-icon class="empty-icon"><Message /></el-icon>
              <span>暂无数据</span>
            </div>
          </template>
          <el-table-column prop="code" label="权限编码" min-width="150" />
          <el-table-column prop="name" label="权限名称" min-width="150" />
          <el-table-column prop="description" label="权限描述" min-width="250" />
          <el-table-column label="操作" width="120" fixed="right" />
        </el-table>
      </el-card>

      <!-- Bottom Padding to prevent content from hiding behind fixed footer -->
      <div class="bottom-padding"></div>
    </div>

    <!-- Fixed Footer Actions -->
    <div class="page-footer">
      <div class="footer-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button plain @click="handleSave">保存并继续</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.client-create-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  background-color: #f5f7fa;
  margin: -20px; /* Counteract layout padding if needed */
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  padding: 24px 32px;
  background-color: #fff;
  border-bottom: 1px solid #eaeaea;
  flex-shrink: 0;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #4b5563;
  cursor: pointer;
  margin-right: 16px;
  border-radius: 4px;
}

.back-btn:hover {
  background-color: #f3f4f6;
}

.header-titles h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.header-titles p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

/* Main Content */
.main-content-scrollable {
  flex-grow: 1;
  overflow-y: auto;
  padding: 24px 32px;
}

.mb-20 {
  margin-bottom: 20px;
}

.bottom-padding {
  height: 80px; /* Space for the footer */
}

/* Cards */
.form-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

/* Top Grid Layout */
.top-grid-layout {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.left-column {
  flex: 3;
}

.right-column {
  flex: 2;
  display: flex;
  flex-direction: column;
}

.basic-info-card {
  height: 100%;
}

.flex-grow-card {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.flex-grow-card :deep(.el-card__body) {
  flex-grow: 1;
}

/* Forms */
.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

.custom-form :deep(.el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before) {
  color: #ef4444;
}

.field-with-help {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.help-text {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 6px;
  line-height: 1.5;
}

.callback-help-list ul {
  margin: 0;
  padding-left: 16px;
}

.callback-help-list li {
  margin-bottom: 2px;
}

.callback-form :deep(.el-form-item__content) {
  align-items: flex-start;
}

/* Status Radio */
.status-radio-group .el-radio {
  margin-right: 24px;
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

.dot-green { background-color: #16a34a; }
.dot-red { background-color: #ef4444; }

.text-green { color: #16a34a; }
.text-red { color: #ef4444; }

/* Config Grid */
.config-row {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
}

.config-row:last-child {
  margin-bottom: 0;
}

.config-col {
  flex: 1;
}

.config-grid :deep(.el-input-group__append) {
  background-color: #f9fafb;
  color: #6b7280;
  padding: 0 16px;
}

/* Scopes */
.card-header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-titles-inline {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.card-header-text {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.card-header-sub {
  font-size: 13px;
  color: #6b7280;
  font-weight: normal;
}

.add-scope-btn {
  background-color: #2563eb;
}

.custom-table {
  border-radius: 6px;
  overflow: hidden;
}

.custom-table :deep(th.el-table__cell) {
  background-color: #f9fafb;
  color: #4b5563;
  font-weight: 500;
}

.custom-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #9ca3af;
}

.empty-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #d1d5db;
}

/* Footer */
.page-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background-color: #fff;
  border-top: 1px solid #eaeaea;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 32px;
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.footer-actions .el-button {
  padding: 10px 24px;
}
</style>
