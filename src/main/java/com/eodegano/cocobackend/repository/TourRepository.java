package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, Long> {}
