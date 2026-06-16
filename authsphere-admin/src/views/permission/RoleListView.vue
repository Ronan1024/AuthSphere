<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Search, Refresh, Setting, ArrowDown, Edit, Delete, Check } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

import { roleApi, type RoleRecord, type RolePayload, type RoleUpdatePayload } from '@/api/role'
import { appClientApi, type AppClientRecord } from '@/api/appClient'
import { realmApi, type RealmRecord } from '@/api/realm'
import { subjectApi, type SubjectRecord } from '@/api/subject'
import { menuApi, type AppMenuRecord } from '@/api/menu'
import { appPermissionApi, type AppPermissionRecord } from '@/api/appPermission'

// State variables
const loading = ref(false)
const tableData = ref<RoleRecord[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  realmId: undefined as string | number | undefined,
  subjectId: undefined as string | number | undefined,
  clientId: undefined as string | number | undefined,
  keyword: '',
  status: undefined as number | undefined,
  roleType: ''
})

// Option lists for dropdowns
const clientOptions = ref<AppClientRecord[]>([])
const realmOptions = ref<RealmRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])

// Dialog states for Create/Edit Role
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const dialogMode = ref<'create' | 'edit'>('create')
const submitLoading = ref(false)
const formRef = ref()

const formModel = reactive({
  id: '',
  realmId: '' as string | number,
  subjectId: undefined as string | number | undefined | null,
  clientId: '' as string | number,
  roleName: '',
  roleCode: '',
  roleType: 'custom',
  dataScope: 'current_subject',
  status: 1,
  remark: ''
})

const formRules = {
  realmId: [{ required: true, message: '请选择所属身份域', trigger: 'change' }],
  clientId: [{ required: true, message: '请选择所属客户端', trigger: 'change' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_:-]+$/, message: '编码仅支持字母、数字、下划线、中划线和冒号', trigger: 'blur' }
  ],
  roleType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// Drawer states for Role Resource Authorization
const authDrawerVisible = ref(false)
const authLoading = ref(false)
const currentAuthRole = ref<RoleRecord | null>(null)
const resourceTreeData = ref<any[]>([])
const checkedKeys = ref<any[]>([])
const treeRef = ref()

// Fetch options
const loadOptions = async () => {
  try {
    const clientsResult = await appClientApi.page({ page: 1, size: 1000 })
    clientOptions.value = clientsResult.records || []

    const realmsResult = await realmApi.page({ page: 1, size: 1000 })
    realmOptions.value = realmsResult.records || []

    const subjectsResult = await subjectApi.list()
    subjectOptions.value = Array.isArray(subjectsResult) ? subjectsResult : []
  } catch (error: any) {
    console.error('加载筛选选项失败', error)
  }
}

// Fetch lists
const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size
    }
    if (query.realmId !== '' && query.realmId !== undefined && query.realmId !== null) params.realmId = query.realmId
    if (query.subjectId !== '' && query.subjectId !== undefined && query.subjectId !== null) params.subjectId = query.subjectId
    if (query.clientId !== '' && query.clientId !== undefined && query.clientId !== null) params.clientId = query.clientId
    if (typeof query.status === 'number') params.status = query.status
    if (query.roleType !== '' && query.roleType !== undefined && query.roleType !== null) params.roleType = query.roleType
    if (query.keyword && query.keyword.trim() !== '') params.keyword = query.keyword.trim()

    const res = await roleApi.page(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleReset = () => {
  query.realmId = undefined
  query.subjectId = undefined
  query.clientId = undefined
  query.keyword = ''
  query.status = undefined
  query.roleType = ''
  handleSearch()
}

// Create/Edit Handlers
const openCreateDialog = () => {
  dialogMode.value = 'create'
  dialogTitle.value = '新增角色'
  
  // Set defaults
  formModel.id = ''
  formModel.realmId = realmOptions.value[0]?.id || ''
  formModel.subjectId = undefined
  formModel.clientId = clientOptions.value[0]?.id || ''
  formModel.roleName = ''
  formModel.roleCode = ''
  formModel.roleType = 'custom'
  formModel.dataScope = 'current_subject'
  formModel.status = 1
  formModel.remark = ''
  
  dialogVisible.value = true
}

const openEditDialog = (row: RoleRecord) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑角色'
  
  formModel.id = row.id
  formModel.realmId = row.realmId
  formModel.subjectId = row.subjectId || null
  formModel.clientId = row.clientId
  formModel.roleName = row.roleName
  formModel.roleCode = row.roleCode
  formModel.roleType = row.roleType
  formModel.dataScope = row.dataScope
  formModel.status = row.status
  formModel.remark = row.remark || ''
  
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      const payload: RolePayload = {
        realmId: formModel.realmId,
        subjectId: formModel.subjectId || null,
        clientId: formModel.clientId,
        roleName: formModel.roleName,
        roleCode: formModel.roleCode,
        roleType: formModel.roleType,
        dataScope: formModel.dataScope,
        status: formModel.status,
        remark: formModel.remark || undefined
      }
      await roleApi.create(payload)
      ElMessage.success('角色创建成功')
    } else {
      const payload: RoleUpdatePayload = {
        roleName: formModel.roleName,
        dataScope: formModel.dataScope,
        status: formModel.status,
        remark: formModel.remark || undefined
      }
      await roleApi.update(formModel.id, payload)
      ElMessage.success('角色修改成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '保存角色失败')
  } finally {
    submitLoading.value = false
  }
}

