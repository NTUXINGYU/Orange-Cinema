import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store'; // Import the Vuex store we just defined
import ElementPlus, { ElMessageBox } from 'element-plus'; // Import ElMessageBox
import 'element-plus/dist/index.css';
import axios from 'axios';

import './assets/font/iconfont.css';

const app = createApp(App);

// --- Axios base configuration ---
axios.defaults.baseURL = 'http://127.0.0.1:9231/';
axios.defaults.headers.post['Content-Type'] = 'application/json';

// --- Axios request interceptor ---
axios.interceptors.request.use(config => {
  // Get token from Vuex store state for consistency
  const token = store.state.token; 
  if (token) {
    config.headers.Token = token; 
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// --- Axios response interceptor for global error handling ---

// A flag to prevent multiple "session expired" dialogs
let isHandlingTokenExpiration = false;

function handleTokenExpiration() {
  if (isHandlingTokenExpiration) {
    return;
  }
  isHandlingTokenExpiration = true;

  ElMessageBox.alert(
    'Your session has expired, or your credentials are no longer valid. You will be redirected to the login page.', 
    'Authentication Expired', 
    {
      confirmButtonText: 'OK',
      type: 'warning',
      // This callback runs after the user clicks "OK"
      callback: () => {
        // Dispatch the 'logout' action in our Vuex store to clear all state
        store.dispatch('logout');

        // Force a full page reload to the login page.
        // This is the cleanest way to reset the entire application's state.
        window.location.href = '/login'; 
      }
    }
  );
}

// Standard HTTP status code messages
const codeMessage = {
  403: 'Forbidden: You do not have permission to access this resource.',
  404: 'Not Found: The requested resource does not exist.',
  500: 'Internal Server Error: Please try again later.',
  502: 'Gateway Error.',
  503: 'Service Unavailable.',
  504: 'Gateway Timeout.'
};

axios.interceptors.response.use(
  response => {
    // If the request was successful, just return the response
    return response;
  },
  error => {
    if (error.response) {
      const status = error.response.status;
      const responseData = error.response.data;
      const messageToDisplay = responseData?.msg;

      if (status === 401) {
        // If we get a 401, trigger our robust logout handler
        handleTokenExpiration();
      } else {
        // For all other errors, display an ElMessage toast
        ElMessage.error(messageToDisplay || codeMessage[status] || `Server Error: ${status}`);
      }
    } else if (error.request) {
      // Handle network errors (e.g., server is down)
      ElMessage.error('Network Error: Could not connect to the server.');
    } else {
      // Handle errors that occurred during request setup
      ElMessage.error(`Request Error: ${error.message}`);
    }

    // It's important to still reject the promise so that local .catch() blocks can also handle the error if needed.
    return Promise.reject(error); 
  }
);

// --- Vue app setup ---
app.config.globalProperties.$http = axios;
app.use(router);
app.use(store);
app.use(ElementPlus); 
app.mount('#app');