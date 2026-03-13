package com.quinzex.service;

import com.quinzex.dto.DownloadResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.repository.EbookRepo;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;


@Service
@RequiredArgsConstructor
public class DownloadService implements IdowloadServie {
    private final OrderRepo  orderRepo;
    private final S3Service s3Service;
    private final EbookRepo ebookRepo;

    @Override
    @Transactional(readOnly = true)
    public DownloadResponse downloadBook(Long bookId) throws IOException {
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
     boolean purchased = orderRepo.existsByUserEmailAndStatusAndBookId(userEmail,"PAID",bookId);
     if(!purchased){
         throw new RuntimeException("You have not purchased this book");
     }
        Ebooks ebook = ebookRepo.findById(bookId).orElseThrow(()->new RuntimeException("Book not found"));
     if(!"EBOOK".equalsIgnoreCase(ebook.getBookCategory())){
         throw new RuntimeException("This book is not downloadable");
     }
        if (ebook.getEbookPdfKey() == null) {
            throw new RuntimeException("PDF not available for this book");
        }
     InputStream stream =  s3Service.downloadFile(ebook.getEbookPdfKey());
     return  DownloadResponse.builder().stream(stream).fileName(ebook.getBookName()).build();

    }
}
