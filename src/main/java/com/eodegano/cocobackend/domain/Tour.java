package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Tour {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "contenttypeid", columnDefinition = "TINYINT")
    private Integer contenttypeid;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "addr1", length = 300)
    private String addr1;

    @Column(name = "addr2", length = 300)
    private String addr2;

    @Column(name = "zipcode", length = 10)
    private String zipcode;

    @Column(name = "tel", length = 100)
    private String tel;

    @Column(name = "firstimage", length = 500)
    private String firstimage;

    @Column(name = "firstimage2", length = 500)
    private String firstimage2;

    @Column(name = "cpyrhtDivCd", length = 10)
    private String cpyrhtDivCd;

    @Column(name = "mapx", precision = 15, scale = 10)
    private BigDecimal mapx;

    @Column(name = "mapy", precision = 15, scale = 10)
    private BigDecimal mapy;

    @Column(name = "mlevel", columnDefinition = "TINYINT")
    private Integer mlevel;

    @Column(name = "lDongRegnCd", length = 10)
    private String lDongRegnCd;

    @Column(name = "lDongSignguCd", length = 10)
    private String lDongSignguCd;

    @Column(name = "lclsSystm1", length = 20)
    private String lclsSystm1;

    @Column(name = "lclsSystm2", length = 20)
    private String lclsSystm2;

    @Column(name = "lclsSystm3", length = 20)
    private String lclsSystm3;

    @Column(name = "createdtime")
    private LocalDateTime createdtime;

    @Column(name = "modifiedtime")
    private LocalDateTime modifiedtime;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @Column(name = "stars", precision = 3, scale = 1)
    private BigDecimal stars;

    @Column(name = "likes")
    private Integer likes;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }

    public int getLikesOrZero() {
        return likes == null ? 0 : likes;
    }
}
