<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>User Management</el-breadcrumb-item>
        <el-breadcrumb-item>User Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="box-card">
      <el-row :gutter="20" class="filter-bar">
        <el-col :xs="24" :sm="6" :md="5">
          <el-input v-model="searchFilters.userName" placeholder="Enter username" clearable @clear="fetchUserList" @keyup.enter="fetchUserList"/>
        </el-col>
        <el-col :xs="24" :sm="6" :md="5">
          <el-input v-model="searchFilters.email" placeholder="Enter email" clearable @clear="fetchUserList" @keyup.enter="fetchUserList"/>
        </el-col>
        <el-col :xs="24" :sm="6" :md="5">
          <el-input v-model="searchFilters.phoneNumber" placeholder="Enter phone number" clearable @clear="fetchUserList" @keyup.enter="fetchUserList"/>
        </el-col>
        <el-col :xs="24" :sm="6" :md="5">
          <el-select v-model="searchFilters.sex" placeholder="Select gender" clearable style="width:100%;" @change="fetchUserList">
            <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="24" :md="4" class="search-button-col">
          <el-button :icon="Search" @click="fetchUserList" type="primary">Search</el-button>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="action-buttons-row">
        <el-col :span="24">
          <el-button type="primary" @click="openAddUserDialog" size="large">
            <i class="iconfont icon-add-button button-icon"></i> Add User
          </el-button>
          <el-button type="danger" @click="confirmBatchDeleteUsers" size="large" :disabled="selectedUsers.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>

      <el-table :data="userList" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoadingTable">
        <el-table-column type="selection" width="45" align="center"></el-table-column>
        <el-table-column type="index" label="#" width="50"></el-table-column>
        <el-table-column prop="userName" label="Username" width="150" sortable></el-table-column>
        <el-table-column label="Avatar" width="80" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="getAvatarPath(scope.row.userPicture)" />
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" show-overflow-tooltip></el-table-column>
        <el-table-column prop="phoneNumber" label="Phone Number" width="150"></el-table-column>
        <el-table-column label="Gender" width="120" align="center">
            <template #default="scope">
                <el-tag :type="getGenderTagType(scope.row.sex)">
                    {{ formatGender(scope.row.sex) }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="sysRole.roleName" label="Role" width="150" sortable></el-table-column>
        <el-table-column label="Actions" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Edit User" placement="top" :enterable="false" :open-delay="300">
              <el-button type="primary" @click="openEditUserDialog(scope.row.userId)" size="small" :icon="EditPen"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete User" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="confirmDeleteUser(scope.row.userId)" size="small" :icon="Delete"></el-button>
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
        :total="totalUserCount"
        background
      >
      </el-pagination>
    </el-card>

    <el-dialog :title="userDialogTitle" v-model="isUserFormDialogVisible" width="60%" @close="resetUserFormDialog" top="5vh">
      <el-form :model="currentUserForm" :rules="userFormRules" ref="userFormRef" label-width="150px">
        <el-form-item label="Username" prop="userName">
          <el-input v-model="currentUserForm.userName" placeholder="Enter username" :disabled="!isAddingUser"></el-input>
        </el-form-item>
        <el-form-item label="Password" prop="password" :required="isAddingUser">
          <el-input v-model="currentUserForm.password" type="password" show-password :placeholder="isAddingUser ? 'Enter password' : 'Enter new password (leave blank to keep current)'"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="currentUserForm.email" placeholder="Enter email address"></el-input>
        </el-form-item>
        <el-form-item label="Phone Number" prop="phoneNumber">
          <el-input v-model="currentUserForm.phoneNumber" placeholder="Enter phone number"></el-input>
        </el-form-item>
        <el-form-item label="Gender" prop="sex">
          <el-radio-group v-model="currentUserForm.sex">
            <el-radio :label="1">Male</el-radio>
            <el-radio :label="2">Female</el-radio>
            <el-radio :label="0">Prefer not to say</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Role" prop="roleId">
          <el-select v-model="currentUserForm.roleId" placeholder="Select role" clearable style="width:100%;">
            <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Avatar">
          <div class="avatar-selector">
            <img
              v-for="avatar in predefinedAvatars"
              :key="avatar.id"
              :src="avatar.path"
              :alt="'Avatar ' + avatar.name"
              @click="currentUserForm.userPicture = avatar.id"
              :class="['predefined-avatar', currentUserForm.userPicture === avatar.id ? 'selected' : '']"
            />
          </div>
          <p class="avatar-tip" v-if="isAddingUser && !currentUserForm.userPicture">Default avatar will be used if none selected.</p>
        </el-form-item>
         <el-form-item label="Birthday" prop="birthday">
            <el-date-picker
                v-model="currentUserForm.birthday"
                type="date"
                placeholder="Select birth date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width:100%;"
            />
        </el-form-item>
        <el-form-item label="Signature" prop="autograph">
            <el-input
                type="textarea"
                :rows="2"
                v-model="currentUserForm.autograph"
                placeholder="User signature"
            />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isUserFormDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitUserForm" :loading="isSubmittingForm">
            <i class="iconfont icon-sure-button button-icon"></i> {{ userFormSubmitButtonText }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus'; // Removed ElNotification
import { ArrowRight, EditPen, Delete, Plus, Search, UserFilled } from '@element-plus/icons-vue';

const isLoadingTable = ref(false);
const isSubmittingForm = ref(false);

const searchFilters = reactive({ userName: '', email: '', phoneNumber: '', sex: null });
const genderOptions = ref([
  { value: 1, label: 'Male' },
  { value: 2, label: 'Female' },
  { value: 0, label: 'Prefer not to say' }
]);
const roleOptions = ref([]);

const queryParameters = reactive({ userName: '', phoneNumber: '', email: '', sex: null, pageNum: 1, pageSize: 7 });
const userList = ref([]);
const totalUserCount = ref(0);
const selectedUsers = ref([]);

const isUserFormDialogVisible = ref(false);
const isAddingUser = ref(true);
const userDialogTitle = computed(() => isAddingUser.value ? 'Add New User' : 'Edit User Information');
const userFormSubmitButtonText = computed(() => isAddingUser.value ? 'Confirm' : 'Save Changes');

const predefinedAvatars = ref([
  { id: "0", path: "/avatars/avatar-0.png", name: "Default" },
  { id: "1", path: "/avatars/avatar-1.png", name: "Avatar 1" },
  { id: "2", path: "/avatars/avatar-2.png", name: "Avatar 2" },
  { id: "3", path: "/avatars/avatar-3.png", name: "Avatar 3" },
]);
const defaultAvatarPathForDisplay = "/avatars/avatar-default.png";

const getAvatarPath = (avatarId) => {
  const selected = predefinedAvatars.value.find(avatar => avatar.id === String(avatarId));
  return selected ? selected.path : defaultAvatarPathForDisplay;
};

const initialUserFormState = {
  userId: null, userName: '', password: '', email: '', phoneNumber: '',
  sex: 0, // Default to 'Prefer not to say'
  userPicture: "0", // Default avatar ID
  roleId: null,
  birthday: "",
  autograph: "",
};
const currentUserForm = reactive({ ...initialUserFormState });
const userFormRef = ref(null);


const checkEmailFormat = (rule, value, cb) => {
  const regEmail = /^([a-zA-Z0-9_.-])+@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
  if (value && value.trim() !== '' && !regEmail.test(value)) {
    cb(new Error('Please enter a valid email address'));
  } else {
    cb();
  }
};
const checkPhoneNumberFormat = (rule, value, cb) => {
  const regMobile = /^[0-9]{8,15}$/;
  if (value && value.trim() !== '' && !regMobile.test(value)) {
    cb(new Error('Phone number must be 8-15 digits.'));
  } else {
    cb();
  }
};

const userFormRules = reactive({
  userName: [{ required: true, message: 'Username is required', trigger: 'blur' }],
  password: [{ required: computed(() => isAddingUser.value), message: 'Password is required for new users', trigger: 'blur' }],
  email: [
    // Email is optional, but if provided, it must be valid
    { validator: checkEmailFormat, trigger: 'blur' }
  ],
  phoneNumber: [
    // Phone number is optional, but if provided, it must be valid
    { validator: checkPhoneNumberFormat, trigger: 'blur' }
  ],
  sex: [{ required: true, message: 'Please select gender', trigger: 'change' }],
  roleId: [{ required: true, message: 'Please select a role', trigger: 'change' }],
});

const formatGender = (sexValue) => {
  const gender = genderOptions.value.find(g => g.value === sexValue);
  return gender ? gender.label : 'Unknown';
};
const getGenderTagType = (sexValue) => {
    if (sexValue === 1) return 'primary'; // Male
    if (sexValue === 2) return 'success'; // Female (using success for different color)
    return 'info'; // Prefer not to say
};


const fetchUserList = async () => {
  isLoadingTable.value = true;
  queryParameters.userName = searchFilters.userName.trim();
  queryParameters.email = searchFilters.email.trim();
  queryParameters.phoneNumber = searchFilters.phoneNumber.trim();
  queryParameters.sex = searchFilters.sex;
  try {
    const { data: res } = await axios.get('sysUser', { params: queryParameters });
    if (res.code === 200) { userList.value = res.data; totalUserCount.value = res.total; }
    else { ElMessage.error(res.msg || 'Failed to fetch users.'); }
  } catch (error) { ElMessage.error('Error fetching users.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};

const fetchRoleOptions = async () => {
  try {
    const { data: res } = await axios.get('sysRole', { params: { pageSize: 1000 } });
    if (res.code === 200) { roleOptions.value = res.data.data || res.data || []; }
    else { ElMessage.error(res.msg || 'Failed to fetch roles.'); }
  } catch (error) { ElMessage.error('Error fetching roles.'); console.error(error); }
};

onMounted(() => { fetchUserList(); fetchRoleOptions(); });

const handlePageSizeChange = (newSize) => { queryParameters.pageSize = newSize; fetchUserList(); };
const handlePageNumChange = (newPage) => { queryParameters.pageNum = newPage; fetchUserList(); };
const handleTableSelectionChange = (selection) => { selectedUsers.value = selection; };

const resetUserFormDialog = () => {
  if (userFormRef.value) userFormRef.value.resetFields();
  Object.assign(currentUserForm, { ...initialUserFormState });
};

const openAddUserDialog = () => {
  isAddingUser.value = true; resetUserFormDialog(); isUserFormDialogVisible.value = true;
  nextTick(() => { if (userFormRef.value) userFormRef.value.clearValidate(); });
};

const openEditUserDialog = async (userId) => {
  isAddingUser.value = false; resetUserFormDialog();
  try {
    const { data: res } = await axios.get(`sysUser/${userId}`);
    if (res.code === 200 && res.data) {
      Object.assign(currentUserForm, res.data);
      currentUserForm.password = '';
      // Ensure userPicture is a string for comparison with avatar.id
      currentUserForm.userPicture = String(currentUserForm.userPicture || '0');
      currentUserForm.sex = Number.isInteger(currentUserForm.sex) ? currentUserForm.sex : 0; // Default if null/undefined
      isUserFormDialogVisible.value = true;
      nextTick(() => { if (userFormRef.value) userFormRef.value.clearValidate(); });
    } else { ElMessage.error(res.msg || 'Failed to fetch user details.'); }
  } catch (error) { ElMessage.error('Error fetching user details.'); }
};

const submitUserForm = async () => {
  if (!userFormRef.value) return;
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmittingForm.value = true;
      const payload = { ...currentUserForm };

      if (!isAddingUser.value && !payload.password) {
        delete payload.password; // Don't send empty password for update
      } else if (isAddingUser.value && !payload.password) {
        // This should be caught by 'required' rule, but as a safeguard for add
        ElMessage.error('Password is required for new users.');
        isSubmittingForm.value = false;
        return;
      }
      // Ensure birthday is null if empty string, to avoid DB issues with empty string for DATE type
      if (payload.birthday === '') {
          payload.birthday = null;
      }

      try {
        let response;
        if (isAddingUser.value) { response = await axios.post('sysUser', payload); }
        else { response = await axios.put('sysUser', payload); }
        const res = response.data;
        if (res.code === 200) { ElMessage.success(`User ${isAddingUser.value ? 'added' : 'updated'} successfully!`); isUserFormDialogVisible.value = false; fetchUserList(); }
        else { ElMessage.error(res.msg || `Failed to ${isAddingUser.value ? 'add' : 'update'} user.`); }
      } catch (error) {
         if (error.isAxiosError && error.response?.data?.msg) {
            ElMessage.error(error.response.data.msg);
        } else {
            ElMessage.error(`Error ${isAddingUser.value ? 'adding' : 'updating'} user.`);
        }
        console.error(error);
      }
      finally { isSubmittingForm.value = false; }
    }
  });
};

const confirmDeleteUser = async (userId) => {
  ElMessageBox.confirm('This will permanently delete the user. Continue?', 'Confirm Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true;
    try {
      const { data: res } = await axios.delete(`sysUser/${userId}`);
      if (res.code === 200) { ElMessage.success('User deleted successfully!'); fetchUserList(); }
      else { ElMessage.error(res.msg || 'Failed to delete user.'); }
    } catch (error) { ElMessage.error('Error deleting user.'); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Deletion cancelled.'));
};
const confirmBatchDeleteUsers = async () => {
  if (selectedUsers.value.length === 0) { ElMessage.warning('Please select users to delete.'); return; }
  ElMessageBox.confirm(`This will permanently delete ${selectedUsers.value.length} selected users. Continue?`, 'Confirm Batch Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning',})
  .then(async () => {
    isSubmittingForm.value = true; const ids = selectedUsers.value.map(u => u.userId);
    try {
      const { data: res } = await axios.delete(`sysUser/${ids.join(',')}`);
      if (res.code === 200) { ElMessage.success('Selected users deleted successfully!'); fetchUserList(); selectedUsers.value = []; }
      else { ElMessage.error(res.msg || 'Failed to batch delete users.'); }
    } catch (error) { ElMessage.error('Error during batch deletion.'); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Batch deletion cancelled.'));
};

</script>

<style scoped>
.board { background-color: #fff; padding: 15px 20px; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-card { box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08); }
.filter-bar { margin-bottom: 20px; display: flex; flex-wrap: wrap; align-items: center; }
.filter-bar .el-col { margin-bottom: 10px; padding-right: 10px !important; padding-left: 10px !important;}
.filter-bar .el-col:last-child { padding-right: 0 !important; }
.search-button-col { display: flex; align-items: center; }
.search-button-col .el-button { margin-left:0; }
.action-buttons-row { margin-bottom: 20px; }
.button-icon { margin-right: 5px; font-size: 1em; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.dialog-footer { display: flex; justify-content: flex-end; }
.el-table th { background-color: #f5f7fa; }

.avatar-selector {
  display: flex;
  gap: 10px; /* Spacing between avatars */
  flex-wrap: wrap; /* Allow avatars to wrap if container is too small */
}
.predefined-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s ease-in-out, opacity 0.2s ease-in-out;
  opacity: 0.7;
}
.predefined-avatar:hover {
  opacity: 1;
  border-color: #FF9800; /* Orange-500 like color */
}
.predefined-avatar.selected {
  opacity: 1;
  border-color: #F57C00; /* Darker orange */
  box-shadow: 0 0 8px rgba(245, 124, 0, 0.5);
}
.avatar-tip {
  font-size: 0.875rem;
  color: #909399;
  margin-top: 8px;
}
</style>