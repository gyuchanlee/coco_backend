package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    List<Accommodation> findByContentidIn(List<Long> contentIds);
}
