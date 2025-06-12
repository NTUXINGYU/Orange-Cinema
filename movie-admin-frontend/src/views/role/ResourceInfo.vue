<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Permission Management</el-breadcrumb-item>
        <el-breadcrumb-item>Resource Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-card>
      <el-row :gutter="20" class="action-bar">
        <el-col :span="24">
          <el-button type="primary" @click="openAddDialog" size="large">
            <i class="iconfont icon-add-button button-icon"></i> Add Resource
          </el-button>
          <el-button type="danger" @click="handleMultipleDelete" size="large" :disabled="selectedResources.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>
      <el-table :data="resourceList" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoading">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="Resource ID" width="150" sortable></el-table-column>
        <el-table-column prop="name" label="Resource Name" width="200" sortable></el-table-column>
        <el-table-column prop="path" label="Resource Path" width="200"></el-table-column>
        <el-table-column prop="level" label="Permission Level" width="180" align="center">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.level === 1">Level 1</el-tag>
            <el-tag type="warning" v-else-if="scope.row.level === 2">Level 2</el-tag>
            <el-tag type="info" v-else>Level 3</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Parent Resource" width="200">
          <template #default="scope">
            {{ scope.row.parent ? scope.row.parent.name : 'N/A' }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Edit Resource" placement="top" :enterable="false" :open-delay="500">
              <el-button type="primary" @click="openEditDialog(scope.row.id)" size="small" :icon="EditPen"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete Resource" placement="top" :enterable="false" :open-delay="500">
              <el-button type="danger" @click="confirmDeleteResource(scope.row.id)" size="small" :icon="Delete"></el-button>
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
        :total="totalResourceCount"
        background
      >
      </el-pagination>
    </el-card>
    <el-dialog title="Add New Resource" v-model="isAddDialogVisible" width="50%" @close="resetAddFormDialog">
      <el-form :model="addResourceForm" :rules="resourceFormRules" ref="addFormRef" label-width="140px" label-position="right">
        <el-form-item label="Resource ID" prop="id">
          <el-input v-model="addResourceForm.id" placeholder="Enter resource ID (optional)"></el-input>
        </el-form-item>
        <el-form-item label="Resource Name" prop="name">
          <el-input v-model="addResourceForm.name" placeholder="Enter resource name"></el-input>
        </el-form-item>
        <el-form-item label="Resource Path" prop="path">
          <el-input v-model="addResourceForm.path" placeholder="Enter resource path (e.g., /users)"></el-input>
        </el-form-item>
        <el-form-item label="Parent Resource" prop="parentId">
          <el-select v-model="addResourceForm.parentId" placeholder="Select parent resource (optional)" clearable style="width: 100%;">
            <el-option
              v-for="item in availableParentResources"
              :key="item.id"
              :label="`${item.name} (ID: ${item.id})`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isAddDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitAddResource" :loading="isSubmitting">
            <i class="iconfont icon-sure-button button-icon"></i> Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog title="Edit Resource" v-model="isEditDialogVisible" width="50%" @close="resetEditFormDialog">
      <el-form :model="editResourceForm" :rules="resourceFormRules" ref="editFormRef" label-width="140px" label-position="right">
        <el-form-item label="Resource ID" prop="id">
          <el-input v-model="editResourceForm.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="Resource Name" prop="name">
          <el-input v-model="editResourceForm.name" placeholder="Enter resource name"></el-input>
        </el-form-item>
        <el-form-item label="Resource Path" prop="path">
          <el-input v-model="editResourceForm.path" placeholder="Enter resource path"></el-input>
        </el-form-item>
        <el-form-item label="Parent Resource" prop="parentId">
          <el-select v-model="editResourceForm.parentId" placeholder="Select parent resource (optional)" clearable style="width: 100%;">
            <el-option
              v-for="item in availableParentResources"
              :key="item.id"
              :label="`${item.name} (ID: ${item.id})`"
              :value="item.id"
              :disabled="item.id === editResourceForm.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isEditDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitEditResource" :loading="isSubmitting">
             <i class="iconfont icon-sure-button button-icon"></i> Save Changes
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowRight, EditPen, Delete } from '@element-plus/icons-vue';

const isLoading = ref(false);
const isSubmitting = ref(false);

const queryParameters = reactive({
  query: '',
  pageNum: 1,
  pageSize: 7
});

const resourceList = ref([]);
const availableParentResources = ref([]);
const totalResourceCount = ref(0);

const isAddDialogVisible = ref(false);
const isEditDialogVisible = ref(false);

const initialAddFormState = {
  id: '',
  name: '',
  path: '',
  level: 0,
  parentId: null
};
const addResourceForm = reactive({ ...initialAddFormState });

const editResourceForm = reactive({
  id: '',
  name: '',
  path: '',
  level: 0,
  parentId: null
});

const resourceFormRules = {
  name: [
    { required: true, message: 'Resource name is required', trigger: 'blur' }
  ],
  path: [
    { required: true, message: 'Resource path is required', trigger: 'blur' }
  ]
};

const addFormRef = ref(null);
const editFormRef = ref(null);

const selectedResources = ref([]);

const fetchResourceList = async () => {
  isLoading.value = true;
  try {
    const { data: response } = await axios.get('sysResource', { params: queryParameters });
    if (response.code === 200) {
      resourceList.value = response.data;
      totalResourceCount.value = response.total;
    } else {
      ElMessage.error(response.msg || 'Failed to fetch resource list.');
    }
  } catch (error) {
    console.error("Error fetching resource list:", error);
    ElMessage.error('Error fetching resource list. Please try again.');
  } finally {
    isLoading.value = false;
  }
};

