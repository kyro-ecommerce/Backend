package com.kyro.catalog.dto;

import com.kyro.catalog.*;
import java.util.List;

public record ProductInternalResponse(
    Long id, String title, int discountPercent, List<ImageResponse> images,
    List<VariantResponse> variants) {
  public ProductInternalResponse(Product p) {
    this(p.getId(), p.getTitle(), p.getDiscountPercent(),
        p.getImages().stream().map(i -> new ImageResponse(i.getId(), i.getDownloadUrl())).toList(),
        p.getVariants().stream().map(v -> new VariantResponse(v.getId(), v.getSku(), v.getVariantName(),
            v.getPrice(), Pricing.salePrice(v.getPrice(), p.getDiscountPercent()), v.getStock(), v.isActive())).toList());
  }
  public record ImageResponse(Long id, String downloadUrl) {}
  public record VariantResponse(Long id, String sku, String variantName, long price, long salePrice, int stock, boolean active) {}
}
