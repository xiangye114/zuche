import request from '@/utils/request'

export const orderApi = {
  create: (data) => request.post('/orders', data),
  getList: (params) => request.get('/orders', { params }),
  getDetail: (id) => request.get(`/orders/${id}`),
  cancel: (id) => request.put(`/orders/${id}/cancel`),
  pay: (id) => request.put(`/orders/${id}/pay`),
  review: (id, data) => request.post(`/orders/${id}/review`, data),
  confirmPickup: (orderId, operatorId, operatorName, remark) => 
    request.put(`/orders/${orderId}/pickup`, { operatorId, operatorName, remark }),
  confirmReturn: (orderId, operatorId, operatorName, remark) => 
    request.put(`/orders/${orderId}/return`, { operatorId, operatorName, remark })
}
