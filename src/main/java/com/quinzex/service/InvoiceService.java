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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));
        if (!isAdmin && (orders.getUserEmail() == null || !orders.getUserEmail().equals(email))) {
            throw new AccessDeniedException("Unauthorized invoice access");
        }

        List<OrderItems> items = orders.getOrderItems();
        List<Long> bookIds=items.stream().map(OrderItems::getBookId).toList();
        Map<Long,Ebooks> ebooksMap = ebookRepo.findAllById(bookIds).stream().collect(Collectors.toMap(Ebooks::getBookId, ebook->ebook));
        List<OrderItemsResponse> itemsResponse = items.stream().map(item->{
            Ebooks ebook = ebooksMap.get(item.getBookId());

            if (ebook == null) {
                throw new IllegalArgumentException("Book not found for id " + item.getBookId());
            }
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
        double gstAmount = subTotal*gstPercentage/100;
        double total = gstAmount + subTotal;
        return InvoiceResponse.builder().orderId(orders.getId()).userEmail(orders.getUserEmail()).createdAt(orders.getCreatedAt()).subTotal(subTotal).gstPercentage(gstPercentage).gstAmount(gstAmount).totalAmount(total).items(itemsResponse).build();
    }

}//end

