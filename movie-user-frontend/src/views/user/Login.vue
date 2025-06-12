<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white shadow-md rounded-lg p-8 w-full max-w-md">
      <div class="text-center mb-6">
        <h1 class="text-2xl font-bold text-orange-500 mt-2">Orange Cinema</h1>
      </div>
      <!-- 登录表单 -->
      <form @submit.prevent="handleLogin" class="space-y-4" novalidate>
        <!-- 用户名 -->
        <div>
          <label for="username" class="block text-sm font-medium text-gray-700">Username</label>
          <div class="mt-1">
            <el-input
              v-model="loginForm.userName"
              id="username"
              type="text"
              placeholder="Enter your username"
              class="w-full custom-input-style" 
              clearable
              required
            />
          </div>
        </div>
        <!-- 密码 -->
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
          <div class="mt-1">
            <el-input
              v-model="loginForm.password"
              id="password"
              :type="passwordFieldType"
              placeholder="Enter your password"
              class="w-full custom-input-style"
              clearable
              required
              autocomplete="current-password"
            >
              <template #suffix>
                <el-icon
                  v-if="loginForm.password" 
                  class="el-input__icon cursor-pointer"
                  @click="togglePasswordVisibility"
                  @mousedown.prevent
                >
                  <View v-if="passwordFieldType === 'password'" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </div>
        </div>
        <div class="pt-2">
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full bg-orange-500 text-white py-2 rounded-lg hover:bg-orange-600 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          >
            <svg v-if="isLoading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{{ isLoading ? 'Logging in...' : 'Login' }}</span>
          </button>
        </div>
        <div class="text-center mt-4">
          <p class="text-sm text-gray-500">
            Don’t have an account?
            <button
              type="button"
              @click="registerAccount"
              class="text-orange-500 hover:underline focus:outline-none"
            >
              Sign up
            </button>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import axios from 'axios';
import { ElMessage, ElInput, ElIcon } from 'element-plus';
import { View, Hide } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/authStore'; 

const router = useRouter();
const isLoading = ref(false);
const authStore = useAuthStore();

const loginForm = reactive({
    userName: "",
    password: "",
});

const passwordFieldType = ref('password');

const togglePasswordVisibility = () => {
  passwordFieldType.value = passwordFieldType.value === 'password' ? 'text' : 'password';
};

async function handleLogin() {
    if (!loginForm.userName.trim()) {
        ElMessage.warning("Username is required.");
        return;
    }
    if (!loginForm.password) {
        ElMessage.warning("Password is required.");
        return;
    }

    isLoading.value = true;

    try {
        const response = await axios.post(
            "http://127.0.0.1:9231/sysUser/login",
            {
                userName: loginForm.userName.trim(),
                password: loginForm.password
            }
        );

        const res = response.data; 

        if (res.code === 200 && res.data && res.data.token && res.data.sysUser) {
            ElMessage.success({ message: "Login successful!", duration: 1000 });
            console.log("Login response data:", res.data);

            const userDataFromApi = { ...res.data.sysUser };
            delete userDataFromApi.password; 
            delete userDataFromApi.salt; 
            const tokenFromApi = res.data.token;

            authStore.loginSuccess(userDataFromApi, tokenFromApi);

            const redirectSessionId = window.sessionStorage.getItem("sessionId"); 
            if (redirectSessionId && redirectSessionId !== '0') {
                window.sessionStorage.removeItem("sessionId");
                router.push({ name: 'ChooseSeat', params: { sessionId: redirectSessionId } });
            } else {
                router.push({ name: 'Home' });
            }

        } else {
            ElMessage.error(res.msg || "Login failed. Please check your credentials.");
        }
    } catch (error) {
        console.error("Login API request failed:", error);
        let errorMsg = "Login failed due to a network or server error.";
        if (error.response && error.response.data && error.response.data.msg) {
            errorMsg = error.response.data.msg;
        } else if (error.response && error.response.status) {
            errorMsg = `Login failed. Server responded with status: ${error.response.status}`;
        }
        ElMessage.error(errorMsg);
    } finally {
        isLoading.value = false;
    }
}

function registerAccount() {
    router.push({ name: "Register" }); 
}
</script>

<style scoped>
.el-input__icon.cursor-pointer {
  cursor: pointer;
}
</style>