package com.kyro.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Category findByName(String name);

  boolean existsByName(String name);

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

  Optional<Category> findByNameIgnoreCase(String name);

  @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
  long countProducts(@Param("categoryId") Long categoryId);

  @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
  List<Category> findByParentCategory(@Param("parentId") Long parentId);

  // Find subcategory by name and parent category
  @Query(
      "SELECT c FROM Category c WHERE c.name = :name AND c.parentCategory.name ="
          + " :parentCategoryName")
  Category findByNameAndParent(
      @Param("name") String name, @Param("parentCategory") String parentCategoryName);

  Optional<Category> findByNameAndParentCategory(String name, Category parentCategory);

  @EntityGraph(attributePaths = {"subCategories"})
  List<Category> findByParentCategoryIsNull();

  List<Category> findByParentCategoryId(Long parentId);

  // Find category by name
  List<Category> findByParentCategoryNameIgnoreCase(String parentCategoryName, Pageable pageable);
}
