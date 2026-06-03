<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus, InfoFilled, FolderOpened, Menu as MenuIcon, Warning, Search, RefreshLeft } from '@element-plus/icons-vue'

const query = reactive({
  name: '',
  code: '',
  parent: '',
  status: ''
})

const loading = ref(false)

// Tree Mock Data
const tableData = [
  {
    id: '1',
    name: '应用管理',
    code: 'app:manage',
    parentName: '根目录',
    routePath: '/app',
    componentPath: 'Layout',
    sort: 1,
    status: 1,
    isVisible: true,
    isBuiltIn: true,
    updateTime: '2024-05-20 16:45:00',
    isFolder: true,
    children: [
      {
        id: '11',
        name: '应用列表',
        code: 'app:list',
        parentName: '应用管理',
        routePath: '/app/list',
        componentPath: 'app/list/index.vue',
        sort: 1,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      },
      {
        id: '12',
        name: '新增应用',
        code: 'app:create',
        parentName: '应用管理',
        routePath: '/app/create',
        componentPath: 'app/create/index.vue',
        sort: 2,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      },
      {
        id: '13',
        name: '应用详情',
        code: 'app:detail',
        parentName: '应用管理',
        routePath: '/app/detail/:id',
        componentPath: 'app/detail/index.vue',
        sort: 3,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      }
    ]
  },
  {
    id: '2',
    name: '权限管理',
    code: 'permission:manage',
    parentName: '根目录',
    routePath: '/permission',
    componentPath: 'Layout',
    sort: 2,
    status: 1,
    isVisible: true,
    isBuiltIn: true,
    updateTime: '2024-05-20 16:45:00',
    isFolder: true,
    children: [
      {
        id: '21',
        name: '权限列表',
        code: 'permission:list',
        parentName: '权限管理',
        routePath: '/permission/list',
        componentPath: 'permission/list/index.vue',
        sort: 1,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      },
      {
        id: '22',
        name: '新增权限',
        code: 'permission:create',
        parentName: '权限管理',
        routePath: '/permission/create',
        componentPath: 'permission/create/index.vue',
        sort: 2,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      }
    ]
  },
  {
    id: '3',
    name: '菜单管理',
    code: 'menu:manage',
    parentName: '根目录',
    routePath: '/menu',
    componentPath: 'Layout',
    sort: 3,
    status: 1,
    isVisible: true,
    isBuiltIn: true,
    updateTime: '2024-05-20 16:45:00',
    isFolder: true,
    children: [
      {
        id: '31',
        name: '菜单列表',
        code: 'menu:resource',
        parentName: '菜单管理',
        routePath: '/menu/resource',
        componentPath: 'menu/resource/index.vue',
        sort: 1,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      },
      {
        id: '32',
        name: '新增菜单',
        code: 'menu:create',
        parentName: '菜单管理',
        routePath: '/menu/create',
        componentPath: 'menu/create/index.vue',
        sort: 2,
        status: 1,
        isVisible: true,
        isBuiltIn: true,
        updateTime: '2024-05-20 16:45:00',
        isFolder: false
      }
    ]
  }
]

// To make the tree expand by default, we capture the row keys
const expandRowKeys = ref(['1', '2', '3'])

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 300)
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.parent = ''
  query.status = ''
}
</script>

