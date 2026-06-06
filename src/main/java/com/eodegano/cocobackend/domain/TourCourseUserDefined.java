package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_course_user_defined")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TourCourseUserDefined {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // FK는 DB 레벨에서만 관리, 비로그인 생성 시 null 허용
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "transport", nullable = false, length = 20)
    private String transport;

    @Column(name = "title", length = 255)
    private String title;

    // JSON 배열 문자열: ["자연", "맛집", "힐링"]
    @Column(name = "theme", columnDefinition = "JSON")
    private String theme;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public void assignUser(Long userId) {
        this.userId = userId;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}
