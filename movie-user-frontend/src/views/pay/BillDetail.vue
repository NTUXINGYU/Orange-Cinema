<template>
  <div class="bg-gray-100 min-h-screen p-4">
    <el-steps :active="activeStep" align-center finish-status="success" class="mb-8">
      <el-step title="Select Showtime"></el-step>
      <el-step title="Choose Seats"></el-step>
      <el-step title="Pay Within 15 Minutes" :status="stepStatus.pay"></el-step>
      <el-step title="Collect Tickets & Enjoy" :status="stepStatus.enjoy"></el-step>
    </el-steps>

    <div v-if="isLoading" class="text-center p-20">
      <el-icon class="is-loading text-4xl text-orange-500 mb-4"><Loading /></el-icon>
      <p class="text-gray-600">Loading order details...</p>
    </div>
    <div v-else-if="fetchError" class="text-center p-20 bg-red-50 rounded-lg">
      <el-icon class="text-6xl text-red-500"><CircleCloseFilled /></el-icon>
      <p class="mt-4 text-xl font-semibold text-red-600">Oops! Failed to load order.</p>
      <p class="mt-2 text-gray-500">{{ fetchError }}</p>
    </div>

    <div v-else-if="billInfo && sessionInfo" class="max-w-4xl mx-auto">
      <div class="status-banner mb-8 p-6 rounded-lg shadow-md flex items-center" :class="statusInfo.bannerClass">
        <el-icon class="text-4xl mr-4"><component :is="statusInfo.icon" /></el-icon>
        <div>
          <div class="font-bold text-xl">{{ statusInfo.title }}</div>
          <p class="text-sm">{{ statusInfo.description }}</p>
        </div>
      </div>
      
      <div class="flex flex-col md:flex-row gap-8">
        
        <div class="w-full md:w-2/3 bg-white p-6 rounded-lg shadow-md self-start">
            <div class="border-b pb-4 mb-4">
                <h3 class="text-lg font-semibold text-gray-800">Order #{{ billInfo.billId }}</h3>
                <p class="text-sm text-gray-500">Placed on: {{ formatDateTime(billInfo.createTime) }}</p>
            </div>
            <div class="text-sm text-gray-600 space-y-2">
                <p><strong>Showtime:</strong> {{ sessionInfo.sessionDate }} {{ sessionInfo.playTime }}</p>
                <p><strong>Hall:</strong> {{ sessionInfo.sysHall?.hallName }} ({{ sessionInfo.sysHall?.hallCategory }})</p>
                <p><strong>Seats:</strong></p>
                <div class="flex flex-wrap gap-2">
                    <span v-for="seat in parsedSeats" :key="seat" class="ticket">{{ seat }}</span>
                </div>
            </div>
        </div>
        <div class="w-full md:w-1/3 bg-white p-6 rounded-lg shadow-md self-start sticky top-28">
          <div class="flex mb-4">
              <img :src="getTmdbImageUrl(sessionInfo.sysMovie?.moviePoster)" class="w-24 h-36 object-cover rounded-md shadow-sm flex-shrink-0" />
              <div class="ml-4 flex flex-col justify-start">
                  <h3 class="text-xl font-bold">{{ sessionInfo.sysMovie?.movieName }}</h3>
                  <p class="text-sm text-gray-500 mt-1">{{ sessionInfo.sysMovie?.movieLength }} min</p>
              </div>
          </div>
          
          <div class="border-t pt-4">
              <div class="flex justify-between items-center mb-4">
                  <span class="text-gray-600 font-semibold">Total Price:</span>
                  <span class="text-2xl font-bold text-orange-600">¥{{ billInfo.price.toFixed(2) }}</span>
              </div>
              
              <div class="space-y-3">
                  <el-button v-if="canBePaid" type="danger" @click="payForOrder" :loading="isPaying" round size="large" class="w-full">Pay Now</el-button>
                  <el-button v-if="canBeCancelled" @click="cancelOrder" :loading="isCancelling" round size="large" class="w-full !ml-0">Cancel Order</el-button>
                  
                  <el-button v-if="isOnlineViewingAvailable" type="success" @click="startOnlineViewing" :loading="isStartingViewing" round size="large" class="w-full !ml-0">
                      <el-icon class="mr-2"><VideoCameraFilled /></el-icon>
                      Watch Online Now
                  </el-button>
              </div>

              <div v-if="billInfo.payState && sessionInfo.sysMovie?.onlineUrl" class="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg text-center text-sm">
                  <p class="text-green-800">
                    Online viewing is available for this movie. The button will be enabled during showtime.
                  </p>
              </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted, defineProps } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';
