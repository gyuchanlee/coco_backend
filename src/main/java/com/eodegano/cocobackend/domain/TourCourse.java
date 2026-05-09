package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_course")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TourCourse {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "distance", length = 100)
    private String distance;

    @Column(name = "infocentertourcourse", length = 500)
    private String infocentertourcourse;

    @Column(name = "schedule", columnDefinition = "TEXT")
    private String schedule;

    @Column(name = "taketime", length = 100)
    private String taketime;

    @Column(name = "theme", length = 300)
    private String theme;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
