<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

import {
  MEMBER_STATUS_DISABLED,
  MEMBER_STATUS_ENABLED,
  MEMBER_TYPE_ADMIN,
  MEMBER_TYPE_MEMBER,
  MEMBER_TYPE_OWNER,
  MEMBER_TYPE_STAFF,
  type SubjectMemberRecord,
  subjectMemberApi,
} from '@/api/subjectMember'

import AddExistingMemberDialog from './AddExistingMemberDialog.vue'
import AddNewAccountMemberDialog from './AddNewAccountMemberDialog.vue'
import EditMemberDialog from './EditMemberDialog.vue'
import MemberDetailDrawer from './MemberDetailDrawer.vue'

const props = defineProps<{
  subjectId: string
  realmCode?: string
}>()

const query = reactive({
  keyword: '',
  memberType: undefined as number | undefined,
  memberStatus: undefined as number | undefined,
  accountStatus: undefined as number | undefined,
  lockStatus: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const tableData = ref<SubjectMemberRecord[]>([])
const selectedMembers = ref<SubjectMemberRecord[]>([])

const addExistingRef = ref()
const addNewAccountRef = ref()
const editMemberRef = ref()
const memberDetailRef = ref()

const normalizeMember = (member: SubjectMemberRecord) => ({
  ...member,
  displayName: member.displayName || member.username,
  mobile: maskMobile(member.mobile),
  email: maskEmail(member.email),
})

const loadMembers = async () => {
  loading.value = true
  try {
    const result = await subjectMemberApi.page({
      page: currentPage.value,
      size: pageSize.value,
      subjectId: props.subjectId,
      keyword: query.keyword || undefined,
      memberType: query.memberType,
      memberStatus: query.memberStatus,
      accountStatus: query.lockStatus === 'locked' ? 3 : query.accountStatus,
    })
    tableData.value = (result.records ?? []).map(normalizeMember)
    total.value = Number(result.total ?? 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '成员列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadMembers()
}

const resetQuery = () => {
  query.keyword = ''
  query.memberType = undefined
  query.memberStatus = undefined
  query.accountStatus = undefined
  query.lockStatus = ''
  handleSearch()
}

const handleSelectionChange = (val: SubjectMemberRecord[]) => {
  selectedMembers.value = val
}

const openAddExisting = () => addExistingRef.value?.open(props.subjectId, props.realmCode)
const openAddNewAccount = () => addNewAccountRef.value?.open(props.subjectId)

const openDetail = (row: SubjectMemberRecord) => memberDetailRef.value?.open(row)
const openEdit = (row: SubjectMemberRecord) => editMemberRef.value?.open(row)

const handleCommand = (command: string, row: SubjectMemberRecord) => {
  if (command === 'disable') {
    ElMessageBox.confirm(`确定要禁用成员「${row.displayName}」吗？禁用后，该成员将无法以当前主体身份登录和访问系统。`, '禁用成员', {
      confirmButtonText: '确定禁用',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning'
    }).then(async () => {
      await subjectMemberApi.disable(row.id)
      ElMessage.success('已禁用成员')
      loadMembers()
    }).catch(() => {})
  } else if (command === 'enable') {
    ElMessageBox.confirm(`确定要启用成员「${row.displayName}」吗？启用后，该成员可以以当前主体身份登录和访问系统。`, '启用成员', {
      confirmButtonText: '确定启用',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--primary',
      type: 'success'
    }).then(async () => {
      await subjectMemberApi.enable(row.id)
      ElMessage.success('已启用成员')
      loadMembers()
    }).catch(() => {})
  } else if (command === 'remove') {
    ElMessageBox.confirm(`确定要移除成员「${row.displayName}」吗？移除后，该成员将不再属于当前主体，且无法恢复。`, '移除成员', {
      confirmButtonText: '确定移除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning'
    }).then(async () => {
      await subjectMemberApi.remove(row.id)
      ElMessage.success('已移除成员')
      loadMembers()
    }).catch(() => {})
  }
}

const getMemberTypeTagType = (type: number) => {
  switch (type) {
    case MEMBER_TYPE_OWNER: return 'warning'
    case MEMBER_TYPE_ADMIN: return 'primary'
    case MEMBER_TYPE_MEMBER: return 'success'
    case MEMBER_TYPE_STAFF: return 'info'
    default: return 'info'
  }
}

const getMemberTypeName = (type: number) => {
  switch (type) {
    case MEMBER_TYPE_OWNER: return 'OWNER'
    case MEMBER_TYPE_ADMIN: return 'ADMIN'
    case MEMBER_TYPE_MEMBER: return 'MEMBER'
    case MEMBER_TYPE_STAFF: return 'STAFF'
    default: return '-'
  }
}

const getStatusText = (status: number) => {
  if (status === MEMBER_STATUS_ENABLED) return '启用'
  if (status === MEMBER_STATUS_DISABLED) return '禁用'
  return '移除'
}

const maskMobile = (mobile?: string) => {
  if (!mobile || mobile.length < 7) return mobile || '-'
  return `${mobile.slice(0, 3)}****${mobile.slice(-4)}`
}

const maskEmail = (email?: string) => {
  if (!email) return '-'
  const [name, domain] = email.split('@')
  if (!domain) return email
  return `${name.slice(0, 2)}***@${domain}`
}

watch([currentPage, pageSize], loadMembers)
watch(() => props.subjectId, handleSearch)
onMounted(loadMembers)
</script>

<template>
  <div class="subject-members-container">
    <div class="filter-bar mb-4">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="请输入账号/手机号/邮箱/昵称" clearable style="width: 240px">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="成员类型">
          <el-select v-model="query.memberType" placeholder="全部" clearable style="width: 120px">
            <el-option label="OWNER" :value="MEMBER_TYPE_OWNER" />
            <el-option label="ADMIN" :value="MEMBER_TYPE_ADMIN" />
            <el-option label="MEMBER" :value="MEMBER_TYPE_MEMBER" />
            <el-option label="STAFF" :value="MEMBER_TYPE_STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="成员状态">
          <el-select v-model="query.memberStatus" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="MEMBER_STATUS_ENABLED" />
            <el-option label="禁用" :value="MEMBER_STATUS_DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="query.accountStatus" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否锁定">
          <el-select v-model="query.lockStatus" placeholder="全部" clearable style="width: 100px">
            <el-option label="正常" value="normal" />
            <el-option label="锁定" value="locked" />
          </el-select>
        </el-form-item>
        <el-form-item class="action-item">
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar mb-4">
      <el-button plain @click="openAddExisting">添加已有账号</el-button>
      <el-button type="primary" @click="openAddNewAccount">新建账号并加入</el-button>
      <el-dropdown trigger="click" class="ml-2" :disabled="selectedMembers.length === 0">
        <el-button>批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item type="danger">批量移除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-table v-loading="loading" :data="tableData" style="width: 100%" @selection-change="handleSelectionChange" class="mb-4">
      <el-table-column type="selection" width="55" />
      <el-table-column label="成员显示名" min-width="160">
        <template #default="{ row }">
          <div class="flex-center">
            <el-avatar :size="24" class="mr-2" style="background-color: #e0e7ff; color: #4f46e5;">
              {{ row.displayName.substring(0, 1) }}
            </el-avatar>
            <span>{{ row.displayName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" min-width="100" />
      <el-table-column prop="mobile" label="手机号" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
      <el-table-column prop="realmName" label="所属身份域" min-width="130" show-overflow-tooltip />
      <el-table-column label="成员类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getMemberTypeTagType(row.memberType)" effect="plain" size="small">{{ getMemberTypeName(row.memberType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="成员状态" width="90">
        <template #default="{ row }">
          <span class="status-text" :class="row.memberStatus === MEMBER_STATUS_ENABLED ? 'text-green' : 'text-red'">
            {{ getStatusText(row.memberStatus) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.accountStatus === 1 ? 'success' : 'danger'" effect="light" size="small" round>
            {{ row.accountStatus === 1 ? '启用' : row.accountStatus === 3 ? '锁定' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="锁定状态" width="90">
        <template #default="{ row }">
          <span class="status-text">{{ row.accountStatus === 3 ? '锁定' : '正常' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="joinedAt" label="加入时间" width="160" class-name="text-secondary text-sm" />
      <el-table-column prop="joinedByUsername" label="添加人" width="120" class-name="text-secondary text-sm" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-dropdown @command="handleCommand($event, row)" trigger="click">
              <el-button link type="primary">
                更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.memberStatus === MEMBER_STATUS_ENABLED" command="disable">禁用成员</el-dropdown-item>
                  <el-dropdown-item v-else command="enable">启用成员</el-dropdown-item>
                  <el-dropdown-item divided type="danger" command="remove">移除成员</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <span class="total-text">共 {{ total }} 条</span>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="prev, pager, next, sizes, jumper"
        :total="total"
      />
    </div>

    <!-- Dialogs -->
    <AddExistingMemberDialog ref="addExistingRef" @success="handleSearch" />
    <AddNewAccountMemberDialog ref="addNewAccountRef" @success="handleSearch" />
    <EditMemberDialog ref="editMemberRef" @success="handleSearch" />
    <MemberDetailDrawer ref="memberDetailRef" @edit="openEdit" />
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  background: #fdfdfe;
  padding: 16px 20px 0;
  border-radius: 8px;
  border: 1px solid #eef2f7;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0 16px;
  width: 100%;
}

.action-item {
  margin-left: auto !important;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #eef2f7;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-buttons :deep(.el-button) {
  margin-left: 0;
}

/* Utilities */
.flex-center { display: flex; align-items: center; }
.mr-2 { margin-right: 8px; }
.ml-2 { margin-left: 8px; }
.mb-4 { margin-bottom: 16px; }
.text-green { color: #16a34a; font-weight: 500; }
.text-red { color: #dc2626; font-weight: 500; }
.text-secondary { color: #6b7280; }
.text-sm { font-size: 13px; }
</style>
