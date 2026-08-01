package com.kyro.catalog.dto;

import com.kyro.catalog.Product;
import java.util.List;

public class ProductInternalResponse {
  private Long id;
  private String title;
  private int price;
  private int discountPersent;
  private int discountedPrice;
  private String color;
  private List<ImageResponse> images;
  private List<SizeResponse> sizes;

  public ProductInternalResponse() {}

  public ProductInternalResponse(Product product) {
    this.id = product.getId();
    this.title = product.getTitle();
    this.price = product.getPrice();
    this.discountPersent = product.getDiscountPersent();
    this.discountedPrice = product.getDiscountedPrice();
    this.color = product.getColor();
    if (product.getImages() != null) {
      this.images =
          product.getImages().stream()
              .map(
                  img -> {
                    ImageResponse r = new ImageResponse();
                    r.setId(img.getId());
                    r.setDownloadUrl(img.getDownloadUrl());
                    return r;
                  })
              .toList();
    }
    if (product.getSizes() != null) {
      this.sizes =
          product.getSizes().stream()
              .map(
                  sz -> {
                    SizeResponse r = new SizeResponse();
                    r.setName(sz.getName());
                    r.setQuantity(sz.getQuantity());
                    return r;
                  })
              .toList();
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getDiscountPersent() {
    return discountPersent;
  }

  public void setDiscountPersent(int discountPersent) {
    this.discountPersent = discountPersent;
  }

  public int getDiscountedPrice() {
    return discountedPrice;
  }

  public void setDiscountedPrice(int discountedPrice) {
    this.discountedPrice = discountedPrice;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public List<ImageResponse> getImages() {
    return images;
  }

  public void setImages(List<ImageResponse> images) {
    this.images = images;
  }

  public List<SizeResponse> getSizes() {
    return sizes;
  }

  public void setSizes(List<SizeResponse> sizes) {
    this.sizes = sizes;
  }

  public static class ImageResponse {
    private Long id;
    private String downloadUrl;

    public ImageResponse() {}

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getDownloadUrl() {
      return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
    }
  }

  public static class SizeResponse {
    private String name;
    private Integer quantity;

    public SizeResponse() {}

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getQuantity() {
      return quantity;
    }

    public void setQuantity(Integer quantity) {
      this.quantity = quantity;
    }
  }
}