import { Loading, CircleCloseFilled, Clock, SuccessFilled, WarningFilled, VideoCameraFilled } from '@element-plus/icons-vue';
import successSound from '@/assets/payment-success.mp3';

const props = defineProps({ billId: { type: [String, Number], required: true } });
const router = useRouter();
const authStore = useAuthStore();
const API_BASE_URL = "http://localhost:9231";
const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

const billInfo = ref(null);
const sessionInfo = ref(null);
const isLoading = ref(true);
const fetchError = ref(null);
const isPaying = ref(false);
const isCancelling = ref(false);
const timeLeft = ref(0);
let timer = null;

const isStartingViewing = ref(false);
const NGINX_VIDEO_SERVER_URL = "http://localhost:80";

const startOnlineViewing = async () => {
    isStartingViewing.value = true;
    try {
        const token = authStore.token;
        if (!token) {
            throw new Error("You are not logged in. Please log in to watch the movie.");
        }

        const { data: response } = await axios.get(`${API_BASE_URL}/viewing/start`, {
            params: { billId: props.billId },
            headers: {
                'Token': token 
            }
        });

        if (response.code === 200 && response.data) {
            const fullStreamUrl = `${NGINX_VIDEO_SERVER_URL}${response.data.streamUrl}`;
            const syncTime = response.data.syncTime;

            ElMessage.success('Authorization successful! Entering the virtual theater...');

            router.push({
                name: 'OnlinePlayer',
                query: {
                    streamUrl: encodeURIComponent(fullStreamUrl),
                    syncTime: syncTime
                }
            });
        } else {
            throw new Error(response.msg || 'Failed to get viewing authorization.');
        }
    } catch (error) {
        const errorMessage = error.response ? error.response.data.msg : error.message;
        ElMessage.error(errorMessage || 'An error occurred while trying to start viewing.');
    } finally {
        isStartingViewing.value = false;
    }
};

const isOnlineViewingAvailable = computed(() => {
    return billInfo.value?.payState && sessionInfo.value?.sysMovie?.onlineUrl;
});


const fetchOrderAndSessionDetails = async () => {
  isLoading.value = true;
  fetchError.value = null;
  billInfo.value = null;
  sessionInfo.value = null;
  try {
    const { data: billResponse } = await axios.get(`${API_BASE_URL}/sysBill/${props.billId}`);
    if (billResponse.code !== 200 || !billResponse.data) { throw new Error(billResponse.msg || 'Failed to retrieve order info.'); }
    if (authStore.currentUserId !== billResponse.data.userId) { throw new Error("You are not authorized to view this order."); }
    billInfo.value = billResponse.data;
    const sessionId = billInfo.value.sessionId;
    if (!sessionId) { throw new Error("Session information is missing from the order."); }
    const { data: sessionResponse } = await axios.get(`${API_BASE_URL}/sysSession/find/${sessionId}`);
    if (sessionResponse.code !== 200 || !sessionResponse.data) { throw new Error(sessionResponse.msg || 'Failed to retrieve showtime details.'); }
    sessionInfo.value = sessionResponse.data;
    startCountdown();
  } catch (error) {
    fetchError.value = error.message;
    ElMessage.error(error.message);
  } finally {
    isLoading.value = false;
  }
};

const startCountdown = () => {
  if (!billInfo.value || billInfo.value.payState || billInfo.value.cancelState) return;
  if (timer) clearInterval(timer);
  const deadline = new Date(billInfo.value.deadline).getTime();
  timeLeft.value = Math.round(Math.max(0, (deadline - Date.now()) / 1000));
  timer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      clearInterval(timer);
      if(!billInfo.value.payState && !billInfo.value.cancelState) { fetchOrderAndSessionDetails(); }
    }
  }, 1000);
};

