import http from './http'
import type { PageResult } from './realm'

export interface RoleQuery {
  page: number
  size: number
  realmId?: string | number
  subjectId?: string | number
  clientId?: string | number
  keyword?: string
  status?: number
}

export interface RoleRecord {
  id: string
  realmId: string
  subjectId?: string
  clientId: string
  roleName: string
  roleCode: string
  roleType: string
  dataScope: string
  status: number
  remark?: string
  createTime?: string
  updateTime?: string
  clientName?: string
  subjectName?: string
  realmName?: string
}

export interface RolePayload {
  realmId: string | number
  subjectId?: string | number | null
  clientId: string | number
  roleName: string
  roleCode: string
  roleType?: string
  dataScope?: string
  status?: number
  remark?: string
}

export interface RoleUpdatePayload {
  roleName: string
  dataScope?: string
  status?: number
  remark?: string
}

export interface RoleResourceResponse {
  roleId: string
  resourceIds: string[]
}

export interface RoleResourceAssignPayload {
  resourceIds: string[]
}

export const roleApi = {
  page(params: RoleQuery) {
    return http.get<unknown, PageResult<RoleRecord>>('/api/iam/roles', { params })
  },
  create(payload: RolePayload) {
    return http.post<unknown, boolean>('/api/iam/roles', payload)
  },
  detail(id: string) {
    return http.get<unknown, RoleRecord>(`/api/iam/roles/${id}`)
  },
  update(id: string, payload: RoleUpdatePayload) {
    return http.put<unknown, boolean>(`/api/iam/roles/${id}`, payload)
  },
  enable(id: string) {
    return http.put<unknown, boolean>(`/api/iam/roles/${id}/enable`)
  },
  disable(id: string) {
    return http.put<unknown, boolean>(`/api/iam/roles/${id}/disable`)
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/api/iam/roles/${id}`)
  },
  listResources(id: string) {
    return http.get<unknown, RoleResourceResponse>(`/api/iam/roles/${id}/resources`)
  },
  assignResources(id: string, payload: RoleResourceAssignPayload) {
    return http.put<unknown, boolean>(`/api/iam/roles/${id}/resources`, payload)
  }
}
