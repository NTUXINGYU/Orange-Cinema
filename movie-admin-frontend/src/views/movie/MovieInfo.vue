<template>
  <div>
    <div class="board">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item :to="{ path: '/welcome' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Movie Management</el-breadcrumb-item>
        <el-breadcrumb-item>Movie Information</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="box-card">
      <el-row :gutter="20" class="filter-bar">
        <el-col :xs="24" :sm="10" :md="8">
          <el-input v-model="searchMovieName" placeholder="Enter movie title" clearable @clear="fetchMovieList" @keyup.enter="fetchMovieList"/>
        </el-col>
        <el-col :xs="24" :sm="14" :md="10">
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="To"
            start-placeholder="Start release date"
            end-placeholder="End release date"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleDateRangeSearchChange"
            style="width: 100%;"
          ></el-date-picker>
        </el-col>
        <el-col :xs="24" :sm="24" :md="6" class="search-button-col">
          <el-button class="el-button-search" :icon="Search" @click="fetchMovieList" type="primary">Search</el-button>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="action-buttons-row">
        <el-col :span="24">
          <el-button type="primary" @click="openAddMovieDialog" size="large">
            <i class="iconfont icon-add-button button-icon"></i> Add Movie
          </el-button>
          <el-button type="danger" @click="checkAndBatchDeleteMovies" size="large" :disabled="selectedMovies.length === 0">
            <i class="iconfont icon-delete-button button-icon"></i> Batch Delete
          </el-button>
        </el-col>
      </el-row>

      <el-table :data="movieList" style="width: 100%" border stripe @selection-change="handleTableSelectionChange" v-loading="isLoadingTable">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="movieId" label="ID" width="80"></el-table-column> 
        <el-table-column prop="movieName" label="Title" width="250" show-overflow-tooltip></el-table-column> 
        <el-table-column prop="movieLength" label="Duration (min)" width="150"></el-table-column> 
        <el-table-column label="Release Date" width="150"> 
            <template #default="scope">
                {{ formatTableDate(scope.row.releaseDate) }}
            </template>
        </el-table-column>
        <el-table-column prop="movieTagline" label="Tagline" show-overflow-tooltip></el-table-column>
        <el-table-column label="Actions" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-tooltip effect="dark" content="Edit Movie" placement="top" :enterable="false" :open-delay="300">
              <el-button type="primary" @click="checkAndOpenEditDialog(scope.row.movieId)" size="small" :icon="EditPen"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Delete Movie" placement="top" :enterable="false" :open-delay="300">
              <el-button type="danger" @click="checkAndConfirmDeleteMovie(scope.row.movieId)" size="small" :icon="Delete"></el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="Manage Categories" placement="top" :enterable="false" :open-delay="300">
              <el-button type="warning" @click="openManageCategoriesDialog(scope.row)" size="small" :icon="Tickets"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination-container"
        @size-change="handlePageSizeChange"
        @current-change="handlePageNumChange"
        :current-page="queryParameters.pageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="queryParameters.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalMovieCount"
        background
      >
      </el-pagination>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="isMovieFormDialogVisible" width="55%" @close="resetMovieFormDialog" top="5vh">
      <el-form :model="currentMovieForm" :rules="movieFormRules" ref="movieFormRef" label-width="160px">
        <el-form-item label="Movie ID" prop="movieId">
          <el-input
            v-model="currentMovieForm.movieId"
            placeholder="Enter Movie ID (Numbers only)"
            :disabled="!isAddingMovie"
            @input="restrictMovieIdToNumbers"
          ></el-input>
        </el-form-item>
        <el-form-item label="Movie Title" prop="movieName">
          <el-input v-model="currentMovieForm.movieName" placeholder="Enter movie title"></el-input>
        </el-form-item>
        <el-form-item label="Duration (minutes)" prop="movieLength">
          <el-input-number v-model="currentMovieForm.movieLength" :min="0" placeholder="Film running time"></el-input-number>
        </el-form-item>
        <el-form-item label="Poster Path" prop="moviePoster">
          <el-input
            v-model="currentMovieForm.moviePoster"
            placeholder="TMDB relative path, e.g., /qVK74wz1L1tUa5TJSdCA2pCgK2h.jpg">
          </el-input>
          <el-image
            v-if="currentMovieForm.moviePoster && currentMovieForm.moviePoster.startsWith('/')"
            :src="`https://image.tmdb.org/t/p/w200${currentMovieForm.moviePoster}`"
            style="width:60px; height: 90px; margin-top:5px;"
            fit="contain"
            lazy
          >
            <template #error>
              <div class="image-slot-error">Load failed</div>
            </template>
          </el-image>
        </el-form-item>
        <el-form-item label="Release Date" prop="releaseDate">
          <el-date-picker
            v-model="currentMovieForm.releaseDate"
            type="date"
            placeholder="Select release date"
            value-format="YYYY-MM-DD"
            style="width:100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="Popularity" prop="moviePopularity">
          <el-input-number v-model="currentMovieForm.moviePopularity" :precision="3" :step="0.001" placeholder="Enter Popularity Index"></el-input-number>
        </el-form-item>
        <el-form-item label="Vote Average" prop="movieVoteAverage">
          <el-input-number v-model="currentMovieForm.movieVoteAverage" :precision="3" :step="0.001" :min="0" :max="10" placeholder="Enter Vote Average"></el-input-number>
        </el-form-item>
        <el-form-item label="Vote Count" prop="movieVoteCount">
          <el-input-number v-model="currentMovieForm.movieVoteCount" :min="0" placeholder="Enter Vote Count"></el-input-number>
        </el-form-item>
        <el-form-item label="Introduction" prop="movieIntroduction">
          <el-input v-model="currentMovieForm.movieIntroduction" type="textarea" :rows="3" placeholder="Brief introduction of the movie"></el-input>
        </el-form-item>
        <el-form-item label="Genres (comma-sep)" prop="movieGenres">
          <el-input v-model="currentMovieForm.movieGenres" placeholder="e.g., Action,Adventure,Science Fiction"></el-input>
        </el-form-item>
        <el-form-item label="Tagline" prop="movieTagline">
          <el-input v-model="currentMovieForm.movieTagline" placeholder="Enter Movie tagline"></el-input>
        </el-form-item>
        <el-form-item prop="onlineUrl">
          <template #label>
            <span>Online Play URL</span>
            <el-tooltip effect="dark" placement="top">
              <template #content>
                Provide the full HLS (.m3u8) URL for online viewing.<br/>
                Leave blank if online viewing is not available.
              </template>
              <el-icon style="margin-left: 4px; cursor: pointer;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-input
            v-model="currentMovieForm.onlineUrl"
            placeholder="/media/movie.m3u8"
            clearable>
          </el-input>
        </el-form-item>
        <el-form-item label="Director(s)" prop="movieDirector">
          <el-input v-model="currentMovieForm.movieDirector" placeholder="Enter Director"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isMovieFormDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitMovieForm" :loading="isSubmittingForm">
            <i class="iconfont icon-sure-button button-icon"></i> {{ formSubmitButtonText }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="Manage Movie Categories" v-model="isManageCategoriesDialogVisible" width="50%" @close="resetManageCategoriesDialog">
      <p v-if="editingMovieForCategories">Managing categories for: <strong>{{ editingMovieForCategories.movieName }}</strong></p>
      <el-form label-width="130px" style="margin-top:20px;">
         <el-form-item label="Current Categories" class="current-categories-item">
           <div v-if="currentMovieAssociatedCategories && currentMovieAssociatedCategories.length > 0" class="tags-wrapper">
             <el-tag
               v-for="category in currentMovieAssociatedCategories"
               :key="category.movieCategoryId"
               closable
               @close="removeCategoryFromMovie(category.movieCategoryId)"
               style="margin-right: 5px; margin-bottom: 5px;"
             >
               {{ category.movieCategoryName }}
             </el-tag>
           </div>
           <div v-else class="empty-categories-text">
             No categories currently assigned to this movie.
           </div>
         </el-form-item>
         <el-form-item label="Add Category" class="add-category-item">
            <el-select
              v-model="selectedCategoryForAddition"
              placeholder="Select category to add"
              clearable
              filterable
              style="width: calc(100% - 100px); margin-right: 10px;"
            >
              <el-option
                v-for="catOption in availableCategoryOptionsForMovie"
                :key="catOption.movieCategoryId"
                :label="catOption.movieCategoryName"
                :value="catOption.movieCategoryId"
              ></el-option>
            </el-select>
            <el-button type="primary" @click="addCategoryToMovie" :disabled="!selectedCategoryForAddition">Add</el-button>
         </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isManageCategoriesDialogVisible = false">Close</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import { ArrowRight, EditPen, Delete, Tickets, Search, QuestionFilled } from '@element-plus/icons-vue';

const isLoadingTable = ref(false);
const isSubmittingForm = ref(false);

const searchMovieName = ref('');
const searchDateRange = ref([]);

const queryParameters = reactive({
  movieId: '', movieArea: '', movieName: '', startDate: '', endDate: '', pageNum: 1, pageSize: 10
});
const movieList = ref([]);
const totalMovieCount = ref(0);
const selectedMovies = ref([]);

const isMovieFormDialogVisible = ref(false);
const isAddingMovie = ref(true);

const dialogTitle = computed(() => isAddingMovie.value ? 'Add New Movie' : 'Edit Movie Information');
const formSubmitButtonText = computed(() => isAddingMovie.value ? 'Confirm' : 'Save Changes');

const initialMovieFormState = {
  movieId: '', movieName: '', movieLength: null, moviePoster: '', releaseDate: null,
  moviePopularity: null, movieVoteAverage: null, movieVoteCount: null,
  movieIntroduction: '', movieGenres: '', delState: 0, movieTagline: '', movieDirector: '',
  onlineUrl: ''
};
const currentMovieForm = reactive({ ...initialMovieFormState });
const movieFormRef = ref(null);

const movieFormRules = {
  movieId: [ { required: true, message: 'Movie ID is required', trigger: 'blur' }, { pattern: /^[0-9]+$/, message: 'Movie ID must consist of numbers only', trigger: 'blur' } ],
  movieName: [{ required: true, message: 'Please enter movie title', trigger: 'blur' }],
  movieLength: [ { type: 'number', message: 'Duration must be a number', trigger: 'blur' }, { validator: (rule, value, callback) => { if (value !== null && value < 0) { callback(new Error('Duration cannot be negative')); } else { callback(); } }, trigger: 'blur' } ],
  releaseDate: [{ required: true, message: 'Please select release date', trigger: 'change' }],
  moviePoster: [{ type: 'string', message: 'Please enter the poster path', trigger: 'blur' }],
  onlineUrl: [{ type: 'string', message: 'Please enter a valid URL format', trigger: 'blur' }]
};

const isManageCategoriesDialogVisible = ref(false);
const editingMovieForCategories = ref(null);
const currentMovieAssociatedCategories = ref([]);
const allMovieCategories = ref([]);
const selectedCategoryForAddition = ref(null);

const restrictMovieIdToNumbers = (value) => { if (value) { currentMovieForm.movieId = value.replace(/[^0-9]/g, ''); } };
const formatTableDate = (dateTimeString) => { if (!dateTimeString) return 'N/A'; return dateTimeString.split(' ')[0]; };

const fetchMovieList = async () => {
  isLoadingTable.value = true;
  queryParameters.movieName = searchMovieName.value.trim();
  queryParameters.movieArea = '';
  queryParameters.startDate = searchDateRange.value?.[0] ? `${searchDateRange.value[0]} 00:00:00` : '';
  queryParameters.endDate = searchDateRange.value?.[1] ? `${searchDateRange.value[1]} 23:59:59` : '';
  try {
    const { data: res } = await axios.get('sysMovie/find', { params: queryParameters });
    if (res.code === 200) { movieList.value = res.data; totalMovieCount.value = res.total; }
    else { ElMessage.error(res.msg || 'Failed to fetch movies.'); }
  } catch (error) { ElMessage.error('Error fetching movies.'); console.error(error); }
  finally { isLoadingTable.value = false; }
};
const fetchAllMovieCategories = async () => {
  try {
    const { data: res } = await axios.get('sysMovieCategory/find');
    if (res.code === 200) { allMovieCategories.value = res.data.data || res.data || []; }
    else { ElMessage.error(res.msg || 'Failed to fetch all movie categories.'); }
  } catch (error) { ElMessage.error('Error fetching all movie categories.'); console.error(error); }
};
const handleDateRangeSearchChange = () => { if (searchDateRange.value === null || (Array.isArray(searchDateRange.value) && searchDateRange.value.length === 2) || searchDateRange.value.length === 0) { fetchMovieList(); } };

onMounted(() => { fetchMovieList(); fetchAllMovieCategories(); });

const handlePageSizeChange = (newSize) => { queryParameters.pageSize = newSize; fetchMovieList(); };
const handlePageNumChange = (newPage) => { queryParameters.pageNum = newPage; fetchMovieList(); };

const resetMovieFormDialog = () => {
  if (movieFormRef.value) movieFormRef.value.resetFields();
  Object.assign(currentMovieForm, { ...initialMovieFormState });
  if(isAddingMovie.value) { currentMovieForm.movieId = ''; }
};
const openAddMovieDialog = () => { isAddingMovie.value = true; resetMovieFormDialog(); isMovieFormDialogVisible.value = true; nextTick(() => { if (movieFormRef.value) movieFormRef.value.clearValidate(); }); };
const openEditMovieDialog = async (movieId) => {
  isAddingMovie.value = false; resetMovieFormDialog();
  try {
    const { data: res } = await axios.get(`sysMovie/find/${movieId}`);
    if (res.code === 200 && res.data) {
      Object.assign(currentMovieForm, res.data);
      currentMovieForm.movieId = String(currentMovieForm.movieId || '');
      currentMovieForm.onlineUrl = currentMovieForm.onlineUrl || '';
      if (currentMovieForm.releaseDate && currentMovieForm.releaseDate.includes(' ')) { currentMovieForm.releaseDate = currentMovieForm.releaseDate.split(' ')[0]; }
      isMovieFormDialogVisible.value = true;
      nextTick(() => { if (movieFormRef.value) movieFormRef.value.clearValidate(); });
    } else { ElMessage.error(res.msg || 'Failed to fetch movie details.'); }
  } catch (error) { ElMessage.error('Error fetching movie details.'); console.error(error); }
};

const submitMovieForm = async () => {
  if (!movieFormRef.value) return;
  await movieFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmittingForm.value = true;
      try {
        const payload = { ...currentMovieForm };
        if (payload.onlineUrl === '') {
          payload.onlineUrl = null;
        }
        if (payload.movieId) { payload.movieId = Number(payload.movieId); }
        payload.movieLength = payload.movieLength === null ? null : Number(payload.movieLength);
        payload.moviePopularity = payload.moviePopularity === null ? null : Number(payload.moviePopularity);
        payload.movieVoteAverage = payload.movieVoteAverage === null ? null : Number(payload.movieVoteAverage);
        payload.movieVoteCount = payload.movieVoteCount === null ? null : Number(payload.movieVoteCount);
        let response;
        if (isAddingMovie.value) { response = await axios.post('sysMovie', payload); }
        else { response = await axios.put('sysMovie', payload); }
        const res = response.data;
        if (res.code === 200) { ElMessage.success(`Movie ${isAddingMovie.value ? 'added' : 'updated'} successfully!`); isMovieFormDialogVisible.value = false; fetchMovieList(); }
        else { ElMessage.error(res.msg || `Failed to ${isAddingMovie.value ? 'add' : 'update'} movie.`); }
       } catch (error) {
        console.error(`Movie form submission error (${isAddingMovie.value ? 'add' : 'update'}):`, error);
        if (error.isAxiosError && error.response?.data?.msg) {
            ElMessage.error(error.response.data.msg);
        } else if (error.isAxiosError && error.response && !error.response.data?.msg) {
             ElMessage.error(`Request failed: Server responded with status ${error.response.status}`);
        } else if (!error.isAxiosError) {
            ElMessage.error(`An unexpected client-side error occurred.`);
        }
      }
      finally { isSubmittingForm.value = false; }
    }
  });
};

