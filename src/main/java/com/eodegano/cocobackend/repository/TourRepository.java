package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    @Query("SELECT t FROM Tour t WHERE t.lDongSignguCd = :lDongSignguCd")
    List<Tour> findByLDongSignguCd(@Param("lDongSignguCd") String lDongSignguCd);

    List<Tour> findByContentidIn(List<Long> contentIds);
}
