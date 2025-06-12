import { createRouter, createWebHistory } from 'vue-router'

// 导入所有需要的视图组件
import HomeView from '../views/HomeView.vue'
import ChooseSession from '../views/cinema/ChooseSession.vue'
import Player from '../views/OnlinePlayer/Player.vue'

import NowPlayingMovies from '../views/movie/NowPlayingMovies.vue'
import PopularMovies from '../views/movie/ComingSoon.vue'
import TopRatedMovies from '../views/movie/TopRatedMovies.vue'
import MovieDetail from '../views/movie/MovieDetail.vue'

import BillDetail from '../views/pay/BillDetail.vue'
import ChooseSeat from '../views/pay/ChooseSeat.vue'

import Search from '../views/search/Search.vue'
import SearchMovie from '../views/search/SearchMovie.vue'

import Login from '../views/user/Login.vue'
import Register from '../views/user/Register.vue'

import UserCenterLayout from '../views/user/UserCenterLayout.vue' // 父布局组件
import UserInfo from '../views/user/UserInfo.vue'                 // 用户信息子页面
import BillInfo from '../views/user/BillInfo.vue'                 // 订单信息子页面

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: HomeView,
    },
    {
      path: '/top_rated',
      name: 'TopRatedMovies',
      component: TopRatedMovies,
    },
    {
      path: '/popular',
      name: 'PopularMovies',
      component: PopularMovies,
    },
    {
      path: '/now-playing',
      name: 'NowPlayingMovies',
      component: NowPlayingMovies,
    },
    {
      path: '/movie/:id',
      name: 'MovieDetail',
      component: MovieDetail,
      props: true,
    },
    {
      path: '/chooseSession',
      name: 'ChooseSession',
      component: ChooseSession,
    },
    {
      path: '/movies',
      name: 'Search',
      component: Search,
    },
    {
      path: '/search',
      name: 'SearchMovie',
      component: SearchMovie,
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
    },
    {
      path: '/bill-detail/:billId',
      name: 'BillDetail',
      component: BillDetail,
      props: true,
    },
    {
      path: '/chooseseat/:sessionId',
      name: 'ChooseSeat',
      component: ChooseSeat,
      props: true,
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('../views/cinema/About.vue'),
    },
    {
      path: '/player',
      name: 'OnlinePlayer',
      component: Player,
      meta: {
        requiresAuth: true, 
        fullscreen: true,
      }
    },
    {
      path: '/user-center',          // 父路由路径
      component: UserCenterLayout,   // 使用布局组件
      redirect: '/user-center/info', // 默认重定向到信息页
      children: [
        {
          path: 'info', // 完整路径: /user-center/info
          name: 'UserInfo',
          component: UserInfo,
        },
        {
          path: 'bill', // 完整路径: /user-center/bill
          name: 'BillInfo',
          component: BillInfo,
        },
      ],
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0, left: 0 };
    }
  }
})

export default router