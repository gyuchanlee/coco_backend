package com.eodegano.cocobackend.controller;

import com.eodegano.cocobackend.dto.ApiResponse;
import com.eodegano.cocobackend.dto.TourCourseGenerateRequestDto;
import com.eodegano.cocobackend.dto.TourCourseGenerateResponseDto;
import com.eodegano.cocobackend.dto.TourCourseTitleUpdateRequestDto;
import com.eodegano.cocobackend.service.TourCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/tour-course")
@RequiredArgsConstructor
public class TourCourseController {

    private final TourCourseService tourCourseService;

    @PostMapping
    public ResponseEntity<ApiResponse<TourCourseGenerateResponseDto>> generateTourCourse(
            @RequestBody @Valid TourCourseGenerateRequestDto request,
            Authentication authentication
    ) {
        log.info("Received tour course generation request: {}", request);

        Long userId = null;

        TourCourseGenerateResponseDto result = tourCourseService.generateTourCourse(request, userId);

        log.info("Tour course generated successfully. CourseId: {}", result.getCourseId());
        return ResponseEntity.ok(ApiResponse.ok("여행 코스가 생성되었습니다.", result));
    }

    @PatchMapping("/{courseId}/title")
    public ResponseEntity<ApiResponse<Void>> updateCourseTitle(
            @PathVariable Long courseId,
            @RequestBody @Valid TourCourseTitleUpdateRequestDto request,
            Authentication authentication
    ) {
        log.info("Updating title for courseId: {}", courseId);
        tourCourseService.updateCourseTitle(courseId, request.getTitle(), authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("코스 제목이 수정되었습니다."));
    }
}
