package com.eodegano.cocobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoOAuthCallbackRequestDto {

    @NotBlank(message = "카카오 AccessToken은 필수입니다.")
    private String kakaoAccessToken;
}
