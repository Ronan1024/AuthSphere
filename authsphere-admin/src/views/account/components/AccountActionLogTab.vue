<script setup lang="ts">
import { reactive } from 'vue'
import type { AccountRecord } from '@/api/account'

defineProps<{
  account: AccountRecord
}>()

const query = reactive({
  actionType: '',
  operator: '',
  timeRange: [] as string[]
})

const handleSearch = () => {
  // 当前数据模型暂未提供账号操作日志表，保留筛选条件用于后续接入真实接口。
}

const resetQuery = () => {
  query.actionType = ''
  query.operator = ''
  query.timeRange = []
}
</script>

<template>
  <div class="action-log-tab-container">
    <div class="filter-bar mb-4">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="操作类型">
          <el-select v-model="query.actionType" placeholder="全部" clearable style="width: 140px">
            <el-option label="登录" value="login" />
            <el-option label="重置密码" value="reset_pwd" />
            <el-option label="禁用第三方绑定" value="disable_bind" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="query.operator" placeholder="请输入操作人" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="操作时间">
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

    <el-table :data="[]" style="width: 100%" class="log-table mb-4">
      <el-table-column prop="time" label="操作时间" width="180" />
      <el-table-column prop="type" label="操作类型" width="140" />
      <el-table-column prop="operator" label="操作人" width="180" />
      <el-table-column prop="content" label="操作内容" min-width="260" />
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.result === 'success' ? 'success' : 'danger'" effect="plain" size="small" round>
            {{ row.result === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="120" />
      <template #empty>
        <el-empty description="暂无账号操作日志" />
      </template>
    </el-table>
  </div>
</template>

<style scoped>
.action-log-tab-container {
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

/* Utilities */
.mb-4 { margin-bottom: 16px; }
</style>
