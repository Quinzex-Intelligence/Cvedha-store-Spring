package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table(name = "cart_items")
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Ebooks ebook;

    private Integer quantity;

    private Boolean active = true;

    private LocalDateTime addedAt = LocalDateTime.now();
}
