<script setup lang="ts">
import { computed, ref, reactive, onMounted, watch } from 'vue'
import { Plus, Search, Refresh, ArrowDown, ArrowLeft, Connection, TopRight } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { realmApi, type RealmRecord } from '@/api/realm'
import { typeCategoryApi, type TypeCategoryRecord } from '@/api/typeCategory'
import { passwordPolicyApi, type PasswordPolicyListItem } from '@/api/passwordPolicy'

import RealmDetailView from './components/RealmDetailView.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const selectedRealm = ref<RealmRecord>()

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  typeCategoryId: undefined as string | number | undefined,
  status: undefined as number | undefined,
  authMethod: '',
  createTimeRange: [] as any[]
})

const tableData = ref<RealmRecord[]>([])
const total = ref(0)
const typeCategoryOptions = ref<TypeCategoryRecord[]>([])

// Create Realm full-page form states
const isCreating = computed(() => route.path.startsWith('/realms/create'))
const isEditing = computed(() => route.name === 'RealmEdit')
const isFormPage = computed(() => isCreating.value || isEditing.value)
const formTitle = computed(() => isEditing.value ? '编辑身份域' : '添加身份域')
const submitLoading = ref(false)
const createFormRef = ref()
const passwordPolicyOptions = ref<PasswordPolicyListItem[]>([])

const createForm = reactive({
  name: '',
  code: '',
  typeCategoryId: '' as string | number,
  typeCategoryCode: '',
  description: '',
  status: 1,
  sortNo: 0,
  loginUrl: 'default',
  authMethods: ['password', 'sms'] as string[],
  authPolicy: 'default',
  sessionTimeout: 8,
  tokenTimeout: 2,
  passwordPolicy: '' as string | number,
  sslRequired: true,
  mfaPolicy: 'default',
  loginFailLock: false,
  remark: ''
})

const CREATE_DRAFT_KEY = 'authsphere:realm-create-draft'
const getDraftKey = () => isEditing.value
  ? `authsphere:realm-edit-draft:${route.params.id}`
  : CREATE_DRAFT_KEY

