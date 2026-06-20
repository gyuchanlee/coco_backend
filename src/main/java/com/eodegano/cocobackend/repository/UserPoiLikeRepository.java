package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.UserPoiLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPoiLikeRepository extends JpaRepository<UserPoiLike, UserPoiLike.UserPoiLikeId> {
    Optional<UserPoiLike> findByUserIdAndContentId(Long userId, Long contentId);
    boolean existsByUserIdAndContentId(Long userId, Long contentId);
}
