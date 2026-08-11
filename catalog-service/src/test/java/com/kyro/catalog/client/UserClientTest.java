package com.kyro.catalog.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UserClientTest {

  @Test
  void ignoresUserFieldsCatalogDoesNotUse() throws Exception {
    UserClient.UserResponse user =
        new ObjectMapper()
            .readValue(
                """
                {"id":16,"email":"user@example.com","firstName":"Ky","lastName":"Ro","mobile":"0900000000"}
                """,
                UserClient.UserResponse.class);

    assertEquals("Ky", user.firstName());
  }
}
