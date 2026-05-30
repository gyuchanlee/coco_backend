package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    List<Attraction> findByContentidIn(List<Long> contentIds);
}
