package com.zuche.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;

@TableName("cancel_reasons")
public class CancelReason {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reasonCode;
    private String reasonName;
    private Integer reasonType; // 1-用户取消，2-系统取消，3-管理员取消
    private Integer sortOrder;
    private Integer isActive; // 0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getReasonCode() {
        return reasonCode;
    }
    
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
    
    public String getReasonName() {
        return reasonName;
    }
    
    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }
    
    public Integer getReasonType() {
        return reasonType;
    }
    
    public void setReasonType(Integer reasonType) {
        this.reasonType = reasonType;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Integer getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
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
