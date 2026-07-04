package com.kyro.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 Cookie là một phần nhỏ dữ liệu được web server gửi đến trình duyệt của người dùng,
 sau đó được lưu trữ trên máy của người dùng. Mỗi khi người dùng truy cập lại trang
 web, trình duyệt sẽ gửi cookie đó kèm theo request.


 */
@Component
public class CookieUtils {
    private static final Logger log = LoggerFactory.getLogger(CookieUtils.class);

    @Value("${app.useSecureCookie}")
    private boolean useSecureCookie;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    /**
     * Tạo và thêm cookie chứa refresh token vào HTTP response
     * Cookie được set với các thuộc tính bảo mật: HttpOnly, Secure, SameSite
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAge) {
        if (response == null) {
            throw new IllegalArgumentException("HttpServletResponse cannot be null");
        }
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
//        Ngăn JavaScript truy cập.
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (maxAge / 1000));
        refreshTokenCookie.setSecure(useSecureCookie);
        String sameSite = useSecureCookie ? "None" : "Lax";
        setResponseHeader(response, refreshTokenCookie, sameSite);
    }

    /**
     * Thiết lập header Set-Cookie với các thuộc tính bảo mật
     */
    private void setResponseHeader(HttpServletResponse response, Cookie refreshTokenCookie, String sameSite) {
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(refreshTokenCookie.getName()).append("=")
                .append(refreshTokenCookie.getValue())
                .append("; HttpOnly; Path=").append(refreshTokenCookie.getPath())
                .append("; Max-Age=").append(refreshTokenCookie.getMaxAge())
                .append(useSecureCookie ? "; Secure" : "")
                .append("; SameSite=").append(sameSite);
        response.setHeader("Set-Cookie", cookieHeader.toString());
    }

    /**
     * Lấy refresh token từ cookie
     */
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Names of the cookie found: " + cookie.getName());
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Xóa refresh token cookie
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Xóa cookie
        response.addCookie(cookie);
    }

    /**
     * In ra log tất cả cookies (dùng để debug)
     */
    public void logCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info("Cookies from request:");
            for (Cookie cookie : cookies) {
                log.info("Cookie name: {}, value: {}", cookie.getName(), cookie.getValue());
            }
        } else {
            log.info("No cookies found in request");
        }
    }
}



/*
Quản lý refresh token cho xác thực JWT, cho phép làm mới token mà không cần đăng nhập lại.
Tập trung bảo mật với HttpOnly, Secure, và SameSite.
Hữu ích trong hệ thống xác thực không trạng thái.
 */