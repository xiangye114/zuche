package com.zuche.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CancelReasonDTO {
    private Long id;
    private String reasonCode;
    private String reasonName;
    private Integer reasonType; // 1-用户取消，2-系统取消，3-管理员取消
    private String reasonTypeText;
    private Integer sortOrder;
    private Integer isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
