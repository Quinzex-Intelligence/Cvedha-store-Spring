package com.quinzex.controller;

import com.quinzex.dto.OrderHistoryResponse;
import com.quinzex.dto.OrderItemsResponse;
import com.quinzex.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    // Get user order history with cursor pagination
    @GetMapping("my/orders")
    public List<OrderHistoryResponse> getUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long cursor
    ) {
        return orderHistoryService.getUserOrders(status, cursor);
    }

    // Get items of a specific order
    @GetMapping("/{orderId}/items")
    public List<OrderItemsResponse> getOrderItems(@PathVariable Long orderId) {
        return orderHistoryService.getOrderItems(orderId);
    }
}