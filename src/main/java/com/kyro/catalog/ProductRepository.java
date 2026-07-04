package com.kyro.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Find products by title containing keyword (case insensitive)
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%'))")
    List<Product> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);

    // More efficient search that can combine keyword and category
    @Query("SELECT p FROM Product p " +
            "WHERE (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    List<Product> findByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId);

    // Find products by multiple category IDs
    List<Product> findByCategoryIdIn(List<Long> categoryIds);

    // Find products by top and second level category names
    @Query("SELECT p FROM Product p WHERE LOWER(p.category.name) = LOWER(:secondCategoryName) " +
            "AND LOWER(p.category.parentCategory.name) = LOWER(:topCategoryName) " +
            "AND p.category.level = 2")
    List<Product> findProductsByTopAndSecondCategoryNames(
            @Param("topCategoryName") String topCategoryName,
            @Param("secondCategoryName") String secondCategoryName);

    // Find top selling products
    @Query("SELECT p FROM Product p ORDER BY p.quantitySold DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    // Advanced search with multiple filters
    @Query("SELECT p FROM Product p " +
            "WHERE (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:color IS NULL OR LOWER(p.color) = LOWER(:color)) " +
            "AND (:minPrice IS NULL OR p.discountedPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.discountedPrice <= :maxPrice) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'price_low' THEN p.discountedPrice END ASC, " +
            "CASE WHEN :sort = 'price_high' THEN p.discountedPrice END DESC, " +
            "CASE WHEN :sort = 'discount' THEN p.discountPersent END DESC, " +
            "CASE WHEN :sort = 'newest' THEN p.createdAt END DESC")
    List<Product> findByFilters(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("color") String color,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("sort") String sort);

    // Get product by ID
    Product getProductById(Long productId);

    // Find products by category name
    List<Product> findByCategoryName(String categoryName);



    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    List<Product> findByCategory(Category category);



    @Query("SELECT p FROM Product p " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:topLevelCategory IS NULL OR :topLevelCategory = '' OR " +
            "     (p.category.level = 1 AND LOWER(p.category.name) = LOWER(:topLevelCategory)) OR " +
            "     (p.category.level = 2 AND LOWER(p.category.parentCategory.name) = LOWER(:topLevelCategory))) " +
            "AND (:secondLevelCategory IS NULL OR :secondLevelCategory = '' OR " +
            "     (p.category.level = 2 AND LOWER(p.category.name) = LOWER(:secondLevelCategory))) " +
            "AND (:color IS NULL OR :color = '' OR LOWER(p.color) = LOWER(:color)) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:inStock IS NULL OR " +
            "     (:inStock = true AND EXISTS (SELECT 1 FROM ProductSize ps WHERE ps.product = p AND ps.quantity > 0)) OR " +
            "     (:inStock = false AND NOT EXISTS (SELECT 1 FROM ProductSize ps WHERE ps.product = p AND ps.quantity > 0)))")
    Page<Product> getProductsWithFilter(
            @Param("keyword") String keyword,
            @Param("topLevelCategory") String topLevelCategory,
            @Param("secondLevelCategory") String secondLevelCategory,
            @Param("color") String color,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable
    );

    // Get distinct top-level categories for all products (admin)
    @Query("SELECT DISTINCT CASE " +
            "WHEN p.category.level = 1 THEN p.category.name " +
            "WHEN p.category.level = 2 THEN p.category.parentCategory.name " +
            "END " +
            "FROM Product p WHERE p.category IS NOT NULL " +
            "ORDER BY CASE " +
            "WHEN p.category.level = 1 THEN p.category.name " +
            "WHEN p.category.level = 2 THEN p.category.parentCategory.name " +
            "END")
    List<String> findDistinctTopLevelCategories();

    // Get distinct second-level categories by top-level category (admin)
    @Query("SELECT DISTINCT p.category.name " +
            "FROM Product p WHERE p.category.level = 2 " +
            "AND LOWER(p.category.parentCategory.name) = LOWER(:topLevelCategory) " +
            "ORDER BY p.category.name")
    List<String> findDistinctSecondLevelCategoriesByTopLevel(
            @Param("topLevelCategory") String topLevelCategory);
}