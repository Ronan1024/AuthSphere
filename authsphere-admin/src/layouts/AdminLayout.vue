<script setup lang="ts">
import { Monitor, OfficeBuilding, Setting, UserFilled } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const handleSelect = (path: string) => {
  router.push(path)
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside width="240px" class="admin-aside">
      <div class="brand">
        <div class="brand-mark">AS</div>
        <div>
          <div class="brand-name">AuthSphere</div>
          <div class="brand-subtitle">统一认证授权中心</div>
        </div>
      </div>

      <el-menu
        class="admin-menu"
        :default-active="activeMenu"
        router
        @select="handleSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/realms">
          <el-icon><OfficeBuilding /></el-icon>
          <span>身份域管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div>
          <div class="page-title">{{ route.meta.title }}</div>
          <div class="page-subtitle">IAM 管理后台</div>
        </div>
        <div class="header-actions">
          <el-button :icon="Setting" circle />
          <el-avatar :icon="UserFilled" />
        </div>
      </el-header>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