const createFormRules = {
  name: [{ required: true, message: '请输入身份域名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入身份域编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-_]{1,31}$/, message: '只允许小写字母、数字、下划线、长横线 3-32', trigger: 'blur' }
  ],
  typeCategoryId: [{ required: true, message: '请选择身份域类型', trigger: 'change' }],
  loginUrl: [{ required: true, message: '请选择默认登录页', trigger: 'change' }],
  passwordPolicy: [{ required: true, message: '请选择密码策略', trigger: 'change' }]
}

// Authentication methods mapping based on realm code for visual fidelity matching the screenshot
const getAuthMethodsText = (row: RealmRecord) => {
  const code = row.code
  if (code === 'platform_realm') return '密码、短信、MFA'
  if (code === 'tenant_realm') return '密码、短信、MFA、邮箱'
  if (code === 'merchant_realm') return '密码、短信'
  if (code === 'consumer_realm') return '短信、MFA'
  if (code === 'provider_realm') return '密码、MFA'
  if (code === 'dev_realm') return '密码'
  if (code === 'partner_realm') return '密码、证书、MFA'
  return '密码、短信'
}

const getLoginPageText = (row: RealmRecord) => {
  return row.loginUrl || '默认登录页'
}

const typeCategoryText = (typeCategoryId?: string | number | null) => {
  if (!typeCategoryId) return '-'
  const match = typeCategoryOptions.value.find(item => String(item.id) === String(typeCategoryId))
  return match ? match.name : String(typeCategoryId)
}

const getCategoryTypeClass = (categoryId?: string | number | null) => {
  const text = typeCategoryText(categoryId)
  if (text.includes('平台')) return 'tag-platform'
  if (text.includes('租户') || text.includes('租')) return 'tag-tenant'
  if (text.includes('商户') || text.includes('商')) return 'tag-merchant'
  if (text.includes('消费者')) return 'tag-consumer'
  if (text.includes('服务')) return 'tag-provider'
  if (text.includes('内部')) return 'tag-internal'
  if (text.includes('合作') || text.includes('伙伴')) return 'tag-partner'
  return 'tag-default'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size
    }
    if (query.name && query.name.trim() !== '') params.name = query.name.trim()
    if (query.code && query.code.trim() !== '') params.code = query.code.trim()
    if (query.typeCategoryId !== '' && query.typeCategoryId !== undefined) params.typeCategoryId = query.typeCategoryId
    if (typeof query.status === 'number') params.status = query.status

    const result = await realmApi.page(params)
    tableData.value = result.records || []
    total.value = result.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取身份域列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTypeCategories = async () => {
  try {
    const result = await typeCategoryApi.list()
    typeCategoryOptions.value = Array.isArray(result) ? result : []
  } catch (error: any) {
    ElMessage.error(error.message || '获取身份域类型失败')
  }
}

const handleTypeChange = (val: any) => {
  const match = typeCategoryOptions.value.find(item => String(item.id) === String(val))
  if (match) {
    createForm.typeCategoryCode = match.code
  } else {
    createForm.typeCategoryCode = '-'
  }
}

const loadPasswordPolicies = async () => {
  try {
    const list = await passwordPolicyApi.list()
    passwordPolicyOptions.value = list || []
    if (!createForm.passwordPolicy && list && list.length > 0) {
      createForm.passwordPolicy = list[0].id
    }
  } catch (error) {}
}

const openTypeManager = () => {
  router.push('/realm/type-categories')
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.typeCategoryId = undefined
  query.status = undefined
  query.authMethod = ''
  query.createTimeRange = []
  handleSearch()
}

const openCreateDialog = () => {
  sessionStorage.removeItem(CREATE_DRAFT_KEY)
  createForm.name = ''
  createForm.code = ''
  createForm.typeCategoryId = typeCategoryOptions.value[0]?.id || ''
  handleTypeChange(createForm.typeCategoryId)
  createForm.description = ''
  createForm.status = 1
  createForm.sortNo = 0
  createForm.loginUrl = 'default'
  createForm.authMethods = ['password', 'sms']
  createForm.authPolicy = 'default'
  createForm.sessionTimeout = 8
  createForm.tokenTimeout = 2
  createForm.sslRequired = true
  createForm.mfaPolicy = 'default'
  createForm.loginFailLock = false
  createForm.remark = ''

  loadPasswordPolicies()
  router.push('/realms/create')
}

const openEditPage = (row: RealmRecord) => {
  Object.assign(createForm, {
    name: row.name,
    code: row.code,
    typeCategoryId: row.typeCategoryId || '',
    typeCategoryCode: '',
    description: row.description || '',
    status: row.status || 1,
    sortNo: 0,
    loginUrl: row.loginUrl || 'default',
    authMethods: ['password', 'sms'],
    authPolicy: 'default',
    sessionTimeout: 8,
    tokenTimeout: 2,
    passwordPolicy: row.passwordPolicy || '',
    sslRequired: true,
    mfaPolicy: row.mfaPolicy || 'default',
    loginFailLock: false,
    remark: '',
  })
  const draftKey = `authsphere:realm-edit-draft:${row.id}`
  sessionStorage.setItem(draftKey, JSON.stringify(createForm))
  router.push(`/realms/${row.id}/edit`)
}

const closeFormPage = () => {
  sessionStorage.removeItem(getDraftKey())
  router.push('/realms')
}

const openDetail = (row: RealmRecord) => {
  selectedRealm.value = row
}

const closeDetail = () => {
  selectedRealm.value = undefined
  fetchData()
}

const toggleStatus = async (row: RealmRecord) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该身份域？`, '提示', { type: 'warning' })
    await realmApi.toggleStatus(row.id)
    ElMessage.success(`身份域已${action}`)
    fetchData()
  } catch (error: any) {
    if (error === 'cancel') return
    ElMessage.error(error.message || `${action}失败`)
  }
}

const submitRealmForm = async (continueCreating: boolean) => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {
      code: createForm.code,
      name: createForm.name,
      typeCategoryId: createForm.typeCategoryId,
      loginUrl: createForm.loginUrl === 'default' ? '' : createForm.loginUrl,
      registerEnabled: createForm.authMethods.includes('sms') || createForm.authMethods.includes('email'),
      ssoEnabled: true,
      description: createForm.description || undefined,
      passwordPolicy: createForm.passwordPolicy || undefined,
      mfaPolicy: createForm.mfaPolicy === 'default' ? 1 : undefined,
    } as any

    if (isEditing.value) {
      await realmApi.update(String(route.params.id), payload)
      ElMessage.success('身份域更新成功')
    } else {
      await realmApi.create(payload)
      ElMessage.success('身份域创建成功')
    }

    if (continueCreating && !isEditing.value) {
      createForm.name = ''
      createForm.code = ''
      createForm.description = ''
      createForm.remark = ''
    } else {
      closeFormPage()
    }
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '创建身份域失败')
  } finally {
    submitLoading.value = false
  }
}

const init = () => {
  if (isCreating.value || isEditing.value) {
    const draft = sessionStorage.getItem(getDraftKey())
    if (draft) {
      try {
        Object.assign(createForm, JSON.parse(draft))
      } catch (error) {
        sessionStorage.removeItem(getDraftKey())
      }
    }
  }
  fetchTypeCategories()
  fetchData()
  if (isCreating.value || isEditing.value) {
    loadPasswordPolicies()
  }
}

watch(
  createForm,
  value => {
    if (isCreating.value || isEditing.value) {
      sessionStorage.setItem(getDraftKey(), JSON.stringify(value))
    }
  },
  { deep: true },
)

onMounted(init)
</script>

<template>
  <section class="realm-page">
    <template v-if="selectedRealm">
      <RealmDetailView 
        :realm="selectedRealm" 
        @back="closeDetail" 
        @edit="() => {}"
      />
    </template>

    <template v-else-if="isFormPage">
      <div class="create-realm-page">
        <!-- Page Header -->
        <div class="create-header">
          <div class="back-action" @click="closeFormPage">
            <el-icon class="back-icon"><ArrowLeft /></el-icon>
            <span class="back-title">{{ formTitle }}</span>
          </div>
        </div>

        <el-form ref="createFormRef" :model="createForm" :rules="createFormRules" label-position="left" label-width="120px" class="create-form">
          <!-- Section 1: 基础信息 -->
          <div class="form-section-card">
            <div class="section-title">基础信息</div>
            <div class="form-grid-2">
              <div class="grid-col">
                <el-form-item label="身份域名称" prop="name">
                  <el-input v-model="createForm.name" placeholder="请输入身份域名称" />
                  <div class="field-hint-text">用于在平台上展示用户的人称名称，建议使用清晰易懂的名称。</div>
                </el-form-item>

                <el-form-item label="身份域编码" prop="code">
                  <el-input v-model="createForm.code" :disabled="isEditing" placeholder="请输入身份域编码" />
                  <div class="field-hint-text">英文、数字或下划线，3~64位，创建后不可修改。</div>
                </el-form-item>

                <el-form-item label="身份域类型" prop="typeCategoryId">
                  <div class="flex-row-align">
                    <el-select v-model="createForm.typeCategoryId" placeholder="请选择身份域类型" @change="handleTypeChange" style="flex: 1;">
                      <el-option v-for="item in typeCategoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                    <el-button link type="primary" class="ml-12 inline-link" @click="openTypeManager">
                      前往类型管理 <el-icon class="el-icon--right"><Connection /></el-icon>
                    </el-button>
                  </div>
                  <div class="field-hint-text">身份域类型由系统字典维护，管理员可新增、编辑、禁用。</div>
                </el-form-item>

                <el-form-item label="类型编码">
                  <el-input :value="createForm.typeCategoryCode" disabled placeholder="-" />
                  <div class="field-hint-text">所选类型的系统字典编码（只读）。</div>
                </el-form-item>
              </div>

              <div class="grid-col">
                <el-form-item label="描述" prop="description">
                  <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="请输入描述信息" maxlength="200" show-word-limit />
                  <div class="field-hint-text">描述该身份域的用途和范围。</div>
                </el-form-item>

                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="createForm.status">
                    <el-radio :label="1">启用</el-radio>
                    <el-radio :label="2">禁用</el-radio>
                  </el-radio-group>
                  <div class="field-hint-text">禁用后，域内的登录和认证将不可用（不影响历史数据）。</div>
                </el-form-item>

                <el-form-item label="排序" prop="sortNo">
                  <el-input-number v-model="createForm.sortNo" :min="0" controls-position="right" style="width: 100%; text-align: left;" />
                  <div class="field-hint-text">数值越小越靠前，默认0。</div>
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- Section 2: 认证配置 -->
          <div class="form-section-card">
            <div class="section-title">认证配置</div>
            <div class="form-grid-2">
              <div class="grid-col">
                <el-form-item label="默认登录页" prop="loginUrl">
                  <div class="flex-row-align">
                    <el-select v-model="createForm.loginUrl" placeholder="请选择登录页" style="flex: 1;">
                      <el-option label="默认登录页" value="default" />
                      <el-option label="自定义模版页" value="custom_template" />
                    </el-select>
                    <el-button link type="primary" class="ml-12 inline-link">
                      前往登录页管理 <el-icon class="el-icon--right"><Connection /></el-icon>
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item label="支持的认证方式" prop="authMethods">
                  <el-checkbox-group v-model="createForm.authMethods">
                    <el-checkbox label="password">密码登录</el-checkbox>
                    <el-checkbox label="sms">短信验证码</el-checkbox>
                    <el-checkbox label="email">邮箱验证码</el-checkbox>
                    <el-checkbox label="wechat">微信扫码登录</el-checkbox>
                    <el-checkbox label="mfa">MFA 多因素认证</el-checkbox>
                  </el-checkbox-group>
                  <div class="field-hint-text">至少选择一种认证方式。</div>
                </el-form-item>
              </div>

              <div class="grid-col">
                <el-form-item label="默认认证策略" prop="authPolicy">
                  <div class="flex-row-align">
                    <el-select v-model="createForm.authPolicy" placeholder="请选择认证策略" style="flex: 1;">
                      <el-option label="默认认证策略" value="default" />
                    </el-select>
                    <el-button link type="primary" class="ml-12 inline-link">
                      前往认证策略管理 <el-icon class="el-icon--right"><Connection /></el-icon>
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item label="会话有效期" prop="sessionTimeout">
                  <div class="flex-row-align">
                    <el-input-number v-model="createForm.sessionTimeout" :min="1" controls-position="right" style="width: calc(100% - 90px);" />
                    <el-select value="hour" placeholder="小时" style="width: 80px; margin-left: 8px;" disabled>
                      <el-option label="小时" value="hour" />
                    </el-select>
                  </div>
                  <div class="field-hint-text">用户登录成功后的会话有效时间，范围：1~168小时。</div>
                </el-form-item>

                <el-form-item label="令牌有效期" prop="tokenTimeout">
                  <div class="flex-row-align">
                    <el-input-number v-model="createForm.tokenTimeout" :min="1" controls-position="right" style="width: calc(100% - 90px);" />
                    <el-select value="hour" placeholder="小时" style="width: 80px; margin-left: 8px;" disabled>
                      <el-option label="小时" value="hour" />
                    </el-select>
                  </div>
                  <div class="field-hint-text">访问令牌（Access Token）的有效期，范围：5分钟~24小时。</div>
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- Section 3: 安全配置 -->
          <div class="form-section-card">
            <div class="section-title">安全配置</div>
            <div class="form-grid-2">
              <div class="grid-col">
                <el-form-item label="密码策略" prop="passwordPolicy">
                  <div class="flex-row-align">
                    <el-select v-model="createForm.passwordPolicy" placeholder="请选择密码策略" style="flex: 1;">
                      <el-option v-for="item in passwordPolicyOptions" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                    <el-button link type="primary" class="ml-12 inline-link">
                      前往密码策略管理 <el-icon class="el-icon--right"><Connection /></el-icon>
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item label="强制 SSL/TLS" prop="sslRequired">
                  <el-switch v-model="createForm.sslRequired" />
                  <div class="field-hint-text">是否强制要求使用 HTTPS 访问登录和认证接口。</div>
                </el-form-item>
              </div>

              <div class="grid-col">
                <el-form-item label="MFA 策略" prop="mfaPolicy">
                  <div class="flex-row-align">
                    <el-select v-model="createForm.mfaPolicy" placeholder="请选择 MFA 策略" style="flex: 1;">
                      <el-option label="默认 MFA 策略" value="default" />
                    </el-select>
                    <el-button link type="primary" class="ml-12 inline-link">
                      前往 MFA 策略管理 <el-icon class="el-icon--right"><Connection /></el-icon>
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item label="登录失败锁定" prop="loginFailLock">
                  <el-switch v-model="createForm.loginFailLock" />
                  <div class="field-hint-text">开启后，连续登录失败将锁定账号（策略在认证策略中配置）。</div>
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- Section 4: 备注信息 -->
          <div class="form-section-card">
            <div class="section-title">备注信息</div>
            <el-form-item label="备注" prop="remark" label-width="120px">
              <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息 (选填)" maxlength="500" show-word-limit />
              <div class="field-hint-text">其他补充说明信息。</div>
            </el-form-item>
          </div>

          <!-- Actions Footer -->
          <div class="form-footer-actions">
            <el-button type="primary" :loading="submitLoading" @click="submitRealmForm(false)" class="btn-save">保存</el-button>
            <el-button v-if="!isEditing" type="primary" plain :loading="submitLoading" @click="submitRealmForm(true)" class="btn-save-continue">保存并继续</el-button>
            <el-button @click="closeFormPage" class="btn-cancel">取消</el-button>
          </div>
        </el-form>
      </div>
    </template>

    <template v-else>
      <div class="page-heading">
        <div class="heading-text">
          <h1>身份域管理</h1>
          <p>
            身份域用于隔离不同的身份空间，拥有独立的认证策略、登录页和数据范围。
            <a href="javascript:void(0)" class="learn-more-link">了解更多 <el-icon><TopRight /></el-icon></a>
          </p>
        </div>
        <div class="heading-actions">
          <el-button type="primary" :icon="Plus" class="primary-action" @click="openCreateDialog">新增身份域</el-button>
          <el-button :icon="Refresh" class="refresh-action" @click="fetchData">刷新</el-button>
        </div>
      </div>

      <el-card shadow="never" class="filter-card">
        <div class="filter-grid">
          <div class="filter-row-layout">
            <div class="filter-col">
              <span class="filter-label">身份域名称</span>
              <el-input v-model="query.name" placeholder="请输入身份域名称" clearable class="input-styled" @keyup.enter="handleSearch" />
            </div>
            <div class="filter-col">
              <span class="filter-label">身份域编码</span>
              <el-input v-model="query.code" placeholder="请输入身份域编码" clearable class="input-styled" @keyup.enter="handleSearch" />
            </div>
            <div class="filter-col">
              <span class="filter-label">身份域类型</span>
              <el-select v-model="query.typeCategoryId" placeholder="全部" clearable class="select-styled">
                <el-option v-for="item in typeCategoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="filter-col">
              <span class="filter-label">状态</span>
              <el-select v-model="query.status" placeholder="全部" clearable class="select-styled">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="2" />
              </el-select>
            </div>
          </div>

          <div class="filter-row-layout second-filter-row">
            <div class="filter-col">
              <span class="filter-label">认证方式</span>
              <el-select v-model="query.authMethod" placeholder="全部" clearable class="select-styled">
                <el-option label="密码" value="password" />
                <el-option label="短信" value="sms" />
                <el-option label="MFA" value="mfa" />
                <el-option label="邮箱" value="email" />
                <el-option label="证书" value="cert" />
              </el-select>
            </div>
            <div class="filter-col date-col">
              <span class="filter-label">创建时间</span>
              <el-date-picker
                v-model="query.createTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="date-picker-styled"
              />
            </div>
          </div>

          <div class="filter-actions-row">
            <el-button type="primary" :icon="Search" class="search-action" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button link class="btn-toggle-expand">
              展开
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="realm-table-card">
        <el-table v-loading="loading" :data="tableData" row-key="id" border class="realm-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="name" label="身份域名称" min-width="150" />
          <el-table-column prop="code" label="身份域编码" min-width="150" />
          <el-table-column label="身份域类型" min-width="110">
            <template #default="{ row }">
              <span class="type-category-tag" :class="getCategoryTypeClass(row.typeCategoryId)">
                {{ typeCategoryText(row.typeCategoryId) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="认证方式" min-width="170">
            <template #default="{ row }">
              {{ getAuthMethodsText(row) }}
            </template>
          </el-table-column>
          <el-table-column label="登录页" min-width="130">
            <template #default="{ row }">
              {{ getLoginPageText(row) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="status-cell" :class="row.status === 1 ? 'status-enabled' : 'status-disabled'">
                <span class="dot"></span> {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button link type="primary" @click="openEditPage(row)">编辑</el-button>
                <el-button link type="primary" @click="openDetail(row)">策略</el-button>
                <el-dropdown trigger="click">
                  <el-button link type="primary">
                    更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :page-sizes="[10, 20, 50]"
            layout="sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </el-card>
    </template>

  </section>
</template>

<style scoped>
.realm-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  color: #172033;
}

.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.heading-text h1 {
  margin: 0 0 10px;
  color: #101828;
  font-size: 26px;
  font-weight: 700;
  line-height: 36px;
}

.heading-text p {
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 22px;
}

.learn-more-link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-left: 8px;
  color: #1677ff;
  text-decoration: none;
}

.learn-more-link:hover {
  color: #0958d9;
}

.heading-actions {
  display: flex;
  flex: none;
  gap: 16px;
  padding-top: 8px;
}

.heading-actions :deep(.el-button) {
  height: 40px;
  margin: 0;
  padding: 0 20px;
  border-radius: 4px;
}

.heading-actions .primary-action {
  min-width: 130px;
  color: #fff;
  background: #1677ff;
  border-color: #1677ff;
}

.heading-actions .refresh-action {
  min-width: 96px;
  color: #344054;
  background: #fff;
  border-color: #d0d5dd;
}

.filter-card {
  margin-bottom: 24px;
  border: 1px solid #eaecf0;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(16 24 40 / 4%);
}

.filter-card :deep(.el-card__body) {
  padding: 28px 24px 18px;
}

.filter-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-row-layout {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px;
}

.filter-col {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.second-filter-row {
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr) minmax(0, 1fr);
}

.date-col {
  grid-column: span 2;
}

.filter-label {
  width: auto;
  color: #344054;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.input-styled,
.select-styled {
  width: 100%;
}

.input-styled :deep(.el-input__wrapper),
.select-styled :deep(.el-input__wrapper) {
  min-height: 34px;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 0 0 1px #d0d5dd inset;
}

.input-styled :deep(.el-input__wrapper.is-focus),
.select-styled :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1677ff inset;
}

.date-picker-styled {
  width: 100% !important;
  min-height: 34px;
  border-radius: 4px;
  background: #fff !important;
  box-shadow: 0 0 0 1px #d0d5dd inset !important;
}

.filter-actions-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: -2px;
}

.filter-actions-row :deep(.el-button) {
  height: 34px;
  margin: 0;
  padding: 0 20px;
  border-radius: 4px;
}

.filter-actions-row .search-action {
  color: #fff;
  background: #1677ff;
  border-color: #1677ff;
}

.btn-toggle-expand {
  padding-right: 4px !important;
  padding-left: 16px !important;
  color: #1677ff;
  font-size: 14px;
}

.realm-table-card {
  border: 1px solid #eaecf0;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(16 24 40 / 4%);
}

.realm-table-card :deep(.el-card__body) {
  padding: 0;
}

.realm-table {
  border: none;
  background: #fff;
}

.realm-table :deep(.el-table__inner-wrapper::before),
.realm-table :deep(.el-table__border-left-patch) {
  display: none;
}

.realm-table :deep(.el-table__header-wrapper) {
  border-bottom: 1px solid #eaecf0;
}

.realm-table :deep(th.el-table__cell) {
  height: 54px;
  padding: 0;
  color: #344054;
  background: #f8fafc;
  border-right: none;
  border-bottom: none;
  font-size: 14px;
  font-weight: 600;
}

.realm-table :deep(td.el-table__cell) {
  height: 66px;
  padding: 0;
  color: #344054;
  border-right: none;
  border-bottom: 1px solid #eaecf0;
  font-size: 14px;
}

.realm-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f8fbff;
}

.type-category-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-platform {
  background-color: #eff6ff;
  color: #2563eb;
  border: 1px solid #dbeafe;
}

.tag-tenant {
  background-color: #f0fdf4;
  color: #16a34a;
  border: 1px solid #dcfce7;
}

.tag-merchant {
  background-color: #fff7ed;
  color: #ea580c;
  border: 1px solid #ffedd5;
}

.tag-consumer {
  background-color: #faf5ff;
  color: #9333ea;
  border: 1px solid #f3e8ff;
}

.tag-provider {
  background-color: #f0fdfa;
  color: #0d9488;
  border: 1px solid #ccfbf1;
}

.tag-internal {
  background-color: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e5e7eb;
}

.tag-partner {
  background-color: #eff6ff;
  color: #2563eb;
  border: 1px solid #dbeafe;
}

.tag-default {
  background-color: #f9fafb;
  color: #6b7280;
  border: 1px solid #f3f4f6;
}

.status-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 24px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-cell .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #9ca3af;
}

.status-enabled {
  background-color: #f0fdf4;
  color: #16a34a;
  border: 1px solid #dcfce7;
}
.status-enabled .dot {
  background-color: #16a34a;
}

.status-disabled {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fee2e2;
}
.status-disabled .dot {
  background-color: #dc2626;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;
}

.row-actions :deep(.el-button.el-button--primary.is-link) {
  height: auto;
  margin: 0;
  padding: 0;
  color: #1677ff;
  background: transparent !important;
  border: 0 !important;
  border-radius: 0;
}

.row-actions :deep(.el-button.el-button--primary.is-link:hover) {
  color: #0958d9;
  background: transparent !important;
}

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 16px 24px;
}

.total-text {
  color: #344054;
  font-size: 14px;
}

.pagination-container :deep(.el-pagination) {
  color: #344054;
}

@media (max-width: 1280px) {
  .filter-row-layout {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .second-filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .date-col {
    grid-column: auto;
  }
}

@media (max-width: 768px) {
  .page-heading {
    flex-direction: column;
  }

  .heading-actions {
    width: 100%;
    padding-top: 0;
  }

  .filter-row-layout,
  .second-filter-row {
    grid-template-columns: 1fr;
  }

  .filter-col {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}

/* Full-page create realm view styling */
.create-realm-page {
  min-height: 100%;
}

.create-header {
  margin-bottom: 18px;
}

.back-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #111827;
  font-size: 24px;
  font-weight: 700;
  transition: color 0.2s;
}

.back-action:hover {
  color: #2563eb;
}

.back-icon {
  font-size: 20px;
}

.create-form {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eaecf0;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(16 24 40 / 4%);
}

.form-section-card {
  padding: 18px 30px 16px;
  border-bottom: 1px solid #eaecf0;
  background: #fff;
}

.section-title {
  margin-bottom: 14px;
  color: #172033;
  font-size: 15px;
  font-weight: 600;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 72px;
}

.grid-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.create-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.create-form :deep(.el-form-item__label) {
  color: #344054;
  font-weight: 500;
}

.create-form :deep(.el-input__wrapper),
.create-form :deep(.el-select__wrapper),
.create-form :deep(.el-textarea__inner) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #d0d5dd inset;
}

.create-form :deep(.el-checkbox) {
  margin-right: 18px;
}

.field-hint-text {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  line-height: 1.4;
}

.flex-row-align {
  display: flex;
  align-items: center;
  width: 100%;
}

.ml-12 {
  margin-left: 12px;
}

.inline-link {
  height: auto;
  padding: 0;
  color: #1677ff;
  font-size: 13px;
}

.form-footer-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
}

.form-footer-actions :deep(.el-button) {
  height: 36px;
  border-radius: 4px;
}

.btn-save,
.btn-cancel {
  min-width: 100px;
}

.btn-save-continue {
  min-width: 120px;
}

@media (max-width: 960px) {
  .form-grid-2 {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
