<template>
  <div class="bg-gray-800 min-h-screen">
    <header class="p-3 bg-gray-900 text-white flex items-center justify-between sticky top-0 z-20 shadow-lg">
      <button 
        @click="goBack" 
        class="text-sm bg-gray-700 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded-full flex items-center transition-colors"
      >
        <el-icon class="mr-2"><ArrowLeftBold /></el-icon>
        Back
      </button>
      <div class="text-sm">
        <ol class="flex items-center space-x-2">
          <li class="text-gray-400">Cinema /</li>
          <li class="text-white font-semibold">Buy Tickets</li>
        </ol>
      </div>
    </header>
    
    <div class="container mx-auto p-4 text-white">
      <div class="mb-8">
        <h2 class="text-xl font-bold mb-4">Select a Date</h2>
        <div class="flex gap-4">
          <button
            v-for="dateInfo in availableDates" :key="dateInfo.value"
            @click="selectDate(dateInfo.value)"
            :class="[
              'px-6 py-3 rounded-lg text-base font-semibold transition-all duration-200 flex-1 md:flex-none', 
              selectedDate === dateInfo.value ? 'bg-orange-500 text-white shadow-lg scale-105' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            ]"
          >
            {{ dateInfo.label }}
          </button>
        </div>
      </div>

      <div v-if="isLoading" class="text-center p-20">
        <el-icon class="is-loading text-4xl text-orange-500 mb-4"><Loading /></el-icon>
        <p class="text-gray-400">Loading movies for {{ selectedDate }}...</p>
      </div>

      <div v-else-if="moviesOnSelectedDate.length > 0">
        <h2 class="text-xl font-bold mb-4">Movies Playing on {{ selectedDate === todayString ? 'Today' : 'Tomorrow' }}</h2>
        <div class="flex gap-6 pb-4 overflow-x-auto">
          <div v-for="movie in moviesOnSelectedDate" :key="movie.movieId" class="flex-shrink-0 cursor-pointer group text-center w-32 sm:w-40" @click="selectMovie(movie)">
            <img
              :src="getTmdbImageUrl(movie.moviePoster)" :alt="movie.movieName"
              :class="[
                'w-full aspect-[2/3] object-cover rounded-lg shadow-lg transition-all duration-300',
                'group-hover:opacity-100',
                selectedMovie && selectedMovie.movieId === movie.movieId ? 'opacity-100' : 'opacity-70'
              ]"
            />
            <h3 
              :class="[
                'mt-3 text-sm font-semibold truncate transition-colors',
                selectedMovie && selectedMovie.movieId === movie.movieId ? 'text-orange-400' : 'text-white'
              ]"
            >{{ movie.movieName }}</h3>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center p-20 bg-gray-900 bg-opacity-50 rounded-lg">
          <el-icon class="text-5xl text-gray-600 mb-4"><Calendar /></el-icon>
          <p class="text-gray-400">No movies are scheduled for this date.</p>
          <p class="text-sm text-gray-500">Please check back later or select another date.</p>
      </div>
      
      <transition name="fade">
        <div v-if="selectedMovie" class="mt-12">
            <!-- [核心修改] 使用 v-if/v-else 来切换显示场次列表或提示信息 -->
            <div v-if="filteredAndTimedSessions.length > 0" class="bg-gray-900 bg-opacity-50 p-4 sm:p-6 rounded-2xl shadow-xl">
              <div class="flex items-baseline gap-3 mb-6">
                <h2 class="text-2xl font-bold">Sessions for <span class="text-orange-400">{{ selectedMovie.movieName }}</span></h2>
                <span class="text-gray-400 text-sm">({{ selectedMovie.movieLength }} min)</span>
              </div>
              
              <div class="space-y-4">
                <div v-for="session in filteredAndTimedSessions" :key="session.sessionId" 
                     class="flex flex-col sm:flex-row items-stretch sm:items-center bg-gray-800 p-4 rounded-lg hover:bg-gray-700 transition-colors"
                >
                  <div class="flex-grow flex items-center mb-4 sm:mb-0">
                    <div class="w-24 text-left">
                      <p class="text-xl font-bold text-white">{{ session.playTime }}</p>
                      <p class="text-xs text-gray-400">Ends {{ session.endTime }}</p>
                    </div>
                    <div class="pl-4 border-l border-gray-600">
                      <p class="font-semibold text-white">{{ session.sysHall.hallName }}</p>
                      <el-tag size="small" effect="dark" class="mt-1">{{ session.sysHall.hallCategory }}</el-tag>
                    </div>
                  </div>
                  
                  <div class="flex items-center justify-between sm:justify-end gap-4">
                    <div class="text-right sm:w-32">
                      <div v-if="session.discountRate && session.discountRate < 1" class="flex flex-col items-end">
                          <span class="text-orange-400 text-xl font-bold">¥{{ calculateDiscountPrice(session.sessionPrice, session.discountRate) }}</span>
                          <div class="flex items-baseline gap-2">
                             <span class="text-gray-500 line-through text-xs">¥{{ session.sessionPrice.toFixed(2) }}</span>
                             <el-tag type="danger" size="small" effect="light">{{ calculateDiscountPercentage(session.discountRate) }}% OFF</el-tag>
                          </div>
                      </div>
                      <span v-else class="text-orange-400 text-xl font-bold">¥{{ session.sessionPrice.toFixed(2) }}</span>
                    </div>

                    <button @click="selectSession(session)" class="bg-orange-600 text-white px-5 py-2 rounded-full font-bold hover:bg-orange-500 transition-colors shadow-lg flex-shrink-0">
                      Select
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <!-- [核心修改] 这是当没有可选场次时显示的提示信息 -->
            <div v-else class="text-center py-10 text-gray-400 mt-8 bg-gray-900 bg-opacity-50 p-6 rounded-2xl shadow-xl">
                <p>All available sessions for <span class="font-semibold text-white">{{ selectedMovie.movieName }}</span> have ended for today.</p>
                <p class="text-sm mt-2">Please check back tomorrow!</p>
            </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ArrowLeftBold, Loading, Calendar } from '@element-plus/icons-vue';

