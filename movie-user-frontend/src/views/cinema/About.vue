<template>
  <div class="bg-gray-100 min-h-screen text-gray-800">
    <div class="container mx-auto px-4 py-12 md:py-20">
      
      <!-- 加载状态 -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center h-96">
        <el-icon class="is-loading text-5xl text-orange-500"><Loading /></el-icon>
        <p class="mt-4 text-xl text-gray-600">Loading Cinema Info...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="fetchError" class="flex flex-col items-center justify-center h-96 bg-red-50 p-8 rounded-lg">
        <el-icon class="text-6xl text-red-500"><CircleCloseFilled /></el-icon>
        <p class="mt-4 text-2xl font-semibold text-red-600">Oops! Something went wrong.</p>
        <p class="mt-2 text-gray-500">{{ fetchError }}</p>
        <el-button type="primary" @click="fetchCinemaInfo" class="mt-6 bg-orange-500 hover:bg-orange-600">Try Again</el-button>
      </div>

      <!-- 主内容区域 -->
      <div v-else class="max-w-5xl mx-auto">
        <!-- 顶部介绍卡片 -->
        <div 
          class="relative bg-white shadow-2xl rounded-2xl p-8 md:p-12 overflow-hidden bg-cover bg-center"
          style="background-image: url('/src/assets/bg.png');"
        >
          <div class="absolute inset-0 bg-white bg-opacity-80 backdrop-blur-sm"></div>
          <div class="relative z-10 text-center">
            <h1 class="text-4xl md:text-5xl font-extrabold text-orange-600 mb-4 tracking-tight">
              {{ cinema.cinemaName || 'Orange Cinema' }}
            </h1>
            <p class="text-lg md:text-xl text-gray-700 max-w-2xl mx-auto">
              Experience the ultimate movie magic with cutting-edge technology and luxurious comfort.
            </p>
          </div>
        </div>

        <!-- 详细信息网格布局 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
          
          <!-- 左侧：地址和联系方式 -->
          <div class="md:col-span-1 bg-white p-8 rounded-2xl shadow-lg flex flex-col space-y-8">
            <div>
              <div class="flex items-center mb-3">
                <el-icon class="text-2xl text-orange-500 mr-3"><Location /></el-icon>
                <h2 class="text-xl font-bold">Our Location</h2>
              </div>
              <p class="text-gray-600 pl-9">{{ cinema.cinemaAddress || 'Address not available' }}</p>
            </div>
            <div>
              <div class="flex items-center mb-3">
                <el-icon class="text-2xl text-orange-500 mr-3"><PhoneFilled /></el-icon>
                <h2 class="text-xl font-bold">Contact Us</h2>
              </div>
              <p class="text-gray-600 pl-9">{{ cinema.cinemaPhone || 'Phone not available' }}</p>
            </div>
            <div>
              <div class="flex items-center mb-3">
                <el-icon class="text-2xl text-orange-500 mr-3"><Clock /></el-icon>
                <h2 class="text-xl font-bold">Business Hours</h2>
              </div>
              <p v-if="cinema.workStartTime && cinema.workEndTime" class="text-gray-600 pl-9">
                {{ cinema.workStartTime }} - {{ cinema.workEndTime }}
              </p>
              <p v-else class="text-gray-500 pl-9 italic">Hours not specified</p>
            </div>
          </div>

          <!-- 右侧：特色影厅 -->
          <div class="md:col-span-2 bg-white p-8 rounded-2xl shadow-lg">
            <div class="flex items-center mb-6">
              <el-icon class="text-3xl text-orange-500 mr-4"><Film /></el-icon>
              <h2 class="text-2xl font-bold">Our Special Theaters</h2>
            </div>
            <div v-if="hallTypes.length > 0" class="flex flex-wrap gap-4">
              <el-tag 
                v-for="hall in hallTypes" 
                :key="hall"
                size="large"
                effect="light"
                class="transition-transform transform hover:scale-105"
              >
                {{ hall }}
              </el-tag>
            </div>
            <div v-else class="text-gray-500 italic">
              Information about special theaters is coming soon.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { Location, PhoneFilled, Clock, Film, Loading, CircleCloseFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const API_BASE_URL = "http://localhost:9231";

const cinema = ref({});
const isLoading = ref(true);
const fetchError = ref(null);

const hallTypes = computed(() => {
  if (!cinema.value.hallCategoryList) {
    return [];
  }
  try {
    const parsed = JSON.parse(cinema.value.hallCategoryList);
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    console.error("Failed to parse hallCategoryList:", e);
    return [];
  }
});

const fetchCinemaInfo = async () => {
  isLoading.value = true;
  fetchError.value = null;
  try {

    const { data: response } = await axios.get(`${API_BASE_URL}/sysCinema`); 

    if (response.code === 200 && response.data) {
      cinema.value = response.data;
    } else {
      throw new Error(response.msg || 'Failed to fetch cinema information.');
    }
  } catch (error) {
    if (error.message.includes('Network Error')) {
      fetchError.value = 'Network Error: Could not connect to the server. Please check if the backend is running.';
    } else {
      fetchError.value = error.message || 'An unknown error occurred.';
    }
    ElMessage.error(fetchError.value);
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchCinemaInfo);
</script>

<style scoped>
.el-tag {
  padding: 16px 20px;
  font-size: 1rem;
  border-radius: 8px;
  border: 1px solid #fde6d3;
  background-color: #fef8f2;
  color: #c4763b;
}
</style>