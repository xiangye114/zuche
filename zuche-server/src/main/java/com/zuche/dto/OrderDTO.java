package com.zuche.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private String userAvatar;
    private Long carId;
    private String carName;
    private String carImage;
    private String carBrand;
    private String carCategory;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer rentalDays;
    private Double dailyRate;
    private Double totalPrice;
    private Double deposit;
    private Long pickupStoreId;
    private String pickupStoreName;
    private Long returnStoreId;
    private String returnStoreName;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
}
