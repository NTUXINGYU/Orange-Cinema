// src/plugins/axios.js
import axios from 'axios'
import router from '@/router'
import store from '@/store' // 如果你需要与 Vuex 交互的话

const _axios = axios.create({
  // baseURL: 'http://localhost:8080/api', // 如果有统一前缀
  // timeout: 10000,
})

_axios.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => Promise.reject(error)
)

_axios.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message

    if (status === 401 || message === 'token失效') {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('loginUser')

      // 可选：更新 store 状态为未登录
      // store.commit('auth/LOGOUT') 你可以定义 auth 模块的 logout mutation

      router.push({ name: 'Login', query: { sessionExpired: 'true' } }).catch(() => {})
    }

    return Promise.reject(error)
  }
)

export default {
  install(app) {
    app.config.globalProperties.$axios = _axios
    app.config.globalProperties.axios = _axios
  }
}

export { _axios } // 如果你想单独 import 使用
