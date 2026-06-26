<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { accountApi, type AccountCreateResponse } from '@/api/account'
import { realmApi, type RealmOption } from '@/api/realm'

const visible = ref(false)
const emit = defineEmits(['success'])
const saving = ref(false)
const realmLoading = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const createdResult = ref<AccountCreateResponse | null>(null)
const editingId = ref<string>()
const submitError = ref('')

const realmOptions = ref<RealmOption[]>([])

const formRef = ref()
const formData = reactive({
  realmId: '',
  username: '',
  nickname: '',
  remark: '',
  phonePrefix: '+86',
  mobile: '',
  email: '',
  credentialType: 'default', // default | temporary
  password: '',
  confirmPassword: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (formData.credentialType === 'temporary') {
    callback()
    return
  }
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  realmId: [{ required: true, message: '请选择所属身份域', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [
    {
      required: true,
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (formData.credentialType === 'temporary' || value) {
          callback()
          return
        }
        callback(new Error('请输入初始密码'))
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
}

const selectedRealm = computed(() =>
  realmOptions.value.find((item) => String(item.id) === String(formData.realmId)),
)

const supportsPasswordCredential = computed(() => {
  const authMethodCodes = selectedRealm.value?.authMethodList?.map((item) =>
    item.code?.trim().toLowerCase().replace(/[-_]/g, ''),
  ) ?? []
  return authMethodCodes.some((code) => code === 'passwordlogin' || code === 'password')
})

const clearPasswordFields = () => {
  formData.password = ''
  formData.confirmPassword = ''
}

const closeCreatedResult = () => {
  createdResult.value = null
  visible.value = false
  emit('success')
}

const loadRealms = async (force = false) => {
  if (realmLoading.value || (!force && realmOptions.value.length > 0)) {
    return
  }
  realmLoading.value = true
  try {
    realmOptions.value = await realmApi.list()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '身份域加载失败')
  } finally {
    realmLoading.value = false
  }
}

const handleRealmVisible = (opened: boolean) => {
  if (opened) {
    loadRealms()
  }
}

const open = (realms: RealmOption[] = [], row?: any) => {
  if (realms.length > 0) {
    realmOptions.value = realms
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
  createdResult.value = null
  submitError.value = ''
  
  if (row) {
    dialogMode.value = 'edit'
    editingId.value = row.id
    formData.realmId = row.realmId
    formData.username = row.username
    formData.nickname = row.nickname
    formData.remark = row.remark || ''
    formData.mobile = row.mobile
    formData.email = row.email || ''
    formData.credentialType = 'temporary'
  } else {
    dialogMode.value = 'create'
    editingId.value = undefined
    formData.realmId = ''
    formData.username = ''
    formData.nickname = ''
    formData.remark = ''
    formData.mobile = ''
    formData.email = ''
    formData.credentialType = 'default'
  }
  
  formData.password = ''
  formData.confirmPassword = ''
  visible.value = true
  loadRealms()
}

watch(
  () => formData.realmId,
  async (realmId) => {
    if (!realmId || dialogMode.value !== 'create') {
      clearPasswordFields()
      return
    }
    if (!supportsPasswordCredential.value) {
      formData.credentialType = 'temporary'
      clearPasswordFields()
      return
    }
    if (formData.credentialType !== 'default' && formData.credentialType !== 'temporary') {
      formData.credentialType = 'default'
    }
  },
)

const submit = async () => {
  if (!formRef.value) return
  submitError.value = ''
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = {
      realmId: formData.realmId,
      username: formData.username,
      nickname: formData.nickname,
      remark: formData.remark || undefined,
      mobile: formData.mobile,
      email: formData.email || undefined,
      useTemporaryPassword: supportsPasswordCredential.value && formData.credentialType === 'temporary',
      password: supportsPasswordCredential.value && formData.credentialType === 'default'
        ? formData.password
        : undefined,
    }

    if (dialogMode.value === 'create') {
      const result = await accountApi.create(payload)
      createdResult.value = result
      ElMessage.success('新建账号成功')
      if (result.temporaryPassword) {
        return
      }
    } else if (editingId.value) {
      await accountApi.update(editingId.value, payload)
      ElMessage.success('账号已更新')
    }

    visible.value = false
    emit('success')
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '账号保存失败'
  } finally {
    saving.value = false
  }
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" :title="dialogMode === 'create' ? '新建账号' : '编辑账号'" :width="dialogMode === 'create' ? '720px' : '480px'" destroy-on-close class="create-account-dialog">
    <template v-if="createdResult?.temporaryPassword">
      <div class="temporary-password-result">
        <div class="result-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <h3>账号已创建</h3>
        <p>当前账号使用临时密码登录，请尽快复制并安全传达给用户。首次登录后将强制修改密码。</p>
        <div class="temporary-password-card">
          <span class="password-label">临时密码</span>
          <code>{{ createdResult.temporaryPassword }}</code>
        </div>
      </div>
    </template>
    <el-form v-else ref="formRef" :model="formData" :rules="rules" label-position="top">
      <el-alert
        v-if="submitError"
        :title="submitError"
        type="error"
        show-icon
        :closable="false"
        class="submit-error-alert"
      />
      <div class="form-grid" :class="{ 'is-edit': dialogMode === 'edit' }">
        <!-- 左栏：基础信息 -->
        <div class="form-column">
          <div class="section-title">基础信息</div>
          <el-form-item label="所属身份域" prop="realmId">
            <el-select
              v-model="formData.realmId"
              placeholder="请选择身份域"
              filterable
              clearable
              :loading="realmLoading"
              style="width: 100%"
              popper-class="account-realm-select-popper"
              @visible-change="handleRealmVisible"
            >
              <el-option v-for="realm in realmOptions" :key="realm.id" :label="realm.name" :value="realm.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="formData.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="账号备注" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="请输入账号备注"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="mobile">
            <div class="mobile-input-group">
              <el-select v-model="formData.phonePrefix" style="width: 100px">
                <el-option label="+86" value="+86" />
                <el-option label="+852" value="+852" />
                <el-option label="+886" value="+886" />
              </el-select>
              <el-input v-model="formData.mobile" placeholder="请输入手机号" class="ml-2 flex-1" />
            </div>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
        </div>

        <!-- 右栏：登录设置 -->
        <div class="form-column" v-if="dialogMode === 'create'">
          <div class="section-title">登录设置</div>
          <template v-if="supportsPasswordCredential">
            <el-form-item label="登录凭证">
              <el-radio-group v-model="formData.credentialType" class="credential-radio">
                <el-radio value="default">默认密码</el-radio>
                <el-radio value="temporary">使用临时密码</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <template v-if="formData.credentialType === 'default'">
              <el-form-item label="初始密码" prop="password">
                <el-input v-model="formData.password" type="password" show-password placeholder="请输入初始密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="formData.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
              </el-form-item>
              <el-form-item label="密码策略">
                <div class="policy-check">
                  <el-icon class="check-icon"><CircleCheck /></el-icon>
                  <span>符合密码策略要求</span>
                </div>
              </el-form-item>
            </template>
          </template>
          <el-form-item v-else label="登录凭证">
            <div class="no-password-hint">
              当前身份域未启用账号密码认证，不展示密码设置。
            </div>
          </el-form-item>
        </div>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <template v-if="createdResult?.temporaryPassword">
          <el-button
            type="primary"
            @click="closeCreatedResult"
          >
            完成
          </el-button>
        </template>
        <template v-else>
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.submit-error-alert {
  margin-bottom: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.form-grid.is-edit {
  grid-template-columns: 1fr;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.mobile-input-group {
  display: flex;
  width: 100%;
}

.credential-radio {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.policy-check {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #10b981;
  font-size: 13px;
  background: #ecfdf5;
  padding: 8px 12px;
  border-radius: 4px;
  width: 100%;
}

.no-password-hint {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid #e2e8f0;
}

.temporary-password-result {
  padding: 24px 8px 8px;
  text-align: center;
}

.result-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: #ecfdf5;
  color: #16a34a;
  font-size: 28px;
}

.temporary-password-result h3 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 22px;
}

.temporary-password-result p {
  margin: 0 auto 20px;
  max-width: 460px;
  color: #64748b;
  line-height: 1.7;
}

.temporary-password-card {
  margin: 0 auto;
  max-width: 420px;
  padding: 16px;
  border: 1px solid #dbeafe;
  background: #f8fbff;
  border-radius: 12px;
  text-align: left;
}

.password-label {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
}

.temporary-password-card code {
  display: block;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  word-break: break-all;
}

.check-icon {
  font-size: 16px;
}

/* Utilities */
.ml-2 { margin-left: 8px; }
.flex-1 { flex: 1; min-width: 0; }
</style>