// Enable/Disable toggle
const handleToggleStatus = async (row: RoleRecord) => {
  const isNormal = row.status === 1
  const action = isNormal ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认要${action}角色「${row.roleName}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    if (isNormal) {
      await roleApi.disable(row.id)
    } else {
      await roleApi.enable(row.id)
    }
    ElMessage.success(`角色已${action}`)
    fetchData()
  } catch (error: any) {
    if (error === 'cancel') return
    ElMessage.error(error.message || '操作失败')
  }
}

// Delete Role
const handleDelete = async (row: RoleRecord) => {
  try {
    await ElMessageBox.confirm(
      `确认删除角色「${row.roleName}」吗？系统角色、启用中或仍绑定账号的角色将无法被删除。`, 
      '警告', 
      {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    await roleApi.delete(row.id)
    ElMessage.success('角色删除成功')
    fetchData()
  } catch (error: any) {
    if (error === 'cancel') return
    ElMessage.error(error.message || '删除角色失败，请先确认角色已禁用且无账号绑定关系。')
  }
}

// Resource Tree Assembly for Authorization
const openAuthDrawer = async (row: RoleRecord) => {
  currentAuthRole.value = row
  authDrawerVisible.value = true
  authLoading.value = true
  checkedKeys.value = []
  resourceTreeData.value = []

  try {
    // 1. Fetch menus and permissions
    const menus = await menuApi.listByClient(row.clientId)
    const permissions = await appPermissionApi.listByClient(row.clientId)
    const activeAuth = await roleApi.listResources(row.id)
    
    // 2. Build Tree Structure
    const menuMap = new Map<string, any>()
    
    // Convert menus
    menus.forEach((m: AppMenuRecord) => {
      menuMap.set(m.id, {
        id: `m_${m.id}`,
        realId: m.id,
        label: m.menuName,
        type: 'menu',
        children: []
      })
    })

    // Group permissions under menuId
    const unmappedPermissions: any[] = []
    permissions.forEach((p: AppPermissionRecord) => {
      const node = {
        id: `p_${p.id}`,
        realId: p.id,
        label: `${p.permissionName} (${p.permissionCode})`,
        type: 'permission',
        permissionType: p.permissionType
      }
      if (p.menuId && menuMap.has(p.menuId)) {
        menuMap.get(p.menuId).children.push(node)
      } else {
        unmappedPermissions.push(node)
      }
    })

    // Establish parent-child relationship for menus
    const rootNodes: any[] = []
    menus.forEach((m: AppMenuRecord) => {
      const node = menuMap.get(m.id)
      if (m.parentId && menuMap.has(m.parentId)) {
        menuMap.get(m.parentId).children.push(node)
      } else {
        rootNodes.push(node)
      }
    })

    // Append unmapped virtual directory if there are orphans
    if (unmappedPermissions.length > 0) {
      rootNodes.push({
        id: 'virtual_unmapped',
        label: '未分类权限/独立接口',
        type: 'menu',
        children: unmappedPermissions
      })
    }

    resourceTreeData.value = rootNodes

    // Set selected leaf/checked keys
    const selectedIds = activeAuth.resourceIds || []
    const selectedKeysList: string[] = []
    
    // Note: To avoid element tree auto-selecting parent nodes when children are not all selected,
    // we should only set checked keys for leaf nodes (permissions, or menus without children).
    const isLeaf = (node: any): boolean => {
      return !node.children || node.children.length === 0
    }

    const traverse = (node: any) => {
      if (isLeaf(node)) {
        const checkId = node.id.startsWith('m_') || node.id.startsWith('p_') ? node.realId : null
        if (checkId && selectedIds.includes(String(checkId))) {
          selectedKeysList.push(node.id)
        }
      } else {
        node.children.forEach((c: any) => traverse(c))
      }
    }

    rootNodes.forEach(traverse)
    checkedKeys.value = selectedKeysList

  } catch (error: any) {
    ElMessage.error(error.message || '加载授权资源失败')
  } finally {
    authLoading.value = false
  }
}

const submitAuth = async () => {
  if (!currentAuthRole.value) return
  
  submitLoading.value = true
  try {
    // Collect all checked keys and half-checked keys to pass down to backend
    // Since tree uses mock node IDs with prefix, we strip prefix to get database ID
    const checked = treeRef.value?.getCheckedNodes(false, true) || []
    const resourceIds: string[] = checked
      .filter((node: any) => node.realId)
      .map((node: any) => String(node.realId))

    await roleApi.assignResources(currentAuthRole.value.id, { resourceIds })
    ElMessage.success('保存授权成功')
    authDrawerVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '保存授权失败')
  } finally {
    submitLoading.value = false
  }
}

// Mapping Display functions
const clientNameText = (clientId: string | number) => {
  const match = clientOptions.value.find(c => String(c.id) === String(clientId))
  return match ? match.clientName : `ID: ${clientId}`
}

const roleTypeText = (code: string) => {
  return code === 'system' ? '系统角色' : '自定义角色'
}

onMounted(() => {
  loadOptions()
  fetchData()
})
</script>

<template>
  <div class="role-page">
    <div class="page-heading">
      <div class="heading-text">
        <h1>角色管理</h1>
        <p>角色用于将客户端资源授权给账号，账号获得角色后即可访问对应的菜单、按钮和接口权限。</p>
      </div>
    </div>

    <!-- Filter Card -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">客户端</span>
            <el-select v-model="query.clientId" placeholder="全部客户端" clearable class="filter-select">
              <el-option v-for="c in clientOptions" :key="c.id" :label="c.clientName" :value="c.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">角色类型</span>
            <el-select v-model="query.roleType" placeholder="全部" clearable class="filter-select-sm">
              <el-option value="system" label="系统角色" />
              <el-option value="custom" label="自定义角色" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">状态</span>
            <el-select v-model="query.status" placeholder="全部" clearable class="filter-select-sm">
              <el-option :value="1" label="启用" />
              <el-option :value="2" label="禁用" />
            </el-select>
          </div>
          <div class="filter-item search-input-group">
            <span class="filter-label">关键词</span>
            <el-input 
              v-model="query.keyword" 
              placeholder="请输入角色名称或编码" 
              clearable 
              class="filter-search-input"
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="filter-buttons">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Toolbar & Table Card -->
    <el-card shadow="never" class="table-card mt-16">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" :icon="Plus" @click="openCreateDialog" class="btn-primary-custom">新增角色</el-button>
        </div>
        <div class="toolbar-right">
          <el-button :icon="Refresh" @click="fetchData" class="btn-refresh">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border class="role-table">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140">
          <template #default="{ row }">
            <code class="code-badge">{{ row.roleCode }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="roleType" label="角色类型" min-width="120">
          <template #default="{ row }">
            <span class="type-tag" :class="row.roleType === 'system' ? 'tag-system' : 'tag-custom'">
              {{ roleTypeText(row.roleType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="所属客户端" min-width="160">
          <template #default="{ row }">
            {{ clientNameText(row.clientId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-cell" :class="row.status === 1 ? 'status-enabled' : 'status-disabled'">
              <span class="dot"></span> {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="175" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openAuthDrawer(row)">授权</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleToggleStatus(row)">
                      {{ row.status === 1 ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item 
                      v-if="row.roleType !== 'system'" 
                      divided 
                      type="danger" 
                      @click="handleDelete(row)"
                      class="text-danger"
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <span class="total-text">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="580px"
      destroy-on-close
      class="role-form-dialog"
    >
      <el-form
        ref="formRef"
        :model="formModel"
        :rules="formRules"
        label-position="top"
        class="form-container"
      >
        <div class="form-row">
          <div class="form-col">
            <el-form-item label="所属身份域" prop="realmId">
              <el-select v-model="formModel.realmId" placeholder="选择身份域" style="width: 100%" :disabled="dialogMode === 'edit'">
                <el-option v-for="r in realmOptions" :key="r.id" :label="r.name" :value="r.id" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-col">
            <el-form-item label="所属客户端" prop="clientId">
              <el-select v-model="formModel.clientId" placeholder="选择客户端" style="width: 100%" :disabled="dialogMode === 'edit'">
                <el-option v-for="c in clientOptions" :key="c.id" :label="c.clientName" :value="c.id" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="form-row">
          <div class="form-col">
            <el-form-item label="角色类型" prop="roleType">
              <el-select v-model="formModel.roleType" placeholder="选择角色类型" style="width: 100%" :disabled="dialogMode === 'edit'">
                <el-option value="custom" label="自定义角色" />
                <el-option value="system" label="系统内置角色" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-col">
            <el-form-item label="所属主体 (可选)" prop="subjectId">
              <el-select v-model="formModel.subjectId" placeholder="选择主体" style="width: 100%" clearable :disabled="dialogMode === 'edit'">
                <el-option v-for="s in subjectOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="form-row">
          <div class="form-col">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="formModel.roleName" placeholder="例如: 商城客服人员" />
            </el-form-item>
          </div>
          <div class="form-col">
            <el-form-item label="角色唯一编码" prop="roleCode">
              <el-input v-model="formModel.roleCode" placeholder="例如: mall_service" :disabled="dialogMode === 'edit'" />
            </el-form-item>
          </div>
        </div>

        <div class="form-row">
          <div class="form-col">
            <el-form-item label="数据权限范围" prop="dataScope">
              <el-select v-model="formModel.dataScope" placeholder="选择数据权限范围" style="width: 100%">
                <el-option value="current_subject" label="当前主体" />
                <el-option value="all" label="全局数据" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-col">
            <el-form-item label="角色状态" prop="status">
              <el-radio-group v-model="formModel.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="2">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>

        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" :rows="3" placeholder="请输入角色的职责或特殊访问说明..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Resource Authorization Drawer -->
    <el-drawer
      v-model="authDrawerVisible"
      title="角色授权配置 (Scopes)"
      size="480px"
      destroy-on-close
      class="auth-config-drawer"
    >
      <div v-loading="authLoading" class="drawer-content">
        <div class="role-meta-info" v-if="currentAuthRole">
          <div class="meta-item">
            <span class="label">角色名称:</span>
            <span class="value">{{ currentAuthRole.roleName }}</span>
          </div>
          <div class="meta-item">
            <span class="label">所属客户端:</span>
            <span class="value">{{ clientNameText(currentAuthRole.clientId) }}</span>
          </div>
        </div>

        <div class="tree-container mt-16 flex-1 overflow-auto">
          <el-tree
            ref="treeRef"
            :data="resourceTreeData"
            show-checkbox
            node-key="id"
            default-expand-all
            :default-checked-keys="checkedKeys"
            :props="{ label: 'label', children: 'children' }"
            class="resource-auth-tree"
          >
            <template #default="{ node, data }">
              <div class="custom-tree-node">
                <span :class="data.type === 'permission' ? 'permission-node' : 'menu-node'">
                  {{ node.label }}
                </span>
                <span 
                  v-if="data.type === 'permission'" 
                  class="type-tag-sub"
                  :class="data.permissionType === 1 ? 'type-btn' : data.permissionType === 2 ? 'type-api' : 'type-data'"
                >
                  {{ data.permissionType === 1 ? '按钮' : data.permissionType === 2 ? '接口' : '数据' }}
                </span>
              </div>
            </template>
          </el-tree>
        </div>

        <div class="drawer-footer mt-16 pt-16">
          <el-button @click="authDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitAuth">保存授权</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.role-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-heading {
  margin-bottom: 24px;
}

.heading-text h1 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.heading-text p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.filter-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #475569;
  white-space: nowrap;
}

.filter-select {
  width: 180px;
}

.filter-select-sm {
  width: 120px;
}

.filter-search-input {
  width: 200px;
}

.filter-buttons {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.table-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.role-table {
  border: none;
}

.code-badge {
  font-family: monospace;
  background-color: #f1f5f9;
  color: #0f172a;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 12px;
}

.type-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}

.tag-system {
  background-color: #e0f2fe;
  color: #0369a1;
}

.tag-custom {
  background-color: #e2fbe8;
  color: #166534;
}

.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}

.status-cell .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #94a3b8;
}

.status-enabled {
  color: #16a34a;
}
.status-enabled .dot {
  background-color: #16a34a;
}

.status-disabled {
  color: #ef4444;
}
.status-disabled .dot {
  background-color: #ef4444;
}

.row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
}

.total-text {
  font-size: 13px;
  color: #64748b;
}

/* Form layouts */
.form-row {
  display: flex;
  gap: 20px;
}

.form-col {
  flex: 1;
}

/* Resource authorization drawer */
.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.role-meta-info {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-meta-info .meta-item {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.role-meta-info .meta-item .label {
  color: #64748b;
}

.role-meta-info .meta-item .value {
  color: #1e293b;
  font-weight: 500;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
  font-size: 13px;
}

.permission-node {
  color: #475569;
}

.menu-node {
  font-weight: 500;
  color: #1e293b;
}

.type-tag-sub {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.type-btn {
  background-color: #e0f2fe;
  color: #0369a1;
}

.type-api {
  background-color: #fef3c7;
  color: #d97706;
}

.type-data {
  background-color: #f3e8ff;
  color: #7e22ce;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e2e8f0;
}

.text-danger {
  color: #ef4444;
}

.mt-16 { margin-top: 16px; }
.pt-16 { padding-top: 16px; }
</style>
