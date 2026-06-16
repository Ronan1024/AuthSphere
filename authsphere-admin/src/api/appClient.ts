import http from './http'
import type { PageResult } from './realm'

export interface AppClientQuery {
  page: number
  size: number
  clientCode?: string
  clientName?: string
  clientType?: string
  status?: number
}

export interface AppClientRecord {
  id: string
  appId: string
  appCode: string
  clientCode: string
  clientName: string
  clientType: string | number
  realmId?: string | number | null
  defaultRealmId?: string | number | null
  realmName?: string
  defaultEntryUrl?: string
  loginMode?: 'IAM_HOSTED' | 'EXTERNAL_REDIRECT' | 'API_ONLY' | 'SERVICE' | string
  externalLoginUrl?: string
  loginCallbackUrl?: string
  loginPageId?: string | number | null
  loginPageName?: string
  authPolicyId?: string | number | null
  authPolicyName?: string
  ossConfigId?: string | number | null
  status: number
  description?: string
  createTime?: string
  updateTime?: string
}

export interface AppClientPayload {
  id?: string
  clientCode: string
  clientName: string
  clientType: string | number
  status: number
  builtIn?: number
  description?: string
  realmId?: string | number | null
  defaultRealmId?: string | number | null
  defaultEntryUrl?: string
  loginMode?: 'IAM_HOSTED' | 'EXTERNAL_REDIRECT' | 'API_ONLY' | 'SERVICE' | string
  externalLoginUrl?: string
  loginCallbackUrl?: string
  loginPageId?: string | number | null
  authPolicyId?: string | number | null
  ossConfigId?: string | number | null
}

export interface AppClientExternalConfigRecord {
  id: string
  appClientId: string
  providerType: string
  providerCode: string
  providerName: string
  appId?: string
  appSecret?: string
  publicKey?: string
  privateKey?: string
  callbackUrl?: string
  configJson?: string
  status: number
}

export interface AppClientExternalConfigPayload {
  providerType: string
  providerCode: string
  providerName: string
  appId?: string
  appSecret?: string
  publicKey?: string
  privateKey?: string
  callbackUrl?: string
  configJson?: string
  status: number
}

export const appClientApi = {
  page(params: AppClientQuery) {
    return http.post<unknown, PageResult<AppClientRecord>>('/api/app-clients/page', params)
  },
  listByApp(appId: string) {
    return http.get<unknown, AppClientRecord[]>(`/api/apps/${appId}/clients`)
  },
  create(appId: string, payload: AppClientPayload) {
    return http.post<unknown, boolean>(`/api/apps/${appId}/clients`, payload)
  },
  detail(id: string) {
    return http.get<unknown, AppClientRecord>(`/api/app-clients/${id}`)
  },
  edit(id: string, payload: AppClientPayload) {
    return http.put<unknown, boolean>(`/api/app-clients/${id}`, payload)
  },
  enable(id: string) {
    return http.post<unknown, boolean>(`/api/app-clients/${id}/enable`)
  },
  disable(id: string) {
    return http.post<unknown, boolean>(`/api/app-clients/${id}/disable`)
  },
  listExternalConfigs(clientId: string) {
    return http.get<unknown, AppClientExternalConfigRecord[]>(`/api/app-clients/${clientId}/external-configs`)
  },
  createExternalConfig(clientId: string, payload: AppClientExternalConfigPayload) {
    return http.post<unknown, boolean>(`/api/app-clients/${clientId}/external-configs`, payload)
  },
  editExternalConfig(configId: string, payload: AppClientExternalConfigPayload) {
    return http.put<unknown, boolean>(`/api/app-client-external-configs/${configId}`, payload)
  },
  enableExternalConfig(configId: string) {
    return http.post<unknown, boolean>(`/api/app-client-external-configs/${configId}/enable`)
  },
  disableExternalConfig(configId: string) {
    return http.post<unknown, boolean>(`/api/app-client-external-configs/${configId}/disable`)
  }
}
