import { ref } from 'vue'
import http from './http'
import type { PageResult } from './subject'

export interface AccountQuery {
  page: number
  size: number
  realmId?: string | number
  realmCode?: string
  keyword?: string
  username?: string
  mobile?: string
  email?: string
  status?: number
}

export interface AccountRecord {
  id: string
  realmId: string
  realmCode?: string
  realmName?: string
  username: string
  nickname?: string
  remark?: string
  avatar?: string
  mobile?: string
  email?: string
  status: number
  subjectMemberCount?: number
  externalIdentityCount?: number
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}

export interface AccountPayload {
  realmId?: string | number
  username: string
  nickname?: string
  remark?: string
  avatar?: string
  mobile: string
  email?: string
  useTemporaryPassword?: boolean
  password?: string
  status?: number
}

export interface AccountCreateResponse {
  temporaryPassword?: string
}

export interface AccountPasswordResetPayload {
  newPassword: string
  forceReset?: boolean
}

export interface AccountSubjectRecord {
  memberId: string
  subjectId: string
  subjectCode?: string
  subjectName?: string
  subjectTypeName?: string
  rootSubjectName?: string
  memberType: number
  memberTypeName?: string
  isDefault?: number
  memberStatus: number
  subjectStatus: number
  joinedAt?: string
}

export interface AccountExternalIdentityRecord {
  id: string
  providerType?: string
  providerCode?: string
  providerName?: string
  externalUserId?: string
  externalUnionId?: string
  externalAppId?: string
  externalNickname?: string
  externalAvatar?: string
  status: number
  bindAt?: string
  lastLoginTime?: string
}

export interface AccountLoginLogQuery {
  page: number
  size: number
  loginType?: string
  success?: boolean
  loginEntryCode?: string
}

export interface AccountLoginLogRecord {
  id: string
  realmName?: string
  loginEntryCode?: string
  providerType?: string
  loginType?: string
  loginIdentifier?: string
  success: boolean
  failReason?: string
  ip?: string
  userAgent?: string
  deviceId?: string
  createTime?: string
}

// In-memory stateful mock data
const mockAccounts = ref<AccountRecord[]>([
  {
    id: '10001',
    realmId: '1',
    realmCode: 'tenant_realm',
    realmName: '租户身份域',
    username: 'lisi',
    nickname: '李四',
    remark: '租户侧运营账号',
    mobile: '13800000001',
    email: 'lisi@example.com',
    status: 1, // 启用
    subjectMemberCount: 2,
    externalIdentityCount: 1,
    lastLoginTime: '2026-06-17 09:32',
    createTime: '2026-05-10 14:20'
  },
  {
    id: '10002',
    realmId: '1',
    realmCode: 'tenant_realm',
    realmName: '租户身份域',
    username: 'wangwu',
    nickname: '王五',
    remark: '商户联系人',
    mobile: '13800000002',
    email: 'wangwu@example.com',
    status: 1,
    subjectMemberCount: 1,
    externalIdentityCount: 0,
    lastLoginTime: '2026-06-18 10:12',
    createTime: '2026-05-12 11:30'
  },
  {
    id: '10003',
    realmId: '2',
    realmCode: 'platform_realm',
    realmName: '平台身份域',
    username: 'old_admin',
    nickname: '管理员',
    remark: '平台初始化管理员',
    mobile: '13900001111',
    email: 'admin@authsphere.com',
    status: 2, // 锁定
    subjectMemberCount: 1,
    externalIdentityCount: 2,
    lastLoginTime: '2026-06-18 08:45',
    createTime: '2026-01-01 00:00'
  }
])

