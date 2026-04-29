package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuche.entity.OrderLog;
import com.zuche.mapper.OrderLogMapper;
import com.zuche.service.OrderLogService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {
    
    @Override
    public List<OrderLog> getLogsByOrderId(Long orderId) {
        return this.list(new LambdaQueryWrapper<OrderLog>()
                .eq(OrderLog::getOrderId, orderId)
                .orderByAsc(OrderLog::getCreateTime));
    }
}
