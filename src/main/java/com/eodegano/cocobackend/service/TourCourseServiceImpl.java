package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.client.GroqApiClient;
import com.eodegano.cocobackend.domain.*;
import com.eodegano.cocobackend.domain.enums.PlaceType;
import com.eodegano.cocobackend.dto.TourCourseAiResponseDto;
import com.eodegano.cocobackend.dto.TourCourseGenerateRequestDto;
import com.eodegano.cocobackend.dto.TourCourseGenerateResponseDto;
import com.eodegano.cocobackend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourCourseServiceImpl implements TourCourseService {

    private final GroqApiClient groqApiClient;
    private final TourRepository tourRepository;
    private final DetailCommonRepository detailCommonRepository;
    private final DetailInfoRepository detailInfoRepository;
    private final AttractionRepository attractionRepository;
    private final FoodRepository foodRepository;
    private final CultureRepository cultureRepository;
    private final EventRepository eventRepository;
    private final LeportsRepository leportsRepository;
    private final ShoppingRepository shoppingRepository;
    private final AccommodationRepository accommodationRepository;
    private final TourCourseUserDefinedRepository tourCourseUserDefinedRepository;
    private final TourCourseUserDefinedDetailRepository tourCourseUserDefinedDetailRepository;

    @Override
    @Transactional
    public TourCourseGenerateResponseDto generateTourCourse(TourCourseGenerateRequestDto request, Long userId) {
        log.info("Generating tour course for user: {}, request: {}", userId, request);

        // 1. DB 데이터 조회 및 JSON 변환
        String placesData = fetchPlacesData(request.getSigunguCode());

        // 2. 사용자 요청 문자열 생성
        String userRequest = buildUserRequest(request);

        // 3. Groq API 호출
        TourCourseAiResponseDto aiResponse = groqApiClient.generateTourCourse(placesData, userRequest);

        // 4. AI 응답 검증
        validateAiResponse(aiResponse, request.getStartDate(), request.getEndDate());

        // 5. DB 저장
        TourCourseUserDefined savedCourse = saveTourCourse(request, userId, aiResponse);

        // 6. 응답 생성
        return buildResponse(savedCourse.getId(), aiResponse);
    }

    private String fetchPlacesData(String sigunguCode) {
        log.info("Fetching places data for sigunguCode: {}", sigunguCode);

        // Tour 데이터 조회
        List<Tour> tours;
        if (sigunguCode == null || sigunguCode.isBlank()) {
            tours = tourRepository.findAll();
        } else {
            tours = tourRepository.findByLDongSignguCd(sigunguCode);
        }

        if (tours.isEmpty()) {
            throw new IllegalArgumentException("해당 지역의 여행지 데이터가 없습니다");
        }

        // contentIds 추출
        List<Long> contentIds = tours.stream()
                .map(Tour::getContentid)
                .collect(Collectors.toList());

        // DetailCommon 조회
        Map<Long, DetailCommon> detailCommonMap = detailCommonRepository.findByContentidIn(contentIds)
                .stream()
                .collect(Collectors.toMap(DetailCommon::getContentid, dc -> dc));

        // DetailInfo 조회
        Map<Long, List<DetailInfo>> detailInfoMap = detailInfoRepository.findByContentidIn(contentIds)
                .stream()
                .collect(Collectors.groupingBy(DetailInfo::getContentid));

        // 타입별 상세 데이터 조회
        Map<Long, Object> typeSpecificMap = fetchTypeSpecificDetails(contentIds);

        // JSON 생성
        return buildPlacesJson(tours, detailCommonMap, detailInfoMap, typeSpecificMap);
    }

    private Map<Long, Object> fetchTypeSpecificDetails(List<Long> contentIds) {
        Map<Long, Object> typeSpecificMap = new HashMap<>();

        attractionRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        foodRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        cultureRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        eventRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        leportsRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        shoppingRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        accommodationRepository.findByContentidIn(contentIds)
                .forEach(item -> typeSpecificMap.put(item.getContentid(), item));

        return typeSpecificMap;
    }

    private String buildPlacesJson(List<Tour> tours,
                                   Map<Long, DetailCommon> detailCommonMap,
                                   Map<Long, List<DetailInfo>> detailInfoMap,
                                   Map<Long, Object> typeSpecificMap) {
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < tours.size(); i++) {
            Tour tour = tours.get(i);
            DetailCommon detailCommon = detailCommonMap.get(tour.getContentid());
            List<DetailInfo> detailInfos = detailInfoMap.getOrDefault(tour.getContentid(), Collections.emptyList());

            json.append("  {\n");
            json.append("    \"contentId\": ").append(tour.getContentid()).append(",\n");
            json.append("    \"type\": \"").append(getPlaceType(tour.getContenttypeid())).append("\",\n");
            json.append("    \"title\": \"").append(escapeJson(tour.getTitle())).append("\",\n");
            json.append("    \"addr\": \"").append(escapeJson(tour.getAddr1())).append("\",\n");
            json.append("    \"mapx\": ").append(tour.getMapx() != null ? tour.getMapx() : "null").append(",\n");
            json.append("    \"mapy\": ").append(tour.getMapy() != null ? tour.getMapy() : "null").append(",\n");

            if (detailCommon != null) {
                json.append("    \"tel\": \"").append(escapeJson(detailCommon.getTel())).append("\",\n");
                json.append("    \"overview\": \"").append(escapeJson(truncate(detailCommon.getOverview(), 200))).append("\",\n");
            }

            // Operating hours from DetailInfo
            String operatingHours = extractOperatingHours(detailInfos);
            json.append("    \"operatingHours\": \"").append(escapeJson(operatingHours)).append("\"\n");

            json.append("  }");
            if (i < tours.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    private String getPlaceType(Integer contentTypeId) {
        if (contentTypeId == null) {
            return "ATTRACTION";
        }
        try {
            return PlaceType.fromContentTypeId(contentTypeId).name();
        } catch (IllegalArgumentException e) {
            return "ATTRACTION";
        }
    }

    private String extractOperatingHours(List<DetailInfo> detailInfos) {
        return detailInfos.stream()
                .filter(info -> info.getInfoname() != null &&
                        (info.getInfoname().contains("이용시간") ||
                         info.getInfoname().contains("영업시간") ||
                         info.getInfoname().contains("운영시간")))
                .map(DetailInfo::getInfotext)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("정보 없음");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String buildUserRequest(TourCourseGenerateRequestDto request) {
        return String.format(
                "인원: %d명, 기간: %s ~ %s, 이동수단: %s, 테마: %s",
                request.getPeopleCount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTransport().getDescription(),
                String.join(", ", request.getTheme())
        );
    }

    private void validateAiResponse(TourCourseAiResponseDto aiResponse, LocalDate startDate, LocalDate endDate) {
        if (aiResponse == null || aiResponse.getSchedule() == null || aiResponse.getSchedule().isEmpty()) {
            throw new IllegalArgumentException("AI 응답이 비어있습니다");
        }

        // Extract all contentIds
        Set<Long> contentIds = new HashSet<>();
        for (TourCourseAiResponseDto.DailyPlan day : aiResponse.getSchedule()) {
            if (day.getPlaces() != null) {
                for (TourCourseAiResponseDto.PlaceVisit place : day.getPlaces()) {
                    contentIds.add(place.getContentId());

                    // Validate date range
                    if (day.getDate().isBefore(startDate) || day.getDate().isAfter(endDate)) {
                        throw new IllegalArgumentException("일정 날짜가 요청 범위를 벗어났습니다: " + day.getDate());
                    }

                    // Validate type
                    try {
                        PlaceType.valueOf(place.getType());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("유효하지 않은 장소 타입입니다: " + place.getType());
                    }
                }
            }
        }

        // Validate contentIds exist in DB
        List<Tour> existingTours = tourRepository.findByContentidIn(new ArrayList<>(contentIds));
        if (existingTours.size() != contentIds.size()) {
            Set<Long> existingIds = existingTours.stream()
                    .map(Tour::getContentid)
                    .collect(Collectors.toSet());
            contentIds.removeAll(existingIds);
            throw new IllegalArgumentException("존재하지 않는 장소 ID가 포함되어 있습니다: " + contentIds);
        }

        log.info("AI response validation successful");
    }

    private TourCourseUserDefined saveTourCourse(TourCourseGenerateRequestDto request,
                                                  Long userId,
                                                  TourCourseAiResponseDto aiResponse) {
        try {
            // Convert theme list to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String themeJson = objectMapper.writeValueAsString(request.getTheme());

            // Save TourCourseUserDefined
            TourCourseUserDefined course = TourCourseUserDefined.builder()
                    .userId(userId)
                    .peopleCount(request.getPeopleCount())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .transport(request.getTransport().name())
                    .theme(themeJson)
                    .build();

            TourCourseUserDefined savedCourse = tourCourseUserDefinedRepository.save(course);

            // Save TourCourseUserDefinedDetail
            List<TourCourseUserDefinedDetail> details = new ArrayList<>();
            for (TourCourseAiResponseDto.DailyPlan day : aiResponse.getSchedule()) {
                if (day.getPlaces() != null) {
                    for (TourCourseAiResponseDto.PlaceVisit place : day.getPlaces()) {
                        TourCourseUserDefinedDetail detail = TourCourseUserDefinedDetail.builder()
                                .tourCourseId(savedCourse.getId())
                                .date(day.getDate())
                                .seq(place.getSeq())
                                .time(place.getTime())
                                .type(place.getType())
                                .contentId(place.getContentId())
                                .build();
                        details.add(detail);
                    }
                }
            }

            tourCourseUserDefinedDetailRepository.saveAll(details);

            log.info("Tour course saved successfully. ID: {}", savedCourse.getId());
            return savedCourse;

        } catch (Exception e) {
            log.error("Failed to save tour course: {}", e.getMessage());
            throw new RuntimeException("여행 코스 저장에 실패했습니다", e);
        }
    }

    private TourCourseGenerateResponseDto buildResponse(Long courseId, TourCourseAiResponseDto aiResponse) {
        List<TourCourseGenerateResponseDto.DailySchedule> schedules = aiResponse.getSchedule().stream()
                .map(day -> {
                    List<TourCourseGenerateResponseDto.PlaceInfo> places = day.getPlaces().stream()
                            .map(place -> TourCourseGenerateResponseDto.PlaceInfo.builder()
                                    .seq(place.getSeq())
                                    .time(place.getTime())
                                    .type(place.getType())
                                    .contentId(place.getContentId())
                                    .build())
                            .collect(Collectors.toList());

                    return TourCourseGenerateResponseDto.DailySchedule.builder()
                            .date(day.getDate())
                            .places(places)
                            .build();
                })
                .collect(Collectors.toList());

        return TourCourseGenerateResponseDto.builder()
                .courseId(courseId)
                .schedule(schedules)
                .build();
    }
}
