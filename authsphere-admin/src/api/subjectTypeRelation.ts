import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface SubjectTypeRelationQuery {
  page: number
  size: number
  parentTypeId?: string | number
  childTypeId?: string | number
  relationType?: number
  status?: number
}

export interface SubjectTypeRelationPayload {
  parentTypeId: string | number
  childTypeId: string | number
  relationType: number
  allowCreate: boolean
  allowManage: boolean
  description?: string
}

export interface SubjectTypeRelationRecord extends SubjectTypeRelationPayload {
  id: string
  parentTypeCode?: string
  parentTypeName?: string
  childTypeCode?: string
  childTypeName?: string
  status: number
  createTime?: string
  updateTime?: string
}

export const subjectTypeRelationApi = {
  page(params: SubjectTypeRelationQuery) {
    return http.post<unknown, PageResult<SubjectTypeRelationRecord>>('/admin/subject/type/relation/page', params)
  },
  list() {
    return http.get<unknown, SubjectTypeRelationRecord[]>('/admin/subject/type/relation/list')
  },
  create(payload: SubjectTypeRelationPayload) {
    return http.post<unknown, boolean>('/admin/subject/type/relation', payload)
  },
  update(id: string, payload: SubjectTypeRelationPayload) {
    return http.put<unknown, boolean>(`/admin/subject/type/relation/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/subject/type/relation/status/${id}`)
  },
  remove(id: string) {
    return http.delete<unknown, boolean>(`/admin/subject/type/relation/${id}`)
  },
}
