package com.quinzex.service;

import com.quinzex.dto.InventoryItemDto;
import com.quinzex.dto.InventoryReserveEvent;
import com.quinzex.entity.Orders;
import com.quinzex.kafka.InventoryProducer;
import com.quinzex.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderExpiryScheduler {
    private final OrderRepo  orderRepo;
    private final InventoryProducer inventoryProducer;

    @Scheduled(fixedRate = 60000) // every 60 seconds
    @Transactional
    public void expireOrdersAndReleaseInventory() {
       List<Orders> orders = orderRepo.findByStatusAndPaymentExpiryTimeBefore(  "INVENTORY_RESERVED", LocalDateTime.now());
       if (orders.isEmpty()) {
           return;
       }
       orders.forEach(order ->{

           order.setStatus("EXPIRED");
           orderRepo.save(order);
           InventoryReserveEvent inventoryReserveEvent = new InventoryReserveEvent();
           inventoryReserveEvent.setOrderId(order.getId());
           inventoryReserveEvent.setItems(order.getOrderItems().stream().map(i-> new InventoryItemDto(i.getBookId(),i.getQuantity())).toList());
           inventoryProducer.sendReleaseEvent(inventoryReserveEvent);
       });

    }
}
