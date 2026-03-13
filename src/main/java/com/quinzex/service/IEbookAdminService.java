package com.quinzex.service;

import com.quinzex.dto.CreateEbookRequest;
import com.quinzex.entity.Ebooks;

import java.io.IOException;
import java.util.List;

public interface IEbookAdminService {
    public List<Ebooks> getInactiveEbooksList(Long cursor);

    public String createEbooks(List<CreateEbookRequest> ebooksRequest) throws IOException;

    public String softDeleteEbooks(List<Long> ids);

    public String activateEbooks(List<Long> ids);



    String editBook(Long id, CreateEbookRequest editEbookRequest) throws IOException;
}
