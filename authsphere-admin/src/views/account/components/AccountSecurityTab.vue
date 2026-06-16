<script setup lang="ts">
import { CircleCheck, Unlock, Warning, Document } from '@element-plus/icons-vue'
import { ref } from 'vue'
import type { AccountRecord } from '@/api/account'

defineProps<{
  account: AccountRecord
}>()

const ACCOUNT_STATUS_LOCKED = 2
const ACCOUNT_STATUS_DISABLED = 3

const credentialList = ref([
  { icon: 'Key', type: '密码凭证', status: '启用', statusType: 'success', detail: '密码已设置<br>强制首次登录改密: <span class="text-blue">否</span><br>下次修改时间: 2025-06-20 09:00:00', updateTime: '2024-05-20 09:00:00', action: '重置密码' },
  { icon: 'Cellphone', type: '短信登录', status: '未开启', statusType: 'info', detail: '绑定手机登录，获取验证码', updateTime: '-', action: '开启' },
  { icon: 'Message', type: '邮箱登录', status: '未开启', statusType: 'info', detail: '绑定邮箱登录，接收验证码', updateTime: '-', action: '开启' },
  { icon: 'Lock', type: 'TOTP 认证', status: '未绑定', statusType: 'info', detail: '用于双因素认证 (2FA)', updateTime: '-', action: '绑定' }
])
</script>

<template>
  <div class="security-tab-container">
    <div class="section-title">账号安全状态</div>
    <div class="status-cards-grid">
      <div class="status-card">
        <div class="icon-wrap bg-green"><el-icon><CircleCheck /></el-icon></div>
        <div class="card-content">
          <span class="label">账号状态</span>
          <strong class="value" :class="account.status === ACCOUNT_STATUS_DISABLED ? 'text-red' : 'text-green'">
            {{ account.status === ACCOUNT_STATUS_DISABLED ? '禁用' : account.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '启用' }}
          </strong>
          <span class="desc">账号登录能力由账号状态和锁定状态共同决定</span>
        </div>
      </div>
      <div class="status-card">
        <div class="icon-wrap bg-blue"><el-icon><Unlock /></el-icon></div>
        <div class="card-content">
          <span class="label">锁定状态</span>
          <strong class="value">{{ account.status === ACCOUNT_STATUS_LOCKED ? '锁定' : '正常' }}</strong>
          <span class="desc">{{ account.status === ACCOUNT_STATUS_LOCKED ? '账号当前无法登录' : '账号未被锁定' }}</span>
        </div>
      </div>
      <div class="status-card">
        <div class="icon-wrap bg-orange"><el-icon><Warning /></el-icon></div>
        <div class="card-content">
          <span class="label">强制改密</span>
          <strong class="value">否</strong>
          <span class="desc">下次登录不需要修改密码</span>
        </div>
      </div>
      <div class="status-card">
        <div class="icon-wrap bg-purple"><el-icon><Document /></el-icon></div>
        <div class="card-content">
          <span class="label">密码过期时间</span>
          <strong class="value">{{ account.lastLoginTime || '-' }}</strong>
          <span class="desc text-green">最近一次成功登录时间</span>
        </div>
      </div>
    </div>

    <div class="main-content-layout">
      <div class="left-col">
        <div class="section-title">登录凭证</div>
        <el-table :data="credentialList" style="width: 100%" class="credential-table">
          <el-table-column label="凭证类型" width="140">
            <template #default="{ row }">
              <span class="flex-center text-secondary"><el-icon class="mr-2"><component :is="row.icon" /></el-icon> {{ row.type }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.statusType" effect="plain" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="详情" min-width="220">
            <template #default="{ row }">
              <div class="text-sm detail-cell" v-html="row.detail"></div>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="160" class-name="text-secondary text-sm" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="primary">{{ row.action }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="right-col">
        <div class="section-title">安全设置</div>
        <div class="info-list">
          <div class="info-item"><span>连续登录失败锁定</span><strong>5 次</strong></div>
          <div class="info-item"><span>锁定时长</span><strong>30 分钟</strong></div>
          <div class="info-item"><span>密码强度策略</span><strong>中强度 (最小8位，包含数字大小写字母、特殊符号)</strong></div>
          <div class="info-item"><span>密码最短使用时长</span><strong>8 小时</strong></div>
          <div class="info-item"><span>密码有效期</span><strong>90 天</strong></div>
          <div class="info-item"><span>最近一次密码修改</span><strong>2024-05-20 09:00:00</strong></div>
        </div>

        <div class="section-title mt-8">最近登录信息</div>
        <div class="info-list">
          <div class="info-item"><span>最后登录时间</span><strong>2024-05-28 10:30:22</strong></div>
          <div class="info-item"><span>最后登录IP</span><strong>120.***.**.23 (中国 上海)</strong></div>
          <div class="info-item"><span>最近登录方式</span><strong>微信小程序</strong></div>
          <div class="info-item"><span>最近登录设备</span><strong>iPhone 15 / iOS 17.4 / 微信 8.0.44</strong></div>
        </div>
        <div class="text-right mt-2">
          <el-button link type="primary" class="text-sm">查看更多登录日志 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.security-tab-container {
  padding-top: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
}

.status-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.status-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: #fdfdfe;
  border: 1px solid #eef2f7;
  border-radius: 8px;
}

.icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.bg-green { background: #dcfce7; color: #16a34a; }
.bg-blue { background: #eff6ff; color: #2563eb; }
.bg-orange { background: #fff7ed; color: #ea580c; }
.bg-purple { background: #faf5ff; color: #9333ea; }

.card-content {
  display: flex;
  flex-direction: column;
}

.card-content .label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.card-content .value {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.card-content .desc {
  font-size: 12px;
  color: #9ca3af;
}

.main-content-layout {
  display: grid;
  grid-template-columns: minmax(0, 6fr) minmax(0, 4fr);
  gap: 32px;
}

.left-col, .right-col {
  display: flex;
  flex-direction: column;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 13px;
  gap: 16px;
}

.info-item span {
  color: #6b7280;
  flex-shrink: 0;
}

.info-item strong {
  color: #111827;
  font-weight: 500;
  text-align: right;
  word-break: break-word;
}

.detail-cell {
  line-height: 1.6;
}

/* Utilities */
.flex-center { display: flex; align-items: center; }
.mr-2 { margin-right: 8px; }
.mt-2 { margin-top: 8px; }
.mt-8 { margin-top: 32px; }
.text-green { color: #16a34a; }
.text-blue { color: #2563eb; }
.text-secondary { color: #6b7280; }
.text-sm { font-size: 13px; }
.text-right { text-align: right; }
</style>
