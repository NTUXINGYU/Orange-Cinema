<template>
  <header class="bg-gray-900 text-white p-4 sticky top-0 z-50 shadow-md">
    <div class="container mx-auto flex justify-between items-center">
      <!-- Logo -->
      <IconLogo class="flex-shrink-0" />

      <!-- PC Navigation (hidden on mobile) -->
      <nav class="hidden md:flex flex-grow justify-center">
        <ul class="flex space-x-8">
          <li>
            <router-link to="/" class="text-lg hover:text-orange-400 transition-colors" :class="{'border-b-2 border-orange-400 text-orange-400': isHomePage}">
              Home
            </router-link>
          </li>
          <li>
            <router-link to="/movies" class="text-lg hover:text-orange-400 transition-colors" :class="{'border-b-2 border-orange-400 text-orange-400': isMoviesPage}">
              Movies
            </router-link>
          </li>
          <li>
            <router-link to="/chooseSession" class="text-lg hover:text-orange-400 transition-colors" :class="{'border-b-2 border-orange-400 text-orange-400': isChooseSessionPage}">
              Buy Tickets
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- Right side items -->
      <div class="flex items-center space-x-4">
        <!-- Search bar -->
        <div class="hidden sm:flex items-center rounded-full border border-gray-600 bg-gray-800 p-1">
          <input v-model="keyword" class="ml-2 w-24 sm:w-32 bg-transparent focus:outline-none py-1 px-2 text-white" placeholder="Search..." @keyup.enter="searchInfo"/>
          <button @click="searchInfo" class="text-gray-400 hover:text-white">
            <el-icon><Search /></el-icon>
          </button>
        </div>
        
        <!-- User Avatar and Dropdown -->
        <el-dropdown @command="handleCommand" trigger="click">
          <span class="flex items-center cursor-pointer">
            <el-avatar :src="currentUserAvatarDisplayPath" :size="40" class="ring-2 ring-gray-500">
              <span v-if="showFallbackAvatar" class="flex items-center justify-center bg-gray-500 w-full h-full rounded-full text-white font-semibold">
                {{ authStore.isLoggedIn && authStore.getUserName ? authStore.getUserName.substring(0,1).toUpperCase() : 'U' }}
              </span>
            </el-avatar>
          </span>
          <template #dropdown>
            <el-dropdown-menu class="bg-white shadow-lg rounded border border-gray-200">
              <el-dropdown-item command="userMenu" v-if="authStore.isLoggedIn" class="p-2 text-gray-700 hover:bg-gray-100" @click="goToUserMenu">
                Personal Center
              </el-dropdown-item>
              <el-dropdown-item command="logout" v-if="authStore.isLoggedIn" class="p-2 text-gray-700 hover:bg-gray-100">
                Log out
              </el-dropdown-item>
              <el-dropdown-item command="login" v-if="!authStore.isLoggedIn" class="p-2 text-gray-700 hover:bg-gray-100" @click="goToLogin">
                Log in
              </el-dropdown-item>
              <el-dropdown-item command="register" v-if="!authStore.isLoggedIn" class="p-2 text-gray-700 hover:bg-gray-100" @click="goToRegister">
                Sign up
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- Hamburger Menu Button (visible on mobile) -->
        <button @click="isMobileMenuOpen = !isMobileMenuOpen" class="md:hidden p-2 text-gray-300 hover:text-white">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7"></path></svg>
        </button>
      </div>
    </div>
    
    <!-- Mobile Menu (Drawer) -->
    <transition name="slide-fade">
        <nav v-if="isMobileMenuOpen" class="md:hidden bg-gray-900 pt-4 pb-8 space-y-2">
            <router-link to="/" class="block text-center py-3 text-lg text-white hover:bg-gray-800 rounded-md" @click="isMobileMenuOpen = false">Home</router-link>
            <router-link to="/movies" class="block text-center py-3 text-lg text-white hover:bg-gray-800 rounded-md" @click="isMobileMenuOpen = false">Movies</router-link>
            <router-link to="/chooseSession" class="block text-center py-3 text-lg text-white hover:bg-gray-800 rounded-md" @click="isMobileMenuOpen = false">Buy Tickets</router-link>
             <!-- Mobile Search bar -->
            <div class="flex items-center rounded-full border border-gray-600 bg-gray-800 p-1 mx-4 mt-4">
              <input v-model="keyword" class="ml-2 w-full bg-transparent focus:outline-none py-1 px-2 text-white" placeholder="Search..." @keyup.enter="searchInfo"/>
              <button @click="searchInfo" class="text-gray-400 hover:text-white pr-2">
                <el-icon><Search /></el-icon>
              </button>
            </div>
        </nav>
    </transition>

  </header>
  <RouterView />
  <footer class="bg-gray-900 text-white p-4">
    <div class="container mx-auto text-center">
      © {{ new Date().getFullYear() }} OrangeCinema. All rights reserved.
    </div>
  </footer>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useRouter, useRoute, RouterLink, RouterView } from 'vue-router';
