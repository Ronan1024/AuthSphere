<script setup lang="ts">
import { Plus, Refresh } from '@element-plus/icons-vue'

const realms = [
  {
    code: 'default',
    name: '默认身份域',
    status: '启用',
    accountCount: 1280,
    updatedAt: '2026-05-28 09:30',
  },
]
</script>

<template>
  <section>
    <div class="toolbar">
      <el-input placeholder="搜索身份域编码或名称" clearable class="toolbar-search" />
      <div class="toolbar-actions">
        <el-button :icon="Refresh">刷新</el-button>
        <el-button type="primary" :icon="Plus">新建身份域</el-button>
      </div>
    </div>

    <el-card shadow="never" class="section-card">
      <el-table :data="realms" row-key="code">
        <el-table-column prop="code" label="编码" min-width="160" />
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountCount" label="账号数" width="140" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default>
            <el-button link type="primary">配置</el-button>
            <el-button link type="primary">账号</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>
