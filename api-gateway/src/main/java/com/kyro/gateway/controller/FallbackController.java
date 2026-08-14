package com.kyro.gateway.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

  @RequestMapping("/ai")
  public Mono<ResponseEntity<ProblemDetail>> aiFallback() {
    String message = "Dịch vụ AI đang bận hoặc tạm thời gián đoạn. Vui lòng thử lại sau!";
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, message);
    problem.setType(URI.create("https://api.kyro.com/errors/dependency-unavailable"));
    problem.setTitle("Dependency Unavailable");
    problem.setProperty("code", "DEPENDENCY_UNAVAILABLE");
    problem.setProperty("message", message);
    problem.setProperty("fallback", true);
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem));
  }
}
