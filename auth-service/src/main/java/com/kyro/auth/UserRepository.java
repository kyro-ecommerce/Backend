package com.kyro.auth;

import com.kyro.enums.UserRole;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);

  User findByEmail(String email);

  List<User> findByRoleNameNot(UserRole roleName, Pageable pageable);

  List<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleNameNot(
      String email, String firstName, String lastName, UserRole roleName, Pageable pageable);

  // Filter users by role
  List<User> findByRoleName(UserRole roleName, Pageable pageable);

  // Search and filter combined
  List<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleName(
      String email, String firstName, String lastName, UserRole roleName, Pageable pageable);

  @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName")
  long countByRoleName(@Param("roleName") UserRole roleName);
}
