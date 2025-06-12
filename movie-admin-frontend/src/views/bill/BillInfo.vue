<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Order Management</el-breadcrumb-item>
        <el-breadcrumb-item>Order Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="box-card">
      <el-row :gutter="20" class="filter-bar">
        <el-col :xs="24" :sm="8" :md="6">
          <el-input v-model="searchFilters.userName" placeholder="Enter username" clearable @clear="fetchBillList" @keyup.enter="fetchBillList"/>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6">
          <el-select v-model="searchFilters.payState" placeholder="Select order status" clearable style="width:100%;" @change="fetchBillList">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6">
          <el-date-picker
            v-model="searchFilters.createTime"
            type="date"
            placeholder="Select order date"
            value-format="YYYY-MM-DD"
            clearable
            style="width:100%;"
            @change="fetchBillList"
          ></el-date-picker>
        </el-col>
         <el-col :xs="24" :sm="24" :md="6" class="search-button-col">
          <el-button :icon="Search" @click="fetchBillList" type="primary">Search</el-button>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="action-buttons-row">
        <el-col :span="24">
          <el-button type="danger" @click="confirmBatchDeleteBills" size="large" :disabled="selectedBills.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>

      <el-table :data="billList" style="width: 100%" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoadingTable">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="billId" label="#" width="60" sortable></el-table-column>
        <el-table-column prop="sysUser.userName" label="Username" width="120"></el-table-column>
        <el-table-column prop="sysSession.sysMovie.movieName" label="Movie Title" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sysSession.sysHall.hallName" label="Hall Name" width="150"></el-table-column>
        <el-table-column prop="sysSession.sessionDate" label="Session Date" width="120"></el-table-column>
        <el-table-column label="Session Time" width="150">
          <template #default="scope"> {{scope.row.sysSession?.playTime}}-{{scope.row.sysSession?.endTime}} </template>
        </el-table-column>
        <el-table-column prop="seats" label="Seats" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="Order Time" width="170" sortable></el-table-column>
        <el-table-column label="Order Status" width="130" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.payState === true" type="success">Completed</el-tag>
            <el-tag v-else-if="scope.row.cancelState === true && scope.row.cancelTime === null" type="warning">Cancelled (Timeout)</el-tag>
            <el-tag v-else-if="scope.row.cancelState === true && scope.row.cancelTime !== null" type="danger">Cancelled (User)</el-tag>
            <el-tag v-else type="info">Unpaid</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Modify Order Status" placement="top" :enterable="false" :open-delay="300">
              <el-button
                type="primary"
                @click="openEditStatusDialog(scope.row)"
                size="small"
                :icon="EditPen"
                :disabled="scope.row.payState === true || (scope.row.cancelState === true && scope.row.cancelTime !== null)"
              ></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete Order" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="confirmDeleteBill(scope.row.billId)" size="small" :icon="Delete"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination-container"
        @size-change="handlePageSizeChange"
        @current-change="handlePageNumChange"
        :current-page="queryParameters.pageNum"
        :page-sizes="[5, 7, 10, 15]"
        :page-size="queryParameters.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalBillCount"
        background
      >
      </el-pagination>
    </el-card>

    <el-dialog title="Modify Order Status" v-model="isEditStatusDialogVisible" width="45%" @close="resetEditStatusFormDialog" top="10vh">
      <el-form :model="currentBillForm" ref="editStatusFormRef" label-width="120px">
        <el-form-item label="Order ID:">
          <span>{{ currentBillForm.billId }}</span>
        </el-form-item>
        <el-form-item label="User:">
          <span>{{ currentBillForm.sysUser?.userName }}</span>
        </el-form-item>
        <el-form-item label="Movie:">
           <span>{{ currentBillForm.sysSession?.sysMovie?.movieName }}</span>
        </el-form-item>
         <el-form-item label="Current Status:">
            <el-tag v-if="originalBillStatus.isPaid" type="success" effect="dark">Completed</el-tag>
            <el-tag v-else-if="originalBillStatus.isCancelled && !originalBillStatus.userCancelled" type="warning" effect="dark">Cancelled (Timeout)</el-tag>
            <el-tag v-else-if="originalBillStatus.isCancelled && originalBillStatus.userCancelled" type="danger" effect="dark">Cancelled (User)</el-tag>
            <el-tag v-else type="info" effect="dark">Unpaid</el-tag>
        </el-form-item>

        <el-form-item label="Set as Paid" prop="payState" v-if="!originalBillStatus.isPaid && !originalBillStatus.isCancelled">
          <el-switch v-model="currentBillForm.payState" />
        </el-form-item>
        <el-form-item label="Set as Cancelled" prop="cancelState" v-if="!originalBillStatus.isPaid && !originalBillStatus.isCancelled">
          <el-switch v-model="currentBillForm.cancelState" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isEditStatusDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitBillStatusChange" :loading="isSubmittingForm">
            <i class="iconfont icon-sure-button button-icon"></i> Confirm Changes
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { ArrowRight, EditPen, Delete, Search, View as ViewIcon } from '@element-plus/icons-vue'; // Renamed View

