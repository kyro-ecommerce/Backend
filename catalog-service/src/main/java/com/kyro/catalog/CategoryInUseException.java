package com.kyro.catalog;

import com.kyro.exceptions.AppException;
import java.util.List;
import org.springframework.http.HttpStatus;

public class CategoryInUseException extends AppException {
  private final List<BlockedCategory> blockedCategories;

  public CategoryInUseException(List<BlockedCategory> blockedCategories) {
    super(
        HttpStatus.CONFLICT,
        "CATEGORY_IN_USE",
        "Category cannot be deleted because it contains products");
    this.blockedCategories = blockedCategories;
  }

  public List<BlockedCategory> getBlockedCategories() {
    return blockedCategories;
  }

  public record BlockedCategory(Long categoryId, String name, long productCount) {}
}
