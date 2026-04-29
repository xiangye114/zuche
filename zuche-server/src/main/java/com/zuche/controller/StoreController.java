package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.entity.Store;
import com.zuche.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
public class StoreController {
    
    private static final Logger log = LoggerFactory.getLogger(StoreController.class);
    
    @Autowired
    private StoreService storeService;
    
    @GetMapping
    public Result<?> getList() {
        log.info("收到获取门店列表请求");
        try {
            List<Store> stores = storeService.list();
            log.info("获取门店列表成功，数量: {}", stores != null ? stores.size() : 0);
            return Result.success(stores);
        } catch (Exception e) {
            log.error("获取门店列表失败", e);
            return Result.error("获取门店列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        log.info("收到获取门店详情请求，id: {}", id);
        try {
            Store store = storeService.getById(id);
            if (store == null) {
                return Result.error("门店不存在");
            }
            return Result.success(store);
        } catch (Exception e) {
            log.error("获取门店详情失败", e);
            return Result.error("获取门店详情失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/admin/{adminId}")
    public Result<?> getByAdminId(@PathVariable Long adminId) {
        log.info("收到根据管理员ID获取门店请求，adminId: {}", adminId);
        try {
            Store store = storeService.getStoreByAdminId(adminId);
            if (store == null) {
                return Result.error("该管理员没有管理的门店");
            }
            return Result.success(store);
        } catch (Exception e) {
            log.error("获取门店失败", e);
            return Result.error("获取门店失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    public Result<?> add(@RequestBody Store store) {
        log.info("收到新增门店请求: {}", store.getName());
        try {
            storeService.save(store);
            return Result.success("门店添加成功");
        } catch (Exception e) {
            log.error("新增门店失败", e);
            return Result.error("新增门店失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Store store) {
        log.info("收到更新门店请求，id: {}", id);
        try {
            store.setId(id);
            storeService.updateById(store);
            return Result.success("门店更新成功");
        } catch (Exception e) {
            log.error("更新门店失败", e);
            return Result.error("更新门店失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        log.info("收到删除门店请求，id: {}", id);
        try {
            storeService.removeById(id);
            return Result.success("门店删除成功");
        } catch (Exception e) {
            log.error("删除门店失败", e);
            return Result.error("删除门店失败: " + e.getMessage());
        }
    }
}
