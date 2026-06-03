<script setup lang="ts">
import { ref } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

import {
  MEMBER_STATUS_ENABLED,
  MEMBER_TYPE_ADMIN,
  MEMBER_TYPE_MEMBER,
  MEMBER_TYPE_OWNER,
  MEMBER_TYPE_STAFF,
  type SubjectMemberRecord,
} from '@/api/subjectMember'

const visible = ref(false)
const currentMember = ref<SubjectMemberRecord | null>(null)

const emit = defineEmits(['edit'])

const open = (member: SubjectMemberRecord) => {
  currentMember.value = member
  visible.value = true
}

const handleEdit = () => {
  emit('edit', currentMember.value)
}

defineExpose({ open })

const getMemberTypeName = (type: number) => {
  switch (type) {
    case MEMBER_TYPE_OWNER: return 'OWNER'
    case MEMBER_TYPE_ADMIN: return 'ADMIN'
    case MEMBER_TYPE_MEMBER: return 'MEMBER'
    case MEMBER_TYPE_STAFF: return 'STAFF'
    default: return '-'
  }
}
</script>

<template>
  <el-drawer v-model="visible" title="成员详情" size="640px">
    <div class="member-detail-container" v-if="currentMember">
      <div class="drawer-header">
        <div class="header-left">
          <el-avatar :size="56" class="member-avatar">{{ currentMember.displayName?.substring(0, 1) || 'U' }}</el-avatar>
          <div class="header-info">
            <div class="name-row">
              <h2>{{ currentMember.displayName }}</h2>
              <span class="username">({{ currentMember.username }})</span>
              <el-tag :type="currentMember.memberStatus === MEMBER_STATUS_ENABLED ? 'success' : 'danger'" effect="light" size="small" class="ml-2">
                {{ currentMember.memberStatus === MEMBER_STATUS_ENABLED ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="handleEdit">编辑</el-button>
          <el-dropdown trigger="click" class="ml-2">
            <el-button>更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="currentMember.memberStatus === MEMBER_STATUS_ENABLED">禁用成员</el-dropdown-item>
                <el-dropdown-item v-else>启用成员</el-dropdown-item>
                <el-dropdown-item divided type="danger">移除成员</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="detail-split-layout mt-6">
        <div class="detail-col">
          <div class="section-title">用户信息</div>
          <div class="info-list">
            <div class="info-item"><span>用户名</span><strong>{{ currentMember.username }}</strong></div>
            <div class="info-item"><span>手机号</span><strong>{{ currentMember.mobile || '-' }}</strong></div>
            <div class="info-item"><span>邮箱</span><strong>{{ currentMember.email || '-' }}</strong></div>
            <div class="info-item"><span>所属身份域</span><strong>{{ currentMember.realmName || '-' }}</strong></div>
            <div class="info-item"><span>账号状态</span><strong>{{ currentMember.accountStatus === 1 ? '启用' : currentMember.accountStatus === 3 ? '锁定' : '禁用' }}</strong></div>
            <div class="info-item"><span>锁定状态</span><strong>{{ currentMember.accountStatus === 3 ? '锁定' : '正常' }}</strong></div>
          </div>
        </div>
        
        <div class="detail-col">
          <div class="section-title">成员信息</div>
          <div class="info-list">
            <div class="info-item"><span>成员类型</span><strong>{{ getMemberTypeName(currentMember.memberType) }}</strong></div>
            <div class="info-item"><span>成员显示名</span><strong>{{ currentMember.displayName }}</strong></div>
            <div class="info-item"><span>成员状态</span><strong>{{ currentMember.memberStatus === MEMBER_STATUS_ENABLED ? '启用' : '禁用' }}</strong></div>
            <div class="info-item"><span>加入时间</span><strong>{{ currentMember.joinedAt || '-' }}</strong></div>
            <div class="info-item"><span>加入人</span><strong>{{ currentMember.joinedByUsername || '-' }}</strong></div>
            <div class="info-item"><span>备注</span><strong>{{ currentMember.remark || '-' }}</strong></div>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.member-detail-container {
  padding: 0 4px;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 24px;
  border-bottom: 1px solid #eef2f7;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.member-avatar {
  background-color: #e0e7ff;
  color: #4f46e5;
  font-weight: 600;
  font-size: 24px;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-row h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.username {
  color: #6b7280;
  font-size: 15px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.detail-split-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.detail-col {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
}

.info-list {
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

.mt-6 {
  margin-top: 24px;
}

.ml-2 {
  margin-left: 8px;
}
</style>
