package com.zuche.dto;

import com.zuche.entity.Car;

public class CarWithOrderDTO {
    private Car car;
    private String orderNo;
    private Integer orderStatus;
    private String orderStatusText;
    
    public CarWithOrderDTO(Car car) {
        this.car = car;
    }
    
    public Car getCar() {
        return car;
    }
    
    public void setCar(Car car) {
        this.car = car;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Integer getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
        this.orderStatusText = getOrderStatusText(orderStatus);
    }
    
    public String getOrderStatusText() {
        return orderStatusText;
    }
    
    private String getOrderStatusText(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已取车";
            case 3: return "已还车";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}