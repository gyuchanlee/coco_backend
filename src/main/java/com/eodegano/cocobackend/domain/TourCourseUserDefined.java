package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_course_user_defined")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TourCourseUserDefined {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // user_id는 NULL 허용 (비회원), FK는 DB 레벨에서만 관리
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "travel_date")
    private LocalDate travelDate;

    @Column(name = "party_size", nullable = false, columnDefinition = "TINYINT")
    private Integer partySize;

    @Column(name = "total_budget")
    private Integer totalBudget;

    @Column(name = "per_budget")
    private Integer perBudget;

    // JSON 컬럼: 코스 POI 배열 (contentid, 순서, 예상비용 등)
    @Column(name = "course_data", nullable = false, columnDefinition = "JSON")
    private String courseData;

    @Column(name = "share_token", length = 100)
    private String shareToken;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

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
}
