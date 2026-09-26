package org.tenacitycodex.renyun.api.v1.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.tenacitycodex.renyun.common.annotation.RateLimit;
import org.tenacitycodex.renyun.common.config.security.SecurityUtil;
import org.tenacitycodex.renyun.common.dto.ApiResponse;
import org.tenacitycodex.renyun.common.dto.UserDTO;
import org.tenacitycodex.renyun.common.dto.request.LoginRequest;
import org.tenacitycodex.renyun.common.dto.request.PatientRegisterRequest;
import org.tenacitycodex.renyun.common.dto.request.RegisterRequest;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.common.util.JwtUtil;
import org.tenacitycodex.renyun.component.ILoginStrategy;
import org.tenacitycodex.renyun.component.LoginStrategyFactory;
import org.tenacitycodex.renyun.module.user.entity.User;
import org.tenacitycodex.renyun.module.user.service.UserService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private static final String COOKIE_ROOT = "/";
    private final LoginStrategyFactory loginStrategyFactory;

    @Autowired
    public AuthController(JwtUtil jwtUtil, UserService userService, LoginStrategyFactory loginStrategyFactory) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.loginStrategyFactory = loginStrategyFactory;
    }

    @CrossOrigin
    @RateLimit
    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest formData, HttpServletResponse httpResponse) {
        log.info("User response: {}", formData);
        String loginType = formData.getLoginType();
        Map<String, Object> params = formData.getCredential();

        ILoginStrategy strategy = loginStrategyFactory.getStrategy(loginType);
        User user = strategy.authenticate(params);

        if (user != null) {
            if ("patient".equals(user.getRole()) && "pending".equals(user.getStatus())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "等待医生审核，请耐心等待");
            }
            if ("patient".equals(user.getRole()) && "rejected".equals(user.getStatus())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "注册申请已被拒绝，请联系医生");
            }
            String token = jwtUtil.generateToken(user);
            ResponseCookie auth = ResponseCookie.from("access_token", token)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path(COOKIE_ROOT)
                    .maxAge(Duration.ofDays(7))
                    .build();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, auth.toString());
            Map<String, Object> userInfo = userService.buildFullUserInfo(user);
            return ResponseEntity.ok(ApiResponse.ok(userInfo));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @RateLimit
    @DeleteMapping("/api/v1/auth/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse httpResponse) {
        if (!SecurityUtil.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Logged in.");
        }
        Cookie userIdCookie = new Cookie("user_id", "");
        Cookie auth = new Cookie("access_token", "");
        userIdCookie.setPath("/");
        userIdCookie.setMaxAge(0);
        auth.setMaxAge(0);
        auth.setPath("/");
        httpResponse.addCookie(userIdCookie);
        httpResponse.addCookie(auth);

        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

    @RateLimit(maxRequests = 2)
    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest payload, HttpServletResponse httpResponse) {
        User registered;
        try {
            registered = userService.register(payload, payload.getPassword());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        if (registered != null) {
            String token = jwtUtil.generateToken(registered);
            ResponseCookie auth = ResponseCookie.from("access_token", token)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path(COOKIE_ROOT)
                    .maxAge(Duration.ofDays(7))
                    .build();
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, auth.toString());
            return ResponseEntity.created(ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(registered.getId())
                            .toUri())
                    .body(ApiResponse.ok(registered));
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "参数不完整");
        }
    }
}
