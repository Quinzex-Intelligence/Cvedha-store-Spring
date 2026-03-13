package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrderResponse {

    private Long orderId;

    private String userEmail;

    private String status;

    private Double totalAmount;

    private LocalDateTime createdAt;

}