const isLoadingTable = ref(false);
const isSubmittingForm = ref(false);

const searchFilters = reactive({ userName: '', payState: null, createTime: '' });
const orderStatusOptions = ref([
  { value: false, label: 'Unpaid' },   // Assuming payState: false means unpaid if not cancelled
  { value: true, label: 'Completed' }, // Assuming payState: true means completed
  // You might need more complex filtering based on cancelState as well
  // Or a specific "Cancelled" status if your backend query supports it
]);

const queryParameters = reactive({ queryByUserName: '', payState: null, createTime: '', pageNum: 1, pageSize: 7 });
const billList = ref([]);
const totalBillCount = ref(0);
const selectedBills = ref([]);

const isEditStatusDialogVisible = ref(false);
const initialBillFormState = {
  billId: null,
  sysUser: { userName: '' },
  sysSession: { sysMovie: { movieName: '' }, sysHall: { hallName: '' }, sessionDate: '', playTime: '', endTime: '' },
  seats: '',
  createTime: '',
  payState: false,
  cancelState: false,
  cancelTime: null,
  sessionId: null // Need sessionId for updating seats on cancel
};
const currentBillForm = reactive({ ...initialBillFormState });
const originalBillStatus = reactive({ isPaid: false, isCancelled: false, userCancelled: false });
const editStatusFormRef = ref(null);


