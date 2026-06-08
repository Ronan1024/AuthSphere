<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  ArrowLeft, CopyDocument, Document, Clock, Connection,
  Right, User, UserFilled, OfficeBuilding, Lock, EditPen, Monitor,
  Key, Link, InfoFilled, Promotion, Stamp, Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { RealmRecord } from '@/api/realm'

const props = defineProps<{
  realm: RealmRecord
  typeCategoryText?: string
}>()

const emit = defineEmits(['back', 'edit'])

const activeTab = ref('basic')

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

const getAuthMethodsText = () => {
  const code = props.realm.code
  if (code === 'platform_realm') return '密码 / 短信'
  if (code === 'tenant_realm') return '密码 / MFA'
  if (code === 'merchant_realm') return '密码 / 邮箱'
  if (code === 'consumer_realm') return '短信 / 邮箱'
  return '密码 / 短信'
}

const getLoginPageName = () => {
  const code = props.realm.code
  if (code === 'tenant_realm') return '租户统一登录页'
  if (code === 'platform_realm') return '平台后台登录页'
  if (code === 'merchant_realm') return '商户后台登录页'
  if (code === 'consumer_realm') return '移动端登录页'
  return '默认登录页'
}

const getLoginPageCode = () => {
  const code = props.realm.code
  if (code === 'tenant_realm') return 'tenant_login_page'
  if (code === 'platform_realm') return 'platform_login_page'
  if (code === 'merchant_realm') return 'merchant_login_page'
  if (code === 'consumer_realm') return 'consumer_login_page'
  return 'default_login_page'
}

const copyCode = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('编码已成功复制')
  }).catch(() => {
    ElMessage.error('复制失败，请手动选择复制')
  })
}

// Mock referenced objects for the login configurations tab
const mockImpactObjects = [
  { name: '租户身份域', code: 'tenant_realm', type: '身份域默认登录页', status: 1 },
  { name: '租户管理端客户端', code: 'tenant_admin_web', type: '继承身份域配置', status: 1 }
]

// Mock audit logs
const mockAuditLogs = [
  { operator: 'admin', action: '修改配置', desc: '更新身份域【默认登录页】为 租户统一登录页', time: '2026-06-08 11:20:15' },
  { operator: 'admin', action: '策略绑定', desc: '绑定安全策略【高强度密码策略】', time: '2026-06-08 10:45:00' },
  { operator: 'system', action: '自动同步', desc: '完成身份域基础元数据刷新', time: '2026-06-08 00:00:02' },
  { operator: 'admin', action: '创建身份域', desc: '初始化创建身份域配置', time: '2026-05-28 14:30:10' }
]

// Mock association summary stats
const stats = computed(() => {
  const code = props.realm.code
  if (code === 'tenant_realm') {
    return { accounts: '128', subjects: '12', clients: '8', policy: '完整' }
  }
  if (code === 'platform_realm') {
    return { accounts: '42', subjects: '5', clients: '3', policy: '完整' }
  }
  if (code === 'merchant_realm') {
    return { accounts: '86', subjects: '8', clients: '4', policy: '待配置' }
  }
  return { accounts: '0', subjects: '0', clients: '0', policy: '缺策略' }
})

const handleTabClick = (tabName: string) => {
  activeTab.value = tabName
}

</script>

