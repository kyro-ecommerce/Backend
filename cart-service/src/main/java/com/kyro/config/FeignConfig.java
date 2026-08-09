package com.kyro.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
  @Bean
  public RequestInterceptor internalApiTokenInterceptor(
      @Value("${internal.api.token:}") String token) {
    return template -> template.header("X-Internal-Token", token);
  }
}
