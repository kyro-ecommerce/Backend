package com.kyro.gateway.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

class GlobalErrorHandlerTest {
  @Test
  void writesProblemDetailForGatewayErrors() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/missing").build());

    new GlobalErrorHandler(new ObjectMapper())
        .handle(exchange, new ResponseStatusException(HttpStatus.NOT_FOUND))
        .block();

    assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    assertEquals(
        MediaType.APPLICATION_PROBLEM_JSON, exchange.getResponse().getHeaders().getContentType());
    String body =
        Flux.from(exchange.getResponse().getBody())
            .map(GlobalErrorHandlerTest::read)
            .collectList()
            .map(parts -> String.join("", parts))
            .block();
    assertTrue(body.contains("RESOURCE_NOT_FOUND"));
    assertTrue(body.contains("\"status\":404"));
    assertTrue(body.contains("\"message\""));
  }

  private static String read(DataBuffer buffer) {
    byte[] bytes = new byte[buffer.readableByteCount()];
    buffer.read(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
