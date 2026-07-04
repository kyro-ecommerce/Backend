package com.kyro.auth;

import com.kyro.enums.UserRole;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserRole name;

    @OneToMany(mappedBy = "role",  cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<User> users = new HashSet<>();

    public Role(UserRole name) {
        this.name = name;
    }
}
