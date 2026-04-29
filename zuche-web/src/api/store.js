import request from '@/utils/request'

export const storeApi = {
  getList: () => request.get('/stores'),
  getDetail: (id) => request.get(`/stores/${id}`),
  getByAdminId: (adminId) => request.get(`/stores/admin/${adminId}`),
  add: (data) => request.post('/stores', data),
  update: (id, data) => request.put(`/stores/${id}`, data),
  delete: (id) => request.delete(`/stores/${id}`)
}
