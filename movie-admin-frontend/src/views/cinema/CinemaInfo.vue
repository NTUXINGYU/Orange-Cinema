<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Cinema Management</el-breadcrumb-item>
        <el-breadcrumb-item>Cinema Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="box-card">
      <el-form :model="cinemaInfo" label-width="180px" class="view-form">
        <el-form-item label="Cinema Name:">
          <el-input class="el-input-show" v-model="cinemaInfo.cinemaName" disabled></el-input>
        </el-form-item>
        <el-form-item label="Cinema Address:">
          <el-input class="el-input-show" v-model="cinemaInfo.cinemaAddress" disabled></el-input>
        </el-form-item>
        <el-form-item label="Cinema Phone:">
          <el-input class="el-input-show" v-model="cinemaInfo.cinemaPhone" disabled></el-input>
        </el-form-item>
        <el-form-item label="Business Hours:">
          <el-input class="el-input-show-time" v-model="cinemaInfo.workStartTime" disabled></el-input>
          <span class="time-separator">to</span>
          <el-input class="el-input-show-time" v-model="cinemaInfo.workEndTime" disabled></el-input>
        </el-form-item>
        <el-form-item label="Hall Types:">
          <el-tag v-for="(hall, index) in displayedHallTypes" :key="index" class="hall-tag">{{ hall }}</el-tag>
          <el-empty v-if="displayedHallTypes.length === 0" description="No hall types available" :image-size="50"></el-empty>
        </el-form-item>
        <el-form-item label="">
          <el-button type="primary" size="large" @click="openEditDialog" :icon="Edit">
            Edit Cinema Information
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="Edit Cinema Information" v-model="isEditDialogVisible" width="60%" @close="handleEditDialogClosed" top="5vh">
      <el-form :model="editCinemaForm" :rules="editCinemaFormRules" ref="editFormRefInstance" label-width="180px">
        <el-form-item label="Cinema Name" prop="cinemaName">
          <el-input v-model="editCinemaForm.cinemaName" placeholder="Enter cinema name"></el-input>
        </el-form-item>
        <el-form-item label="Cinema Address" prop="cinemaAddress">
          <el-input v-model="editCinemaForm.cinemaAddress" placeholder="Enter cinema address"></el-input>
        </el-form-item>
        <el-form-item label="Cinema Phone" prop="cinemaPhone">
          <el-input v-model="editCinemaForm.cinemaPhone" placeholder="Enter cinema phone number"></el-input>
        </el-form-item>
        <el-form-item label="Opening Time" prop="workStartTime">
          <el-time-picker
            v-model="editCinemaForm.workStartTime"
            value-format="HH:mm"
            format="HH:mm"
            placeholder="Select opening time"
            style="width: 100%;"
          ></el-time-picker>
        </el-form-item>
        <el-form-item label="Closing Time" prop="workEndTime">
          <el-time-picker
            v-model="editCinemaForm.workEndTime"
            value-format="HH:mm"
            format="HH:mm"
            placeholder="Select closing time"
            style="width: 100%;"
          ></el-time-picker>
        </el-form-item>

        <el-form-item label="Hall Types" prop="editableHallTypes">
          <div class="hall-type-input-group">
            <el-input class="el-input-hall" placeholder="Enter hall type to add" v-model="newHallTypeInput" clearable @keyup.enter="addEditableHallType"></el-input>
            <el-button type="success" @click="addEditableHallType" :icon="Plus">Add</el-button>
          </div>
           <div class="hall-tags-container">
            <el-tag
              v-for="(hallType, index) in editCinemaForm.editableHallTypes"
              :key="index"
              closable
              @close="removeEditableHallType(index)"
              class="editable-hall-tag"
            >
              {{ hallType }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelCinemaEdit">Cancel</el-button>
          <el-button type="primary" @click="submitCinemaInfoChanges" :loading="isSubmitting">
            <i class="iconfont icon-sure-button button-icon"></i> Confirm Changes
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ArrowRight, Edit, Plus } from '@element-plus/icons-vue';

const cinemaInfo = ref({});
const displayedHallTypes = ref([]);

const isEditDialogVisible = ref(false);
const isSubmitting = ref(false);

const initialEditFormState = {
  cinemaId: null,
  cinemaName: '',
  cinemaAddress: '',
  cinemaPhone: '',
  workStartTime: '',
  workEndTime: '',
  hallCategoryList: '[]',
  editableHallTypes: [],
};
const editCinemaForm = reactive({ ...initialEditFormState });

