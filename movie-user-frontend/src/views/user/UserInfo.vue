<template>
  <!-- 加载状态 -->
  <div v-if="isLoadingPage" class="flex min-h-[calc(100vh-10rem)] items-center justify-center">
    <el-icon class="is-loading text-4xl text-orange-500"><Loading /></el-icon>
    <p class="ml-2 text-xl text-gray-500">Loading user data...</p>
  </div>

  <!-- 错误状态 -->
  <div v-else-if="fetchError" class="flex min-h-[calc(100vh-10rem)] flex-col items-center justify-center rounded-lg bg-white p-8 shadow-md">
    <el-icon class="text-5xl text-red-500"><CircleCloseFilled /></el-icon>
    <p class="mt-4 text-xl text-red-600">Error loading data</p>
    <p class="mt-2 text-gray-500">{{ fetchError }}</p>
    <el-button type="primary" @click="fetchUserData" class="mt-6 bg-orange-500 hover:bg-orange-600">Try Again</el-button>
  </div>
  
  <!-- 未登录或无数据状态 -->
  <div v-else-if="!editableUser.userId" class="flex min-h-[calc(100vh-10rem)] flex-col items-center justify-center rounded-lg bg-white p-8 shadow-md">
    <el-icon class="text-5xl text-orange-400"><UserFilled /></el-icon>
    <p class="mt-4 text-xl text-gray-500">User not logged in or data unavailable.</p>
    <el-button type="primary" @click="() => router.push({ name: 'Login' })" class="mt-6 bg-orange-500 hover:bg-orange-600">Go to Login</el-button>
  </div>

  <!-- 用户信息表单 -->
  <div v-else class="bg-white p-8 rounded-xl shadow-lg">
    <h2 class="text-3xl font-semibold mb-8 text-gray-700 border-b pb-4">User Information</h2>
    <el-form
        :model="editableUser"
        :rules="userFormRules"
        ref="editFormRef"
        label-position="top"
        class="grid grid-cols-1 md:grid-cols-3 gap-x-8"
    >
      <div class="md:col-span-1 mb-6 md:mb-0 flex flex-col items-center">
        <h3 class="text-lg font-medium text-gray-600 mb-4 self-start">Profile Picture</h3>
        <el-avatar :size="120" :src="currentAvatarDisplayPath" class="mb-4 shadow-md" />
        <p class="text-sm text-gray-500 mb-3">Select a new avatar:</p>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 w-full max-w-xs">
          <img
            v-for="avatar in predefinedAvatars"
            :key="avatar.id"
            :src="avatar.path"
            :alt="'Avatar ' + avatar.id"
            @click="selectAvatar(avatar.id)"
            :class="[
              'w-16 h-16 rounded-full object-cover cursor-pointer hover:ring-2 hover:ring-orange-300 transition-all',
              editableUser.userPicture === avatar.id ? 'ring-2 ring-orange-500 shadow-lg' : 'opacity-70 hover:opacity-100'
            ]"
          />
        </div>
      </div>

      <div class="md:col-span-2">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-6">
            <el-form-item label="Username" prop="userName">
                <el-input v-model="editableUser.userName" disabled placeholder="Username" size="large"/>
            </el-form-item>
            <el-form-item label="Phone Number" prop="phoneNumber">
              <el-input v-model="editableUser.phoneNumber" placeholder="Enter your phone number" size="large"/>
            </el-form-item>
        </div>
          <el-form-item label="Email" prop="email">
            <el-input v-model="editableUser.email" placeholder="Enter your email (optional)" size="large"/>
        </el-form-item>
        <el-form-item label="Gender" prop="sex">
          <el-radio-group v-model="editableUser.sex" size="large">
            <el-radio-button :label="0">Male</el-radio-button>
            <el-radio-button :label="1">Female</el-radio-button>
            <el-radio-button :label="2">Prefer not to say</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Birthday" prop="birthday">
          <el-date-picker
            v-model="editableUser.birthday"
            type="date"
            placeholder="Select your birth date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            size="large"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="Signature" prop="autograph">
          <el-input
            type="textarea"
            :rows="3"
            v-model="editableUser.autograph"
            placeholder="Tell us something about yourself"
            size="large"
          />
        </el-form-item>
        <el-form-item class="mt-8">
          <el-button
            type="primary"
            @click="saveUserChanges"
            :loading="isSaving"
            size="large"
            class="w-full sm:w-auto bg-orange-500 hover:bg-orange-600 border-orange-500 hover:border-orange-600"
          >
            {{ isSaving ? 'Saving...' : 'Save Changes' }}
          </el-button>
          <el-button @click="resetForm" size="large" class="w-full sm:w-auto mt-2 sm:mt-0 sm:ml-4">Reset</el-button>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from 'element-plus';
import { Loading, CircleCloseFilled, UserFilled } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/authStore';
import axios from 'axios';

const router = useRouter();
const authStore = useAuthStore();

const API_BASE_URL = "http://localhost:9231";

