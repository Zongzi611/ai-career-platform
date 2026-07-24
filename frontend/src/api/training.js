import request from './request'

export const getTasks = (params) => request.get('/training/tasks', { params })
export const getTask = (id) => request.get(`/training/tasks/${id}`)
export const submitAnswer = (data) => request.post('/training/submit', data)
export const triggerScoring = (recordId) => request.post(`/training/score/${recordId}`)
export const getMyRecords = (params) => request.get('/training/my-records', { params })
export const getRecord = (id) => request.get(`/training/record/${id}`)
