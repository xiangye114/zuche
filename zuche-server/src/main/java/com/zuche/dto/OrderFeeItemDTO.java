package com.zuche.dto;

import lombok.Data;

@Data
public class OrderFeeItemDTO {
    private String itemName;
    private Double amount;
    private String description;
    private Integer type; // 1-正向费用，2-负向费用（折扣）
}
