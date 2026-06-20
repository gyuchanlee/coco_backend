package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_poi_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(UserPoiLike.UserPoiLikeId.class)
public class UserPoiLike {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void preSave() {
        this.createdAt = LocalDateTime.now();
    }

    public static UserPoiLike of(Long userId, Long contentId) {
        UserPoiLike like = new UserPoiLike();
        like.userId = userId;
        like.contentId = contentId;
        return like;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPoiLikeId implements Serializable {
        private Long userId;
        private Long contentId;
    }
}
