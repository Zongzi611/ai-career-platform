import request from './request'

export const getCareerList = (params) => request.get('/career/list', { params })
export const getCareerDetail = (id) => request.get(`/career/${id}`)
export const getRecommend = (params) => request.get('/career/recommend', { params })
export const getIndustries = () => request.get('/career/industries')
