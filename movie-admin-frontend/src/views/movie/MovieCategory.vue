<template>
  <div>
    <!-- Breadcrumb Navigation -->
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Movie Management</el-breadcrumb-item>
        <el-breadcrumb-item>Movie Categories</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Card View -->
    <el-card class="box-card">
      <el-row :gutter="20" class="action-bar">
        <el-col :span="24">
          <el-button type="primary" @click="openAddCategoryDialog" size="large">
            <i class="iconfont icon-detail-button button-icon"></i> Add Category
          </el-button>
          <el-button type="danger" @click="handleBatchDeleteCategories" size="large" :disabled="selectedCategories.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete Categories
          </el-button>
        </el-col>
      </el-row>

      <!-- Category List Table -->
      <el-table :data="movieCategoryList" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoading">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="movieCategoryId" label="Category ID" width="180" sortable></el-table-column>
        <el-table-column prop="movieCategoryName" label="Category Name" sortable></el-table-column>
        <el-table-column label="Actions" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Edit Category" placement="top" :enterable="false" :open-delay="300">
              <el-button type="primary" @click="openEditCategoryDialog(scope.row.movieCategoryId)" size="small" :icon="EditPen"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete Category" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="confirmDeleteCategory(scope.row.movieCategoryId)" size="small" :icon="Delete"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <el-pagination
        class="pagination-container"
        @size-change="handlePageSizeChange"
        @current-change="handlePageNumChange"
        :current-page="queryParameters.pageNum"
        :page-sizes="[5, 7, 10, 15]"
        :page-size="queryParameters.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalCategoriesCount"
        background
      >
      </el-pagination>
    </el-card>

    <!-- Add Category Dialog -->
    <el-dialog title="Add New Category" v-model="isAddDialogVisible" width="40%" @close="resetAddFormDialog">
      <el-form :model="addCategoryForm" :rules="categoryFormRules" ref="addCategoryFormRef" label-width="140px">
        <el-form-item label="Category Name" prop="movieCategoryName">
          <el-input v-model="addCategoryForm.movieCategoryName" placeholder="Enter category name"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isAddDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitAddCategory" :loading="isSubmitting">
             <i class="iconfont icon-sure-button button-icon"></i> Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Edit Category Dialog -->
    <el-dialog title="Edit Category" v-model="isEditDialogVisible" width="40%" @close="resetEditFormDialog">
      <el-form :model="editCategoryForm" :rules="categoryFormRules" ref="editCategoryFormRef" label-width="140px">
        <el-form-item label="Category ID">
          <el-input v-model="editCategoryForm.movieCategoryId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Category Name" prop="movieCategoryName">
          <el-input v-model="editCategoryForm.movieCategoryName" placeholder="Enter category name"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isEditDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitEditCategory" :loading="isSubmitting">
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

const movieCategoryList = ref([]);
const totalCategoriesCount = ref(0);
const selectedCategories = ref([]); // For batch delete

const isAddDialogVisible = ref(false);
const initialAddFormState = { movieCategoryName: '' };
const addCategoryForm = reactive({ ...initialAddFormState });
const addCategoryFormRef = ref(null);

const isEditDialogVisible = ref(false);
const editCategoryForm = reactive({ movieCategoryId: null, movieCategoryName: '' });
const editCategoryFormRef = ref(null);

const categoryFormRules = {
  movieCategoryName: [
    { required: true, message: 'Please enter the movie category name', trigger: 'blur' }
  ]
};

