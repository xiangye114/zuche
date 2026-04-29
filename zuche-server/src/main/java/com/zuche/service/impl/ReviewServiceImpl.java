package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zuche.dto.ReviewDTO;
import com.zuche.entity.Order;
import com.zuche.entity.Review;
import com.zuche.exception.BusinessException;
import com.zuche.mapper.OrderMapper;
import com.zuche.mapper.ReviewMapper;
import com.zuche.mapper.UserMapper;
import com.zuche.mapper.CarMapper;
import com.zuche.entity.User;
import com.zuche.entity.Car;
import com.zuche.service.ReviewService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    
    @Resource
    private ReviewMapper reviewMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private CarMapper carMapper;
    
    @Override
    public Page<Review> getList(Integer page, Integer size, Long carId, Long userId) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getDeleted, 0);
        
        if (carId != null) {
            wrapper.eq(Review::getCarId, carId);
        }
        if (userId != null) {
            wrapper.eq(Review::getUserId, userId);
        }
        
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(pageParam, wrapper);
    }
    
    @Override
    public Review getById(Long id) {
        return reviewMapper.selectById(id);
    }
    
    @Override
    public void create(Long userId, ReviewDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此订单");
        }
        if (order.getStatus() != 4 && order.getStatus() != 3) {
            throw new BusinessException("订单未完成，无法评价");
        }
        
        LambdaQueryWrapper<Review> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Review::getOrderId, dto.getOrderId())
                   .eq(Review::getDeleted, 0);
        if (reviewMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("该订单已评价");
        }
        
        Review review = new Review();
        review.setUserId(userId);
        review.setCarId(dto.getCarId());
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setCreateTime(LocalDateTime.now());
        review.setDeleted(0);
        reviewMapper.insert(review);
    }
    
    @Override
    public void delete(Long id, Long userId) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评价");
        }
        review.setDeleted(1);
        reviewMapper.updateById(review);
    }
    
    @Override
    public List<Map<String, Object>> getCarReviews(Long carId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCarId, carId)
               .eq(Review::getDeleted, 0)
               .orderByDesc(Review::getCreateTime);
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        return reviews.stream().map(review -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", review.getId());
            result.put("userId", review.getUserId());
            result.put("carId", review.getCarId());
            result.put("orderId", review.getOrderId());
            result.put("rating", review.getRating());
            result.put("content", review.getContent());
            result.put("createTime", review.getCreateTime());
            
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                result.put("userName", user.getUsername());
                result.put("userAvatar", user.getAvatar());
            } else {
                result.put("userName", "匿名用户");
                result.put("userAvatar", null);
            }
            
            Car car = carMapper.selectById(review.getCarId());
            if (car != null) {
                result.put("carName", car.getName());
            } else {
                result.put("carName", "未知车辆");
            }
            
            return result;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Double getCarAverageRating(Long carId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCarId, carId)
               .eq(Review::getDeleted, 0);
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        if (reviews.isEmpty()) {
            return 0.0;
        }
        
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
}
