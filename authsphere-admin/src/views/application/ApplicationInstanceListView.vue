<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Search, RefreshLeft, Plus, TopRight, Check, VideoPause, User, InfoFilled, CopyDocument } from '@element-plus/icons-vue'

const router = useRouter()

const query = reactive({
  keyword: '',
  subject: '',
  appName: '',
  status: ''
})

const loading = ref(false)

// Table Data
const tableData = [
  {
    id: '1',
    name: 'AAA商城实例',
    code: 'aaa-mall',
    appName: 'AAA商城',
    subject: 'AAA集团',
    dataRoot: '/aaa',
    method: '管理员开通',
    clientCount: 6,
    status: 1, // 1 for 启用
    createTime: '2024-05-20 10:30:45'
  },
  {
    id: '2',
    name: 'AAA支付实例',
    code: 'aaa-pay',
    appName: 'AAA支付',
    subject: 'AAA集团',
    dataRoot: '/aaa/pay',
    method: '管理员开通',
    clientCount: 5,
    status: 1,
    createTime: '2024-05-18 14:22:10'
  },
  {
    id: '3',
    name: '平台IAM实例',
    code: 'platform-iam',
    appName: '平台IAM',
    subject: '平台团队',
    dataRoot: '/platform',
    method: '系统内置',
    clientCount: 8,
    status: 1,
    createTime: '2024-05-10 09:15:33'
  },
  {
    id: '4',
    name: 'BBB商城实例',
    code: 'bbb-mall',
    appName: 'BBB商城',
    subject: 'BBB集团',
    dataRoot: '/bbb',
    method: '管理员开通',
    clientCount: 3,
    status: 0, // 0 for 停用
    createTime: '2024-04-28 11:05:21'
  },
  {
    id: '5',
    name: '支付平台运营实例',
    code: 'pay-ops',
    appName: '支付平台',
    subject: '运营团队',
    dataRoot: '/ops/pay',
    method: '管理员开通',
    clientCount: 4,
    status: 0,
    createTime: '2024-04-15 16:40:12'
  },
  {
    id: '6',
    name: '企业服务门户实例',
    code: 'enterprise-portal',
    appName: '企业服务门户',
    subject: '企业IT部',
    dataRoot: '/portal',
    method: '管理员开通',
    clientCount: 2,
    status: 1,
    createTime: '2024-05-12 08:55:47'
  }
]

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 300)
}

const handleReset = () => {
  query.keyword = ''
  query.subject = ''
  query.appName = ''
  query.status = ''
}
</script>

