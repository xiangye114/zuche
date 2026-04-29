import request from '@/utils/request'

export const reviewApi = {
  getList: (params) => request.get('/reviews', { params }),
  getDetail: (id) => request.get(`/reviews/${id}`),
  create: (data) => request.post('/reviews', data),
  delete: (id) => request.delete(`/reviews/${id}`),
  getCarReviews: (carId) => request.get(`/reviews/car/${carId}`)
}