export const accountApi = {
  page(params: AccountQuery) {
    return http.post<unknown, PageResult<AccountRecord>>('/admin/account/page', params)
  },
  
  detail(id: string) {
    return http.get<unknown, AccountRecord>(`/admin/account/${id}`)
  },
  
  create(payload: AccountPayload) {
    return http.post<unknown, AccountCreateResponse>('/admin/account', payload)
  },
  
  update(id: string, payload: AccountPayload) {
    return new Promise<boolean>((resolve, reject) => {
      const idx = mockAccounts.value.findIndex(item => item.id === String(id))
      setTimeout(() => {
        if (idx !== -1) {
          mockAccounts.value[idx] = {
            ...mockAccounts.value[idx],
            username: payload.username,
            nickname: payload.nickname,
            remark: payload.remark,
            mobile: payload.mobile,
            email: payload.email
          }
          resolve(true)
        } else {
          reject(new Error('更新失败，账号不存在'))
        }
      }, 200)
    })
  },
  
  toggleStatus(id: string) {
    return new Promise<boolean>((resolve, reject) => {
      const idx = mockAccounts.value.findIndex(item => item.id === String(id))
      setTimeout(() => {
        if (idx !== -1) {
          const current = mockAccounts.value[idx].status
          // If enabled/locked, disable it; if disabled, enable it
          mockAccounts.value[idx].status = current === 3 ? 1 : 3
          resolve(true)
        } else {
          reject(new Error('操作失败，账号不存在'))
        }
      }, 150)
    })
  },
  
  lock(id: string) {
    return new Promise<boolean>((resolve, reject) => {
      const idx = mockAccounts.value.findIndex(item => item.id === String(id))
      setTimeout(() => {
        if (idx !== -1) {
          mockAccounts.value[idx].status = 2
          resolve(true)
        } else {
          reject(new Error('操作失败，账号不存在'))
        }
      }, 150)
    })
  },
  
  unlock(id: string) {
    return new Promise<boolean>((resolve, reject) => {
      const idx = mockAccounts.value.findIndex(item => item.id === String(id))
      setTimeout(() => {
        if (idx !== -1) {
          mockAccounts.value[idx].status = 1
          resolve(true)
        } else {
          reject(new Error('操作失败，账号不存在'))
        }
      }, 150)
    })
  },
  
  resetPassword(id: string, payload: AccountPasswordResetPayload) {
    return new Promise<boolean>((resolve) => {
      console.log(`Reset password for ${id}:`, payload)
      setTimeout(() => {
        resolve(true)
      }, 200)
    })
  },
  
  subjects(id: string) {
    return http.get<unknown, AccountSubjectRecord[]>(`/admin/account/${id}/subjects`)
  },
  
  externalIdentities(id: string) {
    return new Promise<AccountExternalIdentityRecord[]>((resolve) => {
      setTimeout(() => {
        resolve([
          {
            id: 'ext101',
            providerType: 'WECHAT',
            providerCode: 'wechat_mp',
            providerName: '微信快捷登录',
            externalUserId: 'wx_oUp8w5eYt89sdFk2',
            externalUnionId: 'wx_union_889Fad011',
            externalNickname: '小明',
            status: 1,
            bindAt: '2026-05-10 14:22:10',
            lastLoginTime: '2026-06-17 09:32:00'
          }
        ])
      }, 150)
    })
  },
  
  loginLogs(id: string, params: AccountLoginLogQuery) {
    return new Promise<PageResult<AccountLoginLogRecord>>((resolve) => {
      const records: AccountLoginLogRecord[] = [
        {
          id: 'log1001',
          realmName: '租户身份域',
          loginEntryCode: 'mall_platform_pc',
          providerType: 'PASSWORD',
          loginType: '账号密码',
          loginIdentifier: 'lisi',
          success: true,
          ip: '121.23.45.101',
          userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
          deviceId: 'dev_mac_001',
          createTime: '2026-06-17 09:32:15'
        },
        {
          id: 'log1002',
          realmName: '租户身份域',
          loginEntryCode: 'mall_platform_pc',
          providerType: 'SMS',
          loginType: '短信验证码',
          loginIdentifier: '138****0001',
          success: true,
          ip: '121.23.45.101',
          userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X)',
          deviceId: 'dev_iphone_002',
          createTime: '2026-06-16 14:10:02'
        }
      ]
      setTimeout(() => {
        resolve({
          total: records.length,
          records,
          current: params.page,
          size: params.size,
          pages: 1
        })
      }, 200)
    })
  }
}
