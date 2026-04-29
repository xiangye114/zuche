package com.zuche.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zuche.entity.Store;

public interface StoreService extends IService<Store> {
    Store getStoreByAdminId(Long adminId);
}
