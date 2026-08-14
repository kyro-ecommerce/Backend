package com.kyro.cart.dto;

public class CartItemDTO {
  private Long id,productId,variantId; private String productName,sku,variantName,productImageUrl;
  private int quantity,discountPercent; private long price,salePrice; private boolean available=true,priceChanged; private String unavailableReason;
  public Long getId(){return id;} public void setId(Long v){id=v;} public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
  public Long getVariantId(){return variantId;} public void setVariantId(Long v){variantId=v;} public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
  public String getSku(){return sku;} public void setSku(String v){sku=v;} public String getVariantName(){return variantName;} public void setVariantName(String v){variantName=v;}
  public String getProductImageUrl(){return productImageUrl;} public void setProductImageUrl(String v){productImageUrl=v;} public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
  public long getPrice(){return price;} public void setPrice(long v){price=v;} public long getSalePrice(){return salePrice;} public void setSalePrice(long v){salePrice=v;}
  public int getDiscountPercent(){return discountPercent;} public void setDiscountPercent(int v){discountPercent=v;} public boolean isAvailable(){return available;} public void setAvailable(boolean v){available=v;}
  public String getUnavailableReason(){return unavailableReason;} public void setUnavailableReason(String v){unavailableReason=v;} public boolean isPriceChanged(){return priceChanged;} public void setPriceChanged(boolean v){priceChanged=v;}
}
