<template>
  <div class="bg-gray-100 min-h-screen p-4">
    <!-- 步骤条 -->
    <el-steps :active="1" align-center class="mb-8">
      <el-step title="Select Showtime"></el-step>
      <el-step title="Choose Seats"></el-step>
      <el-step title="Pay Within 15 Minutes"></el-step>
      <el-step title="Collect Tickets & Enjoy"></el-step>
    </el-steps>
    
    <!-- 加载与错误状态 -->
    <div v-if="isLoading" class="text-center p-20">
      <el-icon class="is-loading text-4xl text-orange-500 mb-4"><Loading /></el-icon>
      <p class="text-gray-600">Loading session details and seat map...</p>
    </div>
    <div v-else-if="fetchError" class="text-center p-20 bg-red-50 rounded-lg">
      <el-icon class="text-6xl text-red-500"><CircleCloseFilled /></el-icon>
      <p class="mt-4 text-xl font-semibold text-red-600">Oops! Failed to load session info.</p>
      <p class="mt-2 text-gray-500">{{ fetchError }}</p>
    </div>

    <!-- 主内容区域 -->
    <div v-else-if="session" class="flex flex-col md:flex-row gap-8 max-w-7xl mx-auto">
      <!-- 左侧: 选座区域 -->
      <div class="w-full md:w-2/3 bg-white p-6 rounded-lg shadow-md">
        <!-- 座位图例 -->
        <div class="seat-legend flex items-center justify-center space-x-6 mb-6 p-2 bg-gray-50 rounded">
          <div class="flex items-center"><span class="seat-example bg-white border border-gray-300"></span><span class="ml-2 text-sm">Available</span></div>
          <div class="flex items-center"><span class="seat-example bg-gray-400"></span><span class="ml-2 text-sm">Sold</span></div>
          <div class="flex items-center"><span class="seat-example bg-orange-500"></span><span class="ml-2 text-sm">Selected</span></div>
        </div>

        <!-- 银幕 -->
        <div class="screen-container mb-8">
          <div class="screen-bar"></div>
          <div class="screen-text">SCREEN</div>
        </div>

        <!-- 座位图 -->
        <div class="seat-map-container overflow-x-auto">
          <div class="flex flex-col items-center space-y-2">
            <div
              class="flex items-center"
              v-for="(row, rowIndex) in seatLayout"
              :key="rowIndex"
            >
              <div class="row-label w-8 text-center text-gray-500 font-mono">{{ getRowLabel(rowIndex) }}</div>
              <div class="flex space-x-2">
                <div
                  v-for="(seat, colIndex) in row"
                  :key="colIndex"
                  @click="toggleSeat(rowIndex, colIndex)"
                  class="seat"
                  :class="{
                    'available': seat === 0,
                    'selected': seat === 2,
                    'sold': seat === 3                    
                  }"
                ></div>
              </div>
              <div class="row-label w-8 text-center text-gray-500 font-mono">{{ getRowLabel(rowIndex) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="w-full md:w-1/3 bg-white p-6 rounded-lg shadow-md self-start sticky top-28">
        <div class="flex mb-4">
          <img :src="getTmdbImageUrl(session.sysMovie.moviePoster)" class="w-24 h-36 object-cover rounded-md shadow-sm" />
          <div class="ml-4 flex flex-col justify-between">
            <div>
              <h3 class="text-xl font-bold">{{ session.sysMovie.movieName }}</h3>
              <div v-if="session.sysMovie.onlineUrl" class="mt-2">
                <el-tag type="success" effect="dark">
                  <el-icon class="mr-1"><VideoCameraFilled /></el-icon>
                  Online Sync Playback Available
                </el-tag>
              </div>
            </div>
            <p class="text-sm text-gray-500">{{ session.sysMovie.movieLength }} min</p>
          </div>
        </div>

        <div class="space-y-2 border-t pt-4 text-sm text-gray-700">
          <p><strong>Hall:</strong> {{ session.sysHall.hallName }}</p>
          <p><strong>Showtime:</strong> {{ session.sessionDate }} {{ session.playTime }}</p>
          <p><strong>Price:</strong> <span class="font-semibold text-orange-600">¥{{ NowPrice.toFixed(2) }}</span> / ticket</p>
          
          <div class="pt-2">
            <strong>Seats:</strong>
            <div v-if="selectedSeatCoordinates.length > 0" class="flex flex-wrap gap-2 mt-1">
              <span v-for="seat in selectedSeatCoordinates" :key="`${seat.row}-${seat.col}`" class="ticket">
                {{ getRowLabel(seat.row) }} Row {{ seat.col + 1 }} Seat
              </span>
            </div>
            <span v-else class="text-gray-400 italic ml-2">Please select a seat</span>
          </div>
        </div>

        <div class="border-t mt-4 pt-4">
            <div class="flex justify-between items-center text-xl font-bold">
                <span>Total:</span>
                <span class="text-orange-600">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <p v-if="selectedSeatCoordinates.length > 4" class="text-red-500 text-xs mt-2 text-center">You can select up to 4 seats at a time.</p>
        </div>

        <div class="mt-6">
          <el-button
            type="danger" round size="large" class="w-full"
            @click="submitOrder"
            :disabled="isSubmitting || selectedSeatCoordinates.length === 0 || selectedSeatCoordinates.length > 4"
            :loading="isSubmitting"
          >
            {{ isSubmitting ? 'Submitting...' : 'Confirm and Pay' }}
          </el-button>
          <p v-if="session.sysMovie.onlineUrl" class="text-xs text-gray-500 mt-3 text-center">
            <el-icon class="align-middle"><InfoFilled /></el-icon>
            One online viewing link will be provided per order.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';
import { Loading, CircleCloseFilled, VideoCameraFilled, InfoFilled } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const API_BASE_URL = "http://localhost:9231";
const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

const session = ref(null);
const seatLayout = ref([]);
const isLoading = ref(true);
const fetchError = ref(null);
const isSubmitting = ref(false);

const fetchSessionDetails = async () => {
  const sessionId = route.params.sessionId;
  if (!sessionId) {
    fetchError.value = "Session ID is missing.";
    isLoading.value = false;
    return;
  }
  try {
    // 假设你的后端已经配置了 axios 拦截器来自动添加 token
    const { data: response } = await axios.get(`${API_BASE_URL}/sysSession/find/${sessionId}`);
    if (response.code === 200 && response.data) {
      session.value = response.data;
      if (!session.value.sysMovie) {
        session.value.sysMovie = {};
      }
      parseSeatLayout(session.value.sessionSeats);
    } else {
      throw new Error(response.msg || 'Failed to fetch session details.');
    }
  } catch (error) {
    fetchError.value = error.message || 'An unknown network error occurred.';
    ElMessage.error(fetchError.value);
  } finally {
    isLoading.value = false;
  }
};

const parseSeatLayout = (seatJson) => {
  try {
    if (!seatJson) throw new Error("Seat map is not available.");
    const seatData = JSON.parse(seatJson);
    seatLayout.value = Object.entries(seatData)
      .sort(([keyA], [keyB]) => parseInt(keyA) - parseInt(keyB))
      .map(([, value]) => value);
  } catch (e) {
    console.error("Failed to parse seat layout JSON:", e);
    fetchError.value = "Seat map data is corrupted or unavailable.";
  }
};

const toggleSeat = (rowIndex, colIndex) => {
  if (isLoading.value || isSubmitting.value) return;

  const currentStatus = seatLayout.value[rowIndex][colIndex];
  if (currentStatus === 0) { // If seat is available
    if (selectedSeatCoordinates.value.length >= 4) {
      ElMessage.warning("You can select up to 4 seats at a time.");
      return;
    }
    seatLayout.value[rowIndex][colIndex] = 2; // Mark as selected
  } else if (currentStatus === 2) { // If seat is selected
    seatLayout.value[rowIndex][colIndex] = 0; // Mark as available
  }
  // Do nothing for sold (1) or disabled (3) seats
};

const selectedSeatCoordinates = computed(() => {
  const coordinates = [];
  seatLayout.value.forEach((row, rowIndex) => {
    row.forEach((seat, colIndex) => {
      if (seat === 2) { // 2 represents a selected seat
        coordinates.push({ row: rowIndex, col: colIndex });
      }
    });
  });
  return coordinates;
});

const NowPrice = computed(() => {
  if (!session.value) return 0;
  const price = session.value.sessionPrice * (session.value.discountRate || 1);
  return price;
});

const totalPrice = computed(() => {
  if (!session.value) return 0;
  const price = session.value.sessionPrice * (session.value.discountRate || 1);
  return selectedSeatCoordinates.value.length * price;
});

const submitOrder = async () => {
  if (selectedSeatCoordinates.value.length === 0) {
    ElMessage.warning("Please select at least one seat.");
    return;
  }
  
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('You need to be logged in to place an order. Go to login page?', 'Login Required', {
      confirmButtonText: 'Login',
      cancelButtonText: 'Cancel',
      type: 'warning',
    }).then(() => {
      router.push({ name: 'Login', query: { redirect: route.fullPath } });
    }).catch(() => {
      ElMessage.info("Login cancelled.");
    });
    return;
  }
  
  isSubmitting.value = true;
  
  const payload = {
      // Your SysBillVO expects a sysBill object
      sysBill: {
        userId: authStore.currentUserId,
        sessionId: session.value.sessionId,
        seats: selectedSeatCoordinates.value.map(s => `${getRowLabel(s.row)}-${s.col + 1}`).join(','),
        price: totalPrice.value,
        // Other fields like payState, cancelState, etc., will be set by the backend.
      }
  };

  try {
    const { data: response } = await axios.post(`${API_BASE_URL}/sysBill`, payload);

    // [核心修复] 正确处理后端的响应
    if (response.code === 200 && response.data && typeof response.data.billId === 'number') {
      const newBillId = response.data.billId;

      ElMessage.success("Order created successfully! Redirecting to details...");
      
      router.push({ 
        name: 'BillDetail', // Ensure this route name is correct in your router config
        params: { billId: newBillId } 
      });

    } else {
      // Handle business logic errors from the backend (e.g., seat taken)
      throw new Error(response.msg || 'Failed to submit order. Please try again.');
    }
  } catch (error) {
    // Handle network errors or exceptions from the backend
    const errorMessage = error.response?.data?.msg || error.message || 'An unknown error occurred.';
    
    ElMessage.error(errorMessage);
    
    // If the error was about seats being taken, it's good practice to refresh the seat map
    if (errorMessage.toLowerCase().includes("seat")) {
        ElMessage.info("Seat map has been refreshed. Please check seat status.");
        fetchSessionDetails();
    }
  } finally {
    isSubmitting.value = false;
  }
};

const getTmdbImageUrl = (posterPath) => {
  if (!posterPath) return 'https://via.placeholder.com/100x150.png?text=No+Poster';
  return `${TMDB_IMAGE_BASE_URL}${posterPath}`;
};

const getRowLabel = (rowIndex) => {
  // This logic seems to depend on hall settings, assuming it works as intended.
  if (session.value?.sysHall?.rowStart) {
    return String.fromCharCode(session.value.sysHall.rowStart.charCodeAt(0) + rowIndex);
  }
  return rowIndex + 1;
};

const goBack = () => router.go(-1);

onMounted(fetchSessionDetails);
</script>

<style scoped>
.seat-example {
  width: 20px; height: 20px; border-radius: 3px;
}
.screen-container {
  border-bottom: 40px solid #d1d5db; /* bg-gray-300 */
  border-left: 20px solid transparent;
  border-right: 20px solid transparent;
  height: 0;
  width: 80%;
  margin: 0 auto;
  position: relative;
}
.screen-text {
  position: absolute;
  top: 10px;
  width: 100%;
  text-align: center;
  color: #4b5563; /* text-gray-600 */
  font-weight: 600;
  font-size: 0.875rem;
}
.seat {
  width: 24px; height: 24px; margin: 4px;
  border-radius: 4px; transition: all 0.2s;
}
.seat.available {
  background-color: #fff; border: 1px solid #cbd5e1; cursor: pointer;
}
.seat.available:hover {
  background-color: #fef3c7; /* amber-100 */
}
.seat.sold {
  background-color: #9ca3af; /* gray-400 */ cursor: not-allowed;
}
.seat.selected {
  background-color: #f97316; /* orange-500 */ border-color: #ea580c; cursor: pointer;
}
.row-label {
  font-size: 0.75rem;
}
.ticket {
  display: inline-block;
  background-color: #f3f4f6; /* gray-100 */
  color: #374151; /* gray-700 */
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}
</style>