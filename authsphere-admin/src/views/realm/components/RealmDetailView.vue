<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ArrowLeft, EditPen, Warning, InfoFilled } from '@element-plus/icons-vue'
import type { RealmRecord } from '@/api/realm'
import { authMethodApi, type AuthMethodOptionResponse, type AuthMethodRecord } from '@/api/authMethod'

const props = defineProps<{
  realm: RealmRecord
  typeCategoryText?: string
}>()

const emit = defineEmits(['back', 'edit'])

const availableAuthMethods = ref<AuthMethodOptionResponse[]>([])

const fetchAvailableAuthMethods = async () => {
  try {
    availableAuthMethods.value = await authMethodApi.list()
  } catch (error) {
    console.error('Failed to load authentication methods', error)
  }
}

onMounted(fetchAvailableAuthMethods)

// Map category type text
const displayType = computed(() => {
  if (props.typeCategoryText && props.typeCategoryText !== '-') {
    return props.typeCategoryText
  }
  const code = props.realm.code
  if (code.includes('tenant')) return '租户域'
  if (code.includes('platform')) return '平台域'
  if (code.includes('merchant')) return '商户域'
  if (code.includes('consumer')) return '消费者域'
  return '系统内置域'
})

// Mapped form structure from props.realm to resemble creation form state
const detailForm = computed(() => {
  const row = props.realm
  const defaultAuthMethodCode = row.authMethodList?.find(m => String(m.id) === String((row as any).defaultAuthMethodId))?.code
  const mfaAuthMethodCode = row.authMethodList?.find(m => String(m.id) === String((row as any).mfaAuthMethodId))?.code
  
  return {
    name: row.name,
    code: row.code,
    typeCategoryId: row.typeCategoryId || row.realmTypeId || '',
    typeCategoryCode: '',
    description: row.description || '',
    status: row.status || 1,
    sortNo: 10,
    ssoEnabled: row.ssoEnabled !== false,
    ssoSessionTimeout: (row as any).ssoSessionTimeout || 8,
    ssoSingleLogout: (row as any).ssoSingleLogout || 'enabled',
    existingSessionHandler: (row as any).existingSessionHandler || 'auto_redirect',
    noClientIdHandler: (row as any).noClientIdHandler || 'show_app_list',
    authMethods: row.authMethodList && row.authMethodList.length > 0
      ? row.authMethodList.map(m => m.code)
      : [],
    sessionTimeout: (row as any).ssoSessionTimeout || 8,
    tokenTimeout: (row as any).accessTokenTimeout || 120,
    sslRequired: true,
    loginFailLock: false,
    remark: row.description || '',
    policyMethods: row.authMethodList && row.authMethodList.length > 0
      ? row.authMethodList.map(m => m.code)
      : [],
    policyDefaultMethod: defaultAuthMethodCode || '',
    policyMfa: mfaAuthMethodCode || 'none',
    policyCaptcha: (row as any).captchaMode || 'none',

    // Password Security
    passwordMinLength: (row as any).passwordMinLength || 8,
    passwordMaxLength: (row as any).passwordMaxLength || 32,
    passwordComplexity: (row as any).passwordComplexity || 'letters_digits',
    passwordExpireDays: (row as any).passwordExpireDays || 90,
    passwordForceChangeOnFirstLogin: 'yes',
    passwordForceChangeOnReset: 'yes',
    
    // Token Security
    accessTokenTimeout: (row as any).accessTokenTimeout || 120,
    refreshTokenTimeout: (row as any).refreshTokenTimeout || 7,
    tokenRotationEnabled: (row as any).tokenRotationEnabled === false ? 'close' : 'open',
    tokenBlacklistEnabled: (row as any).tokenBlacklistEnabled === false ? 'close' : 'open',
    
    // Account Lock
    loginFailMaxCount: String((row as any).loginFailMaxCount || 5),
    loginFailWindowMinutes: (row as any).loginFailWindowMinutes || 10,
    loginFailLockMinutes: (row as any).loginFailLockMinutes || 30,
    loginFailAutoUnlock: (row as any).loginFailAutoUnlock === false ? 'close' : 'open',
    
    // Session Security
    sessionIdleTimeout: (row as any).sessionIdleTimeout || (row as any).ssoIdleTimeout || 30,
    sessionMultiDevice: (row as any).sessionMultiDevice || 'allow',
    sessionMaxDevices: (row as any).sessionMaxDevices || 5
  }
})

