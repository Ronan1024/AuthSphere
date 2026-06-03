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
  avatar?: string
  mobile: string
  email?: string
  password?: string
  status?: number
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

export const accountApi = {
  page(params: AccountQuery) {
    return http.post<unknown, PageResult<AccountRecord>>('/admin/account/page', params)
  },
  detail(id: string) {
    return http.get<unknown, AccountRecord>(`/admin/account/${id}`)
  },
  create(payload: AccountPayload) {
    return http.post<unknown, boolean>('/admin/account', payload)
  },
  update(id: string, payload: AccountPayload) {
    return http.put<unknown, boolean>(`/admin/account/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/account/status/${id}`)
  },
  lock(id: string) {
    return http.put<unknown, boolean>(`/admin/account/${id}/lock`)
  },
  unlock(id: string) {
    return http.put<unknown, boolean>(`/admin/account/${id}/unlock`)
  },
  resetPassword(id: string, payload: AccountPasswordResetPayload) {
    return http.put<unknown, boolean>(`/admin/account/${id}/password`, payload)
  },
  subjects(id: string) {
    return http.get<unknown, AccountSubjectRecord[]>(`/admin/account/${id}/subjects`)
  },
  externalIdentities(id: string) {
    return http.get<unknown, AccountExternalIdentityRecord[]>(`/admin/account/${id}/external-identities`)
  },
  loginLogs(id: string, params: AccountLoginLogQuery) {
    return http.post<unknown, PageResult<AccountLoginLogRecord>>(`/admin/account/${id}/login-logs`, params)
  },
}
