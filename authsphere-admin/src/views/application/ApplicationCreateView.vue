<script setup lang="ts">
import { ArrowLeft, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { appApi, type AppPayload } from '@/api/app'
import { authPolicyApi, type AuthPolicyOptionResponse } from '@/api/authPolicy'
import { loginPageApi, type LoginPageRecord } from '@/api/loginPage'
import { realmApi, type RealmRecord } from '@/api/realm'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const DRAFT_KEY = 'authsphere:application-create-draft'
const CLIENT_DRAFT_KEY = 'authsphere:application-create-clients-draft'

const formData = reactive<AppPayload>({
  appName: '商城应用',
  appCode: 'mall_app',
  appType: 'WEB',
  entryUrl: '',
  status: 1,
  description: '商城业务应用，包含平台管理、商家经营、消费者移动入口。',
})

const rules: FormRules<AppPayload> = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { max: 64, message: '应用名称不能超过 64 个字符', trigger: 'blur' },
  ],
  appCode: [
    { required: true, message: '请输入应用编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]{1,63}$/, message: '编码需以小写字母开头，仅支持小写字母、数字和下划线', trigger: 'blur' },
  ],
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }],
}

// Client template cards list
const clientsList = ref<any[]>([
  {
    clientName: '商城平台端',
    clientCode: 'mall_admin_client',
    clientType: 'PC', // 'PC', 'MOBILE', 'SERVICE'
    loginMode: 'IAM_HOSTED',
    defaultRealmId: null,
    loginPageId: null,
    authPolicyId: null,
    externalLoginUrl: '',
    loginCallbackUrl: '',
    ossConfigId: null,
    entryUrl: 'http://localhost:8080/mall/login',
    redirectUrl: 'http://localhost:8080/mall/dashboard',
    failUrl: 'http://localhost:8080/login?error=1',
    authPolicy: '账号密码 + TOTP'
  },
  {
    clientName: '消费者小程序',
    clientCode: 'buyer_mini_client',
    clientType: 'MOBILE',
    loginMode: 'IAM_HOSTED',
    defaultRealmId: null,
    loginPageId: null,
    authPolicyId: null,
    externalLoginUrl: '',
    loginCallbackUrl: 'http://localhost:8080/auth/wechat/callback',
    ossConfigId: null,
    mobileType: '微信小程序', // '微信小程序', 'H5 (轻量移动网页)', '原生 App'
    homePath: 'http://localhost:8080/pages/index/index',
    authCallback: 'http://localhost:8080/auth/wechat/callback',
    firstLogin: '要求绑定手机号',
    authPolicy: '微信小程序登录'
  },
  {
    clientName: '开放 API',
    clientCode: 'mall_open_api',
    clientType: 'SERVICE',
    loginMode: 'SERVICE',
    defaultRealmId: null,
    authPolicyId: null,
    ossConfigId: null,
    authMode: 'Client Credentials',
    signMethod: 'HMAC-SHA256',
    ipWhitelist: '可配置',
    rateLimit: '100 次 / 分钟',
    authPolicy: 'Client Credentials'
  }
])

const realmOptions = ref<RealmRecord[]>([])
const loginPageOptions = ref<LoginPageRecord[]>([])
const authPolicyOptions = ref<AuthPolicyOptionResponse[]>([])

const loadClientConfigOptions = async () => {
  try {
    const [realmPage, loginPages, authPolicies] = await Promise.all([
      realmApi.page({ page: 1, size: 100, status: 1 }),
      loginPageApi.list(),
      authPolicyApi.list()
    ])
    realmOptions.value = realmPage.records ?? []
    loginPageOptions.value = loginPages ?? []
    authPolicyOptions.value = authPolicies ?? []
  } catch (error: any) {
    ElMessage.error(error.message || '加载客户端配置选项失败')
  }
}

const appTypeOptions = [
  { label: 'Web 应用', value: 'WEB', description: '浏览器访问的后台、门户或业务系统' },
  { label: '移动应用', value: 'MOBILE', description: '原生 App、小程序或移动端应用' },
  { label: 'API 服务', value: 'API', description: '开放接口或服务间调用入口' },
  { label: '三方应用', value: 'THIRD_PARTY', description: '由外部系统接入的应用定义' },
]

const handleBack = () => {
  router.push('/applications')
}

