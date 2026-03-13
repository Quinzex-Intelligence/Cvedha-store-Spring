package com.quinzex.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class CreateEbookRequest {

    private String bookName;

    private String bookAuthor;

    private String bookDescription;

    private String languageCategory;

    private Double bookPrice;

    private String bookCategory;

    private Integer totalQuantity;

    private LocalDate bookPublishDate;

    private MultipartFile coverPhoto;
    private MultipartFile ebookPdf;
}