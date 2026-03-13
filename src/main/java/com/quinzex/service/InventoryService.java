package com.quinzex.service;

import com.quinzex.entity.OrderItems;

import java.util.List;

public interface InventoryService {
    void reserveStock(List<OrderItems> orderItems);

    void releaseStock(List<OrderItems> orderItems);
}
