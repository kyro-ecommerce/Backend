package com.kyro.auth;

import com.kyro.catalog.Review;
import com.kyro.order.Cart;
import com.kyro.order.Order;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private boolean active = true;

        @Size(max = 50, message = "First name must be less than 50 characters")
        @Column(name = "first_name")
        private String firstName;

        @Size(max = 50, message = "Last name must be less than 50 characters")
        @Column(name = "last_name")
        private String lastName;

        @NaturalId
        @Email(message = "Please provide a valid email address")
        @Size(max = 100, message = "Email must be less than 100 characters")
        @Column(unique = true, nullable = false)
        private String email;

        @Size(min = 8, message = "Password must be at least 8 characters long")
        private String password;

        @Size(max = 15, message = "Phone number must be less than 15 characters")
        private String phone;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Address> address = new ArrayList<>();

        @Column(name = "created_at")
        private LocalDateTime createdAt;;

        @PrePersist
        protected void onCreate() {
                this.createdAt = LocalDateTime.now();
        }

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference
        private Cart cart;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Order> orders;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
        @JsonIgnore
        private List<Review> reviews = new ArrayList<>();

        @ManyToOne
        @JoinColumn(name = "role_id")
        private Role role;

        @Column(name = "is_banned", nullable = false)
        private boolean banned = false;

        // Các trường bổ sung cho OAuth2 -> lưu detail info từ oauth provider
        private String oauthProvider;
        private String oauthProviderId;
        private String imageUrl;

        @Column(name = "website")
        private String website;

        @Column(name = "business_type")
        private String businessType;

        @Column(name = "shop_description")
        private String shopDescription;

        @Column(name = "shop_name")
        private String shopName;

        public User(String firstName, String lastName, String email, String password, Role role) {
                this.firstName = firstName;
                this.lastName = lastName;
                this.email = email;
                this.password = password;
                this.role = role;
                this.active = false;
                this.createdAt = LocalDateTime.now();
        }
        public User(String firstName, String lastName, String email, String password, Role role, String phone) {
                this.firstName = firstName;
                this.lastName = lastName;
                this.email = email;
                this.password = password;
                this.role = role;
                this.phone = phone;
                this.active = false;
                this.createdAt = LocalDateTime.now();
        }
}

