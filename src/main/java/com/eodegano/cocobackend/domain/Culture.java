package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "culture")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Culture {

    @Id
    @Column(name = "contentid")
    private Long contentid;

    @Column(name = "accomcountculture", length = 100)
    private String accomcountculture;

    @Column(name = "chkbabycarriageculture", length = 50)
    private String chkbabycarriageculture;

    @Column(name = "chkcreditcardculture", length = 50)
    private String chkcreditcardculture;

    @Column(name = "chkpetculture", length = 50)
    private String chkpetculture;

    @Column(name = "discountinfo", columnDefinition = "TEXT")
    private String discountinfo;

    @Column(name = "infocenterculture", length = 500)
    private String infocenterculture;

    @Column(name = "parkingculture", columnDefinition = "TEXT")
    private String parkingculture;

    @Column(name = "parkingfee", length = 200)
    private String parkingfee;

    @Column(name = "restdateculture", length = 200)
    private String restdateculture;

    @Column(name = "usefee", columnDefinition = "TEXT")
    private String usefee;

    @Column(name = "usetimeculture", length = 300)
    private String usetimeculture;

    @Column(name = "scale", length = 200)
    private String scale;

    @Column(name = "spendtime", length = 100)
    private String spendtime;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
