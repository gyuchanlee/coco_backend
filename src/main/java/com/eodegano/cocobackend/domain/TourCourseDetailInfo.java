package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_course_detail_info")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TourCourseDetailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "contentid", nullable = false)
    private Long contentid;

    @Column(name = "subcontentid")
    private Long subcontentid;

    @Column(name = "subname", length = 300)
    private String subname;

    @Column(name = "subnum")
    private Integer subnum;

    @Column(name = "subdetailoverview", columnDefinition = "TEXT")
    private String subdetailoverview;

    @Column(name = "subdetailimg", length = 500)
    private String subdetailimg;

    @Column(name = "subdetailalt", length = 300)
    private String subdetailalt;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
