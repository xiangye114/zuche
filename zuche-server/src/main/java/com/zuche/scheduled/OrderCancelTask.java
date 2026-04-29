package com.zuche.scheduled;

import com.zuche.entity.Order;
import com.zuche.entity.Car;
import com.zuche.mapper.OrderMapper;
import com.zuche.mapper.CarMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 * 注意：已移除自动将订单状态更新为"用车中"的定时任务
 * 订单状态流转必须通过人工确认：
 * 0-待确认 -> 1-已确认(代取车) -> 2-已取车(用车中) -> 3-已还车 -> 4-已完成
 */
@Slf4j
@Component
public class OrderCancelTask {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CarMapper carMapper;

    /**
     * 自动取消超时未支付订单（30分钟）
     * 每1分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void cancelUnpaidOrders() {
        log.info("开始检查未支付订单...");

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<Order> unpaidOrders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 0)
                        .lt(Order::getCreateTime, thirtyMinutesAgo)
                        .eq(Order::getDeleted, 0)
        );

        for (Order order : unpaidOrders) {
            try {
                order.setStatus(5); // 已取消
                order.setCancelTime(LocalDateTime.now());
                order.setCancelBy("系统");
                order.setCancelReason("超时未支付");
                order.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);

                if (order.getCarId() != null) {
                    Car car = carMapper.selectById(order.getCarId());
                    if (car != null) {
                        car.setStatus(1);
                        car.setUpdateTime(LocalDateTime.now());
                        carMapper.updateById(car);
                    }
                }

                log.info("自动取消订单: {}", order.getOrderNo());
            } catch (Exception e) {
                log.error("取消订单失败: {}", order.getOrderNo(), e);
            }
        }

        log.info("检查完成，取消了 {} 个未支付订单", unpaidOrders.size());
    }

    /**
     * 【已禁用】自动更新订单状态为用车中
     * 注意：此功能已禁用，订单状态必须通过门店管理员人工确认取车后才会变更为"用车中"
     * 正确的流程：用户支付(状态1) -> 门店管理员确认取车(状态2)
     */
    // @Scheduled(cron = "0 */10 * * * ?")
    // @Transactional
    // public void updateOrderStatusToInProgress() {
    //     log.info("开始更新订单状态为进行中...");
    //     // 此定时任务已禁用，确保订单状态只能通过人工确认变更
    // }

    /**
     * 自动完成已超时订单
     * 每10分钟执行一次
     * 将实际还车时间已过期但状态仍为"已还车"的订单自动标记为"已完成"
     * 
     * 改进说明：使用实际还车时间(returnTime)而非预约还车时间(endTime)判断
     * 确保基于真实业务时间进行状态流转
     */
    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional
    public void updateOrderStatusToCompleted() {
        log.info("开始更新订单状态为已完成...");

        LocalDateTime now = LocalDateTime.now();
        // 只处理状态为"已还车"(3)的订单，自动标记为"已完成"(4)
        // 改进：使用实际还车时间(returnTime)判断，而非预约还车时间(endTime)
        List<Order> returnedOrders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 3)
                        .lt(Order::getReturnTime, now.minusHours(24)) // 实际还车24小时后自动完成
                        .eq(Order::getDeleted, 0)
        );

        for (Order order : returnedOrders) {
            try {
                order.setStatus(4); // 已完成
                order.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);

                log.info("订单状态更新为已完成: {}, 实际还车时间: {}", 
                    order.getOrderNo(), order.getReturnTime());
            } catch (Exception e) {
                log.error("更新订单状态失败: {}", order.getOrderNo(), e);
            }
        }

        log.info("更新完成，将 {} 个订单状态设置为已完成", returnedOrders.size());
    }
}
