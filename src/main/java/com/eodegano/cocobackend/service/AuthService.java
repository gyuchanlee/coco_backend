package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.domain.RefreshToken;
import com.eodegano.cocobackend.domain.User;
import com.eodegano.cocobackend.dto.LoginRequestDto;
import com.eodegano.cocobackend.dto.LoginResponseDto;
import com.eodegano.cocobackend.dto.TokenReissueRequestDto;
import com.eodegano.cocobackend.repository.RefreshTokenRepository;
import com.eodegano.cocobackend.repository.UserRepository;
import com.eodegano.cocobackend.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // ───────────────────────────────────────────────
    // 로그인
    // ───────────────────────────────────────────────
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        // AuthenticationManager가 내부적으로 UserDetailsService + PasswordEncoder로 검증
        // 실패 시 BadCredentialsException, UsernameNotFoundException 등을 잡아 클라이언트 친화적 메시지로 변환
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // authentication.getName() == 인증된 유저의 email (UserDetailsService에서 이미 조회 완료)
        // → 중복 DB 조회 방지를 위해 authentication에서 email 추출 후 단 1회만 조회
        String email = authentication.getName();
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail(), user.getRole());

        // 기존 RefreshToken이 있으면 rotate, 없으면 새로 저장
        refreshTokenRepository.findByUserAndProvider(user, "local")
                .ifPresentOrElse(
                        existing -> existing.rotate(refreshToken, jwtProvider.getRefreshTokenExpiresAt()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .user(user)
                                        .token(refreshToken)
                                        .provider("local")
                                        .expiresAt(jwtProvider.getRefreshTokenExpiresAt())
                                        .build()
                        )
                );

        return new LoginResponseDto(accessToken, refreshToken);
    }

    // ───────────────────────────────────────────────
    // 로그아웃
    // ───────────────────────────────────────────────
    @Transactional
    public void logout(String refreshToken) {
        // RefreshToken DB에서 삭제 → 재발급 불가 상태로 만듦
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    // ───────────────────────────────────────────────
    // AccessToken 재발급
    // Exception : 권한없음 401 에러로 통일
    // ───────────────────────────────────────────────
    @Transactional
    public LoginResponseDto reissue(TokenReissueRequestDto request) {
        String oldRefreshToken = request.getRefreshToken();


        // 1. 토큰 유효성 검증
        if (!jwtProvider.validateToken(oldRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 RefreshToken입니다.");
        }

        // 2. DB에서 RefreshToken 조회
        RefreshToken savedToken = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 RefreshToken입니다."));

        // 3. 만료 여부 체크 (애플리케이션 레벨 이중 검증)
        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "만료된 RefreshToken입니다. 다시 로그인해 주세요.");
        }

        User user = savedToken.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getEmail(), user.getRole());

        // 4. RefreshToken rotate (기존 토큰 갱신)
        savedToken.rotate(newRefreshToken, jwtProvider.getRefreshTokenExpiresAt());

        return new LoginResponseDto(newAccessToken, newRefreshToken);
    }
}
