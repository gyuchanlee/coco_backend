package com.eodegano.cocobackend.dto;

import com.eodegano.cocobackend.domain.User;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserInfoResponseDto {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String address;
    private final LocalDate birthDate;
    private final Byte gender;
    private final String travelType;

    public UserInfoResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.address = user.getAddress();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        this.travelType = user.getTravelType();
    }
}
