package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.dto.*;

public interface UserService {

    UserJoinResponseDto join(UserJoinRequestDto request);

    UserInfoResponseDto getUser(Long userId);

    void updateNickname(Long userId, UserUpdateNicknameRequestDto request);

    void updatePassword(Long userId, UserUpdatePasswordRequestDto request);

    void deleteUser(Long userId);
}
