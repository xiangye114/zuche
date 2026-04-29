package com.zuche.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderLogDTO {
    private Long id;
    private Long orderId;
    private Long operatorId;
    private String operatorName;
    private Integer fromStatus;
    private String fromStatusText;
    private Integer toStatus;
    private String toStatusText;
    private String remark;
    private LocalDateTime createTime;
}
