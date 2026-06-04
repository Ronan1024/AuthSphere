<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, InfoFilled, FolderOpened, Menu as MenuIcon, Warning, Search, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appClientApi, type AppClientRecord } from '@/api/appClient'
import { menuApi, type AppMenuRecord } from '@/api/menu'

const route = useRoute()

const query = reactive({
  name: '',
  code: '',
  parent: '',
  status: ''
})

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  parentId: '',
  menuName: '',
  menuCode: '',
  routePath: '',
  componentPath: '',
  sortNo: 0,
  visible: 1,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }]
}

// Tree Data
const tableData = ref<AppMenuRecord[]>([])
const clientList = ref<AppClientRecord[]>([])
const currentClientId = ref<string>('')

// To make the tree expand by default, we capture the row keys
const expandRowKeys = ref<string[]>([])

const fetchClients = async () => {
  const appId = route.params.id as string
  if (!appId) return
  
  try {
    const clients = await appClientApi.listByApp(appId)
    clientList.value = clients
    if (clients.length > 0) {
      currentClientId.value = clients[0].id
      fetchMenus()
    } else {
      tableData.value = []
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取客户端列表失败')
  }
}

const buildTree = (menus: AppMenuRecord[]): AppMenuRecord[] => {
  const map = new Map<string, AppMenuRecord>()
  const tree: AppMenuRecord[] = []
  
  menus.forEach(menu => {
    map.set(menu.id, { ...menu, children: [] })
  })
  
  menus.forEach(menu => {
    const node = map.get(menu.id)!
    if (menu.parentId && menu.parentId !== '0' && map.has(menu.parentId)) {
      map.get(menu.parentId)!.children!.push(node)
    } else {
      tree.push(node)
    }
  })
  return tree
}

const fetchMenus = async () => {
  if (!currentClientId.value) return
  loading.value = true
  try {
    const data = await menuApi.listByClient(currentClientId.value)
    tableData.value = buildTree(data)
    expandRowKeys.value = data.map(m => m.id)
  } catch (error: any) {
    ElMessage.error(error.message || '获取菜单失败')
  } finally {
    loading.value = false
  }
}

const handleClientChange = () => {
  fetchMenus()
}

const handleOpenCreate = () => {
  if (!currentClientId.value) {
    ElMessage.warning('请先选择客户端')
    return
  }
  isEdit.value = false
  formData.id = ''
  formData.parentId = ''
  formData.menuName = ''
  formData.menuCode = ''
  formData.routePath = ''
  formData.componentPath = ''
  formData.sortNo = 0
  formData.visible = 1
  formData.status = 1
  dialogVisible.value = true
}

const handleOpenEdit = (row: AppMenuRecord) => {
  isEdit.value = true
  formData.id = row.id
  formData.parentId = row.parentId || ''
  formData.menuName = row.menuName
  formData.menuCode = row.menuCode
  formData.routePath = row.routePath || ''
  formData.componentPath = row.componentPath || ''
  formData.sortNo = row.sortNo
  formData.visible = row.visible
  formData.status = row.status
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {
      parentId: formData.parentId || undefined,
      menuName: formData.menuName,
      menuCode: formData.menuCode,
      routePath: formData.routePath || undefined,
      componentPath: formData.componentPath || undefined,
      sortNo: formData.sortNo,
      visible: formData.visible,
      status: formData.status
    }
    if (isEdit.value) {
      await menuApi.edit(formData.id, payload)
      ElMessage.success('菜单修改成功')
    } else {
      await menuApi.create(currentClientId.value, payload)
      ElMessage.success('菜单创建成功')
    }
    dialogVisible.value = false
    fetchMenus()
  } catch (error: any) {
    ElMessage.error(error.message || (isEdit.value ? '修改失败' : '创建失败'))
  } finally {
    submitLoading.value = false
  }
}

const handleStatusToggle = async (row: AppMenuRecord) => {
  try {
    if (row.status === 1) {
      await menuApi.disable(row.id)
      ElMessage.success('禁用成功')
    } else {
      await menuApi.enable(row.id)
      ElMessage.success('启用成功')
    }
    fetchMenus()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleSearch = () => {
  // Client side filtering since tree data is usually small and nested
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.status = ''
}

onMounted(() => {
  fetchClients()
})
</script>

<template>
  <div class="menu-tab-container">
    <!-- Filter and Actions -->
    <div class="filter-action-bar">
      <div class="filter-form">
        <div class="filter-item">
          <label>所属客户端</label>
          <el-select v-model="currentClientId" @change="handleClientChange" placeholder="请选择客户端" style="width: 200px">
            <el-option
              v-for="item in clientList"
              :key="item.id"
              :label="item.clientName"
              :value="item.id"
            />
          </el-select>
        </div>
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
        <el-button type="primary" :icon="Plus" @click="handleOpenCreate">新增菜单</el-button>
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
            <el-icon class="folder-icon text-blue" v-if="row.children && row.children.length > 0"><FolderOpened /></el-icon>
            <el-icon class="list-icon text-gray" v-else><MenuIcon /></el-icon>
            <span class="node-name">{{ row.menuName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="menuCode" label="菜单编码" min-width="140" />
      <el-table-column prop="routePath" label="路由地址" min-width="160" />
      <el-table-column prop="componentPath" label="组件路径" min-width="200" />
      
      <el-table-column label="排序" width="80" align="center">
        <template #header>
          <div class="th-with-icon">
            排序 <el-tooltip content="数值越小越靠前" placement="top"><el-icon><InfoFilled /></el-icon></el-tooltip>
          </div>
        </template>
        <template #default="{ row }">
          {{ row.sortNo }}
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
          <span v-if="row.visible === 1" class="text-green font-medium">可见</span>
          <span v-else class="text-gray font-medium">隐藏</span>
        </template>
      </el-table-column>

      <el-table-column label="是否内置" width="100" align="center">
        <template #default="{ row }">
          <span class="text-blue font-medium" v-if="row.builtIn === 1">是</span>
          <span v-else>否</span>
        </template>
      </el-table-column>

      <el-table-column prop="updateTime" label="更新时间" width="170" />
      
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="handleOpenEdit(row)">编辑</el-button>
            <span class="divider"></span>
            <el-button link type="danger" v-if="row.status === 1" @click="handleStatusToggle(row)">禁用</el-button>
            <el-button link type="primary" v-else @click="handleStatusToggle(row)">启用</el-button>
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

    <!-- Create/Edit Drawer -->
    <el-drawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      size="500px"
      :destroy-on-close="true"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" label-position="top">
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="tableData"
            :props="{ label: 'menuName', children: 'children' }"
            node-key="id"
            placeholder="请选择父级菜单 (不选默认为根菜单)"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="例如：用户管理" />
        </el-form-item>
        <el-form-item label="菜单编码" prop="menuCode">
          <el-input v-model="formData.menuCode" placeholder="例如：user:manage" />
        </el-form-item>
        <el-form-item label="路由地址" prop="routePath">
          <el-input v-model="formData.routePath" placeholder="例如：/system/user" />
        </el-form-item>
        <el-form-item label="组件路径" prop="componentPath">
          <el-input v-model="formData.componentPath" placeholder="例如：system/user/index.vue" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortNo">
              <el-input-number v-model="formData.sortNo" :min="0" :max="9999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否可见" prop="visible">
              <el-switch v-model="formData.visible" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="初始状态" prop="status" v-if="!isEdit">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-drawer>
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
