package com.eodegano.cocobackend.dataMig.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 한국관광공사 TourAPI v2 호출 클라이언트
 * Base URL: https://apis.data.go.kr/B551011/KorService2
 *
 * Spring Boot 4.0 / Spring 7.0 기준
 * - RestClient (RestTemplate 대체)
 * - UriComponentsBuilder.fromUriString() (fromHttpUrl deprecated)
 * - tools.jackson.databind.JsonNode (Jackson 3.x 패키지 변경)
 */
@Slf4j
@Component
public class TourApiClient {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2";
    private static final String GYEONGBUK_AREA_CODE = "35";

    @Value("${tourapi.service-key}")
    private String serviceKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * areaBasedList2 - 지역 기반 관광지 목록 조회
     */
    public JsonNode areaBasedList(Integer contentTypeId, int pageNo, int numOfRows) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(BASE_URL + "/areaBasedList2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CocoTravel")
                .queryParam("_type", "json")
                .queryParam("areaCode", GYEONGBUK_AREA_CODE)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows);

        if (contentTypeId != null) {
            builder.queryParam("contentTypeId", contentTypeId);
        }

        return callApi(builder.build(true).toUri());
    }

    /**
     * detailCommon2 - 공통 정보 조회
     */
    public JsonNode detailCommon(Long contentId) {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/detailCommon2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CocoTravel")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .build(true).toUri();

        return callApi(uri);
    }

    /**
     * detailIntro2 - 소개 정보 조회
     */
    public JsonNode detailIntro(Long contentId, int contentTypeId) {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/detailIntro2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CocoTravel")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(true).toUri();

        return callApi(uri);
    }

    /**
     * detailInfo2 - 반복 정보 조회
     */
    public JsonNode detailInfo(Long contentId, int contentTypeId) {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/detailInfo2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CocoTravel")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(true).toUri();

        return callApi(uri);
    }

    /**
     * areaBasedList2 전체 페이지 수집 (최대 targetCount건)
     */
    public List<JsonNode> areaBasedListAll(Integer contentTypeId, int targetCount) {
        List<JsonNode> results = new ArrayList<>();
        int pageNo = 1;
        int numOfRows = Math.min(targetCount, 100);

        while (results.size() < targetCount) {
            JsonNode response = areaBasedList(contentTypeId, pageNo, numOfRows);
            JsonNode items = extractItems(response);

            if (items == null || !items.isArray() || items.isEmpty()) {
                log.info("더 이상 데이터 없음 (contentTypeId={}, page={})", contentTypeId, pageNo);
                break;
            }

            for (JsonNode item : items) {
                results.add(item);
                if (results.size() >= targetCount) break;
            }

            int totalCount = getTotalCount(response);
            if (results.size() >= totalCount) break;

            pageNo++;
        }

        log.info("areaBasedList 수집 완료: contentTypeId={}, {}건", contentTypeId, results.size());
        return results;
    }

    /** 응답에서 item 배열 추출 */
    public JsonNode extractItems(JsonNode response) {
        try {
            JsonNode items = response
                    .path("response").path("body")
                    .path("items").path("item");
            if (items.isMissingNode() || items.isNull()) return null;
            return items;
        } catch (Exception e) {
            log.warn("items 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /** 응답에서 totalCount 추출 */
    public int getTotalCount(JsonNode response) {
        try {
            JsonNode node = response.path("response").path("body").path("totalCount");
            if (node.isMissingNode() || node.isNull()) return 0;
            return node.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private JsonNode callApi(URI uri) {
        try {
            String body = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                log.error("API 응답 비어있음: uri={}", uri);
                return objectMapper.createObjectNode();
            }

            return objectMapper.readTree(body);

        } catch (Exception e) {
            log.error("API 호출 오류: uri={}, error={}", uri, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    /** null/빈 문자열 안전 추출 */
    public String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isNull()) return null;
        String s = v.stringValue();
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    public Integer integer(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        // 숫자 노드면 바로 intValue, 문자열 노드면 파싱
        if (v.isNumber()) return v.intValue();
        String s = v.stringValue();
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    public Boolean bool(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        if (v.isBoolean()) return v.booleanValue();
        String s = v.stringValue();
        if (s == null) return null;
        s = s.trim();
        return "1".equals(s) || "Y".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
    }
}
