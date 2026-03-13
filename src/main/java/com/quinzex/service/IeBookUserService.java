package com.quinzex.service;

import com.quinzex.dto.EbookUserResponse;


import java.util.List;

public interface IeBookUserService {

    List<EbookUserResponse> getActiveEbooksList(Long cursor);
    List<EbookUserResponse> searchBooks(String keyword, Long cursor);
    List<EbookUserResponse> getBooksByLanguage(String language, Long cursor);
    List<EbookUserResponse> getBooksByCategory(String category, Long cursor);
}
