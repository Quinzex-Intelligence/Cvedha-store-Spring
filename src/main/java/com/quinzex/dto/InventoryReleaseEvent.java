package com.quinzex.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryReleaseEvent {

    private Long orderId;
    private List<InventoryItemDto> items;

}
