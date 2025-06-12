<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white shadow-md rounded-lg p-8 w-full max-w-lg">
      <div class="text-center mb-6">
        <h1 class="text-2xl font-bold text-orange-500 mt-2">Register New User</h1>
      </div>
      <form @submit.prevent="handleRegister" class="space-y-4" novalidate>
        <div>
          <label for="username" class="block text-sm font-medium text-gray-700">Username</label>
          <el-input
            v-model="registerForm.userName"
            id="username"
            type="text"
            placeholder="Enter your username (2-20 characters)"
            class="mt-1 w-full custom-input-style" 
            clearable
          />
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
          <el-input
            v-model="registerForm.password"
            id="password"
            :type="passwordFieldType"
            placeholder="Enter your password (6-16 characters)"
            class="mt-1 w-full custom-input-style"
            clearable
            autocomplete="new-password"
          >
            <template #suffix>
              <el-icon
                v-if="registerForm.password" 
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
        <div>
          <label for="confirmPassword" class="block text-sm font-medium text-gray-700">Confirm Password</label>
          <el-input
            v-model="registerForm.confirmPassword"
            id="confirmPassword"
            :type="confirmPasswordFieldType"
            placeholder="Confirm your password"
            class="mt-1 w-full custom-input-style"
            clearable
            autocomplete="new-password"
          >
            <template #suffix>
              <el-icon
                v-if="registerForm.confirmPassword"
                class="el-input__icon cursor-pointer"
                @click="toggleConfirmPasswordVisibility"
                @mousedown.prevent
              >
                <View v-if="confirmPasswordFieldType === 'password'" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
          <p v-if="passwordMismatchDisplay" class="text-red-500 text-xs mt-1">Passwords do not match.</p>
        </div>
        <div>
          <label for="phoneNumber" class="block text-sm font-medium text-gray-700">Phone Number</label>
          <el-input
            v-model="registerForm.phoneNumber"
            id="phoneNumber"
            type="tel" 
            placeholder="Enter your phone number"
            class="mt-1 w-full custom-input-style"
            clearable
            @input="formatPhoneNumber" 
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Gender</label>
          <div class="flex items-center mt-2 space-x-4">
            <label class="inline-flex items-center cursor-pointer">
              <input
                type="radio"
                value="Prefer not to say"
                v-model="registerForm.sexChoice"
                name="gender"
                class="form-radio text-orange-500 accent-orange-500"
              />
              <span class="ml-2">Prefer not to say</span>
            </label>
            <label class="inline-flex items-center cursor-pointer">
              <input
                type="radio"
                value="Male"
                v-model="registerForm.sexChoice"
                name="gender"
                class="form-radio text-orange-500 accent-orange-500"
              />
              <span class="ml-2">Male</span>
            </label>
            <label class="inline-flex items-center cursor-pointer">
              <input
                type="radio"
                value="Female"
                v-model="registerForm.sexChoice"
                name="gender"
                class="form-radio text-orange-500 accent-orange-500"
              />
              <span class="ml-2">Female</span>
            </label>
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
            <span>{{ isLoading ? 'Registering...' : 'Register' }}</span>
          </button>
        </div>
        <div class="text-center mt-4">
          <p class="text-sm text-gray-500">
            Already have an account?
            <button type="button" @click="goToLogin" class="text-orange-500 hover:underline focus:outline-none">Log in</button>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElInput, ElIcon } from 'element-plus';
import { View, Hide } from '@element-plus/icons-vue';

const router = useRouter();
const isLoading = ref(false);
const API_BASE_URL = 'http://127.0.0.1:9231';

const registerForm = reactive({
  userName: '',
  password: '',
  confirmPassword: '',
  phoneNumber: '',
  sexChoice: 'Prefer not to say',
});

const passwordFieldType = ref('password');
const confirmPasswordFieldType = ref('password');

const genderMap = {
  Male: 0,
  Female: 1,
  'Prefer not to say': 2,
};

const passwordMismatchDisplay = computed(() =>
  registerForm.password && registerForm.confirmPassword && registerForm.password !== registerForm.confirmPassword
);

const togglePasswordVisibility = () => {
  passwordFieldType.value = passwordFieldType.value === 'password' ? 'text' : 'password';
};

const toggleConfirmPasswordVisibility = () => {
  confirmPasswordFieldType.value = confirmPasswordFieldType.value === 'password' ? 'text' : 'password';
};

const formatPhoneNumber = (value) => {
  const  digitsOnly = value.toString().replace(/\D/g, '');
  if (registerForm.phoneNumber !== digitsOnly) {
    registerForm.phoneNumber = digitsOnly;
  }
};


async function handleRegister() {
  if (!registerForm.userName.trim()) {
    ElMessage.error('Username is required.');
    return;
  }
  if (registerForm.userName.trim().length < 2 || registerForm.userName.trim().length > 20) {
    ElMessage.error('Username must be between 2 and 20 characters.');
    return;
  }

  if (!registerForm.password) {
    ElMessage.error('Password is required.');
    return;
  }
  if (registerForm.password.length < 6 || registerForm.password.length > 16) {
    ElMessage.error('Password must be between 6 and 16 characters.');
    return;
  }

  if (!registerForm.confirmPassword) {
    ElMessage.error('Confirm Password is required.');
    return;
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('Passwords do not match. Please re-enter.');
    return;
  }

  if (!registerForm.phoneNumber.trim()) {
    ElMessage.error('Phone Number is required.');
    return;
  }
  const phonePattern = /^[0-9]{8,15}$/;
  if (!phonePattern.test(registerForm.phoneNumber)) { 
    ElMessage.error('Please enter a valid phone number (8-15 digits).');
    return;
  }

  isLoading.value = true;
  const payload = {
    userName: registerForm.userName.trim(),
    password: registerForm.password,
    phoneNumber: registerForm.phoneNumber,
    sex: genderMap[registerForm.sexChoice],
  };

  try {
    const response = await axios.post(`${API_BASE_URL}/sysUser/register`, payload);
    const res = response.data;
    if (res.code === 200) {
      ElMessage.success(res.msg || 'Registration successful! Please log in.');
      router.push({ name: 'Login' });
    } else {
      ElMessage.error(res.msg || 'Registration failed. Please try again.');
    }
  } catch (error) {
    console.error('Registration API request failed:', error);
    let errorMsg = 'Registration failed due to a network or server error. Please try again later.';
    if (error.response && error.response.data && error.response.data.msg) {
      errorMsg = error.response.data.msg;
    } else if (error.response && error.response.status) {
      errorMsg = `Registration failed with server error: ${error.response.status}. Please try again.`;
    }
    ElMessage.error(errorMsg);
  } finally {
    isLoading.value = false;
  }
}

function goToLogin() {
  router.push({ name: 'Login' });
}
</script>

<style scoped>
.form-radio {
  accent-color: #f97316;
}
.el-input__icon.cursor-pointer {
  cursor: pointer;
}
</style>