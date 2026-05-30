package com.eodegano.cocobackend.domain.enums;

public enum PlaceType {
    ATTRACTION(12, "관광지"),
    CULTURE(14, "문화시설"),
    EVENT(15, "축제/공연/행사"),
    LEPORTS(28, "레포츠"),
    ACCOMMODATION(32, "숙박"),
    SHOPPING(38, "쇼핑"),
    FOOD(39, "음식점");

    private final int contentTypeId;
    private final String description;

    PlaceType(int contentTypeId, String description) {
        this.contentTypeId = contentTypeId;
        this.description = description;
    }

    public int getContentTypeId() {
        return contentTypeId;
    }

    public String getDescription() {
        return description;
    }

    public static PlaceType fromContentTypeId(int id) {
        for (PlaceType type : values()) {
            if (type.contentTypeId == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid contentTypeId: " + id);
    }
}
