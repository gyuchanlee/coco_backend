package com.eodegano.cocobackend.dataMig.service;

import com.eodegano.cocobackend.domain.*;
import com.eodegano.cocobackend.repository.*;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TourAPI → MariaDB 데이터 마이그레이션 서비스
 *
 * 실행 순서 (FK 의존 순):
 * 1. tour (areaBasedList2)
 * 2. detail_common (detailCommon2)
 * 3. detailIntro2 계열 (attraction / culture / event / tour_course / leports / accommodation / shopping / food)
 * 4. detailInfo2 계열 (detail_info / tour_course_detail_info)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationService {

    // content type 상수
    private static final int TYPE_ATTRACTION    = 12;
    private static final int TYPE_CULTURE       = 14;
    private static final int TYPE_EVENT         = 15;
    private static final int TYPE_TOUR_COURSE   = 25;
    private static final int TYPE_LEPORTS       = 28;
    private static final int TYPE_ACCOMMODATION = 32;
    private static final int TYPE_SHOPPING      = 38;
    private static final int TYPE_FOOD          = 39;

    // detailInfo2 를 저장하는 타입 (숙박·여행코스 제외 → 별도 테이블)
    private static final Set<Integer> DETAIL_INFO_TYPES = Set.of(
            TYPE_ATTRACTION, TYPE_CULTURE, TYPE_EVENT,
            TYPE_LEPORTS, TYPE_SHOPPING, TYPE_FOOD
    );

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TourApiClient api;

    private final TourRepository             tourRepo;
    private final DetailCommonRepository     detailCommonRepo;
    private final AttractionRepository       attractionRepo;
    private final CultureRepository          cultureRepo;
    private final EventRepository            eventRepo;
    private final TourCourseRepository       tourCourseRepo;
    private final LeportsRepository          leportsRepo;
    private final AccommodationRepository    accommodationRepo;
    private final ShoppingRepository         shoppingRepo;
    private final FoodRepository             foodRepo;
    private final DetailInfoRepository       detailInfoRepo;
    private final TourCourseDetailInfoRepository tourCourseDetailInfoRepo;

    // ----------------------------------------------------------------
    // 전체 실행 (컨트롤러에서 호출)
    // ----------------------------------------------------------------

    public Map<String, Object> runAll(int targetCount) {
        Map<String, Object> result = new LinkedHashMap<>();

        log.info("=== 데이터 마이그레이션 시작 (타겟: 타입별 {}건) ===", targetCount);

        // 1단계: tour 테이블 (전체 타입, 타겟 총 건수)
        int tourSaved = migrateTour(targetCount);
        result.put("tour", tourSaved);

        // 수집된 contentId 목록 타입별로 분리
        Map<Integer, List<Long>> contentIdsByType = loadContentIdsByType();
        log.info("tour 테이블 contentId 로드: {}", contentIdsByType.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().size() + "건").toList());

        // 2단계: detail_common
        int commonSaved = migrateDetailCommon(contentIdsByType);
        result.put("detail_common", commonSaved);

        // 3단계: detailIntro2 계열
        result.put("attraction",    migrateAttraction(contentIdsByType.getOrDefault(TYPE_ATTRACTION, List.of())));
        result.put("culture",       migrateCulture(contentIdsByType.getOrDefault(TYPE_CULTURE, List.of())));
        result.put("event",         migrateEvent(contentIdsByType.getOrDefault(TYPE_EVENT, List.of())));
        result.put("tour_course",   migrateTourCourse(contentIdsByType.getOrDefault(TYPE_TOUR_COURSE, List.of())));
        result.put("leports",       migrateLeports(contentIdsByType.getOrDefault(TYPE_LEPORTS, List.of())));
        result.put("accommodation", migrateAccommodation(contentIdsByType.getOrDefault(TYPE_ACCOMMODATION, List.of())));
        result.put("shopping",      migrateShopping(contentIdsByType.getOrDefault(TYPE_SHOPPING, List.of())));
        result.put("food",          migrateFood(contentIdsByType.getOrDefault(TYPE_FOOD, List.of())));

        // 4단계: detailInfo2 계열
        result.put("detail_info",             migrateDetailInfo(contentIdsByType));
        result.put("tour_course_detail_info",  migrateTourCourseDetailInfo(contentIdsByType.getOrDefault(TYPE_TOUR_COURSE, List.of())));

        log.info("=== 데이터 마이그레이션 완료: {} ===", result);
        return result;
    }

    // ----------------------------------------------------------------
    // 1. tour (areaBasedList2)
    // ----------------------------------------------------------------

    @Transactional
    public int migrateTour(int targetCount) {
        int[] types = {TYPE_ATTRACTION, TYPE_CULTURE, TYPE_EVENT, TYPE_TOUR_COURSE,
                       TYPE_LEPORTS, TYPE_ACCOMMODATION, TYPE_SHOPPING, TYPE_FOOD};

        // 타입별로 골고루 수집 (타겟 / 8 건씩)
        int perType = Math.max(targetCount / types.length, 1);
        int saved = 0;

        for (int typeId : types) {
            List<JsonNode> items = api.areaBasedListAll(typeId, perType);
            for (JsonNode item : items) {
                try {
                    Tour tour = mapTour(item);
                    tourRepo.save(tour);
                    saved++;
                } catch (Exception e) {
                    log.warn("tour 저장 실패: contentid={}, err={}", api.text(item, "contentid"), e.getMessage());
                }
            }
            log.info("tour type={} 저장 {}건", typeId, items.size());
        }
        return saved;
    }

    private Tour mapTour(JsonNode n) {
        return Tour.builder()
                .contentid(Long.parseLong(api.text(n, "contentid")))
                .contenttypeid(api.integer(n, "contenttypeid"))
                .title(Optional.ofNullable(api.text(n, "title")).orElse("(제목없음)"))
                .addr1(api.text(n, "addr1"))
                .addr2(api.text(n, "addr2"))
                .zipcode(api.text(n, "zipcode"))
                .tel(api.text(n, "tel"))
                .firstimage(api.text(n, "firstimage"))
                .firstimage2(api.text(n, "firstimage2"))
                .cpyrhtDivCd(api.text(n, "cpyrhtDivCd"))
                .mapx(parseDecimal(api.text(n, "mapx")))
                .mapy(parseDecimal(api.text(n, "mapy")))
                .mlevel(api.integer(n, "mlevel"))
                .lDongRegnCd(api.text(n, "lDongRegnCd"))
                .lDongSignguCd(api.text(n, "lDongSignguCd"))
                .lclsSystm1(api.text(n, "lclsSystm1"))
                .lclsSystm2(api.text(n, "lclsSystm2"))
                .lclsSystm3(api.text(n, "lclsSystm3"))
                .createdtime(parseDateTime(api.text(n, "createdtime")))
                .modifiedtime(parseDateTime(api.text(n, "modifiedtime")))
                .syncedAt(LocalDateTime.now())
                .build();
    }

    // ----------------------------------------------------------------
    // tour DB에서 contentId 타입별로 로드
    // ----------------------------------------------------------------

    private Map<Integer, List<Long>> loadContentIdsByType() {
        Map<Integer, List<Long>> map = new HashMap<>();
        tourRepo.findAll().forEach(t -> {
            int type = t.getContenttypeid() != null ? t.getContenttypeid() : 0;
            map.computeIfAbsent(type, k -> new ArrayList<>()).add(t.getContentid());
        });
        return map;
    }

    // ----------------------------------------------------------------
    // 2. detail_common (detailCommon2)
    // ----------------------------------------------------------------

    @Transactional
    public int migrateDetailCommon(Map<Integer, List<Long>> contentIdsByType) {
        int saved = 0;
        List<Long> allIds = contentIdsByType.values().stream()
                .flatMap(Collection::stream).toList();

        for (Long contentId : allIds) {
            try {
                JsonNode response = api.detailCommon(contentId);
                JsonNode items = api.extractItems(response);
                if (items == null || !items.isArray() || items.isEmpty()) continue;

                JsonNode n = items.get(0);
                DetailCommon dc = mapDetailCommon(n);
                detailCommonRepo.save(dc);
                saved++;
            } catch (Exception e) {
                log.warn("detail_common 저장 실패: contentid={}, err={}", contentId, e.getMessage());
            }
        }
        log.info("detail_common 저장 완료: {}건", saved);
        return saved;
    }

    private DetailCommon mapDetailCommon(JsonNode n) {
        return DetailCommon.builder()
                .contentid(Long.parseLong(api.text(n, "contentid")))
                .contenttypeid(api.integer(n, "contenttypeid"))
                .title(api.text(n, "title"))
                .tel(api.text(n, "tel"))
                .telname(api.text(n, "telname"))
                .homepage(api.text(n, "homepage"))
                .overview(api.text(n, "overview"))
                .firstimage(api.text(n, "firstimage"))
                .firstimage2(api.text(n, "firstimage2"))
                .cpyrhtDivCd(api.text(n, "cpyrhtDivCd"))
                .addr1(api.text(n, "addr1"))
                .addr2(api.text(n, "addr2"))
                .zipcode(api.text(n, "zipcode"))
                .mapx(parseDecimal(api.text(n, "mapx")))
                .mapy(parseDecimal(api.text(n, "mapy")))
                .mlevel(api.integer(n, "mlevel"))
                .lDongRegnCd(api.text(n, "lDongRegnCd"))
                .lDongSignguCd(api.text(n, "lDongSignguCd"))
                .lclsSystm1(api.text(n, "lclsSystm1"))
                .lclsSystm2(api.text(n, "lclsSystm2"))
                .lclsSystm3(api.text(n, "lclsSystm3"))
                .createdtime(parseDateTime(api.text(n, "createdtime")))
                .modifiedtime(parseDateTime(api.text(n, "modifiedtime")))
                .syncedAt(LocalDateTime.now())
                .build();
    }

    // ----------------------------------------------------------------
    // 3. detailIntro2 계열
    // ----------------------------------------------------------------

    @Transactional
    public int migrateAttraction(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_ATTRACTION));
                if (n == null) continue;
                attractionRepo.save(Attraction.builder()
                        .contentid(id)
                        .accomcount(api.text(n, "accomcount"))
                        .chkbabycarriage(api.text(n, "chkbabycarriage"))
                        .chkcreditcard(api.text(n, "chkcreditcard"))
                        .chkpet(api.text(n, "chkpet"))
                        .expagerange(api.text(n, "expagerange"))
                        .expguide(api.text(n, "expguide"))
                        .heritage1(api.bool(n, "heritage1"))
                        .heritage2(api.bool(n, "heritage2"))
                        .heritage3(api.bool(n, "heritage3"))
                        .infocenter(api.text(n, "infocenter"))
                        .opendate(api.text(n, "opendate"))
                        .parking(api.text(n, "parking"))
                        .restdate(api.text(n, "restdate"))
                        .useseason(api.text(n, "useseason"))
                        .usetime(api.text(n, "usetime"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("attraction 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("attraction 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateCulture(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_CULTURE));
                if (n == null) continue;
                cultureRepo.save(Culture.builder()
                        .contentid(id)
                        .accomcountculture(api.text(n, "accomcountculture"))
                        .chkbabycarriageculture(api.text(n, "chkbabycarriageculture"))
                        .chkcreditcardculture(api.text(n, "chkcreditcardculture"))
                        .chkpetculture(api.text(n, "chkpetculture"))
                        .discountinfo(api.text(n, "discountinfo"))
                        .infocenterculture(api.text(n, "infocenterculture"))
                        .parkingculture(api.text(n, "parkingculture"))
                        .parkingfee(api.text(n, "parkingfee"))
                        .restdateculture(api.text(n, "restdateculture"))
                        .usefee(api.text(n, "usefee"))
                        .usetimeculture(api.text(n, "usetimeculture"))
                        .scale(api.text(n, "scale"))
                        .spendtime(api.text(n, "spendtime"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("culture 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("culture 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateEvent(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_EVENT));
                if (n == null) continue;
                eventRepo.save(Event.builder()
                        .contentid(id)
                        .agelimit(api.text(n, "agelimit"))
                        .bookingplace(api.text(n, "bookingplace"))
                        .discountinfofestival(api.text(n, "discountinfofestival"))
                        .eventenddate(parseDate(api.text(n, "eventenddate")))
                        .eventhomepage(api.text(n, "eventhomepage"))
                        .eventplace(api.text(n, "eventplace"))
                        .eventstartdate(parseDate(api.text(n, "eventstartdate")))
                        .festivalgrade(api.text(n, "festivalgrade"))
                        .placeinfo(api.text(n, "placeinfo"))
                        .playtime(api.text(n, "playtime"))
                        .program(api.text(n, "program"))
                        .spendtimefestival(api.text(n, "spendtimefestival"))
                        .sponsor1(api.text(n, "sponsor1"))
                        .sponsor1tel(api.text(n, "sponsor1tel"))
                        .sponsor2(api.text(n, "sponsor2"))
                        .sponsor2tel(api.text(n, "sponsor2tel"))
                        .subevent(api.text(n, "subevent"))
                        .usetimefestival(api.text(n, "usetimefestival"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("event 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("event 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateTourCourse(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_TOUR_COURSE));
                if (n == null) continue;
                tourCourseRepo.save(TourCourse.builder()
                        .contentid(id)
                        .distance(api.text(n, "distance"))
                        .infocentertourcourse(api.text(n, "infocentertourcourse"))
                        .schedule(api.text(n, "schedule"))
                        .taketime(api.text(n, "taketime"))
                        .theme(api.text(n, "theme"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("tour_course 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("tour_course 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateLeports(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_LEPORTS));
                if (n == null) continue;
                leportsRepo.save(Leports.builder()
                        .contentid(id)
                        .accomcountleports(api.text(n, "accomcountleports"))
                        .chkbabycarriageleports(api.text(n, "chkbabycarriageleports"))
                        .chkcreditcardleports(api.text(n, "chkcreditcardleports"))
                        .chkpetleports(api.text(n, "chkpetleports"))
                        .expagerangeleports(api.text(n, "expagerangeleports"))
                        .infocenterleports(api.text(n, "infocenterleports"))
                        .openperiod(api.text(n, "openperiod"))
                        .parkingfeeleports(api.text(n, "parkingfeeleports"))
                        .parkingleports(api.text(n, "parkingleports"))
                        .reservation(api.text(n, "reservation"))
                        .restdateleports(api.text(n, "restdateleports"))
                        .scaleleports(api.text(n, "scaleleports"))
                        .usefeeleports(api.text(n, "usefeeleports"))
                        .usetimeleports(api.text(n, "usetimeleports"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("leports 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("leports 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateAccommodation(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_ACCOMMODATION));
                if (n == null) continue;
                accommodationRepo.save(Accommodation.builder()
                        .contentid(id)
                        .accomcountlodging(api.text(n, "accomcountlodging"))
                        .checkintime(api.text(n, "checkintime"))
                        .checkouttime(api.text(n, "checkouttime"))
                        .chkcooking(api.text(n, "chkcooking"))
                        .foodplace(api.text(n, "foodplace"))
                        .infocenterlodging(api.text(n, "infocenterlodging"))
                        .parkinglodging(api.text(n, "parkinglodging"))
                        .pickup(api.text(n, "pickup"))
                        .roomcount(api.integer(n, "roomcount"))
                        .reservationlodging(api.text(n, "reservationlodging"))
                        .reservationurl(api.text(n, "reservationurl"))
                        .roomtype(api.text(n, "roomtype"))
                        .scalelodging(api.text(n, "scalelodging"))
                        .subfacility(api.text(n, "subfacility"))
                        .barbecue(api.text(n, "barbecue"))
                        .beauty(api.text(n, "beauty"))
                        .beverage(api.text(n, "beverage"))
                        .bicycle(api.text(n, "bicycle"))
                        .campfire(api.text(n, "campfire"))
                        .fitness(api.text(n, "fitness"))
                        .karaoke(api.text(n, "karaoke"))
                        .publicbath(api.text(n, "publicbath"))
                        .publicpc(api.text(n, "publicpc"))
                        .sauna(api.text(n, "sauna"))
                        .seminar(api.text(n, "seminar"))
                        .sports(api.text(n, "sports"))
                        .refundregulation(api.text(n, "refundregulation"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("accommodation 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("accommodation 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateShopping(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_SHOPPING));
                if (n == null) continue;
                shoppingRepo.save(Shopping.builder()
                        .contentid(id)
                        .chkbabycarriageshopping(api.text(n, "chkbabycarriageshopping"))
                        .chkcreditcardshopping(api.text(n, "chkcreditcardshopping"))
                        .chkpetshopping(api.text(n, "chkpetshopping"))
                        .culturecenter(api.text(n, "culturecenter"))
                        .fairday(api.text(n, "fairday"))
                        .infocentershopping(api.text(n, "infocentershopping"))
                        .opendateshopping(api.text(n, "opendateshopping"))
                        .opentime(api.text(n, "opentime"))
                        .parkingshopping(api.text(n, "parkingshopping"))
                        .restdateshopping(api.text(n, "restdateshopping"))
                        .restroom(api.text(n, "restroom"))
                        .saleitem(api.text(n, "saleitem"))
                        .saleitemcost(api.text(n, "saleitemcost"))
                        .scaleshopping(api.text(n, "scaleshopping"))
                        .shopguide(api.text(n, "shopguide"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("shopping 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("shopping 저장 완료: {}건", saved);
        return saved;
    }

    @Transactional
    public int migrateFood(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode n = firstItem(api.detailIntro(id, TYPE_FOOD));
                if (n == null) continue;
                foodRepo.save(Food.builder()
                        .contentid(id)
                        .chkcreditcardfood(api.text(n, "chkcreditcardfood"))
                        .discountinfofood(api.text(n, "discountinfofood"))
                        .firstmenu(api.text(n, "firstmenu"))
                        .infocenterfood(api.text(n, "infocenterfood"))
                        .kidsfacility(api.text(n, "kidsfacility"))
                        .opendatefood(api.text(n, "opendatefood"))
                        .opentimefood(api.text(n, "opentimefood"))
                        .packing(api.text(n, "packing"))
                        .parkingfood(api.text(n, "parkingfood"))
                        .reservationfood(api.text(n, "reservationfood"))
                        .restdatefood(api.text(n, "restdatefood"))
                        .scalefood(api.text(n, "scalefood"))
                        .seat(api.integer(n, "seat"))
                        .smoking(api.text(n, "smoking"))
                        .treatmenu(api.text(n, "treatmenu"))
                        .lcnsno(api.text(n, "lcnsno"))
                        .syncedAt(LocalDateTime.now())
                        .build());
                saved++;
            } catch (Exception e) {
                log.warn("food 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("food 저장 완료: {}건", saved);
        return saved;
    }

    // ----------------------------------------------------------------
    // 4. detailInfo2 - 일반 타입
    // ----------------------------------------------------------------

    @Transactional
    public int migrateDetailInfo(Map<Integer, List<Long>> contentIdsByType) {
        int saved = 0;
        for (int typeId : DETAIL_INFO_TYPES) {
            List<Long> ids = contentIdsByType.getOrDefault(typeId, List.of());
            for (Long id : ids) {
                try {
                    JsonNode items = api.extractItems(api.detailInfo(id, typeId));
                    if (items == null || !items.isArray()) continue;
                    for (JsonNode n : items) {
                        detailInfoRepo.save(DetailInfo.builder()
                                .contentid(id)
                                .contenttypeid(typeId)
                                .fldgubun(api.text(n, "fldgubun"))
                                .serialnum(api.integer(n, "serialnum"))
                                .infoname(api.text(n, "infoname"))
                                .infotext(api.text(n, "infotext"))
                                .syncedAt(LocalDateTime.now())
                                .build());
                        saved++;
                    }
                } catch (Exception e) {
                    log.warn("detail_info 저장 실패: id={}, type={}, err={}", id, typeId, e.getMessage());
                }
            }
        }
        log.info("detail_info 저장 완료: {}건", saved);
        return saved;
    }

    // ----------------------------------------------------------------
    // 4. detailInfo2 - 여행코스 경유지
    // ----------------------------------------------------------------

    @Transactional
    public int migrateTourCourseDetailInfo(List<Long> ids) {
        int saved = 0;
        for (Long id : ids) {
            try {
                JsonNode items = api.extractItems(api.detailInfo(id, TYPE_TOUR_COURSE));
                if (items == null || !items.isArray()) continue;
                for (JsonNode n : items) {
                    String subContentIdStr = api.text(n, "subcontentid");
                    tourCourseDetailInfoRepo.save(TourCourseDetailInfo.builder()
                            .contentid(id)
                            .subcontentid(subContentIdStr != null ? Long.parseLong(subContentIdStr) : null)
                            .subname(api.text(n, "subname"))
                            .subnum(api.integer(n, "subnum"))
                            .subdetailoverview(api.text(n, "subdetailoverview"))
                            .subdetailimg(api.text(n, "subdetailimg"))
                            .subdetailalt(api.text(n, "subdetailalt"))
                            .syncedAt(LocalDateTime.now())
                            .build());
                    saved++;
                }
            } catch (Exception e) {
                log.warn("tour_course_detail_info 저장 실패: id={}, err={}", id, e.getMessage());
            }
        }
        log.info("tour_course_detail_info 저장 완료: {}건", saved);
        return saved;
    }

    // ----------------------------------------------------------------
    // 유틸
    // ----------------------------------------------------------------

    private JsonNode firstItem(JsonNode response) {
        JsonNode items = api.extractItems(response);
        if (items == null || !items.isArray() || items.isEmpty()) return null;
        return items.get(0);
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s); }
        catch (NumberFormatException e) { return null; }
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank() || s.length() < 14) return null;
        try { return LocalDateTime.parse(s.substring(0, 14), DT_FMT); }
        catch (Exception e) { return null; }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank() || s.length() < 8) return null;
        try { return LocalDate.parse(s.substring(0, 8), D_FMT); }
        catch (Exception e) { return null; }
    }
}
