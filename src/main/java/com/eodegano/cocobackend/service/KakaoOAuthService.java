package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.client.KakaoApiClient;
import com.eodegano.cocobackend.client.KakaoApiClient.KakaoUserInfo;
import com.eodegano.cocobackend.domain.RefreshToken;
import com.eodegano.cocobackend.domain.User;
import com.eodegano.cocobackend.dto.LoginResponseDto;
import com.eodegano.cocobackend.repository.RefreshTokenRepository;
import com.eodegano.cocobackend.repository.UserRepository;
import com.eodegano.cocobackend.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private static final String PROVIDER = "kakao";

    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    /**
     * 카카오 AccessToken으로 자체 JWT 세션을 발급한다.
     * - 기존 카카오 유저 → 로그인 처리
     * - 신규 유저 → 자동 가입 후 로그인 처리
     */
    @Transactional
    public LoginResponseDto kakaoLogin(String kakaoAccessToken) {
        KakaoUserInfo userInfo = kakaoApiClient.getUserInfo(kakaoAccessToken);
        String providerId = String.valueOf(userInfo.getId());

        User user = userRepository.findByProviderAndProviderId(PROVIDER, providerId)
                .orElseGet(() -> registerKakaoUser(userInfo, providerId));

        return issueJwtTokens(user);
    }

    private User registerKakaoUser(KakaoUserInfo userInfo, String providerId) {
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();

        // 동일 이메일로 로컬 계정이 이미 존재하면 카카오 계정으로 연결
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(existing -> {
                    log.info("기존 로컬 계정에 카카오 연결: email={}", email);
                    existing.linkKakao(providerId);
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("카카오 신규 회원 가입: providerId={}", providerId);
                    return userRepository.save(User.ofKakao(email, nickname, providerId));
                });

        return user;
    }

    private LoginResponseDto issueJwtTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail(), user.getRole());

        refreshTokenRepository.findByUserAndProvider(user, PROVIDER)
                .ifPresentOrElse(
                        existing -> existing.rotate(refreshToken, jwtProvider.getRefreshTokenExpiresAt()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .user(user)
                                        .token(refreshToken)
                                        .provider(PROVIDER)
                                        .expiresAt(jwtProvider.getRefreshTokenExpiresAt())
                                        .build()
                        )
                );

        return new LoginResponseDto(accessToken, refreshToken);
    }
}
