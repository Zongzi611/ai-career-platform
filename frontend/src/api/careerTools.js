import request from './request'

export const addBookmark = (careerId) => request.post(`/career-tools/bookmark/${careerId}`)
export const removeBookmark = (careerId) => request.delete(`/career-tools/bookmark/${careerId}`)
export const getBookmarks = () => request.get('/career-tools/bookmarks')
export const getBookmarkIds = () => request.get('/career-tools/bookmark/ids')
export const analyzeSkillGap = (careerId) => request.get(`/career-tools/skill-gap/${careerId}`)
export const getSalaryStats = () => request.get('/career-tools/salary-stats')
export const getLearningPath = (careerId) => request.get(`/career-tools/learning-path/${careerId}`)
