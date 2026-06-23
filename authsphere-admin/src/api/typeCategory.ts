import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface TypeCategoryQuery {
  page: number
  size: number
  code?: string
  name?: string
  status?: number
}

export interface TypeCategoryPayload {
  code: string
  name: string
  description?: string
  systemBuiltin: boolean
  editable: boolean
}

export interface TypeCategoryRecord extends TypeCategoryPayload {
  id: string
  status: number
  createTime?: string
  updateTime?: string
  realmCount?: number
  referenceCount?: number
  enabledCount?: number
  disabledCount?: number
  realmList?: Array<{
    id: string
    code: string
    name: string
    status: number
  }>
}


export const typeCategoryApi = {
  page(params: TypeCategoryQuery) {
    return http.post<unknown, PageResult<TypeCategoryRecord>>('/admin/category/page', params)
  },
  list() {
    return http.get<unknown, TypeCategoryRecord[]>('/admin/category/list')
  },
  detail(id: string) {
    return http.get<unknown, TypeCategoryRecord>(`/admin/category/${id}`)
  },
  create(payload: TypeCategoryPayload) {
    return http.post<unknown, boolean>('/admin/category', payload)
  },
  update(id: string, payload: TypeCategoryPayload) {
    return http.put<unknown, boolean>(`/admin/category/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/category/status/${id}`)
  },
  delete(id: string) {
    return http.delete<unknown, boolean>(`/admin/category/${id}`)
  },
}
