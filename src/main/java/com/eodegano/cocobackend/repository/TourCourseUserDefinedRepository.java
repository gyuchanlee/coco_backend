package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.TourCourseUserDefined;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourCourseUserDefinedRepository extends JpaRepository<TourCourseUserDefined, Long> {

    List<TourCourseUserDefined> findByUserId(Long userId);
}
