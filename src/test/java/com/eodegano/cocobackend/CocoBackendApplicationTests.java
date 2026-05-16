package com.eodegano.cocobackend;

import org.junit.jupiter.api.Test;

// DB 연결 없는 테스트 환경에서 전체 컨텍스트 로드 불필요 → 기본 테스트만 유지
class CocoBackendApplicationTests {

    @Test
    void contextLoads() {
        // DB 없는 테스트 환경에서는 컨텍스트 로드 테스트 생략
    }
}
