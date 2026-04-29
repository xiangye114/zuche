package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.common.PageResult;
import com.zuche.entity.Car;
import com.zuche.entity.Store;
import com.zuche.service.CarService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import com.zuche.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {

    private static final Logger log = LoggerFactory.getLogger(CarController.class);

    @Resource
    private CarService carService;

    @Resource
    private JwtUtils jwtUtils;

    @GetMapping
    public Result<?> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String keyword,
            Long brandId,
            Long categoryId,
            String priceRange,
            Long pickupStoreId,
            Long returnStoreId) {
        Page<Car> pageResult = carService.getList(page, size, keyword, brandId, categoryId, priceRange, pickupStoreId);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }

    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(carService.getById(id));
    }

    @GetMapping("/brands")
    public Result<?> getBrands() {
        return Result.success(carService.getBrands());
    }

    @GetMapping("/categories")
    public Result<?> getCategories() {
        return Result.success(carService.getCategories());
    }

    @GetMapping("/stores")
    public Result<?> getStores() {
        log.info("收到获取门店列表请求");
        try {
            List<Store> stores = carService.getStores();
            log.info("获取门店列表成功，数量: {}", stores != null ? stores.size() : 0);
            return Result.success(stores);
        } catch (Exception e) {
            log.error("获取门店列表失败", e);
            return Result.error("获取门店列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/favorite")
    public Result<?> addFavorite(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        carService.addFavorite(userId, id);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{id}/favorite")
    public Result<?> removeFavorite(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        carService.removeFavorite(userId, id);
        return Result.success("取消收藏成功");
    }

    @GetMapping("/favorites")
    public Result<?> getFavorites(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        return Result.success(carService.getFavorites(userId));
    }

    @GetMapping("/{id}/favorite")
    public Result<?> isFavorite(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Long userId = jwtUtils.getUserId(token);
            boolean isFav = carService.isFavorite(userId, id);
            return Result.success(isFav);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return Result.success(false);
        }
    }
}
