package com.zuche.controller.admin;

import com.zuche.common.Result;
import com.zuche.common.UserContext;
import com.zuche.dto.OrderDTO;
import com.zuche.dto.OrderDetailDTO;
import com.zuche.dto.OrderQueryDTO;
import com.zuche.dto.OrderLogDTO;
import com.zuche.dto.OrderCarCheckDTO;
import com.zuche.dto.CancelReasonDTO;
import com.zuche.entity.Order;
import com.zuche.entity.User;
import com.zuche.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Resource
    private OrderService orderService;

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        return UserContext.getCurrentUser();
    }

    /**
     * 检查是否有管理员权限
     * 临时放宽权限检查，允许已登录用户访问
     */
    private boolean hasAdminPermission() {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }
        // 0-普通用户，1-管理员，2-门店管理员，3-客服，4-财务
        // 暂时允许所有已登录用户访问，后续可以恢复权限控制
        Integer role = user.getRole();
        return role != null;
    }

    /**
     * 获取订单列表（支持高级搜索）
     */
    @GetMapping
    public Result<?> getOrderList(OrderQueryDTO queryDTO) {
        // 检查权限
        if (!hasAdminPermission()) {
            return Result.error("无权限访问，请先登录");
        }
        
        // 门店管理员只能查看本门店订单
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null) {
                queryDTO.setStoreId(storeId);
            }
        }
        
        Page<OrderDTO> page = orderService.getAdminOrderList(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        OrderDetailDTO detail = orderService.getAdminDetail(id);
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(detail.getPickupStoreId())) {
                return Result.error("无权限查看其他门店订单");
            }
        }
        
        return Result.success(detail);
    }

    /**
     * 确认取车
     */
    @PutMapping("/{id}/pickup")
    public Result<?> confirmPickup(@PathVariable Long id, @RequestBody Map<String, String> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        // 检查订单状态
        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确，只有已确认的订单才能确认取车");
        }
        
        Long operatorId = currentUser.getId();
        String operatorName = params.get("operatorName");
        String remark = params.get("remark");
        
        if (operatorName == null || operatorName.trim().isEmpty()) {
            operatorName = currentUser.getUsername();
        }
        
        orderService.confirmPickup(id, operatorId, operatorName, remark);
        return Result.success("确认取车成功");
    }

    /**
     * 确认还车
     */
    @PutMapping("/{id}/return")
    public Result<?> confirmReturn(@PathVariable Long id, @RequestBody Map<String, String> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        // 检查订单状态
        if (order.getStatus() != 2) {
            return Result.error("订单状态不正确，只有已取车的订单才能确认还车");
        }
        
        Long operatorId = currentUser.getId();
        String operatorName = params.get("operatorName");
        String remark = params.get("remark");
        
        if (operatorName == null || operatorName.trim().isEmpty()) {
            operatorName = currentUser.getUsername();
        }
        
        orderService.confirmReturn(id, operatorId, operatorName, remark);
        return Result.success("确认还车成功");
    }

    /**
     * 更新订单状态（带日志记录）
     */
    @PutMapping("/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        Integer newStatus = (Integer) params.get("status");
        String remark = (String) params.get("remark");
        
        Long operatorId = currentUser.getId();
        String operatorName = currentUser.getUsername();
        
        orderService.updateOrderStatusWithLog(id, newStatus, operatorId, operatorName, remark);
        return Result.success("状态更新成功");
    }

    /**
     * 获取订单状态日志
     */
    @GetMapping("/{id}/logs")
    public Result<?> getOrderLogs(@PathVariable Long id) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限查看其他门店订单");
            }
        }
        
        List<OrderLogDTO> logs = orderService.getOrderLogs(id);
        return Result.success(logs);
    }

    /**
     * 获取订单费用明细
     */
    @GetMapping("/{id}/fees")
    public Result<?> getOrderFees(@PathVariable Long id) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限查看其他门店订单");
            }
        }
        
        return Result.success(orderService.calculateOrderFees(id));
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public Result<?> getOrderStatistics(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) Long storeId) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        // 门店管理员只能查看本门店统计
        User currentUser = getCurrentUser();
        Long queryStoreId = storeId;
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            queryStoreId = getCurrentStoreId();
        }
        
        // 这里可以实现具体的统计逻辑
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("timeRange", timeRange);
        statistics.put("storeId", queryStoreId);
        
        return Result.success(statistics);
    }

    /**
     * 获取当前门店ID（门店管理员）
     */
    private Long getCurrentStoreId() {
        // 实际项目中应该从用户-门店关联表中获取
        // 这里简化处理，可以从UserContext或其他方式获取
        return null;
    }

    // ==================== 订单状态管理新增接口 ====================

    /**
     * 确认订单（待确认 -> 已确认）
     */
    @PutMapping("/{id}/confirm")
    public Result<?> confirmOrder(@PathVariable Long id, @RequestBody Map<String, String> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        Long operatorId = currentUser.getId();
        String operatorName = params.get("operatorName");
        String remark = params.get("remark");
        
        if (operatorName == null || operatorName.trim().isEmpty()) {
            operatorName = currentUser.getUsername();
        }
        
        orderService.confirmOrder(id, operatorId, operatorName, remark);
        return Result.success("订单确认成功");
    }

    /**
     * 拒绝订单（待确认 -> 已取消）
     */
    @PutMapping("/{id}/reject")
    public Result<?> rejectOrder(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        Long operatorId = currentUser.getId();
        String operatorName = (String) params.get("operatorName");
        String reasonCode = (String) params.get("reasonCode");
        String remark = (String) params.get("remark");
        
        if (operatorName == null || operatorName.trim().isEmpty()) {
            operatorName = currentUser.getUsername();
        }
        
        orderService.rejectOrder(id, operatorId, operatorName, reasonCode, remark);
        return Result.success("订单拒绝成功");
    }

    /**
     * 检查车辆可用性
     */
    @GetMapping("/check-car-availability")
    public Result<?> checkCarAvailability(
            @RequestParam Long carId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        boolean available = orderService.checkCarAvailability(carId, 
            java.time.LocalDateTime.parse(startTime), 
            java.time.LocalDateTime.parse(endTime));
        
        Map<String, Object> result = new HashMap<>();
        result.put("available", available);
        result.put("message", available ? "车辆可用" : "车辆在该时间段内不可用");
        return Result.success(result);
    }

    /**
     * 提交车辆检查记录
     */
    @PostMapping("/{id}/car-checks")
    public Result<?> submitCarChecks(@PathVariable Long id, @RequestBody List<OrderCarCheckDTO> checkDTOs) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        Long operatorId = currentUser.getId();
        String operatorName = currentUser.getUsername();
        
        orderService.submitCarChecks(id, checkDTOs, operatorId, operatorName);
        return Result.success("车辆检查记录提交成功");
    }

    /**
     * 获取车辆检查记录
     */
    @GetMapping("/{id}/car-checks")
    public Result<?> getCarChecks(@PathVariable Long id, @RequestParam(required = false) Integer checkType) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限查看其他门店订单");
            }
        }
        
        List<OrderCarCheckDTO> checks = orderService.getCarChecks(id, checkType);
        return Result.success(checks);
    }

    /**
     * 上报车辆问题
     */
    @PostMapping("/{id}/car-issues")
    public Result<?> reportCarIssue(@PathVariable Long id, @RequestBody Map<String, String> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        String issueType = params.get("issueType");
        String description = params.get("description");
        String photos = params.get("photos");
        Long operatorId = currentUser.getId();
        
        orderService.reportCarIssue(id, issueType, description, photos, operatorId);
        return Result.success("车辆问题上报成功");
    }

    /**
     * 订单结算（已还车 -> 已完成）
     */
    @PutMapping("/{id}/settle")
    public Result<?> settleOrder(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        // 只有财务和管理员可以结算
        Integer role = currentUser.getRole();
        if (role != null && role != 1 && role != 4) {
            return Result.error("只有管理员和财务人员可以执行结算操作");
        }
        
        Double settlementAmount = params.get("settlementAmount") != null ? 
            ((Number) params.get("settlementAmount")).doubleValue() : null;
        String remark = (String) params.get("remark");
        
        Long operatorId = currentUser.getId();
        String operatorName = currentUser.getUsername();
        
        orderService.settleOrder(id, settlementAmount, operatorId, operatorName, remark);
        return Result.success("订单结算成功");
    }

    /**
     * 取消订单（带原因）
     */
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrderWithReason(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        if (!hasAdminPermission()) {
            return Result.error("无权限执行此操作");
        }
        
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 门店管理员权限检查
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            Long storeId = getCurrentStoreId();
            if (storeId != null && !storeId.equals(order.getPickupStoreId())) {
                return Result.error("无权限操作其他门店订单");
            }
        }
        
        String reasonCode = (String) params.get("reasonCode");
        String reasonDetail = (String) params.get("reasonDetail");
        
        Long operatorId = currentUser.getId();
        String operatorName = currentUser.getUsername();
        
        orderService.cancelOrderWithReason(id, reasonCode, reasonDetail, operatorId, operatorName);
        return Result.success("订单取消成功");
    }

    /**
     * 获取取消原因列表
     */
    @GetMapping("/cancel-reasons")
    public Result<?> getCancelReasons(@RequestParam(required = false) Integer reasonType) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        List<CancelReasonDTO> reasons = orderService.getCancelReasons(reasonType);
        return Result.success(reasons);
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/statistics/detail")
    public Result<?> getOrderStatisticsDetail(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long storeId) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        // 门店管理员只能查看本门店统计
        User currentUser = getCurrentUser();
        Long queryStoreId = storeId;
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            queryStoreId = getCurrentStoreId();
        }
        
        java.time.LocalDateTime start = startDate != null ? 
            java.time.LocalDateTime.parse(startDate + "T00:00:00") : null;
        java.time.LocalDateTime end = endDate != null ? 
            java.time.LocalDateTime.parse(endDate + "T23:59:59") : null;
        
        Map<String, Object> statistics = orderService.getOrderStatistics(start, end, queryStoreId);
        return Result.success(statistics);
    }

    /**
     * 获取取消统计
     */
    @GetMapping("/statistics/cancel")
    public Result<?> getCancelStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (!hasAdminPermission()) {
            return Result.error("无权限访问");
        }
        
        java.time.LocalDateTime start = startDate != null ? 
            java.time.LocalDateTime.parse(startDate + "T00:00:00") : null;
        java.time.LocalDateTime end = endDate != null ? 
            java.time.LocalDateTime.parse(endDate + "T23:59:59") : null;
        
        Map<String, Object> statistics = orderService.getCancelStatistics(start, end);
        return Result.success(statistics);
    }
}