<template>
  <div class="realm-detail-layout">
    <!-- Top Action Header -->
    <div class="detail-header-actions">
      <div class="header-left">
        <el-button :icon="ArrowLeft" class="btn-back" @click="emit('back')">返回</el-button>
        <span class="crumb-text">身份域管理 / 身份域 / {{ realm.name }}</span>
      </div>
      <div class="header-right">
        <el-button class="btn-action-normal" @click="emit('edit', realm)">编辑</el-button>
        <el-button class="btn-action-danger" v-if="realm.status === 1">禁用</el-button>
        <el-button class="btn-action-success" v-else>启用</el-button>
      </div>
    </div>

    <!-- Detail Hero Block -->
    <div class="detail-hero-card">
      <div class="hero-top-row">
        <h2>
          {{ realm.name }} 
          <span class="badge" :class="realm.status === 1 ? 'green' : 'red'">
            {{ realm.status === 1 ? '启用' : '禁用' }}
          </span>
        </h2>
        <div class="hero-actions">
          <button class="btn-hero-small" @click="copyCode(realm.code)">复制编码</button>
          <button class="btn-hero-small" @click="handleTabClick('audit')">查看日志</button>
        </div>
      </div>
      <div class="hero-meta-grid">
        <div class="meta-item">
          <span class="meta-label">身份域编码</span>
          <span class="meta-value">{{ realm.code }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">身份域类型</span>
          <span class="meta-value">{{ displayType }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">默认登录页</span>
          <span class="meta-value">{{ getLoginPageName() }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">认证方式</span>
          <span class="meta-value">{{ getAuthMethodsText() }}</span>
        </div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="detail-navigation-tabs">
      <span class="detail-tab-item" :class="activeTab === 'basic' ? 'active' : ''" @click="handleTabClick('basic')">基础信息</span>
      <span class="detail-tab-item" :class="activeTab === 'login' ? 'active' : ''" @click="handleTabClick('login')">登录配置</span>
      <span class="detail-tab-item" :class="activeTab === 'auth' ? 'active' : ''" @click="handleTabClick('auth')">认证策略</span>
      <span class="detail-tab-item" :class="activeTab === 'security' ? 'active' : ''" @click="handleTabClick('security')">安全策略</span>
      <span class="detail-tab-item" :class="activeTab === 'relation' ? 'active' : ''" @click="handleTabClick('relation')">关联对象</span>
      <span class="detail-tab-item" :class="activeTab === 'audit' ? 'active' : ''" @click="handleTabClick('audit')">审计日志</span>
    </div>

    <!-- Tabs Content Container -->
    <div class="tabs-content-area">
      <!-- Tab 1: 基础信息 -->
      <div v-if="activeTab === 'basic'" class="tab-pane-grid">
        <!-- Left: Basic Info Table -->
        <div class="pane-card">
          <div class="pane-card-head">
            <h3>基础信息</h3>
            <p>身份域自身信息，不在此处管理账号和角色。</p>
          </div>
          <div class="info-list-rows">
            <div class="info-row">
              <span class="info-label">身份域名称</span>
              <strong class="info-val">{{ realm.name }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">身份域编码</span>
              <strong class="info-val">{{ realm.code }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">身份域类型</span>
              <strong class="info-val">{{ displayType }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">强制 HTTPS</span>
              <strong class="info-val">开启</strong>
            </div>
            <div class="info-row">
              <span class="info-label">排序</span>
              <strong class="info-val">10</strong>
            </div>
            <div class="info-row">
              <span class="info-label">描述</span>
              <strong class="info-val">{{ realm.description || '暂无描述内容' }}</strong>
            </div>
          </div>
        </div>

        <!-- Right: Binding Configurations -->
        <div class="pane-card">
          <div class="pane-card-head">
            <h3>绑定配置</h3>
            <p>身份域只引用认证相关配置，客户端资源与角色不在此处配置。</p>
          </div>
          <div class="binding-list-rows">
            <div class="binding-item">
              <div class="binding-info">
                <span class="binding-title">默认登录页</span>
                <span class="binding-desc">{{ getLoginPageName() }} ({{ getLoginPageCode() }})</span>
              </div>
              <span class="binding-op-link" @click="handleTabClick('login')">查看</span>
            </div>
            <div class="binding-item">
              <div class="binding-info">
                <span class="binding-title">默认认证策略</span>
                <span class="binding-desc">租户默认认证策略 tenant_auth_policy</span>
              </div>
              <span class="binding-op-link" @click="handleTabClick('auth')">查看</span>
            </div>
            <div class="binding-item">
              <div class="binding-info">
                <span class="binding-title">密码策略</span>
                <span class="binding-desc">高强度密码策略</span>
              </div>
              <span class="binding-op-link" @click="handleTabClick('security')">查看</span>
            </div>
            <div class="binding-item">
              <div class="binding-info">
                <span class="binding-title">Token 策略</span>
                <span class="binding-desc">Access Token 120 分钟</span>
              </div>
              <span class="binding-op-link" @click="handleTabClick('security')">查看</span>
            </div>
          </div>
        </div>

        <!-- Flowchart mapping (Added at full width below details) -->
        <div class="pane-card span-full" style="margin-top: 16px;">
          <div class="pane-card-head">
            <h3>主体与域关联关系链路</h3>
          </div>
          <div class="flowchart-premium">
            <div class="flow-node">
              <div class="node-icon bg-blue"><el-icon><Right /></el-icon></div>
              <div class="node-text">
                <h4>登录入口</h4>
                <p>用户发起登录</p>
              </div>
            </div>
            <div class="flow-arrow"><el-icon><Right /></el-icon></div>
            <div class="flow-node highlight">
              <div class="node-icon bg-indigo"><el-icon><Lock /></el-icon></div>
              <div class="node-text">
                <h4>身份域 (Realm)</h4>
                <p>隔离安全策略</p>
              </div>
            </div>
            <div class="flow-arrow"><el-icon><Right /></el-icon></div>
            <div class="flow-node">
              <div class="node-icon bg-green"><el-icon><User /></el-icon></div>
              <div class="node-text">
                <h4>账号 (Account)</h4>
                <p>登录实体对象</p>
              </div>
            </div>
            <div class="flow-arrow"><el-icon><Right /></el-icon></div>
            <div class="flow-node">
              <div class="node-icon bg-purple"><el-icon><Connection /></el-icon></div>
              <div class="node-text">
                <h4>关系成员</h4>
                <p>关联账号与主体</p>
              </div>
            </div>
            <div class="flow-arrow"><el-icon><Right /></el-icon></div>
            <div class="flow-node">
              <div class="node-icon bg-orange"><el-icon><Stamp /></el-icon></div>
              <div class="node-text">
                <h4>主体 (Subject)</h4>
                <p>授权策略载体</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 2: 登录配置 -->
      <div v-if="activeTab === 'login'" class="tab-pane-grid">
        <!-- Left: Interactive Live Mock Preview -->
        <div class="pane-card">
          <div class="pane-card-head">
            <h3>登录页预览</h3>
            <p>展示当前的登录界面布局。预览不产生真实登录行为，不生成 Token。</p>
          </div>
          <div class="login-preview-container">
            <div class="preview-login">
              <div class="login-left">
                <h3>{{ displayType === '平台域' ? '平台管理后台' : '租户管理后台' }}</h3>
                <p>统一身份空间登录入口，支持账号密码、短信验证码与 MFA 二次安全校验。</p>
              </div>
              <div class="login-right">
                <h3>登录 IAM</h3>
                <div class="login-field-mock">用户名 / 手机号 / 邮箱</div>
                <div class="login-field-mock">密码</div>
                <div class="login-field-mock">验证码（失败 3 次后显示）</div>
                <div class="login-btn-mock">登录</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Configurations Summary -->
        <div class="pane-card">
          <div class="pane-card-head">
            <h3>当前配置</h3>
            <p>身份域默认配置，可被客户端登录配置覆盖。</p>
          </div>
          <div class="info-list-rows">
            <div class="info-row">
              <span class="info-label">默认登录页</span>
              <strong class="info-val">{{ getLoginPageName() }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">登录页编码</span>
              <strong class="info-val">{{ getLoginPageCode() }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">认证方式</span>
              <strong class="info-val">{{ getAuthMethodsText() }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">默认认证策略</span>
              <strong class="info-val">租户默认认证策略</strong>
            </div>
            <div class="info-row">
              <span class="info-label">登录失败规则</span>
              <strong class="info-val">5 次失败锁定 30 分钟</strong>
            </div>
            <div class="info-row">
              <span class="info-label">MFA 触发</span>
              <strong class="info-val">管理员每次登录</strong>
            </div>
          </div>
        </div>

        <!-- Bottom: Referenced clients / Impact Analysis Table -->
        <div class="pane-card span-full" style="margin-top: 16px;">
          <div class="pane-card-head">
            <h3>影响范围</h3>
            <p>修改登录页或认证策略时，将直接影响以下已绑定引用对象。</p>
          </div>
          <el-table :data="mockImpactObjects" class="premium-table">
            <el-table-column prop="name" label="引用对象" min-width="160" />
            <el-table-column prop="code" label="对象编码" min-width="160" />
            <el-table-column prop="type" label="引用类型" min-width="160" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <span class="badge" :class="row.status === 1 ? 'green' : 'red'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default>
                <span class="op-link">查看</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Tab 3: 认证策略 -->
      <div v-if="activeTab === 'auth'" class="tab-pane-grid">
        <div class="pane-card span-full">
          <div class="pane-card-head">
            <h3>绑定的认证策略</h3>
            <p>认证流程链、锁定规则和MFA控制标准。</p>
          </div>
          <div class="info-list-rows" style="max-width: 600px;">
            <div class="info-row">
              <span class="info-label">策略名称</span>
              <strong class="info-val">租户默认认证策略 (tenant_auth_policy)</strong>
            </div>
            <div class="info-row">
              <span class="info-label">认证流水线</span>
              <strong class="info-val">密码验证 -> MFA 二次校验（对于特权管理员账号）</strong>
            </div>
            <div class="info-row">
              <span class="info-label">密码容错限制</span>
              <strong class="info-val">连续输错密码 5 次，锁定 30 分钟。</strong>
            </div>
            <div class="info-row">
              <span class="info-label">锁定类型</span>
              <strong class="info-val">基于 IP 和 Username 联合锁定</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 4: 安全策略 -->
      <div v-if="activeTab === 'security'" class="tab-pane-grid">
        <div class="pane-card">
          <div class="pane-card-head">
            <h3>密码与安全策略</h3>
            <p>密码强度与会话生命周期配置。</p>
          </div>
          <div class="info-list-rows">
            <div class="info-row">
              <span class="info-label">密码策略名称</span>
              <strong class="info-val">高强度密码策略</strong>
            </div>
            <div class="info-row">
              <span class="info-label">密码复杂度</span>
              <strong class="info-val">大写字母+小写字母+数字+特殊字符，最小8位</strong>
            </div>
            <div class="info-row">
              <span class="info-label">强制过期</span>
              <strong class="info-val">90 天内强制更换密码</strong>
            </div>
            <div class="info-row">
              <span class="info-label">历史密码防重</span>
              <strong class="info-val">不可与最近 3 次密码重复</strong>
            </div>
          </div>
        </div>

        <div class="pane-card">
          <div class="pane-card-head">
            <h3>会话与 Token 策略</h3>
            <p>令牌有效周期设置。</p>
          </div>
          <div class="info-list-rows">
            <div class="info-row">
              <span class="info-label">会话有效期</span>
              <strong class="info-val">8 小时 (Session Timeout)</strong>
            </div>
            <div class="info-row">
              <span class="info-label">Access Token 有效期</span>
              <strong class="info-val">120 分钟</strong>
            </div>
            <div class="info-row">
              <span class="info-label">Refresh Token 有效期</span>
              <strong class="info-val">30 天</strong>
            </div>
            <div class="info-row">
              <span class="info-label">单浏览器多登录</span>
              <strong class="info-val">允许共存</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 5: 关联对象 -->
      <div v-if="activeTab === 'relation'" class="tab-pane-grid">
        <div class="pane-card span-full">
          <div class="pane-card-head">
            <h3>归属统计</h3>
            <p>身份空间中已经分配的对象总量。</p>
          </div>
          <div class="stats-summary-grid">
            <div class="stat-box">
              <el-icon class="stat-box-icon text-blue"><UserFilled /></el-icon>
              <div class="stat-box-text">
                <span class="stat-num">{{ stats.accounts }}</span>
                <span class="stat-lbl">关联账号数</span>
              </div>
            </div>
            <div class="stat-box">
              <el-icon class="stat-box-icon text-orange"><Stamp /></el-icon>
              <div class="stat-box-text">
                <span class="stat-num">{{ stats.subjects }}</span>
                <span class="stat-lbl">关联主体数</span>
              </div>
            </div>
            <div class="stat-box">
              <el-icon class="stat-box-icon text-green"><Monitor /></el-icon>
              <div class="stat-box-text">
                <span class="stat-num">{{ stats.clients }}</span>
                <span class="stat-lbl">启用客户端</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 6: 审计日志 -->
      <div v-if="activeTab === 'audit'" class="tab-pane-grid">
        <div class="pane-card span-full">
          <div class="pane-card-head">
            <h3>操作审计记录</h3>
            <p>仅记录对当前身份域属性及策略绑定的修改日志。</p>
          </div>
          <el-table :data="mockAuditLogs" class="premium-table">
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="action" label="操作类型" width="150">
              <template #default="{ row }">
                <span class="badge gray">{{ row.action }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="变更描述" min-width="280" />
            <el-table-column prop="time" label="操作时间" width="180" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.realm-detail-layout {
  font-family: "Inter", "Segoe UI", Arial, sans-serif;
  color: #334155;
}

/* Header bar */
.detail-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.btn-back {
  border: 1px solid #E5E7EB;
  color: #334155;
  font-weight: 600;
}
.crumb-text {
  font-size: 14px;
  color: #64748B;
  font-weight: 500;
}
.header-right {
  display: flex;
  gap: 10px;
}
.btn-action-normal {
  border: 1px solid #E5E7EB;
  color: #334155;
  font-weight: 600;
}
.btn-action-danger {
  background-color: #fff;
  border-color: #FCA5A5;
  color: #DC2626;
  font-weight: 600;
}
.btn-action-success {
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
}

/* Hero card */
.detail-hero-card {
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.02);
}
.hero-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.hero-top-row h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  display: flex;
  align-items: center;
  gap: 12px;
}
.hero-actions {
  display: flex;
  gap: 8px;
}
.btn-hero-small {
  height: 30px;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 600;
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 4px;
  color: #334155;
  cursor: pointer;
  transition: all 0.15s ease;
}
.btn-hero-small:hover {
  border-color: #2563EB;
  color: #2563EB;
}
.hero-meta-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.meta-item {
  display: flex;
  flex-direction: column;
}
.meta-label {
  font-size: 12px;
  color: #64748B;
  margin-bottom: 6px;
}
.meta-value {
  font-size: 14px;
  font-weight: 700;
  color: #334155;
}

/* Navigation tabs */
.detail-navigation-tabs {
  display: flex;
  height: 48px;
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 0 14px;
  align-items: center;
  margin-bottom: 16px;
  gap: 6px;
}
.detail-tab-item {
  height: 32px;
  border-radius: 6px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  color: #64748B;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.detail-tab-item.active {
  background-color: #EFF6FF;
  color: #2563EB;
}
.detail-tab-item:hover:not(.active) {
  color: #2563EB;
  background-color: #F8FAFC;
}

/* Tabs content grid */
.tab-pane-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.span-full {
  grid-column: span 2;
}

/* Pane card */
.pane-card {
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 20px;
}
.pane-card-head {
  margin-bottom: 18px;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 12px;
}
.pane-card-head h3 {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #0F172A;
  font-weight: 700;
}
.pane-card-head p {
  margin: 0;
  font-size: 13px;
  color: #64748B;
}

/* Info row values */
.info-list-rows {
  display: flex;
  flex-direction: column;
}
.info-row {
  display: flex;
  height: 48px;
  align-items: center;
  border-bottom: 1px solid #E5E7EB;
}
.info-row:last-child {
  border-bottom: 0;
}
.info-label {
  width: 150px;
  color: #64748B;
  font-size: 13px;
  font-weight: 500;
}
.info-val {
  font-size: 13px;
  color: #334155;
  font-weight: 600;
}

/* Binding items list */
.binding-list-rows {
  display: flex;
  flex-direction: column;
}
.binding-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 54px;
  border-bottom: 1px solid #E5E7EB;
}
.binding-item:last-child {
  border-bottom: 0;
}
.binding-info {
  display: flex;
  flex-direction: column;
}
.binding-title {
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
}
.binding-desc {
  font-size: 12px;
  color: #64748B;
  margin-top: 4px;
}
.binding-op-link {
  color: #2563EB;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
}
.binding-op-link:hover {
  color: #1D4ED8;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
}
.badge.green {
  background-color: #DCFCE7;
  color: #16A34A;
}
.badge.red {
  background-color: #FEE2E2;
  color: #DC2626;
}
.badge.gray {
  background-color: #F1F5F9;
  color: #64748B;
}

/* Login Preview Pane */
.login-preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
}
.preview-login {
  width: 100%;
  max-width: 580px;
  height: 280px;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
}
.login-left {
  background-color: #0F1B2D;
  color: #fff;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.login-left h3 {
  font-size: 18px;
  margin: 0 0 10px 0;
  font-weight: 700;
}
.login-left p {
  color: #CBD5E1;
  font-size: 11px;
  line-height: 1.6;
  margin: 0;
}
.login-right {
  padding: 24px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.login-right h3 {
  font-size: 16px;
  color: #0F172A;
  margin: 0 0 14px 0;
  font-weight: 700;
}
.login-field-mock {
  height: 32px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  margin-bottom: 8px;
  color: #94A3B8;
  display: flex;
  align-items: center;
  padding: 0 10px;
  font-size: 11px;
}
.login-btn-mock {
  height: 32px;
  border-radius: 6px;
  background-color: #2563EB;
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  margin-top: 6px;
  cursor: default;
}

/* Referenced elements link color */
.op-link {
  color: #2563EB;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
}
.op-link:hover {
  color: #1D4ED8;
}

/* Flowchart */
.flowchart-premium {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F8FAFC;
  padding: 20px 24px;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
  overflow-x: auto;
}
.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  min-width: 110px;
}
.flow-node.highlight .node-icon {
  background-color: #EFF6FF;
  color: #2563EB;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.15);
}
.node-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 8px;
}
.bg-blue { background-color: #EFF6FF; color: #2563EB; }
.bg-indigo { background-color: #EEF2F6; color: #4F46E5; }
.bg-green { background-color: #DCFCE7; color: #16A34A; }
.bg-purple { background-color: #F5F3FF; color: #8B5CF6; }
.bg-orange { background-color: #FFEDD5; color: #EA580C; }

.node-text h4 {
  margin: 0 0 2px 0;
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
}
.node-text p {
  margin: 0;
  font-size: 11px;
  color: #64748B;
}
.flow-arrow {
  color: #CBD5E1;
  font-size: 18px;
}

/* Stats summary boxes */
.stats-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.stat-box {
  background-color: #F8FAFC;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-box-icon {
  font-size: 28px;
}
.text-blue { color: #2563EB; }
.text-orange { color: #EA580C; }
.text-green { color: #16A34A; }

.stat-box-text {
  display: flex;
  flex-direction: column;
}
.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
}
.stat-lbl {
  font-size: 12px;
  color: #64748B;
}

@media (max-width: 1024px) {
  .tab-pane-grid {
    grid-template-columns: 1fr;
  }
  .span-full {
    grid-column: span 1;
  }
}
</style>
