package com.quinzex.repository;

import com.quinzex.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<Orders, Long> {
    List<Orders> findByUserEmail(String userEmail);
    Optional<Orders> findByRazorpayOrderId(String razorpayOrderId);

    List<Orders> findByStatusAndPaymentExpiryTimeBefore(
            String status,
            LocalDateTime time
    );



    @Query(value = """
SELECT * FROM orders
WHERE user_email = :email
AND (:status IS NULL OR status = :status)
AND (:cursor IS NULL OR id < :cursor)
ORDER BY id DESC
LIMIT 10
""", nativeQuery = true)
    List<Orders> findUserOrdersWithCursor(
            @Param("email") String email,
            @Param("status") String status,
            @Param("cursor") Long cursor
    );

    @Query(value = """
SELECT * FROM orders
WHERE (:status IS NULL OR status = :status)
AND (:cursor IS NULL OR id < :cursor)
ORDER BY id DESC
LIMIT 10
""", nativeQuery = true)
    List<Orders> findAllOrdersWithCursor(
            @Param("status") String status,
            @Param("cursor") Long cursor
    );

    @Query(value = """
SELECT COALESCE(SUM(total_amount),0)
FROM orders
WHERE status = 'PAID'
""", nativeQuery = true)
    Double getTotalRevenue();

    @Query("""
       SELECT o
       FROM Orders o
       JOIN FETCH o.orderItems
       WHERE o.userEmail = :email
       AND o.status = :status
       """)
    List<Orders> findByUserEmailAndStatus(@Param("email") String email,
                                          @Param("status") String status);


    @Query("""
SELECT COUNT(oi) > 0
FROM Orders o
JOIN o.orderItems oi
WHERE o.userEmail = :email
AND o.status = :status
AND oi.bookId = :bookId
""")
    boolean existsByUserEmailAndStatusAndBookId(String email, String status, Long bookId);


}