const editCinemaFormRules = {
  cinemaName: [{ required: true, message: 'Please enter cinema name', trigger: 'blur' }],
  cinemaAddress: [{ required: true, message: 'Please enter cinema address', trigger: 'blur' }],
  cinemaPhone: [{ required: true, message: 'Please enter cinema phone number', trigger: 'blur' }],
  workStartTime: [{ required: true, message: 'Please select opening time', trigger: 'change' }],
  workEndTime: [{ required: true, message: 'Please select closing time', trigger: 'change' }],
};

const newHallTypeInput = ref('');
const editFormRefInstance = ref(null);

const parseJsonString = (jsonString, defaultValue = []) => {
  try {
    const parsed = JSON.parse(jsonString);
    return Array.isArray(parsed) ? parsed : defaultValue;
  } catch (e) {
    return defaultValue;
  }
};

const fetchCinemaInfo = async () => {
  try {
    const { data: response } = await axios.get('sysCinema');
    if (response.code === 200 && response.data) {
      cinemaInfo.value = response.data;
      displayedHallTypes.value = parseJsonString(response.data.hallCategoryList, []);
    } else {
      ElMessage.error(response.msg || 'Failed to fetch cinema information.');
    }
  } catch (error) {
    ElMessage.error('Error fetching cinema information.');
  }
};

onMounted(fetchCinemaInfo);

const openEditDialog = () => {
  Object.assign(editCinemaForm, cinemaInfo.value);
  editCinemaForm.editableHallTypes = [...displayedHallTypes.value];

  isEditDialogVisible.value = true;
  nextTick(() => {
    if (editFormRefInstance.value) editFormRefInstance.value.clearValidate();
  });
};

const handleEditDialogClosed = () => {
  if (editFormRefInstance.value) editFormRefInstance.value.resetFields();
  newHallTypeInput.value = '';
  Object.assign(editCinemaForm, initialEditFormState);
};

const cancelCinemaEdit = () => {
  isEditDialogVisible.value = false;
};

const addEditableHallType = () => {
  if (newHallTypeInput.value && !editCinemaForm.editableHallTypes.includes(newHallTypeInput.value.trim())) {
    editCinemaForm.editableHallTypes.push(newHallTypeInput.value.trim());
    newHallTypeInput.value = '';
  } else if (!newHallTypeInput.value) {
    ElMessage.warning('Hall type cannot be empty.');
  } else {
    ElMessage.warning('Hall type already exists.');
  }
};

const removeEditableHallType = (index) => {
  editCinemaForm.editableHallTypes.splice(index, 1);
};

const submitCinemaInfoChanges = async () => {
  if (!editFormRefInstance.value) return;
  await editFormRefInstance.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;

      const payload = { ...editCinemaForm };
      payload.hallCategoryList = JSON.stringify(editCinemaForm.editableHallTypes);
      delete payload.editableHallTypes;

      try {
        const { data: response } = await axios.put('sysCinema/update', payload);
        if (response.code === 200) {
          ElMessage.success('Cinema information updated successfully!');
          isEditDialogVisible.value = false;
          await fetchCinemaInfo();
        } else {
          ElMessage.error(response.msg || 'Failed to update cinema information.');
        }
      } catch (error) {
        ElMessage.error('Error updating cinema information.');
      } finally {
        isSubmitting.value = false;
      }
    }
  });
};

</script>

<style scoped>
.board {
  background-color: #fff;
  padding: 15px 20px;
  margin-bottom: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.el-card {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08);
}
.view-form .el-form-item {
  margin-bottom: 10px;
}
.el-input-show {
  width: 70%;
}
.el-input-show :deep(.el-input__inner) {
  background-color: #f5f7fa;
  color: #606266;
  cursor: default;
}
.el-input-show-time {
  width: 120px;
  margin-right: 5px;
}
.el-input-show-time :deep(.el-input__inner) {
  background-color: #f5f7fa;
  color: #606266;
  cursor: default;
  text-align: center;
}
.time-separator {
  margin: 0 10px;
  color: #606266;
}
.hall-tag {
  margin: 0 8px 8px 0;
}
.button-icon {
  margin-right: 5px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
.hall-type-input-group {
  display: flex;
  margin-bottom: 10px;
}
.el-input-hall {
  flex-grow: 1;
  margin-right: 10px;
}
.hall-tags-container {
  margin-top: 10px;
}
.editable-hall-tag {
  margin: 0 8px 8px 0;
}
</style>