package com.astrotech.chat.service;


import com.astrotech.chat.entites.UserSession;
import com.astrotech.chat.core.OTPRatelimit;
import com.astrotech.chat.core.TrimWhiteSpace;
import com.astrotech.chat.dto.request.LoginRequest;
import com.astrotech.chat.dto.request.ResetPasswordRequest;
import com.astrotech.chat.dto.request.UserRequest;
import com.astrotech.chat.dto.request.VerifyEmailRequest;
import com.astrotech.chat.dto.response.AccessTokenResponse;
import com.astrotech.chat.dto.response.RefreshTokenResponse;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.jwt.Jwt;
import com.astrotech.chat.jwt.JwtProvider;
import com.astrotech.chat.jwt.JwtResponseCookie;
import com.astrotech.chat.mappers.UserMapper;
import com.astrotech.chat.repositories.UserRepository;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import com.astrotech.chat.tasks.SendVerifyAndPasswordResetEmail;
import com.astrotech.chat.validators.request.RequestValidators;
import com.astrotech.chat.verification.UserVerification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

import static com.astrotech.chat.core.AppGenerators.generateUniqueSessionId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RequestValidators requestValidator;
    private final BlacklistedTokenService blacklistedTokenService;
    private final Jwt jwtService;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtResponseCookie responseCookie;
    private final UserVerification verificationService;
    private final JobScheduler jobScheduler;
    private final SendVerifyAndPasswordResetEmail sendVerifyAndPasswordResetEmail;
    private final AuthenticationManager authenticationManager;
    private final OTPRatelimit otpRatelimit;
    private final JwtProvider jwtProvider;
    private final UserSessionService userSessionService;


    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> register(UserRequest request) {

        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.email(), false);
        var nickName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.nickName(), false);


        requestValidator.throwIfTrue(
                userRepository.findProjectionByEmail(email).isPresent(),
                "Email already exists");
        requestValidator.throwIfTrue(userRepository.findProjectionByNickName(nickName).isPresent(), "Nickname already exists");
        requestValidator.throwIfTrue(userRepository.findProjectionByPhoneNumber(request.phoneNumber()).isPresent(), "PhoneNumber already exists");
        var user = UserMapper.create(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var savedUser = userRepository.save(user);
        var userDto = UserMapper.response(savedUser);
        if (userDto.email() != null) {
            resendVerificationEmail(savedUser.getEmail());
        }
        return ApiResponseBuilder.success("Registered successfully! Please click the link in your email or enter the OTP sent to your phone and email to verify your account.", null);

    }

    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(HttpServletResponse response, LoginRequest request, HttpServletRequest servletRequest) {
        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.email(), false);

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );
        var sessionId = generateUniqueSessionId();


        SecurityContextHolder.getContext().setAuthentication(authentication);

        var user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Invalid credentials"));
        var accessToken = jwtProvider.generateAccessToken(user.getId(), user.getDisplayName(), sessionId, user.getRole(), user.isVerified());
        var refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getDisplayName(), sessionId, user.getRole(), user.isVerified());
        responseCookie.setCookies(response, accessToken, refreshToken);

        user.setLastLoginAt(Instant.now());
        var savedUser = userRepository.save(user);
        var userAgent = servletRequest.getHeader("User-Agent");
        var ipAddress = getClientIp(servletRequest);


        UserSession session = null;
        if (request.deviceInfo() != null && ipAddress != null && userAgent != null) {
            session = userSessionService.createUserSession(userAgent, request.deviceInfo(), savedUser.getId(), ipAddress, sessionId, accessToken, refreshToken);
        }
        var gottenSessionId = (session != null) ? session.getId() : null;

        return ApiResponseBuilder.success(
                "Logged in Successfully",
                new AccessTokenResponse(new RefreshTokenResponse(accessToken, refreshToken),
                        user.getId(),
                        user.getDisplayName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.isVerified(), gottenSessionId)
        );
    }

    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        var sessionKeyHeader = request.getHeader("Session-Key");
        var accessToken = responseCookie.extractJakartaCookie(request, "access_token");
        var refreshToken = responseCookie.extractJakartaCookie(request, "refresh_token");


        var validAccessToken = jwtService.validateAndExtractClaims(accessToken);
        var validRefreshToken = jwtService.validateAndExtractClaims(refreshToken);



        if (validAccessToken.isPresent()) {
            var blacklisted = blacklistedTokenService.blacklist(accessToken, JwtType.ACCESS);
            userService.updateUserStatus(blacklisted.getUserId());
        } else {
            log.warn("Access token was missing or invalid during logout, skipping blacklist.");
        }

        if (validRefreshToken.isPresent()) {
            blacklistedTokenService.blacklist(refreshToken, JwtType.REFRESH);
        } else {
            log.warn("Refresh token was missing or invalid during logout, skipping blacklist.");
        }
        if (sessionKeyHeader != null && !sessionKeyHeader.isEmpty()) {

            userSessionService.deactivateUser(sessionKeyHeader);
        }

        responseCookie.clearCookies(response);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of("message", "Logout successful"));
    }

    @Transactional
    public RefreshTokenResponse refresh(String refreshToken, HttpServletResponse response) {





        var claims = jwtProvider.extractClaims(refreshToken, JwtType.REFRESH);
        var sessionId = claims.sessionId();
        if (blacklistedTokenService.isBlacklisted(claims.id())) {
            throw new BadRequestException("Refresh token already revoked");
        }



        blacklistedTokenService.blacklist(refreshToken, JwtType.REFRESH);


        var newAccessToken = jwtProvider.generateAccessToken(claims.userId(), claims.displayName(), sessionId, claims.role(), claims.emailVerified());
        var newRefreshToken = jwtProvider.generateRefreshToken(claims.userId(), claims.displayName(), sessionId, claims.role(), claims.emailVerified());

        responseCookie.setCookies(
                response,
                newAccessToken,
                newRefreshToken);

        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken);

    }



    @Transactional
    public Object resendVerificationEmail(
            String email) {
        var trimmedEmail = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(email, false);
        var user = userRepository.findByEmail(trimmedEmail)
                .orElse(null);

        if (user != null) {

            if (user.isVerified()) {

                return Map.of(
                        "message",
                        "Email already verified.");
            }

            var allowed = otpRatelimit
                    .canRequestOtp(user.getEmail());

            if (allowed) {

                var otp = verificationService
                        .generateOtp(user.getEmail());

                var token = verificationService
                        .generateVerifyToken(
                                user.getEmail());

                otpRatelimit
                        .recordOtp(
                                user.getEmail());
                var userEmail = user.getEmail();
                var name = user.getFullName();
                var phoneNumber = user.getPhoneNumber();

                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail.sendVerificationEmail(userEmail,
                                otp,
                                name,
                                token));
                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail.sendVerificationSms(
                                phoneNumber,
                                otp,
                                name

                        ));
            }
        }

        return Map.of(
                "message",
                "If the email exists, a verification message has been sent");
    }

    @Transactional
    public Object resendPasswordResetLink(
            String email) {

        var user = userRepository.findByEmail(email)
                .orElse(null);

        Long retryAfter = null;

        if (user != null) {

            boolean allowed = otpRatelimit
                    .canRequestOtp(user.getEmail());

            if (allowed) {

                var token = verificationService
                        .generateResetToken(
                                user.getEmail());

                var otp = verificationService
                        .generateOtp(
                                user.getEmail());
                var userEmail = user.getEmail();
                var name = user.getFullName();
                var phoneNumber = user.getPhoneNumber();

                otpRatelimit
                        .recordOtp(
                                user.getEmail());

                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail
                                .sendPasswordResetEmail(
                                        userEmail,
                                        otp,
                                        name,
                                        token));
                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail
                                .sendPasswordResetSms(
                                        phoneNumber,
                                        otp,
                                        name

                                ));
            }

            retryAfter = otpRatelimit
                    .getRetryAfter(
                            user.getEmail());
        }

        assert retryAfter != null;
        return Map.of(
                "message",
                "If the email exists, a reset link has been sent",
                "retry_after",
                retryAfter);
    }

    @Transactional
    public Object forgotPassword(
            String email) {

        return resendPasswordResetLink(email);
    }

    @Transactional
    public Object verifyEmail(
            VerifyEmailRequest request) {

        if ((request.otp() == null
                && request.token() == null)
                ||
                (request.otp() != null
                        && request.token() != null)) {

            throw new BadRequestException("Provide either a token or an Otp");
        }

        String email;

        if (request.otp() != null) {

            email = verificationService.verifyOtp(
                    request.otp());

        } else {

            email = verificationService.verifyVerifyToken(
                    request.token());
        }

        if (email == null) {

            throw new BadRequestException(
                    "Invalid or expired verification");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "If the email exists, a reset link has been sent"));

        user.setVerified(true);
        user.setVerifiedAt(Instant.now());

        userRepository.save(user);

        return Map.of(
                "message",
                "Email verified successfully");
    }

    @Transactional
    public Object resetPassword(
            ResetPasswordRequest request) {

        String email = null;

        if (request.token() != null
                && !request.token().isBlank()) {

            email = verificationService.verifyResetToken(
                    request.token());
        }

        if (email == null
                && request.otp() != null
                && !request.otp().isBlank()) {

            email = verificationService.verifyOtp(
                    request.otp());
        }

        if (email == null) {

            throw new BadRequestException(
                    "Invalid or expired token");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid reset data"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);

        return Map.of(
                "message",
                "Password reset successfully");
    }
    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}
