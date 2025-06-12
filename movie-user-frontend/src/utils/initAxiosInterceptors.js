import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/authStore';
import router from '@/router'

export default function initAxiosInterceptors() {
  // 请求拦截器：自动加 token
  axios.interceptors.request.use(
    config => {
      const authStore = useAuthStore()
      const token = authStore.getToken
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
      return config
    },
    error => Promise.reject(error)
  )

  // 响应拦截器：处理登录失效
  axios.interceptors.response.use(
    response => response,
    error => {
      if (error.response && error.response.status === 401) {
        const authStore = useAuthStore()
        ElMessage.error('Session expired. Redirecting to login...')
        authStore.logout(true)
      } else if (error.response) {
        ElMessage.error(error.response.data.message || 'Request failed.')
      } else {
        ElMessage.error('Network error')
      }
      return Promise.reject(error)
    }
  )
}
