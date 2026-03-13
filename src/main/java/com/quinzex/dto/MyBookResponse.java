package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyBookResponse {

    private Long bookId;

    private String bookName;

    private String author;

    private String coverPhotoUrl;

    private String pdfUrl;
}