package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.DetailCommon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailCommonRepository extends JpaRepository<DetailCommon, Long> {
    List<DetailCommon> findByContentidIn(List<Long> contentIds);
}
