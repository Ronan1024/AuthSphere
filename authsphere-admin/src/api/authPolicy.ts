import http from './http'
import type { PageResult } from './passwordPolicy'

export interface AuthPolicyPageQuery {
  page: number
  size: number
  code?: string
  name?: string
  authMethod?: string
  status?: number
}

export interface AuthPolicyPayload {
  code: string
  name: string
  applicableRealmId?: string | null
  authMethods: string[]
  defaultAuthMethod: string
  captchaEnabled: boolean
  captchaFailureThreshold?: number
  captchaTtlSeconds?: number
  captchaErrorLimit?: number
  maxFailureCount: number
  failureWindowMinutes: number
  lockMinutes: number
  notifyUser: boolean
  riskLogEnabled: boolean
  mfaEnabled: boolean
  mfaTriggers?: string[]
  mfaMethods?: string[]
  rememberDeviceEnabled: boolean
  rememberDeviceDays?: number
  ipRestrictionEnabled: boolean
  deviceCheckEnabled: boolean
  remoteLoginCheckEnabled: boolean
  abnormalTimeCheckEnabled: boolean
  status: number
  description?: string
}

export interface AuthPolicyReference {
  id: string
  code: string
  name: string
  referenceType: string // e.g. REALM, CLIENT
  status: number
}

export interface AuthPolicyRecord {
  id: string
  code: string
  name: string
  authMethods: string[]
  captchaEnabled: boolean
  mfaEnabled: boolean
  maxFailureCount: number
  lockMinutes: number
  status: number
  referenceCount: number
  createTime?: string
  updateTime?: string
}

export interface AuthPolicyDetailResponse extends AuthPolicyRecord {
  applicableRealmId?: string | null
  applicableRealmName?: string
  defaultAuthMethod: string
  captchaFailureThreshold?: number
  captchaTtlSeconds?: number
  captchaErrorLimit?: number
  failureWindowMinutes: number
  notifyUser: boolean
  riskLogEnabled: boolean
  mfaTriggers?: string[]
  mfaMethods?: string[]
  rememberDeviceEnabled: boolean
  rememberDeviceDays?: number
  ipRestrictionEnabled: boolean
  deviceCheckEnabled: boolean
  remoteLoginCheckEnabled: boolean
  abnormalTimeCheckEnabled: boolean
  systemBuiltin?: boolean
  description?: string
  realmReferenceCount: number
  clientReferenceCount: number
  references: AuthPolicyReference[]
}

export interface AuthPolicyOptionResponse {
  id: string
  code: string
  name: string
}

export interface AuthPolicyCopyRequest {
  code: string
  name: string
}

export const authPolicyApi = {
  page(params: AuthPolicyPageQuery) {
    return http.post<unknown, PageResult<AuthPolicyRecord>>('/admin/auth/policy/page', params)
  },
  list() {
    return http.get<unknown, AuthPolicyOptionResponse[]>('/admin/auth/policy/list')
  },
  detail(id: string) {
    return http.get<unknown, AuthPolicyDetailResponse>(`/admin/auth/policy/${id}`)
  },
  create(payload: AuthPolicyPayload) {
    return http.post<unknown, string>('/admin/auth/policy', payload)
  },
  update(id: string, payload: AuthPolicyPayload) {
    return http.put<unknown, boolean>(`/admin/auth/policy/${id}`, payload)
  },
  copy(id: string, payload: AuthPolicyCopyRequest) {
    return http.post<unknown, string>(`/admin/auth/policy/${id}/copy`, payload)
  },
  enable(id: string) {
    return http.put<unknown, boolean>(`/admin/auth/policy/${id}/enable`)
  },
  disable(id: string) {
    return http.put<unknown, boolean>(`/admin/auth/policy/${id}/disable`)
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/admin/auth/policy/${id}`)
  }
}
