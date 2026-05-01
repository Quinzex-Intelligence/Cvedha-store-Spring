package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EbookAdminResponse {

    private Long bookId;
    private String bookName;
    private String bookAuthor;
    private String bookDescription;
    private Double bookPrice;
    private LocalDate bookPublishDate;

    private String coverPhotoUrl;
    private String pdfUrl;

    private String bookCategory;
    private String languageCategory;

    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;

    private Boolean active;
}