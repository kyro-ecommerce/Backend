package com.kyro.auth;

import com.kyro.enums.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
  Optional<Role> findByName(UserRole role);
}
