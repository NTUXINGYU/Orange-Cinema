<template>
    <main class="flex-grow bg-gray-800 text-white p-6 ">
        <div class="p-3 container mx-auto">
            <div class="mb-4 flex items-center">
                <label class="block text-sm font-medium text-gray-300 mr-4">GENRE</label>
                <div class="flex flex-wrap gap-4">
                    <button
                        v-for="(type, index) in types"
                        :key="index"
                        :class="['px-3 py-2 rounded-full transition-colors duration-150', type === selectedType ? 'bg-blue-500 text-white' : 'bg-gray-900 text-gray-100 hover:bg-gray-700']"
                        @click="selectType(type)"
                        :disabled="isLoading"
                    >
                        {{ type }}
                    </button>
                </div>
            </div>

            <div class="mb-4 flex items-center">
                <label class="block text-sm font-medium text-gray-300 ml-2 mr-5">YEAR</label>
                <div class="flex flex-wrap gap-4">
                    <button
                        v-for="(year, index) in years"
                        :key="index"
                        :class="['px-3 py-2 rounded-full transition-colors duration-150', year === selectedYear ? 'bg-blue-500 text-white' : 'bg-gray-900 text-gray-100 hover:bg-gray-700']"
                        @click="selectYear(year)"
                        :disabled="isLoading"
                    >
                        {{ year }}
                    </button>
                </div>
            </div>

            <div class="pt-3 flex items-center">
                <label class="block text-sm font-medium text-gray-300 ml-2 mr-5">SORT</label>
                <div class="flex flex-wrap gap-6">
                    <label class="flex items-center cursor-pointer">
                        <input
                            type="radio"
                            name="sortOption"
                            value="popularity"
                            class="mr-2 accent-blue-500"
                            :checked="selectedSort === 'popularity'"
                            @change="selectSort('popularity')"
                            :disabled="isLoading"
                        />
                        Most popular
                    </label>
                    <label class="flex items-center cursor-pointer">
                        <input
                            type="radio"
                            name="sortOption"
                            value="newest"
                            class="mr-2 accent-blue-500"
                            :checked="selectedSort === 'newest'"
                            @change="selectSort('newest')"
                            :disabled="isLoading"
                        />
                        Newest
                    </label>
                    <label class="flex items-center cursor-pointer">
                        <input
                            type="radio"
                            name="sortOption"
                            value="rating"
                            class="mr-2 accent-blue-500"
                            :checked="selectedSort === 'rating'"
                            @change="selectSort('rating')"
                            :disabled="isLoading"
                        />
                        Rating
                    </label>
                </div>
            </div>
        </div>

        <div v-if="isLoading" class="text-center py-10">
            <p>Loading movies...</p>
        </div>

         <div v-if="errorMsg && !isLoading" class="text-center py-10 text-red-400">
            <p>{{ errorMsg }}</p>
        </div>

        <div v-if="!isLoading && !errorMsg && movies.length === 0" class="text-center py-10 text-gray-400">
            <p>No movies found matching your criteria.</p>
        </div>

        <div v-if="!isLoading && !errorMsg && movies.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 mt-6 container mx-auto">
            <div
                v-for="movie in movies"
                :key="movie.movieId"
                class="bg-gray-900 p-4 rounded cursor-pointer transform hover:scale-105 transition-transform duration-200 flex flex-col"
                @click="goToDetail(movie.movieId)"
            >
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

        <div v-if="!isLoading && totalItems > itemsPerPage" class="flex justify-center mt-8 mb-4">
            <button
                class="px-4 py-2 bg-gray-700 text-white rounded-l disabled:opacity-50 hover:bg-gray-600 transition-colors duration-150"
                :disabled="currentPage <= 1 || isLoading"
                @click="changePage(currentPage - 1)"
            >
                Previous
            </button>
            <span class="px-4 py-2 mx-px bg-gray-800 border-t border-b border-gray-700">{{ currentPage }} / {{ totalPages }}</span>
            <button
                class="px-4 py-2 bg-gray-700 text-white rounded-r disabled:opacity-50 hover:bg-gray-600 transition-colors duration-150"
                :disabled="currentPage >= totalPages || isLoading"
                @click="changePage(currentPage + 1)"
            >
                Next
            </button>
        </div>
    </main>
</template>

