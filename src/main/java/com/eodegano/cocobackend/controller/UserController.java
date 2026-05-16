package com.eodegano.cocobackend.controller;

import com.eodegano.cocobackend.dto.*;
import com.eodegano.cocobackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<UserJoinResponseDto> join(@RequestBody @Valid UserJoinRequestDto request) {
        return ResponseEntity.ok(userService.join(request));
    }

    // 회원정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponseDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 닉네임 수정
    @PatchMapping("/{userId}/nickname")
    public ResponseEntity<Void> updateNickname(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateNicknameRequestDto request) {
        userService.updateNickname(userId, request);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 수정
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdatePasswordRequestDto request) {
        userService.updatePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴 (소프트 삭제)
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
