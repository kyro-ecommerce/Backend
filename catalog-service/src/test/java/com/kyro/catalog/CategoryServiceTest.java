package com.kyro.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kyro.catalog.dto.CategoryDTO;
import com.kyro.catalog.dto.CategoryRequest;
import com.kyro.exceptions.AppException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
  @Mock CategoryRepository repository;
  private CategoryService service;

  @BeforeEach
  void setUp() {
    service = new CategoryService(repository);
    lenient()
        .when(repository.save(any(Category.class)))
        .thenAnswer(
            invocation -> {
              Category category = invocation.getArgument(0);
              if (category.getId() == null) category.setId(99L);
              return category;
            });
  }

  @Test
  void createsTrimmedLevelOneCategory() {
    CategoryDTO result = service.addCategory(new CategoryRequest("  Laptop  ", 1, null));

    assertEquals("Laptop", result.getName());
    assertEquals(1, result.getLevel());
    assertEquals(true, result.isParent());
  }

  @Test
  void createsLevelTwoOnlyUnderLevelOneParent() {
    Category parent = category(1L, "Laptop", 1);
    when(repository.findById(1L)).thenReturn(Optional.of(parent));

    CategoryDTO result = service.addCategory(new CategoryRequest("Gaming", 2, 1L));

    assertEquals(1L, result.getParentId());
    assertEquals(false, result.isParent());
    assertThrows(
        IllegalArgumentException.class,
        () -> service.addCategory(new CategoryRequest("Invalid", 2, null)));
  }

  @Test
  void rejectsCaseInsensitiveDuplicate() {
    when(repository.existsByNameIgnoreCase("laptop")).thenReturn(true);

    AppException error =
        assertThrows(
            AppException.class,
            () -> service.addCategory(new CategoryRequest(" laptop ", 1, null)));

    assertEquals("CATEGORY_ALREADY_EXISTS", error.getErrorCode());
    verify(repository, never()).save(any());
  }

  @Test
  void updatesNameAndReportsNotFound() {
    Category category = category(5L, "Old", 1);
    when(repository.findById(5L)).thenReturn(Optional.of(category));

    assertEquals("New", service.updateCategory(5L, " New ").getName());
    assertThrows(EntityNotFoundException.class, () -> service.updateCategory(6L, "Missing"));
  }

  @Test
  void deletesEmptyTree() {
    Category parent = category(1L, "Laptop", 1);
    Category child = category(2L, "Gaming", 2);
    parent.addSubCategory(child);
    when(repository.findById(1L)).thenReturn(Optional.of(parent));

    service.deleteCategory(1L);

    verify(repository).delete(parent);
  }

  @Test
  void blocksTreeDeletionWithExactProductCounts() {
    Category parent = category(1L, "Laptop", 1);
    Category child = category(2L, "Gaming", 2);
    parent.addSubCategory(child);
    when(repository.findById(1L)).thenReturn(Optional.of(parent));
    when(repository.countProducts(1L)).thenReturn(2L);
    when(repository.countProducts(2L)).thenReturn(3L);

    CategoryInUseException error =
        assertThrows(CategoryInUseException.class, () -> service.deleteCategory(1L));

    assertEquals(
        List.of(
            new CategoryInUseException.BlockedCategory(1L, "Laptop", 2L),
            new CategoryInUseException.BlockedCategory(2L, "Gaming", 3L)),
        error.getBlockedCategories());
    verify(repository, never()).delete(any());
  }

  private Category category(Long id, String name, int level) {
    Category category = new Category();
    category.setId(id);
    category.setName(name);
    if (level == 2) category.setParentCategory(new Category("Parent"));
    return category;
  }
}
