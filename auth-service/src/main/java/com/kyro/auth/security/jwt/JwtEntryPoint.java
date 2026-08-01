package com.kyro.auth.security.jwt;

import com.kyro.utils.ErrorResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {
  private final ErrorResponseUtils errorResponseUtils;

  public JwtEntryPoint(ErrorResponseUtils errorResponseUtils) {
    this.errorResponseUtils = errorResponseUtils;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    errorResponseUtils.sendAuthenticationError(
        response, "Thông tin xác thực không hợp lệ. Vui lòng đăng nhập lại.");
  }
}
