import request from '@/utils/request'

export const userApi = {
  getInfo: () => request.get('/auth/info'),
  updateInfo: (data) => request.put('/auth/info', data),
  updatePassword: (data) => request.put('/auth/password', data),
  getOrders: (params) => request.get('/orders', { params }),
  getOrderDetail: (id) => request.get(`/orders/${id}`)
}
