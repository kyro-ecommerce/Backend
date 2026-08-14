package com.kyro.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Locale;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
  private final ObjectMapper objectMapper;

  public GlobalErrorHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
    if (exchange.getResponse().isCommitted()) {
      return Mono.error(exception);
    }

    HttpStatusCode status =
        exception instanceof ErrorResponse error
            ? error.getStatusCode()
            : HttpStatus.INTERNAL_SERVER_ERROR;
    String code = codeFor(status);
    String message = messageFor(status);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
    problem.setType(
        URI.create(
            "https://api.kyro.com/errors/" + code.toLowerCase(Locale.ENGLISH).replace('_', '-')));
    problem.setTitle(titleFor(status));
    problem.setProperty("code", code);
    problem.setProperty("message", message);

    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    try {
      DataBuffer buffer =
          exchange.getResponse().bufferFactory().wrap(objectMapper.writeValueAsBytes(problem));
      return exchange.getResponse().writeWith(Mono.just(buffer));
    } catch (JsonProcessingException serializationFailure) {
      return Mono.error(serializationFailure);
    }
  }

  private static String codeFor(HttpStatusCode status) {
    return switch (status.value()) {
      case 400 -> "INVALID_ARGUMENT";
      case 401 -> "UNAUTHORIZED";
      case 403 -> "FORBIDDEN";
      case 404 -> "RESOURCE_NOT_FOUND";
      case 405 -> "METHOD_NOT_ALLOWED";
      case 406 -> "NOT_ACCEPTABLE";
      case 413 -> "PAYLOAD_TOO_LARGE";
      case 415 -> "UNSUPPORTED_MEDIA_TYPE";
      case 429 -> "RATE_LIMIT_EXCEEDED";
      case 502 -> "DEPENDENCY_ERROR";
      case 503 -> "DEPENDENCY_UNAVAILABLE";
      default -> "INTERNAL_ERROR";
    };
  }

  private static String messageFor(HttpStatusCode status) {
    return status.is5xxServerError()
        ? "The service is temporarily unavailable"
        : "The request could not be processed";
  }

  private static String titleFor(HttpStatusCode status) {
    HttpStatus resolved = HttpStatus.resolve(status.value());
    return resolved == null ? "Request Failed" : resolved.getReasonPhrase();
  }
}
