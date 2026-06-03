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
  reason: '',
  remark: ''
})

const rules = {
  reason: [{ required: true, message: '请选择锁定原因', trigger: 'change' }]
}

const open = (account: AccountRecord) => {
  currentAccount.value = account
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.reason = ''
  formData.remark = ''
  visible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (!currentAccount.value) return
      submitting.value = true
      try {
        await accountApi.lock(currentAccount.value.id)
        ElMessage.success('账号已锁定')
        visible.value = false
        emit('success')
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '账号锁定失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="锁定账号" width="480px" destroy-on-close>
    <el-alert
      title="锁定账号后，用户将无法登录系统，直到解锁为止。"
      type="info"
      show-icon
      :closable="false"
      class="mb-6"
    />

    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" label-position="left">
      <el-form-item label="锁定原因" prop="reason">
        <el-select v-model="formData.reason" placeholder="请选择锁定原因" style="width: 100%">
          <el-option label="安全风险防范" value="security" />
          <el-option label="违规操作" value="violation" />
          <el-option label="管理员手动锁定" value="manual" />
        </el-select>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input 
          v-model="formData.remark" 
          type="textarea" 
          :rows="4" 
          placeholder="请输入备注信息（选填）" 
          maxlength="200" 
          show-word-limit 
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submit">确认锁定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.mb-6 { margin-bottom: 24px; }
</style>
