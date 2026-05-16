package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByEmail(String email);

    // 활성 유저만 이메일 중복 체크 (삭제된 유저 제외)
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // 삭제되지 않은 유저 조회
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // 탈퇴한 유저 이메일로 조회 (재가입 확인용)
    Optional<User> findByEmailAndDeletedAtIsNotNull(String email);
}
