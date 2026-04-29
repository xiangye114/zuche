
package com.zuche.dto;

import com.zuche.entity.Car;
import java.util.ArrayList;
import java.util.List;

public class CarRecommendVO extends Car {
    private Integer recommendScore;
    private List<String> recommendReasons;
    
    public CarRecommendVO() {
        this.recommendScore = 0;
        this.recommendReasons = new ArrayList<>();
    }
    
    public CarRecommendVO(Car car) {
        this();
        if (car != null) {
            this.setId(car.getId());
            this.setName(car.getName());
            this.setBrandId(car.getBrandId());
            this.setCategoryId(car.getCategoryId());
            this.setStoreId(car.getStoreId());
            this.setColor(car.getColor());
            this.setPricePerDay(car.getPricePerDay());
            this.setDeposit(car.getDeposit());
            this.setSeats(car.getSeats());
            this.setTransmission(car.getTransmission());
            this.setFuelType(car.getFuelType());
            this.setImage(car.getImage());
            this.setDescription(car.getDescription());
            this.setStatus(car.getStatus());
            this.setCreateTime(car.getCreateTime());
            this.setUpdateTime(car.getUpdateTime());
            this.setDeleted(car.getDeleted());
        }
    }
    
    public Integer getRecommendScore() {
        return recommendScore;
    }
    
    public void setRecommendScore(Integer recommendScore) {
        this.recommendScore = recommendScore;
    }
    
    public List<String> getRecommendReasons() {
        return recommendReasons;
    }
    
    public void setRecommendReasons(List<String> recommendReasons) {
        this.recommendReasons = recommendReasons;
    }
    
    public void addRecommendReason(String reason) {
        if (this.recommendReasons == null) {
            this.recommendReasons = new ArrayList<>();
        }
        if (reason != null && !this.recommendReasons.contains(reason)) {
            this.recommendReasons.add(reason);
        }
    }
}
