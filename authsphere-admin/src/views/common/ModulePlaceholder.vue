<script setup lang="ts">
import { Plus, Refresh, Setting } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => String(route.meta.title ?? '功能页面'))
const description = computed(() => String(route.meta.moduleDescription ?? '该功能页面待完善'))
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
      <div class="heading-actions">
        <el-button :icon="Refresh">刷新</el-button>
        <el-button type="primary" :icon="Plus">新增</el-button>
      </div>
    </div>

    <el-card shadow="never" class="section-card">
      <div class="placeholder-content">
        <div class="placeholder-icon">
          <el-icon><Setting /></el-icon>
        </div>
        <div>
          <h2>{{ title }}</h2>
          <p>{{ description }}</p>
          <p class="placeholder-note">当前为前端菜单与页面结构占位，后续按业务模型补充列表、表单和详情。</p>
        </div>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.module-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-heading {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.page-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.placeholder-content {
  display: flex;
  gap: 18px;
  align-items: center;
  min-height: 180px;
}

.placeholder-icon {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  color: #2563eb;
  background: #eff6ff;
  font-size: 28px;
}

.placeholder-content h2 {
  margin: 0 0 8px;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.placeholder-content p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.placeholder-note {
  margin-top: 10px !important;
  font-size: 13px !important;
}

@media (max-width: 768px) {
  .page-heading,
  .placeholder-content {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
