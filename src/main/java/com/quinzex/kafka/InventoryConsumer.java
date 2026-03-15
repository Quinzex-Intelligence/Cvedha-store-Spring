package com.quinzex.kafka;

import com.quinzex.dto.InventoryItemDto;
import com.quinzex.dto.InventoryReleaseEvent;
import com.quinzex.dto.InventoryReserveEvent;
import com.quinzex.entity.OrderItems;
import com.quinzex.entity.Orders;
import com.quinzex.repository.OrderRepo;
import com.quinzex.service.InventoryService;
import com.quinzex.service.InventoryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final InventoryServiceImpl inventoryService;
    private final InventoryProducer inventoryProducer;
    private final OrderRepo orderRepo;

    @KafkaListener(topics = "inventoryReserveTopic",groupId = "inventory-group")
    public void  handleReserve(InventoryReserveEvent inventoryReserveEvent){
     try{
         List<OrderItems> orderItems = convert(inventoryReserveEvent.getItems());
         inventoryService.reserveStock(orderItems);
         inventoryProducer.sendReservedEvent(inventoryReserveEvent);
     }catch (Exception e){
         log.error("Inventory reserve failed for order {}", inventoryReserveEvent.getOrderId(), e);
          inventoryProducer.sendFailedEvent(inventoryReserveEvent);
     }

    }
    @KafkaListener(topics = "inventoryReleaseTopic",groupId = "inventory-group")
 public void handleRelease(InventoryReleaseEvent event){
        List<OrderItems> orderItems = convert(event.getItems());
        inventoryService.releaseStock(orderItems);
        log.info("Releasing stock for order {}", event.getOrderId());
 }

 @KafkaListener(topics = "inventoryReservedTopic", groupId = "order-group")
 public void handleReserved(InventoryReserveEvent inventoryReserveEvent){
     Orders order = orderRepo.findById(inventoryReserveEvent.getOrderId()).orElseThrow();
     if(!"PENDING".equals(order.getStatus())) {
         return;
     }
     order.setStatus("INVENTORY_RESERVED");
     order.setPaymentExpiryTime(LocalDateTime.now().plusMinutes(15));
     orderRepo.save(order);
 }
    @KafkaListener(topics = "inventoryFailedTopic", groupId = "order-group")
public void handleFailed(InventoryReserveEvent inventoryReserveEvent){
        Orders order = orderRepo.findById(inventoryReserveEvent.getOrderId()).orElseThrow();
        if(!"PENDING".equals(order.getStatus())) {
            return;
        }
        order.setStatus("FAILED");
        orderRepo.save(order);
}
    private List<OrderItems> convert(List<InventoryItemDto> inventoryItemDto){
        return inventoryItemDto.stream().map(item->{
            OrderItems orderItems = new OrderItems();
            orderItems.setBookId(item.getBookId());
            orderItems.setQuantity(item.getQuantity());
            return orderItems;
        }).toList();
    }
}
