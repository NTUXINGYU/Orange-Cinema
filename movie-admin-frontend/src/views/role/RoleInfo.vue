<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Permission Management</el-breadcrumb-item>
        <el-breadcrumb-item>Role Management</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card>
      <el-row :gutter="20" class="action-bar">
        <el-col :span="24">
          <el-button type="primary" @click="openAddRoleDialog" size="large">
            <i class="iconfont icon-add-button button-icon"></i> Add Role
          </el-button>
          <el-button type="danger" @click="confirmBatchDeleteRoles" size="large" :disabled="selectedRoles.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>

      <el-table :data="roleList" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoadingTable">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="roleId" label="Role ID" width="120" sortable></el-table-column>
        <el-table-column prop="roleName" label="Role Name" width="200"></el-table-column>
        <el-table-column prop="roleDesc" label="Role Description" show-overflow-tooltip></el-table-column>
        <el-table-column label="Actions" width="180" fixed="right" align="center"> 
          <template #default="scope">
            <el-tooltip effect="dark" content="Edit Role" placement="top" :enterable="false" :open-delay="300">
              <el-button type="primary" @click="openEditRoleDialog(scope.row.roleId)" size="small" :icon="EditPen"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete Role" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="checkAndDeleteRole(scope.row.roleId)" size="small" :icon="Delete"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination-container"
        @size-change="handlePageSizeChange"
        @current-change="handlePageNumChange"
        :current-page="queryConfig.pageNum"
        :page-sizes="[5, 7, 10, 15]"
        :page-size="queryConfig.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalRolesCount"
        background
      >
      </el-pagination>
    </el-card>

    <el-dialog title="Add New Role" v-model="isAddDialogVisible" width="45%" @close="resetAddFormDialog">
      <el-form :model="addRoleForm" :rules="roleFormValidationRules" ref="addRoleFormRef" label-width="120px">
        <el-form-item label="Role Name" prop="roleName">
          <el-input v-model="addRoleForm.roleName" placeholder="Enter role name"></el-input>
        </el-form-item>
        <el-form-item label="Role Description" prop="roleDesc">
          <el-input v-model="addRoleForm.roleDesc" type="textarea" placeholder="Enter role description"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isAddDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitAddRole" :loading="isSubmittingForm">
             <i class="iconfont icon-sure-button button-icon"></i> Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="Edit Role" v-model="isEditDialogVisible" width="45%" @close="resetEditFormDialog">
      <el-form :model="editRoleForm" :rules="roleFormValidationRules" ref="editRoleFormRef" label-width="120px">
        <el-form-item label="Role ID" prop="roleId">
          <el-input v-model="editRoleForm.roleId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Role Name" prop="roleName">
          <el-input v-model="editRoleForm.roleName" placeholder="Enter role name"></el-input>
        </el-form-item>
        <el-form-item label="Role Description" prop="roleDesc">
          <el-input v-model="editRoleForm.roleDesc" type="textarea" placeholder="Enter role description"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer"> 
          <el-button @click="isEditDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitEditRole" :loading="isSubmittingForm">
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
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { ArrowRight, EditPen, Delete } from '@element-plus/icons-vue'; // Removed Setting icon

const isLoadingTable = ref(false);
const isSubmittingForm = ref(false);

const queryConfig = reactive({
  query: '',
  pageNum: 1,
  pageSize: 7
});
const totalRolesCount = ref(0);

const roleList = ref([]);
const selectedRoles = ref([]);

const isAddDialogVisible = ref(false);
const initialAddRoleFormState = { roleName: '', roleDesc: '' };
const addRoleForm = reactive({ ...initialAddRoleFormState });
const addRoleFormRef = ref(null);

const isEditDialogVisible = ref(false);
const editRoleForm = reactive({ roleId: null, roleName: '', roleDesc: '' });
const editRoleFormRef = ref(null);

const roleFormValidationRules = {
  roleName: [{ required: true, message: 'Please enter role name', trigger: 'blur' }],
  roleDesc: [{ required: true, message: 'Please enter role description', trigger: 'blur' }]
};


const fetchRoleList = async () => {
  isLoadingTable.value = true;
  try {
    const { data: res } = await axios.get('sysRole', { params: queryConfig });
    if (res.code === 200) {
      roleList.value = res.data;
      totalRolesCount.value = res.total;
    } else {
      ElMessage.error(res.msg || 'Failed to fetch roles.');
    }
  } catch (error) {
    ElMessage.error('Error fetching roles. Please try again.');
    console.error("Fetch roles error:", error);
  } finally {
    isLoadingTable.value = false;
  }
};

onMounted(() => {
  fetchRoleList();
});

const handlePageSizeChange = (newSize) => {
  queryConfig.pageSize = newSize;
  fetchRoleList();
};
const handlePageNumChange = (newPage) => {
  queryConfig.pageNum = newPage;
  fetchRoleList();
};

const openAddRoleDialog = () => {
  Object.assign(addRoleForm, initialAddRoleFormState);
  if (addRoleFormRef.value) addRoleFormRef.value.clearValidate();
  isAddDialogVisible.value = true;
};
const resetAddFormDialog = () => {
  if (addRoleFormRef.value) addRoleFormRef.value.resetFields();
};
const submitAddRole = async () => {
  if (!addRoleFormRef.value) return;
  await addRoleFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmittingForm.value = true;
      try {
        const { data: res } = await axios.post('sysRole', addRoleForm);
        if (res.code === 200) {
          ElMessage.success('Role added successfully!');
          isAddDialogVisible.value = false;
          fetchRoleList();
        } else {
          ElMessage.error(res.msg || 'Failed to add role.');
        }
      } catch (error) {
        ElMessage.error('Error adding role.');
      } finally {
        isSubmittingForm.value = false;
      }
    }
  });
};

