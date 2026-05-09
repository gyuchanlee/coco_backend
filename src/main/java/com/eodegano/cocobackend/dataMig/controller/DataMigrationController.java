package com.eodegano.cocobackend.dataMig.controller;

import com.eodegano.cocobackend.dataMig.service.DataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> runAll(
            @RequestParam(defaultValue = "300") int count) {

        log.info("전체 마이그레이션 시작 요청: targetCount={}", count);
        Map<String, Object> result = migrationService.runAll(count);
        return ResponseEntity.ok(result);
    }

    /**
     * tour 테이블만 수집
     */
    @PostMapping("/tour")
    public ResponseEntity<Map<String, Object>> migrateTour(
            @RequestParam(defaultValue = "300") int count) {

        int saved = migrationService.migrateTour(count);
        return ResponseEntity.ok(Map.of("tour", saved));
    }
}
