package com.quinzex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class InventoryReserveEvent {

    private Long orderId;
    private List<InventoryItemDto> items;


}