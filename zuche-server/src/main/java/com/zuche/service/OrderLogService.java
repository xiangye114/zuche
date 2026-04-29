package com.zuche.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zuche.entity.OrderLog;
import java.util.List;

public interface OrderLogService extends IService<OrderLog> {
    List<OrderLog> getLogsByOrderId(Long orderId);
}
