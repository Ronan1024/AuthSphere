<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import { MEMBER_TYPE_ADMIN, MEMBER_TYPE_MEMBER, MEMBER_TYPE_OWNER, MEMBER_TYPE_STAFF, subjectMemberApi, type SubjectMemberRecord } from '@/api/subjectMember'

const visible = ref(false)
const submitLoading = ref(false)

const emit = defineEmits(['success'])

const formRef = ref()
const formData = reactive({
  memberType: MEMBER_TYPE_ADMIN,
  displayName: '',
  remark: ''
})

const currentMember = ref<SubjectMemberRecord | null>(null)

const rules = {
  memberType: [{ required: true, message: '请选择成员类型', trigger: 'change' }],
  displayName: [{ required: true, message: '请输入成员显示名', trigger: 'blur' }]
}

const open = (member: SubjectMemberRecord) => {
  currentMember.value = member
  formData.memberType = member.memberType || MEMBER_TYPE_ADMIN
  formData.displayName = member.displayName || member.username
  formData.remark = member.remark || ''
  visible.value = true
}

const submit = async () => {
  if (!formRef.value || !currentMember.value) return
  const member = currentMember.value
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      try {
        await subjectMemberApi.update(member.id, {
          accountId: member.accountId,
          memberType: formData.memberType,
          displayName: formData.displayName,
          remark: formData.remark,
        })
        ElMessage.success('成员信息已更新')
        visible.value = false
        emit('success')
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '成员信息更新失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="编辑成员" width="560px" destroy-on-close>
    <div class="edit-member-container" v-if="currentMember">
      <div class="member-profile-card">
        <el-avatar :size="48" class="member-avatar">{{ currentMember.displayName?.substring(0, 1) || 'U' }}</el-avatar>
        <div class="profile-info">
          <div class="profile-name">
            <strong>{{ currentMember.displayName }}</strong>
            <span class="username">({{ currentMember.username }})</span>
          </div>
          <div class="profile-meta">
            <span>用户名: {{ currentMember.username }}</span>
            <span class="divider"></span>
            <span>手机号: {{ currentMember.mobile || '-' }}</span>
            <span class="divider"></span>
            <span>邮箱: {{ currentMember.email || '-' }}</span>
          </div>
        </div>
      </div>

      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="member-form mt-6">
        <el-form-item label="成员类型" prop="memberType">
          <el-select v-model="formData.memberType" placeholder="请选择成员类型" style="width: 100%">
            <el-option label="OWNER" :value="MEMBER_TYPE_OWNER" />
            <el-option label="ADMIN" :value="MEMBER_TYPE_ADMIN" />
            <el-option label="MEMBER" :value="MEMBER_TYPE_MEMBER" />
            <el-option label="STAFF" :value="MEMBER_TYPE_STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="成员显示名" prop="displayName">
          <el-input v-model="formData.displayName" placeholder="请输入成员显示名" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" placeholder="如：商户业务管理员" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submit">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.member-profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #eef2f7;
}

.member-avatar {
  background-color: #e0e7ff;
  color: #4f46e5;
  font-weight: 600;
  font-size: 20px;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-name {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.profile-name strong {
  font-size: 16px;
  color: #111827;
}

.username {
  color: #6b7280;
  font-size: 14px;
}

.profile-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}

.divider {
  width: 1px;
  height: 12px;
  background-color: #d1d5db;
}

.mt-6 {
  margin-top: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
