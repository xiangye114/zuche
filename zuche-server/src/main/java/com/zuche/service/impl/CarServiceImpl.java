package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zuche.entity.*;
import com.zuche.mapper.*;
import com.zuche.service.CarService;
import com.zuche.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {
    
    @Resource
    private CarMapper carMapper;
    @Resource
    private BrandMapper brandMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private StoreMapper storeMapper;
    @Resource
    private FavoriteMapper favoriteMapper;
    
    @Override
    public Page<Car> getList(Integer page, Integer size, String keyword, Long brandId, Long categoryId, String priceRange, Long pickupStoreId) {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getDeleted, 0);
        wrapper.eq(Car::getStatus, 1);  // 只显示可用车辆

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Car::getName, keyword);
        }
        if (brandId != null) {
            wrapper.eq(Car::getBrandId, brandId);
        }
        if (categoryId != null) {
            wrapper.eq(Car::getCategoryId, categoryId);
        }
        if (pickupStoreId != null) {
            wrapper.eq(Car::getStoreId, pickupStoreId);
        }
        if (priceRange != null && !priceRange.isEmpty()) {
            if (priceRange.startsWith("0-")) {
                double maxPrice = Double.parseDouble(priceRange.substring(2));
                wrapper.le(Car::getPricePerDay, maxPrice);
            } else if (priceRange.endsWith("-")) {
                double minPrice = Double.parseDouble(priceRange.substring(0, priceRange.length() - 1));
                wrapper.ge(Car::getPricePerDay, minPrice);
            } else if (priceRange.contains("-")) {
                String[] prices = priceRange.split("-");
                double minPrice = Double.parseDouble(prices[0]);
                double maxPrice = Double.parseDouble(prices[1]);
                wrapper.between(Car::getPricePerDay, minPrice, maxPrice);
            }
        }

        Page<Car> carPage = new Page<>(page, size);
        return carMapper.selectPage(carPage, wrapper);
    }
    
    @Override
    public Car getById(Long id) {
        return carMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void add(Car car) {
        car.setCreateTime(LocalDateTime.now());
        car.setUpdateTime(LocalDateTime.now());
        car.setDeleted(0);
        car.setStatus(1);
        carMapper.insert(car);
    }
    
    @Override
    @Transactional
    public void update(Car car) {
        car.setUpdateTime(LocalDateTime.now());
        carMapper.updateById(car);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Car car = getById(id);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        if (car.getStatus() == 2) {
            throw new BusinessException("车辆已出租，无法删除");
        }
        car.setDeleted(1);
        car.setUpdateTime(LocalDateTime.now());
        carMapper.updateById(car);
    }
    
    @Override
    public List<Brand> getBrands() {
        return brandMapper.selectList(new LambdaQueryWrapper<Brand>().eq(Brand::getDeleted, 0));
    }
    
    @Override
    public List<Category> getCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getDeleted, 0));
    }
    
    @Override
    public List<Store> getStores() {
        try {
            LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Store::getDeleted, 0);
            return storeMapper.selectList(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("查询门店失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void addFavorite(Long userId, Long carId) {
        // 检查是否已经收藏
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getCarId, carId)
            .eq(Favorite::getDeleted, 0));
        
        if (existing != null) {
            throw new BusinessException("已经收藏过该车辆");
        }
        
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setCarId(carId);
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setDeleted(0);
        favoriteMapper.insert(favorite);
    }
    
    @Override
    @Transactional
    public void removeFavorite(Long userId, Long carId) {
        int deleted = favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getCarId, carId)
            .eq(Favorite::getDeleted, 0));
        
        if (deleted == 0) {
            throw new BusinessException("未收藏该车辆");
        }
    }
    
    @Override
    public boolean isFavorite(Long userId, Long carId) {
        Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getCarId, carId)
            .eq(Favorite::getDeleted, 0));
        return favorite != null;
    }
    
    @Override
    public List<Car> getFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getDeleted, 0));
        return favorites.stream()
            .map(fav -> carMapper.selectById(fav.getCarId()))
            .filter(car -> car != null && car.getDeleted() == 0)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<Car> getAvailableCars(Integer page, Integer size, Long storeId, String startTime, String endTime) {
        Page<Car> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<Car>()
            .eq(Car::getDeleted, 0);
        
        if (storeId != null) {
            wrapper.eq(Car::getStoreId, storeId);
        }
        
        wrapper.eq(Car::getStatus, 1);
        
        wrapper.orderByDesc(Car::getCreateTime);
        return carMapper.selectPage(pageParam, wrapper);
    }
}
