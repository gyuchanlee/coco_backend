package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Shopping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingRepository extends JpaRepository<Shopping, Long> {
    List<Shopping> findByContentidIn(List<Long> contentIds);
}
