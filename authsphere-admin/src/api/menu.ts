import http from './http'

export interface AppMenuRecord {
  id: string
  appCode: string
  clientCode: string
  parentId?: string
  menuCode: string
  menuName: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortNo: number
  visible: number
  status: number
  builtIn: number
  createTime?: string
  updateTime?: string
  // For UI tree
  children?: AppMenuRecord[]
}

export interface AppMenuPayload {
  parentId?: string
  menuCode: string
  menuName: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortNo: number
  visible: number
  status: number
}

export const menuApi = {
  listByClient(clientId: string) {
    return http.get<unknown, AppMenuRecord[]>(`/api/app-clients/${clientId}/menus`)
  },
  create(clientId: string, payload: AppMenuPayload) {
    return http.post<unknown, boolean>(`/api/app-clients/${clientId}/menus`, payload)
  },
  detail(id: string) {
    return http.get<unknown, AppMenuRecord>(`/api/app-client-menus/${id}`)
  },
  edit(id: string, payload: AppMenuPayload) {
    return http.put<unknown, boolean>(`/api/app-client-menus/${id}`, payload)
  },
  enable(id: string) {
    return http.post<unknown, boolean>(`/api/app-client-menus/${id}/enable`)
  },
  disable(id: string) {
    return http.post<unknown, boolean>(`/api/app-client-menus/${id}/disable`)
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/api/app-client-menus/${id}`)
  }
}
