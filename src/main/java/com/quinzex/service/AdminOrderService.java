package com.quinzex.service;
import com.quinzex.dto.AdminOrderResponse;
import com.quinzex.dto.OrderItemsResponse;
import com.quinzex.dto.RevenueResponse;
import com.quinzex.entity.Ebooks;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.repository.EbookRepo;
import com.quinzex.repository.OrderItemRepo;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepo  orderRepo;
    private final EbookRepo    ebookRepo;
    private final S3Service s3Service;
    private final OrderItemRepo  orderItemRepo;

    public List<AdminOrderResponse> getAllOrders(String status, Long cursor){
        List<Orders> orders = orderRepo.findAllOrdersWithCursor(status, cursor);
        return orders.stream().map(order->AdminOrderResponse.builder().orderId(order.getId()).userEmail(order.getUserEmail()).status(order.getStatus()).totalAmount(order.getTotalAmount()).createdAt(order.getCreatedAt()).build()).toList();
    }

    public RevenueResponse getTotalRevenue(){
        Double revenue = orderRepo.getTotalRevenue();
        return RevenueResponse.builder()
                .totalRevenue(revenue)
                .build();
    }
    public List<OrderItemsResponse> getOrderItems(Long orderId){

        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        List<OrderItems> items = orderItemRepo.findByOrder_Id(orderId);

        return items.stream().map(item -> {

            Ebooks ebook = ebookRepo.findById(item.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            String coverUrl = null;
            if(ebook.getCoverPhoto() != null){
                coverUrl = s3Service.generatePresignedUrl(ebook.getCoverPhoto());
            }

            return OrderItemsResponse.builder()
                    .bookId(ebook.getBookId())
                    .bookName(ebook.getBookName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .coverPhotoUrl(coverUrl)
                    .build();

        }).toList();
    }
}
