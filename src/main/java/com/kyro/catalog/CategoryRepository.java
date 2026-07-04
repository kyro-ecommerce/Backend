package com.kyro.catalog;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
    List<Category> findByParentCategory(@Param("parentId") Long parentId);

    // Tìm kiếm danh mục con có tên và cha là danh mục cha đã cho
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.parentCategory.name = :parentCategoryName")
    Category findByNameAndParent(@Param("name") String name, @Param("parentCategory") String parentCategoryName);

    Optional<Category> findByNameAndParentCategory(String name, Category parentCategory);

    List<Category> findByLevel(int level);
    List<Category> findByParentCategoryId(Long parentId);

    // Tìm kiếm danh mục con có tên là danh mục cha đã cho
    List<Category> findByParentCategoryNameIgnoreCase(String parentCategoryName, Pageable pageable);
}