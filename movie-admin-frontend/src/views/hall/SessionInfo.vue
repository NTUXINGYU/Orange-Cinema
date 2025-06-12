<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Session Management</el-breadcrumb-item>
        <el-breadcrumb-item>Session Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="box-card">
      <el-row :gutter="20" class="filter-bar">
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="searchFilters.hallId" placeholder="Select Hall" clearable style="width:100%;" @change="fetchSessionList">
            <el-option v-for="item in hallOptions" :key="item.hallId" :label="item.hallName" :value="item.hallId"></el-option>
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="searchFilters.movieId" placeholder="Select Movie" clearable filterable style="width:100%;" @change="fetchSessionList">
            <el-option v-for="item in movieOptions" :key="item.movieId" :label="`${item.movieName} (${item.movieDirector || 'Unknown'})`" :value="item.movieId"></el-option>
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-date-picker
            v-model="searchFilters.sessionDate"
            type="date"
            placeholder="Select Date"
            value-format="YYYY-MM-DD"
            clearable
            style="width:100%;"
            @change="fetchSessionList"
          ></el-date-picker>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" class="search-button-col">
          <el-button :icon="Search" @click="fetchSessionList" type="primary">Search</el-button>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="action-buttons-row">
        <el-col :span="24">
          <el-button type="primary" @click="openAddSessionDialog" size="large">
            <i class="iconfont icon-add-button button-icon"></i> Add Session
          </el-button>
          <el-button type="danger" @click="checkAndBatchDeleteSessions" size="large" :disabled="selectedSessions.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>

      <el-table :data="sessionList" style="width: 100%" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoadingTable">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="sessionId" label="#" width="60"></el-table-column>
        <el-table-column prop="sysHall.hallName" label="Hall Name"></el-table-column>
        <el-table-column prop="sysMovie.movieName" label="Movie Title" show-overflow-tooltip></el-table-column>
        <el-table-column prop="discountRate" label="Discount" width="110" align="center">
            <template #default="scope">
                {{ scope.row.discountRate !== null ? (scope.row.discountRate * 100).toFixed(0) + '%' : 'No Discount' }}
            </template>
        </el-table-column>
        <el-table-column prop="sessionDate" label="Date" width="120"></el-table-column>
        <el-table-column label="Time" width="150">
          <template #default="scope">
            <span>{{scope.row.playTime}} - {{scope.row.endTime}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sessionPrice" label="Price" width="100" align="right">
            <template #default="scope">${{ scope.row.sessionPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="seatNums" label="Total Seats" width="120" align="center"></el-table-column>
        <el-table-column prop="sallNums" label="Sold" width="100" align="center"></el-table-column>
        <el-table-column label="Remaining" width="120" align="center">
          <template #default="scope">
            <span>{{ scope.row.seatNums - scope.row.sallNums }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sessionTips" label="Tips" show-overflow-tooltip></el-table-column>
        <el-table-column label="Actions" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Edit Session" placement="top" :enterable="false" :open-delay="300">
              <el-button type="primary" @click="checkAndOpenEditSessionDialog(scope.row.sessionId)" size="small" :icon="EditPen"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete Session" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="checkAndConfirmDeleteSession(scope.row)" size="small" :icon="Delete"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="View Seats" placement="top" :enterable="false" :open-delay="300">
              <el-button type="warning" @click="openViewSeatsDialog(scope.row.sessionId)" size="small" :icon="View"></el-button>
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
        :total="totalSessionCount"
        background
      >
      </el-pagination>
    </el-card>

    <el-dialog :title="sessionDialogTitle" v-model="isSessionFormDialogVisible" width="55%" @close="resetSessionFormDialog" top="5vh">
      <el-form :model="currentSessionForm" :rules="sessionFormRules" ref="sessionFormRef" label-width="150px">
        <el-form-item label="Hall Name" prop="hallId">
          <el-select v-model="currentSessionForm.hallId" placeholder="Select Hall" clearable style="width:100%;">
            <el-option v-for="item in hallOptions" :key="item.hallId" :label="item.hallName" :value="item.hallId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Movie Title" prop="movieId">
          <el-select v-model="currentSessionForm.movieId" placeholder="Select Movie" clearable filterable style="width:100%;">
            <el-option v-for="item in movieOptions" :key="item.movieId" :label="`${item.movieName} (${item.movieDirector || 'Unknown'})`" :value="item.movieId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Discount Rate" prop="discountRate">
          <el-input-number
            v-model="currentSessionForm.discountRate"
            :precision="2"
            :step="0.01"
            :min="0.01"
            :max="1.00"
            placeholder="e.g., 0.85 (15% off), leave empty for no discount"
            controls-position="right"
            style="width:100%;"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="Session Date" prop="sessionDate">
          <el-date-picker
            v-model="currentSessionForm.sessionDate"
            type="date"
            placeholder="Select date"
            value-format="YYYY-MM-DD"
            style="width:100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="Play Time" prop="playTime">
          <el-time-picker
            v-model="currentSessionForm.playTime"
            placeholder="Select play time"
            value-format="HH:mm"
            format="HH:mm"
            style="width:100%;"
          ></el-time-picker>
        </el-form-item>
        <el-form-item label="Session Price" prop="sessionPrice">
          <el-input-number v-model="currentSessionForm.sessionPrice" :precision="2" :step="0.5" :min="0" placeholder="Enter price"></el-input-number>
        </el-form-item>
        <el-form-item label="Tips" prop="sessionTips">
          <el-input v-model="currentSessionForm.sessionTips" type="textarea" placeholder="Enter any tips for this session"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isSessionFormDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitSessionForm" :loading="isSubmittingForm">
            <i class="iconfont icon-sure-button button-icon"></i> {{ sessionFormSubmitButtonText }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="View Seats" v-model="isViewSeatsDialogVisible" :style="{ width: viewSeatsDialogComputedWidth }" @close="resetViewSeatsDialog" top="5vh" custom-class="view-seats-dialog">
      <div class="seat-legend">
        <span>Enabled:</span> <span class="seat seat-enabled-legend"></span>
        <span style="margin-left: 20px;">Disabled:</span> <span class="seat seat-disabled-legend"></span>
        <span style="margin-left: 20px;">Sold:</span> <span class="seat seat-sold-legend"></span>
      </div>
      <div class="seat-map-container" v-if="Object.keys(currentViewSeatsState).length > 0">
         <div class="seat-column-header-row">
          <span class="seat-row-label"></span>
          <div class="seats-in-row">
            <span v-for="colNumber in viewingHallSeatNumsPerRow" :key="`col-header-${colNumber}`" class="seat-column-header">
              {{ colNumber }}
            </span>
          </div>
        </div>
        <div class="seat-row" v-for="rowKey in viewSeatRowKeys" :key="rowKey">
          <span class="seat-row-label">{{ rowKey }}</span>
          <div class="seats-in-row">
            <span
              v-for="(seatStatus, seatIndex) in currentViewSeatsState[rowKey]"
              :key="seatIndex"
              class="seat"
              :class="{
                'seat-enabled': seatStatus === 0,
                'seat-disabled': seatStatus === 1,
                'seat-sold': seatStatus === 3
              }"
            >
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="No seat information available or hall is empty." />
       <template #footer>
        <span class="dialog-footer">
          <el-button @click="isViewSeatsDialogVisible = false">Close</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { ArrowRight, EditPen, Delete, View, Search, Grid as GridIcon } from '@element-plus/icons-vue';

const isLoadingTable = ref(false);
const isSubmittingForm = ref(false);

const searchFilters = reactive({ hallId: '', movieId: '', sessionDate: '' });
const hallOptions = ref([]);
const movieOptions = ref([]);

const queryParameters = reactive({ hallId: '', movieId: '', sessionDate: '', pageNum: 1, pageSize: 7 });
const sessionList = ref([]);
const totalSessionCount = ref(0);
const selectedSessions = ref([]);

const isSessionFormDialogVisible = ref(false);
const isAddingSession = ref(true);
const sessionDialogTitle = computed(() => isAddingSession.value ? 'Add New Session' : 'Edit Session');
const sessionFormSubmitButtonText = computed(() => isAddingSession.value ? 'Confirm' : 'Save Changes');

const initialSessionFormState = {
  sessionId: null, hallId: '', movieId: '',
  playTime: '', endTime: '', deadline: '', sessionDate: null,
  sessionPrice: null, sessionTips: '', sessionSeats: '', seatNums: 0,
  discountRate: null
};
const currentSessionForm = reactive({ ...initialSessionFormState });
const sessionFormRef = ref(null);
const sessionFormRules = {
  hallId: [{ required: true, message: 'Please select a hall', trigger: 'change' }],
  movieId: [{ required: true, message: 'Please select a movie', trigger: 'change' }],
  discountRate: [
    { type: 'number', message: 'Discount rate must be a number', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== null && value !== undefined && (value <= 0 || value > 1)) {
          callback(new Error('Discount rate must be between 0.01 and 1.00 (e.g., 0.85 for 15% off)'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  sessionDate: [{ required: true, message: 'Please select session date', trigger: 'change' }],
  playTime: [{ required: true, message: 'Please select play time', trigger: 'change' }],
  sessionPrice: [{ type: 'number', required: true, message: 'Session price is required', trigger: 'blur', min: 0 }],
};

const isViewSeatsDialogVisible = ref(false);
const currentViewSeatsState = reactive({});
const viewSeatsDialogComputedWidth = ref('60%');
const viewingHallSeatNumsPerRow = ref(10);
const viewSeatRowKeys = computed(() => Object.keys(currentViewSeatsState).sort((a,b) => parseInt(a,10) - parseInt(b,10)));


const formatFullDateTime = (dateStr, timeStr) => {
  if (!dateStr || !timeStr) return null;
  return `${dateStr} ${timeStr}:00`;
};

const calculateEndTimeAndDeadline = async (movieId, sessionDate, playTime) => {
  if (!movieId || !sessionDate || !playTime) return { endTime: '', deadline: '' };
  try {
    const movie = movieOptions.value.find(m => m.movieId === movieId);
    if (!movie || typeof movie.movieLength !== 'number') {
      ElMessage.warning('Movie length not found or invalid to calculate end time.');
      return { endTime: '', deadline: '' };
    }
    const movieLengthMinutes = movie.movieLength;

    const startDate = new Date(`${sessionDate}T${playTime}:00`);
    if (isNaN(startDate.getTime())) {
        ElMessage.error('Invalid date or time format for calculation.');
        return { endTime: '', deadline: '' };
    }

    const endDate = new Date(startDate.getTime() + movieLengthMinutes * 60000);
    const deadlineDate = new Date(endDate.getTime() + 20 * 60000);

    const formatTime = (dt) => `${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`;
    const formatDateTime = (dt) => {
        const YYYY = dt.getFullYear();
        const MM = String(dt.getMonth() + 1).padStart(2, '0');
        const DD = String(dt.getDate()).padStart(2, '0');
        return `${YYYY}-${MM}-${DD} ${formatTime(dt)}:00`;
    };
    return { endTime: formatTime(endDate), deadline: formatDateTime(deadlineDate) };
  } catch (e) {
    console.error("Error calculating end time/deadline", e);
    ElMessage.error('Could not calculate end time.');
    return { endTime: '', deadline: '' };
  }
};


const fetchSessionList = async () => {
  isLoadingTable.value = true;
  queryParameters.hallId = searchFilters.hallId || '';
  queryParameters.movieId = searchFilters.movieId || '';
  queryParameters.sessionDate = searchFilters.sessionDate || '';
  try {
    const { data: res } = await axios.get('sysSession', { params: queryParameters });
    if (res.code === 200) { sessionList.value = res.data; totalSessionCount.value = res.total; }
    else { ElMessage.error(res.msg || 'Failed to fetch sessions.'); }
  } catch (error) { ElMessage.error('Error fetching sessions.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};

const fetchHallOptions = async () => {
  try {
    const { data: res } = await axios.get('sysHall', { params: { pageSize: 1000 } });
    if (res.code === 200) { hallOptions.value = res.data.data || res.data || []; }
    else { ElMessage.error('Failed to fetch hall options.'); }
  } catch (error) { ElMessage.error('Error fetching hall options.'); }
};
const fetchMovieOptions = async () => {
  try {
    const { data: res } = await axios.get('sysMovie/find', { params: { pageSize: 1000 } });
    if (res.code === 200) { movieOptions.value = res.data.data || res.data || []; }
    else { ElMessage.error('Failed to fetch movie options.'); }
  } catch (error) { ElMessage.error('Error fetching movie options.'); }
};

onMounted(() => { fetchSessionList(); fetchHallOptions(); fetchMovieOptions(); });

const handlePageSizeChange = (newSize) => { queryParameters.pageSize = newSize; fetchSessionList(); };
const handlePageNumChange = (newPage) => { queryParameters.pageNum = newPage; fetchSessionList(); };

const resetSessionFormDialog = () => {
  if (sessionFormRef.value) sessionFormRef.value.resetFields();
  Object.assign(currentSessionForm, { ...initialSessionFormState });
};
const openAddSessionDialog = () => {
  isAddingSession.value = true; resetSessionFormDialog(); isSessionFormDialogVisible.value = true;
  nextTick(() => { if (sessionFormRef.value) sessionFormRef.value.clearValidate(); });
};
const openEditSessionDialog = async (sessionId) => {
  isAddingSession.value = false; resetSessionFormDialog();
  try {
    const { data: res } = await axios.get(`sysSession/find/${sessionId}`);
    if (res.code === 200 && res.data) {
      Object.assign(currentSessionForm, res.data);
      if(currentSessionForm.sessionDate && currentSessionForm.sessionDate.includes(" ")) {
         currentSessionForm.sessionDate = currentSessionForm.sessionDate.split(" ")[0];
      }
      isSessionFormDialogVisible.value = true;
      nextTick(() => { if (sessionFormRef.value) sessionFormRef.value.clearValidate(); });
    } else { ElMessage.error(res.msg || 'Failed to fetch session details.'); }
  } catch (error) { ElMessage.error('Error fetching session details.'); }
};

const submitSessionForm = async () => {
  if (!sessionFormRef.value) return;
  await sessionFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmittingForm.value = true;
      const payload = { ...currentSessionForm };

      const times = await calculateEndTimeAndDeadline(payload.movieId, payload.sessionDate, payload.playTime);
      if (!times.endTime || !times.deadline) { isSubmittingForm.value = false; return; }
      payload.endTime = times.endTime;
      payload.deadline = times.deadline;

      if (payload.discountRate === '') payload.discountRate = null;

      if (isAddingSession.value) {
        try {
            const hall = hallOptions.value.find(h => h.hallId === payload.hallId);
            if(hall && hall.seatState){
                payload.sessionSeats = hall.seatState;
                payload.seatNums = hall.seatNums;
            } else { ElMessage.error("Could not find hall information to initialize seats."); isSubmittingForm.value = false; return; }
        } catch(e) { ElMessage.error("Error initializing seats."); isSubmittingForm.value = false; return; }
      }

      try {
        let response;
        if (isAddingSession.value) { response = await axios.post('sysSession', payload); }
        else { response = await axios.put('sysSession', payload); }
        const res = response.data;
        if (res.code === 200) { ElMessage.success(`Session ${isAddingSession.value ? 'added' : 'updated'} successfully!`); isSessionFormDialogVisible.value = false; fetchSessionList(); }
        else { ElMessage.error(res.msg || `Failed to ${isAddingSession.value ? 'add' : 'update'} session.`); }
      } catch (error) { ElMessage.error(`Error ${isAddingSession.value ? 'adding' : 'updating'} session.`); }
      finally { isSubmittingForm.value = false; }
    }
  });
};

const checkSessionBills = async (sessionId) => {
  try {
    const { data: res } = await axios.get('sysBill', { params: { sessionId }});
    if (res.code === 200 && res.total > 0 && res.data && res.data.length > 0) {
      const billIds = res.data.map(b => b.billId).join(', ');
      ElNotification({ title: 'Action Blocked', message: `This session has associated bills (ID(s): ${billIds}).`, type: 'warning', duration: 0 });
      return true;
    } return false;
  } catch (error) { ElMessage.error('Failed to check session bills.'); return true; }
};
const checkAndOpenEditSessionDialog = async (sessionId) => { if (await checkSessionBills(sessionId)) return; openEditSessionDialog(sessionId);};
const checkAndConfirmDeleteSession = async (session) => {
  if (new Date(session.deadline) <= new Date()) { }
  else if (await checkSessionBills(session.sessionId)) return;

  ElMessageBox.confirm('This will permanently delete the session. Continue?', 'Confirm Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true;
    try {
      const { data: res } = await axios.delete(`sysSession/${session.sessionId}`);
      if (res.code === 200) { ElMessage.success('Session deleted successfully!'); fetchSessionList(); }
      else { ElMessage.error(res.msg || 'Failed to delete session.'); }
    } catch (error) { ElMessage.error('Error deleting session.'); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Deletion cancelled.'));
};
const handleTableSelectionChange = (selection) => { selectedSessions.value = selection; };
const checkAndBatchDeleteSessions = async () => {
  if (selectedSessions.value.length === 0) { ElMessage.warning('Please select sessions to delete.'); return; }
  isLoadingTable.value = true; let canDeleteAll = true;
  for (const session of selectedSessions.value) {
    if (new Date(session.deadline) <= new Date()) continue;
    if (await checkSessionBills(session.sessionId)) { canDeleteAll = false; break; }
  }
  isLoadingTable.value = false; if (!canDeleteAll) return;
  ElMessageBox.confirm(`This will permanently delete ${selectedSessions.value.length} selected sessions. Continue?`, 'Confirm Batch Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true; const ids = selectedSessions.value.map(s => s.sessionId);
    try {
      const { data: res } = await axios.delete(`sysSession/${ids.join(',')}`);
      if (res.code === 200) { ElMessage.success('Selected sessions deleted successfully!'); fetchSessionList(); selectedSessions.value = []; }
      else { ElMessage.error(res.msg || 'Failed to batch delete sessions.'); }
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
      } else {
         ElMessage.info('Seat data is not available for this session.');
      }
      viewingHallSeatNumsPerRow.value = res.data.sysHall?.seatNumsRow || 10;
      const baseWidth = 150;
      const seatTotalWidth = viewingHallSeatNumsPerRow.value * (28 + 4 + 2);
      viewSeatsDialogComputedWidth.value = `${Math.min(baseWidth + seatTotalWidth + 60, window.innerWidth * 0.95)}px`;
      isViewSeatsDialogVisible.value = true;
    } else {
      ElMessage.error(res.msg || 'Failed to fetch session details for seat view.');
    }
  } catch (error) {
    ElMessage.error('Error fetching session details for seat view.');
    console.error("View seats error:", error);
  }
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