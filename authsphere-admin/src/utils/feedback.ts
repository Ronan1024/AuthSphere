import { ElMessage } from 'element-plus'

const MESSAGE_DURATION = 4500

export const showErrorMessage = (message: string) => {
  ElMessage.closeAll('error')
  ElMessage({
    message,
    type: 'error',
    showClose: true,
    duration: MESSAGE_DURATION,
    grouping: true,
    customClass: 'app-error-message',
  })
}

export const showSuccessMessage = (message: string) => {
  ElMessage({
    message,
    type: 'success',
    duration: 2500,
    grouping: true,
    customClass: 'app-success-message',
  })
}
