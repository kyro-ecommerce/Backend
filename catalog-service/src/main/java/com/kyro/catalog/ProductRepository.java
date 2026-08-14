package com.kyro.catalog;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.id = :id")
  Optional<Product> findByIdWithLock(@Param("id") Long id);

  // Find products by title containing keyword (case insensitive)
  @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%'))")
  List<Product> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);

  // More efficient search that can combine keyword and category
  @Query(
      "SELECT p FROM Product p "
          + "WHERE (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%'))) "
          + "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
  List<Product> findByKeywordAndCategory(
      @Param("keyword") String keyword, @Param("categoryId") Long categoryId);

  // Find products by multiple category IDs
  List<Product> findByCategoryIdIn(List<Long> categoryIds);

  // Get product by ID
  Product getProductById(Long productId);

  // Find products by category name
  List<Product> findByCategoryName(String categoryName);

  @Query(
      "SELECT p FROM Product p WHERE "
          + "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Product> searchProducts(@Param("keyword") String keyword);

  List<Product> findByCategory(Category category);

  // Get distinct top-level categories for all products (admin)
  @Query(
      "SELECT DISTINCT CASE "
          + "WHEN p.category.parentCategory IS NULL THEN p.category.name "
          + "ELSE p.category.parentCategory.name "
          + "END "
          + "FROM Product p WHERE p.category IS NOT NULL "
          + "ORDER BY CASE "
          + "WHEN p.category.parentCategory IS NULL THEN p.category.name "
          + "ELSE p.category.parentCategory.name "
          + "END")
  List<String> findDistinctTopLevelCategories();

  // Get distinct second-level categories by top-level category (admin)
  @Query(
      "SELECT DISTINCT p.category.name "
          + "FROM Product p WHERE p.category.parentCategory IS NOT NULL "
          + "AND LOWER(p.category.parentCategory.name) = LOWER(:topLevelCategory) "
          + "ORDER BY p.category.name")
  List<String> findDistinctSecondLevelCategoriesByTopLevel(
      @Param("topLevelCategory") String topLevelCategory);
}
