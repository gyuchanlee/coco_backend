package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation_detail_info")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccommodationDetailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "contentid", nullable = false)
    private Long contentid;

    @Column(name = "roomcode", length = 50)
    private String roomcode;

    @Column(name = "roomtitle", length = 200)
    private String roomtitle;

    @Column(name = "roomsize1", length = 50)
    private String roomsize1;

    @Column(name = "roomsize2", length = 50)
    private String roomsize2;

    @Column(name = "roomcount")
    private Integer roomcount;

    @Column(name = "roombasecount")
    private Integer roombasecount;

    @Column(name = "roommaxcount")
    private Integer roommaxcount;

    @Column(name = "roomoffseasonminfee1")
    private Integer roomoffseasonminfee1;

    @Column(name = "roomoffseasonminfee2")
    private Integer roomoffseasonminfee2;

    @Column(name = "roompeakseasonminfee1")
    private Integer roompeakseasonminfee1;

    @Column(name = "roompeakseasonminfee2")
    private Integer roompeakseasonminfee2;

    @Column(name = "roomintro", columnDefinition = "TEXT")
    private String roomintro;

    @Column(name = "roombathfacility", length = 10)
    private String roombathfacility;

    @Column(name = "roombath", length = 10)
    private String roombath;

    @Column(name = "roomhometheater", length = 10)
    private String roomhometheater;

    @Column(name = "roomaircondition", length = 10)
    private String roomaircondition;

    @Column(name = "roomtv", length = 10)
    private String roomtv;

    @Column(name = "roompc", length = 10)
    private String roompc;

    @Column(name = "roomcable", length = 10)
    private String roomcable;

    @Column(name = "roominternet", length = 10)
    private String roominternet;

    @Column(name = "roomrefrigerator", length = 10)
    private String roomrefrigerator;

    @Column(name = "roomtoiletries", length = 10)
    private String roomtoiletries;

    @Column(name = "roomsofa", length = 10)
    private String roomsofa;

    @Column(name = "roomcook", length = 10)
    private String roomcook;

    @Column(name = "roomtable", length = 10)
    private String roomtable;

    @Column(name = "roomhairdryer", length = 10)
    private String roomhairdryer;

    @Column(name = "roomimg1", length = 500)
    private String roomimg1;

    @Column(name = "roomimg1alt", length = 300)
    private String roomimg1alt;

    @Column(name = "cpyrhtDivCd1", length = 10)
    private String cpyrhtDivCd1;

    @Column(name = "roomimg2", length = 500)
    private String roomimg2;

    @Column(name = "roomimg2alt", length = 300)
    private String roomimg2alt;

    @Column(name = "cpyrhtDivCd2", length = 10)
    private String cpyrhtDivCd2;

    @Column(name = "roomimg3", length = 500)
    private String roomimg3;

    @Column(name = "roomimg3alt", length = 300)
    private String roomimg3alt;

    @Column(name = "cpyrhtDivCd3", length = 10)
    private String cpyrhtDivCd3;

    @Column(name = "roomimg4", length = 500)
    private String roomimg4;

    @Column(name = "roomimg4alt", length = 300)
    private String roomimg4alt;

    @Column(name = "cpyrhtDivCd4", length = 10)
    private String cpyrhtDivCd4;

    @Column(name = "roomimg5", length = 500)
    private String roomimg5;

    @Column(name = "roomimg5alt", length = 300)
    private String roomimg5alt;

    @Column(name = "cpyrhtDivCd5", length = 10)
    private String cpyrhtDivCd5;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
