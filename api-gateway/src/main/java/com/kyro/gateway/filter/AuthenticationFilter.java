package com.kyro.gateway.filter;

import com.kyro.gateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter
    extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  private final JwtUtils jwtUtils;

  public AuthenticationFilter(JwtUtils jwtUtils) {
    super(Config.class);
    this.jwtUtils = jwtUtils;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      if (org.springframework.http.HttpMethod.OPTIONS.equals(request.getMethod())) {
        return chain.filter(exchange);
      }

      String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (authHeader == null) {
        return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
      }

      if (!authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Invalid Authorization Header format", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);

      if (!jwtUtils.validateToken(token)) {
        return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
      }

      Claims claims = jwtUtils.getClaims(token);
      String userId = String.valueOf(claims.get("id"));
      String email = claims.getSubject();
      List<?> roles = claims.get("roles", List.class);
      String rolesStr =
          roles != null ? String.join(",", roles.stream().map(Object::toString).toList()) : "";

      // Strip any untrusted user headers sent by client and inject verified headers from JWT
      ServerHttpRequest mutatedRequest =
          request
              .mutate()
              .headers(
                  h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Email");
                    h.remove("X-User-Roles");
                  })
              .header("X-User-Id", userId)
              .header("X-User-Email", email)
              .header("X-User-Roles", rolesStr)
              .build();

      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    };
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);
    return response.setComplete();
  }

  public static class Config {
    // Configuration fields if needed
  }
}
