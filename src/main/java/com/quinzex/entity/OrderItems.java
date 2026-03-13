package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="order_items")
@Data
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    private Orders order;

    private Long bookId;

    private Integer quantity;

    private Double price;
}
