package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Leports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeportsRepository extends JpaRepository<Leports, Long> {
    List<Leports> findByContentidIn(List<Long> contentIds);
}
