import request from './request'

export const optimizeResume = (data) => request.post('/resume/optimize', data)
export const getResumeHistory = () => request.get('/resume/history')
export const getResumeDetail = (id) => request.get(`/resume/${id}`)
export const deleteResume = (id) => request.delete(`/resume/${id}`)
export const uploadResumeFile = (formData) => request.post('/resume/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
