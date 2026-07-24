import request from './request'

export const getTypes = () => request.get('/assessment/types')
export const getQuestions = (typeId) => request.get(`/assessment/${typeId}/questions`)
export const startAssessment = (typeId) => request.post('/assessment/start', null, { params: { typeId } })
export const submitAnswer = (data) => request.post('/assessment/submit', data)
export const completeAssessment = (recordId) => request.post(`/assessment/complete/${recordId}`)
export const getResult = (resultId) => request.get(`/assessment/result/${resultId}`)
export const getMyResults = () => request.get('/assessment/my-results')
export const generateReport = (resultId) => request.post(`/assessment/generate-report/${resultId}`, null, { timeout: 180000 })
