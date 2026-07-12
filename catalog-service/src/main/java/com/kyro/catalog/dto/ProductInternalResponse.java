package com.kyro.catalog.dto;

import com.kyro.catalog.Product;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductInternalResponse {
  private Long id;
  private String title;
  private int price;
  private int discountPersent;
  private int discountedPrice;
  private String color;
  private List<ImageResponse> images;
  private List<SizeResponse> sizes;

  public ProductInternalResponse(Product product) {
    this.id = product.getId();
    this.title = product.getTitle();
    this.price = product.getPrice();
    this.discountPersent = product.getDiscountPersent();
    this.discountedPrice = product.getDiscountedPrice();
    this.color = product.getColor();
    if (product.getImages() != null) {
      this.images = product.getImages().stream().map(img -> {
        ImageResponse r = new ImageResponse();
        r.setId(img.getId());
        r.setDownloadUrl(img.getDownloadUrl());
        return r;
      }).toList();
    }
    if (product.getSizes() != null) {
      this.sizes = product.getSizes().stream().map(sz -> {
        SizeResponse r = new SizeResponse();
        r.setName(sz.getName());
        r.setQuantity(sz.getQuantity());
        return r;
      }).toList();
    }
  }

  @Data
  public static class ImageResponse {
    private Long id;
    private String downloadUrl;
  }

  @Data
  public static class SizeResponse {
    private String name;
    private Integer quantity;
  }
}
