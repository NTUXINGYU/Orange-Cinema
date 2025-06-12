<template>
  <div class="w-full bg-gray-800">
    <!-- Banner Section -->
    <div class="w-full bg-gray-900">
      <div class="relative w-full max-w-7xl mx-auto overflow-hidden">
        <div
          class="flex transition-transform duration-700 ease-in-out"
          :style="{ transform: `translateX(-${currentBanner * 100}%)` }"
        >
          <div 
            v-for="(banner, index) in banners"
            :key="index"
            class="w-full flex-shrink-0 bg-black flex justify-center items-center h-48 sm:h-64 md:h-80 lg:h-96"
          >
            <img
              :src="banner"
              alt="Banner Image"
              class="w-auto h-full object-contain"
            />
          </div>
        </div>
        <div class="absolute bottom-4 left-1/2 flex -translate-x-1/2 space-x-2 z-10">
          <span
            v-for="(_, index) in banners"
            :key="index"
            :class="[
              'block h-2 w-2 cursor-pointer rounded-full transition-all duration-300',
              currentBanner === index ? 'w-4 bg-white' : 'bg-gray-500 hover:bg-white'
            ]"
            @click="currentBanner = index"
          ></span>
        </div>
      </div>
    </div>

    <!-- Now Playing Movies -->
    <div class="p-4 md:p-6 text-white">
      <div class="container mx-auto">
        <h2 class="text-xl md:text-2xl mb-4 flex justify-between items-center font-bold">
          <span>Now Playing</span>
          <router-link to="/now-playing" class="text-sm text-gray-400 hover:text-orange-400 transition-colors">
            More >>
          </router-link>
        </h2>
        <div class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-4">
          <div 
            v-for="movie in NowPlayingMovies" 
            :key="movie.movieId" 
            class="bg-gray-900 rounded-lg overflow-hidden shadow-lg cursor-pointer group" 
            @click="goToDetail(movie.movieId)"
          >
            <img 
              :src="getMoviePosterUrl(movie.moviePoster)" 
              :alt="movie.movieName" 
              class="w-full aspect-[2/3] object-cover transition-transform duration-300 group-hover:scale-105"
            />
            <div class="p-3">
              <h3 class="text-base text-white truncate font-semibold" :title="movie.movieName">{{ movie.movieName }}</h3>
              <div class="text-xs text-gray-400 flex justify-between items-center mt-1">
                <span>{{ movie.releaseDate ? movie.releaseDate.substring(0, 4) : 'N/A' }}</span>
                <span class="flex items-center">
                  <svg class="w-3 h-3 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
                  {{ movie.movieVoteAverage != null ? movie.movieVoteAverage.toFixed(1) : 'N/A' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Coming Soon Movies -->
    <div class="p-4 md:p-6 text-white">
       <div class="container mx-auto">
        <h2 class="text-xl md:text-2xl mb-4 flex justify-between items-center font-bold">
          <span>Coming Soon</span>
          <router-link to="/popular" class="text-sm text-gray-400 hover:text-orange-400 transition-colors">
            More >>
          </router-link>
        </h2>
        <div class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-4">
            <div v-for="movie in ComingSoonMovies" :key="movie.movieId" class="bg-gray-900 rounded-lg overflow-hidden shadow-lg cursor-pointer group" @click="goToDetail(movie.movieId)">
              <img :src="getMoviePosterUrl(movie.moviePoster)" :alt="movie.movieName" class="w-full aspect-[2/3] object-cover transition-transform duration-300 group-hover:scale-105"/>
              <div class="p-3">
                <h3 class="text-base text-white truncate font-semibold" :title="movie.movieName">{{ movie.movieName }}</h3>
                <div class="text-xs text-gray-400 flex justify-between items-center mt-1">
                  <span>{{ movie.releaseDate ? movie.releaseDate.substring(0, 4) : 'N/A' }}</span>
                   <span class="flex items-center">
                    <svg class="w-3 h-3 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
                    {{ movie.movieVoteAverage != null ? movie.movieVoteAverage.toFixed(1) : 'N/A' }}
                  </span>
                </div>
              </div>
            </div>
        </div>
      </div>
    </div>

    <!-- Top Rated Movies -->
    <div class="p-4 md:p-6 text-white">
       <div class="container mx-auto">
        <h2 class="text-xl md:text-2xl mb-4 flex justify-between items-center font-bold">
          <span>Top Rated</span>
          <router-link to="/top_rated" class="text-sm text-gray-400 hover:text-orange-400 transition-colors">
            More >>
          </router-link>
        </h2>
        <div class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-4">
            <div v-for="movie in TopRatedMovies" :key="movie.movieId" class="bg-gray-900 rounded-lg overflow-hidden shadow-lg cursor-pointer group" @click="goToDetail(movie.movieId)">
              <img :src="getMoviePosterUrl(movie.moviePoster)" :alt="movie.movieName" class="w-full aspect-[2/3] object-cover transition-transform duration-300 group-hover:scale-105"/>
              <div class="p-3">
                <h3 class="text-base text-white truncate font-semibold" :title="movie.movieName">{{ movie.movieName }}</h3>
                <div class="text-xs text-gray-400 flex justify-between items-center mt-1">
                  <span>{{ movie.releaseDate ? movie.releaseDate.substring(0, 4) : 'N/A' }}</span>
                   <span class="flex items-center">
                    <svg class="w-3 h-3 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
                    {{ movie.movieVoteAverage != null ? movie.movieVoteAverage.toFixed(1) : 'N/A' }}
                  </span>
                </div>
              </div>
            </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import banner1 from "@/assets/banner1.png";
import banner2 from "@/assets/banner2.png";
import banner3 from "@/assets/banner3.png";

const router = useRouter();
const NowPlayingMovies = ref([]);
const ComingSoonMovies = ref([]);
const TopRatedMovies = ref([]);
const banners = ref([banner1, banner2, banner3]);
const currentBanner = ref(0);
let rotationInterval = null;

const API_BASE_URL = 'http://127.0.0.1:9231';
const IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/w500';

const getMoviePosterUrl = (posterPath) => {
  return posterPath ? `${IMAGE_BASE_URL}${posterPath}` : 'https://via.placeholder.com/200x300.png?text=No+Image';
};

const fetchMovies = async () => {
  const fetchRankingList = (listId) => {
    return axios.get(`${API_BASE_URL}/sysMovie/find/rankingList/${listId}`, {
      params: { pageNum: 1, pageSize: 6 }
    });
  };

  try {
    const [nowPlayingRes, comingSoonRes, topRatedRes] = await Promise.all([
      fetchRankingList(1),
      fetchRankingList(2),
      fetchRankingList(3)
    ]);

    NowPlayingMovies.value = nowPlayingRes.data.data;
    ComingSoonMovies.value = comingSoonRes.data.data;
    TopRatedMovies.value = topRatedRes.data.data;

  } catch (error) {
    console.error('Failed to get film information', error);
  }
};

function goToDetail(id) {
  router.push({ name: 'MovieDetail', params: { id } });
}

const startRotation = () => {
  rotationInterval = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % banners.value.length;
  }, 3000);
};

const stopRotation = () => {
  clearInterval(rotationInterval);
};

onMounted(() => {
  fetchMovies();
  startRotation();
});

onUnmounted(() => {
  stopRotation();
});
</script>
