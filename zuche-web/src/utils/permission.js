import { useUserStore } from '@/store/user'

/**
 * 用户角色枚举
 */
export const UserRole = {
  USER: 0,
  SUPER_ADMIN: 1,
  STORE_ADMIN: 2
}

/**
 * 角色名称映射
 */
export const RoleNameMap = {
  [UserRole.USER]: '普通用户',
  [UserRole.SUPER_ADMIN]: '超级管理员',
  [UserRole.STORE_ADMIN]: '门店管理员'
}

/**
 * 获取当前用户角色
 */
export function getCurrentRole() {
  const userStore = useUserStore()
  return userStore.user?.role ?? UserRole.USER
}

/**
 * 判断是否为超级管理员
 */
export function isSuperAdmin() {
  return getCurrentRole() === UserRole.SUPER_ADMIN
}

/**
 * 判断是否为门店管理员
 */
export function isStoreAdmin() {
  return getCurrentRole() === UserRole.STORE_ADMIN
}

/**
 * 判断是否为管理员（超级管理员或门店管理员）
 */
export function isAdmin() {
  const role = getCurrentRole()
  return role === UserRole.SUPER_ADMIN || role === UserRole.STORE_ADMIN
}

/**
 * 判断是否有删除权限
 * 只有超级管理员有删除权限
 */
export function hasDeletePermission() {
  return isSuperAdmin()
}

/**
 * 判断是否有门店管理权限
 * 只有超级管理员有门店管理权限
 */
export function hasStoreManagePermission() {
  return isSuperAdmin()
}

/**
 * 判断是否有品牌/分类管理权限
 * 只有超级管理员有修改权限
 */
export function hasBrandCategoryManagePermission() {
  return isSuperAdmin()
}

/**
 * 权限检查函数
 * @param {Array|Number} roles - 允许的角色或角色数组
 * @returns {Boolean}
 */
export function hasRole(roles) {
  if (!roles) return true
  const currentRole = getCurrentRole()
  if (Array.isArray(roles)) {
    return roles.includes(currentRole)
  }
  return currentRole === roles
}

/**
 * 检查是否有权限访问菜单
 * @param {String} menuKey - 菜单标识
 */
export function hasMenuPermission(menuKey) {
  const role = getCurrentRole()
  
  // 权限配置
  const menuPermissions = {
    'dashboard': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN],
    'cars': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN],
    'orders': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN],
    'users': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN],
    'stores': [UserRole.SUPER_ADMIN], // 只有超级管理员
    'brands': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN], // 门店管理员只能查看
    'categories': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN], // 门店管理员只能查看
    'knowledge': [UserRole.SUPER_ADMIN, UserRole.STORE_ADMIN]
  }
  
  const allowedRoles = menuPermissions[menuKey]
  if (!allowedRoles) return false
  
  return allowedRoles.includes(role)
}

/**
 * 检查是否有按钮操作权限
 * @param {String} action - 操作类型：delete, edit, add
 * @param {String} module - 模块名称
 */
export function hasButtonPermission(action, module) {
  const role = getCurrentRole()
  
  // 删除操作只有超级管理员可以执行
  if (action === 'delete') {
    return role === UserRole.SUPER_ADMIN
  }
  
  // 品牌/分类的增改只有超级管理员可以执行
  if ((module === 'brands' || module === 'categories') && (action === 'add' || action === 'edit')) {
    return role === UserRole.SUPER_ADMIN
  }
  
  // 门店管理只有超级管理员可以执行
  if (module === 'stores' && (action === 'add' || action === 'edit' || action === 'delete')) {
    return role === UserRole.SUPER_ADMIN
  }
  
  // 其他操作，管理员都可以执行
  return role === UserRole.SUPER_ADMIN || role === UserRole.STORE_ADMIN
}

export default {
  UserRole,
  RoleNameMap,
  getCurrentRole,
  isSuperAdmin,
  isStoreAdmin,
  isAdmin,
  hasDeletePermission,
  hasStoreManagePermission,
  hasBrandCategoryManagePermission,
  hasRole,
  hasMenuPermission,
  hasButtonPermission
}
