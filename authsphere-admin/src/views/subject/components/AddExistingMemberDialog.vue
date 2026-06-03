<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import { accountApi, type AccountRecord } from '@/api/account'
import { MEMBER_TYPE_ADMIN, MEMBER_TYPE_MEMBER, MEMBER_TYPE_OWNER, MEMBER_TYPE_STAFF, subjectMemberApi } from '@/api/subjectMember'

const visible = ref(false)
const currentStep = ref(1) // 1: 选择账号, 2: 设置成员信息
const loading = ref(false)
const submitLoading = ref(false)
const subjectId = ref('')
const realmCode = ref<string>()

const emit = defineEmits(['success'])

// Step 1: Account Selection
const searchKeyword = ref('')
const selectedAccountId = ref('')
const selectedAccount = ref<AccountRecord | null>(null)
const accountOptions = ref<AccountRecord[]>([])

const handleSelectionChange = (row: AccountRecord) => {
  selectedAccountId.value = row.id
  selectedAccount.value = row
}

// Step 2: Member Info
const formRef = ref()
const formData = reactive({
  memberType: MEMBER_TYPE_ADMIN,
  displayName: '',
  remark: ''
})

const rules = {
  memberType: [{ required: true, message: '请选择成员类型', trigger: 'change' }],
  displayName: [{ required: true, message: '请输入成员显示名', trigger: 'blur' }]
}

const open = (currentSubjectId: string, currentRealmCode?: string) => {
  subjectId.value = currentSubjectId
  realmCode.value = currentRealmCode
  currentStep.value = 1
  searchKeyword.value = ''
  selectedAccountId.value = ''
  selectedAccount.value = null
  formData.memberType = MEMBER_TYPE_ADMIN
  formData.displayName = ''
  formData.remark = ''
  visible.value = true
  searchAccounts()
}

const searchAccounts = async () => {
  loading.value = true
  try {
    const result = await accountApi.page({
      page: 1,
      size: 10,
      realmCode: realmCode.value,
      username: searchKeyword.value || undefined,
    })
    accountOptions.value = result.records ?? []
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号搜索失败')
  } finally {
    loading.value = false
  }
}

const nextStep = () => {
  if (!selectedAccount.value) {
    ElMessage.warning('请选择一个账号')
    return
  }
  formData.displayName = selectedAccount.value.username
  currentStep.value = 2
}

const prevStep = () => {
  currentStep.value = 1
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      try {
        await subjectMemberApi.addExisting(subjectId.value, {
          accountId: selectedAccountId.value,
          memberType: formData.memberType,
          displayName: formData.displayName,
          remark: formData.remark,
        })
      ElMessage.success('添加成员成功')
      visible.value = false
      emit('success')
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '添加成员失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="添加已有账号为成员" width="720px" destroy-on-close class="multi-step-dialog">
    <div class="step-header">
      <el-steps :active="currentStep" align-center class="custom-steps">
        <el-step title="选择账号" />
        <el-step title="设置成员信息" />
      </el-steps>
    </div>

    <div v-show="currentStep === 1" class="step-content">
      <div class="search-bar mb-4">
        <el-input v-model="searchKeyword" placeholder="请输入用户名" clearable style="width: 280px" @keyup.enter="searchAccounts" />
        <el-button type="primary" @click="searchAccounts">搜索</el-button>
      </div>
      <el-table v-loading="loading" :data="accountOptions" style="width: 100%" border @current-change="handleSelectionChange" highlight-current-row>
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-radio v-model="selectedAccountId" :value="row.id" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="mobile" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="realmName" label="所属身份域" width="130" />
      </el-table>
    </div>

    <div v-show="currentStep === 2" class="step-content">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="member-form">
        <el-form-item label="已选账号">
          <span class="selected-account-text">{{ selectedAccount?.username }}</span>
        </el-form-item>
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
          <el-input v-model="formData.remark" placeholder="商户运营管理员" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="currentStep === 1" @click="visible = false">取消</el-button>
        <el-button v-if="currentStep === 1" type="primary" @click="nextStep" :disabled="!selectedAccountId">下一步</el-button>
        
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

.mb-4 {
  margin-bottom: 16px;
}

.member-form {
  max-width: 500px;
  margin: 0 auto;
}

.selected-account-text {
  color: #374151;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
