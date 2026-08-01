package com.kyro.auth;

import com.kyro.enums.UserRole;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;

@Entity
public class Role {

  public Role() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private UserRole name;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
  private Collection<User> users = new HashSet<>();

  public Role(UserRole name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UserRole getName() {
    return name;
  }

  public void setName(UserRole name) {
    this.name = name;
  }

  public Collection<User> getUsers() {
    return users;
  }

  public void setUsers(Collection<User> users) {
    this.users = users;
  }
}
