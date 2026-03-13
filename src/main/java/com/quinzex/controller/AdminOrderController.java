package com.quinzex.controller;

import com.quinzex.dto.AdminOrderResponse;
import com.quinzex.dto.OrderItemsResponse;
import com.quinzex.dto.RevenueResponse;
import com.quinzex.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // Get all user orders with cursor pagination
    @GetMapping("get/all/orders/admin")
  //  @PreAuthorize("hasAuthority('ADMIN_ORDERS')")
    public List<AdminOrderResponse> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long cursor
    ) {
        return adminOrderService.getAllOrders(status, cursor);
    }

    // View items inside a specific order
    @GetMapping("/{orderId}/items")
 //   @PreAuthorize("hasAuthority('ADMIN_ORDERS')")
    public List<OrderItemsResponse> getOrderItems(@PathVariable Long orderId) {
        return adminOrderService.getOrderItems(orderId);
    }

    // Get total revenue from all paid orders
    @GetMapping("/revenue")
  //  @PreAuthorize("hasAuthority('ADMIN_ORDERS')")
    public RevenueResponse getTotalRevenue() {
        return adminOrderService.getTotalRevenue();
    }
}