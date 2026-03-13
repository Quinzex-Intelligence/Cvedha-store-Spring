package com.quinzex.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkCreateEbookRequest {

    private List<CreateEbookRequest> books;
}