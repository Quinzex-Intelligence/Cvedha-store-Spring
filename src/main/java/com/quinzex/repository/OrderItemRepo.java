package com.quinzex.repository;

import com.quinzex.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepo extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrder_Id(Long orderId);
}
