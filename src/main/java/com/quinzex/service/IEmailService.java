package com.quinzex.service;

import com.quinzex.entity.OrderItems;

import java.util.List;

public interface IEmailService {
  void sendOrderSuccessEmail(String userEmail,
                                   Long orderId,
                                   Double amount,
                                   List<OrderItems> items);
}
