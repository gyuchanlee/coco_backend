package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Culture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultureRepository extends JpaRepository<Culture, Long> {
    List<Culture> findByContentidIn(List<Long> contentIds);
}
