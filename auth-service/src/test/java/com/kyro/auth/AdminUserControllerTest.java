package com.kyro.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class AdminUserControllerTest {
  @Test
  void adminUserSortIsWhitelistedAndStable() {
    var pageable = AdminUserController.userPageable(0, 10, List.of("name,desc"));
    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("firstName").getDirection());
    assertNotNull(pageable.getSort().getOrderFor("lastName"));
    assertNotNull(pageable.getSort().getOrderFor("id"));
    assertThrows(
        IllegalArgumentException.class,
        () -> AdminUserController.userPageable(0, 10, List.of("password,asc")));
  }

  @Test
  void numericAndDateSortsDoNotApplyLowercase() {
    assertFalse(AdminUserController.userPageable(0, 10, List.of("id,asc"))
        .getSort().getOrderFor("id").isIgnoreCase());
    assertFalse(AdminUserController.userPageable(0, 10, List.of("createdAt,desc"))
        .getSort().getOrderFor("createdAt").isIgnoreCase());
  }
}
