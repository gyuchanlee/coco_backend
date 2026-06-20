package com.eodegano.cocobackend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TourCourseListItemDto {
    private Long courseId;
    private String title;
    private int peopleCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String transport;
    private List<String> theme;
    private LocalDateTime createdAt;
}