const openEditRoleDialog = async (roleId) => {
  try {
    const { data: res } = await axios.get(`sysRole/${roleId}`);
    if (res.code === 200 && res.data) {
      Object.assign(editRoleForm, res.data);
      if (editRoleFormRef.value) editRoleFormRef.value.clearValidate();
      isEditDialogVisible.value = true;
    } else {
      ElMessage.error(res.msg || 'Failed to fetch role details.');
    }
  } catch (error) {
    ElMessage.error('Error fetching role details.');
  }
};
const resetEditFormDialog = () => {
  if (editRoleFormRef.value) editRoleFormRef.value.resetFields();
};
const submitEditRole = async () => {
  if (!editRoleFormRef.value) return;
  await editRoleFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmittingForm.value = true;
      try {
        const { data: res } = await axios.put('sysRole/', editRoleForm);
        if (res.code === 200) {
          ElMessage.success('Role updated successfully!');
          isEditDialogVisible.value = false;
          fetchRoleList();
        } else {
          ElMessage.error(res.msg || 'Failed to update role.');
        }
      } catch (error) {
        ElMessage.error('Error updating role.');
      } finally {
        isSubmittingForm.value = false;
      }
    }
  });
};

const handleTableSelectionChange = (selection) => {
  selectedRoles.value = selection;
};

const showDeletionBlockedNotification = (title, userIds) => {
  ElNotification({
    title: title,
    message: `Cannot delete role. It is associated with the following user ID(s): ${userIds}. Please reassign users before deleting.`,
    type: 'warning',
    duration: 0,
  });
};

const checkUsersForRole = async (roleId) => {
  try {
    const { data: res } = await axios.get('sysUser', { params: { roleId: roleId, pageNum: 1, pageSize: 1 } });
    if (res.code === 200 && res.total > 0) {
      const userCheckDetails = await axios.get('sysUser', { params: { roleId: roleId, pageNum: 1, pageSize: 5 } });
      const userIds = userCheckDetails.data.data.map(u => u.userId).join(', ');
      return userIds || 'one or more users';
    }
    return null;
  } catch (error) {
    console.error("Error checking users for role:", error);
    ElMessage.error('Could not verify role associations.');
    return 'verification_failed';
  }
};

const deleteRoleApiCall = async (roleIdOrIds) => {
    isSubmittingForm.value = true;
    try {
        const url = Array.isArray(roleIdOrIds) ? `sysRole/${roleIdOrIds.join(',')}` : `sysRole/${roleIdOrIds}`;
        const { data: res } = await axios.delete(url);
        if (res.code === 200) {
            ElMessage.success(`Role(s) deleted successfully!`);
            fetchRoleList();
            selectedRoles.value = [];
        } else {
            ElMessage.error(res.msg || 'Failed to delete role(s).');
        }
    } catch (error) {
        ElMessage.error('Error during role deletion.');
    } finally {
        isSubmittingForm.value = false;
    }
};

const checkAndDeleteRole = async (roleId) => {
  const associatedUserIds = await checkUsersForRole(roleId);
  if (associatedUserIds === 'verification_failed') return;

  if (associatedUserIds) {
    showDeletionBlockedNotification('Deletion Blocked', associatedUserIds);
  } else {
    ElMessageBox.confirm('This will permanently delete the role. Continue?', 'Confirm Deletion', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
    }).then(() => {
      deleteRoleApiCall(roleId);
    }).catch(() => {
      ElMessage.info('Deletion cancelled.');
    });
  }
};

const confirmBatchDeleteRoles = async () => {
  if (selectedRoles.value.length === 0) {
    ElMessage.warning('Please select roles to delete.');
    return;
  }
  let canDeleteAll = true;
  let blockingRoleInfo = [];
  isLoadingTable.value = true;
  for (const role of selectedRoles.value) {
    const associatedUserIds = await checkUsersForRole(role.roleId);
    if (associatedUserIds === 'verification_failed') {
         ElMessage.error("Verification failed for one or more roles. Batch delete aborted.");
         isLoadingTable.value = false;
         return;
    }
    if (associatedUserIds) {
      canDeleteAll = false;
      blockingRoleInfo.push(`Role '${role.roleName}' (ID: ${role.roleId}) is used by user(s): ${associatedUserIds}`);
    }
  }
  isLoadingTable.value = false;

  if (!canDeleteAll) {
    const message = `Cannot delete all selected roles due to associations:<ul>${blockingRoleInfo.map(info => `<li>${info}</li>`).join('')}</ul>Please reassign users first.`;
    ElMessageBox.alert(message, 'Batch Deletion Blocked', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: 'OK',
        type: 'warning'
    });
    return;
  }

  ElMessageBox.confirm(`This will permanently delete ${selectedRoles.value.length} selected role(s). Continue?`, 'Confirm Batch Deletion', {
    confirmButtonText: 'Delete',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => {
    const idsToDelete = selectedRoles.value.map(role => role.roleId);
    deleteRoleApiCall(idsToDelete);
  }).catch(() => {
    ElMessage.info('Batch deletion cancelled.');
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