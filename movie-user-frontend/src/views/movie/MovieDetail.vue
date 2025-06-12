<template>
    <header class="p-3 bg-gray-800 text-white flex items-center justify-between">
       <button 
        @click="goBack" 
        class="text-sm bg-gray-700 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded-full flex items-center transition-colors"
      >
        <el-icon class="mr-2"><ArrowLeftBold /></el-icon>
        Back
      </button>
  
      <div class="text-sm">
        <ol class="flex items-center space-x-2">
        <li class="text-gray-400">Movies</li>
          <li class="text-gray-400">/</li>
          <li class="text-white font-semibold">
            {{ currentMovieTitle }}
          </li>
        </ol>
      </div>
    </header>

    <div class="p-3 bg-gray-800 text-white min-h-fit" v-if="movie && !fetchError">
        <div class="flex flex-col md:flex-row items-center md:items-start w-full max-w-5xl mx-auto md:space-x-16">
            <div class="w-64 h-96 overflow-hidden rounded shadow-md flex-shrink-0 mb-4 md:mb-0">
                <img :src="'https://image.tmdb.org/t/p/w500' + movie.moviePoster"
                    :alt="movie.movieName"
                    class="w-full h-full object-cover"
                    v-if="movie.moviePoster" />
                <div v-else class="w-full h-full bg-gray-700 flex items-center justify-center">
                    <span class="text-gray-400">No Poster</span>
                </div>
            </div>
            <div class="md:ml-8 flex-1 mt-3 md:mt-0">
                <p><strong class="text-3xl pt-6 pb-3 block">{{ movie.movieName }}</strong></p>
                <p><em class="pb-6 block">{{ movie.tagline || movie.movieTagline }}</em></p>
                <p><strong>Genre: </strong>
                    <span v-if="movie.movieCategoryList && movie.movieCategoryList.length > 0">
                        <span v-for="(category, index) in movie.movieCategoryList" :key="category.categoryId || index"> 
                            {{ category.movieCategoryName || category.name }}{{ index < movie.movieCategoryList.length - 1 ? ', ' : '' }}
                        </span>
                    </span>
                    <span v-else>N/A</span>
                </p>
                <p><strong>Release: </strong>{{ (movie.releaseDate || '').split(" ")[0] }}</p>
                <p><strong>Running Time: </strong>{{ movie.movieLength || movie.runtime }} minutes</p>
                <p><strong>Director: </strong>{{ movie.movieDirector || 'N/A' }}</p> 
                <p><strong>Rating: ★ </strong>{{ movie.movieVoteAverage || movie.vote_average || 'N/A' }} / 10 
                   <span v-if="movie.movieVoteCount || movie.vote_count">(Rated by {{ movie.movieVoteCount || movie.vote_count }} people)</span>
                </p>
                <div class="mt-8">
                    <el-button v-if="isCheckingSessions" class="w-full md:w-64 h-12 text-lg" type="primary" :loading="true" disabled>Checking Availability...</el-button>
                    <el-button v-else-if="hasSessions" class="w-full md:w-64 h-12 text-lg" type="primary" @click="toChooseSession">Buy Tickets</el-button>
                    <el-button v-else class="w-full md:w-64 h-12 text-lg" type="info" plain disabled>No Sessions Available</el-button>

                    <div v-if="movie && movie.onlineUrl" class="mt-3 w-full md:w-64 h-12 flex items-center justify-center">
                        <el-tag type="success" size="large" effect="dark" class="w-full h-full flex items-center justify-center">
                        <el-icon class="mr-1"><VideoCameraFilled /></el-icon>
                        Online Sync Playback Available
                        </el-tag>
                    </div>
                </div>
            </div>
        </div>

        <div class="mt-8 max-w-5xl mx-auto">
            <h2 class="text-xl font-semibold mb-4 text-white">Synopsis</h2>
            <p class="mb-4 text-white">{{ movie.movieIntroduction || movie.overview || 'No synopsis available.' }}</p>
            
            <div v-if="movie">
                <h2 class="text-xl font-semibold mb-4 text-white">Cast</h2>
                <div v-if="isLoadingSupplementary" class="text-gray-400 mb-4 p-4 bg-gray-700 rounded-lg flex justify-center items-center min-h-[10rem]">
                    Loading cast...
                </div>
                <div v-else-if="cast && cast.length > 0" class="flex overflow-x-auto mb-4 space-x-4 p-4 bg-gray-700 rounded-lg">
                    <div v-for="actor in cast" :key="actor.id || actor.cast_id" class="flex flex-col items-center w-32 flex-shrink-0">
                        <div class="relative w-24 h-24 rounded-full shadow-md mb-2">
                            <img
                                v-if="actor.profile_path"
                                :src="'https://image.tmdb.org/t/p/w185' + actor.profile_path"
                                :alt="actor.name"
                                class="w-full h-full object-cover rounded-full"
                            />
                            <div v-else class="flex justify-center items-center w-full h-full bg-gray-500 rounded-full">
                                <el-icon class="text-gray-300 text-4xl">
                                    <PictureFilled />
                                </el-icon>
                            </div>
                        </div>
                        <p class="text-center text-sm font-medium text-white">{{ actor.name }}</p>
                        <p class="text-center text-xs text-gray-300">{{ actor.character }}</p>
                    </div>
                </div>
                <div v-else class="text-gray-400 mb-4 p-4 bg-gray-700 rounded-lg flex justify-center items-center min-h-[5rem]">
                    No cast information available.
                </div>
            </div>

            <div v-if="movie">
                <h2 class="text-xl font-semibold mb-4 text-white">Production Companies</h2>
                 <div v-if="isLoadingSupplementary" class="text-gray-400 mb-4 p-4 bg-gray-700 rounded-lg flex justify-center items-center min-h-[10rem]">
                    Loading production companies...
                </div>
                <div v-else-if="companies && companies.length > 0" class="flex overflow-x-auto mb-4 space-x-4 p-4 bg-gray-700 rounded-lg">
                    <div v-for="company in companies" :key="company.id" class="flex flex-col items-center w-40 flex-shrink-0">
                        <div class="min-w-[9rem] min-h-[7.5rem] flex flex-col items-center justify-center">
                            <div v-if="company.logo_path" class="overflow-hidden shadow-md mb-2 w-[9rem] h-[4rem] bg-white p-1 rounded flex items-center justify-center">
                                <img 
                                    :src="'https://image.tmdb.org/t/p/w200' + company.logo_path"
                                    :alt="company.name"
                                    class="object-contain max-w-full max-h-full"
                                />
                            </div>
                            <div v-else class="min-w-[9rem] min-h-[4rem] bg-gray-500 mb-2 flex justify-center items-center shadow-md rounded">
                                <el-icon class="text-gray-300 text-4xl">
                                    <PictureFilled />
                                </el-icon>
                            </div>
                            <div class="w-36 max-w-full text-center justify-center">
                                <p class="text-sm font-medium text-white overflow-wrap break-word">{{ company.name }}</p>
                            </div>
                        </div>
                    </div>
                </div>
                 <div v-else class="text-gray-400 mb-4 p-4 bg-gray-700 rounded-lg flex justify-center items-center min-h-[5rem]">
                    No production company information available.
                </div>
            </div>
        </div>
    </div>
    <div v-else-if="isLoading && !fetchError" class="p-3 bg-gray-800 text-white min-h-screen flex justify-center items-center">
        <p class="text-center text-xl">Loading movie details...</p>
    </div>
    <div v-else-if="fetchError" class="p-3 bg-gray-800 text-white min-h-screen flex justify-center items-center">
        <p class="text-center text-red-500 text-xl">
            Failed to load movie details. <br/>
            <span class="text-sm">{{ fetchError }}</span>
        </p>
    </div>
    <div v-else-if="!movie && !isLoading && !fetchError" class="p-3 bg-gray-800 text-white min-h-screen flex justify-center items-center">
         <p class="text-center text-xl">No movie data available or movie not found.</p>
    </div>
