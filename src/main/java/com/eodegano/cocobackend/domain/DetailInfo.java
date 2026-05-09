package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "detail_info")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DetailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "contentid", nullable = false)
    private Long contentid;

    @Column(name = "contenttypeid", nullable = false, columnDefinition = "TINYINT")
    private Integer contenttypeid;

    @Column(name = "fldgubun", length = 10)
    private String fldgubun;

    @Column(name = "serialnum")
    private Integer serialnum;

    @Column(name = "infoname", length = 200)
    private String infoname;

    @Column(name = "infotext", columnDefinition = "TEXT")
    private String infotext;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