const fetchBillList = async () => {
  isLoadingTable.value = true;
  queryParameters.queryByUserName = searchFilters.userName.trim();
  queryParameters.payState = searchFilters.payState; // Will be true, false, or null
  queryParameters.createTime = searchFilters.createTime || '';
  try {
    const { data: res } = await axios.get('sysBill', { params: queryParameters });
    if (res.code === 200) { billList.value = res.data; totalBillCount.value = res.total; }
    else { ElMessage.error(res.msg || 'Failed to fetch bills.'); }
  } catch (error) { ElMessage.error('Error fetching bills.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};


onMounted(fetchBillList);

const handlePageSizeChange = (newSize) => { queryParameters.pageSize = newSize; fetchBillList(); };
const handlePageNumChange = (newPage) => { queryParameters.pageNum = newPage; fetchBillList(); };
const handleTableSelectionChange = (selection) => { selectedBills.value = selection; };


const openEditStatusDialog = async (billFullObject) => {
  // It's better to fetch the latest bill data in case it changed
  isSubmittingForm.value = true;
  try {
    const { data: res } = await axios.get(`sysBill/${billFullObject.billId}`);
    if (res.code === 200 && res.data) {
      Object.assign(currentBillForm, res.data);
      // Store original status for UI logic
      originalBillStatus.isPaid = !!res.data.payState;
      originalBillStatus.isCancelled = !!res.data.cancelState;
      originalBillStatus.userCancelled = !!(res.data.cancelState && res.data.cancelTime);

      // Reset switches for new interaction, but keep original data for display
      currentBillForm.payState = res.data.payState;
      currentBillForm.cancelState = res.data.cancelState;

      isEditStatusDialogVisible.value = true;
    } else {
      ElMessage.error(res.msg || 'Failed to fetch bill details.');
    }
  } catch (error) {
    ElMessage.error('Error fetching bill details.');
  } finally {
    isSubmittingForm.value = false;
  }
};

const resetEditStatusFormDialog = () => {
  if (editStatusFormRef.value) editStatusFormRef.value.resetFields(); // Might not be needed if not a deep form
  Object.assign(currentBillForm, initialBillFormState);
  Object.assign(originalBillStatus, { isPaid: false, isCancelled: false, userCancelled: false });
};

const submitBillStatusChange = async () => {
  isSubmittingForm.value = true;
  const billToUpdate = { ...currentBillForm }; // Use a copy

  // Logic from original editBillInfo
  if (billToUpdate.cancelState && billToUpdate.payState) {
    ElMessage.error('Cannot set both Paid and Cancelled status to true.');
    isSubmittingForm.value = false;
    return;
  }

  try {
    let res;
    if (billToUpdate.payState && !originalBillStatus.isPaid) { // If marking as paid
      billToUpdate.cancelState = false; // Cannot be paid and cancelled
      billToUpdate.cancelTime = null;
      res = (await axios.put('sysBill', billToUpdate)).data; // Assuming PUT /sysBill updates the whole bill
      if (res.code === 200) ElMessage.success('Order marked as paid successfully.');

    } else if (billToUpdate.cancelState && !originalBillStatus.isCancelled) { // If marking as cancelled by user
      billToUpdate.payState = false; // Cannot be cancelled and paid
      billToUpdate.cancelTime = new Date().toISOString().slice(0, 19).replace('T', ' '); // YYYY-MM-DD HH:mm:ss

      const { data: sessionRes } = await axios.get(`sysSession/find/${billToUpdate.sessionId}`);
      if (sessionRes.code !== 200 || !sessionRes.data || !sessionRes.data.sessionSeats) {
        throw new Error('Failed to fetch session seat data for cancellation.');
      }
      let sessionSeats = JSON.parse(sessionRes.data.sessionSeats);
      const orderSeats = JSON.parse(billToUpdate.seats); // Assuming bill.seats is '["1排1座", "1排2座"]'

      for (const seatStr of orderSeats) {
        const rowMatch = seatStr.match(/^(\w+)排/);
        const colMatch = seatStr.match(/排(\d+)座$/);
        if (rowMatch && colMatch) {
          const row = rowMatch[1];
          const col = parseInt(colMatch[1], 10);
          if (sessionSeats[row] && sessionSeats[row][col - 1] !== undefined) {
            sessionSeats[row][col - 1] = 0; // Mark as available
          }
        }
      }
      // Use your specific backend endpoint for cancellation that also updates seats
      res = (await axios.put('sysBill/cancel', { sysBill: billToUpdate, sessionSeats: JSON.stringify(sessionSeats) })).data;
      if (res.code === 200) ElMessage.success('Order cancelled successfully and seats updated.');
    } else {
      // If no actual status change is being made through the switches (e.g., just opened and closed)
      // Or if trying to un-cancel or un-pay which is not typical via simple switches
      ElMessage.info("No status change applied or action not supported.");
      isSubmittingForm.value = false;
      isEditStatusDialogVisible.value = false;
      return;
    }

    if (res.code !== 200) {
      throw new Error(res.msg || 'Failed to update order status.');
    }
    isEditStatusDialogVisible.value = false;
    fetchBillList();

  } catch (error) {
    console.error("Error updating bill status:", error);
    ElMessage.error(error.message || 'An error occurred while updating order status.');
  } finally {
    isSubmittingForm.value = false;
  }
};


const confirmDeleteBill = async (billId) => {
  ElMessageBox.confirm('This will permanently delete the order. Continue?', 'Confirm Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true;
    try {
      const { data: res } = await axios.delete(`sysBill/${billId}`);
      if (res.code === 200) { ElMessage.success('Order deleted successfully!'); fetchBillList(); }
      else { ElMessage.error(res.msg || 'Failed to delete order.'); }
    } catch (error) { ElMessage.error('Error deleting order.'); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Deletion cancelled.'));
};

const confirmBatchDeleteBills = async () => {
  if (selectedBills.value.length === 0) { ElMessage.warning('Please select orders to delete.'); return; }
  ElMessageBox.confirm(`This will permanently delete ${selectedBills.value.length} selected orders. Continue?`, 'Confirm Batch Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true; const ids = selectedBills.value.map(b => b.billId);
    try {
      const { data: res } = await axios.delete(`sysBill/${ids.join(',')}`); // Assuming backend supports comma-separated IDs
      if (res.code === 200) { ElMessage.success('Selected orders deleted successfully!'); fetchBillList(); selectedBills.value = []; }
      else { ElMessage.error(res.msg || 'Failed to batch delete orders.'); }
    } catch (error) { ElMessage.error('Error during batch deletion.'); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Batch deletion cancelled.'));
};

const resetViewSeatsDialog = () => {
  for (const key in currentViewSeatsState) { delete currentViewSeatsState[key]; }
  viewingHallSeatNumsPerRow.value = 10;
};
const openViewSeatsDialog = async (sessionId) => {
  resetViewSeatsDialog();
  try {
    isLoadingTable.value = true;
    const { data: res } = await axios.get(`sysSession/find/${sessionId}`);
    if (res.code === 200 && res.data) {
      if (res.data.sessionSeats) {
        const parsedSeats = JSON.parse(res.data.sessionSeats);
        for (const key in parsedSeats) { if (Object.prototype.hasOwnProperty.call(parsedSeats, key)) { currentViewSeatsState[key] = [...parsedSeats[key]]; } }
      } else { ElMessage.info('Seat data is not available for this session.'); }
      
      if (res.data.sysHall && typeof res.data.sysHall.seatNumsRow === 'number') {
        viewingHallSeatNumsPerRow.value = res.data.sysHall.seatNumsRow;
      } else {
        viewingHallSeatNumsPerRow.value = 10; // Fallback if data is missing
        console.warn("sysHall.seatNumsRow not found or invalid in session data, using default for seat view.");
      }
      
      const baseWidth = 150;
      const seatTotalWidth = viewingHallSeatNumsPerRow.value * (28 + 4 + 2);
      viewSeatsDialogComputedWidth.value = `${Math.min(baseWidth + seatTotalWidth + 60, window.innerWidth * 0.95)}px`;
      isViewSeatsDialogVisible.value = true;
    } else { ElMessage.error(res.msg || 'Failed to fetch session details for seat view.'); }
  } catch (error) { ElMessage.error('Error fetching session details for seat view.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};

</script>

<style scoped>
.board { background-color: #fff; padding: 15px 20px; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-card { box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08); }
.filter-bar { margin-bottom: 20px; display: flex; flex-wrap: wrap; align-items: center; }
.filter-bar .el-col { margin-bottom: 10px; padding-right: 10px !important; padding-left: 10px !important;}
.filter-bar .el-col:last-child { padding-right: 0 !important; }
.search-button-col { display: flex; align-items: center; justify-content: flex-start; }
.search-button-col .el-button:first-child { margin-left: 0; }
.action-buttons-row { margin-bottom: 20px; }
.button-icon { margin-right: 5px; font-size: 1em; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.dialog-footer { display: flex; justify-content: flex-end; }
.el-table th { background-color: #f5f7fa; }

.seat-legend { display: flex; align-items: center; margin-bottom: 15px; padding-left: 10px; }
.seat-legend span { margin-right: 8px; vertical-align: middle; }
.seat-map-container { user-select: none; padding: 10px; border: 1px solid #eee; border-radius: 4px; max-height: 60vh; overflow-y: auto; background-color: #fff; }
.seat-column-header-row { display: flex; align-items: center; margin-bottom: 5px; padding-left: 0; }
.seat-column-header { display: flex; justify-content: center; align-items: center; width: 28px; height: 20px; margin: 2px; font-size: 12px; color: #303133; font-weight: 500; }
.seat-row { display: flex; align-items: center; margin-bottom: 6px; }
.seat-row-label { width: 50px; min-width: 50px; text-align: center; margin-right: 10px; font-weight: 500; color: #606266; font-size: 14px;}
.seats-in-row { display: flex; flex-wrap: nowrap; }
.seat { display: flex; justify-content: center; align-items: center; width: 28px; height: 24px; margin: 2px; background-position: center center; background-repeat: no-repeat; background-size: contain; cursor: default; border: 1px solid #dcdfe6; border-radius: 3px; box-sizing: border-box; font-size: 10px; color: #909399; }
.seat-enabled-legend, .seat-enabled { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAHKSURBVEhLxZfbysIwDMezKeJUVAR9A/H9X0FQZMOLeem14sWc4AnP9muybM4557eT/mCYdLT/tknaqQgJ/ACVf7/Oz4TfbvVisYDlcgm1Wg2SRON0OoGqqtDr9bglAAoHGY1GQgqzl5zz+SyGwyF7z7wITyYTtrJD13W2HrzEuFqtspUdnU4Hbrcbew5fSa5SqfQbYbmzoCgKew5fK6disciWgyd8vV5hPB5DoVAA27bpSct6vQbLsmjFhmFwK0MpJjFNky2H3W4nZEf24rPZbMTlcmFPiPv9LgaDAXu+rNY0jS0HzG5Zh+zFRwo9bS/GuNvtgpwM+SQsVwetVosa/GDnpMhFsfUAJ3I4HMgmYRQIZl1awsbzTyYyqzHRkhK2Yj+RwklwBYMHRpDMhd0tTrXiT52jwCsxCrqPZc3RARLM7O1266V/XMrlMlQqFfYcVqsVZXa9Xo8Wzhq/cOYx/i+e8KeYZIFfgyyMxWw2o4Y8mc/nXty9jz08NqfTKTSbTapBLAt8kh6buDocGh+3xNrtNjQaDbLxRSj7/f7pNolLv98Xx+ORvVfeBla+S3VJ4K7hGO/I7S8MDht2UbjklspRokj+NRQKwB/pWISi3oSUQQAAAABJRU5ErkJggg=="); }
.seat-disabled-legend, .seat-disabled { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAKVSURBVEhLxZXPThpRFMY/hhFBG4okbdiYxgXpxmJi0phi4sKlL+HWlSsfwMSFr+DKjSufwMSFiyakaaagQaOxW1txUUjDHwVh5vacmdtxgGEsA1N/yYHLvdzzzT33nDMhQeAFUOT3f+fFhAeGutRs4tfpKV5dXECoqpz9R8hlKxqFsrqK96mUnOzGVfhrsYh3+/tIXV8Dk5Nydkh0HW3a+21zE59WVuTkE33C5/k8PmxvW4ITE/SPkFzxQacDUOS0nR18XFiQkxZ9dzx9dARwaCOR0UQZ9kP29vAQupz6S39y8UmVMeZcOIwInfp54QAQdJDe2LkLt9tAq2Vmp294L/vhe6Zxb13YwrSMvKZRZMIoLy2hvLYG3N/7E+c9FN7f2SzK8/MQiQS0szO5aGFndfHkBJndXSAeNzc2ZmehUw3GCwUru4eBTlpbXETs6grqwwNgGBBkX7a2kF1eNv9inziWy1minFxU/NM3N2jHYr5PbJAPtdGwHpp8hiiS6YMDUPBNTOE6WfL21sxAGx7Xav5KivaIx0fy7kghGqv0QHR+6yd/GGQUcx52M0Idu+0UDn+ORxovLp24i8CEO9SrvQhM+LlL8hQWnJHcBHxYiKvDA7OOqzTobGwgWalYjZ2hjlObm0N7Zmb4kqIkipZKmLq7e6oU8ldJJqHu7YGK1kOYke3OF1xKzvLsEfa+Y34IDrcfc4q6YAsrfDcBozgy3RSeIvuRyVidinsrNfixW7WKn+m0qcXYLwlum9+Pj5G4vITO709KEDaDmrsfFPLBrtnMrkhvqDfr63gt13nBlYZhiFyhIH8Nz2dNE005dmNgcgl6Fxv8dvGJXq9DcIgHYId63LBbvqpBeJfTCHiJMoEJewP8Afy6sw903o8jAAAAAElFTkSuQmCC"); }
.seat-sold-legend, .seat-sold { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAANwSURBVEhLxZZLTBNRFIb/mWlpjbWAYgISNBofGyMGwQdEg0sTdaciaNy40UTxmeiKuDPGxwZXsjAuDNHohmhcYARjjGJBoYSFhAUYA+EpSksfQ8dz7gwwbacj5SEfXMq9c+757z3n3DuVNALLgGx8/neWTThlqIMjgL+3Gd3B11AklzE6NzTEkKFloTT/LNZtzDJG47EUbm/twouhk+hTOpAhGYNpMkXNSX+qPO+wv6xcHzSRJNzW/hm1A3vhcgD0C2mewswUeQ6T+IUcPwqLthujOkk5bh68AycpOklwIaKMQvMdCvBqsNoYmSVJOENeiQXqxcECEYzrHRP/paplKVnGUlil3ERiVJ2W9Z4atg9Gab4xN0qfXGRWB2dWmJ598bVAURzY4T6CA9mXEUpDnO1GQ8D5nfXYlFmMICkWeSux2XEQ3ow8fPX5DUudmapuamlA3dhReKiweCTXsRVrXdvQFWyAYw5JH54ErhY/pfqQ8aCtAnuyD6Mn0IgQ/bBAjBZyxvMBZaVlwn5mx22jT4Qon1sXjQ6o3+GWssWkKE0KUAitds9jLHqtuF707/oq4HGyDy8mtZA4HexToepuGD8tbBhdOAIMSa1xCeejMBEZFvkqyTuGm7ubMEKhNIvz/xxe3qlG2bzvq8Qat34MI1NBKirDkBC+ZRW0FoGuxbmkiSY7gUTV6CSL9z+e4+dEB2r2NYrdTYvzQq4Y4b3nq8JqQ5SRLC8Baea5LmxlY8JLV/Uj/0X0Bzpxq6xRCCaGN2fFrChjVclmzNG1hXdT57+E/okuXC95hnOFtXHhTURVqShssBVOXDWL885/h/vhdmSRaHx44/hHFG2FnbJbXCbmxmF/3FWNh+2nkEmifEEk2nBzKR5xIlKhn+MwcKNpA/44+kQ1M/xmyXfuglcpIAdUfWkgQ8Gg2olhtTvO3yp1PW6X99KqbIQZNrZbtR3sJtGXWdg21DyRb635NLOoFUKYa0ilq22piSE6cwcIYYmKZAuOI0CpDHOjh4vdJshvQeyQ0BKaIseERnl+2/oSPeE3kDWnuHm4xWLpFdY0siyL4yga7dXrysWJohpIdNEIWNiKqZCmtX7yG730afn4TdOiRseClMU1qQbopf7L6KVPIDyGkJq6bpK+ZS4W7Nb6RaFje5wWgp0os2TC9gB/AXsuAMA34noyAAAAAElFTkSuQmCC"); }

:global(.view-seats-dialog .el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 10px;
}
</style>