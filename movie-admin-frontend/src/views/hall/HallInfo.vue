<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Hall Management</el-breadcrumb-item>
        <el-breadcrumb-item>Hall Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="box-card">
      <el-row :gutter="20" class="filter-bar">
        <el-col :xs="24" :sm="8" :md="7">
          <el-input v-model="searchHallName" placeholder="Enter hall name" clearable @clear="fetchHallList" @keyup.enter="fetchHallList"/>
        </el-col>
        <el-col :xs="24" :sm="8" :md="7">
          <el-select v-model="searchHallCategory" placeholder="Select hall category" clearable style="width:100%;" @clear="fetchHallList" @change="fetchHallList">
            <el-option
              v-for="item in hallCategoryOptions"
              :key="item"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
        </el-col>
         <el-col :xs="24" :sm="8" :md="10" class="search-button-col">
          <el-button :icon="Search" @click="fetchHallList" type="primary">Search</el-button>
          <el-button type="primary" @click="openAddHallDialog" size="medium" style="margin-left: 10px;">
            <i class="iconfont icon-add-button button-icon"></i> Add Hall
          </el-button>
          <el-button type="danger" @click="checkAndBatchDeleteHalls" size="medium" :disabled="selectedHalls.length === 0" style="margin-left: 10px;">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>

      <el-table :data="hallList" style="width: 100%" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoadingTable">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="hallId" label="Hall ID" width="100" sortable></el-table-column>
        <el-table-column prop="hallName" label="Hall Name"></el-table-column>
        <el-table-column prop="hallCategory" label="Category"></el-table-column>
        <el-table-column prop="rowStart" label="Start Row" align="center"></el-table-column>
        <el-table-column prop="rowNums" label="Num of Rows" align="center"></el-table-column>
        <el-table-column prop="seatNumsRow" label="Seats/Row" align="center"></el-table-column>
        <el-table-column prop="seatNums" label="Enabled Seats" align="center"></el-table-column>
        <el-table-column label="Actions" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Delete Hall" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="checkAndConfirmDeleteHall(scope.row.hallId)" size="small" :icon="Delete"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Arrange Seats" placement="top" :enterable="false" :open-delay="300">
              <el-button type="warning" @click="openArrangeSeatsDialog(scope.row.hallId)" size="small" :icon="Grid"></el-button>
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
        :total="totalHallCount"
        background
      >
      </el-pagination>
    </el-card>

    <el-dialog title="Add New Hall" v-model="isAddHallDialogVisible" width="55%" @close="resetAddHallFormDialog" top="5vh">
      <el-form :model="addHallForm" :rules="hallFormRules" ref="addHallFormRefInstance" label-width="140px">
        <el-form-item label="Hall Name" prop="hallName">
          <el-input v-model="addHallForm.hallName" placeholder="Enter hall name"></el-input>
        </el-form-item>
        <el-form-item label="Hall Category" prop="hallCategory">
          <el-select v-model="addHallForm.hallCategory" placeholder="Select hall category" clearable style="width:100%;">
            <el-option v-for="item in hallCategoryOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Starting Row Label" prop="rowStart">
          <el-input v-model="addHallForm.rowStart" disabled></el-input>
        </el-form-item>
        <el-form-item label="Number of Rows" prop="rowNums">
          <el-input-number v-model="addHallForm.rowNums" :min="1" placeholder="e.g., 10"></el-input-number>
        </el-form-item>
        <el-form-item label="Seats per Row" prop="seatNumsRow">
          <el-input-number v-model="addHallForm.seatNumsRow" :min="1" placeholder="e.g., 10"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isAddHallDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitAddHallForm" :loading="isSubmittingForm">
            <i class="iconfont icon-sure-button button-icon"></i> Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      title="Arrange Seats"
      v-model="isArrangeSeatsDialogVisible"
      :style="{ width: arrangeDialogComputedWidth }"
      custom-class="arrange-seats-dialog"
      @close="resetArrangeSeatsDialog"
      top="5vh"
    >
      <div class="seat-legend">
        <span>Enabled:</span> <span class="seat seat-enabled-legend"></span>
        <span style="margin-left: 20px;">Disabled:</span> <span class="seat seat-disabled-legend"></span>
      </div>

      <div class="seat-map-container" v-if="seatRowKeys.length > 0 && currentEditingHallForSeats">
        <div class="seat-column-header-row">
          <span class="seat-row-label"></span>
          <div class="seats-in-row">
            <span v-for="colNumber in currentEditingHallForSeats.seatNumsRow" :key="`col-${colNumber}`" class="seat-column-header">
              {{ colNumber }}
            </span>
          </div>
        </div>
        <div class="seat-row" v-for="rowKey in seatRowKeys" :key="rowKey">
          <span class="seat-row-label">{{ rowKey }}</span>
          <div class="seats-in-row">
            <span
              v-for="(seatStatus, seatIndex) in currentSeatsState[rowKey]"
              :key="seatIndex"
              class="seat"
              :class="{
                'seat-enabled': seatStatus === 0,
                'seat-disabled': seatStatus === 1
              }"
              @click="toggleSeatStatus(rowKey, seatIndex)"
            >
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="No seats to display. Hall dimensions might be missing or zero." />

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isArrangeSeatsDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="saveSeatArrangement" :loading="isSubmittingForm">
             <i class="iconfont icon-sure-button button-icon"></i> Save Arrangement
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
import { ArrowRight, Delete, Grid, Search } from '@element-plus/icons-vue'; // Removed EditPen as Edit Hall is removed

