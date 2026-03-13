package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemsResponse {

    private Long bookId;
    private String bookName;
    private Double price;
    private Integer quantity;
    private String coverPhotoUrl;
    private String bookCategory;
    private String ebookPdfUrl;

}