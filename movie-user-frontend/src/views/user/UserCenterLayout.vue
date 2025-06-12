<template>
  <div class="flex flex-col md:flex-row w-full bg-gray-50 min-h-screen">
    <!-- Top/Side Navigation -->
    <aside class="w-full md:w-64 bg-white shadow-xl flex-shrink-0">
      <!-- Header Area -->
      <div class="h-20 flex items-center justify-center border-b border-gray-200 sticky top-0 bg-white z-10">
        <span class="text-2xl font-bold text-orange-500">User Center</span>
      </div>
      
      <!-- Navigation container -->
      <nav class="md:mt-6">
        <ul class="flex flex-row md:flex-col">
          <!-- User Information Link -->
          <li class="w-1/2 md:w-full">
            <a
              @click="navigate('/user-center/info')"
              :class="[
                'flex flex-col items-center justify-center md:flex-row md:justify-start pt-3 pb-2 md:py-4 px-2 md:px-8 text-gray-600 hover:bg-orange-50 hover:text-orange-500 transition-colors duration-200 cursor-pointer',
                isActive('/user-center/info') ? 'bg-orange-100 text-orange-600 md:border-l-4 border-orange-500 font-semibold' : ''
              ]"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mb-1 md:mb-0 md:mr-3" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" /></svg>
              <span class="text-xs md:text-base">Information</span>
            </a>
          </li>
          
          <!-- Orders Link -->
          <li class="w-1/2 md:w-full">
            <a
              @click="navigate('/user-center/bill')"
              :class="[
                'flex flex-col items-center justify-center md:flex-row md:justify-start pt-3 pb-2 md:py-4 px-2 md:px-8 text-gray-600 hover:bg-orange-50 hover:text-orange-500 transition-colors duration-200 cursor-pointer',
                isActive('/user-center/bill') ? 'bg-orange-100 text-orange-600 md:border-l-4 border-orange-500 font-semibold' : ''
              ]"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mb-1 md:mb-0 md:mr-3" viewBox="0 0 20 20" fill="currentColor"><path d="M4 3a2 2 0 100 4h12a2 2 0 100-4H4z" /><path fill-rule="evenodd" d="M3 8h14v7a2 2 0 01-2 2H5a2 2 0 01-2-2V8zm5 3a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1z" clip-rule="evenodd" /></svg>
              <span class="text-xs md:text-base">Orders</span>
            </a>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 p-4 md:p-8 overflow-y-auto">
      <router-view v-slot="{ Component }" :key="route.fullPath">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const currentPath = computed(() => route.path);

const isActive = (path) => {
  return currentPath.value === path;
};

function navigate(path) {
  if (route.path !== path) {
    router.push(path);
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>