package com.eodegano.cocobackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    // 테스트용 시크릿 (256bit 이상)
    private static final String SECRET = "test-secret-key-for-jwt-provider-unit-test-must-be-long-enough";
    private static final String EMAIL = "test@test.com";
    private static final String ROLE = "USER";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        // @Value 대신 ReflectionTestUtils로 필드 직접 주입 (Spring Context 없이)
        ReflectionTestUtils.setField(jwtProvider, "secret", SECRET);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiry", 900000L);   // 15분
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpiry", 604800000L); // 7일
        jwtProvider.init(); // @PostConstruct 수동 호출
    }

    @Test
    @DisplayName("AccessToken 생성 후 유효성 검증 성공")
    void generateAndValidateAccessToken() {
        String token = jwtProvider.generateAccessToken(EMAIL, ROLE);

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("RefreshToken 생성 후 유효성 검증 성공")
    void generateAndValidateRefreshToken() {
        String token = jwtProvider.generateRefreshToken(EMAIL, ROLE);

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void getEmailFromToken() {
        String token = jwtProvider.generateAccessToken(EMAIL, ROLE);

        assertThat(jwtProvider.getEmail(token)).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("토큰에서 Authentication 추출 시 email, 권한 정상 매핑")
    void getAuthenticationFromToken() {
        String token = jwtProvider.generateAccessToken(EMAIL, ROLE);

        Authentication authentication = jwtProvider.getAuthentication(token);

        assertThat(authentication.getName()).isEqualTo(EMAIL);
        assertThat(authentication.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + ROLE));
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void expiredTokenValidationFails() {
        // expiry = 0 으로 즉시 만료 토큰 생성
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiry", 0L);
        String expiredToken = jwtProvider.generateAccessToken(EMAIL, ROLE);

        assertThat(jwtProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰 검증 실패")
    void tamperedTokenValidationFails() {
        String token = jwtProvider.generateAccessToken(EMAIL, ROLE);
        String tampered = token + "tampered";

        assertThat(jwtProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("RefreshToken 만료시간 LocalDateTime 반환")
    void getRefreshTokenExpiresAt() {
        assertThat(jwtProvider.getRefreshTokenExpiresAt()).isNotNull();
    }
}
