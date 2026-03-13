package com.quinzex.service;

import com.quinzex.dto.MyBookResponse;

import java.util.List;

public interface IMyBooksService {
    public List<MyBookResponse> getMyBooks();
}
