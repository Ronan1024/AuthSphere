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
        path: 'realm/auth-methods',
        name: 'RealmAuthMethod',
        component: () => import('@/views/realm/AuthMethodView.vue'),
        meta: {
          title: '认证方式',
          breadcrumb: ['身份域管理', '认证方式'],
          moduleDescription: '管理和配置登录可用的具体认证方式（账号密码、短信验证码、邮箱验证码、TOTP 动态口令、外部 OIDC 等）。',
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
        path: 'accounts/create',
        name: 'AccountCreate',
        component: () => import('@/views/account/AccountCreateView.vue'),
        meta: {
          title: '新增账号',
          breadcrumb: ['用户管理', '用户列表', '新增账号'],
          moduleDescription: '只创建登录账号，不直接分配应用角色。',
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
        path: 'subjects/create',
        name: 'SubjectCreate',
        component: () => import('@/views/subject/SubjectCreateView.vue'),
        meta: {
          title: '新增主体',
          breadcrumb: ['主体管理', '主体列表', '新增主体'],
          moduleDescription: '创建业务主体，用于承载成员账号、应用开通和数据边界。',
        },
      },
      {
        path: 'subjects/detail/:id',
        name: 'SubjectDetail',
        component: () => import('@/views/subject/SubjectDetailView.vue'),
        meta: {
          title: '主体详情',
          breadcrumb: ['主体管理', '主体列表', '主体详情'],
          moduleDescription: '查看主体的基础信息、配置入口、成员账号与应用实例授权权限。',
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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
