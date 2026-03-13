package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table(name = "orders")
@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private Double subTotal;


    private Double totalAmount;

    private String status;

    private LocalDateTime paymentExpiryTime;

    private String razorpayOrderId;

    private LocalDateTime createdAt;

    private String razorpayPaymentId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItems> orderItems;
}
