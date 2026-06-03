import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface PasswordPolicyQuery {
  page: number
  size: number
  code?: string
  name?: string
  status?: number
}

export interface PasswordPolicyPayload {
  code: string
  name: string
  minLength: number
  maxLength: number
  requireUppercase: boolean
  requireLowercase: boolean
  requireDigit: boolean
  requireSpecialChar: boolean
  disallowUsername: boolean
  historyCount: number
  expireDays: number
  retryLimit: number
  lockMinutes: number
  description?: string
}

export interface PasswordPolicyRecord extends PasswordPolicyPayload {
  id: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface PasswordPolicyListItem {
  id: string
  code: string
  name: string
  minLength: number
  maxLength: number
  expireDays: number
  retryLimit: number
  lockMinutes: number
  status: number
  createTime?: string
  updateTime?: string
}

export const passwordPolicyApi = {
  page(params: PasswordPolicyQuery) {
    return http.post<unknown, PageResult<PasswordPolicyRecord>>('/admin/password/policy/page', params)
  },
  detail(id: string) {
    return http.get<unknown, PasswordPolicyRecord>(`/admin/password/policy/${id}`)
  },
  create(payload: PasswordPolicyPayload) {
    return http.post<unknown, PasswordPolicyRecord>('/admin/password/policy', payload)
  },
  update(id: string, payload: PasswordPolicyPayload) {
    return http.put<unknown, PasswordPolicyRecord>(`/admin/password/policy/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/password/policy/status/${id}`)
  },
  list() {
    return http.get<unknown, PasswordPolicyListItem[]>('/admin/password/policy/list')
  },
}
