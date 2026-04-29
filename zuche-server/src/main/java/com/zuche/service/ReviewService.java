package com.zuche.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zuche.entity.Review;
import com.zuche.dto.ReviewDTO;
import java.util.List;
import java.util.Map;

public interface ReviewService {
    Page<Review> getList(Integer page, Integer size, Long carId, Long userId);
    Review getById(Long id);
    void create(Long userId, ReviewDTO dto);
    void delete(Long id, Long userId);
    List<Map<String, Object>> getCarReviews(Long carId);
    Double getCarAverageRating(Long carId);
}
