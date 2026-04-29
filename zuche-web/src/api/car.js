import request from '@/utils/request'

export const carApi = {
  getList: (params) => request.get('/cars', { params }),
  getDetail: (id) => request.get(`/cars/${id}`),
  getBrands: () => request.get('/cars/brands'),
  getCategories: () => request.get('/cars/categories'),
  getStores: () => request.get('/cars/stores'),
  addFavorite: (id) => request.post(`/cars/${id}/favorite`),
  removeFavorite: (id) => request.delete(`/cars/${id}/favorite`),
  isFavorite: (id) => request.get(`/cars/${id}/favorite`),
  getFavorites: () => request.get('/cars/favorites')
}