const getMethodLabel = (method: string) => {
  if (method === 'password_login' || method === 'password') return '账号密码'
  if (method === 'totp_login' || method === 'totp') return 'TOTP 动态口令'
  if (method === 'extended_login' || method === 'extended') return '扩展登录'
  if (method === 'client_credentials') return 'Client Credentials'
  if (method === 'sms') return '短信'
  if (method === 'email') return '邮箱'
  return method
}

const mergeAuthMethodOptions = (options: AuthMethodOptionResponse[], selectedCodes: string[]) => {
  const optionMap = new Map<string, AuthMethodOptionResponse>()
  options.forEach(option => {
    optionMap.set(option.code, option)
  })
  selectedCodes.forEach(code => {
    if (!optionMap.has(code)) {
      optionMap.set(code, {
        id: code,
        code,
        name: getMethodLabel(code),
        description: '当前身份域已绑定的认证方式'
      })
    }
  })
  return Array.from(optionMap.values())
}

const displayAuthMethods = computed(() => mergeAuthMethodOptions(availableAuthMethods.value, detailForm.value.policyMethods))

const getMfaLabel = (mfa: string) => {
  if (mfa === 'totp') return 'TOTP'
  if (mfa === 'sms') return '短信'
  if (mfa === 'email') return '邮件'
  return '不启用'
}

const getSummaryMainLoginText = () => {
  return detailForm.value.policyMethods.map(method => getMethodLabel(method)).join('、')
}

const getSummaryMfaText = () => {
  if (detailForm.value.policyMfa === 'none') return '不启用'
  return getMfaLabel(detailForm.value.policyMfa)
}

const getSsoSessionText = () => {
  return detailForm.value.ssoEnabled
    ? `${detailForm.value.ssoSessionTimeout} 小时有效 / ${detailForm.value.sessionIdleTimeout} 分钟空闲超时`
    : '该身份域不共享跨客户端登录态'
}

const getSsoLogoutText = () => {
  return detailForm.value.ssoSingleLogout === 'enabled' ? '统一退出同域客户端' : '仅退出当前客户端'
}

const getNoClientHandlerText = () => {
  if (detailForm.value.noClientIdHandler === 'show_app_list') return '展示应用选择页'
  if (detailForm.value.noClientIdHandler === 'show_error') return '返回错误提示'
  return '跳转系统默认入口'
}

const getExistingSessionHandlerText = () => {
  if (detailForm.value.existingSessionHandler === 'auto_redirect') return '自动进入目标客户端'
  if (detailForm.value.existingSessionHandler === 'prompt') return '提示用户确认'
  return '展示应用选择页'
}

// Referenced objects for "影响范围" card
const mockImpactObjects = computed(() => {
  return [
    { name: props.realm.name + '管理端', code: props.realm.code + '_web', type: 'SSO 客户端范围', status: props.realm.status || 1 },
    { name: props.realm.name + '开放接口', code: props.realm.code + '_api', type: '绑定当前身份域', status: props.realm.status || 1 }
  ]
})
</script>

