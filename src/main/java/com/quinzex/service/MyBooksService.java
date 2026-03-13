package com.quinzex.service;

import com.quinzex.dto.MyBookResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.repository.EbookRepo;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyBooksService implements IMyBooksService {

    private final OrderRepo orderRepo;
    private final EbookRepo ebookRepo;
    private final S3Service s3Service;

    @Override
    @Transactional(readOnly = true)
    public List<MyBookResponse> getMyBooks() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Orders> orders = orderRepo.findByUserEmailAndStatus(userEmail, "PAID");

        List<Long> bookIds = orders.stream().flatMap(order->order.getOrderItems().stream().map(OrderItems::getBookId)).distinct().collect(Collectors.toList());

        List<Ebooks> ebooks = ebookRepo.findAllById(bookIds);
        Map<Long, Ebooks> ebookMap =  ebooks.stream().collect(Collectors.toMap(Ebooks::getBookId, e->e,(existing,replacement)->existing));
        List<MyBookResponse> response = new ArrayList<>();
        bookIds.forEach(bookId -> {
            Ebooks ebook = ebookMap.get(bookId);
            String pdfUrl = null;
            if ("EBOOK".equalsIgnoreCase(ebook.getBookCategory()) && ebook.getEbookPdfKey() != null) {
                pdfUrl = s3Service.generatePresignedUrl(ebook.getEbookPdfKey());
            }

            String coverUrl = null;
            if (ebook.getCoverPhoto() != null) {
                coverUrl = s3Service.generatePresignedUrl(ebook.getCoverPhoto());
            }
            response.add(
                    MyBookResponse.builder()
                            .bookId(ebook.getBookId())
                            .bookName(ebook.getBookName())
                            .author(ebook.getBookAuthor())
                            .coverPhotoUrl(coverUrl)
                            .pdfUrl(pdfUrl)
                            .build()
            );
        });
        return response;
    }
}
