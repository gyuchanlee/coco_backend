package com.eodegano.cocobackend.controller;

import com.eodegano.cocobackend.dto.LoginRequestDto;
import com.eodegano.cocobackend.dto.LoginResponseDto;
import com.eodegano.cocobackend.dto.TokenReissueRequestDto;
import com.eodegano.cocobackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        // "Bearer " 제거 후 토큰 추출
        String refreshToken = bearerToken.substring(7);
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    // AccessToken 재발급
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponseDto> reissue(@RequestBody @Valid TokenReissueRequestDto request) {
        return ResponseEntity.ok(authService.reissue(request));
    }
}