// Add a default client card
const addClient = () => {
  const index = clientsList.value.length + 1
  clientsList.value.push({
    clientName: `新建客户端_${index}`,
    clientCode: `new_client_${index}`,
    clientType: 'PC',
    loginMode: 'IAM_HOSTED',
    defaultRealmId: null,
    loginPageId: null,
    authPolicyId: null,
    externalLoginUrl: '',
    loginCallbackUrl: '',
    ossConfigId: null,
    entryUrl: 'http://localhost:8080/login',
    redirectUrl: 'http://localhost:8080/dashboard',
    failUrl: 'http://localhost:8080/error',
    authPolicy: '账号密码'
  })
}

// Remove a client card
const removeClient = (index: number) => {
  clientsList.value.splice(index, 1)
}

// Handle change of client terminal type to reset preview defaults
const changeClientType = (client: any, type: string) => {
  client.clientType = type
  if (type === 'PC') {
    client.loginMode = client.loginMode === 'SERVICE' ? 'IAM_HOSTED' : client.loginMode || 'IAM_HOSTED'
    client.entryUrl = client.entryUrl || 'http://localhost:8080/login'
    client.redirectUrl = client.redirectUrl || 'http://localhost:8080/dashboard'
    client.failUrl = client.failUrl || 'http://localhost:8080/error'
    client.authPolicy = '账号密码'
  } else if (type === 'MOBILE') {
    client.loginMode = client.loginMode === 'SERVICE' ? 'IAM_HOSTED' : client.loginMode || 'IAM_HOSTED'
    client.entryUrl = client.entryUrl || client.homePath || 'http://localhost:8080/pages/index/index'
    client.mobileType = '微信小程序'
    client.homePath = client.homePath || 'http://localhost:8080/pages/index/index'
    client.authCallback = client.authCallback || 'http://localhost:8080/auth/wechat/callback'
    client.firstLogin = '要求绑定手机号'
    client.authPolicy = '微信小程序登录'
  } else if (type === 'SERVICE') {
    client.loginMode = 'SERVICE'
    client.loginPageId = null
    client.externalLoginUrl = ''
    client.authMode = 'Client Credentials'
    client.signMethod = 'HMAC-SHA256'
    client.ipWhitelist = '可配置'
    client.rateLimit = '100 次 / 分钟'
    client.authPolicy = 'Client Credentials'
  }
}

const loginModeOptions = [
  { label: '认证中心托管登录页', value: 'IAM_HOSTED' },
  { label: '跳转客户自有登录页', value: 'EXTERNAL_REDIRECT' },
  { label: '客户自有登录体系，仅接口接入', value: 'API_ONLY' }
]

const changeLoginMode = (client: any, mode: string) => {
  client.loginMode = mode
  if (mode !== 'IAM_HOSTED') {
    client.loginPageId = null
  }
  if (mode !== 'EXTERNAL_REDIRECT') {
    client.externalLoginUrl = ''
  }
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    // Map clientsList to AppClientPayload structure
    const payloadClients = clientsList.value.map(client => {
      let finalClientType = 1 // default ADMIN_WEB
      if (client.clientType === 'PC') {
        finalClientType = 1
      } else if (client.clientType === 'MOBILE') {
        finalClientType = client.mobileType === 'H5 (轻量移动网页)' ? 4 : 3
      } else if (client.clientType === 'SERVICE') {
        finalClientType = client.authMode === 'Client Credentials' ? 5 : 6
      }

      let desc = client.clientName + '客户端。'
      if (client.clientType === 'MOBILE') {
        desc += `形态: ${client.mobileType} | 首页: ${client.homePath} | 回调: ${client.authCallback}`
      } else if (client.clientType === 'SERVICE') {
        desc += `授权模式: ${client.authMode} | 签名: ${client.signMethod} | 限流: ${client.rateLimit}`
      } else {
        desc += `登录入口: ${client.entryUrl || ''} | 跳转: ${client.redirectUrl}`
      }

      return {
        clientCode: client.clientCode.trim(),
        clientName: client.clientName.trim(),
        clientType: finalClientType,
        status: 1,
        realmId: client.defaultRealmId || undefined,
        defaultRealmId: client.defaultRealmId || undefined,
        loginMode: client.clientType === 'SERVICE' ? 'SERVICE' : client.loginMode || 'IAM_HOSTED',
        externalLoginUrl: client.loginMode === 'EXTERNAL_REDIRECT' ? client.externalLoginUrl || undefined : undefined,
        loginCallbackUrl: client.loginCallbackUrl || client.authCallback || undefined,
        loginPageId: client.loginMode === 'IAM_HOSTED' ? client.loginPageId || undefined : undefined,
        authPolicyId: client.authPolicyId || undefined,
        ossConfigId: client.ossConfigId || undefined,
        defaultEntryUrl: client.entryUrl || client.homePath || '',
        description: desc
      }
    })

    const payload: AppPayload = {
      appName: formData.appName.trim(),
      appCode: formData.appCode.trim(),
      appType: formData.appType,
      entryUrl: formData.entryUrl?.trim() || undefined,
      status: formData.status,
      description: formData.description?.trim() || undefined,
      clients: payloadClients
    }

    await appApi.create(payload)
    
    sessionStorage.removeItem(DRAFT_KEY)
    sessionStorage.removeItem(CLIENT_DRAFT_KEY)
    ElMessage.success('应用及客户端模板保存成功')
    router.push('/applications')
  } catch (error: any) {
    ElMessage.error(error.message || '创建应用及客户端失败')
  } finally {
    submitLoading.value = false
  }
}

