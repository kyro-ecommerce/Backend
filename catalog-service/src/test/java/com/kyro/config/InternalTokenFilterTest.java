package com.kyro.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class InternalTokenFilterTest {
  @Test
  void rejectsInternalRequestWithoutValidToken() throws Exception {
    InternalTokenFilter filter = new InternalTokenFilter();
    ReflectionTestUtils.setField(filter, "expectedToken", "secret");
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/api/v1/internal/products/1");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (request1, response1) -> {};

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
  }

  @Test
  void forwardsInternalRequestWithValidToken() throws Exception {
    InternalTokenFilter filter = new InternalTokenFilter();
    ReflectionTestUtils.setField(filter, "expectedToken", "secret");
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/api/v1/internal/products/1");
    request.addHeader("X-Internal-Token", "secret");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean forwarded = new AtomicBoolean();
    FilterChain chain = (request1, response1) -> forwarded.set(true);

    filter.doFilter(request, response, chain);

    assertEquals(true, forwarded.get());
  }
}
