<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus, Document } from '@element-plus/icons-vue'

const query = reactive({
  name: '',
  code: '',
  menu: '',
  status: ''
})

const loading = ref(false)

const tableData = [
  {
    id: '1',
    name: '查看应用',
    code: 'app:view',
    menu: '应用管理',
    type: 'BUTTON',
    status: 1, // 1 for 启用
    isBuiltIn: true,
    description: '查看应用详情',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '2',
    name: '新增应用',
    code: 'app:create',
    menu: '应用管理',
    type: 'BUTTON',
    status: 1,
    isBuiltIn: true,
    description: '创建应用',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '3',
    name: '编辑应用',
    code: 'app:update',
    menu: '应用管理',
    type: 'BUTTON',
    status: 1,
    isBuiltIn: true,
    description: '修改应用信息',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '4',
    name: '删除应用',
    code: 'app:delete',
    menu: '应用管理',
    type: 'BUTTON',
    status: 0, // 0 for 禁用
    isBuiltIn: false,
    description: '删除应用',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '5',
    name: '查看权限',
    code: 'permission:view',
    menu: '权限资源',
    type: 'BUTTON',
    status: 1,
    isBuiltIn: true,
    description: '查看权限列表',
    updateTime: '2024-05-20 16:45:00'
  },
  {
    id: '6',
    name: '新增权限',
    code: 'permission:create',
    menu: '权限资源',
    type: 'BUTTON',
    status: 1,
    isBuiltIn: false,
    description: '新增权限资源',
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
  query.code = ''
  query.menu = ''
  query.status = ''
}
</script>

<template>
  <div class="permission-tab-container">
    <!-- Filter and Actions -->
    <div class="filter-action-bar">
      <div class="filter-form">
        <div class="filter-item">
          <label>权限名称</label>
          <el-input v-model="query.name" placeholder="请输入权限名称" clearable style="width: 160px" />
        </div>
        <div class="filter-item">
          <label>权限编码</label>
          <el-input v-model="query.code" placeholder="请输入权限编码" clearable style="width: 160px" />
        </div>
        <div class="filter-item">
          <label>所属菜单</label>
          <el-select v-model="query.menu" placeholder="请选择所属菜单" clearable style="width: 160px">
            <el-option label="应用管理" value="app" />
            <el-option label="权限资源" value="permission" />
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
        <el-button type="primary" :icon="Plus">新增权限</el-button>
        <el-button plain class="btn-batch-enable">
          <template #icon>
            <div class="icon-circle icon-circle-blue">
              <span class="dot"></span>
            </div>
          </template>
          批量启用
        </el-button>
        <el-button type="danger" plain class="btn-batch-disable">
          <template #icon>
            <div class="icon-circle icon-circle-red">
              <span class="line"></span>
            </div>
          </template>
          批量禁用
        </el-button>
      </div>
    </div>

    <!-- Data Table -->
    <el-table :data="tableData" v-loading="loading" style="width: 100%" class="custom-table" border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="name" label="权限名称" min-width="120" />
      <el-table-column prop="code" label="权限编码" min-width="150" />
      <el-table-column prop="menu" label="所属菜单" min-width="120" />
      <el-table-column label="权限类型" min-width="100">
        <template #default="{ row }">
          <span class="type-tag">{{ row.type }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="80">
        <template #default="{ row }">
          <span v-if="row.status === 1" class="status-tag status-green">启用</span>
          <span v-else class="status-tag status-red">禁用</span>
        </template>
      </el-table-column>
      <el-table-column label="是否内置" min-width="100">
        <template #default="{ row }">
          {{ row.isBuiltIn ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="150" />
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary">详情</el-button>
            <span class="divider"></span>
            <el-button link type="primary">编辑</el-button>
            <span class="divider"></span>
            <el-button link type="danger" v-if="row.status === 1">禁用</el-button>
            <el-button link type="primary" v-else>启用</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination-container">
      <span class="total-text">共 36 条</span>
      <el-pagination
        background
        layout="sizes, prev, pager, next, jumper"
        :total="36"
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
          <h4>资源说明</h4>
          <p>权限资源用于定义系统中的操作权限（如按钮权限、接口权限等），并与角色进行绑定，实现精细化的权限控制。</p>
        </div>
        <div class="info-right">
          <ul>
            <li>
              权限类型说明：
              <span class="type-badge">BUTTON: 按钮权限</span>
              <span class="type-badge">MENU: 菜单权限</span>
              <span class="type-badge">API: 接口权限</span>
              <span class="type-badge">DATA: 数据权限</span>
            </li>
            <li>内置权限由系统默认提供，建议谨慎修改；自定义权限可根据业务需要灵活扩展。</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.permission-tab-container {
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

.icon-circle {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid currentColor;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle .dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: currentColor;
}

.icon-circle .line {
  width: 6px;
  height: 2px;
  background-color: currentColor;
  border-radius: 1px;
}

.icon-circle-blue { color: #3b82f6; }
.icon-circle-red { color: #ef4444; }

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
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-green {
  background-color: #dcfce7;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.status-red {
  background-color: #fee2e2;
  color: #ef4444;
  border: 1px solid #fecaca;
}

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
