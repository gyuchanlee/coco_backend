package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leports")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Leports {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "accomcountleports", length = 100)
    private String accomcountleports;

    @Column(name = "chkbabycarriageleports", length = 50)
    private String chkbabycarriageleports;

    @Column(name = "chkcreditcardleports", length = 50)
    private String chkcreditcardleports;

    @Column(name = "chkpetleports", length = 50)
    private String chkpetleports;

    @Column(name = "expagerangeleports", length = 100)
    private String expagerangeleports;

    @Column(name = "infocenterleports", length = 500)
    private String infocenterleports;

    @Column(name = "openperiod", length = 200)
    private String openperiod;

    @Column(name = "parkingfeeleports", length = 200)
    private String parkingfeeleports;

    @Column(name = "parkingleports", columnDefinition = "TEXT")
    private String parkingleports;

    @Column(name = "reservation", columnDefinition = "TEXT")
    private String reservation;

    @Column(name = "restdateleports", length = 200)
    private String restdateleports;

    @Column(name = "scaleleports", length = 200)
    private String scaleleports;

    @Column(name = "usefeeleports", columnDefinition = "TEXT")
    private String usefeeleports;

    @Column(name = "usetimeleports", length = 300)
    private String usetimeleports;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
