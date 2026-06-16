import http from './http'
import type { PageResult } from './subject'

export interface AppPermissionPagePayload {
  page: number
  size: number
  permissionType?: number
}

export interface AppPermissionRecord {
  id: string
  appCode: string
  clientCode: string
  menuId?: string
  permissionCode: string
  permissionName: string
  permissionType: number
  apiPath?: string
  method?: string
  description?: string
  status: number
  builtIn: number
  createTime?: string
  updateTime?: string
}

export interface AppPermissionPayload {
  menuId?: string
  permissionCode: string
  permissionName: string
  permissionType: number
  apiPath?: string
  method?: string
  description?: string
  status: number
  builtIn?: number
}

export const appPermissionApi = {
  listByClient(clientId: string) {
    return http.get<unknown, AppPermissionRecord[]>(`/api/app-clients/${clientId}/permissions`)
  },
  page(clientId: string, payload: AppPermissionPagePayload) {
    return http.post<unknown, PageResult<AppPermissionRecord>>(`/api/app-clients/${clientId}/permissions/page`, payload)
  },
  create(clientId: string, payload: AppPermissionPayload) {
    return http.post<unknown, boolean>(`/api/app-clients/${clientId}/permissions`, payload)
  },
  detail(id: string) {
    return http.get<unknown, AppPermissionRecord>(`/api/app-client-permissions/${id}`)
  },
  edit(id: string, payload: AppPermissionPayload) {
    return http.put<unknown, boolean>(`/api/app-client-permissions/${id}`, payload)
  },
  enable(id: string) {
    return http.post<unknown, boolean>(`/api/app-client-permissions/${id}/enable`)
  },
  disable(id: string) {
    return http.post<unknown, boolean>(`/api/app-client-permissions/${id}/disable`)
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/api/app-client-permissions/${id}`)
  }
}
