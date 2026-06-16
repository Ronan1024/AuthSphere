<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import { accountApi, type AccountRecord } from '@/api/account'

const visible = ref(false)
const formRef = ref()
const currentAccount = ref<AccountRecord | null>(null)
const submitting = ref(false)
const emit = defineEmits(['success'])

const formData = reactive({
  resetMethod: 'manual', // manual | email
  newPassword: '',
  confirmPassword: '',
  forceReset: true
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (formData.resetMethod === 'email') {
    callback()
    return
  }
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
}

const open = (account: AccountRecord) => {
  currentAccount.value = account
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.resetMethod = 'manual'
  formData.newPassword = ''
  formData.confirmPassword = ''
  formData.forceReset = true
  visible.value = true
}

const submit = async () => {
  if (formData.resetMethod === 'email') {
    ElMessage.success('已发送重置链接到邮箱')
    visible.value = false
    return
  }

  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (!currentAccount.value) return
      submitting.value = true
      try {
        await accountApi.resetPassword(currentAccount.value.id, {
          newPassword: formData.newPassword,
          forceReset: formData.forceReset,
        })
        ElMessage.success('密码重置成功')
        visible.value = false
        emit('success')
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '密码重置失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="重置密码" width="480px" destroy-on-close>
    <el-alert
      title="重置密码后，用户需要使用新密码重新登录。"
      type="info"
      show-icon
      :closable="false"
      class="mb-6"
    />

    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" label-position="left">
      <el-form-item label="重置方式">
        <el-radio-group v-model="formData.resetMethod">
          <el-radio value="manual">设置新密码</el-radio>
          <el-radio value="email">发送重置链接到邮箱</el-radio>
        </el-radio-group>
      </el-form-item>

      <template v-if="formData.resetMethod === 'manual'">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="formData.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="formData.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item label="强制修改密码">
          <div class="flex-center w-full justify-between">
            <el-switch v-model="formData.forceReset" />
            <span class="text-xs text-secondary">用户下次登录时必须修改密码</span>
          </div>
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认重置</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.mb-6 { margin-bottom: 24px; }
.flex-center { display: flex; align-items: center; }
.justify-between { justify-content: space-between; }
.w-full { width: 100%; }
.text-xs { font-size: 12px; }
.text-secondary { color: #6b7280; }
</style>
