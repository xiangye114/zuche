package com.zuche.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {
    // 基础订单信息
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String userRealName;
    private String userIdCard;
    private Integer userIdCardType;
    private String userIdCardTypeText;
    private String userAvatar;
    
    // 车辆详细信息
    private Long carId;
    private String carName;
    private String carImage;
    private String carBrand;
    private String carCategory;
    private String carColor;
    private Integer carSeats;
    private String carTransmission;
    private String carFuelType;
    private Double carPricePerDay;
    private Double carDeposit;
    private String carPlateNumber;
    private String carVin;
    
    // 租赁信息
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer rentalDays;
    private Double dailyRate;
    
    // 费用明细（预约时）
    private Double rentalFee;
    private Double deposit;
    private Double insuranceFee;
    private Double serviceFee;
    private Double overtimeFee;
    private Double mileageFee;
    private Double discountAmount;
    private Double totalPrice;
    private List<OrderFeeItemDTO> feeItems;
    
    // 实际费用明细（结算时计算）
    private Integer actualRentalDays;
    private Double actualRentalFee;
    private Double actualInsuranceFee;
    private Double actualServiceFee;
    private Double actualOvertimeFee;
    private Double settlementAmount;
    
    // 门店信息
    private Long pickupStoreId;
    private String pickupStoreName;
    private String pickupStoreAddress;
    private String pickupStorePhone;
    private Long returnStoreId;
    private String returnStoreName;
    private String returnStoreAddress;
    private String returnStorePhone;
    
    // 取还车信息
    private LocalDateTime pickupTime;
    private String pickupOperator;
    private String pickupRemark;
    private LocalDateTime returnTime;
    private String returnOperator;
    private String returnRemark;
    
    // 状态信息
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payTime;
    
    // 评价信息
    private String reviewContent;
    private Integer reviewRating;
    private LocalDateTime reviewTime;
    
    // 状态变更日志
    private List<OrderLogDTO> statusLogs;
    
    // 操作权限
    private Boolean canConfirmPickup;
    private Boolean canConfirmReturn;
    private Boolean canCancel;
    private Boolean canModify;
}
