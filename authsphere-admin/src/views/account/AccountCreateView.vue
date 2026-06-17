<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

import { type RealmRecord, realmApi } from '@/api/realm'
import { accountApi } from '@/api/account'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

const router = useRouter()
const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const optionsLoading = ref(false)

// Select Options
const realmOptions = ref<RealmRecord[]>([])

// Form Model
const form = reactive({
  realmId: '',
  username: '',
  mobile: '',
  email: '',
  password: '',
  confirmPassword: '',
  forceReset: '1', // 1: 是, 0: 否
  status: 1, // 1: 启用, 3: 禁用

  // Subject relation mock details
  joinSubjectMode: 'none', // none: 暂不加入, immediate: 立即加入
  defaultSubjectId: 'none',
  remark: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  realmId: [{ required: true, message: '请选择所属身份域', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  status: [{ required: true, message: '请选择账号状态', trigger: 'change' }]
}

const goBack = () => {
  router.push('/accounts')
}

const fetchOptions = async () => {
  optionsLoading.value = true
  try {
    const realmPage = await realmApi.page({ page: 1, size: 100, status: 1 })
    realmOptions.value = realmPage.records ?? []
  } catch (error) {
    showErrorMessage('获取身份域列表失败')
  } finally {
    optionsLoading.value = false
  }
}

const submitForm = async (continueAdd = false) => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项')
    return
  }

  saveLoading.value = true
  try {
    const payload = {
      realmId: form.realmId,
      username: form.username,
      nickname: form.username, // Use username as nickname default
      mobile: form.mobile,
      email: form.email || undefined,
      password: form.password,
      status: form.status
    }

    await accountApi.create(payload)
    showSuccessMessage('账号已新建')
    
    if (continueAdd) {
      // Clear form except realm selection
      form.username = ''
      form.mobile = ''
      form.email = ''
      form.password = ''
      form.confirmPassword = ''
      form.remark = ''
      if (formRef.value) formRef.value.resetFields(['username', 'mobile', 'email', 'password', 'confirmPassword'])
    } else {
      router.push('/accounts')
    }
  } catch (error: any) {
    showErrorMessage(error.message || '保存账号失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  fetchOptions()
})
</script>

<template>
  <section class="account-create-page" v-loading="optionsLoading">
    <div class="top-breadcrumb">
      <span class="crumb-back" @click="goBack"><el-icon><ArrowLeft /></el-icon> 返回账号列表</span>
      <span class="divider">/</span>
      <span class="crumb-curr">新增账号</span>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="create-form-container"
    >
      <!-- Section 1: 基础信息 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>基础信息</h2>
            <p>只创建登录账号，不直接分配应用角色</p>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <div class="form-item-wrap">
            <el-form-item label="所属身份域 *" prop="realmId">
              <el-select v-model="form.realmId" placeholder="请选择所属身份域">
                <el-option
                  v-for="item in realmOptions"
                  :key="item.id"
                  :label="`${item.name} ${item.code}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <span class="field-help-text">账号登录时只能在所属身份域内认证。</span>
          </div>

          <div class="form-item-wrap">
            <el-form-item label="用户名 *" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
            <span class="field-help-text">当前身份域下唯一，建议英文、数字、下划线。</span>
          </div>

          <el-form-item label="手机号" prop="mobile">
            <el-input v-model="form.mobile" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱地址" />
          </el-form-item>
        </div>
      </div>

      <!-- Section 2: 登录安全 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>登录安全</h2>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <div class="form-item-wrap">
            <el-form-item label="初始密码 *" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
            </el-form-item>
            <span class="field-help-text">保存时加密，不保存明文。</span>
          </div>

          <el-form-item label="确认密码 *" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
          </el-form-item>

          <el-form-item label="首次登录强制改密">
            <el-select v-model="form.forceReset">
              <el-option label="是" value="1" />
              <el-option label="否" value="0" />
            </el-select>
          </el-form-item>

          <el-form-item label="账号状态" prop="status">
            <el-select v-model="form.status">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="3" />
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- Section 3: 主体关系 -->
      <div class="form-section-card">
        <div class="section-card-header">
          <div class="header-title-wrap">
            <h2>主体关系</h2>
          </div>
        </div>
        <div class="section-card-body grid-2col">
          <div class="form-item-wrap">
            <el-form-item label="是否立即加入主体">
              <el-select v-model="form.joinSubjectMode">
                <el-option label="暂不加入，创建后在账号详情或主体详情中维护" value="none" />
                <el-option label="立即加入主体" value="immediate" />
              </el-select>
            </el-form-item>
            <span class="field-help-text">加入主体只表示账号可代表该主体，不代表拥有应用权限。</span>
          </div>

          <el-form-item label="默认主体">
            <el-select v-model="form.defaultSubjectId">
              <el-option label="暂不设置" value="none" />
            </el-select>
          </el-form-item>

          <div class="span-2col">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注说明"
              />
            </el-form-item>
          </div>
        </div>
      </div>

      <!-- Action Row -->
      <div class="form-action-row">
        <el-button @click="goBack">取消</el-button>
        <el-button @click="submitForm(true)">保存并继续新增</el-button>
        <el-button type="primary" :loading="saveLoading" @click="submitForm(false)">保存</el-button>
      </div>
    </el-form>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

.account-create-page {
  --primary-color: #0369A1;      /* Security Blue */
  --primary-hover: #0284c7;
  --secondary-color: #0EA5E9;    /* Sky Blue */
  --success-color: #16A34A;      /* Protected Green */
  --bg-color: #F0F9FF;           /* Theme Background */
  --text-main: #0C4A6E;          /* Deep Navy Text */
  --text-muted: #475569;
  --border-light: rgba(226, 232, 240, 0.8);
  --font-family-display: 'Lexend', system-ui, -apple-system, sans-serif;
  --font-family-body: 'Source Sans 3', system-ui, -apple-system, sans-serif;

  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-family-body);
}

.top-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 12px;
  color: var(--text-muted);
  font-family: var(--font-family-display);
}

.crumb-back {
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s ease;
}

.crumb-back:hover {
  color: var(--primary-color);
}

.crumb-curr {
  color: var(--text-main);
  font-weight: 500;
}

.create-form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
  overflow: hidden;
}

.section-card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
}

.header-title-wrap h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.header-title-wrap p {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: var(--text-muted);
}

.section-card-body {
  padding: 24px;
}

.grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}

.span-2col {
  grid-column: span 2;
}

.form-item-wrap {
  display: flex;
  flex-direction: column;
}

.field-help-text {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: -8px;
  margin-bottom: 8px;
}

.form-action-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
  margin-bottom: 30px;
}
</style>