<template>
  <div class="app-container">
    <!-- Header -->
    <div class="page-header">
      <div class="header-title">
        <h2>应用实例列表</h2>
        <p class="subtitle">查看主体已开通的应用实例</p>
      </div>
    </div>

    <!-- Filter Form -->
    <div class="filter-card">
      <div class="filter-item">
        <label>实例名称/编码</label>
        <el-input v-model="query.keyword" placeholder="请输入实例名称或编码" clearable style="width: 220px" />
      </div>
      <div class="filter-item">
        <label>归属主体</label>
        <el-select v-model="query.subject" placeholder="请选择归属主体" clearable style="width: 200px">
          <el-option label="AAA集团" value="AAA集团" />
          <el-option label="平台团队" value="平台团队" />
          <el-option label="BBB集团" value="BBB集团" />
          <el-option label="运营团队" value="运营团队" />
        </el-select>
      </div>
      <div class="filter-item">
        <label>应用名称</label>
        <el-select v-model="query.appName" placeholder="请选择应用名称" clearable style="width: 200px">
          <el-option label="AAA商城" value="AAA商城" />
          <el-option label="AAA支付" value="AAA支付" />
          <el-option label="平台IAM" value="平台IAM" />
        </el-select>
      </div>
      <div class="filter-item">
        <label>状态</label>
        <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 160px">
          <el-option label="启用" value="1" />
          <el-option label="停用" value="0" />
        </el-select>
      </div>
      <div class="filter-actions">
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon-wrapper bg-blue-100 text-blue-600">
          <el-icon><CopyDocument /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">实例总数</div>
          <div class="stat-value">6<span class="stat-unit">个</span> <el-icon class="trend-icon text-blue-500"><TopRight /></el-icon></div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper bg-green-100 text-green-600">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">启用中</div>
          <div class="stat-value">4<span class="stat-unit">个</span> <el-icon class="trend-icon text-green-500"><TopRight /></el-icon></div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper bg-orange-100 text-orange-500">
          <el-icon><VideoPause /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">停用中</div>
          <div class="stat-value">2<span class="stat-unit">个</span> <el-icon class="trend-icon text-orange-400"><TopRight /></el-icon></div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrapper bg-purple-100 text-purple-600">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">客户端总数</div>
          <div class="stat-value">28<span class="stat-unit">个</span> <el-icon class="trend-icon text-purple-500"><TopRight /></el-icon></div>
        </div>
      </div>
    </div>

    <!-- Action Bar -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="router.push('/applications/instances/create')">新增应用实例</el-button>
      <el-button plain class="btn-batch-enable">
        <template #icon>
          <div class="icon-circle icon-circle-blue">
            <span class="play-triangle"></span>
          </div>
        </template>
        批量启用
      </el-button>
      <el-button plain class="btn-batch-disable text-gray-500">
        <template #icon>
          <div class="icon-circle icon-circle-gray">
            <span class="pause-lines"></span>
          </div>
        </template>
        批量禁用
      </el-button>
    </div>

    <!-- Table -->
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table" border>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="name" label="实例名称" min-width="160" />
        <el-table-column prop="code" label="实例编码" min-width="140" />
        <el-table-column prop="appName" label="应用名称" min-width="140" />
        <el-table-column prop="subject" label="归属主体" min-width="120" />
        <el-table-column label="数据隔离根" min-width="140">
          <template #header>
            <div class="th-with-icon">
              数据隔离根
              <el-tooltip content="租户/主体的数据隔离路径" placement="top">
                <el-icon class="text-gray-400"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <template #default="{ row }">
            {{ row.dataRoot }}
          </template>
        </el-table-column>
        <el-table-column prop="method" label="开通方式" min-width="120" />
        <el-table-column prop="clientCount" label="客户端数" min-width="100" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 1" class="status-tag status-green">启用</span>
            <span v-else class="status-tag status-orange">停用</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="router.push(`/applications/instances/detail/${row.id}`)">详情</el-button>
              <span class="divider"></span>
              <el-button link type="primary">编辑</el-button>
              <span class="divider"></span>
              <el-button link type="primary">客户端</el-button>
              <span class="divider"></span>
              <el-button link type="danger" v-if="row.status === 1">禁用</el-button>
              <el-button link type="primary" v-else>启用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <span class="total-text">共 6 条</span>
        <el-pagination
          background
          layout="sizes, prev, pager, next, jumper"
          :total="6"
          :page-sizes="[10, 20, 50]"
          :default-page-size="10"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-container {
  padding: 24px;
  background-color: #fff;
  min-height: calc(100vh - 60px);
}

.page-header {
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
  color: #6b7280;
}

/* Filter Card */
.filter-card {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 20px 24px;
  background-color: #f8fafc;
  border-radius: 8px;
  margin-bottom: 24px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-left: auto;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background-color: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.stat-icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.bg-blue-100 { background-color: #eff6ff; }
.text-blue-600 { color: #2563eb; }
.text-blue-500 { color: #3b82f6; }

.bg-green-100 { background-color: #dcfce7; }
.text-green-600 { color: #16a34a; }
.text-green-500 { color: #22c55e; }

.bg-orange-100 { background-color: #ffedd5; }
.text-orange-500 { color: #f97316; }
.text-orange-400 { color: #fb923c; }

.bg-purple-100 { background-color: #f3e8ff; }
.text-purple-600 { color: #9333ea; }
.text-purple-500 { color: #a855f7; }

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-title {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #0f172a;
  display: flex;
  align-items: baseline;
  gap: 4px;
  line-height: 1;
}

.stat-unit {
  font-size: 14px;
  font-weight: 400;
  color: #64748b;
}

.trend-icon {
  font-size: 16px;
  margin-left: 2px;
}

/* Action Bar */
.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.icon-circle {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid currentColor;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-triangle {
  width: 0;
  height: 0;
  border-top: 3px solid transparent;
  border-bottom: 3px solid transparent;
  border-left: 5px solid currentColor;
  margin-left: 2px;
}

.pause-lines {
  width: 6px;
  height: 6px;
  border-left: 2px solid currentColor;
  border-right: 2px solid currentColor;
}

.icon-circle-blue { color: #3b82f6; }
.icon-circle-gray { color: #6b7280; }
.text-gray-500 { color: #6b7280; }

/* Table */
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
  padding: 12px 0;
}

.custom-table :deep(td.el-table__cell) {
  border-bottom: 1px solid #eaeaea;
  padding: 12px 0;
}

.th-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.text-gray-400 {
  color: #9ca3af;
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-green {
  background-color: #dcfce7;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.status-orange {
  background-color: #ffedd5;
  color: #ea580c;
  border: 1px solid #fed7aa;
}

.row-actions {
  display: flex;
  align-items: center;
}

.row-actions .el-button {
  padding: 0;
  height: auto;
  font-size: 13px;
}

.divider {
  width: 1px;
  height: 12px;
  background-color: #e5e7eb;
  margin: 0 8px;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0 0 0;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}
</style>