</template>


<script setup>
import { onMounted, ref, watch, defineProps } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { PictureFilled } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const movie = ref(null);
const companies = ref([]);
const cast = ref([]);
const currentMovieTitle = ref("Loading...");
const isLoading = ref(true);
const isLoadingSupplementary = ref(true);
const fetchError = ref(null);

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  }
});
const backendBaseUrl = 'http://127.0.0.1:9231';

const hasSessions = ref(false); 
const isCheckingSessions = ref(true);

const fetchMovieDetail = async (movieIdParam) => {
    isLoading.value = true;
    isLoadingSupplementary.value = true;
    fetchError.value = null;
    movie.value = null;
    companies.value = [];
    cast.value = [];
    currentMovieTitle.value = "Loading...";
    hasSessions.value = false;
    isCheckingSessions.value = true;

    const movieId = movieIdParam || route.params.id;

    if (!movieId) {
        fetchError.value = "Movie ID is missing.";
        isLoading.value = false;
        isLoadingSupplementary.value = false;
        isCheckingSessions.value = false;
        currentMovieTitle.value = "Error";
        return;
    }

    const sysMoviePromise = axios.get(`${backendBaseUrl}/sysMovie/find/${movieId}`)
        .then(response => {
            if (response.data && response.data.data) {
                const sysData = response.data.data;
                movie.value = {
                    movieId: sysData.movieId,
                    movieName: sysData.movieName,
                    moviePoster: sysData.moviePoster,
                    tagline: sysData.movieTagline,
                    movieCategoryList: sysData.movieCategoryList || [],
                    releaseDate: sysData.releaseDate,
                    movieLength: sysData.movieLength,
                    movieDirector: sysData.movieDirector,
                    movieVoteAverage: sysData.movieVoteAverage,
                    movieVoteCount: sysData.movieVoteCount,
                    movieIntroduction: sysData.movieIntroduction,
                    onlineUrl: sysData.onlineUrl,
                };
                currentMovieTitle.value = movie.value.movieName || "Movie Detail";
            }
        })
        .catch(error => {
            console.error("Failed to fetch from local DB:", error);
        });

    const sessionCheckPromise = axios.get(`${backendBaseUrl}/sysSession/${movieId}`)
        .then(response => {
            if (response.data && response.data.code === 200 && response.data.data.length > 0) {
                hasSessions.value = true;
            } else {
                hasSessions.value = false;
            }
        })
        .catch(error => {
            console.error("Failed to check for movie sessions:", error);
            hasSessions.value = false;
        })
        .finally(() => {
            isCheckingSessions.value = false;
        });

    const tmdbDetailPromise = axios.get(`${backendBaseUrl}/tmdb/movie/${movieId}`)
        .then(response => {
            if (response.data) {
                const tmdbData = response.data;
                companies.value = tmdbData.production_companies || [];

                if (movie.value) {
                    if (!movie.value.moviePoster && tmdbData.poster_path) movie.value.moviePoster = tmdbData.poster_path;
                    if (!movie.value.tagline && tmdbData.tagline) movie.value.tagline = tmdbData.tagline;
                    if ((!movie.value.movieCategoryList || movie.value.movieCategoryList.length === 0) && tmdbData.genres) {
                        movie.value.movieCategoryList = tmdbData.genres.map(g => ({ categoryId: g.id, movieCategoryName: g.name, name: g.name }));
                    }
                    if (!movie.value.movieVoteAverage && tmdbData.vote_average) movie.value.movieVoteAverage = tmdbData.vote_average.toFixed(1);
                    if (!movie.value.movieVoteCount && tmdbData.vote_count) movie.value.movieVoteCount = tmdbData.vote_count;
                    if (!movie.value.movieLength && tmdbData.runtime) movie.value.movieLength = tmdbData.runtime;
                    if (!movie.value.movieIntroduction && tmdbData.overview) movie.value.movieIntroduction = tmdbData.overview;
                    if (!movie.value.releaseDate && tmdbData.release_date) movie.value.releaseDate = tmdbData.release_date + " 00:00:00";
                    if (!movie.value.movieName && (tmdbData.title || tmdbData.original_title)) {
                        movie.value.movieName = tmdbData.title || tmdbData.original_title;
                        currentMovieTitle.value = movie.value.movieName;
                    }
                } else {
                    movie.value = {
                        movieId: movieId,
                        movieName: tmdbData.title || tmdbData.original_title,
                        moviePoster: tmdbData.poster_path,
                        tagline: tmdbData.tagline,
                        movieVoteAverage: tmdbData.vote_average?.toFixed(1),
                        movieVoteCount: tmdbData.vote_count,
                        movieIntroduction: tmdbData.overview,
                        releaseDate: tmdbData.release_date ? tmdbData.release_date + " 00:00:00" : "N/A 00:00:00",
                        movieLength: tmdbData.runtime,
                        movieCategoryList: tmdbData.genres?.map(g => ({ categoryId: g.id, movieCategoryName: g.name, name: g.name })) || [],
                    };
                    currentMovieTitle.value = movie.value.movieName || "Movie Detail";
                }
            }
        })
        .catch(e => {
            console.error("Failed to fetch from TMDB details:", e);
            if (!movie.value) {
                fetchError.value = fetchError.value || `Failed to load supplementary TMDB details: ${e.message}`;
            }
        });

    const creditsPromise = axios.get(`${backendBaseUrl}/tmdb/movie/${movieId}/credits`)
        .then(response => {
            if (response.data) {
                cast.value = response.data.cast || [];
                const director = response.data.crew?.find(person => person.department === "Directing" && person.job === "Director");
                if (movie.value && director && !movie.value.movieDirector) {
                    movie.value.movieDirector = director.name;
                }
            }
        })
        .catch(e => {
            console.error("Failed to fetch from TMDB credits:", e);
        });

    try {
        await Promise.allSettled([sysMoviePromise, sessionCheckPromise, tmdbDetailPromise, creditsPromise]);
    } finally {
        isLoading.value = false;
        isLoadingSupplementary.value = false;

        if (!movie.value && !fetchError.value) {
            fetchError.value = "Essential movie information could not be retrieved.";
            currentMovieTitle.value = "Error";
        } else if (movie.value && !movie.value.movieId) {
            movie.value.movieId = movieId;
        }
    }
};

function goBack() {
    router.go(-1);
}

function toChooseSession() {
    if (movie.value && movie.value.movieId) {
        router.push({ name: "ChooseSession", params: { movieId: movie.value.movieId } });
    } else if (movie.value && route.params.id) {
        router.push({ name: "ChooseSession", params: { movieId: route.params.id } });
    } else {
    }
}

onMounted(() => {
    fetchMovieDetail(props.id);
});

watch(() => props.id, (newId, oldId) => {
    if (newId && newId !== oldId) {
        fetchMovieDetail(newId);
    }
}, { immediate: false });

</script>