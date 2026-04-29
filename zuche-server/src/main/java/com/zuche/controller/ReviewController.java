package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.common.PageResult;
import com.zuche.dto.ReviewDTO;
import com.zuche.entity.Review;
import com.zuche.service.ReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zuche.utils.JwtUtils;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    
    @Resource
    private ReviewService reviewService;
    
    @Resource
    private JwtUtils jwtUtils;
    
    @GetMapping
    public Result<?> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Long carId,
            Long userId) {
        Page<Review> pageResult = reviewService.getList(page, size, carId, userId);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }
    
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(reviewService.getById(id));
    }
    
    @PostMapping
    public Result<?> create(@Valid @RequestBody ReviewDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        reviewService.create(userId, dto);
        return Result.success("评价成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        reviewService.delete(id, userId);
        return Result.success("删除成功");
    }
    
    @GetMapping("/car/{carId}")
    public Result<?> getCarReviews(@PathVariable Long carId) {
        List<Map<String, Object>> reviews = reviewService.getCarReviews(carId);
        Double avgRating = reviewService.getCarAverageRating(carId);
        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviews);
        data.put("avgRating", avgRating);
        return Result.success(data);
    }
}
