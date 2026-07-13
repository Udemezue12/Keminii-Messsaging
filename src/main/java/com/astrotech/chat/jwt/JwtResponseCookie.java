package com.astrotech.chat.jwt;


import com.astrotech.chat.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class JwtResponseCookie {

    private final JwtConfig jwtConfig;

    public void setCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken) {

        ResponseCookie accessCookie = ResponseCookie.from(
                        "access_token",
                        accessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtConfig.getAccessExpiration())
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refresh_token",
                        refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtConfig.getRefreshExpiration())
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString());
    }

    public void clearCookies(
            HttpServletResponse response) {

        var accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        var refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        var csrfCookie = ResponseCookie.from("XSRF-TOKEN", "")
                .path("/")
                .httpOnly(false)
                .maxAge(0)
                .secure(true)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", csrfCookie.toString());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString());
    }

    public String extractJakartaCookie(
            HttpServletRequest request,
            String name) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
    public String extractSpringCookie(HttpServletRequest request, String name) {
        var cookie = WebUtils.getCookie(request, name);
        return (cookie != null) ? cookie.getValue() : null;
    }
}
