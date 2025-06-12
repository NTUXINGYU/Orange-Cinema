<template>
    <!-- 电影展示区 -->
    <main class="flex-grow bg-gray-800 text-white p-6">
        <div class="container mx-auto">
            <h2 class="text-xl mb-4">Search Results</h2>
            <div>
                <div v-if="movies.length === 0" class="text-center text-gray-400">
                    There are no movies that matched your query.
                </div>
                <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
                    <div v-for="movie in movies" :key="movie.movieId" class="bg-gray-900 p-4 rounded" @click="goToDetail(movie.movieId)">
                    <img
                        :src="'https://image.tmdb.org/t/p/w500'+movie.moviePoster"
                        :alt="movie.movieName"
                        class="w-full h-60 object-cover rounded mb-2"
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
        <!-- 分页按钮 -->
        <div class="flex justify-center mt-6">
            <button
                @click="changePage(currentPage -1)"
                :disabled="currentPage ===1"
                class="bg-gray-700 mtext-white p-2 rounded-l disabled:opacity-50"
            >
            Previous
            </button>
            <span class="bg-gray-800 text-white p-2">{{ currentPage }}</span>
            <button
                @click="changePage(currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="bg-gray-700 mtext-white p-2 rounded-r disabled:opacity-50"
            >
            Next
            </button>
        </div>
    </main>
</template>

<script setup>
    import { ref, watch, onMounted, computed } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import axios from 'axios';
    import { ElMessage } from 'element-plus';

    const route = useRoute();
    const router = useRouter();
    const movies = ref([]);
    const currentPage = ref(1);
    const itemsPerPage = ref(24);
    const totalItems = ref(0);
    const searchKeyword = ref('');
    const isLoading = ref(false);
    const errorMsg = ref('');

    const totalPages = computed(() => {
        if (totalItems.value === 0 || itemsPerPage.value === 0) {
            return 1;
        }
        return Math.ceil(totalItems.value / itemsPerPage.value);
    });

    const searchMovies = async (query) => {
        const trimmedQuery = query ? query.trim() : '';
        if (!trimmedQuery) {
            movies.value = [];
            totalItems.value = 0;
            errorMsg.value = 'Please enter a search term.';
            return;
        }

        console.log(`Making API request to /sysMovie/find with movieName: "${trimmedQuery}", pageNum: ${currentPage.value}`);
        isLoading.value = true;
        errorMsg.value = '';

        try {
            const response = await axios.get('http://127.0.0.1:9231/sysMovie/find', {
                params: {
                    movieName: trimmedQuery,
                    pageNum: currentPage.value,
                    pageSize: itemsPerPage.value
                }
            });

            console.log("Backend Response:", response.data);

            if (response.data && response.data.code === 200 && response.data.data) {
                movies.value = response.data.data || [];
                totalItems.value = response.data.total || 0;
                if (movies.value.length === 0) {
                    errorMsg.value = `No movies found matching "${trimmedQuery}".`;
                }
            } else {
                throw new Error(response.data.message || 'Failed to fetch search results.');
            }
        } catch (error) {
            console.error('Error fetching movies:', error);
            movies.value = [];
            totalItems.value = 0;
            if (error.response) {
               errorMsg.value = `Error ${error.response.status}: Could not fetch results.`;
            } else if (error.request) {
               errorMsg.value = 'Network Error: Unable to connect to the server.';
            } else if (error.message){
               errorMsg.value = error.message;
            } else {
                errorMsg.value = 'An unexpected error occurred during the search.';
            }
            ElMessage.error(errorMsg.value);
        } finally {
            isLoading.value = false;
        }
    };

    watch(
        () => route.query.kw,
        (newQueryValue) => {
            const newQuery = typeof newQueryValue === 'string' ? newQueryValue : '';
            searchKeyword.value = newQuery.trim();
            currentPage.value = 1;
            searchMovies(searchKeyword.value);
        },
        { immediate: true }
    );

    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
            currentPage.value = page;
            searchMovies(searchKeyword.value);
        }
    }

    function goToDetail(id){
        if (!id) {
            console.error("Cannot navigate to detail page without a movie ID.");
            return;
        }
        router.push({ name: 'MovieDetail', params: {id} });
    }

</script>