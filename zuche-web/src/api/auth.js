import request from '@/utils/request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getInfo: () => request.get('/auth/info'),
  updateInfo: (data) => request.put('/auth/info', data),
  updatePassword: (data) => request.put('/auth/password', data)
}