const checkMovieSessions = async (movieId) => {
  try {
    const { data: res } = await axios.get('sysSession/isAbleEdit', { params: { movieId } });
    if (res.code === 200 && res.data && res.data.length > 0) { const sessionIds = res.data.map(s => s.sessionId).join(', '); ElNotification({ title: 'Action Blocked', message: `This movie has active sessions (ID(s): ${sessionIds}). Please resolve them before proceeding.`, type: 'warning', duration: 0 }); return true; }
    return false;
  } catch (error) { ElMessage.error('Failed to check movie sessions.'); console.error(error); return true; }
};
const checkAndOpenEditDialog = async (movieId) => { if (await checkMovieSessions(movieId)) return; openEditMovieDialog(movieId); };
const checkAndConfirmDeleteMovie = async (movieId) => {
  if (await checkMovieSessions(movieId)) return;
  ElMessageBox.confirm('This will permanently delete the movie. Continue?', 'Confirm Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning', })
  .then(async () => {
    isSubmittingForm.value = true;
    try {
      const { data: res } = await axios.delete(`sysMovie/${movieId}`);
      if (res.code === 200) { ElMessage.success('Movie deleted successfully!'); fetchMovieList(); }
      else { ElMessage.error(res.msg || 'Failed to delete movie.'); }
    } catch (error) { ElMessage.error('Error deleting movie.'); console.error(error); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Deletion cancelled.'));
};
const handleTableSelectionChange = (selection) => { selectedMovies.value = selection; };
const checkAndBatchDeleteMovies = async () => {
  if (selectedMovies.value.length === 0) { ElMessage.warning('Please select movies to delete.'); return; }
  isLoadingTable.value = true; let canDeleteAll = true;
  for (const movie of selectedMovies.value) { if (await checkMovieSessions(movie.movieId)) { canDeleteAll = false; break; } }
  isLoadingTable.value = false; if (!canDeleteAll) return;
  ElMessageBox.confirm(`This will permanently delete ${selectedMovies.value.length} selected movies. Continue?`, 'Confirm Batch Deletion', { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning', })
  .then(async () => {
    isSubmittingForm.value = true; const ids = selectedMovies.value.map(m => m.movieId);
    try {
      const { data: res } = await axios.delete(`sysMovie/${ids.join(',')}`);
      if (res.code === 200) { ElMessage.success('Selected movies deleted successfully!'); fetchMovieList(); selectedMovies.value = []; }
      else { ElMessage.error(res.msg || 'Failed to batch delete movies.'); }
    } catch (error) { ElMessage.error('Error during batch deletion.'); console.error(error); }
    finally { isSubmittingForm.value = false; }
  }).catch(() => ElMessage.info('Batch deletion cancelled.'));
};

const resetManageCategoriesDialog = () => { editingMovieForCategories.value = null; currentMovieAssociatedCategories.value = []; selectedCategoryForAddition.value = null; };
const openManageCategoriesDialog = async (movie) => {
  resetManageCategoriesDialog();
  editingMovieForCategories.value = { movieId: movie.movieId, movieName: movie.movieName };
  isManageCategoriesDialogVisible.value = true;
  isSubmittingForm.value = true;
  try {
    const { data: movieDetailsRes } = await axios.get(`sysMovie/find/${movie.movieId}`);
    if (movieDetailsRes.code === 200 && movieDetailsRes.data && movieDetailsRes.data.movieCategoryList) {
      currentMovieAssociatedCategories.value = movieDetailsRes.data.movieCategoryList;
    } else { currentMovieAssociatedCategories.value = []; ElMessage.error(movieDetailsRes.msg || 'Failed to fetch current movie categories.');}
  } catch (error) { currentMovieAssociatedCategories.value = []; ElMessage.error('Error fetching current movie categories.'); console.error(error); }
  finally { isSubmittingForm.value = false; }
};

const availableCategoryOptionsForMovie = computed(() => {
  const currentCategoryIds = new Set(currentMovieAssociatedCategories.value.map(cat => cat.movieCategoryId));
  return allMovieCategories.value.filter(option => !currentCategoryIds.has(option.movieCategoryId));
});

const addCategoryToMovie = async () => {
  if (!editingMovieForCategories.value?.movieId || !selectedCategoryForAddition.value) { ElMessage.warning('Please select a movie and a category to add.'); return; }
  isSubmittingForm.value = true;
  try {
    const { data: res } = await axios.post(`sysMovieToCategory/${editingMovieForCategories.value.movieId}/${selectedCategoryForAddition.value}`);
    if (res.code === 200) {
      ElMessage.success('Category added successfully!');
      const addedCategory = allMovieCategories.value.find(cat => cat.movieCategoryId === selectedCategoryForAddition.value);
      if (addedCategory) { currentMovieAssociatedCategories.value.push(addedCategory); }
      selectedCategoryForAddition.value = null;
    } else { ElMessage.error(res.msg || 'Failed to add category.'); }
  } catch (error) { ElMessage.error('Error adding category.'); console.error(error); }
  finally { isSubmittingForm.value = false; }
};

const removeCategoryFromMovie = async (categoryIdToRemove) => {
  if (!editingMovieForCategories.value?.movieId) return;
  try {
    await ElMessageBox.confirm('Are you sure you want to remove this category from the movie?', 'Confirm Removal', { confirmButtonText: 'Remove', cancelButtonText: 'Cancel', type: 'warning'} );
    isSubmittingForm.value = true;
    const { data: res } = await axios.delete(`sysMovieToCategory/${editingMovieForCategories.value.movieId}/${categoryIdToRemove}`);
    if (res.code === 200) {
      ElMessage.success('Category removed successfully!');
      currentMovieAssociatedCategories.value = currentMovieAssociatedCategories.value.filter(cat => cat.movieCategoryId !== categoryIdToRemove);
    } else { ElMessage.error(res.msg || 'Failed to remove category.'); }
  } catch (actionOrError) {
    if (actionOrError === 'cancel' || actionOrError === 'close') { ElMessage.info('Removal cancelled.'); }
  } finally {
    isSubmittingForm.value = false;
  }
};

</script>

<style scoped>
.board { background-color: #fff; padding: 15px 20px; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-card { box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08); }
.filter-bar { margin-bottom: 20px; display: flex; flex-wrap: wrap; align-items: flex-end; }
.filter-bar .el-col { margin-bottom: 10px; }
.el-date-picker { width: 100%; }
.search-button-col { display: flex; align-items: flex-end; }
.el-button-search { margin-left: 10px; }
.action-buttons-row { margin-bottom: 20px; }
.button-icon { margin-right: 5px; font-size: 1em; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.dialog-footer { display: flex; justify-content: flex-end; }
.el-table th { background-color: #f5f7fa; }
.image-slot-error { display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; font-size: 14px; }
.current-categories-item .tags-wrapper { margin-bottom: 10px; min-height: 50px; }
.add-category-item { margin-top: 15px; }
.empty-categories-text { color: #909399; font-size: 14px; line-height: 32px; padding: 5px 0; }
</style>