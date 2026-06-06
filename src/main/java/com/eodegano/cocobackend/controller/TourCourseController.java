package com.eodegano.cocobackend.controller;

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

import static org.springframework.web.bind.annotation.RequestMethod.PATCH;

@Slf4j
@RestController
@RequestMapping("/api/v1/tour-course")
@RequiredArgsConstructor
public class TourCourseController {

    private final TourCourseService tourCourseService;

    @PostMapping
    public ResponseEntity<TourCourseGenerateResponseDto> generateTourCourse(
            @RequestBody @Valid TourCourseGenerateRequestDto request,
            Authentication authentication
    ) {
        log.info("Received tour course generation request: {}", request);

        // userId는 항상 null (비로그인 허용, 저장 기능은 추후 별도 API)
        Long userId = null;

        TourCourseGenerateResponseDto response = tourCourseService.generateTourCourse(request, userId);

        log.info("Tour course generated successfully. CourseId: {}", response.getCourseId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{courseId}/title")
    public ResponseEntity<Void> updateCourseTitle(
            @PathVariable Long courseId,
            @RequestBody @Valid TourCourseTitleUpdateRequestDto request,
            Authentication authentication
    ) {
        log.info("Updating title for courseId: {}", courseId);
        tourCourseService.updateCourseTitle(courseId, request.getTitle(), authentication.getName());
        return ResponseEntity.ok().build();
    }
}