<script setup>
    import { ref, onMounted, computed } from 'vue';
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

    const types = [
        'All', 'History', 'Animation', 'Thriller',
        'Comedy', 'Crime', 'Fantasy', 'Horror',
        'Documentary', 'TV Movie', 'Family', 'Drama',
        'Music', 'Western', 'War', 'Action',
        'Romance', 'Mystery', 'Adventure', 'Science Fiction'
    ];

    const typeMap = {
        'All': null, 'History': 1, 'Animation': 2, 'Thriller': 3,
        'Comedy': 4, 'Crime': 5, 'Fantasy': 6, 'Horror': 7,
        'Documentary': 8, 'TV Movie': 9, 'Family': 10, 'Drama': 11,
        'Music': 12, 'Western': 13, 'War': 14, 'Action': 15,
        'Romance': 16, 'Mystery': 17, 'Adventure': 18, 'Science Fiction': 19
    };
    const selectedType = ref('All');

    const years = [
        'All', '2026', '2025', '2024', '2023', '2022', '2021', '2010-2020', '2000-2009', 'Before 2000'
    ];
    const yearRangeMap = {
        'All': { start: null, end: null },
        '2026': { start: '2026-01-01', end: '2026-12-31' },
        '2025': { start: '2025-01-01', end: '2025-12-31' },
        '2024': { start: '2024-01-01', end: '2024-12-31' },
        '2023': { start: '2023-01-01', end: '2023-12-31' },
        '2022': { start: '2022-01-01', end: '2022-12-31' },
        '2021': { start: '2021-01-01', end: '2021-12-31' },
        '2010-2020': { start: '2010-01-01', end: '2020-12-31' },
        '2000-2009': { start: '2000-01-01', end: '2009-12-31' },
        'Before 2000': { start: null, end: '1999-12-31' },
    };
    const selectedYear = ref('All');

    const sortOptions = {
        'popularity': 1,
        'newest': 2,
        'rating': 3,
    };
    const selectedSort = ref('popularity');

    const SESSION_STORAGE_KEY = 'movieListFilterState';

    const saveStateToSessionStorage = () => {
        const state = {
            type: selectedType.value,
            year: selectedYear.value,
            sort: selectedSort.value,
            page: currentPage.value
        };
        try {
            sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(state));
        } catch (e) {
            console.error("无法保存筛选状态到 sessionStorage:", e);
        }
    };

    const loadStateFromSessionStorage = () => {
        const savedStateJSON = sessionStorage.getItem(SESSION_STORAGE_KEY);
        if (savedStateJSON) {
            try {
                const savedState = JSON.parse(savedStateJSON);
                selectedType.value = savedState.type || 'All';
                selectedYear.value = savedState.year || 'All';
                selectedSort.value = savedState.sort || 'popularity';
                currentPage.value = savedState.page || 1;
                return true;
            } catch (e) {
                console.error("解析保存的筛选状态失败:", e);
                sessionStorage.removeItem(SESSION_STORAGE_KEY);
                return false;
            }
        }
        return false;
    };


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

        const params = {
            pageNum: currentPage.value,
            pageSize: itemsPerPage.value,
            movieCategoryId: typeMap[selectedType.value],
            startDate: yearRangeMap[selectedYear.value]?.start,
            endDate: yearRangeMap[selectedYear.value]?.end,
            movieSort: sortOptions[selectedSort.value],
        };

        Object.keys(params).forEach(key => {
            if (params[key] === null || params[key] === undefined || params[key] === '') {
                delete params[key];
            }
        });

        console.log("Request Params:", params);

        try {
            const response = await axios.get('http://127.0.0.1:9231/sysMovie/find', { params });
            console.log("Backend Response:", response.data);

            if (response.data && response.data.code === 200) {
                movies.value = response.data.data || [];
                totalItems.value = response.data.total || 0;

                if (movies.value.length === 0) {
                     if (currentPage.value > 1) {
                        console.warn("Fetched empty data on page > 1, might be past the last page.");
                    }
                }

            } else {
                throw new Error(response.data.msg || 'Failed to fetch movies from backend.');
            }
        } catch (error) {
            console.error('Error fetching movies:', error);
            movies.value = [];
            totalItems.value = 0;
             if (error.response) {
               errorMsg.value = `Error ${error.response.status}: Could not fetch results. ${error.response.data?.msg || ''}`;
            } else if (error.request) {
               errorMsg.value = 'Network Error: Unable to connect to the server.';
            } else if (error.message){
               errorMsg.value = error.message;
            } else {
                errorMsg.value = 'An unexpected error occurred.';
            }
            ElMessage.error(errorMsg.value);
        } finally {
            isLoading.value = false;
        }
    };

    const selectType = (type) => {
        if (selectedType.value === type || isLoading.value) return;
        selectedType.value = type;
        currentPage.value = 1;
        saveStateToSessionStorage();
        fetchMovies();
    };

    const selectYear = (year) => {
         if (selectedYear.value === year || isLoading.value) return;
        selectedYear.value = year;
        currentPage.value = 1;
        saveStateToSessionStorage();
        fetchMovies();
    };

    const selectSort = (sortKey) => {
         if (selectedSort.value === sortKey || isLoading.value) return;
        selectedSort.value = sortKey;
        currentPage.value = 1;
        saveStateToSessionStorage();
        fetchMovies();
    };

    const changePage = (page) => {
        const targetPage = page;
        if (targetPage >= 1 && targetPage <= totalPages.value && targetPage !== currentPage.value && !isLoading.value) {
            currentPage.value = targetPage;
            saveStateToSessionStorage();
            window.scrollTo(0, 0);
            fetchMovies();
        }
    };


    function goToDetail(movieId){
        if (!movieId) {
            console.error("Movie ID is missing, cannot navigate.");
            ElMessage.warning("Cannot open movie details: ID is missing.");
            return;
        }
        router.push({ name: 'MovieDetail', params: { id: movieId } });
    }

    onMounted(() => {
        loadStateFromSessionStorage();
        fetchMovies();
    });

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
.accent-blue-500 {
  accent-color: #3b82f6;
}
</style>