package com.kyro.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a product review. Stores user details as denormalized fields to prevent
 * inter-service calls.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review")
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "rating")
  @Max(value = 5, message = "Rating must be at most 5")
  @NotNull(message = "Rating is required")
  private Integer rating;

  @Size(max = 500, message = "Content must be less than 500 characters")
  @Column(name = "review_content")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnore
  private Product product;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_first_name")
  private String userFirstName;

  @Column(name = "user_last_name")
  private String userLastName;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