<template>
  <div class="menu-tab-container">
    <!-- Filter and Actions -->
    <div class="filter-action-bar">
      <div class="filter-form">
        <div class="filter-item">
          <label>菜单名称</label>
          <el-input v-model="query.name" placeholder="请输入菜单名称" clearable style="width: 150px" />
        </div>
        <div class="filter-item">
          <label>菜单编码</label>
          <el-input v-model="query.code" placeholder="请输入菜单编码" clearable style="width: 150px" />
        </div>
        <div class="filter-item">
          <label>父级菜单</label>
          <el-select v-model="query.parent" placeholder="请选择父级菜单" clearable style="width: 150px">
            <el-option label="根目录" value="root" />
            <el-option label="应用管理" value="app" />
            <el-option label="权限管理" value="permission" />
            <el-option label="菜单管理" value="menu" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>状态</label>
          <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 110px">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </div>
        <div class="filter-btn-group">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </div>
      </div>
      <div class="action-btn-group">
        <el-button type="primary" :icon="Plus">新增菜单</el-button>
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

    <!-- Tree Data Table -->
    <el-table 
      :data="tableData" 
      v-loading="loading" 
      style="width: 100%" 
      class="custom-tree-table" 
      row-key="id"
      :expand-row-keys="expandRowKeys"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      border
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="菜单名称" min-width="200">
        <template #default="{ row }">
          <div class="menu-name-cell">
            <el-icon class="folder-icon text-blue" v-if="row.isFolder"><FolderOpened /></el-icon>
            <el-icon class="list-icon text-gray" v-else><MenuIcon /></el-icon>
            <span class="node-name">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="菜单编码" min-width="140" />
      <el-table-column prop="parentName" label="上级菜单" min-width="100" />
      <el-table-column prop="routePath" label="路由地址" min-width="160" />
      <el-table-column prop="componentPath" label="组件路径" min-width="200" />
      
      <el-table-column label="排序" width="80" align="center">
        <template #header>
          <div class="th-with-icon">
            排序 <el-tooltip content="数值越小越靠前" placement="top"><el-icon><InfoFilled /></el-icon></el-tooltip>
          </div>
        </template>
        <template #default="{ row }">
          {{ row.sort }}
        </template>
      </el-table-column>
      
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <div class="status-cell text-green" v-if="row.status === 1">
            <span class="dot"></span> 启用
          </div>
          <div class="status-cell text-red" v-else>
            <span class="dot"></span> 禁用
          </div>
        </template>
      </el-table-column>

      <el-table-column label="是否可见" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.isVisible" class="text-green font-medium">可见</span>
          <span v-else class="text-gray font-medium">隐藏</span>
        </template>
      </el-table-column>

      <el-table-column label="是否内置" width="100" align="center">
        <template #default="{ row }">
          <span class="text-blue font-medium" v-if="row.isBuiltIn">是</span>
          <span v-else>否</span>
        </template>
      </el-table-column>

      <el-table-column prop="updateTime" label="更新时间" width="170" />
      
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

    <!-- Info Box -->
    <div class="info-box">
      <div class="info-icon">
        <el-icon><InfoFilled /></el-icon>
      </div>
      <div class="info-content-wrapper">
        <div class="info-left">
          <h4>菜单资源说明</h4>
          <p>菜单资源用于定义前端导航、页面入口与模块组织，支持树形层级与路由组件配置。可控制菜单的可见性、启用状态及排序，便于进行权限分配与界面管理。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-tab-container {
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

/* Tree Table Styles */
.custom-tree-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
}

.custom-tree-table :deep(th.el-table__cell) {
  background-color: #f9fafb;
  color: #111827;
  font-weight: 500;
  border-bottom: 1px solid #eaeaea;
}

.custom-tree-table :deep(td.el-table__cell) {
  border-bottom: 1px solid #eaeaea;
}

.menu-name-cell {
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
  gap: 8px;
  margin-left: 4px;
}

.custom-tree-table :deep(.el-table__expand-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
  height: auto;
}

.folder-icon {
  font-size: 16px;
}

.list-icon {
  font-size: 16px;
}

.node-name {
  font-size: 14px;
  color: #111827;
}

.text-blue { color: #3b82f6; }
.text-gray { color: #9ca3af; }
.text-green { color: #16a34a; }
.text-red { color: #ef4444; }
.font-medium { font-weight: 500; }

.th-with-icon {
  display: inline-flex;
  align-items: center;
  gap: 4px;
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
  flex-grow: 1;
}

.info-left {
  flex: 1;
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
</style>
