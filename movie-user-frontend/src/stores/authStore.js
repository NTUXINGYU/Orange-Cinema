import { defineStore } from 'pinia';
import router from '@/router'; 

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    user: null, 
    token: null,
  }),

  getters: {
    getUserName: (state) => (state.user ? state.user.userName : ''),
    isAuthenticated: (state) => state.isLoggedIn,
    getToken: (state) => state.token,
    currentUserId: (state) => (state.user ? state.user.userId : null), 
    currentUser: (state) => state.user,
  },

  actions: {
    loginSuccess(userData, tokenData) {
      this.user = userData;
      this.token = tokenData;
      this.isLoggedIn = true;
      window.sessionStorage.setItem('token', tokenData);
      window.sessionStorage.setItem('loginUser', JSON.stringify(userData));
    },

    logout(redirectToLogin = true) {
      this.user = null;
      this.token = null;
      this.isLoggedIn = false;
      window.sessionStorage.removeItem('token');
      window.sessionStorage.removeItem('loginUser');
      if (redirectToLogin && router) {
        router.push({ name: 'Login', query: { sessionExpired: 'true' } }).catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            console.error(err);
          }
        });
      }
    },

    checkAuthStatus() {
      const storedToken = window.sessionStorage.getItem('token');
      const storedUserString = window.sessionStorage.getItem('loginUser');

      if (storedToken && storedUserString) {
        try {
          const storedUser = JSON.parse(storedUserString);
          this.user = storedUser; 
          this.token = storedToken;
          this.isLoggedIn = true;
        } catch (error) {
          console.error("Error parsing stored user data from sessionStorage:", error);
          this.logout(false);
        }
      } else {
        if (this.isLoggedIn) {
          this.logout(false);
        }
      }
    },
  },
});