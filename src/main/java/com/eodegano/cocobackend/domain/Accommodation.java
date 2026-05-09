package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Accommodation {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "accomcountlodging", length = 100)
    private String accomcountlodging;

    @Column(name = "checkintime", length = 50)
    private String checkintime;

    @Column(name = "checkouttime", length = 50)
    private String checkouttime;

    @Column(name = "chkcooking", length = 50)
    private String chkcooking;

    @Column(name = "foodplace", length = 300)
    private String foodplace;

    @Column(name = "infocenterlodging", length = 500)
    private String infocenterlodging;

    @Column(name = "parkinglodging", columnDefinition = "TEXT")
    private String parkinglodging;

    @Column(name = "pickup", length = 100)
    private String pickup;

    @Column(name = "roomcount")
    private Integer roomcount;

    @Column(name = "reservationlodging", columnDefinition = "TEXT")
    private String reservationlodging;

    @Column(name = "reservationurl", length = 500)
    private String reservationurl;

    @Column(name = "roomtype", length = 200)
    private String roomtype;

    @Column(name = "scalelodging", length = 200)
    private String scalelodging;

    @Column(name = "subfacility", columnDefinition = "TEXT")
    private String subfacility;

    @Column(name = "barbecue", length = 10)
    private String barbecue;

    @Column(name = "beauty", length = 10)
    private String beauty;

    @Column(name = "beverage", length = 10)
    private String beverage;

    @Column(name = "bicycle", length = 10)
    private String bicycle;

    @Column(name = "campfire", length = 10)
    private String campfire;

    @Column(name = "fitness", length = 10)
    private String fitness;

    @Column(name = "karaoke", length = 10)
    private String karaoke;

    @Column(name = "publicbath", length = 10)
    private String publicbath;

    @Column(name = "publicpc", length = 10)
    private String publicpc;

    @Column(name = "sauna", length = 10)
    private String sauna;

    @Column(name = "seminar", length = 10)
    private String seminar;

    @Column(name = "sports", length = 10)
    private String sports;

    @Column(name = "refundregulation", columnDefinition = "TEXT")
    private String refundregulation;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
