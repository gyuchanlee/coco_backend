package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Shopping {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "chkbabycarriageshopping", length = 50)
    private String chkbabycarriageshopping;

    @Column(name = "chkcreditcardshopping", length = 50)
    private String chkcreditcardshopping;

    @Column(name = "chkpetshopping", length = 50)
    private String chkpetshopping;

    @Column(name = "culturecenter", length = 500)
    private String culturecenter;

    @Column(name = "fairday", length = 200)
    private String fairday;

    @Column(name = "infocentershopping", length = 500)
    private String infocentershopping;

    @Column(name = "opendateshopping", length = 100)
    private String opendateshopping;

    @Column(name = "opentime", length = 300)
    private String opentime;

    @Column(name = "parkingshopping", columnDefinition = "TEXT")
    private String parkingshopping;

    @Column(name = "restdateshopping", length = 200)
    private String restdateshopping;

    @Column(name = "restroom", length = 200)
    private String restroom;

    @Column(name = "saleitem", columnDefinition = "TEXT")
    private String saleitem;

    @Column(name = "saleitemcost", columnDefinition = "TEXT")
    private String saleitemcost;

    @Column(name = "scaleshopping", length = 200)
    private String scaleshopping;

    @Column(name = "shopguide", columnDefinition = "TEXT")
    private String shopguide;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
