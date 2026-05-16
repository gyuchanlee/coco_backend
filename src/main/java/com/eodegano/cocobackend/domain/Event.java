package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Event {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "agelimit", length = 100)
    private String agelimit;

    @Column(name = "bookingplace", length = 300)
    private String bookingplace;

    @Column(name = "discountinfofestival", columnDefinition = "TEXT")
    private String discountinfofestival;

    @Column(name = "eventenddate")
    private LocalDate eventenddate;

    @Column(name = "eventhomepage", length = 500)
    private String eventhomepage;

    @Column(name = "eventplace", length = 300)
    private String eventplace;

    @Column(name = "eventstartdate")
    private LocalDate eventstartdate;

    @Column(name = "festivalgrade", length = 50)
    private String festivalgrade;

    @Column(name = "placeinfo", columnDefinition = "TEXT")
    private String placeinfo;

    @Column(name = "playtime", length = 200)
    private String playtime;

    @Column(name = "program", columnDefinition = "TEXT")
    private String program;

    @Column(name = "spendtimefestival", length = 100)
    private String spendtimefestival;

    @Column(name = "sponsor1", length = 200)
    private String sponsor1;

    @Column(name = "sponsor1tel", length = 100)
    private String sponsor1tel;

    @Column(name = "sponsor2", length = 200)
    private String sponsor2;

    @Column(name = "sponsor2tel", length = 100)
    private String sponsor2tel;

    @Column(name = "subevent", columnDefinition = "TEXT")
    private String subevent;

    @Column(name = "usetimefestival", columnDefinition = "TEXT")
    private String usetimefestival;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
