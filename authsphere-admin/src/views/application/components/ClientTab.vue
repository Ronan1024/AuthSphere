<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus, Document } from '@element-plus/icons-vue'

const query = reactive({
  name: '',
  clientId: '',
  type: '',
  status: ''
})

const loading = ref(false)

const tableData = [
  {
    id: '1',
    name: '统一门户 Web 端',
    clientId: 'portal_web',
    type: 'Web 应用',
    domain: '企业AAA',
    status: 1, // 1 for 启用
    description: '门户系统前端界面客户端',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '2',
    name: '统一门户移动端 APP',
    clientId: 'portal_mobile',
    type: '移动应用',
    domain: '企业AAA',
    status: 1,
    description: '门户系统 iOS/Android 客户端',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '3',
    name: '门户 OpenAPI',
    clientId: 'portal_api',
    type: 'API 服务',
    domain: '系统内部',
    status: 1,
    description: '提供给其他系统调用的 OpenAPI 客户端',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '4',
    name: '门户测试客户端',
    clientId: 'portal_test',
    type: 'Web 应用',
    domain: '测试域',
    status: 0, // 0 for 禁用
    description: '用于测试环境联调的客户端',
    updateTime: '2024-05-20 16:45:00'
  }
]

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 300)
}

const handleReset = () => {
  query.name = ''
  query.clientId = ''
  query.type = ''
  query.status = ''
}
</script>

<template>
  <div class="client-tab-container">
    <!-- Filter and Actions -->
    <div class="filter-action-bar">
      <div class="filter-form">
        <div class="filter-item">
          <label>客户端名称</label>
          <el-input v-model="query.name" placeholder="请输入名称" clearable style="width: 160px" />
        </div>
        <div class="filter-item">
          <label>client_id</label>
          <el-input v-model="query.clientId" placeholder="请输入 ID" clearable style="width: 160px" />
        </div>
        <div class="filter-item">
          <label>客户端类型</label>
          <el-select v-model="query.type" placeholder="请选择类型" clearable style="width: 160px">
            <el-option label="Web 应用" value="web" />
            <el-option label="移动应用" value="mobile" />
            <el-option label="API 服务" value="api" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>状态</label>
          <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </div>
        <div class="filter-btn-group">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
      <div class="action-btn-group">
        <el-button type="primary" :icon="Plus" @click="$router.push('/applications/detail/1/clients/create')">新增客户端</el-button>
      </div>
    </div>

    <!-- Data Table -->
    <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table" border>
      <el-table-column prop="name" label="客户端名称" min-width="150" />
      <el-table-column prop="clientId" label="client_id" min-width="150" />
      <el-table-column label="客户端类型" min-width="120">
        <template #default="{ row }">
          <span class="type-tag">{{ row.type }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="domain" label="默认身份域" min-width="120" />
      <el-table-column label="状态" min-width="80">
        <template #default="{ row }">
          <div class="status-cell" :class="row.status === 1 ? 'text-green' : 'text-red'">
            <span class="dot"></span> {{ row.status === 1 ? '启用' : '禁用' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary">详情</el-button>
            <span class="divider"></span>
            <el-button link type="primary">配置</el-button>
            <span class="divider"></span>
            <el-button link type="danger" v-if="row.status === 1">禁用</el-button>
            <el-button link type="primary" v-else>启用</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination-container">
      <span class="total-text">共 4 条</span>
      <el-pagination
        background
        layout="sizes, prev, pager, next, jumper"
        :total="4"
        :page-sizes="[10, 20, 50]"
        :default-page-size="10"
      />
    </div>

    <!-- Info Box -->
    <div class="info-box">
      <div class="info-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="info-content-wrapper">
        <div class="info-left">
          <h4>应用客户端说明</h4>
          <p>客户端是系统对外的访问入口，用于管理第三方应用或自建应用接入的身份认证配置。</p>
        </div>
        <div class="info-right">
          <ul>
            <li>
              支持多种客户端类型：
              <span class="type-badge">Web 应用</span>
              <span class="type-badge">原生移动应用</span>
              <span class="type-badge">M2M 服务间调用 API</span>
            </li>
            <li>每个客户端对应独立的 client_id 和 client_secret，请妥善保管凭证信息。</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.client-tab-container {
  padding: 0;
}

/* Filter and Actions */
.filter-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #4b5563;
  white-space: nowrap;
}

.filter-btn-group {
  display: flex;
  gap: 12px;
  margin-left: 8px;
}

.action-btn-group {
  display: flex;
  gap: 12px;
}

/* Table specific styles */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
}

.custom-table :deep(th.el-table__cell) {
  background-color: #f9fafb;
  color: #111827;
  font-weight: 500;
  border-bottom: 1px solid #eaeaea;
}

.custom-table :deep(td.el-table__cell) {
  border-bottom: 1px solid #eaeaea;
}

.type-tag {
  color: #3b82f6;
  font-size: 13px;
  font-weight: 500;
  background-color: #eff6ff;
  padding: 2px 8px;
  border-radius: 4px;
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
.text-red { color: #ef4444; }

.row-actions {
  display: flex;
  align-items: center;
}

.row-actions .el-button {
  padding: 0;
  height: auto;
}

.divider {
  width: 1px;
  height: 12px;
  background-color: #e5e7eb;
  margin: 0 8px;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}

/* Info Box */
.info-box {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.info-icon {
  width: 40px;
  height: 40px;
  background-color: #eff6ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  font-size: 20px;
  flex-shrink: 0;
}

.info-content-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  flex-grow: 1;
}

.info-left {
  flex: 1;
  min-width: 300px;
}

.info-left h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #111827;
  font-weight: 600;
}

.info-left p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.info-right {
  flex: 2;
  min-width: 400px;
}

.info-right ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.8;
}

.info-right li {
  margin-bottom: 4px;
}

.info-right li:last-child {
  margin-bottom: 0;
}

.type-badge {
  display: inline-block;
  margin-left: 8px;
  color: #475569;
}
</style>
