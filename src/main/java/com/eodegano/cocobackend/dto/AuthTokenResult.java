package com.eodegano.cocobackend.dto;

/**
 * 서비스 레이어 내부용 토큰 결과.
 * AccessToken은 응답 바디로, RefreshToken은 HttpOnly 쿠키로 내려주기 위해 분리.
 */
public record AuthTokenResult(String accessToken, String refreshToken) {}
