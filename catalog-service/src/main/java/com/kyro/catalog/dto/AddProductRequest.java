package com.kyro.catalog.dto;

import com.kyro.catalog.Category;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AddProductRequest {
  private String name;
  private String brand;
  private BigDecimal price;
  private int inventory;
  private String description;
  private Category category;
}