const fetchAllResourcesForDropdown = async () => {
  try {
    const { data: response } = await axios.get('sysResource', { params: { pageSize: 1000 } });
    if (response.code === 200) {
      availableParentResources.value = response.data;
    } else {
      ElMessage.error(response.msg || 'Failed to fetch parent resources.');
    }
  } catch (error) {
    console.error("Error fetching all resources:", error);
    ElMessage.error('Error fetching parent resources.');
  }
};

onMounted(() => {
  fetchResourceList();
  fetchAllResourcesForDropdown();
});

const handlePageSizeChange = (newSize) => {
  queryParameters.pageSize = newSize;
  fetchResourceList();
};

const handlePageNumChange = (newPage) => {
  queryParameters.pageNum = newPage;
  fetchResourceList();
};

const openAddDialog = () => {
  Object.assign(addResourceForm, initialAddFormState);
  if (addFormRef.value) {
    addFormRef.value.clearValidate();
  }
  isAddDialogVisible.value = true;
};

const resetAddFormDialog = () => {
  if (addFormRef.value) {
    addFormRef.value.resetFields();
  }
  Object.assign(addResourceForm, initialAddFormState);
};

const submitAddResource = async () => {
  if (!addFormRef.value) return;
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      const payload = { ...addResourceForm };
      if (payload.parentId === null || payload.parentId === '') {
        payload.parentId = 0;
      }
      try {
        const { data: response } = await axios.post('sysResource', payload);
        if (response.code === 200) {
          ElMessage.success('Resource added successfully!');
          isAddDialogVisible.value = false;
          fetchResourceList();
          fetchAllResourcesForDropdown();
        } else {
          ElMessage.error(response.msg || 'Failed to add resource.');
        }
      } catch (error) {
        ElMessage.error('Error adding resource. Please try again.');
      } finally {
        isSubmitting.value = false;
      }
    }
  });
};

const openEditDialog = async (resourceId) => {
  try {
    const { data: response } = await axios.get(`sysResource/${resourceId}`);
    if (response.code === 200 && response.data) {
      Object.assign(editResourceForm, response.data);
      if (editResourceForm.parentId === 0) {
        editResourceForm.parentId = null;
      }
      if (editFormRef.value) {
        editFormRef.value.clearValidate();
      }
      isEditDialogVisible.value = true;
    } else {
      ElMessage.error(response.msg || 'Failed to fetch resource details.');
    }
  } catch (error) {
    ElMessage.error('Error fetching resource details.');
  }
};

const resetEditFormDialog = () => {
  if (editFormRef.value) {
    editFormRef.value.resetFields();
  }
};

const submitEditResource = async () => {
  if (!editFormRef.value) return;
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      const payload = { ...editResourceForm };
      if (payload.parentId === null || payload.parentId === '') {
        payload.parentId = 0;
      }
      try {
        const { data: response } = await axios.put('sysResource/', payload);
        if (response.code === 200) {
          ElMessage.success('Resource updated successfully!');
          isEditDialogVisible.value = false;
          fetchResourceList();
          fetchAllResourcesForDropdown();
        } else {
          ElMessage.error(response.msg || 'Failed to update resource.');
        }
      } catch (error) {
        ElMessage.error('Error updating resource. Please try again.');
      } finally {
        isSubmitting.value = false;
      }
    }
  });
};

const handleTableSelectionChange = (selection) => {
  selectedResources.value = selection;
};

const confirmDelete = async (title, message) => {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning',
    });
    return true;
  } catch (action) {
    if (action === 'cancel') {
      ElMessage.info('Deletion cancelled.');
    }
    return false;
  }
};

const confirmDeleteResource = async (resourceId) => {
  if (await confirmDelete('Delete Resource', 'This will permanently delete the resource. Continue?')) {
    try {
      isSubmitting.value = true;
      const { data: response } = await axios.delete(`sysResource/${resourceId}`);
      if (response.code === 200) {
        ElMessage.success('Resource deleted successfully!');
        fetchResourceList();
        fetchAllResourcesForDropdown();
      } else {
        ElMessage.error(response.msg || 'Failed to delete resource.');
      }
    } catch (error) {
      ElMessage.error('Error deleting resource.');
    } finally {
      isSubmitting.value = false;
    }
  }
};

const handleMultipleDelete = async () => {
  if (selectedResources.value.length === 0) {
    ElMessage.warning('Please select resources to delete.');
    return;
  }
  if (await confirmDelete('Batch Delete Resources', `This will permanently delete ${selectedResources.value.length} selected resources. Continue?`)) {
    const idsToDelete = selectedResources.value.map(item => item.id);
    try {
      isSubmitting.value = true;
      const { data: response } = await axios.delete(`sysResource/${idsToDelete.join(',')}`);
      if (response.code === 200) {
        ElMessage.success('Selected resources deleted successfully!');
        fetchResourceList();
        selectedResources.value = [];
        fetchAllResourcesForDropdown();
      } else {
        ElMessage.error(response.msg || 'Failed to delete selected resources.');
      }
    } catch (error) {
      ElMessage.error('Error during batch deletion.');
    } finally {
      isSubmitting.value = false;
    }
  }
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
.action-bar {
  margin-bottom: 20px;
}
.button-icon {
  margin-right: 5px;
  font-size: 1em; 
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
.el-select {
  width: 100%; 
}
.el-table th { 
  background-color: #f5f7fa;
}
</style>