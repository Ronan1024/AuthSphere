<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Download, ArrowDown, Monitor, Iphone, Coin, Platform } from '@element-plus/icons-vue'

const router = useRouter()
const query = reactive({
  name: '',
  code: '',
  type: '',
  status: '',
  client: '',
  dateRange: []
})

const activeTab = ref('all')

const loading = ref(false)

const mockTableData = [
  {
    id: '1',
    name: '统一门户系统',
    description: '企业统一门户与集成平台',
    code: 'unified-portal',
    type: 'Web应用',
    typeClass: 'bg-blue',
    client: 'Web浏览器',
    status: 1, // 已启用
    creator: '张管理员',
    createTime: '2024-05-20 10:30:00'
  },
  {
    id: '2',
    name: '移动办公应用',
    description: '移动办公与审批',
    code: 'mobile-office',
    type: '移动应用',
    typeClass: 'bg-green',
    client: 'iOS, Android',
    status: 1, // 已启用
    creator: '李管理员',
    createTime: '2024-05-18 14:20:00'
  },
  {
    id: '3',
    name: '用户中心服务',
    description: '用户管理与认证服务',
    code: 'user-center',
    type: 'API服务',
    typeClass: 'bg-purple',
    client: 'Web, 移动端',
    status: 1, // 已启用
    creator: '系统管理员',
    createTime: '2024-05-15 09:15:00'
  },
  {
    id: '4',
    name: '数据分析平台',
    description: '数据分析与报表平台',
    code: 'data-analytics',
    type: 'Web应用',
    typeClass: 'bg-orange',
    client: 'Web浏览器',
    status: 2, // 停用
    creator: '王管理员',
    createTime: '2024-05-10 16:45:00'
  },
  {
    id: '5',
    name: '第三方集成服务',
    description: '对接第三方系统服务',
    code: 'third-party-api',
    type: 'API服务',
    typeClass: 'bg-teal',
    client: 'Web, 移动端',
    status: 1, // 已启用
    creator: '赵管理员',
    createTime: '2024-05-08 11:20:00'
  },
  {
    id: '6',
    name: '桌面管理工具',
    description: '桌面端系统管理工具',
    code: 'desktop-admin',
    type: '桌面应用',
    typeClass: 'bg-gray',
    client: 'Windows, Mac',
    status: 0, // 未启用
    creator: '张管理员',
    createTime: '2024-05-01 08:30:00'
  }
]

const tableData = ref(mockTableData)

const getAppIcon = (type: string) => {
  if (type === 'Web应用') return Monitor
  if (type === '移动应用') return Iphone
  if (type === 'API服务') return Coin
  if (type === '桌面应用') return Platform
  return Monitor
}

const getStatusClass = (status: number) => {
  if (status === 1) return 'text-green'
  if (status === 2) return 'text-orange'
  return 'text-gray'
}

const getStatusText = (status: number) => {
  if (status === 1) return '已启用'
  if (status === 2) return '停用'
  return '未启用'
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.type = ''
  query.status = ''
  query.client = ''
  query.dateRange = []
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}
</script>

<template>
  <div class="application-list-page">
    <div class="page-header">
      <div class="header-left">
        <h1>应用列表</h1>
        <p>管理身份域下的所有应用，支持应用的创建、配置与生命周期管理。</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="router.push('/applications/create')">新建应用</el-button>
        <el-button :icon="Download">导出</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="app-filter-form">
        <el-form-item label="应用名称">
          <el-input v-model="query.name" placeholder="请输入应用名称" clearable />
        </el-form-item>
        <el-form-item label="应用编码">
          <el-input v-model="query.code" placeholder="请输入应用编码" clearable />
        </el-form-item>
        <el-form-item label="应用类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="Web应用" value="web" />
            <el-option label="移动应用" value="mobile" />
            <el-option label="API服务" value="api" />
            <el-option label="桌面应用" value="desktop" />
          </el-select>
        </el-form-item>
        <el-form-item label="应用状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="已启用" value="1" />
            <el-option label="停用" value="2" />
            <el-option label="未启用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column label="应用名称" min-width="260">
          <template #default="{ row }">
            <div class="app-name-cell">
              <div class="app-icon" :class="row.typeClass">
                <el-icon><component :is="getAppIcon(row.type)" /></el-icon>
              </div>
              <div class="app-info">
                <strong>{{ row.name }}</strong>
                <span>{{ row.description }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="应用编码" min-width="150" />
        <el-table-column label="应用类型" min-width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :class="'tag-' + row.typeClass">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="client" label="应用客户端" min-width="150" />
        <el-table-column label="应用状态" width="100">
          <template #default="{ row }">
            <div class="status-cell" :class="getStatusClass(row.status)">
              <span class="dot"></span> {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="router.push(`/applications/detail/${row.id}`)">详情</el-button>
              <el-button link type="primary">编辑</el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>停用应用</el-dropdown-item>
                    <el-dropdown-item>配置实例</el-dropdown-item>
                    <el-dropdown-item divided type="danger">删除应用</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <span class="total-text">共 18 条</span>
        <el-pagination
          layout="sizes, prev, pager, next, jumper"
          :total="18"
          :page-sizes="[10, 20, 50]"
          :default-page-size="10"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.application-list-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.header-left p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.filter-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  margin-bottom: 24px;
  padding: 24px 24px 6px 24px;
}

.app-filter-form :deep(.el-form-item) {
  margin-bottom: 18px;
  margin-right: 24px;
}

.app-filter-form :deep(.el-form-item__label) {
  font-weight: 400;
  color: #4b5563;
}

.form-actions {
  width: 100%;
  margin-top: 4px;
  display: flex;
}

.tabs-wrapper {
  margin-bottom: 16px;
}

.app-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eaeaea;
}

.app-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  color: #4b5563;
  padding: 0 24px;
}

.app-tabs :deep(.el-tabs__item.is-active) {
  color: #2563eb;
  font-weight: 600;
}

.table-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.app-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.app-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  color: #ffffff;
}

.bg-blue { background-color: #3b82f6; }
.bg-green { background-color: #10b981; }
.bg-purple { background-color: #8b5cf6; }
.bg-orange { background-color: #f97316; }
.bg-teal { background-color: #14b8a6; }
.bg-gray { background-color: #64748b; }

.tag-bg-blue { color: #3b82f6; border-color: #bfdbfe; background-color: #eff6ff; }
.tag-bg-green { color: #10b981; border-color: #bbf7d0; background-color: #f0fdf4; }
.tag-bg-purple { color: #8b5cf6; border-color: #ddd6fe; background-color: #f5f3ff; }
.tag-bg-orange { color: #f97316; border-color: #fed7aa; background-color: #fff7ed; }
.tag-bg-teal { color: #14b8a6; border-color: #99f6e4; background-color: #f0fdfa; }
.tag-bg-gray { color: #64748b; border-color: #cbd5e1; background-color: #f8fafc; }

.app-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.app-info strong {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.app-info span {
  font-size: 12px;
  color: #6b7280;
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
.text-orange { color: #ea580c; }
.text-gray { color: #9ca3af; }

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-top: 1px solid #eaeaea;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}
</style>
