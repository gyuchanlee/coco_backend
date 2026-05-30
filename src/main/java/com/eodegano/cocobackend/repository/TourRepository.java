package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByLDongSignguCd(String lDongSignguCd);
    List<Tour> findByContentidIn(List<Long> contentIds);
}
