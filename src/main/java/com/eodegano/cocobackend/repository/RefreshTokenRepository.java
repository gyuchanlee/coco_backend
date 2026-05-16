package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.RefreshToken;
import com.eodegano.cocobackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 문자열로 조회 (재발급, 로그아웃 시 사용)
    Optional<RefreshToken> findByToken(String token);

    // 유저 + provider 조합으로 조회 (로그인 시 기존 토큰 갱신 여부 확인)
    Optional<RefreshToken> findByUserAndProvider(User user, String provider);

    // 로그아웃 시 삭제
    void deleteByToken(String token);

    // 회원 탈퇴 시 해당 유저의 모든 토큰 삭제 (provider 무관)
    void deleteByUser(User user);
}
