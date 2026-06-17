import http from './http'
import type { AppClientPayload } from './appClient'
import type { PageResult } from './realm'

export interface AppQuery {
  page: number
  size: number
  appCode?: string
  appName?: string
  appType?: string
  status?: number
}

export interface AppRecord {
  id: string
  appCode: string
  appName: string
  appType: string
  entryUrl?: string
  icon?: string
  status: number
  description?: string
  createTime?: string
  updateTime?: string
  menuSize?: number
  permissionSize?: number
  clientSize?: number
  instanceSize?: number
  clientName?: string[]
}

export interface AppPayload {
  id?: string
  appCode: string
  appName: string
  appType: string
  entryUrl?: string
  icon?: string
  status: number
  builtIn?: number
  description?: string
  clients?: AppClientPayload[]
}

export const appApi = {
  page(params: AppQuery) {
    return http.post<unknown, PageResult<AppRecord>>('/api/apps/page', params)
  },
  create(payload: AppPayload) {
    return http.post<unknown, string>('/api/apps', payload)
  },
  update(id: string, payload: AppPayload) {
    return http.put<unknown, boolean>(`/api/apps/${id}`, payload)
  },
  enable(id: string) {
    return http.post<unknown, boolean>(`/api/apps/${id}/enable`)
  },
  disable(id: string) {
    return http.post<unknown, boolean>(`/api/apps/${id}/disable`)
  },
  detail(id: string) {
    return http.get<unknown, AppRecord>(`/api/apps/${id}`)
  },
}
