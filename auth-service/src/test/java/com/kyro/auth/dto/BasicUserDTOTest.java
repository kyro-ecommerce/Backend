package com.kyro.auth.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class BasicUserDTOTest {

  @Test
  void exposesBannedStatusToAdminFrontend() throws Exception {
    BasicUserDTO user = new BasicUserDTO();
    user.setBanned(true);

    String json = new ObjectMapper().writeValueAsString(user);

    assertTrue(json.contains("\"banned\":true"));
  }
}
