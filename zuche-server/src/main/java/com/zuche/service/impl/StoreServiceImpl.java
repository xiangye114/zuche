package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuche.entity.Store;
import com.zuche.mapper.StoreMapper;
import com.zuche.service.StoreService;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {
    
    @Override
    public Store getStoreByAdminId(Long adminId) {
        return this.getOne(new LambdaQueryWrapper<Store>()
                .eq(Store::getAdminId, adminId));
    }
}
