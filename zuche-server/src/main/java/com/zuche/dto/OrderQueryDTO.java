package com.zuche.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String orderNo;
    private String userName;
    private String userPhone;
    private Integer status;
    private Long storeId;
    private Long carId;
    private Long categoryId;
    private Long brandId;
    private LocalDateTime startTimeBegin;
    private LocalDateTime startTimeEnd;
    private LocalDateTime endTimeBegin;
    private LocalDateTime endTimeEnd;
    private LocalDateTime createTimeBegin;
    private LocalDateTime createTimeEnd;
    private String timeRange; // today, week, month, custom
    private String sortField;
    private String sortOrder;
}
