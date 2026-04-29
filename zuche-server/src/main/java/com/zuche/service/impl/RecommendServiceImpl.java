
package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zuche.dto.CarRecommendVO;
import com.zuche.entity.Car;
import com.zuche.entity.Favorite;
import com.zuche.entity.Order;
import com.zuche.mapper.CarMapper;
import com.zuche.mapper.FavoriteMapper;
import com.zuche.mapper.OrderMapper;
import com.zuche.service.RecommendService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {
    
    @Resource
    private CarMapper carMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private FavoriteMapper favoriteMapper;
    
    private static final String REASON_BRAND_MATCH = "BRAND_MATCH";
    private static final String REASON_CATEGORY_MATCH = "CATEGORY_MATCH";
    private static final String REASON_PRICE_MATCH = "PRICE_MATCH";
    private static final String REASON_HOT = "HOT";
    
    @Override
    public List<Car> getHotCars(int limit) {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getDeleted, 0)
               .eq(Car::getStatus, 1)
               .orderByDesc(Car::getCreateTime)
               .last("LIMIT " + limit);
        return carMapper.selectList(wrapper);
    }
    
    @Override
    public List<CarRecommendVO> getRecommendForUser(Long userId, int limit) {
        List<Long> preferredBrandIds = new ArrayList<>();
        List<Long> preferredCategoryIds = new ArrayList<>();
        Double avgPrice = null;
        
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                   .eq(Order::getDeleted, 0)
                   .in(Order::getStatus, 1, 2, 3);
        List<Order> orders = orderMapper.selectList(orderWrapper);
        
        if (!orders.isEmpty()) {
            for (Order order : orders) {
                Car car = carMapper.selectById(order.getCarId());
                if (car != null) {
                    if (car.getBrandId() != null) {
                        preferredBrandIds.add(car.getBrandId());
                    }
                    if (car.getCategoryId() != null) {
                        preferredCategoryIds.add(car.getCategoryId());
                    }
                }
            }
            
            double total = orders.stream()
                    .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                    .sum();
            avgPrice = total / orders.size();
        }
        
        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId);
        List<Favorite> favorites = favoriteMapper.selectList(favWrapper);
        
        for (Favorite fav : favorites) {
            Car car = carMapper.selectById(fav.getCarId());
            if (car != null) {
                if (car.getBrandId() != null) {
                    preferredBrandIds.add(car.getBrandId());
                }
                if (car.getCategoryId() != null) {
                    preferredCategoryIds.add(car.getCategoryId());
                }
            }
        }
        
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getDeleted, 0)
               .eq(Car::getStatus, 1);
        
        if (!preferredBrandIds.isEmpty()) {
            wrapper.in(Car::getBrandId, preferredBrandIds.stream().distinct().collect(Collectors.toList()));
        }
        
        List<Car> cars = carMapper.selectList(wrapper);
        
        if (cars.isEmpty()) {
            return getHotCarsWithScore(limit);
        }
        
        List<CarRecommendVO> recommendVOs = new ArrayList<>();
        for (Car car : cars) {
            CarRecommendVO vo = new CarRecommendVO(car);
            int score = 0;
            
            if (preferredBrandIds.contains(car.getBrandId())) {
                score += 10;
                vo.addRecommendReason(REASON_BRAND_MATCH);
            }
            if (preferredCategoryIds.contains(car.getCategoryId())) {
                score += 5;
                vo.addRecommendReason(REASON_CATEGORY_MATCH);
            }
            if (avgPrice != null && car.getPricePerDay() != null) {
                double priceDiff = Math.abs(car.getPricePerDay() - avgPrice);
                if (priceDiff < 50) {
                    score += 8;
                    vo.addRecommendReason(REASON_PRICE_MATCH);
                } else if (priceDiff < 100) {
                    score += 5;
                    vo.addRecommendReason(REASON_PRICE_MATCH);
                }
            }
            
            vo.setRecommendScore(score);
            recommendVOs.add(vo);
        }
        
        return recommendVOs.stream()
                .sorted((a, b) -> b.getRecommendScore() - a.getRecommendScore())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    private List<CarRecommendVO> getHotCarsWithScore(int limit) {
        List<Car> hotCars = getHotCars(limit);
        return hotCars.stream()
                .map(car -> {
                    CarRecommendVO vo = new CarRecommendVO(car);
                    vo.setRecommendScore(5);
                    vo.addRecommendReason(REASON_HOT);
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Car> getSimilarCars(Long carId, int limit) {
        Car car = carMapper.selectById(carId);
        if (car == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getDeleted, 0)
               .eq(Car::getStatus, 1)
               .ne(Car::getId, carId);
        
        if (car.getBrandId() != null) {
            wrapper.eq(Car::getBrandId, car.getBrandId());
        } else if (car.getCategoryId() != null) {
            wrapper.eq(Car::getCategoryId, car.getCategoryId());
        }
        
        wrapper.orderByDesc(Car::getCreateTime)
               .last("LIMIT " + limit);
        
        List<Car> similarCars = carMapper.selectList(wrapper);
        
        if (similarCars.size() < limit) {
            LambdaQueryWrapper<Car> extraWrapper = new LambdaQueryWrapper<>();
            extraWrapper.eq(Car::getDeleted, 0)
                       .eq(Car::getStatus, 1)
                       .ne(Car::getId, carId);
            
            if (!similarCars.isEmpty()) {
                extraWrapper.notIn(Car::getId, similarCars.stream().map(Car::getId).collect(Collectors.toList()));
            }
            
            extraWrapper.orderByDesc(Car::getCreateTime)
                       .last("LIMIT " + (limit - similarCars.size()));
            similarCars.addAll(carMapper.selectList(extraWrapper));
        }
        
        return similarCars;
    }
}
