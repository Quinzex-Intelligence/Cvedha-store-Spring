package com.quinzex.service;

import com.quinzex.dto.InvoiceResponse;
import com.quinzex.dto.OrderItemsResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.entity.Gst;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.repository.EbookRepo;
import com.quinzex.repository.GstRepo;
import com.quinzex.repository.OrderItemRepo;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final EbookRepo ebookRepo;
    private final GstRepo gstRepo;
    private final S3Service s3Service;
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long orderId){

        Orders orders = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!orders.getUserEmail().equals(email)) {
            throw new RuntimeException("Unauthorized invoice access");
        }
        List<OrderItems> items = orders.getOrderItems();
        List<OrderItemsResponse> itemsResponse = items.stream().map(item->{
            Ebooks ebook = ebookRepo.findById(item.getBookId()).orElseThrow(() -> new IllegalArgumentException("Book not found"));
            String coverUrl = null;
            if (ebook.getCoverPhoto() != null) {
                coverUrl = s3Service.generatePresignedUrl(ebook.getCoverPhoto());
            }
            return OrderItemsResponse.builder().bookId(ebook.getBookId()).bookName(ebook.getBookName()).price(item.getPrice()).quantity(item.getQuantity()).coverPhotoUrl(coverUrl).build();
        }).toList();
        double subTotal = orders.getSubTotal();
        double gstPercentage = gstRepo.findTopByOrderByIdDesc()
                .map(g -> (double) g.getGstPercentage())
                .orElse(0.0);
        double gstAmount = (subTotal*gstPercentage)/100;
        double total = gstAmount + subTotal;
        return InvoiceResponse.builder().orderId(orders.getId()).userEmail(orders.getUserEmail()).createdAt(orders.getCreatedAt()).subTotal(subTotal).gstPercentage(gstPercentage).gstAmount(gstAmount).totalAmount(total).items(itemsResponse).build();
    }

}
