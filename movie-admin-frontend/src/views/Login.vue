<template>
  <div class="login-page-wrapper">
    <div class="login-form-container">
      <h2 class="login-title">
        <i class="iconfont icon-login-title login-title-icon"></i>
        Admin Login
      </h2>

      <el-form
        :model="loginCredentials"
        :rules="loginRules"
        ref="loginFormInstance"
        class="login-actual-form"
        @submit.prevent="performLogin"
      >
        <el-form-item prop="userName">
          <el-input
            v-model="loginCredentials.userName"
            placeholder="Username"
            clearable
            size="large"
          >
            <template #prepend>
              <i class="iconfont icon-user"></i>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginCredentials.password"
            type="password"
            placeholder="Password"
            show-password
            clearable
            size="large"
          >
            <template #prepend>
              <i class="iconfont icon-lock"></i>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="performLogin"
            :loading="isLoggingIn"
            native-type="submit"
            size="large"
            class="login-button"
          >
            <i class="iconfont icon-sure-button login-button-icon"></i>
            Login
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useStore } from 'vuex'; // [核心修改 1/3] Import useStore
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const router = useRouter();
const store = useStore(); // [核心修改 2/3] Get the store instance
const isLoggingIn = ref(false);

const loginCredentials = ref({
  userName: '',
  password: ''
});

const loginFormInstance = ref(null);

const loginRules = ref({
  userName: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
  ]
});

const performLogin = async () => {
  if (!loginFormInstance.value) return;

  try {
    const isValid = await loginFormInstance.value.validate();
    if (!isValid) return;

    isLoggingIn.value = true;

    const response = await axios.post('sysUser/login', loginCredentials.value);
    const res = response.data;

    if (res.code !== 200) {
      ElMessage.error(res.msg || 'Login failed. Please check your credentials.');
      return;
    }

    const user = res.data?.sysUser;
    const token = res.data?.token;

    if (!user || !token) {
      ElMessage.error('Login failed: Invalid server response.');
      return;
    }

    if (user.sysRole?.roleId !== 1 && user.sysRole?.roleId !== 2) {
        ElMessage.error('Sorry, you do not have permission to log in, please contact the administrator for permissions.');
        return;
    }

    ElMessage.success('Login successful.');

    // --- [核心修改 3/3] ---
    // Instead of manually setting sessionStorage, dispatch the Vuex action.
    // The action will handle both updating the state and persisting to sessionStorage.
    await store.dispatch('login', {
        token: token,
        userInfo: { // Pass a structured userInfo object
            sysUser: user,
            cinemaId: res.data.cinemaId,
            cinemaName: res.data.cinemaName
        }
    });
    
    // The permission logic can be simplified or moved to the store if needed.
    // For now, setting it here is fine.
    sessionStorage.setItem('btnPermission', 'admin');

    await router.push('/welcome');

  } catch (error) {
    // Error handling can be improved to use the global interceptor's message
    console.error("Login error:", error);
    if (!error.response) { // If it's a network error, the global handler already showed a message.
        ElMessage.error("An unexpected error occurred during login.");
    }
  } finally {
    isLoggingIn.value = false;
  }
};
</script>


<style scoped>
/* Styles remain unchanged */
.login-page-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
  box-sizing: border-box;
}

.login-form-container {
  width: 100%;
  max-width: 380px;
  padding: 40px 30px;
  background-color: #ffffff;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-title-icon {
  font-size: 24px;
  margin-right: 10px;
  color: #409EFF;
}

.login-actual-form {
  width: 100%;
}

:deep(.el-input-group__prepend) {
  padding: 0 12px;
  background-color: #fff;
  border-right: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-input-group__prepend .iconfont) {
  font-size: 16px;
  color: #909399;
}

:deep(.el-input.is-focus .el-input-group__prepend .iconfont) {
  color: #409EFF;
}

:deep(.el-input--large .el-input__wrapper) {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  padding-left: 10px;
}

.login-button {
  width: 100%;
  max-width: 320px;
  font-size: 16px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-button-icon {
  margin-right: 8px;
}
</style>