const isLoadingTable = ref(false);
const isSubmittingForm = ref(false);

const searchHallName = ref('');
const searchHallCategory = ref('');
const hallCategoryOptions = ref([]);

const queryParameters = reactive({ hallName: '', hallCategory: '', pageNum: 1, pageSize: 7 });
const hallList = ref([]);
const totalHallCount = ref(0);
const selectedHalls = ref([]);

const isAddHallDialogVisible = ref(false);
const initialAddHallFormState = {
  hallId: null, cinemaId: '1', hallName: '', hallCategory: '',
  rowStart: '1',
  rowNums: 10, seatNumsRow: 10,
  seatNums: 100, seatState: '', delState: 0
};
const addHallForm = reactive({ ...initialAddHallFormState });
const addHallFormRefInstance = ref(null);

const hallFormRules = {
  hallName: [{ required: true, message: 'Please enter hall name', trigger: 'blur' }],
  hallCategory: [{ required: true, message: 'Please select hall category', trigger: 'change' }],
  rowNums: [{ type: 'number', required: true, message: 'Number of rows is required and must be a number', trigger: 'blur', min: 1 }],
  seatNumsRow: [{ type: 'number', required: true, message: 'Seats per row is required and must be a number', trigger: 'blur', min: 1 }],
};

const isArrangeSeatsDialogVisible = ref(false);
const currentEditingHallForSeats = ref(null);
const currentSeatsState = reactive({});
const arrangeDialogComputedWidth = ref('60%');

const seatRowKeys = computed(() => {
  return Object.keys(currentSeatsState).sort((a, b) => parseInt(a, 10) - parseInt(b, 10));
});

const parseJsonString = (jsonString, defaultValue = {}) => {
  try {
    const parsed = JSON.parse(jsonString);
    return (typeof parsed === 'object' && parsed !== null) ? parsed : defaultValue;
  } catch (e) { return defaultValue; }
};

