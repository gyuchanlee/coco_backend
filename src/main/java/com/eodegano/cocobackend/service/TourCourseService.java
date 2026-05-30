package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.dto.TourCourseGenerateRequestDto;
import com.eodegano.cocobackend.dto.TourCourseGenerateResponseDto;

public interface TourCourseService {
    TourCourseGenerateResponseDto generateTourCourse(TourCourseGenerateRequestDto request, Long userId);
}
