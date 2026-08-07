package com.kyro.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/ai")
    public Mono<ResponseEntity<Map<String, Object>>> aiFallback() {
        Map<String, Object> response = Map.of(
                "status", 503,
                "message", "Dịch vụ AI đang bận hoặc tạm thời gián đoạn. Vui lòng thử lại sau!",
                "fallback", true
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
