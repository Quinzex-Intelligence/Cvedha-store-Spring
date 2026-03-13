package com.quinzex.service;

import com.quinzex.dto.DownloadResponse;

import java.io.IOException;
import java.io.InputStream;

public interface IdowloadServie {
    DownloadResponse downloadBook(Long bookId) throws IOException;
}
