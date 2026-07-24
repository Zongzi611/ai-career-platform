import request from './request'

export const createSession = () => request.post('/ai/chat/session')
export const getSessions = () => request.get('/ai/chat/sessions')
export const getHistory = (sessionId) => request.get(`/ai/chat/history/${sessionId}`)
export const deleteSession = (sessionId) => request.delete(`/ai/chat/session/${sessionId}`)
