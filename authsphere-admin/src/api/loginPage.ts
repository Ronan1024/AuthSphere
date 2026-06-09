import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface AuthMethodInfo {
  id: string
  name: string
  description?: string
}

export interface LoginPageQuery {
  page: number
  size: number
  code?: string
  name?: string
  applicableRealmTypeId?: string | number | null
  authMethod?: string
  status?: number
}

export interface LoginPagePayload {
  code: string
  name: string
  applicableRealmTypeId?: string | number | null
  pageTitle: string
  pageSubtitle?: string
  logoUrl?: string
  backgroundUrl?: string
  authMethods: string[]
  defaultAuthMethod: string
  showForgotPassword: boolean
  forgotPasswordUrl?: string
  showRegister: boolean
  registerUrl?: string
  showThirdPartyLogin: boolean
  successRedirectUrl?: string
  failurePromptMode: string
  defaultPage: boolean
  status: number
  description?: string
}

export interface LoginPageRecord {
  id: string
  code: string
  name: string
  applicableRealmTypeId?: string | number | null
  applicableRealmTypeName?: string
  authMethod?: AuthMethodInfo[]
  defaultPage: boolean
  status: number
  referenceCount?: number
  // Detail extensions
  pageTitle?: string
  pageSubtitle?: string
  logoUrl?: string
  backgroundUrl?: string
  defaultAuthMethod?: string
  showForgotPassword?: boolean
  forgotPasswordUrl?: string
  showRegister?: boolean
  registerUrl?: string
  showThirdPartyLogin?: boolean
  successRedirectUrl?: string
  failurePromptMode?: string
  systemBuiltin?: boolean
  description?: string
  realmReferenceCount?: number
  clientReferenceCount?: number
  createTime?: string
  updateTime?: string
}

export interface LoginPagePreview {
  pageTitle: string
  pageSubtitle?: string
  logoUrl?: string
  backgroundUrl?: string
  authMethods: string[]
  defaultAuthMethod: string
  showForgotPassword: boolean
  forgotPasswordUrl?: string
  showRegister: boolean
  registerUrl?: string
  showThirdPartyLogin: boolean
  failurePromptMode: string
}

export const loginPageApi = {
  page(params: LoginPageQuery) {
    return http.post<unknown, PageResult<LoginPageRecord>>('/admin/login/page/page', params)
  },
  list() {
    return http.get<unknown, LoginPageRecord[]>('/admin/login/page/list')
  },
  detail(id: string | number) {
    return http.get<unknown, LoginPageRecord>(`/admin/login/page/${id}`)
  },
  preview(id: string | number) {
    return http.get<unknown, LoginPagePreview>(`/admin/login/page/${id}/preview`)
  },
  create(payload: LoginPagePayload) {
    return http.post<unknown, string | number>('/admin/login/page', payload)
  },
  update(id: string | number, payload: LoginPagePayload) {
    return http.put<unknown, boolean>(`/admin/login/page/${id}`, payload)
  },
  enable(id: string | number) {
    return http.put<unknown, boolean>(`/admin/login/page/${id}/enable`)
  },
  disable(id: string | number) {
    return http.put<unknown, boolean>(`/admin/login/page/${id}/disable`)
  },
  delete(id: string | number) {
    return http.delete<unknown, boolean>(`/admin/login/page/${id}`)
  }
}