watch(
  formData,
  value => sessionStorage.setItem(DRAFT_KEY, JSON.stringify(value)),
  { deep: true },
)

watch(
  clientsList,
  value => sessionStorage.setItem(CLIENT_DRAFT_KEY, JSON.stringify(value)),
  { deep: true },
)

onMounted(() => {
  loadClientConfigOptions()
  const draft = sessionStorage.getItem(DRAFT_KEY)
  if (draft) {
    try {
      Object.assign(formData, JSON.parse(draft))
    } catch (e) {
      sessionStorage.removeItem(DRAFT_KEY)
    }
  }

  const clientDraft = sessionStorage.getItem(CLIENT_DRAFT_KEY)
  if (clientDraft) {
    try {
      clientsList.value = JSON.parse(clientDraft)
    } catch (e) {
      sessionStorage.removeItem(CLIENT_DRAFT_KEY)
    }
  }
})
</script>

<template>
  <div class="application-create-page-wrapper">
    <!-- Header -->
    <div class="form-navigation-header-row">
      <div class="nav-title-left">
        <div class="breadcrumb-trail">应用管理 / 新增应用</div>
        <h1>新增应用</h1>
        <p class="subtitle-text">一个页面完成基础信息、客户端模板、登录入口与资源范围预设；登录入口由客户端承接，身份域只提供默认认证与 SSO 规则。</p>
      </div>
      <div class="nav-buttons-right">
        <el-button class="btn-op-outline" @click="handleBack">返回列表</el-button>
      </div>
    </div>

    <!-- Status Flow Progress Indicators -->
    <div class="create-flow-steps-card">
      <div class="step-progress-item completed">
        <div class="step-num">1</div>
        <div class="step-details">
          <strong>1. 应用基础信息</strong>
          <span>定义应用身份</span>
        </div>
      </div>
      <div class="step-arrow"></div>
      <div class="step-progress-item active">
        <div class="step-num">2</div>
        <div class="step-details">
          <strong>2. 客户端模板</strong>
          <span>区分 PC / 移动端 / 服务端</span>
        </div>
      </div>
      <div class="step-arrow"></div>
      <div class="step-progress-item pending">
        <div class="step-num">3</div>
        <div class="step-details">
          <strong>3. 创建预览</strong>
          <span>创建后进入详情维护完整资源</span>
        </div>
      </div>
    </div>

    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top">
      <!-- Section 1: 基础信息 -->
      <div class="form-section-card-custom">
        <div class="card-title-header-custom">
          <h3>基础信息</h3>
          <p>定义应用基础属性与适用范围。</p>
          <span class="right-header-tip">新增时不配置接口明细，接口后续在详情中按客户端维护。</span>
        </div>
        <div class="card-body-custom">
          <div class="grid-2-col">
            <div>
              <el-form-item label="应用名称 *" prop="appName">
                <el-input v-model="formData.appName" placeholder="如：商城应用" />
              </el-form-item>
              <el-form-item label="应用编码 *" prop="appCode" class="mt-16">
                <el-input v-model="formData.appCode" placeholder="如：mall_app" />
              </el-form-item>
            </div>
            <div>
              <el-form-item label="应用类型 *" prop="appType">
                <el-select v-model="formData.appType" placeholder="请选择应用类型">
                  <el-option
                    v-for="item in appTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="应用说明" class="mt-16">
                <el-input
                  v-model="formData.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入应用说明，例如主营业务、入口描述等"
                />
              </el-form-item>
            </div>
          </div>
        </div>
      </div>

      <!-- Section 2: 客户端模板 -->
      <div class="clients-template-section-header">
        <div class="section-title-wrap">
          <h2>客户端模板</h2>
          <p>客户端类型表示终端形态，登录入口、默认身份域和覆盖认证策略按客户端配置。</p>
        </div>
        <el-button type="primary" class="btn-add-client-card" @click="addClient">添加客户端</el-button>
      </div>

      <!-- Clients List Cards -->
      <div class="clients-cards-list-wrap">
        <div v-for="(client, index) in clientsList" :key="index" class="client-card-item-box">
          
          <!-- Card Header Bar -->
          <div class="client-card-header-bar">
            <div class="header-badge-title-group">
              <span class="terminal-type-badge" :class="client.clientType.toLowerCase()">
                {{ client.clientType === 'SERVICE' ? 'API' : client.clientType === 'MOBILE' ? 'M' : 'PC' }}
              </span>
              <span class="client-card-title">{{ client.clientName }}</span>
              <span class="client-meta-tag client-type">{{ client.clientType === 'SERVICE' ? '服务端' : client.clientType === 'MOBILE' ? '移动端' : 'PC' }}</span>
              <span class="client-meta-code">{{ client.clientCode }}</span>
            </div>
            <el-button class="btn-delete-card-text" type="danger" link @click="removeClient(index)">删除</el-button>
          </div>

          <!-- Card Inner Grid (Modern 2-Column Layout: Basic Info | Specific Config) -->
          <div class="client-card-inner-grid-layout">
            
            <!-- Column 1: Basic Parameters -->
            <div class="client-basic-fields-col">
              <el-form-item label="客户端名称 *">
                <el-input v-model="client.clientName" />
              </el-form-item>
              <el-form-item label="客户端编码 *" class="mt-12">
                <el-input v-model="client.clientCode" />
              </el-form-item>

              <!-- Terminal Type choices -->
              <el-form-item label="终端类型 *" class="mt-12">
                <div class="checkbox-cards-grid-3">
                  <div 
                    class="checkbox-card-item" 
                    :class="{ checked: client.clientType === 'PC' }" 
                    @click="changeClientType(client, 'PC')"
                  >
                    <div class="flex-align-center-row">
                      <span class="custom-radio-circle" :class="{ checked: client.clientType === 'PC' }"></span>
                      <span class="checkbox-title ml-8">PC</span>
                    </div>
                    <p class="checkbox-desc">网页、应用</p>
                  </div>
                  <div 
                    class="checkbox-card-item" 
                    :class="{ checked: client.clientType === 'MOBILE' }" 
                    @click="changeClientType(client, 'MOBILE')"
                  >
                    <div class="flex-align-center-row">
                      <span class="custom-radio-circle" :class="{ checked: client.clientType === 'MOBILE' }"></span>
                      <span class="checkbox-title ml-8">移动端</span>
                    </div>
                    <p class="checkbox-desc">页面、接口</p>
                  </div>
                  <div 
                    class="checkbox-card-item" 
                    :class="{ checked: client.clientType === 'SERVICE' }" 
                    @click="changeClientType(client, 'SERVICE')"
                  >
                    <div class="flex-align-center-row">
                      <span class="custom-radio-circle" :class="{ checked: client.clientType === 'SERVICE' }"></span>
                      <span class="checkbox-title ml-8">服务端</span>
                    </div>
                    <p class="checkbox-desc">接口、限流</p>
                  </div>
                </div>
              </el-form-item>

              <el-form-item v-if="client.clientType !== 'SERVICE'" label="登录接入方式 *" class="mt-12">
                <el-select v-model="client.loginMode" placeholder="请选择登录接入方式" style="width: 100%" @change="changeLoginMode(client, $event)">
                  <el-option
                    v-for="item in loginModeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </div>

            <!-- Column 2: Terminal Specific Config Fields -->
            <div class="client-detail-fields-col">
              <!-- PC Details -->
              <div v-if="client.clientType === 'PC'" class="detail-fields-grid">
                <el-form-item label="默认身份域">
                  <el-select v-model="client.defaultRealmId" placeholder="请选择默认身份域" clearable style="width: 100%">
                    <el-option v-for="realm in realmOptions" :key="realm.id" :label="realm.name" :value="realm.id" />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="client.loginMode === 'IAM_HOSTED'" label="登录入口模板">
                  <el-select v-model="client.loginPageId" placeholder="请选择入口模板" clearable style="width: 100%">
                    <el-option v-for="page in loginPageOptions" :key="page.id" :label="page.name" :value="page.id" />
                  </el-select>
                </el-form-item>
                <el-form-item v-else-if="client.loginMode === 'EXTERNAL_REDIRECT'" label="客户登录页地址">
                  <el-input v-model="client.externalLoginUrl" placeholder="如：https://customer.example.com/login" />
                </el-form-item>
                <el-form-item v-else label="接口接入说明">
                  <el-input model-value="客户系统自有登录体系，仅调用注册账户与获取登录信息接口，不做登录页转发。" disabled />
                </el-form-item>
                <el-form-item label="默认入口地址 *">
                  <el-input v-model="client.entryUrl" placeholder="如：http://localhost:8080/mall/login" />
                </el-form-item>
                <el-form-item label="成功跳转 *">
                  <el-input v-model="client.redirectUrl" placeholder="如：http://localhost:8080/mall/dashboard" />
                </el-form-item>
                <el-form-item label="覆盖认证策略">
                  <el-select v-model="client.authPolicyId" placeholder="默认使用身份域策略" clearable style="width: 100%">
                    <el-option v-for="policy in authPolicyOptions" :key="policy.id" :label="policy.name" :value="policy.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="失败跳转">
                  <el-input v-model="client.failUrl" placeholder="如：http://localhost:8080/login?error=1" />
                </el-form-item>
                <el-form-item label="登录回调地址" class="span-2-cols">
                  <el-input v-model="client.loginCallbackUrl" placeholder="如：https://app.example.com/auth/callback" />
                </el-form-item>
              </div>

              <!-- Mobile Details -->
              <div v-else-if="client.clientType === 'MOBILE'" class="detail-fields-grid">
                <el-form-item label="默认身份域">
                  <el-select v-model="client.defaultRealmId" placeholder="请选择默认身份域" clearable style="width: 100%">
                    <el-option v-for="realm in realmOptions" :key="realm.id" :label="realm.name" :value="realm.id" />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="client.loginMode === 'IAM_HOSTED'" label="登录入口模板">
                  <el-select v-model="client.loginPageId" placeholder="请选择入口模板" clearable style="width: 100%">
                    <el-option v-for="page in loginPageOptions" :key="page.id" :label="page.name" :value="page.id" />
                  </el-select>
                </el-form-item>
                <el-form-item v-else-if="client.loginMode === 'EXTERNAL_REDIRECT'" label="客户登录页地址">
                  <el-input v-model="client.externalLoginUrl" placeholder="如：https://customer.example.com/mobile-login" />
                </el-form-item>
                <el-form-item v-else label="接口接入说明">
                  <el-input model-value="移动端自有登录体系，仅通过接口同步账号与登录信息。" disabled />
                </el-form-item>
                <el-form-item label="移动端形态 *">
                  <el-select v-model="client.mobileType" placeholder="选择终端" style="width: 100%">
                    <el-option label="微信小程序" value="微信小程序" />
                    <el-option label="H5 (轻量移动网页)" value="H5 (轻量移动网页)" />
                    <el-option label="原生 App" value="原生 App" />
                  </el-select>
                </el-form-item>
                <el-form-item label="首页路径 *">
                  <el-input v-model="client.homePath" placeholder="如：http://localhost:8080/pages/index/index" />
                </el-form-item>
                <el-form-item label="覆盖认证策略">
                  <el-select v-model="client.authPolicyId" placeholder="默认使用身份域策略" clearable style="width: 100%">
                    <el-option v-for="policy in authPolicyOptions" :key="policy.id" :label="policy.name" :value="policy.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="首次登录 *">
                  <el-select v-model="client.firstLogin" style="width: 100%">
                    <el-option label="要求绑定手机号" value="要求绑定手机号" />
                    <el-option label="直接进入主页" value="直接进入主页" />
                  </el-select>
                </el-form-item>
                <el-form-item label="授权回调" class="span-2-cols">
                  <el-input v-model="client.loginCallbackUrl" placeholder="如：http://localhost:8080/auth/wechat/callback" />
                </el-form-item>
              </div>

              <!-- Service Details -->
              <div v-else class="detail-fields-grid">
                <el-form-item label="默认身份域">
                  <el-select v-model="client.defaultRealmId" placeholder="请选择默认身份域" clearable style="width: 100%">
                    <el-option v-for="realm in realmOptions" :key="realm.id" :label="realm.name" :value="realm.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="授权模式 *">
                  <el-select v-model="client.authMode" style="width: 100%">
                    <el-option label="Client Credentials" value="Client Credentials" />
                    <el-option label="Authorization Code" value="Authorization Code" />
                  </el-select>
                </el-form-item>
                <el-form-item label="签名方式 *">
                  <el-select v-model="client.signMethod" placeholder="签名类型" style="width: 100%">
                    <el-option label="HMAC-SHA256" value="HMAC-SHA256" />
                    <el-option label="RSA-SHA256" value="RSA-SHA256" />
                  </el-select>
                </el-form-item>
                <el-form-item label="认证策略">
                  <el-select v-model="client.authPolicyId" placeholder="选择接口认证策略" clearable style="width: 100%">
                    <el-option v-for="policy in authPolicyOptions" :key="policy.id" :label="policy.name" :value="policy.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="IP 白名单">
                  <el-input v-model="client.ipWhitelist" placeholder="如：127.0.0.1, 10.0.0.0/8" />
                </el-form-item>
                <el-form-item label="接口限流" class="span-2-cols">
                  <el-input v-model="client.rateLimit" placeholder="例如：100 次 / 分钟" />
                </el-form-item>
              </div>
            </div>

          </div>

        </div>
      </div>

      <!-- Action Footer row -->
      <div class="bottom-actions-row">
        <el-button class="btn-op-outline" @click="handleBack">取消</el-button>
        <el-button class="btn-op-outline ml-12">保存草稿</el-button>
        <el-button type="primary" class="btn-create-app-submit ml-12" :loading="submitLoading" @click="submitForm">
          创建应用
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.application-create-page-wrapper {
  width: 100%;
  padding: 24px;
  box-sizing: border-box;
  color: #334155;
}

