package com.quinzex.service;

import com.quinzex.entity.Ebooks;
import com.quinzex.entity.OrderItems;
import com.quinzex.repository.EbookRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final EbookRepo ebookRepo;
    @Transactional
    public void reserveStock(List<OrderItems> orderItems) {
        for (OrderItems item : orderItems) {
        int updated = ebookRepo.reserveStock(item.getBookId(), item.getQuantity());
         if(updated==0){
             throw  new IllegalArgumentException(  "Insufficient stock for book ");
         }
     }
    }

    @Transactional
    public void releaseStock(List<OrderItems> orderItems) {
        for (OrderItems item : orderItems) {
        ebookRepo.releaseStock(item.getBookId(), item.getQuantity());
     }
    }
}
