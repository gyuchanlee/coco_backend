# Changelog

모든 주요 변경 사항을 이 파일에 기록합니다.  
형식: `[버전] - 날짜 / 유형 / 내용`

---

## [0.2.6] - 2026-06-20

### feat

- **Tier 기반 확률적 POI 샘플링** (`TourCourseServiceImpl.selectByTypeQuota`)
  - Hard exclusion: `stars ≤ 1` POI 제거 (품질 하한 보장)
  - Tier A(`stars ≥ 4`) 70% 슬롯 / Tier B(`stars 2-3·null`) 30% 슬롯 분할
  - Tier A 부족분은 Tier B로 보충, 전체 부족분은 ATTRACTION으로 채움
  - Cold-start 보호: `stars = null` → Tier B 편입 (제외 없음)
  - `applyOrderStrategy()`: likes 데이터 있으면 Tier 내 DESC 정렬, 없으면 shuffle

- **POI 좋아요 토글 API** (`POST /api/v1/poi/{contentId}/like`)
  - `user_poi_like` 중계 테이블로 중복 좋아요 방지 (composite PK: user_id + content_id)
  - `tour.likes` 원자적 JPQL UPDATE(`@Modifying`) — 동시성 안전
  - 응답: `{ liked: boolean, likes: int }`
  - 신규 파일: `UserPoiLike`, `UserPoiLikeRepository`, `PoiLikeService`, `PoiLikeServiceImpl`, `PoiController`, `PoiLikeResponseDto`

- **코스 소유권 이전** (`PATCH /api/v1/tour-course/{courseId}/assign`)
  - 비로그인 생성 코스(userId=null)에 로그인 사용자 ID 귀속
  - 이미 소유자 있으면 403 반환

- **코스 목록 조회** (`GET /api/v1/tour-course`)
  - 로그인 사용자의 전체 코스 목록 반환 (`TourCourseListItemDto`)
  - 신규 파일: `TourCourseListItemDto`

- **코스 상세 조회** (`GET /api/v1/tour-course/{courseId}`)
  - 소유자 인증 후 코스 헤더 + 일정 상세 반환 (`TourCourseShareResponseDto`)
  - 신규 파일: `TourCourseShareResponseDto`

- **코스 삭제** (`DELETE /api/v1/tour-course/{courseId}`)
  - 소유자 인증 후 detail → course 순 삭제 (FK 위반 방지)

- **공개 코스 뷰** (`GET /api/v1/tour-course/{courseId}/view`)
  - 인증 불필요 (permitAll) — 카카오 공유 링크 수신자용 읽기 전용 뷰
  - BOQ11 확정: share_snapshot 테이블 없이 courseId 직접 공개 조회

- **Tour 엔티티 확장**: `stars INT`, `likes INT` 컬럼 추가, `getLikesOrZero()` 헬퍼

### fix

- `@Modifying(clearAutomatically = true)` 추가 (`TourRepository.incrementLikes`, `decrementLikes`)
  - 원자적 JPQL UPDATE 후 JPA L1 캐시 미갱신으로 stale likes 카운트 반환되던 문제 수정

### refactor

- `ObjectMapper` 필드 주입으로 변경 (`TourCourseServiceImpl`)
  - `new ObjectMapper()` 매 호출 생성 → `@RequiredArgsConstructor` 빈 주입 (Jackson 3.x `tools.jackson` 패키지)
  - `parseTheme`: raw `List.class` → `TypeReference<List<String>>` 타입 안전 역직렬화
  - `saveTourCourse`: 로컬 `new ObjectMapper()` 제거 후 주입 필드 사용

### docs

- BOQ11 확정 반영: 공유 스키마 결정 기록
- CO2, CO3, CO4, CO5, PO5, SH2 구현 완료 상태 업데이트
- CO6 단계 1 부분 구현 반영 (Tier 샘플링·likes 정렬)
- DA4 likes 수집 파이프라인 완료 상태 반영
- API 엔드포인트 현황 테이블 업데이트

---

## [0.2.5] - 2026-06-20

### feat

- **공통 API 응답 포맷 표준화** (`ApiResponse<T>` 래퍼 도입)
  - 전체 Controller·GlobalExceptionHandler·Security 핸들러(401/403) 통일

### fix

- GlobalExceptionHandler 내부 정보 노출 차단 (`ResponseStatusException`, `NoSuchElementException`)
- validation 오류 응답 개선 (필드명 노출 제거, getDefaultMessage()만 반환)
- `IllegalArgumentException` 응답값 변경

---

## [0.2.4] - 2026-06-06

### fix

- accessToken(body) + RefreshToken(HttpOnly) 응답 수정

---

## [0.2.3] - 2026-06-06

### feat

- 카카오 OAuth2 인증 기능 구현 (AccessToken을 통한 카카오 API 서버 유저 정보 등록)

---

## [0.2.2] - 2026-06-06

### feat/fix

- 여행코스 타이틀 추가 및 수정 API 구현

---

## [0.2.1] - 2026-06-06

### feat/fix

- 여행 코스 생성 기능 수정 및 테스트 완료
- 제안서, 기능분해도 작성
