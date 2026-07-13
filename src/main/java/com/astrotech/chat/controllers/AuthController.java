package com.astrotech.chat.controllers;

import com.astrotech.chat.dto.request.*;
import com.astrotech.chat.dto.response.AccessTokenResponse;
import com.astrotech.chat.dto.response.RefreshTokenResponse;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "For user registration, login, logout, email verification, forgot password and reset password")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRequest userRequest) {
        return authService.register(userRequest);
    }

    @PostMapping("/login")
    @Ratelimit
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(HttpServletResponse response, @Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(response, request, servletRequest);

    }

    @PostMapping("/logout")
    @Ratelimit
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        return authService.logout(request, response);

    }

    @PostMapping("/refresh")
    @Ratelimit
    public RefreshTokenResponse refresh(@CookieValue(value = "refresh_token") String refreshToken, HttpServletResponse response) {
        return authService.refresh(refreshToken, response);
    }

    @PostMapping("/verify-email")
    @Ratelimit(times = 4, seconds = 8)
    public Object verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {

        return authService.verifyEmail(request);
    }

    @PostMapping("/forgot-password")
    @Ratelimit(times = 4, seconds = 8)
    public Object forgotPassword(@Valid @RequestBody ResendVerificationRequest request) {

        return authService.forgotPassword(request.email());
    }


    @PostMapping("/resend-email-verification-link")
    @Ratelimit(times = 4, seconds = 8)
    public Object resendVerification(@Valid @RequestBody ResendVerificationRequest request) {

        var result = authService.resendVerificationEmail(request.email());

        if (result instanceof Map && "Email already verified.".equals(((Map<?, ?>) result).get("message"))) {
            return ResponseEntity.badRequest().body(result);
        }

        return result;
    }

    @PostMapping("/resend-password-verification-link")
    @Ratelimit(times = 4, seconds = 8)
    public Object resendPasswordVerification(@Valid @RequestBody ResendVerificationRequest request) {

        return authService.resendPasswordResetLink(request.email());
    }

    @PostMapping("/reset-password")
    @Ratelimit
    public Object resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        return authService.resetPassword(request);

    }


}
