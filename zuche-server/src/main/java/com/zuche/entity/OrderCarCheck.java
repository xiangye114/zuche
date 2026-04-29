package com.zuche.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;

@TableName("order_car_checks")
public class OrderCarCheck {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Integer checkType; // 1-取车检查，2-还车检查
    private String checkItem;
    private Integer checkResult; // 0-异常，1-正常
    private String description;
    private String photos;
    private String checkedBy;
    private LocalDateTime checkedTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Integer getCheckType() {
        return checkType;
    }
    
    public void setCheckType(Integer checkType) {
        this.checkType = checkType;
    }
    
    public String getCheckItem() {
        return checkItem;
    }
    
    public void setCheckItem(String checkItem) {
        this.checkItem = checkItem;
    }
    
    public Integer getCheckResult() {
        return checkResult;
    }
    
    public void setCheckResult(Integer checkResult) {
        this.checkResult = checkResult;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPhotos() {
        return photos;
    }
    
    public void setPhotos(String photos) {
        this.photos = photos;
    }
    
    public String getCheckedBy() {
        return checkedBy;
    }
    
    public void setCheckedBy(String checkedBy) {
        this.checkedBy = checkedBy;
    }
    
    public LocalDateTime getCheckedTime() {
        return checkedTime;
    }
    
    public void setCheckedTime(LocalDateTime checkedTime) {
        this.checkedTime = checkedTime;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
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
}
