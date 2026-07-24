import request from './request'

export const startInterview = (data) => request.post('/interview/start', data)
export const submitAnswer = (data) => request.post('/interview/answer', data)
export const getSessions = () => request.get('/interview/sessions')
export const getDetail = (sessionId) => request.get(`/interview/${sessionId}`)
export const deleteSession = (sessionId) => request.delete(`/interview/${sessionId}`)
