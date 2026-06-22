import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface RealmQuery {
  page: number
  size: number
  code?: string
  name?: string
  status?: number
  realmTypeId?: string | number | null
  typeCategoryId?: string | number | null
}


export interface RealmPayload {
  code: string
  name: string
  status?: number
  realmTypeId?: string | number | null
  typeCategoryId?: string | number | null
  registerEnabled: boolean
  ssoEnabled: boolean
  ssoSessionTimeout?: number
  ssoIdleTimeout?: number
  ssoSingleLogout?: string
  existingSessionHandler?: string
  noClientIdHandler?: string
  authMethodIds?: Array<string | number>
  defaultAuthMethodId?: string | number | null
  mfaAuthMethodId?: string | number | null
  captchaMode?: string
  captchaThreshold?: number
  passwordMinLength?: number
  passwordMaxLength?: number
  passwordComplexity?: string
  passwordExpireDays?: number
  accessTokenTimeout?: number
  refreshTokenTimeout?: number
  tokenRotationEnabled?: boolean
  tokenBlacklistEnabled?: boolean
  sessionIdleTimeout?: number
  sessionMultiDevice?: string
  sessionMaxDevices?: number
  loginFailMaxCount?: number
  loginFailWindowMinutes?: number
  loginFailLockMinutes?: number
  loginFailAutoUnlock?: boolean
  description?: string
}

export interface RealmRecord extends RealmPayload {
  id: string
  typeCategoryId?: string | number | null
  realmTypeName?: string
  defaultAuthMethodName?: string
  ssoClientCount?: number
  accountCount?: number
  authMethodList?: { id: string | number; code: string; name: string; description?: string }[]
  status?: number
  createTime?: string
  updateTime?: string
}

export interface RealmDetailRecord extends RealmRecord {
  authMethodIds?: Array<string | number>
}

export const realmApi = {
  page(params: RealmQuery) {
    return http.post<unknown, PageResult<RealmRecord>>('/admin/realm/page', params)
  },
  detail(id: string) {
    return http.get<unknown, RealmDetailRecord>(`/admin/realm/${id}`)
  },
  create(payload: RealmPayload) {
    return http.post<unknown, boolean>('/admin/realm', payload)
  },
  update(id: string, payload: RealmPayload) {
    return http.put<unknown, boolean>(`/admin/realm/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/realm/status/${id}`)
  },
  list() {
    return http.get<unknown, RealmRecord[]>('/admin/realm/list')
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/admin/realm/${id}`)
  },
}
