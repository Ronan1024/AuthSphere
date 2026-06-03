<script setup lang="ts">
import {
  Bell,
  Connection,
  Fold,
  Help,
  Lock,
  OfficeBuilding,
  Operation,
  Setting,
  UserFilled,
} from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const asideCollapsed = ref(false)
const openedMenus = ref<string[]>([])

const breadcrumbs = computed(() => {
  if (route.path === '/realms' && route.query.realmId) {
    return ['身份域管理', '身份域详情']
  }
  if (route.path === '/subjects' && route.query.subjectId) {
    return ['主体管理', '主体列表', '主体详情']
  }
  if (route.path === '/accounts' && route.query.accountId) {
    return ['账号管理', '账号列表', '账号详情']
  }
  const value = route.meta.breadcrumb
  if (Array.isArray(value) && value.every((item) => typeof item === 'string')) {
    return value
  }
  return route.meta.title ? [String(route.meta.title)] : []
})

const handleSelect = (path: string) => {
  router.push(path)
}

const toggleAside = () => {
  asideCollapsed.value = !asideCollapsed.value
}

interface MenuChild {
  index: string
  label: string
}

interface MenuGroup {
  index: string
  label: string
  icon: typeof UserFilled
  children: MenuChild[]
}

const menuItems = [
  {
    index: 'identity-tenant',
    label: '身份域管理',
    icon: UserFilled,
    children: [
      { index: '/realms', label: '身份域列表' },
      { index: '/realm/type-categories', label: '身份域类型' },
      { index: '/organizations', label: '组织管理' },
    ],
  },
  {
    index: 'user-management',
    label: '用户管理',
    icon: UserFilled,
    children: [
      { index: '/accounts', label: '用户列表' },
    ],
  },
  {
    index: 'subject-management',
    label: '主体管理',
    icon: UserFilled,
    children: [
      { index: '/subjects/types', label: '主体类型' },
      { index: '/subjects/type-relations', label: '关系规则' },
      { index: '/subjects', label: '主体列表' },
    ],
  },
  {
    index: 'application-management',
    label: '应用管理',
    icon: Connection,
    children: [
      { index: '/applications', label: '应用列表' },
      { index: '/applications/instances', label: '应用实例' },
      { index: '/applications/clients', label: '应用客户端' },
    ],
  },
  {
    index: 'permission-center',
    label: '权限中心',
    icon: Lock,
    children: [
      { index: '/permission/roles', label: '角色管理' },
      { index: '/permission/resources', label: '权限资源' },
      { index: '/permission/policies', label: '授权策略' },
    ],
  },
  {
    index: 'security-center',
    label: '安全中心',
    icon: Setting,
    children: [
      { index: '/security/password-policy', label: '密码策略' },
      { index: '/security/mfa-policy', label: 'MFA策略' },
      { index: '/security/session-policy', label: '会话策略' },
      { index: '/security/risk-control', label: '风险控制' },
    ],
  },
  {
    index: 'audit-ops',
    label: '审计与运维',
    icon: Operation,
    children: [
      { index: '/ops/login-logs', label: '登录日志' },
      { index: '/ops/operation-logs', label: '操作日志' },
      { index: '/ops/token-management', label: 'Token管理' },
      { index: '/ops/system-config', label: '系统配置' },
    ],
  },
] satisfies MenuGroup[]

const currentMenuGroup = computed(() => {
  return menuItems.find((item) =>
    item.children.some((child) => child.index === route.path),
  )?.index
})

watch(
  currentMenuGroup,
  (group) => {
    openedMenus.value = group && !asideCollapsed.value ? [group] : []
  },
  { immediate: true },
)
</script>

<template>
  <el-container class="admin-shell">
    <el-aside :width="asideCollapsed ? '72px' : '232px'" class="admin-aside" :class="{ collapsed: asideCollapsed }">
      <div class="brand">
        <div class="brand-mark">
          <OfficeBuilding />
        </div>
        <div v-if="!asideCollapsed">
          <div class="brand-name">认证授权中心</div>
        </div>
      </div>

      <el-menu
        class="admin-menu"
        :default-active="activeMenu"
        :collapse="asideCollapsed"
        :collapse-transition="false"
        :default-openeds="openedMenus"
        router
        @select="handleSelect"
      >
        <template v-for="item in menuItems" :key="item.index">
          <el-sub-menu :index="item.index">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.index"
              :index="child.index"
            >
              <span>{{ child.label }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button
            class="header-fold-button"
            :icon="Fold"
            text
            @click="toggleAside"
          />
          <el-breadcrumb separator="›" class="app-breadcrumb">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-actions">
          <el-badge :value="12" class="notice-badge">
            <el-button :icon="Bell" circle />
          </el-badge>
          <el-button :icon="Help" circle />
          <el-avatar :icon="UserFilled" />
          <span class="account-name">admin</span>
        </div>
      </el-header>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
