<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { accountApi, type AccountExternalIdentityRecord, type AccountRecord } from '@/api/account'

const props = defineProps<{
  account: AccountRecord
}>()

const bindList = ref<AccountExternalIdentityRecord[]>([])
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(20)

const loadBindings = async () => {
  loading.value = true
  try {
    bindList.value = await accountApi.externalIdentities(props.account.id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '第三方绑定加载失败')
  } finally {
    loading.value = false
  }
}

const maskValue = (value?: string) => {
  if (!value) return '-'
  if (value.length <= 8) return `${value.slice(0, 2)}***`
  return `${value.slice(0, 4)}***${value.slice(-4)}`
}

onMounted(loadBindings)
</script>

<template>
  <div class="third-party-tab-container">
    <el-alert
      title="账号绑定第三方身份后，可使用对应平台快速登录。解绑后，将无法使用该平台直接登录，请谨慎操作。"
      type="info"
      show-icon
      :closable="false"
      class="mb-6"
    />

    <div class="section-title">第三方身份绑定</div>
    <el-table v-loading="loading" :data="bindList" style="width: 100%" class="bind-table mb-4">
      <el-table-column label="平台类型" width="160">
        <template #default="{ row }">
          <div class="flex-center">
            <span class="bind-icon mr-2">
              {{ row.providerType?.substring(0, 1) || 'P' }}
            </span>
            <span>{{ row.providerType || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="providerName" label="身份源名称" min-width="150" />
      <el-table-column label="第三方用户ID" min-width="180">
        <template #default="{ row }">{{ maskValue(row.externalUserId) }}</template>
      </el-table-column>
      <el-table-column label="UnionID" min-width="160">
        <template #default="{ row }">{{ maskValue(row.externalUnionId) }}</template>
      </el-table-column>
      <el-table-column prop="externalNickname" label="昵称" width="100" />
      <el-table-column prop="bindAt" label="绑定时间" width="180" class-name="text-secondary" />
      <el-table-column prop="lastLoginTime" label="最近登录时间" width="180" class-name="text-secondary" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary">详情</el-button>
          <el-button link type="primary" disabled>解绑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container mb-8">
      <span class="total-text">共 {{ bindList.length }} 条</span>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="prev, pager, next, sizes"
        :total="bindList.length"
      />
    </div>

    <div class="instruction-box">
      <div class="section-title">绑定说明</div>
      <ul class="instruction-list">
        <li>第三方用户ID（如 openid、unionid、user_id）由第三方平台生成，用于识别同一平台下的唯一用户。</li>
        <li>一个第三方身份只能绑定一个本系统账号。</li>
        <li>如需使用其他平台登录，请先完成绑定。</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.third-party-tab-container {
  padding-top: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
}

.bind-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 12px;
  color: white;
  font-weight: bold;
}
.bind-icon.wechat { background: #07c160; }
.bind-icon.alipay { background: #1677ff; }
.bind-icon.tiktok { background: #000000; }

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #eef2f7;
}

.total-text {
  font-size: 13px;
  color: #6b7280;
}

.instruction-box {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px;
}

.instruction-box .section-title {
  margin-bottom: 12px;
}

.instruction-list {
  margin: 0;
  padding-left: 18px;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.8;
}

.instruction-list li {
  margin-bottom: 4px;
}

/* Utilities */
.flex-center { display: flex; align-items: center; }
.mr-2 { margin-right: 8px; }
.mb-4 { margin-bottom: 16px; }
.mb-6 { margin-bottom: 24px; }
.mb-8 { margin-bottom: 32px; }
.text-secondary { color: #6b7280; }
</style>
