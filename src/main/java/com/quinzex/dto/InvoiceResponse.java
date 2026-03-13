package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponse {

    private Long orderId;
    private String userEmail;
    private LocalDateTime createdAt;

    private Double subTotal;
    private Double gstPercentage;
    private Double gstAmount;
    private Double totalAmount;

    private List<OrderItemsResponse> items;
}