import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface SubjectQuery {
  page: number
  size: number
  subjectTypeId?: string | number
  realmId?: string | number
  code?: string
  name?: string
  rootSubjectId?: string | number
  parentSubjectId?: string | number
  isRoot?: number
  status?: number
}

export interface SubjectPayload {
  subjectTypeId: string | number
  realmId: string | number
  code: string
  name: string
  rootSubjectId?: string | number | null
  parentSubjectId?: string | number | null
  isRoot: number | boolean
  builtIn: number | boolean
  description?: string
}

export interface SubjectRecord extends SubjectPayload {
  id: string
  subjectTypeCode?: string
  subjectTypeName?: string
  realmCode?: string
  realmName?: string
  parentId?: string | number
  parentName?: string
  rootSubjectCode?: string
  rootSubjectName?: string
  parentSubjectCode?: string
  parentSubjectName?: string
  memberCount?: number
  appCount?: number
  childCount?: number
  contractCount?: number
  clientCount?: number
  status: number
  createTime?: string
  updateTime?: string
}

export const subjectApi = {
  page(params: SubjectQuery) {
    return http.post<unknown, PageResult<SubjectRecord>>('/admin/subject/page', params)
  },
  list() {
    return http.get<unknown, SubjectRecord[]>('/admin/subject/list')
  },
  detail(id: string) {
    return http.get<unknown, SubjectRecord>(`/admin/subject/${id}`)
  },
  create(payload: SubjectPayload) {
    return http.post<unknown, boolean>('/admin/subject', payload)
  },
  update(id: string, payload: SubjectPayload) {
    return http.put<unknown, boolean>(`/admin/subject/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/subject/status/${id}`)
  },
  remove(id: string) {
    return http.delete<unknown, boolean>(`/admin/subject/${id}`)
  },
}
