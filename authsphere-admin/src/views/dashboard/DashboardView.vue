<script setup lang="ts">
import { Connection, Key, Lock, User } from '@element-plus/icons-vue'

const metrics = [
  { label: '身份域', value: 12, icon: Connection },
  { label: '账号', value: 3280, icon: User },
  { label: '角色', value: 86, icon: Lock },
  { label: '策略', value: 154, icon: Key },
]
</script>

<template>
  <section class="dashboard">
    <el-row :gutter="16">
      <el-col v-for="item in metrics" :key="item.label" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <div class="metric-value">{{ item.value.toLocaleString() }}</div>
            <div class="metric-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-title">近期审计</div>
      </template>
      <el-table :data="[]" empty-text="暂无审计记录">
        <el-table-column prop="operator" label="操作人" min-width="160" />
        <el-table-column prop="action" label="动作" min-width="180" />
        <el-table-column prop="resource" label="资源" min-width="220" />
        <el-table-column prop="createdAt" label="时间" min-width="180" />
      </el-table>
    </el-card>
  </section>
</template>
