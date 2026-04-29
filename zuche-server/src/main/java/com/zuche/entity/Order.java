package com.zuche.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;

@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long carId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // 费用相关字段
    private Integer rentalDays;
    private Double dailyRate;
    private Double rentalFee;
    private Double insuranceFee;
    private Double serviceFee;
    private Double overtimeFee;
    private Double mileageFee;
    private Double discountAmount;
    private Double totalPrice;
    private Double deposit;
    
    private Long pickupStoreId;
    private Long returnStoreId;
    private LocalDateTime pickupTime;
    private String pickupOperator;
    private String pickupRemark;
    private LocalDateTime returnTime;
    private String returnOperator;
    private String returnRemark;
    
    // 车辆检查字段
    private Integer mileageOut;
    private Integer mileageIn;
    private Integer fuelLevelOut;
    private Integer fuelLevelIn;
    private String damageDescription;
    private String damagePhotos;
    
    // 时间记录字段
    private LocalDateTime payTime;
    private LocalDateTime confirmTime;
    private LocalDateTime cancelTime;
    
    // 取消信息字段
    private String cancelReason;
    private String cancelBy;
    private String cancelReasonCode;
    
    // 结算字段
    private Double settlementAmount;
    private LocalDateTime settlementTime;
    private String settlementBy;
    
    // 实际费用字段（结算时计算）
    private Integer actualRentalDays; // 实际租车天数
    private Double actualRentalFee; // 实际租金
    private Double actualInsuranceFee; // 实际保险费用
    private Double actualServiceFee; // 实际服务费用
    private Double actualOvertimeFee; // 实际超时费用
    
    private Integer status; // 0-待确认，1-已确认，2-已取车，3-已还车，4-已完成，5-已取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getCarId() {
        return carId;
    }
    
    public void setCarId(Long carId) {
        this.carId = carId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getRentalDays() {
        return rentalDays;
    }
    
    public void setRentalDays(Integer rentalDays) {
        this.rentalDays = rentalDays;
    }
    
    public Double getDailyRate() {
        return dailyRate;
    }
    
    public void setDailyRate(Double dailyRate) {
        this.dailyRate = dailyRate;
    }
    
    public Double getRentalFee() {
        return rentalFee;
    }
    
    public void setRentalFee(Double rentalFee) {
        this.rentalFee = rentalFee;
    }
    
    public Double getInsuranceFee() {
        return insuranceFee;
    }
    
    public void setInsuranceFee(Double insuranceFee) {
        this.insuranceFee = insuranceFee;
    }
    
    public Double getServiceFee() {
        return serviceFee;
    }
    
    public void setServiceFee(Double serviceFee) {
        this.serviceFee = serviceFee;
    }
    
    public Double getOvertimeFee() {
        return overtimeFee;
    }
    
    public void setOvertimeFee(Double overtimeFee) {
        this.overtimeFee = overtimeFee;
    }
    
    public Double getMileageFee() {
        return mileageFee;
    }
    
    public void setMileageFee(Double mileageFee) {
        this.mileageFee = mileageFee;
    }
    
    public Double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public Double getDeposit() {
        return deposit;
    }
    
    public void setDeposit(Double deposit) {
        this.deposit = deposit;
    }
    
    public Long getPickupStoreId() {
        return pickupStoreId;
    }
    
    public void setPickupStoreId(Long pickupStoreId) {
        this.pickupStoreId = pickupStoreId;
    }
    
    public Long getReturnStoreId() {
        return returnStoreId;
    }
    
    public void setReturnStoreId(Long returnStoreId) {
        this.returnStoreId = returnStoreId;
    }
    
    public LocalDateTime getPickupTime() {
        return pickupTime;
    }
    
    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }
    
    public String getPickupOperator() {
        return pickupOperator;
    }
    
    public void setPickupOperator(String pickupOperator) {
        this.pickupOperator = pickupOperator;
    }
    
    public String getPickupRemark() {
        return pickupRemark;
    }
    
    public void setPickupRemark(String pickupRemark) {
        this.pickupRemark = pickupRemark;
    }
    
    public LocalDateTime getReturnTime() {
        return returnTime;
    }
    
    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }
    
    public String getReturnOperator() {
        return returnOperator;
    }
    
    public void setReturnOperator(String returnOperator) {
        this.returnOperator = returnOperator;
    }
    
    public String getReturnRemark() {
        return returnRemark;
    }
    
    public void setReturnRemark(String returnRemark) {
        this.returnRemark = returnRemark;
    }
    
    public Integer getMileageOut() {
        return mileageOut;
    }
    
    public void setMileageOut(Integer mileageOut) {
        this.mileageOut = mileageOut;
    }
    
    public Integer getMileageIn() {
        return mileageIn;
    }
    
    public void setMileageIn(Integer mileageIn) {
        this.mileageIn = mileageIn;
    }
    
    public Integer getFuelLevelOut() {
        return fuelLevelOut;
    }
    
    public void setFuelLevelOut(Integer fuelLevelOut) {
        this.fuelLevelOut = fuelLevelOut;
    }
    
    public Integer getFuelLevelIn() {
        return fuelLevelIn;
    }
    
    public void setFuelLevelIn(Integer fuelLevelIn) {
        this.fuelLevelIn = fuelLevelIn;
    }
    
    public String getDamageDescription() {
        return damageDescription;
    }
    
    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }
    
    public String getDamagePhotos() {
        return damagePhotos;
    }
    
    public void setDamagePhotos(String damagePhotos) {
        this.damagePhotos = damagePhotos;
    }
    
    public LocalDateTime getPayTime() {
        return payTime;
    }
    
    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }
    
    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }
    
    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }
    
    public LocalDateTime getCancelTime() {
        return cancelTime;
    }
    
    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }
    
    public String getCancelReason() {
        return cancelReason;
    }
    
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
    
    public String getCancelBy() {
        return cancelBy;
    }
    
    public void setCancelBy(String cancelBy) {
        this.cancelBy = cancelBy;
    }
    
    public String getCancelReasonCode() {
        return cancelReasonCode;
    }
    
    public void setCancelReasonCode(String cancelReasonCode) {
        this.cancelReasonCode = cancelReasonCode;
    }
    
    public Double getSettlementAmount() {
        return settlementAmount;
    }
    
    public void setSettlementAmount(Double settlementAmount) {
        this.settlementAmount = settlementAmount;
    }
    
    public LocalDateTime getSettlementTime() {
        return settlementTime;
    }
    
    public void setSettlementTime(LocalDateTime settlementTime) {
        this.settlementTime = settlementTime;
    }
    
    public String getSettlementBy() {
        return settlementBy;
    }
    
    public void setSettlementBy(String settlementBy) {
        this.settlementBy = settlementBy;
    }
    
    public Integer getActualRentalDays() {
        return actualRentalDays;
    }
    
    public void setActualRentalDays(Integer actualRentalDays) {
        this.actualRentalDays = actualRentalDays;
    }
    
    public Double getActualRentalFee() {
        return actualRentalFee;
    }
    
    public void setActualRentalFee(Double actualRentalFee) {
        this.actualRentalFee = actualRentalFee;
    }
    
    public Double getActualInsuranceFee() {
        return actualInsuranceFee;
    }
    
    public void setActualInsuranceFee(Double actualInsuranceFee) {
        this.actualInsuranceFee = actualInsuranceFee;
    }
    
    public Double getActualServiceFee() {
        return actualServiceFee;
    }
    
    public void setActualServiceFee(Double actualServiceFee) {
        this.actualServiceFee = actualServiceFee;
    }
    
    public Double getActualOvertimeFee() {
        return actualOvertimeFee;
    }
    
    public void setActualOvertimeFee(Double actualOvertimeFee) {
        this.actualOvertimeFee = actualOvertimeFee;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
