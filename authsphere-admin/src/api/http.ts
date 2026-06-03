import axios, { AxiosError } from 'axios'

export interface ApiResult<T> {
  data: T
  code: string
  message: string
  version: string
}

const SUCCESS_CODE = '0000'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== SUCCESS_CODE) {
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body.data
    }
    return response.data
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const responseBody = error.response?.data
    const message =
      responseBody?.message ||
      (typeof responseBody === 'string' ? responseBody : undefined) ||
      error.message ||
      '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default http
