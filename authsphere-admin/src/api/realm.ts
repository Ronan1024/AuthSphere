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
}

export interface RealmPayload {
  code: string
  name: string
  typeCategoryId?: string | number | null
  loginUrl?: string
  registerEnabled: boolean
  ssoEnabled: boolean
  passwordPolicy?: string | number | null
  mfaPolicy?: string | number | null
  uniquePolicy?: string | number | null
  description?: string
}

export interface RealmRecord extends RealmPayload {
  id: string
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
}
