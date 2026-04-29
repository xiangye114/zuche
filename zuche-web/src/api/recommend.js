import request from '@/utils/request'

export const recommendApi = {
  getHotCars: (limit = 6) => request.get('/recommend/hot', { params: { limit } }),
  getRecommendForUser: (limit = 6) => request.get('/recommend/for-user', { params: { limit } }),
  getSimilarCars: (carId, limit = 4) => request.get(`/recommend/similar/${carId}`, { params: { limit } })
}
