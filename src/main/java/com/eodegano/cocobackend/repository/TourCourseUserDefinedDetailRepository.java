package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.TourCourseUserDefinedDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourCourseUserDefinedDetailRepository extends JpaRepository<TourCourseUserDefinedDetail, Long> {
    List<TourCourseUserDefinedDetail> findByTourCourseId(Long tourCourseId);
}
