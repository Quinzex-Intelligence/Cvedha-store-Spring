package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EbookUserResponse {

    private Long bookId;

    private String bookName;

    private String bookAuthor;

    private String bookDescription;

    private Double bookPrice;

    private String coverPhotoUrl;

    private LocalDate bookPublishDate;

    private String languageCategory;

    private String bookCategory;

    private Boolean inStock;
}