package com.zuche.common;

/**
 * 用户角色枚举
 * 基于users表的role字段定义
 * 0-普通用户，1-超级管理员，2-门店管理员，3-客服，4-财务
 */
public enum UserRole {
    USER(0, "普通用户", "基础操作权限"),
    SUPER_ADMIN(1, "超级管理员", "系统全部操作权限"),
    STORE_ADMIN(2, "门店管理员", "门店相关管理权限"),
    CUSTOMER_SERVICE(3, "客服", "订单处理、客户服务等权限"),
    FINANCE(4, "财务", "财务报表、结算等权限");

    private final int code;
    private final String name;
    private final String description;

    UserRole(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取角色
     */
    public static UserRole getByCode(Integer code) {
        if (code == null) {
            return USER;
        }
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return USER;
    }

    /**
     * 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }

    /**
     * 是否为门店管理员
     */
    public boolean isStoreAdmin() {
        return this == STORE_ADMIN;
    }

    /**
     * 是否为管理员（超级管理员、门店管理员、客服、财务）
     */
    public boolean isAdmin() {
        return this == SUPER_ADMIN || this == STORE_ADMIN || this == CUSTOMER_SERVICE || this == FINANCE;
    }

    /**
     * 是否有订单处理权限（确认取车、还车）
     */
    public boolean hasOrderProcessPermission() {
        return this == SUPER_ADMIN || this == STORE_ADMIN || this == CUSTOMER_SERVICE;
    }

    /**
     * 是否有删除权限
     * 只有超级管理员有删除权限
     */
    public boolean hasDeletePermission() {
        return this == SUPER_ADMIN;
    }

    /**
     * 是否有门店管理权限
     * 只有超级管理员有门店管理权限
     */
    public boolean hasStoreManagePermission() {
        return this == SUPER_ADMIN;
    }

    /**
     * 是否有品牌/分类管理权限
     * 只有超级管理员有修改权限
     */
    public boolean hasBrandCategoryManagePermission() {
        return this == SUPER_ADMIN;
    }
}
