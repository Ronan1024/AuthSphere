import http from './http'
import type { PageResult } from './subject'

export const MEMBER_STATUS_ENABLED = 1
export const MEMBER_STATUS_DISABLED = 2
export const MEMBER_STATUS_REMOVED = 3

export const MEMBER_TYPE_OWNER = 1
export const MEMBER_TYPE_ADMIN = 2
export const MEMBER_TYPE_MEMBER = 3
export const MEMBER_TYPE_STAFF = 4

export interface SubjectMemberQuery {
  page: number
  size: number
  subjectId?: string | number
  accountId?: string | number
  keyword?: string
  memberType?: number
  memberStatus?: number
  accountStatus?: number
  realmId?: string | number
}

export interface SubjectMemberPayload {
  accountId: string | number
  memberType: number
  displayName?: string
  remark?: string
}

export interface SubjectMemberCreateAccountPayload {
  username: string
  mobile?: string
  email?: string
  password: string
  memberType: number
  displayName?: string
  remark?: string
}

export interface SubjectMemberRecord {
  id: string
  subjectId: string
  subjectCode?: string
  subjectName?: string
  accountId: string
  realmId: string
  realmCode?: string
  realmName?: string
  username: string
  mobile?: string
  email?: string
  memberType: number
  memberTypeName: string
  displayName?: string
  remark?: string
  memberStatus: number
  accountStatus: number
  joinedAt?: string
  joinedByAccountId?: string
  joinedByUsername?: string
  disabledAt?: string
  removedAt?: string
  createTime?: string
  updateTime?: string
}

export const subjectMemberApi = {
  page(params: SubjectMemberQuery) {
    return http.post<unknown, PageResult<SubjectMemberRecord>>('/admin/subject-member/page', params)
  },
  addExisting(subjectId: string, payload: SubjectMemberPayload) {
    return http.post<unknown, boolean>(`/admin/subject-member/subject/${subjectId}/existing-account`, payload)
  },
  createAccountAndAdd(subjectId: string, payload: SubjectMemberCreateAccountPayload) {
    return http.post<unknown, boolean>(`/admin/subject-member/subject/${subjectId}/new-account`, payload)
  },
  update(id: string, payload: SubjectMemberPayload) {
    return http.put<unknown, boolean>(`/admin/subject-member/${id}`, payload)
  },
  enable(id: string) {
    return http.put<unknown, boolean>(`/admin/subject-member/${id}/enable`)
  },
  disable(id: string) {
    return http.put<unknown, boolean>(`/admin/subject-member/${id}/disable`)
  },
  setDefault(id: string) {
    return http.put<unknown, boolean>(`/admin/subject-member/${id}/set-default`)
  },
  remove(id: string) {
    return http.delete<unknown, boolean>(`/admin/subject-member/${id}`)
  },
}
