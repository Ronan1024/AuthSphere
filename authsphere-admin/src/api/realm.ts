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
  realmTypeId?: string | number | null
  typeCategoryId?: string | number | null
  registerEnabled: boolean
  ssoEnabled: boolean
  ssoSessionTimeout?: number
  ssoIdleTimeout?: number
  ssoSingleLogout?: string
  existingSessionHandler?: string
  noClientIdHandler?: string
  authPolicyId?: string | number | null
  passwordPolicy?: string | number | null
  mfaPolicy?: string | number | null
  uniquePolicy?: string | number | null
  description?: string
}

export interface RealmRecord extends RealmPayload {
  id: string
  realmTypeName?: string
  authPolicyName?: string
  ssoClientCount?: number
  accountCount?: number
  authMethodList?: { id: string | number; name: string; description?: string }[]
  status?: number
  createTime?: string
  updateTime?: string
}

export const realmApi = {
  page(params: RealmQuery) {
    return http.post<unknown, PageResult<RealmRecord>>('/admin/realm/page', params)
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