const fetchMovieCategoryList = async () => {
  isLoading.value = true;
  try {
    const { data: response } = await axios.get('sysMovieCategory/find', { params: queryParameters });
    if (response.code === 200) {
      movieCategoryList.value = response.data;
      totalCategoriesCount.value = response.total;
    } else {
      ElMessage.error(response.msg || 'Failed to fetch movie categories.');
    }
  } catch (error) {
    ElMessage.error('Error fetching movie categories. Please try again.');
    console.error("Fetch categories error:", error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchMovieCategoryList);

const handlePageSizeChange = (newSize) => {
  queryParameters.pageSize = newSize;
  fetchMovieCategoryList();
};

const handlePageNumChange = (newPage) => {
  queryParameters.pageNum = newPage;
  fetchMovieCategoryList();
};

const resetAddFormDialog = () => {
  if (addCategoryFormRef.value) {
    addCategoryFormRef.value.resetFields();
  }
  Object.assign(addCategoryForm, initialAddFormState); 
};

const openAddCategoryDialog = () => {
  Object.assign(addCategoryForm, initialAddFormState);
  if (addCategoryFormRef.value) addCategoryFormRef.value.clearValidate();
  isAddDialogVisible.value = true;
};

const submitAddCategory = async () => {
  if (!addCategoryFormRef.value) return;
  await addCategoryFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        const { data: response } = await axios.post('sysMovieCategory', addCategoryForm);
        if (response.code === 200) {
          ElMessage.success('Movie category added successfully!');
          isAddDialogVisible.value = false;
          fetchMovieCategoryList();
        } else {
          ElMessage.error(response.msg || 'Failed to add movie category.');
        }
      } catch (error) {
        ElMessage.error('Error adding movie category.');
      } finally {
        isSubmitting.value = false;
      }
    }
  });
};

const openEditCategoryDialog = async (categoryId) => {
  try {
    const { data: response } = await axios.get(`sysMovieCategory/${categoryId}`);
    if (response.code === 200 && response.data) {
      Object.assign(editCategoryForm, response.data);
      if (editCategoryFormRef.value) editCategoryFormRef.value.clearValidate();
      isEditDialogVisible.value = true;
    } else {
      ElMessage.error(response.msg || 'Failed to fetch category details.');
    }
  } catch (error) {
    ElMessage.error('Error fetching category details.');
  }
};

const resetEditFormDialog = () => {
  if (editCategoryFormRef.value) {
    editCategoryFormRef.value.resetFields();
  }
};

const submitEditCategory = async () => {
  if (!editCategoryFormRef.value) return;
  await editCategoryFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        const { data: response } = await axios.put('sysMovieCategory', editCategoryForm);
        if (response.code === 200) {
          ElMessage.success('Movie category updated successfully!');
          isEditDialogVisible.value = false;
          fetchMovieCategoryList();
        } else {
          ElMessage.error(response.msg || 'Failed to update movie category.');
        }
      } catch (error) {
        ElMessage.error('Error updating movie category.');
      } finally {
        isSubmitting.value = false;
      }
    }
  });
};

const handleTableSelectionChange = (selection) => {
  selectedCategories.value = selection;
};

const confirmDeletion = async (itemDescription = "this item") => {
   try {
    await ElMessageBox.confirm(`This action will permanently delete ${itemDescription}. Continue?`, 'Confirm Deletion', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning',
    });
    return true;
  } catch {
    ElMessage.info('Deletion cancelled.');
    return false;
  }
};

const deleteCategoryApiCall = async (ids) => {
    isSubmitting.value = true;
    try {
        const url = `sysMovieCategory/${Array.isArray(ids) ? ids.join(',') : ids}`;
        const { data: response } = await axios.delete(url);
        if (response.code === 200) {
            ElMessage.success('Movie category/categories deleted successfully!');
            fetchMovieCategoryList();
            selectedCategories.value = []; 
        } else {
            ElMessage.error(response.msg || 'Failed to delete movie category/categories.');
        }
    } catch (error) {
        ElMessage.error('Error during category deletion.');
    } finally {
        isSubmitting.value = false;
    }
};

const confirmDeleteCategory = async (categoryId) => {
  if (await confirmDeletion(`category ID: ${categoryId}`)) {
    deleteCategoryApiCall(categoryId);
  }
};

const handleBatchDeleteCategories = async () => {
  if (selectedCategories.value.length === 0) {
    ElMessage.warning('Please select categories to delete.');
    return;
  }
  if (await confirmDeletion(`${selectedCategories.value.length} selected categories`)) {
    const idsToDelete = selectedCategories.value.map(item => item.movieCategoryId);
    deleteCategoryApiCall(idsToDelete);
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
.el-table th {
  background-color: #f5f7fa;
}
</style>