const payForOrder = async () => {
  try {
    await ElMessageBox.confirm(
      `<div class="text-center"><p class="mb-4">Please scan the QR code to pay</p><img src="/src/assets/qr-code-placeholder.png" alt="QR Code" class="w-48 h-48 mx-auto" /><p class="text-2xl font-bold mt-4">¥ ${billInfo.value.price.toFixed(2)}</p></div>`,
      'Confirm Payment', { confirmButtonText: 'I have paid', cancelButtonText: 'Cancel Payment', dangerouslyUseHTMLString: true, center: true }
    );
    isPaying.value = true;
    ElMessage({ message: 'Confirming payment, please wait...', type: 'info', duration: 2500 });
    await new Promise(resolve => setTimeout(resolve, 2000));
    const { data: response } = await axios.post(`${API_BASE_URL}/sysBill/pay/${billInfo.value.billId}`);
    if (response.code === 200) {
      new Audio(successSound).play();
      ElMessage.success("Payment successful!");
      await fetchOrderAndSessionDetails();
    } else {
      throw new Error(response.msg || 'Payment confirmation failed.');
    }
  } catch (error) {
    if (error !== 'cancel') { ElMessage.error(error.message || "Payment was cancelled or failed."); }
    else { ElMessage.info("Payment cancelled."); }
  } finally {
    isPaying.value = false;
  }
};

const cancelOrder = async () => {
    try {
        await ElMessageBox.confirm('Are you sure you want to cancel this order?', 'Confirm Cancellation', { confirmButtonText: 'Yes, Cancel', cancelButtonText: 'No', type: 'warning' });
        isCancelling.value = true;
        const { data: response } = await axios.post(`${API_BASE_URL}/sysBill/cancel/${billInfo.value.billId}`);
        if (response.code === 200) {
            ElMessage.info("Order has been cancelled.");
            await fetchOrderAndSessionDetails();
        } else {
            throw new Error(response.msg || 'Cancellation failed.');
        }
    } catch (error) {
        if (error !== 'cancel') { ElMessage.error(error.message || 'An unknown error occurred.'); }
    } finally {
        isCancelling.value = false;
    }
};

const parsedSeats = computed(() => {
  if (!billInfo.value?.seats) return [];
  return billInfo.value.seats.split(',').map(s => { const [row, col] = s.split('-'); return `${row} Row ${col} Seat`; });
});

const isExpired = computed(() => timeLeft.value <= 0 && !billInfo.value?.payState && !billInfo.value?.cancelState);
const canBePaid = computed(() => billInfo.value && !billInfo.value.payState && !billInfo.value.cancelState && !isExpired.value);
const canBeCancelled = computed(() => canBePaid.value);


const activeStep = computed(() => {
    if (!billInfo.value) return 2;
    if (billInfo.value.payState) return 4;
    return 2;
});

const stepStatus = computed(() => {
    const defaultStatus = { pay: 'process', enjoy: 'wait' };
    if (!billInfo.value) return defaultStatus;

    if (billInfo.value.payState) {
        return { pay: 'success', enjoy: 'success' }; 
    }
    if (billInfo.value.cancelState || isExpired.value) {
        return { pay: 'error', enjoy: 'wait' };
    }
    return defaultStatus;
});


const statusInfo = computed(() => {
    if (!billInfo.value) return {};
    if (billInfo.value.payState) return { icon: SuccessFilled, bannerClass: 'bg-green-100 text-green-800', title: 'Payment Successful', description: 'Enjoy the movie!' };
    if (billInfo.value.cancelState) return { icon: WarningFilled, bannerClass: 'bg-red-100 text-red-800', title: 'Order Cancelled', 'description': 'This order has been cancelled.' };
    if (isExpired.value) return { icon: WarningFilled, bannerClass: 'bg-gray-200 text-gray-700', title: 'Order Expired', description: 'This order expired due to non-payment.' };
    const minutes = Math.floor(timeLeft.value / 60);
    const seconds = timeLeft.value % 60;
    return { icon: Clock, bannerClass: 'bg-yellow-100 text-yellow-800', title: 'Pending Payment', description: `Please complete payment within ${minutes}m ${String(seconds).padStart(2, '0')}s.` };
});

const getTmdbImageUrl = (posterPath) => {
  if (!posterPath) return 'https://via.placeholder.com/100x150.png?text=No+Poster';
  return `${TMDB_IMAGE_BASE_URL}${posterPath}`;
};

const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '';
    return new Date(dateTimeString).toLocaleString('en-US', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};

onMounted(fetchOrderAndSessionDetails);
onUnmounted(() => { if (timer) clearInterval(timer); });
</script>

<style scoped>
.ticket {
  display: inline-block;
  background-color: #f3f4f6;
  color: #374151;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 500;
  border: 1px solid #e5e7eb;
}
.el-button+.el-button {
    margin-left: 0;
}
</style>