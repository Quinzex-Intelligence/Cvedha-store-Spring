package com.quinzex.service;

import com.quinzex.dto.OrderHistoryResponse;
import com.quinzex.dto.OrderItemsResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.repository.EbookRepo;
import com.quinzex.repository.OrderItemRepo;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final EbookRepo ebookRepo;
    private final S3Service s3Service;

    public List<OrderHistoryResponse> getUserOrders(String status, Long cursor) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (status == null) {
            status = "PAID";
        }
        List<Orders> orders = orderRepo.findUserOrdersWithCursor(email, status, cursor);
        return orders.stream().map(order -> OrderHistoryResponse.builder().orderId(order.getId())
                        .status(order.getStatus()).totalAmount(order.getTotalAmount()).createdAt(order.getCreatedAt()).build())
                .toList();
    }

    public List<OrderItemsResponse> getOrderItems(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Orders order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not Found"));
        if (!order.getUserEmail().equals(email)) {
            throw new RuntimeException("Unauthorized order access");
        }
        List<OrderItems> items = orderItemRepo.findByOrder_Id(orderId);
        List<Long> bookIds = items.stream().map(OrderItems::getBookId).distinct().toList();
        Map<Long,Ebooks> ebbokMap= ebookRepo.findAllById(bookIds).stream().collect(Collectors.toMap(Ebooks::getBookId, e->e));
        return items.stream().map(item -> {

            Ebooks ebook = ebbokMap.get(item.getBookId());
            String coverUrl = null;
            if (ebook.getCoverPhoto() != null) {
                coverUrl = s3Service.generatePresignedUrl(ebook.getCoverPhoto());
            }

            String ebookUrl = null;
            if ("PAID".equals(order.getStatus()) && "EBOOK".equalsIgnoreCase(ebook.getBookCategory())
                    && ebook.getEbookPdfKey() != null) {
                ebookUrl = s3Service.generatePresignedUrl(ebook.getEbookPdfKey());
            }

            return OrderItemsResponse.builder()
                    .bookId(ebook.getBookId())
                    .bookName(ebook.getBookName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .coverPhotoUrl(coverUrl)
                    .bookCategory(ebook.getBookCategory())
                    .ebookPdfUrl(ebookUrl)
                    .build();

        }).toList();
    }

}
