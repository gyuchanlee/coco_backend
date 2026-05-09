package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attraction")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Attraction {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "accomcount", length = 100)
    private String accomcount;

    @Column(name = "chkbabycarriage", length = 50)
    private String chkbabycarriage;

    @Column(name = "chkcreditcard", length = 50)
    private String chkcreditcard;

    @Column(name = "chkpet", length = 50)
    private String chkpet;

    @Column(name = "expagerange", length = 100)
    private String expagerange;

    @Column(name = "expguide", columnDefinition = "TEXT")
    private String expguide;

    @Column(name = "heritage1")
    private Boolean heritage1;

    @Column(name = "heritage2")
    private Boolean heritage2;

    @Column(name = "heritage3")
    private Boolean heritage3;

    @Column(name = "infocenter", length = 500)
    private String infocenter;

    @Column(name = "opendate", length = 100)
    private String opendate;

    @Column(name = "parking", columnDefinition = "TEXT")
    private String parking;

    @Column(name = "restdate", length = 200)
    private String restdate;

    @Column(name = "useseason", length = 200)
    private String useseason;

    @Column(name = "usetime", length = 300)
    private String usetime;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