const isLoadingPage = ref(true);
const isSaving = ref(false);
const fetchError = ref(null);
const editFormRef = ref(null);

const editableUser = reactive({
  userId: null,
  userName: "",
  email: "",
  phoneNumber: "",
  sex: 2,
  birthday: "",
  autograph: "",
  userPicture: "0",
  password: "",
  salt: "",
  roleId: null,
});
let originalUser = {};

const predefinedAvatars = ref([
  { id: "0", path: "/avatars/avatar-0.png", name: "Default" },
  { id: "1", path: "/avatars/avatar-1.png", name: "Avatar 1" },
  { id: "2", path: "/avatars/avatar-2.png", name: "Avatar 2" },
  { id: "3", path: "/avatars/avatar-3.png", name: "Avatar 3" },
]);
const defaultAvatarPath = "/avatars/avatar-default.png";

const currentAvatarDisplayPath = computed(() => {
  const selected = predefinedAvatars.value.find(avatar => avatar.id === editableUser.userPicture);
  return selected ? selected.path : defaultAvatarPath;
});

const userFormRules = {
  phoneNumber: [
    { required: true, message: "Phone number is required", trigger: "blur" },
    { pattern: /^[0-9]{8,15}$/, message: 'Phone number must be 8-15 digits.', trigger: "blur" }
  ],
  sex: [{ required: true, message: "Gender selection is required", trigger: "change" }],
  email: [
    { type: 'email', message: 'Please input a valid email address', trigger: ['blur', 'change'] }
  ]
};

function selectAvatar(avatarId) {
  editableUser.userPicture = avatarId;
}

async function fetchUserData() {
  const userIdToFetch = authStore.currentUserId;
  if (!authStore.isAuthenticated || !userIdToFetch) {
    fetchError.value = "User is not authenticated. Please log in.";
    isLoadingPage.value = false;
    return;
  }

  isLoadingPage.value = true;
  fetchError.value = null;
  try {
    const response = await axios.get(`${API_BASE_URL}/sysUser/${userIdToFetch}`);
    if (response.data && response.data.code === 200 && response.data.data) {
      const userDataFromApi = response.data.data;
      
      Object.assign(editableUser, {
        userId: userDataFromApi.userId,
        userName: userDataFromApi.userName,
        email: userDataFromApi.email || '',
        phoneNumber: userDataFromApi.phoneNumber || '',
        sex: Number.isInteger(userDataFromApi.sex) ? userDataFromApi.sex : 2,
        birthday: userDataFromApi.birthday || "",
        autograph: userDataFromApi.autograph || '',
        userPicture: userDataFromApi.userPicture || "0",
        password: userDataFromApi.password || "",
        salt: userDataFromApi.salt || "",
        roleId: userDataFromApi.roleId || null,
      });

      originalUser = JSON.parse(JSON.stringify(editableUser));
    } else {
      throw new Error(response.data?.msg || "Failed to fetch user data.");
    }
  } catch (error) {
    fetchError.value = error.response?.data?.msg || error.message || "An unknown error occurred.";
    ElMessage.error(`Error loading user data: ${fetchError.value}`);
  } finally {
    isLoadingPage.value = false;
  }
}

async function saveUserChanges() {
  if (!editFormRef.value) return;
  
  try {
    const valid = await editFormRef.value.validate();
    if (valid) {
      isSaving.value = true;
      const dataToSave = { ...editableUser, birthday: editableUser.birthday === "" ? null : editableUser.birthday };
      const response = await axios.put(`${API_BASE_URL}/sysUser`, dataToSave);
      
      if (response.data && response.data.code === 200) {
        ElMessage.success("User information saved successfully!");
        originalUser = JSON.parse(JSON.stringify(editableUser));
        
        // Update Pinia store
        const userForStoreUpdate = { ...authStore.currentUser, ...editableUser };
        authStore.loginSuccess(userForStoreUpdate, authStore.getToken);
      } else {
        throw new Error(response.data?.msg || "Failed to save data.");
      }
    } else {
      ElMessage.warning('Please check the form for errors.');
    }
  } catch (error) {
    ElMessage.error(`Failed to save: ${error.message}`);
  } finally {
    isSaving.value = false;
  }
}

function resetForm() {
  if (editFormRef.value) {
    editFormRef.value.resetFields();
  }
  Object.assign(editableUser, JSON.parse(JSON.stringify(originalUser)));
  ElMessage.info('Form has been reset to original values.');
}

onMounted(() => {
  fetchUserData();
});
</script>

<style scoped>
.el-form-item__label {
  font-weight: 500;
  color: #4A5568;
  padding-bottom: 0.25rem !important;
}
.el-radio-button:first-child .el-radio-button__inner {
    border-left: var(--el-border);
    border-radius: var(--el-border-radius-base) 0 0 var(--el-border-radius-base);
}
.el-radio-button:last-child .el-radio-button__inner {
    border-radius: 0 var(--el-border-radius-base) var(--el-border-radius-base) 0;
}
</style>