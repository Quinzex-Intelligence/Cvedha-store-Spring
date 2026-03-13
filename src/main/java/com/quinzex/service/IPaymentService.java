package com.quinzex.service;

import com.quinzex.dto.PaymentVerificationRequest;

public interface IPaymentService {
    String createRazorPayOrder(Long orderId) throws Exception;

    void verifyPayment(PaymentVerificationRequest request);

    void processWebhook(String payload, String signature);

    Long retryPayment(Long oldOrderId);
}
