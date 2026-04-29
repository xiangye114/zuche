import request from '@/utils/request'

export const adminApi = {
  // 仪表盘数据
  getDashboardData: () => request({
    url: '/admin/dashboard',
    method: 'get'
  }),
  
  // 车辆管理
  getCars: (params) => request({
    url: '/admin/cars',
    method: 'get',
    params
  }),
  addCar: (data) => request({
    url: '/admin/cars',
    method: 'post',
    data
  }),
  updateCar: (id, data) => request({
    url: `/admin/cars/${id}`,
    method: 'put',
    data
  }),
  deleteCar: (id) => request({
    url: `/admin/cars/${id}`,
    method: 'delete'
  }),
  batchUpdateCarStatus: (data) => request({
    url: '/admin/cars/batch/status',
    method: 'post',
    data
  }),
  batchDeleteCars: (data) => request({
    url: '/admin/cars/batch/delete',
    method: 'post',
    data
  }),
  syncCarStatus: () => request({
    url: '/admin/cars/sync-status',
    method: 'post'
  }),
  exportCars: (params) => request({
    url: '/admin/cars/export',
    method: 'get',
    params,
    responseType: 'blob'
  }),
    
  // 订单管理 - 高级搜索
  getOrders: (params) => request({
    url: '/admin/orders',
    method: 'get',
    params
  }),
  getOrderDetail: (id) => request({
    url: `/admin/orders/${id}`,
    method: 'get'
  }),
  confirmOrder: (id, data) => request({
    url: `/admin/orders/${id}/confirm`,
    method: 'put',
    data
  }),
  rejectOrder: (id, data) => request({
    url: `/admin/orders/${id}/reject`,
    method: 'put',
    data
  }),
  confirmPickup: (id, data) => request({
    url: `/admin/orders/${id}/pickup`,
    method: 'put',
    data
  }),
  confirmReturn: (id, data) => request({
    url: `/admin/orders/${id}/return`,
    method: 'put',
    data
  }),
  settleOrder: (id, data) => request({
    url: `/admin/orders/${id}/settle`,
    method: 'put',
    data
  }),
  cancelOrder: (id, data) => request({
    url: `/admin/orders/${id}/cancel`,
    method: 'put',
    data
  }),
  updateOrderStatus: (id, data) => request({
    url: `/admin/orders/${id}/status`,
    method: 'put',
    data
  }),
  getOrderLogs: (id) => request({
    url: `/admin/orders/${id}/logs`,
    method: 'get'
  }),
  getOrderFees: (id) => request({
    url: `/admin/orders/${id}/fees`,
    method: 'get'
  }),
  // 车辆检查
  submitCarChecks: (id, data) => request({
    url: `/admin/orders/${id}/car-checks`,
    method: 'post',
    data
  }),
  getCarChecks: (id, checkType) => request({
    url: `/admin/orders/${id}/car-checks`,
    method: 'get',
    params: { checkType }
  }),
  reportCarIssue: (id, data) => request({
    url: `/admin/orders/${id}/car-issues`,
    method: 'post',
    data
  }),
  // 取消原因
  getCancelReasons: (params) => request({
    url: '/admin/orders/cancel-reasons',
    method: 'get',
    params
  }),
  // 统计
  getOrderStatistics: (params) => request({
    url: '/admin/orders/statistics',
    method: 'get',
    params
  }),
  getOrderStatisticsDetail: (params) => request({
    url: '/admin/orders/statistics/detail',
    method: 'get',
    params
  }),
  getCancelStatistics: (params) => request({
    url: '/admin/orders/statistics/cancel',
    method: 'get',
    params
  }),
  // 车辆可用性检查
  checkCarAvailability: (params) => request({
    url: '/admin/orders/check-car-availability',
    method: 'get',
    params
  }),
  exportOrders: (params) => request({
    url: '/admin/orders/export',
    method: 'get',
    params,
    responseType: 'blob'
  }),
  
  // 用户管理
  getUsers: (params) => request({
    url: '/admin/users',
    method: 'get',
    params
  }),
  updateUserStatus: (id, status) => request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data: { status }
  }),
  batchUpdateUserStatus: (data) => request({
    url: '/admin/users/batch/status',
    method: 'post',
    data
  }),
  batchDeleteUsers: (data) => request({
    url: '/admin/users/batch/delete',
    method: 'post',
    data
  }),
  addUser: (data) => request({
    url: '/admin/users',
    method: 'post',
    data
  }),
  updateUser: (id, data) => request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  }),
  exportUsers: (params) => request({
    url: '/admin/users/export',
    method: 'get',
    params,
    responseType: 'blob'
  }),
    
  // 品牌管理
  getBrands: () => request({
    url: '/admin/brands',
    method: 'get'
  }),
  addBrand: (data) => request({
    url: '/admin/brands',
    method: 'post',
    data
  }),
  updateBrand: (id, data) => request({
    url: `/admin/brands/${id}`,
    method: 'put',
    data
  }),
  deleteBrand: (id) => request({
    url: `/admin/brands/${id}`,
    method: 'delete'
  }),
  
  // 分类管理
  getCategories: () => request({
    url: '/admin/categories',
    method: 'get'
  }),
  addCategory: (data) => request({
    url: '/admin/categories',
    method: 'post',
    data
  }),
  updateCategory: (id, data) => request({
    url: `/admin/categories/${id}`,
    method: 'put',
    data
  }),
  deleteCategory: (id) => request({
    url: `/admin/categories/${id}`,
    method: 'delete'
  }),
  
  // 门店管理
  getStores: (params) => request({
    url: '/admin/stores',
    method: 'get',
    params
  }),
  addStore: (data) => request({
    url: '/admin/stores',
    method: 'post',
    data
  }),
  updateStore: (id, data) => request({
    url: `/admin/stores/${id}`,
    method: 'put',
    data
  }),
  deleteStore: (id) => request({
    url: `/admin/stores/${id}`,
    method: 'delete'
  }),
  
  // 知识库管理
  getKnowledge: (params) => request({
    url: '/admin/knowledge',
    method: 'get',
    params
  }),
  addKnowledge: (data) => request({
    url: '/admin/knowledge',
    method: 'post',
    data
  }),
  updateKnowledge: (id, data) => request({
    url: `/admin/knowledge/${id}`,
    method: 'put',
    data
  }),
  deleteKnowledge: (id) => request({
    url: `/admin/knowledge/${id}`,
    method: 'delete'
  })
}
