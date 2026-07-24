const TOKEN_KEY = 'qkc_token'
const USER_KEY = 'qkc_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function setUserInfo(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getUserInfo() {
  const u = localStorage.getItem(USER_KEY)
  return u ? JSON.parse(u) : null
}

export function getRoles() {
  const u = getUserInfo()
  return u?.roles || []
}

export function hasRole(role) {
  return getRoles().includes(role)
}
