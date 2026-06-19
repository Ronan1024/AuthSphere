import http from './http'
import type { PageResult } from './passwordPolicy'

export interface AuthMethodPageQuery {
  page: number
  size: number
  name?: string
  code?: string
  position?: string
  status?: number
}

export interface AuthMethodPayload {
  code: string
  name: string
  positions: string[]
  applicableRange?: string
  status: number
  sortNo: number
  description?: string
  params: Record<string, any>
}

export interface AuthMethodReference {
  id: string
  code: string
  name: string
  role: string
  usage?: string
  status: number
}

export interface AuthMethodRecord {
  id: string
  code: string
  name: string
  positions: string[]
  applicableRange: string
  status: number
  referenceCount: number
  sortNo: number
  description?: string
  createTime?: string
  updateTime?: string
}

export interface AuthMethodDetailResponse extends AuthMethodRecord {
  systemBuiltin: boolean
  params: Record<string, any>
  references: AuthMethodReference[]
}

export interface AuthMethodOptionResponse {
  id: string
  code: string
  name: string
  description?: string
}

export interface AuthMethodTemplateResponse {
  template: string
  defaultCode: string
  defaultName: string
  title: string
  description: string
  allowedParamKeys: string[]
}

export const authMethodApi = {
  page(params: AuthMethodPageQuery) {
    return http.post<unknown, PageResult<AuthMethodRecord>>('/admin/auth/method/page', params)
  },
  list(position?: string) {
    return http.get<unknown, AuthMethodOptionResponse[]>('/admin/auth/method/list', { params: { position } })
  },
  templates() {
    return http.get<unknown, AuthMethodTemplateResponse[]>('/admin/auth/method/templates')
  },
  detail(id: string) {
    return http.get<unknown, AuthMethodDetailResponse>(`/admin/auth/method/${id}`)
  },
  create(payload: AuthMethodPayload) {
    return http.post<unknown, string>('/admin/auth/method', payload)
  },
  update(id: string, payload: AuthMethodPayload) {
    return http.put<unknown, boolean>(`/admin/auth/method/${id}`, payload)
  },
  enable(id: string) {
    return http.put<unknown, boolean>(`/admin/auth/method/${id}/enable`)
  },
  disable(id: string) {
    return http.put<unknown, boolean>(`/admin/auth/method/${id}/disable`)
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/admin/auth/method/${id}`)
  }
}
