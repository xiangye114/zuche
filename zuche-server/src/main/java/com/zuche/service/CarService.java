package com.zuche.service;

import com.zuche.entity.Car;
import com.zuche.entity.Brand;
import com.zuche.entity.Category;
import com.zuche.entity.Store;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

public interface CarService {
    Page<Car> getList(Integer page, Integer size, String keyword, Long brandId, Long categoryId, String priceRange, Long pickupStoreId);
    Car getById(Long id);
    void add(Car car);
    void update(Car car);
    void delete(Long id);
    List<Brand> getBrands();
    List<Category> getCategories();
    List<Store> getStores();
    void addFavorite(Long userId, Long carId);
    void removeFavorite(Long userId, Long carId);
    boolean isFavorite(Long userId, Long carId);
    List<Car> getFavorites(Long userId);
    Page<Car> getAvailableCars(Integer page, Integer size, Long storeId, String startTime, String endTime);
}
