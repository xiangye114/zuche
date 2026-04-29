package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.dto.CarRecommendVO;
import com.zuche.entity.Car;
import com.zuche.service.RecommendService;
import com.zuche.utils.JwtUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {
    
    @Resource
    private RecommendService recommendService;
    
    @Resource
    private JwtUtils jwtUtils;
    
    @GetMapping("/hot")
    public Result<?> getHotCars(@RequestParam(defaultValue = "6") Integer limit) {
        List<Car> cars = recommendService.getHotCars(limit);
        return Result.success(cars);
    }
    
    @GetMapping("/for-user")
    public Result<?> getRecommendForUser(
            @RequestParam(defaultValue = "6") Integer limit,
            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.success(recommendService.getHotCars(limit));
            }
            String token = authHeader.substring(7);
            Long userId = jwtUtils.getUserId(token);
            List<CarRecommendVO> cars = recommendService.getRecommendForUser(userId, limit);
            return Result.success(cars);
        } catch (Exception e) {
            return Result.success(recommendService.getHotCars(limit));
        }
    }
    
    @GetMapping("/similar/{carId}")
    public Result<?> getSimilarCars(
            @PathVariable Long carId,
            @RequestParam(defaultValue = "4") Integer limit) {
        List<Car> cars = recommendService.getSimilarCars(carId, limit);
        return Result.success(cars);
    }
}
