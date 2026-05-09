package com.eodegano.cocobackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_avg_price")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FoodAvgPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lclsSystm3", nullable = false, length = 20)
    private String lclsSystm3;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "avg_price")
    private Integer avgPrice;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "ref_year_month", length = 10)
    private String refYearMonth;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist @PreUpdate
    public void preSave() {
        this.syncedAt = LocalDateTime.now();
    }
}
