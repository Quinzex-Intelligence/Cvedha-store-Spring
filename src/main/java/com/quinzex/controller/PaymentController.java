package com.quinzex.controller;

import com.quinzex.dto.MyBookResponse;
import com.quinzex.dto.PaymentVerificationRequest;
import com.quinzex.service.IMyBooksService;
import com.quinzex.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;
    private final IMyBooksService myBooksService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<String> createPaymentOrder(@PathVariable Long orderId) throws Exception {
        String razorpayOrderId = paymentService.createRazorPayOrder(orderId);
        return ResponseEntity.ok(razorpayOrderId);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        paymentService.verifyPayment(request);
        return ResponseEntity.ok("Payment successful");
    }

    @PostMapping("/retry/{orderId}")
    public ResponseEntity<Long> retryPayment(@PathVariable Long orderId) {
        Long newOrderId = paymentService.retryPayment(orderId);
        return ResponseEntity.ok(newOrderId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }
    @GetMapping("/my-books")
    public List<MyBookResponse> getMyBooks() {
        return myBooksService.getMyBooks();
    }
}//end