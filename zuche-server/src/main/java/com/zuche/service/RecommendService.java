package com.zuche.service;

import com.zuche.dto.CarRecommendVO;
import com.zuche.entity.Car;
import java.util.List;

public interface RecommendService {
    List<Car> getHotCars(int limit);
    List<CarRecommendVO> getRecommendForUser(Long userId, int limit);
    List<Car> getSimilarCars(Long carId, int limit);
}
