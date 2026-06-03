<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

import { accountApi, type AccountRecord, type AccountSubjectRecord } from '@/api/account'
import { subjectMemberApi, MEMBER_STATUS_ENABLED, MEMBER_TYPE_ADMIN, MEMBER_TYPE_MEMBER, MEMBER_TYPE_OWNER, MEMBER_TYPE_STAFF } from '@/api/subjectMember'

const props = defineProps<{
  account: AccountRecord
}>()

const subjectList = ref<AccountSubjectRecord[]>([])
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)

const loadSubjects = async () => {
  loading.value = true
  try {
    subjectList.value = await accountApi.subjects(props.account.id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加入主体加载失败')
  } finally {
    loading.value = false
  }
}

const handleCommand = (command: string, row: AccountSubjectRecord) => {
  if (command === 'disable') {
    ElMessageBox.confirm(`确定要禁用该成员吗？`, '提示', { type: 'warning' }).then(async () => {
      await subjectMemberApi.disable(row.memberId)
      ElMessage.success('已禁用')
      loadSubjects()
    }).catch(() => {})
  } else if (command === 'enable') {
    ElMessageBox.confirm(`确定要启用该成员吗？`, '提示', { type: 'success' }).then(async () => {
      await subjectMemberApi.enable(row.memberId)
      ElMessage.success('已启用')
      loadSubjects()
    }).catch(() => {})
  } else if (command === 'remove') {
    ElMessageBox.confirm(`确定要移除该主体绑定吗？`, '提示', { type: 'warning' }).then(async () => {
      await subjectMemberApi.remove(row.memberId)
      ElMessage.success('已移除')
      loadSubjects()
    }).catch(() => {})
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

onMounted(loadSubjects)
</script>

<template>
  <div class="account-subject-tab-container">
    <div class="split-layout">
      <!-- 左侧用户信息卡片 -->
      <div class="left-col">
        <div class="user-card">
          <div class="user-header">
            <el-avatar :size="48" class="member-avatar">{{ account?.nickname?.substring(0, 1) || 'U' }}</el-avatar>
            <div class="user-name">
              <strong>{{ account?.nickname }} ({{ account?.username }})</strong>
              <el-tag :type="account?.status === 3 ? 'danger' : 'success'" effect="light" size="small" class="mt-1">
                {{ account?.status === 3 ? '禁用' : account?.status === 2 ? '锁定' : '启用' }}
              </el-tag>
            </div>
          </div>
          <div class="user-info-list">
            <div class="info-item"><span>用户名</span><strong>{{ account?.username }}</strong></div>
            <div class="info-item"><span>手机号</span><strong>{{ account?.mobile || '-' }}</strong></div>
            <div class="info-item"><span>邮箱</span><strong>{{ account?.email || '-' }}</strong></div>
            <div class="info-item"><span>所属身份域</span><strong>{{ account?.realmName || '-' }}</strong></div>
          </div>
        </div>
      </div>

      <!-- 右侧主体列表 -->
      <div class="right-col">
        <el-table v-loading="loading" :data="subjectList" style="width: 100%" class="subject-table">
          <el-table-column prop="subjectName" label="主体名称" min-width="120" />
          <el-table-column prop="subjectCode" label="主体标识" min-width="140" />
          <el-table-column prop="subjectTypeName" label="主体类型" width="100" />
          <el-table-column prop="rootSubjectName" label="数据隔离根" min-width="180" />
          <el-table-column label="成员类型" width="90">
            <template #default="{ row }">
              <span class="text-xs font-medium text-gray-500">{{ getMemberTypeName(row.memberType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="成员状态" width="80">
            <template #default="{ row }">
              <span class="status-text" :class="row.memberStatus === MEMBER_STATUS_ENABLED ? 'text-green' : 'text-red'">
                {{ row.memberStatus === MEMBER_STATUS_ENABLED ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="主体状态" width="80">
            <template #default="{ row }">
              <span class="status-text text-green" v-if="row.subjectStatus === 1">启用</span>
              <span class="status-text text-red" v-else>禁用</span>
            </template>
          </el-table-column>
          <el-table-column prop="joinedAt" label="加入时间" width="160" class-name="text-secondary text-sm" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary">查看主体</el-button>
              <el-button link type="primary" @click="handleCommand('disable', row)" v-if="row.memberStatus === MEMBER_STATUS_ENABLED">禁用</el-button>
              <el-button link type="primary" @click="handleCommand('enable', row)" v-else>启用</el-button>
              <el-button link type="primary" @click="handleCommand('remove', row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <span class="total-text">共 {{ subjectList.length }} 条</span>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            layout="sizes, prev, pager, next"
            :total="subjectList.length"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.account-subject-tab-container {
  padding-top: 8px;
}

.split-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.left-col {
  width: 300px;
  flex-shrink: 0;
}

.right-col {
  flex-grow: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eef2f7;
  padding: 16px;
}

.user-card {
  background: #fdfdfe;
  border-radius: 8px;
  border: 1px solid #eef2f7;
  padding: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.member-avatar {
  background-color: #e0e7ff;
  color: #4f46e5;
  font-weight: 600;
  font-size: 20px;
}

.user-name {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name strong {
  font-size: 16px;
  color: #111827;
}

.mt-1 { margin-top: 4px; }

.user-info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 13px;
  gap: 16px;
}

.info-item span {
  color: #6b7280;
  flex-shrink: 0;
}

.info-item strong {
  color: #111827;
  font-weight: 500;
  text-align: right;
  word-break: break-word;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}

/* Utilities */
.text-green { color: #16a34a; font-weight: 500; }
.text-red { color: #dc2626; font-weight: 500; }
.text-secondary { color: #6b7280; }
.text-sm { font-size: 13px; }
.text-xs { font-size: 12px; }
.font-medium { font-weight: 500; }
.text-gray-500 { color: #6b7280; }
</style>
