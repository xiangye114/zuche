package com.zuche.common;

import com.zuche.entity.User;

/**
 * 用户上下文工具类
 * 基于ThreadLocal存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }

    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        CURRENT_USER.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static UserRole getCurrentUserRole() {
        User user = getCurrentUser();
        if (user == null || user.getRole() == null) {
            return UserRole.USER;
        }
        return UserRole.getByCode(user.getRole());
    }

    /**
     * 判断当前用户是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return getCurrentUserRole().isSuperAdmin();
    }

    /**
     * 判断当前用户是否为门店管理员
     */
    public static boolean isStoreAdmin() {
        return getCurrentUserRole().isStoreAdmin();
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return getCurrentUserRole().isAdmin();
    }

    /**
     * 获取当前用户的门店ID
     * 仅门店管理员有门店ID
     */
    public static Long getCurrentStoreId() {
        User user = getCurrentUser();
        // 门店管理员的门店ID可以从用户关联信息中获取
        // 这里简化处理，实际项目中可能需要从用户-门店关联表查询
        return null;
    }
}
