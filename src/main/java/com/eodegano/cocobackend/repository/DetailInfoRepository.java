package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.DetailInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailInfoRepository extends JpaRepository<DetailInfo, Long> {
    List<DetailInfo> findByContentidIn(List<Long> contentIds);
}