/* Header */
.form-navigation-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.breadcrumb-trail {
  font-size: 12px;
  color: #64748B;
  margin-bottom: 6px;
}
.nav-title-left h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
}
.subtitle-text {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #64748B;
  line-height: 1.5;
}
.nav-buttons-right {
  display: flex;
  gap: 12px;
}
.btn-op-outline {
  height: 36px;
  border: 1px solid #E2E8F0;
  background-color: #fff;
  font-weight: 600;
  color: #475569;
  border-radius: 6px;
  font-size: 13px;
}
.btn-op-outline:hover {
  border-color: #2563EB;
  color: #2563EB;
}

/* Steps Progress Card */
.create-flow-steps-card {
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}
.step-progress-item {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.step-progress-item.completed .step-num {
  border-color: #10B981;
  background-color: #F0FDF4;
  color: #10B981;
}
.step-progress-item.active .step-num {
  border-color: #2563EB;
  background-color: #EFF6FF;
  color: #2563EB;
}
.step-progress-item.pending .step-num {
  border-color: #CBD5E1;
  background-color: #F8FAFC;
  color: #64748B;
}
.step-num {
  width: 36px;
  height: 36px;
  border: 1px solid;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
}
.step-details {
  display: flex;
  flex-direction: column;
}
.step-details strong {
  font-size: 13.5px;
  color: #0F172A;
  font-weight: 700;
}
.step-details span {
  font-size: 11px;
  color: #64748B;
  margin-top: 2px;
}
.step-arrow {
  width: 24px;
  height: 1px;
  background-color: #E2E8F0;
  margin: 0 20px;
}

/* Form Cards */
.form-section-card-custom {
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.card-title-header-custom {
  margin-bottom: 20px;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 16px;
  display: flex;
  align-items: center;
  position: relative;
}
.card-title-header-custom h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.card-title-header-custom p {
  margin: 0 0 0 12px;
  font-size: 12px;
  color: #64748B;
}
.right-header-tip {
  position: absolute;
  right: 0;
  font-size: 12px;
  color: #64748B;
}
.card-body-custom {
  padding-top: 4px;
}

/* Clients template section header */
.clients-template-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 28px;
  margin-bottom: 16px;
}
.section-title-wrap h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
}
.section-title-wrap p {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #64748B;
}
.btn-add-client-card {
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
  height: 36px;
  border-radius: 6px;
}
.btn-add-client-card:hover {
  background-color: #1D4ED8;
  border-color: #1D4ED8;
}

