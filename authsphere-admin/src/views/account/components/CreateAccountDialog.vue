<script setup lang="ts">
import { ref, reactive } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { accountApi } from '@/api/account'
import { realmApi, type RealmRecord } from '@/api/realm'

const visible = ref(false)
const emit = defineEmits(['success'])
const saving = ref(false)
const realmLoading = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()

const realmOptions = ref<RealmRecord[]>([])

const formRef = ref()
const formData = reactive({
  realmId: '',
  username: '',
  nickname: '',
  phonePrefix: '+86',
  mobile: '',
  email: '',
  credentialType: 'default', // default | none
  password: '',
  confirmPassword: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (formData.credentialType === 'none') {
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
        if (formData.credentialType === 'none' || value) {
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

const loadRealms = async (force = false) => {
  if (realmLoading.value || (!force && realmOptions.value.length > 0)) {
    return
  }
  realmLoading.value = true
  try {
    const result = await realmApi.page({ page: 1, size: 100, status: 1 })
    realmOptions.value = result.records ?? []
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

const open = (realms: RealmRecord[] = [], row?: any) => {
  if (realms.length > 0) {
    realmOptions.value = realms
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  if (row) {
    dialogMode.value = 'edit'
    editingId.value = row.id
    formData.realmId = row.realmId
    formData.username = row.username
    formData.nickname = row.nickname
    formData.mobile = row.mobile
    formData.email = row.email || ''
    formData.credentialType = 'none'
  } else {
    dialogMode.value = 'create'
    editingId.value = undefined
    formData.realmId = ''
    formData.username = ''
    formData.nickname = ''
    formData.mobile = ''
    formData.email = ''
    formData.credentialType = 'default'
  }
  
  formData.password = ''
  formData.confirmPassword = ''
  visible.value = true
  loadRealms()
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      saving.value = true
      try {
        const payload = {
          realmId: formData.realmId,
          username: formData.username,
          nickname: formData.nickname,
          mobile: formData.mobile,
          email: formData.email || undefined,
          password: formData.credentialType === 'default' ? formData.password : undefined,
        }
        
        if (dialogMode.value === 'create') {
          await accountApi.create(payload)
          ElMessage.success('新建账号成功')
        } else if (editingId.value) {
          await accountApi.update(editingId.value, payload)
          ElMessage.success('账号已更新')
        }
        
        visible.value = false
        emit('success')
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '账号保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" :title="dialogMode === 'create' ? '新建账号' : '编辑账号'" :width="dialogMode === 'create' ? '720px' : '480px'" destroy-on-close class="create-account-dialog">
    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">
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
          <el-form-item label="登录凭证">
            <el-radio-group v-model="formData.credentialType" class="credential-radio">
              <el-radio value="default">默认密码</el-radio>
              <el-radio value="none">不设置密码 (首次登录时设置)</el-radio>
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
        </div>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
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

.check-icon {
  font-size: 16px;
}

/* Utilities */
.ml-2 { margin-left: 8px; }
.flex-1 { flex: 1; min-width: 0; }
</style>
