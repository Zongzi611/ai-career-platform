import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/auth'
import { setToken, setUserInfo, removeToken, getToken, getUserInfo as getCachedUser } from '../utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getCachedUser() || null
  }),
  actions: {
    async login(credentials) {
      const res = await loginApi(credentials)
      if (res.code !== 200) {
        throw { response: { data: res } }
      }
      this.token = res.data.token
      this.userInfo = res.data.userInfo
      setToken(res.data.token)
      setUserInfo(res.data.userInfo)
      return res.data
    },
    async fetchUserInfo() {
      const res = await getUserInfoApi()
      this.userInfo = res.data
      setUserInfo(res.data)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
    }
  },
  getters: {
    roles: (state) => state.userInfo?.roles || [],
    isStudent: (state) => state.userInfo?.roles?.includes('ROLE_STUDENT'),
    isTeacher: (state) => state.userInfo?.roles?.includes('ROLE_TEACHER'),
    isAdmin: (state) => state.userInfo?.roles?.includes('ROLE_ADMIN')
  }
})
