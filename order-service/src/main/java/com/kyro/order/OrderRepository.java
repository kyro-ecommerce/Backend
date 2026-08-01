package com.kyro.order;

import com.kyro.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository interface for managing Order database operations in order-service. */
public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByUserId(Long userId);

  List<Order> findByOrderDateBetweenAndOrderStatus(
      LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);

  List<Order> findByOrderDateGreaterThanEqualAndOrderStatus(
      LocalDateTime startDate, OrderStatus status);

  List<Order> findByOrderStatus(OrderStatus status);

  List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

  @Modifying
  @Query("DELETE FROM Order o WHERE o.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.shippingAddress")
  List<Order> findAllWithUser();

  List<Order> findOrderByOrderStatus(OrderStatus status);

  List<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.shippingAddress ORDER BY o.orderDate DESC")
  List<Order> findAllWithUserOrderByOrderDateDesc();

  List<Order> findAllByOrderByOrderDateDesc();

  @Query(
      "SELECT o FROM Order o WHERE "
          + "(:search IS NULL OR :search = '' OR "
          + "LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR "
          + "CAST(o.id AS string) LIKE CONCAT('%', :search, '%')) "
          + "AND (:status IS NULL OR o.orderStatus = :status) "
          + "AND (CAST(:startDate AS timestamp) IS NULL OR o.orderDate >= :startDate) "
          + "AND (CAST(:endDate AS timestamp) IS NULL OR o.orderDate <= :endDate)")
  Page<Order> findAdminOrdersWithFilters(
      @Param("search") String search,
      @Param("status") OrderStatus status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE "
          + "(CAST(:startDate AS timestamp) IS NULL OR o.orderDate >= :startDate) "
          + "AND (CAST(:endDate AS timestamp) IS NULL OR o.orderDate <= :endDate) "
          + "AND (:status IS NULL OR o.orderStatus = :status)")
  Long countOrdersByStatusAndDateRange(
      @Param("status") OrderStatus status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT SUM(o.totalDiscountedPrice) FROM Order o WHERE "
          + "(CAST(:startDate AS timestamp) IS NULL OR o.orderDate >= :startDate) "
          + "AND (CAST(:endDate AS timestamp) IS NULL OR o.orderDate <= :endDate) "
          + "AND o.orderStatus = 'DELIVERED'")
  Double sumRevenueByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT CAST(FUNCTION('DATE', o.orderDate) AS string), SUM(o.totalDiscountedPrice) FROM Order o WHERE "
          + "(CAST(:startDate AS timestamp) IS NULL OR o.orderDate >= :startDate) "
          + "AND (CAST(:endDate AS timestamp) IS NULL OR o.orderDate <= :endDate) "
          + "GROUP BY FUNCTION('DATE', o.orderDate) ORDER BY FUNCTION('DATE', o.orderDate) ASC")
  List<Object[]> findDailyRevenueByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
