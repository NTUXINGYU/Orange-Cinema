<template>
  <el-container class="home-container">
    <el-header>
      <div class="header-left-content" @click="toWelcome">
        <img src="../assets/logo02.jpg" alt="Logo" class="header-logo">
        <span class="header-title">Cinema Management System</span>
      </div>
      <el-button type="danger" @click="logout" class="logout-button">
        <i class="iconfont icon-login-out" style="margin-right: 5px;"></i>
        Logout
      </el-button>
    </el-header>
    <el-container class="main-content-wrapper">
      <el-aside :width="isAsideCollapsed ? '64px' : '200px'" class="home-aside">
        <div class="toggle-button" @click="toggleAsideCollapse">
          <i :class="isAsideCollapsed ? 'iconfont icon-arrow-right' : 'iconfont icon-arrow-left'"></i>
        </div>
        <el-menu
          background-color="white"
          text-color="#303133"
          active-text-color="#409EFF"
          unique-opened
          :collapse="isAsideCollapsed"
          :collapse-transition="false"
          router
          :default-active="activeMenuPath"
          class="home-el-menu"
        >
          <el-sub-menu v-for="item in menuList" :index="String(item.id)" :key="item.id">
            <template #title>
              <i :class="iconList[item.id]" class="menu-icon"></i>
              <span>{{ item.name }}</span>
            </template>
            <el-menu-item
              v-for="subItem in item.children"
              :index="'/' + subItem.path"
              :key="subItem.id"
              @click="handleMenuItemClick('/' + subItem.path)"
            >
              <i class="iconfont icon-detail-button sub-menu-icon"></i>
              <span>{{ subItem.name }}</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main class="home-main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';

const router = useRouter();
const route = useRoute();
const store = useStore();

const menuList = ref([]);
const iconList = ref({
  '1': 'iconfont icon-home-menu',
  '2': 'iconfont icon-movieManage-menu',
  '3': 'iconfont icon-movieLow-menu',
  '4': 'iconfont icon-order-menu',
  '5': 'iconfont icon-user-menu',
  '6': 'iconfont icon-power-menu',
  '7': 'iconfont icon-userPower-menu',
  '8': 'iconfont icon-edit-button'
});

const isAsideCollapsed = ref(false);

const activeMenuPath = computed(() => route.path);

const getMenuList = () => {
  // [核心修复] 根据你的 store 文件, 正确的路径是 store.state.userInfo
  const userInfo = store.state.userInfo;

  if (userInfo && userInfo.sysUser?.sysRole?.children) {
    menuList.value = userInfo.sysUser.sysRole.children;
  } else {
    console.error("User info or menu data not found in Vuex state. Logging out.");
    menuList.value = [];
    logout(); // 如果没有菜单信息，强制登出
  }
};

const logout = () => {
  store.dispatch('logout');
  router.push('/login');
};

const toWelcome = () => {
  if (route.path !== '/welcome') {
    router.push('/welcome');
  }
};

const toggleAsideCollapse = () => {
  isAsideCollapsed.value = !isAsideCollapsed.value;
};

const handleMenuItemClick = (path) => {
  if (route.path !== path) {
    router.push(path);
  }
};

onMounted(() => {
  getMenuList();
  // 当直接访问/home时，如果菜单存在，自动跳转到/welcome
  if (route.path === '/home' && menuList.value.length > 0) {
     router.push('/welcome');
  }
});
</script>

<style scoped>
/* 样式保持不变 */
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-header {
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  color: #303133;
  font-size: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  height: 60px;
  flex-shrink: 0;
}

.header-left-content {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.header-logo {
  height: 40px;
  width: 40px;
  margin-right: 10px;
  border-radius: 4px;
}

.header-title {
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
}

.logout-button .iconfont {
  font-size: 16px;
}

.main-content-wrapper {
  flex-grow: 1;
  overflow: hidden;
}

.home-aside {
  background-color: #ffffff;
  box-shadow: 2px 0 6px rgba(0,21,41,.05);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.toggle-button {
  background-color: #f8f9fa;
  color: #606266;
  font-size: 18px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
  padding: 8px 0;
  border-bottom: 1px solid #e6e6e6;
}

.toggle-button:hover {
  background-color: #f0f2f5;
}

.home-el-menu {
  border-right: none;
  flex-grow: 1;
  overflow-y: auto;
}

.menu-icon, .sub-menu-icon {
  margin-right: 10px;
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.el-menu-item.is-active {
  background-color: #ecf5ff !important;
}

.home-main {
  background-color: #f0f2f5;
  padding: 20px;
  flex-grow: 1;
  overflow-y: auto;
}
</style>