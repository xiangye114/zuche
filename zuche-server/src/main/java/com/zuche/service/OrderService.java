package com.zuche.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zuche.dto.OrderDTO;
import com.zuche.dto.OrderDetailDTO;
import com.zuche.dto.OrderQueryDTO;
import com.zuche.dto.OrderFeeItemDTO;
import com.zuche.dto.OrderLogDTO;
import com.zuche.dto.OrderCarCheckDTO;
import com.zuche.dto.CancelReasonDTO;
import com.zuche.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Page<OrderDTO> getList(Integer page, Integer size, Long userId, Integer status);
    Order getById(Long id);
    OrderDetailDTO getDetail(Long id, Long userId);
    OrderDetailDTO getAdminDetail(Long id);
    void create(Order order);
    void pay(Long id);
    void cancel(Long id);
    void review(Long orderId, Long userId, String content);
    
    void confirmPickup(Long orderId, Long operatorId, String operatorName, String remark);
    void confirmReturn(Long orderId, Long operatorId, String operatorName, String remark);
    Page<OrderDTO> getStoreOrders(Integer page, Integer size, Long storeId, Integer status);
    
    // 新增方法
    Page<OrderDTO> getAdminOrderList(OrderQueryDTO queryDTO);
    List<OrderFeeItemDTO> calculateOrderFees(Long orderId);
    void updateOrderStatusWithLog(Long orderId, Integer newStatus, Long operatorId, String operatorName, String remark);
    List<OrderLogDTO> getOrderLogs(Long orderId);
    
    // ========== 订单状态管理流程优化 - 新增方法 ==========
    
    /**
     * 确认订单（待确认 -> 已确认）
     */
    void confirmOrder(Long orderId, Long operatorId, String operatorName, String remark);
    
    /**
     * 拒绝订单（待确认 -> 已取消）
     */
    void rejectOrder(Long orderId, Long operatorId, String operatorName, String reasonCode, String remark);
    
    /**
     * 检查车辆可用性
     */
    boolean checkCarAvailability(Long carId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    /**
     * 检查用户资质
     */
    boolean checkUserQualification(Long userId);
    
    /**
     * 提交车辆检查记录
     */
    void submitCarChecks(Long orderId, List<OrderCarCheckDTO> checkItems, Long operatorId, String operatorName);
    
    /**
     * 获取车辆检查记录
     */
    List<OrderCarCheckDTO> getCarChecks(Long orderId, Integer checkType);
    
    /**
     * 上报车辆问题
     */
    void reportCarIssue(Long orderId, String issueType, String description, String photos, Long userId);
    
    /**
     * 结算订单（已还车 -> 已完成）
     */
    void settleOrder(Long orderId, Double settlementAmount, Long operatorId, String operatorName, String remark);
    
    /**
     * 取消订单（带原因）
     */
    void cancelOrderWithReason(Long orderId, String reasonCode, String reasonDetail, Long operatorId, String operatorName);
    
    /**
     * 获取取消原因列表
     */
    List<CancelReasonDTO> getCancelReasons(Integer reasonType);
    
    /**
     * 获取订单统计
     */
    Map<String, Object> getOrderStatistics(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, Long storeId);
    
    /**
     * 获取取消统计
     */
    Map<String, Object> getCancelStatistics(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}