/* Client Card Items */
.clients-cards-list-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.client-card-item-box {
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.client-card-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 16px;
  margin-bottom: 20px;
}
.header-badge-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}
.terminal-type-badge {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 800;
  color: #fff;
}
.terminal-type-badge.pc {
  background-color: #3B82F6;
}
.terminal-type-badge.mobile {
  background-color: #EC4899;
}
.terminal-type-badge.service {
  background-color: #10B981;
}

.client-card-title {
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
}
.client-meta-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.client-meta-tag.client-type {
  background-color: #EFF6FF;
  color: #2563EB;
}
.client-meta-tag.client-entry {
  background-color: #F8FAFC;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.client-meta-code {
  font-size: 12px;
  color: #64748B;
  font-family: monospace;
}
.btn-delete-card-text {
  font-weight: 600;
  font-size: 13px;
}

/* Card inner grid */
.client-card-inner-grid-layout {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 32px;
}
@media (max-width: 992px) {
  .client-card-inner-grid-layout {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

.client-basic-fields-col {
  display: flex;
  flex-direction: column;
}

.client-detail-fields-col {
  display: flex;
  flex-direction: column;
}

.detail-fields-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}

.span-2-cols {
  grid-column: span 2;
}

.client-card-item-box :deep(.el-input),
.client-card-item-box :deep(.el-select) {
  width: 100%;
  max-width: 100%;
}

/* Form items override inside client cards */
.client-card-item-box :deep(.el-form-item) {
  margin-bottom: 0;
}
.client-card-item-box :deep(.el-form-item__label) {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
  padding: 0;
  line-height: normal;
}
.client-card-item-box :deep(.el-input__wrapper),
.client-card-item-box :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  box-shadow: none !important;
}
.client-card-item-box :deep(.el-textarea__inner) {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  box-shadow: none !important;
}

