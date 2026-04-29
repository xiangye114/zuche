package com.zuche.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderCarCheckDTO {
    private Long id;
    private Long orderId;
    private Integer checkType; // 1-取车检查，2-还车检查
    private String checkTypeText;
    private String checkItem;
    private Integer checkResult; // 0-异常，1-正常
    private String checkResultText;
    private String description;
    private String photos;
    private String checkedBy;
    private LocalDateTime checkedTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
