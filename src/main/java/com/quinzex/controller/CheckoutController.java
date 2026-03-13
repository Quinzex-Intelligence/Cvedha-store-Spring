package com.quinzex.controller;

import com.quinzex.service.IcheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CheckoutController {

    private final IcheckoutService checkoutService;

    @PostMapping("/checkout")
    public ResponseEntity<Long> checkout() {
        Long orderId = checkoutService.checkout();
        return ResponseEntity.ok(orderId);
    }
}