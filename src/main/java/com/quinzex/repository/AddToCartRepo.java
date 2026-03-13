package com.quinzex.repository;

import com.quinzex.entity.CartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddToCartRepo extends JpaRepository<CartItem,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CartItem> findByUserEmailAndEbook_BookIdAndActiveTrue(String userEmail, Long bookId);


    long countByUserEmailAndActiveTrue(String userEmail);
    @Query("""
SELECT c FROM CartItem c
JOIN FETCH c.ebook
WHERE c.userEmail = :userEmail
AND c.active = true
AND c.ebook.active = true
""")
    List<CartItem> findActiveCartWithBook(String userEmail);
}
