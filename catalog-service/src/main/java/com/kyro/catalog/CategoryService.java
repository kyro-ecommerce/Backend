package com.kyro.catalog;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public Category addCategory(Category category) {
    return Optional.of(category)
        .filter(c -> !categoryRepository.existsByName(c.getName()))
        .map(categoryRepository::save)
        .orElseThrow(() -> new EntityExistsException(category.getName() + "already exists"));
  }

  public Category updateCategory(Category category) {
    // Ensure hierarchy level does not exceed 2
    if (category.getParentCategory() != null) {
      Category parentCategory = findCategoryById(category.getParentCategory().getId());
      if (parentCategory.getParentCategory() != null) {
        throw new EntityExistsException(parentCategory.getName() + "already exists");
      }
      category.setLevel(2);
      category.setParent(false);
    } else {
      category.setLevel(1);
      category.setParent(true);
    }

    return Optional.ofNullable(findCategoryById(category.getId()))
        .map(
            oldCategory -> {
              oldCategory.setName(category.getName());
              return categoryRepository.save(oldCategory);
            })
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
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
    return categoryRepository.findByLevel(1);
  }

  public List<Category> getChildTopCategories(String topCategory) {
    // Create Pageable for first page (index 0) with size 5
    Pageable limit = PageRequest.of(0, 5);
    // Call modified repository method
    return categoryRepository.findByParentCategoryNameIgnoreCase(topCategory, limit);
  }

  //    //    public List<Category> getAllCategories() {
  //        return categoryRepository.findAll();
  //    }

}
