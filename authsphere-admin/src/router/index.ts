import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/realms',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台', breadcrumb: ['概览'] },
      },
      {
        path: 'realms',
        name: 'RealmList',
        component: () => import('@/views/realm/RealmListView.vue'),
        meta: { title: '身份域管理', breadcrumb: ['身份域管理', '身份域列表'] },
      },
      {
        path: 'realms/create',
        name: 'RealmCreate',
        component: () => import('@/views/realm/RealmListView.vue'),
        meta: {
          title: '添加身份域',
          breadcrumb: ['身份域管理', '身份域列表', '添加身份域'],
        },
      },
      {
        path: 'realms/:id/edit',
        name: 'RealmEdit',
        component: () => import('@/views/realm/RealmListView.vue'),
        meta: {
          title: '编辑身份域',
          breadcrumb: ['身份域管理', '身份域列表', '编辑身份域'],
        },
      },
      {
        path: 'realm/type-categories',
        name: 'RealmTypeCategory',
        component: () => import('@/views/realm/RealmTypeCategoryView.vue'),
        meta: {
          title: '身份域类型',
          breadcrumb: ['身份域管理', '身份域类型'],
          moduleDescription: '维护身份域类型分类，用于区分内部员工、外部伙伴、客户域等身份空间。',
        },
      },
      {
        path: 'realm/login-pages',
        name: 'RealmLoginPage',
        component: () => import('@/views/realm/LoginPageView.vue'),
        meta: {
          title: '登录页管理',
          breadcrumb: ['身份域管理', '登录页管理'],
          moduleDescription: '维护身份域或客户端未登录时展示的登录入口页面。',
        },
      },
      {
        path: 'realm/auth-policies',
        name: 'RealmAuthPolicy',
        component: () => import('@/views/realm/AuthPolicyView.vue'),
        meta: {
          title: '认证策略',
          breadcrumb: ['身份域管理', '认证策略'],
          moduleDescription: '认证策略用于定义用户登录时如何验证用户，包括登录方式、验证码、失败锁定、MFA 与风险校验。',
        },
      },
      {
        path: 'accounts',
        name: 'UserManagement',
        component: () => import('@/views/account/AccountListView.vue'),
        meta: {
          title: '用户列表',
          breadcrumb: ['用户管理', '用户列表'],
          moduleDescription: '管理账号基础信息、账号状态、身份域归属和登录安全状态。',
        },
      },
      {
        path: 'subjects/types',
        name: 'SubjectTypeManagement',
        component: () => import('@/views/subject/SubjectTypeView.vue'),
        meta: {
          title: '主体类型',
          breadcrumb: ['主体管理', '主体类型'],
          moduleDescription: '维护主体类型，例如员工、客户、伙伴、服务账号等，用于区分账号进入系统后的身份载体。',
        },
      },
      {
        path: 'subjects/type-relations',
        name: 'SubjectTypeRelationManagement',
        component: () => import('@/views/subject/SubjectTypeRelationView.vue'),
        meta: {
          title: '关系规则',
          breadcrumb: ['主体管理', '关系规则'],
          moduleDescription: '维护主体类型之间允许建立的父子关系和创建、管理能力边界。',
        },
      },
      {
        path: 'subjects',
        name: 'SubjectList',
        component: () => import('@/views/subject/SubjectListView.vue'),
        meta: {
          title: '主体列表',
          breadcrumb: ['主体管理', '主体列表'],
          moduleDescription: '管理账号进入系统后的身份主体，以及主体与账号、组织、身份域之间的绑定关系。',
        },
      },
      {
        path: 'organizations',
        name: 'OrganizationManagement',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '组织管理',
          breadcrumb: ['身份域管理', '组织管理'],
          moduleDescription: '维护组织、部门和主体关系，为账号归属与授权范围提供基础结构。',
        },
      },
      {
        path: 'applications',
        name: 'ApplicationList',
        component: () => import('@/views/application/ApplicationListView.vue'),
        meta: {
          title: '应用列表',
          breadcrumb: ['身份域', '应用管理', '应用列表'],
          moduleDescription: '管理身份域下的所有应用，支持应用的创建、配置与生命周期管理。',
        },
      },
      {
        path: 'applications/create',
        name: 'ApplicationCreate',
        component: () => import('@/views/application/ApplicationCreateView.vue'),
        meta: {
          title: '新增应用',
          breadcrumb: ['应用管理', '应用列表', '新增应用'],
          moduleDescription: '创建一个新的应用，定义应用的基本信息和访问配置。',
        },
      },
      {
        path: 'applications/detail/:id',
        name: 'ApplicationDetail',
        component: () => import('@/views/application/ApplicationDetailView.vue'),
        meta: {
          title: '应用详情',
          breadcrumb: ['应用管理', '应用列表', '应用详情'],
          moduleDescription: '查看和管理应用的基本信息、实例、菜单与权限配置。',
        },
      },
      {
        path: 'applications/detail/:id/clients/create',
        name: 'ClientCreate',
        component: () => import('@/views/application/ClientCreateView.vue'),
        meta: {
          title: '添加客户端',
          breadcrumb: ['应用管理', '应用列表', '应用详情', '应用客户端', '添加客户端'],
          moduleDescription: '为应用添加新的客户端配置。',
        },
      },
      {
        path: 'applications/detail/:id/clients/config/:clientId',
        name: 'ClientConfig',
        component: () => import('@/views/application/ClientConfigView.vue'),
        meta: {
          title: '客户端配置',
          breadcrumb: ['应用管理', '应用列表', '应用详情', '应用客户端', '客户端配置'],
          moduleDescription: '对应用下的具体访问客户端进行高级鉴权策略与资源权限配置。',
        },
      },
      {
        path: 'applications/instances',
        name: 'ApplicationInstances',
        component: () => import('@/views/application/ApplicationInstanceListView.vue'),
        meta: {
          title: '应用实例',
          breadcrumb: ['身份域', '应用管理', '应用实例'],
          moduleDescription: '管理应用的实例配置。',
        },
      },
      {
        path: 'instances/create',
        name: 'ApplicationInstanceCreate',
        component: () => import('@/views/application/ApplicationInstanceCreateView.vue'),
        meta: {
          title: '新增应用实例',
          breadcrumb: ['应用管理', '应用实例', '新增应用实例'],
          moduleDescription: '为指定主体开通一个应用实例，并初始化默认可用能力。',
        },
      },
      {
        path: 'instances/detail/:id',
        name: 'ApplicationInstanceDetail',
        component: () => import('@/views/application/ApplicationInstanceDetailView.vue'),
        meta: {
          title: '应用实例详情',
          breadcrumb: ['应用管理', '应用实例', '应用实例详情'],
          moduleDescription: '应用实例的详细信息页，提供实例的基本信息、关系预览及能力概览等内容。',
        },
      },
      {
        path: 'applications/clients',
        name: 'ApplicationClients',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '应用客户端',
          breadcrumb: ['身份域', '应用管理', '应用客户端'],
          moduleDescription: '管理 OAuth2/OIDC Client、Client ID、密钥、授权方式和回调地址。',
        },
      },
      {
        path: 'permission/roles',
        name: 'RoleManagement',
        component: () => import('@/views/permission/RoleListView.vue'),
        meta: {
          title: '角色管理',
          breadcrumb: ['权限中心', '角色管理'],
          moduleDescription: '维护角色、角色编码、授权范围和角色模板。',
        },
      },
      {
        path: 'permission/resources',
        name: 'PermissionResources',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '权限资源',
          breadcrumb: ['权限中心', '权限资源'],
          moduleDescription: '维护菜单、接口、按钮、数据对象等可授权资源。',
        },
      },
      {
        path: 'permission/policies',
        name: 'AuthorizationPolicies',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '授权策略',
          breadcrumb: ['权限中心', '授权策略'],
          moduleDescription: '配置 RBAC、条件策略、数据权限和授权记录。',
        },
      },
      {
        path: 'security/password-policy',
        name: 'PasswordPolicy',
        component: () => import('@/views/security/PasswordPolicyView.vue'),
        meta: {
          title: '密码策略',
          breadcrumb: ['安全中心', '密码策略'],
          moduleDescription: '配置密码复杂度、有效期、历史密码和重试限制。',
        },
      },
      {
        path: 'security/mfa-policy',
        name: 'MfaPolicy',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: 'MFA策略',
          breadcrumb: ['安全中心', 'MFA策略'],
          moduleDescription: '配置多因素认证触发条件、认证方式和豁免规则。',
        },
      },
      {
        path: 'security/session-policy',
        name: 'SessionPolicy',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '会话策略',
          breadcrumb: ['安全中心', '会话策略'],
          moduleDescription: '配置会话超时、单点登录会话、Token 刷新和强制下线规则。',
        },
      },
      {
        path: 'security/risk-control',
        name: 'RiskControl',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '风险控制',
          breadcrumb: ['安全中心', '风险控制'],
          moduleDescription: '配置异常登录识别、风险事件、冻结规则和安全告警。',
        },
      },
      {
        path: 'ops/login-logs',
        name: 'LoginLogs',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '登录日志',
          breadcrumb: ['审计与运维', '登录日志'],
          moduleDescription: '查询登录时间、登录结果、身份域、客户端、IP 与风险原因。',
        },
      },
      {
        path: 'ops/operation-logs',
        name: 'OperationLogs',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '操作日志',
          breadcrumb: ['审计与运维', '操作日志'],
          moduleDescription: '追踪管理员操作、对象变更、结果和审计详情。',
        },
      },
      {
        path: 'ops/token-management',
        name: 'TokenManagement',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: 'Token管理',
          breadcrumb: ['审计与运维', 'Token管理'],
          moduleDescription: '查看 Token 签发、刷新、撤销、过期和风险处理状态。',
        },
      },
      {
        path: 'ops/system-config',
        name: 'SystemConfig',
        component: () => import('@/views/common/ModulePlaceholder.vue'),
        meta: {
          title: '系统配置',
          breadcrumb: ['审计与运维', '系统配置'],
          moduleDescription: '维护系统字典、类型分类、通知模板和基础运行参数。',
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
