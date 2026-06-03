<script setup lang="ts">
import { Monitor } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

import { accountApi, type AccountLoginLogRecord, type AccountRecord } from '@/api/account'

const props = defineProps<{
  account: AccountRecord
}>()

const query = reactive({
  method: '',
  result: '',
  timeRange: [] as string[]
})

const logList = ref<AccountLoginLogRecord[]>([])
const total = ref(0)
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(20)

const loadLogs = async () => {
  loading.value = true
  try {
    const result = await accountApi.loginLogs(props.account.id, {
      page: currentPage.value,
      size: pageSize.value,
      loginType: query.method || undefined,
      success: query.result === '' ? undefined : query.result === 'success',
    })
    logList.value = result.records ?? []
    total.value = Number(result.total ?? 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录日志加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadLogs()
}

const resetQuery = () => {
  query.method = ''
  query.result = ''
  query.timeRange = []
  handleSearch()
}

watch([currentPage, pageSize], loadLogs)
onMounted(loadLogs)
</script>

<template>
  <div class="login-log-tab-container">
    <div class="filter-bar mb-4">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="登录方式">
          <el-select v-model="query.method" placeholder="全部" clearable style="width: 140px">
            <el-option label="账号密码" value="PASSWORD" />
            <el-option label="短信" value="SMS" />
            <el-option label="邮箱" value="EMAIL" />
            <el-option label="微信小程序" value="WECHAT_MINI" />
            <el-option label="支付宝小程序" value="ALIPAY_MINI" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="query.result" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录时间">
          <el-date-picker
            v-model="query.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item class="action-item">
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="logList" style="width: 100%" class="log-table mb-4">
      <el-table-column prop="createTime" label="登录时间" width="180" />
      <el-table-column label="登录入口" width="160">
        <template #default="{ row }">
          <div class="flex-center">
            <el-icon class="mr-2 text-secondary"><Monitor /></el-icon>
            {{ row.loginEntryCode || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="登录方式" width="140">
        <template #default="{ row }">
          <div class="flex-center">
            <el-icon class="mr-2 text-secondary"><Monitor /></el-icon>
            {{ row.loginType || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.success ? 'success' : 'danger'" effect="plain" size="small" round>
            {{ row.success ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="loginIdentifier" label="登录标识" min-width="160" show-overflow-tooltip />
      <el-table-column prop="failReason" label="失败原因" min-width="140" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="150" />
      <el-table-column prop="deviceId" label="设备ID" width="140" />
      <el-table-column prop="userAgent" label="User-Agent" min-width="240" show-overflow-tooltip />
    </el-table>

    <div class="pagination-container">
      <span class="total-text">共 {{ total }} 条</span>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="prev, pager, next, sizes"
        :total="total"
      />
    </div>
  </div>
</template>

<style scoped>
.login-log-tab-container {
  padding-top: 8px;
}

.filter-bar {
  display: flex;
  background: #f8fafc;
  padding: 16px 20px;
  border-radius: 8px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 24px;
  width: 100%;
}

.filter-form :deep(.el-form-item) {
  margin: 0;
}

.action-item {
  margin-left: auto !important;
}

.bind-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 10px;
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

/* Utilities */
.flex-center { display: flex; align-items: center; }
.mr-2 { margin-right: 8px; }
.mb-4 { margin-bottom: 16px; }
.text-secondary { color: #6b7280; }
</style>
