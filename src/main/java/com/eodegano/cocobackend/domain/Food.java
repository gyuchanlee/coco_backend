package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "food")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Food {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "chkcreditcardfood", length = 50)
    private String chkcreditcardfood;

    @Column(name = "discountinfofood", columnDefinition = "TEXT")
    private String discountinfofood;

    @Column(name = "firstmenu", length = 300)
    private String firstmenu;

    @Column(name = "infocenterfood", length = 500)
    private String infocenterfood;

    @Column(name = "kidsfacility", length = 50)
    private String kidsfacility;

    @Column(name = "opendatefood", length = 100)
    private String opendatefood;

    @Column(name = "opentimefood", length = 300)
    private String opentimefood;

    @Column(name = "packing", length = 50)
    private String packing;

    @Column(name = "parkingfood", columnDefinition = "TEXT")
    private String parkingfood;

    @Column(name = "reservationfood", columnDefinition = "TEXT")
    private String reservationfood;

    @Column(name = "restdatefood", length = 200)
    private String restdatefood;

    @Column(name = "scalefood", length = 200)
    private String scalefood;

    @Column(name = "seat")
    private Integer seat;

    @Column(name = "smoking", length = 50)
    private String smoking;

    @Column(name = "treatmenu", columnDefinition = "TEXT")
    private String treatmenu;

    @Column(name = "lcnsno", length = 100)
    private String lcnsno;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
