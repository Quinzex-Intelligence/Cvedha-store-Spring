package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderHistoryResponse {

    private Long orderId;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;

}