/* Grid layout utilities */
.grid-2-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 20px;
}
.grid-3-col {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px 20px;
}
.mt-4 { margin-top: 4px; }
.mt-12 { margin-top: 12px; }
.mt-16 { margin-top: 16px; }
.mt-20 { margin-top: 20px; }
.ml-12 { margin-left: 12px; }
.ml-8 { margin-left: 8px; }

/* Checkbox cards choice styling */
.checkbox-cards-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.checkbox-card-item {
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 12px 14px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}
.checkbox-card-item:hover {
  border-color: #CBD5E1;
}
.checkbox-card-item.checked {
  border-color: #2563EB;
  background-color: #EFF6FF;
}
.checkbox-title {
  font-size: 13px;
  font-weight: 700;
  color: #1E293B;
}
.checkbox-desc {
  font-size: 11px;
  color: #64748B;
  margin: 4px 0 0 0;
  line-height: 1.4;
}
.flex-align-center-row {
  display: flex;
  align-items: center;
}
.custom-radio-circle {
  width: 14px;
  height: 14px;
  border: 1px solid #CBD5E1;
  border-radius: 50%;
  display: inline-block;
  position: relative;
  background-color: #fff;
}
.custom-radio-circle.checked {
  border-color: #2563EB;
  background-color: #2563EB;
}
.custom-radio-circle.checked::after {
  content: "";
  width: 6px;
  height: 6px;
  background-color: #fff;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* Entrance chips styling */
.entrance-chips-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.entry-chip-item {
  padding: 6px 14px;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
  font-size: 12.5px;
  color: #475569;
  background-color: #fff;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.15s ease;
}
.entry-chip-item:hover {
  border-color: #CBD5E1;
  color: #1E293B;
}
.entry-chip-item.active {
  background-color: #EFF6FF;
  border-color: #3B82F6;
  color: #2563EB;
  font-weight: 600;
}

/* PC Mockup Wireframe style */
.mockup-browser-wireframe {
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  height: 160px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 10px rgba(0,0,0,0.03);
  position: sticky;
  top: 16px;
}
.mockup-browser-head {
  height: 24px;
  background-color: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  padding: 0 8px;
}
.mock-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #CBD5E1;
  margin-right: 4px;
}
.mockup-url-bar {
  flex: 1;
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 4px;
  height: 14px;
  margin-left: 12px;
  font-size: 9px;
  color: #94A3B8;
  display: flex;
  align-items: center;
  padding: 0 6px;
}
.mockup-browser-body-grid {
  flex: 1;
  display: flex;
}
.mockup-sidebar-left {
  width: 50px;
  background-color: #F8FAFC;
  border-right: 1px solid #E2E8F0;
  padding: 12px 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.mock-block {
  height: 5px;
  background-color: #E2E8F0;
  border-radius: 2px;
}
.mock-block.w-80 { width: 80%; }
.mock-block.w-60 { width: 60%; }
.mock-block.w-70 { width: 70%; }
.mockup-content-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.mockup-header-bar {
  height: 20px;
  border-bottom: 1px solid #F1F5F9;
}
.mockup-inner-contents {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mock-line-holder {
  height: 6px;
  background-color: #F1F5F9;
  border-radius: 2px;
  width: 100%;
}
.mock-line-holder.short {
  width: 60%;
}

/* Phone Mockup Wireframe style */
.mockup-phone-wireframe {
  width: 140px;
  height: 180px;
  border: 4px solid #334155;
  border-radius: 20px;
  background-color: #fff;
  margin: 0 auto;
  position: sticky;
  top: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}
.phone-screen-notch {
  width: 50px;
  height: 10px;
  background-color: #334155;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}
.phone-screen-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.phone-header-placeholder {
  height: 32px;
  background-color: #EFF6FF;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #DBEAFE;
  padding-top: 8px;
}
.phone-top-title {
  font-size: 8px;
  color: #1D4ED8;
  font-weight: 700;
}
.phone-body-placeholders {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.phone-banner-mock {
  height: 40px;
  background-color: #EFF6FF;
  border-radius: 6px;
}
.phone-card-mock {
  height: 24px;
  border: 1px solid #F1F5F9;
  background-color: #fff;
  border-radius: 4px;
}

/* Terminal Mockup Wireframe style */
.mockup-terminal-code-box {
  background-color: #0F172A;
  border-radius: 8px;
  border: 1px solid #1E293B;
  padding: 10px 14px;
  height: 150px;
  display: flex;
  flex-direction: column;
  font-family: monospace;
  position: sticky;
  top: 16px;
}
.terminal-header-top {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.t-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}
.t-dot.red { background-color: #EF4444; }
.t-dot.yellow { background-color: #F59E0B; }
.t-dot.green { background-color: #10B981; }
.terminal-title {
  font-size: 9px;
  color: #64748B;
  margin-left: 8px;
}
.terminal-console-body {
  flex: 1;
  color: #38BDF8;
  font-size: 11px;
}
.cmd-line {
  display: block;
}

/* Alert Banner Styling */
.alert-banner-custom {
  background-color: #EFF6FF;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.alert-banner-custom .vertical-bar {
  width: 3px;
  height: 16px;
  background-color: #2563EB;
  border-radius: 2px;
}
.alert-content-text {
  font-size: 12.5px;
  color: #1E40AF;
  line-height: 1.5;
  font-weight: 500;
}

/* Card bottom fields configuration form */
.card-bottom-fields-form-wrap {
  border-top: 1px dashed #E2E8F0;
  padding-top: 20px;
}
.field-hint {
  font-size: 11px;
  color: #94A3B8;
  margin-top: 4px;
}

/* Bottom Footer Action buttons */
.bottom-actions-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 32px;
  border-top: 1px solid #E2E8F0;
  padding-top: 24px;
  padding-bottom: 32px;
}
.btn-create-app-submit {
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
  height: 36px;
  border-radius: 6px;
}
.btn-create-app-submit:hover {
  background-color: #1D4ED8;
  border-color: #1D4ED8;
}
</style>