const fetchHallList = async () => {
  isLoadingTable.value = true;
  queryParameters.hallName = searchHallName.value.trim();
  queryParameters.hallCategory = searchHallCategory.value;
  try {
    const { data: res } = await axios.get('sysHall', { params: queryParameters });
    if (res.code === 200) { hallList.value = res.data; totalHallCount.value = res.total; }
    else { ElMessage.error(res.msg || 'Failed to fetch halls.'); }
  } catch (error) { ElMessage.error('Error fetching halls.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};

const fetchCinemaHallCategories = async () => {
  try {
    const { data: res } = await axios.get('sysCinema');
    if (res.code === 200 && res.data) {
      hallCategoryOptions.value = parseJsonString(res.data.hallCategoryList, []);
    } else { ElMessage.error(res.msg || 'Failed to fetch hall categories from cinema info.'); }
  } catch (error) { ElMessage.error('Error fetching hall categories.'); console.error(error); }
};

onMounted(() => { fetchHallList(); fetchCinemaHallCategories(); });

const handlePageSizeChange = (newSize) => { queryParameters.pageSize = newSize; fetchHallList(); };
const handlePageNumChange = (newPage) => { queryParameters.pageNum = newPage; fetchHallList(); };

const generateInitialSeatState = (rows, seatsPerRow) => {
  const seats = {};
  if (!rows || !seatsPerRow ) return seats;
  for (let i = 0; i < rows; i++) {
    const rowLabel = String(1 + i);
    seats[rowLabel] = Array(seatsPerRow).fill(0);
  }
  return seats;
};

const resetAddHallFormDialog = () => {
  if (addHallFormRefInstance.value) addHallFormRefInstance.value.resetFields();
  Object.assign(addHallForm, { ...initialAddHallFormState });
  addHallForm.rowStart = '1';
};
const openAddHallDialog = () => {
  resetAddHallFormDialog();
  isAddHallDialogVisible.value = true;
  nextTick(() => { if (addHallFormRefInstance.value) addHallFormRefInstance.value.clearValidate(); });
};

const submitAddHallForm = async () => {
  if (!addHallFormRefInstance.value) return;
  addHallForm.rowStart = '1';
  await addHallFormRefInstance.value.validate(async (valid) => {
    if (valid) {
      isSubmittingForm.value = true;
      const payload = { ...addHallForm };
      payload.rowNums = Number(payload.rowNums);
      payload.seatNumsRow = Number(payload.seatNumsRow);
      payload.seatNums = payload.rowNums * payload.seatNumsRow;
      payload.seatState = JSON.stringify(generateInitialSeatState(payload.rowNums, payload.seatNumsRow));

      try {
        const { data: res } = await axios.post('sysHall', payload);
        if (res.code === 200) { ElMessage.success('Hall added successfully!'); isAddHallDialogVisible.value = false; fetchHallList(); }
        else { ElMessage.error(res.msg || 'Failed to add hall.'); }
      } catch (error) { ElMessage.error('Error adding hall.'); console.error(error); }
      finally { isSubmittingForm.value = false; }
    }
  });
};

const checkHallSessions = async (hallId) => {
  try {
    const { data: res } = await axios.get('sysSession/isAbleEdit', { params: { hallId } });
    if (res.code === 200 && res.data && res.data.length > 0) { const sessionIds = res.data.map(s => s.sessionId).join(', '); ElNotification({ title: 'Action Blocked', message: `This hall has active sessions (ID(s): ${sessionIds}). Please resolve them.`, type: 'warning', duration: 0 }); return true; }
    return false;
  } catch (error) { ElMessage.error('Failed to check hall sessions.'); console.error(error); return true; }
};

const checkAndConfirmDeleteHall = async (hallId) => {
  if (await checkHallSessions(hallId)) return;
  ElMessageBox.confirm('This will permanently delete the hall. Continue?', 'Confirm Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true;
    try {
      const payload = [{ hallId: hallId }];
      const { data: res } = await axios.post('sysHall/delete', payload);
      if (res.code === 200) { ElMessage.success('Hall deleted successfully!'); fetchHallList(); }
      else { ElMessage.error(res.msg || 'Failed to delete hall.'); }
    } catch (error) { ElMessage.error('Error deleting hall.'); console.error(error); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Deletion cancelled.'));
};
const handleTableSelectionChange = (selection) => { selectedHalls.value = selection; };
const checkAndBatchDeleteHalls = async () => {
  if (selectedHalls.value.length === 0) { ElMessage.warning('Please select halls to delete.'); return; }
  isLoadingTable.value = true; let canDeleteAll = true;
  for (const hall of selectedHalls.value) { if (await checkHallSessions(hall.hallId)) { canDeleteAll = false; break; } }
  isLoadingTable.value = false; if (!canDeleteAll) return;
  ElMessageBox.confirm(`This will permanently delete ${selectedHalls.value.length} selected halls. Continue?`, 'Confirm Batch Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true; const idsToDeletePayload = selectedHalls.value.map(h => ({ hallId: h.hallId }));
    try {
      const { data: res } = await axios.post('sysHall/delete', idsToDeletePayload);
      if (res.code === 200) { ElMessage.success('Selected halls deleted successfully!'); fetchHallList(); selectedHalls.value = []; }
      else { ElMessage.error(res.msg || 'Failed to batch delete halls.'); }
    } catch (error) { ElMessage.error('Error during batch deletion.'); console.error(error); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Batch deletion cancelled.'));
};

const resetArrangeSeatsDialog = () => {
  currentEditingHallForSeats.value = null;
  for (const key in currentSeatsState) { delete currentSeatsState[key]; }
};
const openArrangeSeatsDialog = async (hallId) => {
  resetArrangeSeatsDialog();
  try {
    isLoadingTable.value = true;
    const { data: res } = await axios.get(`sysHall/${hallId}`);
    if (res.code === 200 && res.data) {
      currentEditingHallForSeats.value = { ...res.data };
      const parsedSeats = parseJsonString(res.data.seatState, {});
      Object.keys(currentSeatsState).forEach(key => delete currentSeatsState[key]);
      for (const key in parsedSeats) { if (Object.prototype.hasOwnProperty.call(parsedSeats, key)) { currentSeatsState[key] = [...parsedSeats[key]]; } }
      const seatNumsPerRow = res.data.seatNumsRow || 10;
      const baseWidth = 150;
      const seatTotalWidth = seatNumsPerRow * (28 + 4 + 2);
      arrangeDialogComputedWidth.value = `${Math.min(baseWidth + seatTotalWidth + 60, window.innerWidth * 0.95)}px`;
      isArrangeSeatsDialogVisible.value = true;
    } else { ElMessage.error(res.msg || 'Failed to fetch hall seat details.'); }
  } catch (error) { ElMessage.error('Error fetching hall seat details.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};

const toggleSeatStatus = (rowKey, seatIndex) => {
  if (currentSeatsState.hasOwnProperty(rowKey) && currentSeatsState[rowKey] && typeof currentSeatsState[rowKey][seatIndex] !== 'undefined') {
    currentSeatsState[rowKey][seatIndex] = currentSeatsState[rowKey][seatIndex] === 0 ? 1 : 0;
  } else { console.warn(`Attempted to toggle non-existent seat: Row ${rowKey}, Index ${seatIndex}`); }
};

const saveSeatArrangement = async () => {
  if (!currentEditingHallForSeats.value) return;
  isSubmittingForm.value = true;
  const hallDataToUpdate = { ...currentEditingHallForSeats.value };
  hallDataToUpdate.seatState = JSON.stringify(currentSeatsState);
  let enabledSeatCount = 0;
  for (const rowKey in currentSeatsState) { if (Object.prototype.hasOwnProperty.call(currentSeatsState, rowKey)) { for (const seatStatus of currentSeatsState[rowKey]) { if (seatStatus === 0) enabledSeatCount++; } } }
  hallDataToUpdate.seatNums = enabledSeatCount;
  hallDataToUpdate.rowStart = '1';

  try {
    const { data: res } = await axios.put('sysHall', hallDataToUpdate);
    if (res.code === 200) { ElMessage.success('Seat arrangement saved successfully!'); isArrangeSeatsDialogVisible.value = false; fetchHallList(); }
    else { ElMessage.error(res.msg || 'Failed to save seat arrangement.'); }
  } catch (error) { ElMessage.error('Error saving seat arrangement.'); console.error(error); }
  finally { isSubmittingForm.value = false; }
};

</script>

<style scoped>
.board { background-color: #fff; padding: 15px 20px; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-card { box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08); }
.filter-bar { margin-bottom: 20px; display: flex; flex-wrap: wrap; align-items: center; }
.filter-bar .el-col { margin-bottom: 10px; padding-right: 10px !important; padding-left: 10px !important;}
.filter-bar .el-col:last-child { padding-right: 0 !important; }
.search-button-col { display: flex; align-items: center; justify-content: flex-start; }
.search-button-col .el-button { margin-left: 0; }
.action-buttons-row { margin-bottom: 20px; }
.button-icon { margin-right: 5px; font-size: 1em; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.dialog-footer { display: flex; justify-content: flex-end; }
.el-table th { background-color: #f5f7fa; }

.seat-legend { display: flex; align-items: center; margin-bottom: 15px; padding-left: 10px; }
.seat-legend span { margin-right: 8px; vertical-align: middle; }
.seat-map-container { user-select: none; padding: 10px; border: 1px solid #eee; border-radius: 4px; max-height: 60vh; overflow-y: auto; background-color: #fff; }
.seat-row { display: flex; align-items: center; margin-bottom: 6px; }
.seat-row-label { width: 50px; min-width: 50px; text-align: center; margin-right: 10px; font-weight: bold; color: #606266; font-size: 14px;}
.seats-in-row { display: flex; flex-wrap: nowrap; }
.seat { display: flex; justify-content: center; align-items: center; width: 28px; height: 24px; margin: 2px; background-position: center center; background-repeat: no-repeat; background-size: contain; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 3px; box-sizing: border-box; font-size: 10px; color: #909399; transition: transform 0.1s ease-out, background-color 0.2s; }
.seat:hover { transform: scale(1.05); border-color: #c0c4cc; }
.seat-enabled-legend, .seat-enabled { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAHKSURBVEhLxZfbysIwDMezKeJUVAR9A/H9X0FQZMOLeem14sWc4AnP9muybM4557eT/mCYdLT/tknaqQgJ/ACVf7/Oz4TfbvVisYDlcgm1Wg2SRON0OoGqqtDr9bglAAoHGY1GQgqzl5zz+SyGwyF7z7wITyYTtrJD13W2HrzEuFqtspUdnU4Hbrcbew5fSa5SqfQbYbmzoCgKew5fK6disciWgyd8vV5hPB5DoVAA27bpSct6vQbLsmjFhmFwK0MpJjFNky2H3W4nZEf24rPZbMTlcmFPiPv9LgaDAXu+rNY0jS0HzG5Zh+zFRwo9bS/GuNvtgpwM+SQsVwetVosa/GDnpMhFsfUAJ3I4HMgmYRQIZl1awsbzTyYyqzHRkhK2Yj+RwklwBYMHRpDMhd0tTrXiT52jwCsxCrqPZc3RARLM7O1266V/XMrlMlQqFfYcVqsVZXa9Xo8Wzhq/cOYx/i+e8KeYZIFfgyyMxWw2o4Y8mc/nXty9jz08NqfTKTSbTapBLAt8kh6buDocGh+3xNrtNjQaDbLxRSj7/f7pNolLv98Xx+ORvVfeBla+S3VJ4K7hGO/I7S8MDht2UbjklspRokj+NRQKwB/pWISi3oSUQQAAAABJRU5ErkJggg=="); }
.seat-enabled:hover { border-color: #a0cfff; }
.seat-disabled-legend, .seat-disabled { background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAKVSURBVEhLxZXPThpRFMY/hhFBG4okbdiYxgXpxmJi0phi4sKlL+HWlSsfwMSFr+DKjSufwMSFiyakaaagQaOxW1txUUjDHwVh5vacmdtxgGEsA1N/yYHLvdzzzT33nDMhQeAFUOT3f+fFhAeGutRs4tfpKV5dXECoqpz9R8hlKxqFsrqK96mUnOzGVfhrsYh3+/tIXV8Dk5Nydkh0HW3a+21zE59WVuTkE33C5/k8PmxvW4ITE/SPkFzxQacDUOS0nR18XFiQkxZ9dzx9dARwaCOR0UQZ9kP29vAQupz6S39y8UmVMeZcOIwInfp54QAQdJDe2LkLt9tAq2Vmp294L/vhe6Zxb13YwrSMvKZRZMIoLy2hvLYG3N/7E+c9FN7f2SzK8/MQiQS0szO5aGFndfHkBJndXSAeNzc2ZmehUw3GCwUru4eBTlpbXETs6grqwwNgGBBkX7a2kF1eNv9inziWy1minFxU/NM3N2jHYr5PbJAPtdGwHpp8hiiS6YMDUPBNTOE6WfL21sxAGx7Xav5KivaIx0fy7kghGqv0QHR+6yd/GGQUcx52M0Idu+0UDn+ORxovLp24i8CEO9SrvQhM+LlL8hQWnJHcBHxYiKvDA7OOqzTobGwgWalYjZ2hjlObm0N7Zmb4kqIkipZKmLq7e6oU8ldJJqHu7YGK1kOYke3OF1xKzvLsEfa+Y34IDrcfc4q6YAsrfDcBozgy3RSeIvuRyVidinsrNfixW7WKn+m0qcXYLwlum9+Pj5G4vITO709KEDaDmrsfFPLBrtnMrkhvqDfr63gt13nBlYZhiFyhIH8Nz2dNE005dmNgcgl6Fxv8dvGJXq9DcIgHYId63LBbvqpBeJfTCHiJMoEJewP8Afy6sw903o8jAAAAAElFTkSuQmCC"); }
.seat-disabled:hover { border-color: #fbc4c4; }

.seat-column-header-row { display: flex; align-items: center; margin-bottom: 5px; padding-left: 0; }
.seat-column-header {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 28px;  
  height: 20px; 
  margin: 2px;  
  font-size: 12px;
  color: #303133;
  font-weight: 500;
}

:global(.arrange-seats-dialog .el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 10px;
}
</style>