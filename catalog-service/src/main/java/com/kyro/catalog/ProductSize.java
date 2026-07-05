package com.kyro.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sizes")
public class ProductSize {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Size name is required")
  @Column(name = "name")
  private String name;

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be greater than or equal to 0")
  @Column(name = "quantity")
  private Integer quantity;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnore
  private Product product;
}
