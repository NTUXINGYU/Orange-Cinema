import { createRouter, createWebHistory } from 'vue-router';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { ElMessage } from 'element-plus';

const Login = () => import('../views/Login.vue');
const Home = () => import('../views/Home.vue');
const Welcome = () => import('../views/Welcome.vue');
const CinemaInfo = () => import('../views/cinema/CinemaInfo.vue');
const MovieInfo = () => import('../views/movie/MovieInfo.vue');
const MovieCategory = () => import('../views/movie/MovieCategory.vue');
const HallInfo = () => import('../views/hall/HallInfo.vue');
const SessionInfo = () => import('../views/hall/SessionInfo.vue');
const BillInfo = () => import('../views/bill/BillInfo.vue');
const UserInfo = () => import('../views/user/UserInfo.vue');
const RoleInfo = () => import('../views/role/RoleInfo.vue');
const ResourceInfo = () => import('../views/role/ResourceInfo.vue');
const Error404 = () => import('../views/Error404.vue');

const routes = [
  {
    path: '/',
    redirect: '/login',
    meta: { title: 'Redirecting to Login' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: 'Login' }
  },
  {
    path: '/home',
    name: 'HomeLayout',
    component: Home,
    redirect: '/welcome',
    meta: { requiresAuth: true, title: 'Home' },
    children: [
      { path: '/welcome', name: 'Welcome', component: Welcome, meta: { title: 'Welcome' } },
      { path: '/cinema', name: 'CinemaInfo', component: CinemaInfo, meta: { title: 'Cinema Management' } },
      { path: '/movie', name: 'MovieInfo', component: MovieInfo, meta: { title: 'Movie Management' } },
      { path: '/movieCategory', name: 'MovieCategory', component: MovieCategory, meta: { title: 'Movie Categories' } },
      { path: '/hall', name: 'HallInfo', component: HallInfo, meta: { title: 'Hall Management' } },
      { path: '/session', name: 'SessionInfo', component: SessionInfo, meta: { title: 'Session Management' } },
      { path: '/user', name: 'UserInfo', component: UserInfo, meta: { title: 'User Management' } },
      { path: '/bill', name: 'BillInfo', component: BillInfo, meta: { title: 'Bill Management' } },
      { path: '/role', name: 'RoleInfo', component: RoleInfo, meta: { title: 'Role Management' } },
      { path: '/resource', name: 'ResourceInfo', component: ResourceInfo, meta: { title: 'Resource Management' } }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: Error404,
    meta: { title: 'Page Not Found' }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  }
});

NProgress.configure({ showSpinner: false });

router.beforeEach((to, from, next) => {
  NProgress.start();

  if (to.meta.title) {
    document.title = `${to.meta.title} - YourAppName`;
  } else {
    document.title = 'YourAppName';
  }

  if (to.path === '/login') {
    const token = window.sessionStorage.getItem('token');
    if (token && to.name === 'Login') {
        next({ path: '/welcome' });
        NProgress.done();
        return;
    }
    return next();
  }

  const token = window.sessionStorage.getItem('token');
  if (!token) {
    ElMessage.error('Authentication failed or expired, please login again.');
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
    NProgress.done();
    return;
  }

  next();
});

router.afterEach(() => {
  NProgress.done();
});

export default router;