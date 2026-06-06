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

    // 하루 식사 횟수: 2(점심+저녁) or 3(아침+점심+저녁)
    private static final int MEALS_PER_DAY = 2;
    private static final int MAX_TRIP_DAYS = 7;

    private static final int QUOTA_FOOD          = MEALS_PER_DAY * MAX_TRIP_DAYS; // 2*7=14 → 3*7=21
    private static final int QUOTA_ACCOMMODATION =  4;
    private static final int QUOTA_ATTRACTION    = 12;
    private static final int QUOTA_CULTURE       =  5;
    private static final int QUOTA_LEPORTS       =  3;
    private static final int QUOTA_SHOPPING      =  2;
    private static final int QUOTA_EVENT         =  2;
    // 총합: MEALS_PER_DAY=2 기준 40개, =3 기준 47개

    private final GroqApiClient groqApiClient;
    private final TourRepository tourRepository;
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

        List<Tour> allTours = (sigunguCode == null || sigunguCode.isBlank())
                ? tourRepository.findAll()
                : tourRepository.findByLDongSignguCd(sigunguCode);

        if (allTours.isEmpty()) {
            throw new IllegalArgumentException("해당 지역의 여행지 데이터가 없습니다");
        }

        List<Tour> selected = selectByTypeQuota(allTours);
        log.info("Selected {} places for AI (from {} total)", selected.size(), allTours.size());
        return buildPlacesJson(selected);
    }

    private List<Tour> selectByTypeQuota(List<Tour> allTours) {
        Map<String, Integer> quotaMap = new HashMap<>();
        quotaMap.put("FOOD",          QUOTA_FOOD);
        quotaMap.put("ACCOMMODATION", QUOTA_ACCOMMODATION);
        quotaMap.put("ATTRACTION",    QUOTA_ATTRACTION);
        quotaMap.put("CULTURE",       QUOTA_CULTURE);
        quotaMap.put("LEPORTS",       QUOTA_LEPORTS);
        quotaMap.put("SHOPPING",      QUOTA_SHOPPING);
        quotaMap.put("EVENT",         QUOTA_EVENT);

        Map<String, List<Tour>> byType = allTours.stream()
                .collect(Collectors.groupingBy(t -> getPlaceType(t.getContenttypeid())));

        List<Tour> selected = new ArrayList<>();
        int totalQuota = quotaMap.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : quotaMap.entrySet()) {
            List<Tour> pool = byType.getOrDefault(entry.getKey(), Collections.emptyList());
            Collections.shuffle(pool);
            selected.addAll(pool.subList(0, Math.min(entry.getValue(), pool.size())));
        }

        // 할당량 합계보다 적게 뽑혔을 경우 ATTRACTION으로 보충
        int deficit = totalQuota - selected.size();
        if (deficit > 0) {
            Set<Long> selectedIds = selected.stream()
                    .map(Tour::getContentid)
                    .collect(Collectors.toSet());
            List<Tour> attractionPool = byType.getOrDefault("ATTRACTION", Collections.emptyList())
                    .stream()
                    .filter(t -> !selectedIds.contains(t.getContentid()))
                    .collect(Collectors.toList());
            Collections.shuffle(attractionPool);
            selected.addAll(attractionPool.subList(0, Math.min(deficit, attractionPool.size())));
        }

        Collections.shuffle(selected);
        return selected;
    }

    private String buildPlacesJson(List<Tour> tours) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < tours.size(); i++) {
            Tour tour = tours.get(i);
            json.append("{\"id\":").append(tour.getContentid())
                .append(",\"t\":\"").append(getPlaceType(tour.getContenttypeid()))
                .append("\",\"n\":\"").append(escapeJson(tour.getTitle()))
                .append("\"}");
            if (i < tours.size() - 1) {
                json.append(",");
            }
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

        Set<Long> contentIds = new HashSet<>();
        for (TourCourseAiResponseDto.DailyPlan day : aiResponse.getSchedule()) {
            if (day.getPlaces() != null) {
                for (TourCourseAiResponseDto.PlaceVisit place : day.getPlaces()) {
                    contentIds.add(place.getContentId());

                    if (day.getDate().isBefore(startDate) || day.getDate().isAfter(endDate)) {
                        throw new IllegalArgumentException("일정 날짜가 요청 범위를 벗어났습니다: " + day.getDate());
                    }

                    try {
                        PlaceType.valueOf(place.getType());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("유효하지 않은 장소 타입입니다: " + place.getType());
                    }
                }
            }
        }

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
            ObjectMapper objectMapper = new ObjectMapper();
            String themeJson = objectMapper.writeValueAsString(request.getTheme());

            TourCourseUserDefined course = TourCourseUserDefined.builder()
                    .userId(userId)
                    .peopleCount(request.getPeopleCount())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .transport(request.getTransport().name())
                    .theme(themeJson)
                    .build();

            TourCourseUserDefined savedCourse = tourCourseUserDefinedRepository.save(course);

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
