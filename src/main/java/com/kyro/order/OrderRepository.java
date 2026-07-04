package com.kyro.order;

import com.kyro.enums.OrderStatus;




import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByOrderDateBetweenAndOrderStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);
    List<Order> findByOrderDateGreaterThanEqualAndOrderStatus(LocalDateTime startDate, OrderStatus status);
    List<Order> findByOrderStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress")
    List<Order> findAllWithUser();

    List<Order> findOrderByOrderStatus(OrderStatus status);

    List<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress ORDER BY o.orderDate DESC")
    List<Order> findAllWithUserOrderByOrderDateDesc();



    @Query("SELECT o FROM Order o WHERE " +
            "(:search IS NULL OR " +
            "LOWER(CONCAT(o.user.firstName, ' ', o.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(o.id AS string) LIKE CONCAT('%', :search, '%')) " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findAdminOrdersWithFilters(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
            "AND (:status IS NULL OR o.orderStatus = :status)")
    Long countOrdersByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalDiscountedPrice) FROM Order o WHERE " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
            "AND o.orderStatus = 'DELIVERED'")
    Double sumRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
