import './assets/style.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElIcon from '@element-plus/icons-vue'
import axios from 'axios'
import { useAuthStore } from './stores/authStore'
import initAxiosInterceptors from './utils/initAxiosInterceptors'

axios.interceptors.request.use(config => {
  const token = window.sessionStorage.getItem('token')
  if (token) {
    config.headers.Token = token
  }
  return config
}, error => Promise.reject(error))

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const authStore = useAuthStore()
authStore.checkAuthStatus()
app.use(router)
app.use(ElementPlus)
initAxiosInterceptors()
Object.keys(ElIcon).forEach(key => {
  app.component(key, ElIcon[key])
})

app.mount('#app')