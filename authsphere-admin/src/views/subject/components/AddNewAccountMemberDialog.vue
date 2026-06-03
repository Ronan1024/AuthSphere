<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import { MEMBER_TYPE_ADMIN, MEMBER_TYPE_MEMBER, MEMBER_TYPE_OWNER, MEMBER_TYPE_STAFF, subjectMemberApi } from '@/api/subjectMember'

const visible = ref(false)
const currentStep = ref(1)
const submitLoading = ref(false)
const subjectId = ref('')

const emit = defineEmits(['success'])

// Step 1: Account Info
const accountFormRef = ref()
const accountData = reactive({
  username: '',
  mobile: '',
  email: '',
  password: '',
  confirmPassword: '',
  forceReset: true
})

const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== accountData.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const accountRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
}

// Step 2: Member Info
const memberFormRef = ref()
const memberData = reactive({
  memberType: MEMBER_TYPE_MEMBER,
  displayName: '',
  remark: ''
})

const memberRules = {
  memberType: [{ required: true, message: '请选择成员类型', trigger: 'change' }],
  displayName: [{ required: true, message: '请输入成员显示名', trigger: 'blur' }]
}

const open = (currentSubjectId: string) => {
  subjectId.value = currentSubjectId
  currentStep.value = 1
  accountData.username = ''
  accountData.mobile = ''
  accountData.email = ''
  accountData.password = ''
  accountData.confirmPassword = ''
  accountData.forceReset = true
  
  memberData.memberType = MEMBER_TYPE_MEMBER
  memberData.displayName = ''
  memberData.remark = ''
  
  visible.value = true
}

const nextStep = async () => {
  if (!accountFormRef.value) return
  await accountFormRef.value.validate((valid: boolean) => {
    if (valid) {
      memberData.displayName = accountData.username // Default to username
      currentStep.value = 2
    }
  })
}

const prevStep = () => {
  currentStep.value = 1
}

const submit = async () => {
  if (!memberFormRef.value) return
  await memberFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      try {
        await subjectMemberApi.createAccountAndAdd(subjectId.value, {
          username: accountData.username,
          mobile: accountData.mobile,
          email: accountData.email,
          password: accountData.password,
          memberType: memberData.memberType,
          displayName: memberData.displayName,
          remark: memberData.remark,
        })
      ElMessage.success('新建账号并加入主体成功')
      visible.value = false
      emit('success')
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '新建账号并加入主体失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="新建账号并加入主体" width="720px" destroy-on-close class="multi-step-dialog">
    <div class="step-header">
      <el-steps :active="currentStep" align-center class="custom-steps">
        <el-step title="账号信息" />
        <el-step title="设置成员信息" />
      </el-steps>
    </div>

    <div v-show="currentStep === 1" class="step-content">
      <el-form ref="accountFormRef" :model="accountData" :rules="accountRules" label-width="120px" class="centered-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="accountData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="accountData.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="accountData.email" placeholder="请输入邮箱 (选填)" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="accountData.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="accountData.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="首次登录强制修改密码" prop="forceReset" label-width="160px">
          <el-switch v-model="accountData.forceReset" />
        </el-form-item>
      </el-form>
    </div>

    <div v-show="currentStep === 2" class="step-content">
      <el-form ref="memberFormRef" :model="memberData" :rules="memberRules" label-width="100px" class="centered-form">
        <el-form-item label="已创建账号">
          <div class="created-account-info">
            <div class="account-name">{{ accountData.username }}</div>
            <div class="account-mobile">{{ accountData.mobile }}</div>
          </div>
        </el-form-item>
        <el-form-item label="成员类型" prop="memberType">
          <el-select v-model="memberData.memberType" placeholder="请选择成员类型" style="width: 100%">
            <el-option label="OWNER" :value="MEMBER_TYPE_OWNER" />
            <el-option label="ADMIN" :value="MEMBER_TYPE_ADMIN" />
            <el-option label="MEMBER" :value="MEMBER_TYPE_MEMBER" />
            <el-option label="STAFF" :value="MEMBER_TYPE_STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="成员显示名" prop="displayName">
          <el-input v-model="memberData.displayName" placeholder="请输入成员显示名" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="memberData.remark" placeholder="门店运营人员" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="currentStep === 1" @click="visible = false">取消</el-button>
        <el-button v-if="currentStep === 1" type="primary" @click="nextStep">下一步</el-button>
        
        <el-button v-if="currentStep === 2" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep === 2" @click="visible = false">取消</el-button>
        <el-button v-if="currentStep === 2" type="primary" :loading="submitLoading" @click="submit">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.multi-step-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.step-header {
  margin-bottom: 30px;
  padding: 0 40px;
}

.custom-steps :deep(.el-step__title) {
  font-size: 14px;
}

.step-content {
  min-height: 280px;
}

.centered-form {
  max-width: 500px;
  margin: 0 auto;
}

.created-account-info {
  background: #f0fdf4;
  border-radius: 4px;
  padding: 8px 16px;
  width: 100%;
}

.account-name {
  color: #111827;
  font-weight: 500;
  font-size: 14px;
}

.account-mobile {
  color: #6b7280;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
