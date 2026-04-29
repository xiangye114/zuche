package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.common.PageResult;
import com.zuche.dto.OrderDTO;
import com.zuche.entity.Order;
import com.zuche.entity.OrderLog;
import com.zuche.entity.Store;
import com.zuche.service.OrderService;
import com.zuche.service.OrderLogService;
import com.zuche.service.StoreService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import com.zuche.utils.JwtUtils;
import com.zuche.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private OrderLogService orderLogService;

    @Resource
    private StoreService storeService;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录或token无效");
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserId(token);
    }

    @GetMapping
    public Result<?> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            log.info("开始获取订单列表, page={}, size={}, status={}", page, size, status);
            Long userId = getUserIdFromRequest(request);
            log.info("获取到用户ID: {}", userId);
            Page<OrderDTO> pageResult = orderService.getList(page, size, userId, status);
            log.info("查询到订单数量: {}", pageResult.getRecords().size());
            return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return Result.success(orderService.getDetail(id, userId));
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<?> create(@RequestBody Order order, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        order.setUserId(userId);
        orderService.create(order);
        return Result.success(order);
    }

    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        try {
            orderService.pay(id);
            return Result.success("支付成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("支付失败", e);
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success("订单取消成功");
    }

    @PostMapping("/{id}/review")
    public Result<?> review(@PathVariable Long id, @RequestBody String content, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        orderService.review(id, userId, content);
        return Result.success("评价成功");
    }



    @PutMapping("/{id}/pickup")
    public Result<?> confirmPickup(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer role = jwtUtils.getRole(request.getHeader("Authorization").substring(7));
            String operatorName = params.get("operatorName");
            String remark = params.get("remark");
            
            if (operatorName == null) {
                operatorName = "用户";
            }
            
            orderService.confirmPickup(id, userId, operatorName, remark);
            return Result.success("确认取车成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("确认取车失败", e);
            return Result.error("确认取车失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/return")
    public Result<?> confirmReturn(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            String operatorName = params.get("operatorName");
            String remark = params.get("remark");
            
            if (operatorName == null) {
                operatorName = "用户";
            }
            
            orderService.confirmReturn(id, userId, operatorName, remark);
            return Result.success("确认还车成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("确认还车失败", e);
            return Result.error("确认还车失败: " + e.getMessage());
        }
    }

    @GetMapping("/store/orders")
    public Result<?> getStoreOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer role = jwtUtils.getRole(request.getHeader("Authorization").substring(7));
            
            if (role == null || role != 2) {
                return Result.error("无权限访问");
            }
            
            Store store = storeService.getStoreByAdminId(userId);
            if (store == null) {
                return Result.error("您尚未绑定门店");
            }
            
            Page<OrderDTO> pageResult = orderService.getStoreOrders(page, size, store.getId(), status);
            return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取门店订单失败", e);
            return Result.error("获取门店订单失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/logs")
    public Result<?> getOrderLogs(@PathVariable Long id) {
        try {
            List<OrderLog> logs = orderLogService.getLogsByOrderId(id);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取订单日志失败", e);
            return Result.error("获取订单日志失败: " + e.getMessage());
        }
    }
}