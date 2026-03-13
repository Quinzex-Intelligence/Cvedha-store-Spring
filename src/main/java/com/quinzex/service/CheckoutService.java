package com.quinzex.service;

import com.quinzex.dto.InventoryItemDto;
import com.quinzex.dto.InventoryReserveEvent;
import com.quinzex.entity.CartItem;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.kafka.InventoryProducer;
import com.quinzex.repository.AddToCartRepo;
import com.quinzex.repository.GstRepo;
import com.quinzex.repository.OrderItemRepo;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService implements IcheckoutService {

    private final AddToCartRepo  addToCartRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final InventoryProducer  inventoryProducer;
    private final GstRepo  gstRepo;

    @Override
    @Transactional
    public Long checkout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String userEmail =  authentication.getName();

      List<CartItem> cartItems = addToCartRepo.findActiveCartWithBook(userEmail);
      if(cartItems.isEmpty()){
          throw  new RuntimeException("cart is Empty");
      }
          Orders   order = new Orders();
          order.setUserEmail(userEmail);
          order.setStatus("PENDING");
          order.setCreatedAt(LocalDateTime.now());
        double subTotal = cartItems.stream().mapToDouble(item-> item.getEbook().getBookPrice()* item.getQuantity()).sum();
        double gstPercentage = gstRepo.findTopByOrderByIdDesc()
                .map(g -> (double) g.getGstPercentage())
                .orElse(0.0);
        double gstAmount = (subTotal*gstPercentage)/100;
        double totalAmount = subTotal + gstAmount;
        order.setSubTotal(subTotal);
        if (totalAmount <= 0) {
            throw new RuntimeException("Invalid order amount");
        }
        order.setTotalAmount(totalAmount);
          orderRepo.save(order);

          List<OrderItems> orderItems = cartItems.stream().map(cartItem -> {
              OrderItems orderItem = new OrderItems();
              orderItem.setOrder(order);
              orderItem.setBookId(cartItem.getEbook().getBookId());
              orderItem.setQuantity(cartItem.getQuantity());
              orderItem.setPrice(cartItem.getEbook().getBookPrice());

              return orderItem;
          }).toList();
          orderItemRepo.saveAll(orderItems);


          InventoryReserveEvent event = new InventoryReserveEvent();
          event.setOrderId(order.getId());
          event.setItems(
                  orderItems.stream()
                          .map(item -> new InventoryItemDto(item.getBookId(), item.getQuantity()))
                          .toList()
          );
          inventoryProducer.sendReserveEvent(event);
          return order.getId();
    }
}
