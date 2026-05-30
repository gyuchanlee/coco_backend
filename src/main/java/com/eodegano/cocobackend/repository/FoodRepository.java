package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByContentidIn(List<Long> contentIds);
}
