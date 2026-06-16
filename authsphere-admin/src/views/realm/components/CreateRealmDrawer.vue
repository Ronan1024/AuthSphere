<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { User, Goods, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { realmApi, type RealmPayload } from '@/api/realm'
import { typeCategoryApi, type TypeCategoryRecord } from '@/api/typeCategory'

const visible = ref(false)
const emit = defineEmits(['success'])
const saving = ref(false)

const activeStep = ref(1) // 1, 2, 3

const typeCategoryLoading = ref(false)
const typeCategoryOptions = ref<TypeCategoryRecord[]>([])

const formRef = ref()
const formData = reactive({
  name: '',
  code: '',
  typeCategoryId: null as string | number | null,
  uniqueScope: 'realm',
  description: '',
  registerEnabled: false,
  ssoEnabled: true,
  passwordPolicy: null as string | number | null
})

const rules = {
  name: [{ required: true, message: '请输入身份域名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入身份域编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-_]{1,31}$/, message: '只允许小写字母、数字、下划线、长横线 3-32', trigger: 'blur' }
  ],
  typeCategoryId: [{ required: true, message: '请选择身份域类型', trigger: 'change' }],
  uniqueScope: [{ required: true, message: '请选择账号唯一性范围', trigger: 'change' }]
}

const typeCards = computed(() => {
  return [
    { id: typeCategoryOptions.value.find(t => t.code === 'plt')?.id || 'plt', icon: User, title: '平台员工', desc: '适用于平台运营、管理等' },
    { id: typeCategoryOptions.value.find(t => t.code === 'ent')?.id || 'ent', icon: Goods, title: '企业客户', desc: '适用于企业/商户/卖方' },
    { id: typeCategoryOptions.value.find(t => t.code === 'csm')?.id || 'csm', icon: User, title: '消费者', desc: '适用于 C 端消费者账号' },
    { id: typeCategoryOptions.value.find(t => t.code === 'sys')?.id || 'sys', icon: Monitor, title: '系统', desc: '适用于系统账号、API等' }
  ]
})

const fetchTypeCategories = async () => {
  typeCategoryLoading.value = true
  try {
    const result = await typeCategoryApi.list()
    typeCategoryOptions.value = Array.isArray(result) ? result : []
  } catch (error) {
    typeCategoryOptions.value = []
  } finally {
    typeCategoryLoading.value = false
  }
}

const open = async () => {
  if (formRef.value) formRef.value.resetFields()
  activeStep.value = 1
  formData.name = ''
  formData.code = ''
  formData.typeCategoryId = null
  formData.uniqueScope = 'realm'
  formData.description = ''
  formData.registerEnabled = false
  formData.ssoEnabled = true
  formData.passwordPolicy = null
  
  visible.value = true
  await fetchTypeCategories()
}

const nextStep = async () => {
  if (activeStep.value === 1) {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
  }
  if (activeStep.value < 3) {
    activeStep.value++
  }
}

const prevStep = () => {
  if (activeStep.value > 1) {
    activeStep.value--
  }
}

const submit = async () => {
  saving.value = true
  try {
    const payload: RealmPayload = {
      code: formData.code,
      name: formData.name,
      realmTypeId: formData.typeCategoryId,
      typeCategoryId: formData.typeCategoryId,
      registerEnabled: formData.registerEnabled,
      ssoEnabled: formData.ssoEnabled,
      description: formData.description
    }
    await realmApi.create(payload)
    ElMessage.success('身份域已成功创建')
    visible.value = false
    emit('success')
  } catch (error: any) {
    ElMessage.error(error.message || '创建身份域失败')
  } finally {
    saving.value = false
  }
}

defineExpose({ open })
</script>

<template>
  <el-drawer v-model="visible" title="新增身份域" size="720px" destroy-on-close class="create-realm-drawer">
    <div class="drawer-header-steps">
      <el-steps :active="activeStep" align-center finish-status="success">
        <el-step title="基础信息" />
        <el-step title="登录与安全策略" />
        <el-step title="确认创建" />
      </el-steps>
    </div>

    <div class="drawer-body">
      <el-form ref="formRef" :model="formData" :rules="rules" label-position="left" label-width="140px">
        
        <!-- Step 1 -->
        <div v-show="activeStep === 1" class="step-content">
          <el-form-item label="身份域名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入身份域名称" />
          </el-form-item>
          <el-form-item label="身份域编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入身份域编码" />
            <div class="input-tip">只允许小写字母、数字、下划线、长横线 3-32</div>
          </el-form-item>
          <el-form-item label="身份域类型" prop="typeCategoryId" class="type-cards-item">
            <div class="type-card-grid">
              <div 
                v-for="card in typeCards" 
                :key="card.id"
                class="type-card"
                :class="{ active: formData.typeCategoryId === card.id }"
                @click="formData.typeCategoryId = card.id"
              >
                <div class="type-icon"><el-icon><component :is="card.icon" /></el-icon></div>
                <div class="type-title">{{ card.title }}</div>
                <div class="type-desc">{{ card.desc }}</div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="账号唯一性范围" prop="uniqueScope">
            <el-select v-model="formData.uniqueScope" placeholder="请选择" style="width: 100%">
              <el-option label="Realm 内唯一 (推荐)" value="realm" />
              <el-option label="全局唯一" value="global" />
            </el-select>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述 (选填)" maxlength="200" show-word-limit />
          </el-form-item>
        </div>

        <!-- Step 2 -->
        <div v-show="activeStep === 2" class="step-content">
          <el-form-item label="允许注册">
            <el-switch v-model="formData.registerEnabled" />
            <div class="input-tip">开启后，用户可在登录入口自行注册账号。</div>
          </el-form-item>
          <el-form-item label="开启 SSO">
            <el-switch v-model="formData.ssoEnabled" />
            <div class="input-tip">用于统一登录和跨应用会话承接。</div>
          </el-form-item>
          <!-- Additional policies can be added here -->
        </div>

        <!-- Step 3 -->
        <div v-show="activeStep === 3" class="step-content">
          <div class="confirm-summary">
            <h3>确认身份域信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="身份域名称">{{ formData.name }}</el-descriptions-item>
              <el-descriptions-item label="身份域编码">{{ formData.code }}</el-descriptions-item>
              <el-descriptions-item label="唯一性范围">{{ formData.uniqueScope === 'realm' ? 'Realm 内唯一' : '全局唯一' }}</el-descriptions-item>
              <el-descriptions-item label="允许注册">{{ formData.registerEnabled ? '是' : '否' }}</el-descriptions-item>
            </el-descriptions>
            <p class="confirm-tip">创建完成后，您可以在身份域详情中继续配置详细策略与应用入口。</p>
          </div>
        </div>

      </el-form>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <el-button v-if="activeStep === 1" @click="visible = false">取消</el-button>
        <el-button v-if="activeStep > 1" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 3" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 3" type="primary" :loading="saving" @click="submit">确认创建</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped>
.drawer-header-steps {
  padding: 24px 40px;
  border-bottom: 1px solid #eaeaea;
  margin-bottom: 24px;
}

.drawer-body {
  padding: 0 40px;
}

.step-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.input-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
  line-height: 1.4;
}

.type-cards-item :deep(.el-form-item__content) {
  display: block;
}

.type-card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 8px;
}

.type-card {
  border: 1px solid #eaeaea;
  border-radius: 4px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.type-card:hover {
  border-color: #000000;
}

.type-card.active {
  border-color: #000000;
  background-color: #fafafa;
}

.type-icon {
  font-size: 24px;
  color: #000000;
  margin-bottom: 8px;
}

.type-card.active .type-icon {
  color: #000000;
}

.type-title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.confirm-summary {
  background: #fafafa;
  padding: 24px;
  border-radius: 4px;
  border: 1px solid #eaeaea;
}

.confirm-summary h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 16px;
  color: #111827;
}

.confirm-tip {
  margin-top: 16px;
  margin-bottom: 0;
  font-size: 13px;
  color: #6b7280;
}

.drawer-footer {
  padding: 16px 24px;
  border-top: 1px solid #eaeaea;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