<template>
  <div class="create-realm-page-wrapper">
    <!-- Breadcrumb / Header Row -->
    <div class="form-navigation-header-row">
        <div class="nav-title-left">
          <h1>身份域详情</h1>
        <p class="subtitle-text">查看当前身份空间（{{ realm.name }}）的默认认证、SSO 会话和安全规则。</p>
      </div>
      <div class="nav-buttons-right">
        <el-button class="btn-op-outline" @click="emit('back')">返回列表</el-button>
        <el-button type="primary" class="btn-new-realm" @click="emit('edit', realm)">编辑</el-button>
      </div>
    </div>

    <div class="form-tabs-sub">
      <span class="tab-sub-item" @click="emit('back')">列表</span>
      <span class="tab-sub-item active">详情</span>
      <span class="tab-sub-item" @click="emit('edit', realm)">编辑 / 新增</span>
    </div>

    <!-- Two-column grid -->
    <div class="realm-form-two-column-layout">
      <!-- Left Column: Form Details (Read-only) -->
      <div class="form-left-column" style="width: 100%;">
        <el-form label-position="top" class="create-form read-only-form form-grid-layout">
          <div class="form-grid-left">
          
          <!-- Card 1: 基础信息 -->
          <div class="form-section-card-custom">
            <div class="card-title-header-custom">
              <h3>基础信息</h3>
              <p>定义身份空间的名称、编码、类型和状态。</p>
            </div>
            <div class="card-body-custom">
              <div class="grid-2-col">
                <el-form-item label="身份域名称">
                  <el-input :model-value="detailForm.name" disabled />
                </el-form-item>
                <el-form-item label="身份域编码">
                  <el-input :model-value="detailForm.code" disabled />
                </el-form-item>
              </div>
              <div class="grid-2-col mt-16">
                <el-form-item label="身份域类型">
                  <el-select :model-value="detailForm.typeCategoryId" disabled placeholder="选择类型">
                    <el-option :label="displayType" :value="detailForm.typeCategoryId" />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态">
                  <el-select :model-value="detailForm.status" disabled>
                    <el-option label="启用" :value="1" />
                    <el-option label="禁用" :value="2" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="full-width-field mt-16">
                <el-form-item label="说明">
                  <el-input :model-value="detailForm.description" type="textarea" :rows="3" disabled />
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- Card 2: SSO 基础规则 -->
          <div class="form-section-card-custom mt-20">
            <div class="card-title-header-custom">
              <h3>SSO 基础规则</h3>
              <p>配置该身份域的单点登录规则，控制跨应用的会话与退出行为。</p>
            </div>
            <div class="card-body-custom">
              <div class="grid-2-col">
                <el-form-item label="启用 SSO">
                  <el-radio-group :model-value="detailForm.ssoEnabled" class="premium-radio-group" disabled>
                    <el-radio-button :value="true">启用</el-radio-button>
                    <el-radio-button :value="false">停用</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="SSO 会话有效期">
                  <el-input :model-value="detailForm.ssoSessionTimeout" disabled>
                    <template #append>小时</template>
                  </el-input>
                </el-form-item>
              </div>
              <div class="grid-2-col mt-16">
                <el-form-item label="统一退出">
                  <el-radio-group :model-value="detailForm.ssoSingleLogout" class="premium-radio-group" disabled>
                    <el-radio-button value="enabled">启用</el-radio-button>
                    <el-radio-button value="current_client_only">仅退出当前客户端</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </div>
              <div class="grid-2-col mt-16">
                <el-form-item label="已有 SSO 会话时">
                  <el-select :model-value="detailForm.existingSessionHandler" disabled style="width: 100%">
                    <el-option label="自动进入目标客户端" value="auto_redirect" />
                    <el-option label="提示用户确认" value="prompt" />
                  </el-select>
                </el-form-item>
                <el-form-item label="未指定 client_id 时">
                  <el-select :model-value="detailForm.noClientIdHandler" disabled style="width: 100%">
                    <el-option label="展示应用选择页" value="show_app_list" />
                    <el-option label="展示错误提示页" value="show_error" />
                  </el-select>
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- Card 3: 默认认证策略 -->
          <div class="form-section-card-custom mt-20">
            <div class="card-title-header-custom">
              <h3>默认认证策略</h3>
              <p>当前身份域直接保存主认证方式、默认认证方式、MFA 和图形验证码规则。</p>
            </div>
            <div class="card-body-custom">
              <div class="policy-details-sub-card">
                <div class="full-width-field">
                  <el-form-item label="主登录认证方式">
                    <div class="checkbox-cards-grid-3" style="pointer-events: none;">
                      <div
                        v-for="method in displayAuthMethods"
                        :key="method.code"
                        class="checkbox-card-item"
                        :class="{ checked: detailForm.policyMethods.includes(method.code) }"
                      >
                        <el-checkbox
                          :model-value="detailForm.policyMethods.includes(method.code)"
                          disabled
                        >
                          <span class="checkbox-title">{{ method.name }}</span>
                        </el-checkbox>
                        <p class="checkbox-desc">{{ method.description || '自定义认证方式' }}</p>
                      </div>
                    </div>
                  </el-form-item>
                </div>

                <div class="grid-3-col mt-16">
                  <el-form-item label="默认认证方式">
                    <el-select :model-value="detailForm.policyDefaultMethod" disabled>
                      <el-option
                        v-if="!detailForm.policyMethods.includes(detailForm.policyDefaultMethod) && detailForm.policyDefaultMethod"
                        :label="getMethodLabel(detailForm.policyDefaultMethod)"
                        :value="detailForm.policyDefaultMethod"
                      />
                      <el-option
                        v-for="method in displayAuthMethods.filter(m => detailForm.policyMethods.includes(m.code))"
                        :key="method.code"
                        :label="method.name"
                        :value="method.code"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="MFA 认证方式">
                    <el-select :model-value="detailForm.policyMfa" disabled>
                      <el-option label="不启用" value="none" />
                      <el-option label="TOTP 动态口令" value="totp" />
                      <el-option label="短信 MFA" value="sms" />
                      <el-option label="邮件 MFA" value="email" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="图形验证码">
                    <el-select :model-value="detailForm.policyCaptcha" disabled>
                      <el-option label="不启用" value="none" />
                      <el-option label="失败 3 次后启用" value="threshold" />
                      <el-option label="每次登录都启用" value="always" />
                    </el-select>
                  </el-form-item>
                </div>
              </div>
            </div>
          </div>

          </div>
          <div class="form-grid-right">
            <!-- Card 4: 安全配置 -->
            <div class="form-section-card-custom">
            <div class="card-title-header-custom">
              <h3>安全配置</h3>
              <p>管理员设置的密码复杂度、Token 有效期、账号锁定及会话规则。</p>
            </div>
            
            <div class="card-body-custom" style="border-top: none; padding-top: 0;">
              <!-- 密码安全 -->
              <div class="sub-security-block">
                <div class="sub-sec-title">
                  <h4>密码安全</h4>
                  <p>控制密码复杂度、有效期和首次登录改密。</p>
                </div>
                <div class="grid-4-col mt-12">
                  <el-form-item label="最小长度">
                    <el-input-number :model-value="detailForm.passwordMinLength" disabled style="width: 100%;" />
                  </el-form-item>
                  <el-form-item label="最大长度">
                    <el-input-number :model-value="detailForm.passwordMaxLength" disabled style="width: 100%;" />
                  </el-form-item>
                  <el-form-item label="复杂度">
                    <el-select :model-value="detailForm.passwordComplexity" disabled>
                      <el-option label="数字 + 字母" value="letters_digits" />
                      <el-option label="数字 + 大小写 + 特殊字符" value="letters_digits_symbols" />
                      <el-option label="不限制" value="none" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="密码有效期">
                    <el-input :model-value="detailForm.passwordExpireDays" disabled>
                      <template #append>天</template>
                    </el-input>
                  </el-form-item>
                </div>
                <div class="grid-4-col mt-12">
                  <el-form-item label="首次登录改密">
                    <el-switch
                      :model-value="detailForm.passwordForceChangeOnFirstLogin"
                      active-value="yes"
                      inactive-value="no"
                      active-text="是"
                      inactive-text="否"
                      inline-prompt
                      disabled
                    />
                  </el-form-item>
                  <el-form-item label="重置后改密">
                    <el-switch
                      :model-value="detailForm.passwordForceChangeOnReset"
                      active-value="yes"
                      inactive-value="no"
                      active-text="是"
                      inactive-text="否"
                      inline-prompt
                      disabled
                    />
                  </el-form-item>
                </div>
              </div>

              <!-- Token 安全 -->
              <div class="sub-security-block mt-24">
                <div class="sub-sec-title">
                  <h4>Token 安全</h4>
                  <p>控制 Access Token、Refresh Token 有效期和刷新规则。</p>
                </div>
                <div class="grid-4-col mt-12">
                  <el-form-item label="Access Token">
                    <el-input :model-value="detailForm.accessTokenTimeout" disabled>
                      <template #append>分钟</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="Refresh Token">
                    <el-input :model-value="detailForm.refreshTokenTimeout" disabled>
                      <template #append>天</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="Refresh 轮换">
                    <el-switch
                      :model-value="detailForm.tokenRotationEnabled"
                      active-value="open"
                      inactive-value="close"
                      active-text="开启"
                      inactive-text="关闭"
                      inline-prompt
                      disabled
                    />
                  </el-form-item>
                  <el-form-item label="Token 黑名单">
                    <el-switch
                      :model-value="detailForm.tokenBlacklistEnabled"
                      active-value="open"
                      inactive-value="close"
                      active-text="开启"
                      inactive-text="关闭"
                      inline-prompt
                      disabled
                    />
                  </el-form-item>
                </div>
              </div>

              <!-- 账号锁定 -->
              <div class="sub-security-block mt-24">
                <div class="sub-sec-title">
                  <h4>账号锁定</h4>
                  <p>控制登录失败后的账号保护规则。</p>
                </div>
                <div class="grid-4-col mt-12">
                  <el-form-item label="失败次数">
                    <el-select :model-value="detailForm.loginFailMaxCount" disabled>
                      <el-option label="3 次" value="3" />
                      <el-option label="5 次" value="5" />
                      <el-option label="10 次" value="10" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="统计周期">
                    <el-input :model-value="detailForm.loginFailWindowMinutes" disabled>
                      <template #append>分钟</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="锁定时长">
                    <el-input :model-value="detailForm.loginFailLockMinutes" disabled>
                      <template #append>分钟</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="自动解锁">
                    <el-switch
                      :model-value="detailForm.loginFailAutoUnlock"
                      active-value="open"
                      inactive-value="close"
                      active-text="开启"
                      inactive-text="关闭"
                      inline-prompt
                      disabled
                    />
                  </el-form-item>
                </div>
              </div>

              <!-- 会话安全 -->
              <div class="sub-security-block mt-24">
                <div class="sub-sec-title">
                  <h4>会话安全</h4>
                  <p>控制在线会话、空闲超时和多端登录。</p>
                </div>
                <div class="grid-4-col mt-12">
                  <el-form-item label="会话有效期">
                    <el-input :model-value="detailForm.sessionTimeout" disabled>
                      <template #append>小时</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="空闲超时">
                    <el-input :model-value="detailForm.sessionIdleTimeout" disabled>
                      <template #append>分钟</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="多端登录">
                    <el-switch
                      :model-value="detailForm.sessionMultiDevice"
                      active-value="allow"
                      inactive-value="limit"
                      active-text="允许"
                      inactive-text="限制"
                      inline-prompt
                      disabled
                    />
                  </el-form-item>
                  <el-form-item label="最大设备数">
                    <el-input :model-value="detailForm.sessionMaxDevices" disabled>
                      <template #append>台</template>
                    </el-input>
                  </el-form-item>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Card: 影响范围 -->
          <div class="form-section-card-custom mt-20">
            <div class="card-title-header-custom">
              <h3>影响范围</h3>
              <p>当前绑定使用该身份域的客户端对象。</p>
            </div>
            <div class="card-body-custom">
              <el-table :data="mockImpactObjects" size="small" class="premium-table">
                <el-table-column prop="name" label="引用对象" min-width="110" />
                <el-table-column prop="type" label="类型" min-width="110" />
                <el-table-column label="状态" width="70" align="center">
                  <template #default="{ row }">
                    <span class="badge-dot" :class="row.status === 1 ? 'green' : 'red'"></span>
                    <span style="font-size: 11px; color: #475569;">{{ row.status === 1 ? '启用' : '禁用' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </el-form>
    </div>
  </div>
  </div>
</template>

<style scoped>
.create-realm-page-wrapper {
  padding: 0;
}

/* Form navigation header */
.form-navigation-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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
.btn-new-realm {
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
  height: 36px;
  padding: 0 16px;
  border-radius: 6px;
}
.btn-new-realm:hover {
  background-color: #1D4ED8;
  border-color: #1D4ED8;
}

/* Sub tabs under header */
.form-tabs-sub {
  display: flex;
  gap: 24px;
  border-bottom: 1px solid #E2E8F0;
  margin-bottom: 24px;
  padding-bottom: 12px;
}
.tab-sub-item {
  font-size: 14px;
  font-weight: 600;
  color: #64748B;
  cursor: pointer;
  position: relative;
  padding: 4px 0;
}
.tab-sub-item.active {
  color: #2563EB;
}
.tab-sub-item.active::after {
  content: "";
  position: absolute;
  bottom: -13px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #2563EB;
}

/* Grid Layout */
.realm-form-two-column-layout {
  display: block;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
}
.form-grid-layout {
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 24px;
  align-items: start;
}
@media (max-width: 1024px) {
  .form-grid-layout {
    grid-template-columns: 1fr;
  }
}

/* Custom Cards */
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
}
.card-title-header-custom h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.card-title-header-custom p {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #64748B;
}
.card-body-custom {
  padding-top: 4px;
}

/* Grids */
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
.grid-4-col {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 20px;
}
.full-width-field {
  width: 100%;
}
.mt-12 { margin-top: 12px; }
.mt-16 { margin-top: 16px; }
.mt-20 { margin-top: 20px; }
.mt-24 { margin-top: 24px; }
.ml-8 { margin-left: 8px; }

/* ElFormItem label style override */
.form-left-column :deep(.el-form-item) {
  margin-bottom: 0;
}
.form-left-column :deep(.el-form-item__label) {
  font-size: 12.5px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
  padding: 0;
  line-height: normal;
}
.form-left-column :deep(.el-input__wrapper),
.form-left-column :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  box-shadow: none !important;
}
.form-left-column :deep(.el-input-group) {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  background-color: #fff;
  overflow: hidden;
  display: inline-flex;
  width: 100%;
}
.form-left-column :deep(.el-input-group .el-input__wrapper) {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  height: 34px;
}
.form-left-column :deep(.el-input-group__append) {
  border: none !important;
  border-radius: 0 !important;
  background-color: #F8FAFC !important;
  color: #64748B !important;
  padding: 0 12px !important;
  font-size: 13px;
  font-weight: 500;
  border-left: 1px solid #E2E8F0 !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}
.form-left-column :deep(.el-textarea__inner) {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  box-shadow: none !important;
}

/* Alert banner styling */
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

/* Card select option styling */
.checkbox-cards-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.checkbox-card-item {
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 14px;
  background-color: #fff;
  cursor: default;
  transition: all 0.2s ease;
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

.policy-details-sub-card {
  border: 1px dashed #BFDBFE;
  background-color: #F8FAFC;
  border-radius: 8px;
  padding: 20px;
  margin-top: 16px;
}

/* Security Blocks */
.sub-security-block {
  border-bottom: 1px solid #F1F5F9;
  padding: 20px 0;
}
.sub-security-block:first-child {
  padding-top: 8px;
}
.sub-security-block:last-child {
  border-bottom: none;
  padding-bottom: 8px;
}
.sub-sec-title h4 {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
}
.sub-sec-title p {
  margin: 2px 0 0 0;
  font-size: 11px;
  color: #64748B;
}

/* Custom disabled overrides for premium read-only form elements */
.read-only-form :deep(.el-input.is-disabled .el-input__wrapper),
.read-only-form :deep(.el-select.is-disabled .el-select__wrapper),
.read-only-form :deep(.el-textarea.is-disabled .el-textarea__inner),
.read-only-form :deep(.el-input-number.is-disabled .el-input__wrapper) {
  background-color: #F8FAFC !important;
  border-color: #E2E8F0 !important;
  box-shadow: none !important;
  color: #0F172A !important;
  cursor: default !important;
}

.read-only-form :deep(.el-input.is-disabled .el-input__inner),
.read-only-form :deep(.el-select.is-disabled .el-select__placeholder),
.read-only-form :deep(.el-textarea.is-disabled .el-textarea__inner) {
  color: #0F172A !important;
  -webkit-text-fill-color: #0F172A !important;
  cursor: default !important;
  font-weight: 500;
}

.read-only-form :deep(.el-input-number.is-disabled .el-input-number__decrease),
.read-only-form :deep(.el-input-number.is-disabled .el-input-number__increase) {
  display: none !important;
}

/* Checkboxes read-only style */
.read-only-form :deep(.el-checkbox.is-disabled) {
  cursor: default !important;
}
.read-only-form :deep(.el-checkbox.is-disabled .el-checkbox__label) {
  color: #334155 !important;
  cursor: default !important;
  font-weight: 600;
}
.read-only-form :deep(.el-checkbox.is-disabled.is-checked .el-checkbox__inner) {
  background-color: #2563EB !important;
  border-color: #2563EB !important;
}
.read-only-form :deep(.el-checkbox.is-disabled.is-checked .el-checkbox__inner::after) {
  border-color: #fff !important;
}

/* Right Previews columns */
.preview-right-column {
  position: relative;
}
.sticky-preview-wrapper {
  display: none !important;
}
.preview-title-bar {
  margin-bottom: 16px;
}
.preview-title-bar h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.preview-title-bar .subtitle {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #64748B;
}

/* Simulated Browser window */
.mock-browser-window {
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
  background-color: #fff;
}
.browser-header-dots {
  background-color: #F8FAFC;
  height: 38px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  padding: 0 16px;
  position: relative;
}
.browser-header-dots .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  display: inline-block;
}
.browser-header-dots .dot.red { background-color: #EF4444; }
.browser-header-dots .dot.yellow { background-color: #F59E0B; }
.browser-header-dots .dot.green { background-color: #10B981; }
.browser-address-bar {
  flex: 1;
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  height: 24px;
  margin-left: 20px;
  font-size: 11px;
  color: #64748B;
  display: flex;
  align-items: center;
  padding: 0 10px;
}
.browser-address-bar .lock-icon {
  margin-right: 4px;
}

/* Browser content login page mockup */
.browser-content-area {
  padding: 32px 24px;
  background-color: #EFF6FF;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 320px;
}
.login-card-preview-inner {
  background-color: #fff;
  border-radius: 12px;
  border: 1px solid #F1F5F9;
  padding: 24px;
  width: 100%;
  max-width: 290px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.login-inner-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  text-align: center;
}
.login-inner-subtitle {
  margin: 4px 0 16px 0;
  font-size: 11px;
  color: #64748B;
  text-align: center;
}

/* Mock tabs */
.login-methods-tabs-preview {
  display: flex;
  border-bottom: 1px solid #F1F5F9;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}
.tab-preview-item {
  font-size: 12px;
  color: #64748B;
  padding-bottom: 6px;
  cursor: pointer;
  font-weight: 600;
  position: relative;
}
.tab-preview-item.active {
  color: #2563EB;
}
.tab-preview-item.active::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #2563EB;
}

/* Mock Inputs */
.mock-input-field {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  height: 36px;
  display: flex;
  align-items: center;
  padding: 0 12px;
}
.placeholder-txt {
  font-size: 12px;
  color: #94A3B8;
}

.premium-table {
  width: 100%;
}
.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}
.badge-dot.green {
  background-color: #10B981;
}
.badge-dot.red {
  background-color: #EF4444;
}

@media (max-width: 1200px) {
  .realm-form-two-column-layout {
    grid-template-columns: 1fr;
  }
}
.premium-radio-group :deep(.el-radio-button__inner) {
  border-radius: 8px !important;
  border: 1px solid #E2E8F0 !important;
  margin-right: 8px;
  background-color: #fff;
  color: #64748B;
  transition: all 0.2s ease;
  font-weight: 500;
  box-shadow: none !important;
}
.premium-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #fff !important;
  color: #3B82F6 !important;
  border-color: #3B82F6 !important;
  font-weight: 600;
}
.premium-radio-group :deep(.el-radio-button:first-child .el-radio-button__inner),
.premium-radio-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 8px !important;
}
</style>
