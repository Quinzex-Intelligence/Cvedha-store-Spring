package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Table
@Entity
@Data
public class Ebooks {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long bookId;

    private String bookName;

    private String bookAuthor;

    @Column(columnDefinition = "TEXT")
    private String bookDescription;

    private Double bookPrice;

    private Boolean active =true;

   private String coverPhoto;

    private Integer totalQuantity;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    private String languageCategory;

    private String bookCategory;

    private String ebookPdfKey;


    private LocalDate bookPublishDate; //active or inactive book

    @Version
    private Long version;

}
