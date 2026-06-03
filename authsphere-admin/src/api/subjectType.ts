import http from './http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface SubjectTypeQuery {
  page: number
  size: number
  code?: string
  name?: string
  category?: string
  status?: number
}

export interface SubjectTypePayload {
  code: string
  name: string
  category?: string
  canHaveMembers: boolean
  canOpenApp: boolean
  canAssignRole: boolean
  canBeRoot: boolean
  builtIn: boolean
  description?: string
}

export interface SubjectTypeRecord extends SubjectTypePayload {
  id: string
  status: number
  createTime?: string
  updateTime?: string
}

export const subjectTypeApi = {
  page(params: SubjectTypeQuery) {
    return http.post<unknown, PageResult<SubjectTypeRecord>>('/admin/subject/type/page', params)
  },
  list() {
    return http.get<unknown, SubjectTypeRecord[]>('/admin/subject/type/list')
  },
  detail(id: string) {
    return http.get<unknown, SubjectTypeRecord>(`/admin/subject/type/${id}`)
  },
  create(payload: SubjectTypePayload) {
    return http.post<unknown, boolean>('/admin/subject/type', payload)
  },
  update(id: string, payload: SubjectTypePayload) {
    return http.put<unknown, boolean>(`/admin/subject/type/${id}`, payload)
  },
  toggleStatus(id: string) {
    return http.put<unknown, boolean>(`/admin/subject/type/status/${id}`)
  },
  remove(id: string) {
    return http.delete<unknown, boolean>(`/admin/subject/type/${id}`)
  },
}
