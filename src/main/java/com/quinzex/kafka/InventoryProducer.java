package com.quinzex.kafka;

import com.quinzex.dto.InventoryReserveEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryProducer {

    private final KafkaTemplate<String, InventoryReserveEvent> kafkaTemplate;

    //checkout-> inventory
    public void sendReserveEvent(InventoryReserveEvent inventoryReserveEvent) {
        kafkaTemplate.send("inventoryReserveTopic",inventoryReserveEvent.getOrderId().toString(), inventoryReserveEvent);
    }
    //paymentFail / timeout -> release stock
    public void sendReleaseEvent(InventoryReserveEvent inventoryReserveEvent) {
        kafkaTemplate.send("inventoryReleaseTopic", inventoryReserveEvent.getOrderId().toString(),inventoryReserveEvent);
    }

    //inventory success -> order service response
    public void sendReservedEvent(InventoryReserveEvent inventoryReserveEvent) {
        kafkaTemplate.send("inventoryReservedTopic", inventoryReserveEvent.getOrderId().toString(), inventoryReserveEvent);
    }
    //inventory fail -> order service response
    public void sendFailedEvent(InventoryReserveEvent inventoryReserveEvent) {
        kafkaTemplate.send("inventoryFailedTopic", inventoryReserveEvent.getOrderId().toString(), inventoryReserveEvent);
    }
}
