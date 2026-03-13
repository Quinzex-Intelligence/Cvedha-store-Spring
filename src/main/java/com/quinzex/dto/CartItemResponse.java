package com.quinzex.dto;

import lombok.Data;

@Data
public class CartItemResponse {

    private Long cartItemId;

    private Long bookId;

    private String bookName;

    private String coverPhotoUrl;

    private Double price;

    private Integer quantity;

    private String languageCategory;

    private Double subTotal;
}