package com.eodegano.cocobackend.controller;

import com.eodegano.cocobackend.dto.ApiResponse;
import com.eodegano.cocobackend.dto.AuthTokenResult;
import com.eodegano.cocobackend.dto.KakaoOAuthCallbackRequestDto;
import com.eodegano.cocobackend.dto.LoginRequestDto;
import com.eodegano.cocobackend.dto.LoginResponseDto;
import com.eodegano.cocobackend.service.AuthService;
import com.eodegano.cocobackend.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoOAuthService kakaoOAuthService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    // ───────────────────────────────────────────────
    // 로그인 — AccessToken: body / RefreshToken: HttpOnly Cookie
    // ───────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @RequestBody @Valid LoginRequestDto request,
            HttpServletResponse response) {
        AuthTokenResult result = authService.login(request);
        setRefreshTokenCookie(response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("로그인에 성공했습니다.", new LoginResponseDto(result.accessToken())));
    }

    // ───────────────────────────────────────────────
    // 로그아웃 — 쿠키에서 RefreshToken 추출 후 삭제
    // ───────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.ok("로그아웃되었습니다."));
    }

    // ───────────────────────────────────────────────
    // AccessToken 재발급 — 쿠키에서 RefreshToken 추출
    // ───────────────────────────────────────────────
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponseDto>> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "RefreshToken 쿠키가 없습니다. 다시 로그인해 주세요.");
        }
        AuthTokenResult result = authService.reissue(refreshToken);
        setRefreshTokenCookie(response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("토큰이 재발급되었습니다.", new LoginResponseDto(result.accessToken())));
    }

    // ───────────────────────────────────────────────
    // 카카오 OAuth 콜백 — FE에서 발급한 카카오 AccessToken을 검증 후 자체 JWT 발급
    // ───────────────────────────────────────────────
    @PostMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponseDto>> kakaoCallback(
            @RequestBody @Valid KakaoOAuthCallbackRequestDto request,
            HttpServletResponse response) {
        AuthTokenResult result = kakaoOAuthService.kakaoLogin(request.getKakaoAccessToken());
        setRefreshTokenCookie(response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("카카오 로그인에 성공했습니다.", new LoginResponseDto(result.accessToken())));
    }

    // ───────────────────────────────────────────────
    // 쿠키 헬퍼
    // ───────────────────────────────────────────────
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