const API_BASE_URL = "http://localhost:9231";
const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const router = useRouter();

const availableDates = ref([]);
const selectedDate = ref('');
const todayString = ref('');
const isLoading = ref(true);

const allSessionsOnDate = ref([]);
const moviesOnSelectedDate = ref([]);
const selectedMovie = ref(null);

const getTmdbImageUrl = (posterPath) => {
  if (!posterPath) return 'https://via.placeholder.com/200x300.png?text=No+Poster'; 
  return `${TMDB_IMAGE_BASE_URL}${posterPath}`;
};

const calculateDiscountPrice = (price, rate) => {
  return (price * rate).toFixed(2);
};

const calculateDiscountPercentage = (rate) => {
  return Math.round((1 - rate) * 100); 
};

const formatDateToValue = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const generateAvailableDates = () => {
  const today = new Date();
  const tomorrow = new Date();
  tomorrow.setDate(today.getDate() + 1);
  
  todayString.value = formatDateToValue(today);
  
  availableDates.value = [
    { label: `Today, ${today.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}`, value: todayString.value },
    { label: `Tomorrow, ${tomorrow.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}`, value: formatDateToValue(tomorrow) }
  ];
};

const fetchSessionsByDate = async (date) => {
  if (!date) return;
  isLoading.value = true;
  allSessionsOnDate.value = [];
  moviesOnSelectedDate.value = [];
  selectedMovie.value = null;

  try {
    const { data: response } = await axios.get(`${API_BASE_URL}/sysSession`, { params: { sessionDate: date } });
    if (response.code === 200) {
      allSessionsOnDate.value = response.data;
      extractMoviesFromSessions(response.data);
    } else {
      ElMessage.error(response.msg || `Failed to fetch sessions for ${date}.`);
    }
  } catch (error) {
    ElMessage.error('Network error while fetching sessions.');
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const extractMoviesFromSessions = (sessions) => {
  if (!sessions || sessions.length === 0) {
    moviesOnSelectedDate.value = [];
    return;
  }
  
  const movieMap = new Map();
  sessions.forEach(session => {
    // [核心修改] 移除 isSessionValid 的判断。只要有排片，就提取电影。
    if (session.sysMovie && !movieMap.has(session.sysMovie.movieId)) {
      if (!session.sysMovie.movieCategoryList) {
        session.sysMovie.movieCategoryList = [];
      }
      movieMap.set(session.sysMovie.movieId, session.sysMovie);
    }
  });

  moviesOnSelectedDate.value = Array.from(movieMap.values());
  
  if (moviesOnSelectedDate.value.length > 0) {
    selectMovie(moviesOnSelectedDate.value[0]);
  }
};

const selectDate = (date) => {
  if (selectedDate.value !== date) {
      selectedDate.value = date;
  }
};

const selectMovie = (movie) => {
  selectedMovie.value = movie;
};

const filteredAndTimedSessions = computed(() => {
  if (!selectedMovie.value) return [];
  
  const now = new Date();
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
  
  return allSessionsOnDate.value
    .filter(session => {
      const isMovieMatch = session.movieId === selectedMovie.value.movieId;
      if (!isMovieMatch) return false;

      // [逻辑保留] 过滤逻辑依然只在这里生效，保证场次列表是准确的
      if (selectedDate.value === todayString.value) {
        return session.playTime > currentTime;
      }
      
      return true;
    })
    .map(session => {
      if (session.playTime && selectedMovie.value?.movieLength) {
          const [hours, minutes] = session.playTime.split(':').map(Number);
          const startTime = new Date();
          startTime.setHours(hours, minutes, 0, 0);
          const endTimeDate = new Date(startTime.getTime() + selectedMovie.value.movieLength * 60000);
          return {
              ...session,
              endTime: `${String(endTimeDate.getHours()).padStart(2, '0')}:${String(endTimeDate.getMinutes()).padStart(2, '0')}`
          };
      }
      return session;
    })
    .sort((a, b) => a.playTime.localeCompare(b.playTime));
});


const selectSession = (session) => {
  if (session && session.sessionId) {
    router.push({ name: 'ChooseSeat', params: { sessionId: session.sessionId } });
  } else {
    console.error("Attempted to navigate with invalid session:", session);
    ElMessage.error("Sorry, something went wrong with this session. Please try another one.");
  }
};

watch(selectedDate, (newDate, oldDate) => {
  if (newDate && newDate !== oldDate) {
    fetchSessionsByDate(newDate);
  }
});

onMounted(() => {
  generateAvailableDates();
  selectedDate.value = todayString.value;
});
</script>

<style scoped>
.overflow-x-auto::-webkit-scrollbar { height: 8px; }
.overflow-x-auto::-webkit-scrollbar-track { background: #2d3748; }
.overflow-x-auto::-webkit-scrollbar-thumb { background: #4a5568; border-radius: 4px; }
.overflow-x-auto::-webkit-scrollbar-thumb:hover { background: #718096; }

.el-tag--small {
    padding: 0 6px;
    height: 20px;
    line-height: 18px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>