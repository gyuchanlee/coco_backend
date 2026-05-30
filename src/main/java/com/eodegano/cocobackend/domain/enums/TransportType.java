package com.eodegano.cocobackend.domain.enums;

public enum TransportType {
    CAR("자동차"),
    PUBLIC_TRANSPORT("대중교통"),
    WALK("도보");

    private final String description;

    TransportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
