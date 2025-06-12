import { createStore } from 'vuex';

// A helper function to safely parse JSON from sessionStorage
const getInitialUserInfo = () => {
  try {
    const userInfo = window.sessionStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
  } catch (e) {
    console.error("Failed to parse user info from sessionStorage", e);
    return null;
  }
};

const store = createStore({
  // state holds all the application-level state.
  state: {
    // Initialize token from sessionStorage. If it doesn't exist, it will be an empty string.
    token: window.sessionStorage.getItem('token') || '',
    // Initialize userInfo from sessionStorage.
    userInfo: getInitialUserInfo(),
  },

  // mutations are synchronous functions that directly modify the state.
  mutations: {
    /**
     * Sets the authentication token.
     * @param {object} state - The current state.
     * @param {string} token - The JWT token to be stored.
     */
    SET_TOKEN(state, token) {
      state.token = token;
      window.sessionStorage.setItem('token', token);
    },

    /**
     * Sets the current user's information.
     * @param {object} state - The current state.
     * @param {object} userInfo - The user object to be stored.
     */
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo;
      window.sessionStorage.setItem('userInfo', JSON.stringify(userInfo));
    },

    /**
     * Clears all authentication-related state.
     * This is used for logging out.
     * @param {object} state - The current state.
     */
    CLEAR_AUTH_DATA(state) {
      state.token = '';
      state.userInfo = null;
      window.sessionStorage.removeItem('token');
      window.sessionStorage.removeItem('userInfo');
    }
  },

  // actions are asynchronous functions that can commit mutations.
  // They are used for handling async operations like API calls.
  actions: {
    /**
     * Action to handle user login.
     * It commits mutations to update the state.
     * @param {object} context - The store context (includes commit, state, etc.).
     * @param {object} authData - An object containing the token and userInfo.
     */
    login({ commit }, authData) {
      commit('SET_TOKEN', authData.token);
      commit('SET_USER_INFO', authData.userInfo);
    },

    /**
     * Action to handle user logout.
     * It commits the mutation to clear all auth data.
     * @param {object} context - The store context.
     */
    logout({ commit }) {
      commit('CLEAR_AUTH_DATA');
    }
  },

  // getters are like computed properties for stores.
  // They can be used to derive state.
  getters: {
    /**
     * A getter to easily check if a user is authenticated.
     * @param {object} state - The current state.
     * @returns {boolean} - True if a token exists, false otherwise.
     */
    isAuthenticated: state => !!state.token,

    /**
     * A getter to get the current user's information.
     * @param {object} state - The current state.
     * @returns {object|null} - The user object or null.
     */
    currentUser: state => state.userInfo,

    /**
     * A getter to get the current user's ID.
     * @param {object} state - The current state.
     * @returns {number|null} - The user ID or null.
     */
    currentUserId: state => state.userInfo?.userId || null,
  },

  // modules are used to split the store into smaller, more manageable pieces.
  // For this project, we are not using modules.
  modules: {
  }
});

export default store;