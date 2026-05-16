package com.eodegano.cocobackend.dto;

import com.eodegano.cocobackend.domain.User;
import lombok.Getter;

@Getter
public class UserJoinResponseDto {

    private final Long id;
    private final String email;
    private final String nickname;

    public UserJoinResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
    }
}
