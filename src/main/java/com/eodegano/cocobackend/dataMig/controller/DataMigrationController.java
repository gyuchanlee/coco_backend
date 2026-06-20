package com.eodegano.cocobackend.dataMig.controller;

import com.eodegano.cocobackend.dataMig.service.DataMigrationService;
import com.eodegano.cocobackend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 데이터 마이그레이션 수동 트리거 컨트롤러
 * ⚠️ 개발/테스트 전용 - 운영 시 비활성화 또는 인증 추가 필요
 *
 * POST /api/admin/migration/run?count=300
 * POST /api/admin/migration/tour?count=300
 * POST /api/admin/migration/detail-common
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
public class DataMigrationController {

    private final DataMigrationService migrationService;

    /**
     * 전체 마이그레이션 실행
     * @param count 타입별 수집 목표 건수 (기본 300)
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runAll(
            @RequestParam(defaultValue = "300") int count) {

        log.info("전체 마이그레이션 시작 요청: targetCount={}", count);
        Map<String, Object> result = migrationService.runAll(count);
        return ResponseEntity.ok(ApiResponse.ok("전체 마이그레이션이 완료되었습니다.", result));
    }

    /**
     * tour 테이블만 수집
     */
    @PostMapping("/tour")
    public ResponseEntity<ApiResponse<Map<String, Object>>> migrateTour(
            @RequestParam(defaultValue = "300") int count) {

        int saved = migrationService.migrateTour(count);
        return ResponseEntity.ok(ApiResponse.ok("tour 마이그레이션이 완료되었습니다.", Map.of("tour", saved)));
    }

    /**
     * tour 테이블의 contentId 기준으로 detail_common만 수집
     */
    @PostMapping("/detail-common")
    public ResponseEntity<ApiResponse<Map<String, Object>>> migrateDetailCommon() {

        Map<Integer, List<Long>> contentIdsByType = migrationService.loadContentIdsByType();
        int saved = migrationService.migrateDetailCommon(contentIdsByType);
        return ResponseEntity.ok(ApiResponse.ok("detail_common 마이그레이션이 완료되었습니다.", Map.of("detail_common", saved)));
    }
}
