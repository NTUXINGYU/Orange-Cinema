<template>
    <main class="flex-grow bg-gray-800 text-white p-6">
        <div class="container mx-auto">
            <h2 class="text-2xl mb-4">Coming Soon</h2>
            <div>
                <div v-if="isLoading" class="text-center py-10">
                    <p>Loading...</p>
                </div>
                <div v-else-if="errorMsg" class="text-center py-10 text-red-400">
                    <p>{{ errorMsg }}</p>
                </div>
                <div v-else-if="movies.length === 0" class="text-center text-gray-400">
                    No top rated movies found.
                </div>
                <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
                    <div v-for="movie in movies" :key="movie.movieId" class="bg-gray-900 p-4 rounded cursor-pointer transform hover:scale-105 transition-transform duration-200 flex flex-col" @click="goToDetail(movie.movieId)">
                        <img
                            :src="movie.moviePoster ? 'https://image.tmdb.org/t/p/w500' + movie.moviePoster : '/placeholder-poster.png'"
                            :alt="movie.movieName"
                            class="w-full h-64 object-cover rounded mb-2 bg-gray-700 flex-shrink-0"
                            @error="($event.target.src = '/placeholder-poster.png')"
                        />
                        <div class="flex flex-col justify-between flex-grow mt-1">
                            <h3 class="text-lg text-white truncate font-semibold mb-1" :title="movie.movieName">{{ movie.movieName }}</h3>
                            <div class="text-sm text-gray-400 flex justify-between items-center">
                               <span>{{ movie.releaseDate ? movie.releaseDate.substring(0, 4) : 'N/A' }}</span>
                               <span>⭐ {{ movie.movieVoteAverage != null ? movie.movieVoteAverage.toFixed(1) : 'N/A' }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="!isLoading && !errorMsg && totalItems > itemsPerPage" class="flex justify-center mt-8 mb-4">
            <button
                @click="changePage(currentPage - 1)"
                :disabled="currentPage === 1 || isLoading"
                class="px-4 py-2 bg-gray-700 text-white rounded-l disabled:opacity-50 hover:bg-gray-600 transition-colors duration-150"
            >
                Previous
            </button>
            <span class="px-4 py-2 mx-px bg-gray-800 border-t border-b border-gray-700">{{ currentPage }} / {{ totalPages }}</span>
            <button
                @click="changePage(currentPage + 1)"
                :disabled="currentPage === totalPages || isLoading"
                class="px-4 py-2 bg-gray-700 text-white rounded-r disabled:opacity-50 hover:bg-gray-600 transition-colors duration-150"
            >
                Next
            </button>
        </div>
    </main>
</template>

<script setup>
    import { onMounted, ref, computed } from 'vue';
    import { useRouter } from 'vue-router';
    import axios from 'axios';
    import { ElMessage } from 'element-plus';

    const router = useRouter();
    const movies = ref([]);
    const currentPage = ref(1);
    const itemsPerPage = ref(24);
    const totalItems = ref(0);
    const isLoading = ref(false);
    const errorMsg = ref('');

    const totalPages = computed(() => {
        if (totalItems.value === 0 || itemsPerPage.value === 0) {
            return 1;
        }
        return Math.ceil(totalItems.value / itemsPerPage.value);
    });

    const fetchMovies = async () => {
        if (isLoading.value) return;
        isLoading.value = true;
        errorMsg.value = '';

        try {
            const response = await axios.get(`http://127.0.0.1:9231/sysMovie/find/rankingList/2`, {
                params: {
                    pageNum: currentPage.value,
                    pageSize: itemsPerPage.value
                }
            });

            console.log("Backend Response:", response.data);

            if (response.data && response.data.code === 200) {
                movies.value = response.data.data || [];
                totalItems.value = response.data.total || 0;

                 if (movies.value.length === 0 && currentPage.value > 1) {
                    console.warn("Fetched empty data on page > 1, might be past the last page.");
                 }
            } else {
                throw new Error(response.data.msg || 'Failed to fetch movies.');
            }
        } catch (error) {
            console.error('Failed to get film information', error);
            movies.value = [];
            totalItems.value = 0;
             if (error.response) {
               errorMsg.value = `Error ${error.response.status}: Could not fetch results. ${error.response.data?.msg || ''}`;
            } else if (error.request) {
               errorMsg.value = 'Network Error: Unable to connect to the server.';
            } else if (error.message){
               errorMsg.value = error.message;
            } else {
                errorMsg.value = 'An unexpected error occurred while fetching movies.';
            }
            ElMessage.error(errorMsg.value);
        } finally {
            isLoading.value = false;
        }
    };

    const changePage = (targetPage) => {
        if (targetPage >= 1 && targetPage <= totalPages.value && targetPage !== currentPage.value && !isLoading.value) {
            currentPage.value = targetPage;
            window.scrollTo(0, 0);
            fetchMovies();
        }
    }

    function goToDetail(id){
        if (!id) {
            console.error("Movie ID is missing, cannot navigate.");
            ElMessage.warning("Cannot open movie details: ID is missing.");
            return;
        }
        router.push({ name: 'MovieDetail', params: {id} });
    }

    onMounted(fetchMovies);

</script>

<style scoped>
button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.truncate {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>