import IconLogo from './components/icons/IconLogo.vue';
import { Search } from '@element-plus/icons-vue';
import { ElButton, ElIcon, ElMessage, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const isMobileMenuOpen = ref(false);

const isHomePage = ref(false);
const isMoviesPage = ref(false);
const isChooseSessionPage = ref(false);
const keyword = ref('');

const predefinedAvatarsList = [
  { id: "0", path: "/avatars/avatar-0.png" },
  { id: "1", path: "/avatars/avatar-1.png" },
  { id: "2", path: "/avatars/avatar-2.png" },
  { id: "3", path: "/avatars/avatar-3.png" },
];
const defaultDisplayAvatarPath = "/avatars/avatar-default.png";

const currentUserAvatarDisplayPath = computed(() => {
  if (authStore.isLoggedIn && authStore.user && authStore.user.userPicture !== null && authStore.user.userPicture !== undefined) {
    const avatarId = String(authStore.user.userPicture);
    const selectedAvatar = predefinedAvatarsList.find(avatar => avatar.id === avatarId);
    return selectedAvatar ? selectedAvatar.path : defaultDisplayAvatarPath;
  }
  return defaultDisplayAvatarPath;
});

const showFallbackAvatar = computed(() => {
  if (!authStore.isLoggedIn || !authStore.user || authStore.user.userPicture === null || authStore.user.userPicture === undefined) {
    return true;
  }
  const avatarId = String(authStore.user.userPicture);
  return !predefinedAvatarsList.some(avatar => avatar.id === avatarId);
});

watch(
  () => route.path,
  (newPath) => {
    isHomePage.value = newPath === '/';
    isMoviesPage.value = newPath === '/movies';
    isChooseSessionPage.value = newPath === '/chooseSession';
    isMobileMenuOpen.value = false; // Close mobile menu on route change
  },
  { immediate: true }
);

const searchInfo = () => {
  const searchTerm = keyword.value.trim();
  if (!searchTerm) {
    ElMessage.warning('Please enter a movie name to search.');
    return;
  }
  isMobileMenuOpen.value = false;
  router.push({
    name: 'SearchMovie',
    query: { kw: searchTerm }
  });
};

function handleCommand(command) {
  switch (command) {
    case "userMenu":
      goToUserMenu();
      break;
    case "logout":
      authStore.logout();
      ElMessage.success('Logged out successfully.');
      if (route.meta.requiresAuth || route.name === 'UserInfo' || route.name === 'BillInfo') {
           router.push({ name: 'Login' });
      } else if (route.name !== 'Home' && route.name !== 'Login' && route.name !== 'Register') {
          router.push({name: 'Home'});
      }
      break;
  }
}

function goToLogin() { router.push({ name: 'Login' }); }
function goToRegister() { router.push({ name: 'Register' }); }
function goToUserMenu() { 
  if (authStore.isLoggedIn) {
      router.push({ name: 'UserInfo' });
  }
}
</script>

<style scoped>
.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.3s ease-out;
}
.slide-fade-enter-from, .slide-fade-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

.el-avatar > span {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
}
</style>