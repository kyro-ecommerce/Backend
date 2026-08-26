package com.kyro.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "product_attribute")
public class ProductAttribute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnore
  private Product product;

  @NotBlank
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank
  @Column(nullable = false, length = 255)
  private String value;

  @Column(length = 30)
  private String unit;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    id = value;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product value) {
    product = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String value) {
    unit = value;
  }
}
