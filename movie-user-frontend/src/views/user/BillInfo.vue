<template>
  <div class="bg-white p-6 md:p-8 rounded-xl shadow-lg">
    <h2 class="text-3xl font-semibold mb-6 text-gray-700 border-b pb-4">My Orders</h2>

    <!-- 1. Loading State (Skeleton Screen) -->
    <el-skeleton v-if="isLoading" :rows="5" animated />

    <!-- 2. Error State -->
    <div v-else-if="fetchError" class="flex flex-col items-center justify-center min-h-[400px] bg-red-50 p-6 rounded-lg">
      <el-icon class="text-5xl text-red-500"><CircleCloseFilled /></el-icon>
      <p class="mt-4 text-xl text-red-600 font-semibold">Failed to load orders</p>
      <p class="mt-2 text-gray-500">{{ fetchError }}</p>
      <el-button type="primary" @click="fetchUserOrders" class="mt-6">Try Again</el-button>
    </div>
    
    <!-- 3. Empty State -->
    <div v-else-if="!orders.length" class="flex flex-col items-center justify-center text-center min-h-[400px] bg-gray-50 rounded-lg p-8">
      <el-icon class="text-6xl text-gray-300 mb-4"><Document /></el-icon>
      <h3 class="text-2xl font-semibold text-gray-800">No Orders Yet</h3>
      <p class="mt-2 max-w-sm text-gray-500">
        You haven't booked any tickets yet. All your future and past orders will be displayed here.
      </p>
      <el-button type="primary" size="large" @click="goToMovies" class="mt-8">
        Browse Movies Now
      </el-button>
    </div>

    <!-- 4. Orders List (When data exists) -->
    <div v-else>
      <el-table :data="orders" style="width: 100%" v-loading="isTableLoading" element-loading-text="Loading...">
        
        <el-table-column label="Movie" min-width="200">
          <template #default="scope">
            <div class="flex flex-col">
              <!-- [最终修复] 使用正确的深层嵌套路径访问数据 -->
              <span class="font-semibold text-gray-800">{{ scope.row.sysSession?.sysMovie?.movieName || 'N/A' }}</span>
              <span class="text-sm text-gray-500 mt-1">Order #{{ scope.row.billId }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Show Time" min-width="180">
            <template #default="scope">
              <div>
                <!-- [最终修复] 使用正确的嵌套路径访问数据 -->
                <span>{{ scope.row.sysSession?.sessionDate }}</span>
                <span class="ml-2 font-semibold">{{ scope.row.sysSession?.playTime }}</span>
              </div>
            </template>
        </el-table-column>

        <el-table-column prop="price" label="Price" width="120" align="center">
          <template #default="scope">
            <span class="font-semibold text-lg">¥{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.payState" type="success" effect="light">Paid</el-tag>
            <el-tag v-else-if="scope.row.cancelState" type="info" effect="light">Canceled</el-tag>
            <el-tag v-else-if="isOrderExpired(scope.row)" type="warning" effect="plain">Expired</el-tag>
            <el-tag v-else type="warning" effect="light">Unpaid</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="Actions" width="150" fixed="right" align="center">
          <template #default="scope">
              <el-button
                size="small"
                type="primary"
                link
                @click="viewOrderDetails(scope.row.billId)"
              >
                View Details
              </el-button>
              <el-popconfirm
                v-if="!scope.row.payState && !scope.row.cancelState && !isOrderExpired(scope.row)"
                title="Are you sure you want to cancel this order?"
                confirm-button-text="Yes, Cancel"
                cancel-button-text="No"
                @confirm="handleCancel(scope.row.billId)"
              >
                <template #reference>
                  <el-button
                    size="small"
                    type="danger"
                    link
                  >
                    Cancel Order
                  </el-button>
                </template>
              </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-6">
        <el-pagination
          v-if="pagination.total > 0"
          background 
          layout="prev, pager, next, total"
          :total="pagination.total"
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import axios from 'axios';
import { ElMessage, ElPopconfirm, ElSkeleton } from 'element-plus';
import { CircleCloseFilled, Document } from '@element-plus/icons-vue';

const API_BASE_URL = "http://localhost:9231";
const authStore = useAuthStore();
const router = useRouter();

const orders = ref([]);
const isLoading = ref(true);
const isTableLoading = ref(false);
const fetchError = ref(null);

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

async function fetchUserOrders() {
  if (!authStore.isAuthenticated || !authStore.currentUserId) {
    fetchError.value = "Please log in to view your orders.";
    isLoading.value = false;
    return;
  }
  
  // For initial load, show skeleton. For page changes, show table loading.
  isTableLoading.value = orders.value.length > 0; 
  fetchError.value = null;

  try {
    const { data: responseData } = await axios.get(`${API_BASE_URL}/sysBill`, {
      params: { 
        userId: authStore.currentUserId, 
        pageNum: pagination.currentPage, 
        pageSize: pagination.pageSize 
      }
    });

    if (responseData && responseData.code === 200) {
      // Correctly parse the data based on the definitive log
      orders.value = responseData.data || [];
      pagination.total = responseData.total || 0;
    } else {
      throw new Error(responseData?.msg || "Failed to fetch orders.");
    }
  } catch (error) {
    const errorMessage = error.response?.data?.msg || error.message || "An unknown error occurred.";
    fetchError.value = errorMessage;
    ElMessage.error(`Error loading orders: ${errorMessage}`);
    orders.value = [];
    pagination.total = 0;
  } finally {
    isLoading.value = false;
    isTableLoading.value = false;
  }
}

async function handleCancel(billId) {
  try {
    const { data: response } = await axios.post(`${API_BASE_URL}/sysBill/cancel/${billId}`);
    if (response.code === 200) {
        ElMessage.success("Order has been cancelled successfully.");
        fetchUserOrders();
    } else {
        throw new Error(response.msg || 'Cancellation failed.');
    }
  } catch (error) {
    ElMessage.error(error.message || 'An unknown error occurred during cancellation.');
  }
}

const isOrderExpired = (order) => {
    if (order.payState || order.cancelState) return false;
    if (!order.deadline) return false;
    return new Date(order.deadline).getTime() < Date.now();
}

function viewOrderDetails(billId) {
    router.push({ name: 'BillDetail', params: { billId: billId } });
}

function handlePageChange(newPage) {
  pagination.currentPage = newPage;
  fetchUserOrders();
}

function goToMovies() {
  router.push({ name: 'Home' }); 
}

onMounted(() => {
  fetchUserOrders();
});
</script>

<style scoped>
.el-table .el-button+.el-button {
  margin-left: 8px;
}
</style>