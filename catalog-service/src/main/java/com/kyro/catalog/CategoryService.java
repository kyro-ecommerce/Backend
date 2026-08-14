package com.kyro.catalog;

import com.kyro.catalog.dto.CategoryDTO;
import com.kyro.catalog.dto.CategoryRequest;
import com.kyro.exceptions.AppException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  public CategoryDTO addCategory(CategoryRequest request) {
    String name = validatedName(request.name(), null);
    if (request.level() != null && request.level() == 2 && request.parentId() == null) {
      throw new IllegalArgumentException("Parent category is required for a child category");
    }
    if (request.level() != null && request.level() == 1 && request.parentId() != null) {
      throw new IllegalArgumentException("Top-level category cannot have a parent");
    }
    Category category = new Category();
    category.setName(name);
    if (request.parentId() != null) {
      Category parent = findCategoryById(request.parentId());
      if (parent.getParentCategory() != null) {
        throw new IllegalArgumentException("Parent category must be level 1");
      }
      category.setParentCategory(parent);
    }
    return toDTO(categoryRepository.save(category));
  }

  @Transactional
  public CategoryDTO updateCategory(Long id, String name) {
    Category category = findCategoryById(id);
    category.setName(validatedName(name, id));
    return toDTO(categoryRepository.save(category));
  }

  @Transactional
  public void deleteCategory(Long id) {
    Category category = findCategoryById(id);
    List<Category> tree =
        category.getParentCategory() == null
            ? java.util.stream.Stream.concat(
                    java.util.stream.Stream.of(category), category.getSubCategories().stream())
                .toList()
            : List.of(category);
    List<CategoryInUseException.BlockedCategory> blocked =
        tree.stream()
            .map(
                c ->
                    new CategoryInUseException.BlockedCategory(
                        c.getId(), c.getName(), categoryRepository.countProducts(c.getId())))
            .filter(c -> c.productCount() > 0)
            .toList();
    if (!blocked.isEmpty()) {
      throw new CategoryInUseException(blocked);
    }
    categoryRepository.delete(category);
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public Category findCategoryByName(String name) {
    return categoryRepository.findByName(name);
  }

  public Category findCategoryById(Long categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  public List<Category> getAllParentCategories() {
    return categoryRepository.findByParentCategoryIsNull();
  }

  @Transactional(readOnly = true)
  public List<CategoryDTO> getCategoryTree() {
    return categoryRepository.findByParentCategoryIsNull().stream()
        .map(
            parent -> {
              CategoryDTO dto = toDTO(parent);
              dto.setSubCategories(parent.getSubCategories().stream().map(this::toDTO).toList());
              return dto;
            })
        .toList();
  }

  public List<Category> getChildTopCategories(String topCategory) {
    // Create Pageable for first page (index 0) with size 5
    Pageable limit = PageRequest.of(0, 5);
    // Call modified repository method
    return categoryRepository.findByParentCategoryNameIgnoreCase(topCategory, limit);
  }

  private String validatedName(String value, Long id) {
    String name = value == null ? "" : value.trim();
    if (name.isEmpty() || name.length() > 50) {
      throw new IllegalArgumentException("Category name must be between 1 and 50 characters");
    }
    boolean duplicate =
        id == null
            ? categoryRepository.existsByNameIgnoreCase(name)
            : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    if (duplicate) {
      throw new AppException(
          HttpStatus.CONFLICT, "CATEGORY_ALREADY_EXISTS", "Category name already exists");
    }
    return name;
  }

  private CategoryDTO toDTO(Category category) {
    return new CategoryDTO(category, categoryRepository.countProducts(category.getId()));
  }
}
