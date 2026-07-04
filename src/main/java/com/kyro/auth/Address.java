package com.kyro.auth;

import com.kyro.order.Order;



import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 50, message = "Full name must be less than 50 characters")
    @Column(name = "full_name")
    private String fullName;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must be less than 100 characters")
    @Column(name = "province")
    private String province;

    @NotBlank(message = "district is required")
    @Size(max = 50, message = "district must be less than 50 characters")
    @Column(name = "district")
    private String district;

    @NotBlank(message = "ward is required")
    @Size(max = 50, message = "ward must be less than 50 characters")
    @Column(name = "ward")
    private String ward;

    @NotBlank(message = "street is required")
    @Size(max = 50, message = "street must be less than 50 characters")
    @Column(name = "street")
    private String street;


    @Size(max = 100, message = "note must be less than 100 characters")
    @Column(name = "note")
    private String note;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank(message = "Mobile number is required")
    @Size(max = 15, message = "Mobile number must be less than 15 characters")
    @Column(name = "phoneNumber")
    private String phoneNumber;

    @OneToMany(mappedBy = "shippingAddress", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders;

}
