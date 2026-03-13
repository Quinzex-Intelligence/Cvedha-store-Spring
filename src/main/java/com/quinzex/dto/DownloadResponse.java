package com.quinzex.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Data
@Builder
public class DownloadResponse {
    private final InputStream stream;
    private final String fileName;
}
