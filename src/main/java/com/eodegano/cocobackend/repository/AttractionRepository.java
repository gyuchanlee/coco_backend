package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {}
