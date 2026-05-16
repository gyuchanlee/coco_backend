package com.eodegano.cocobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenReissueRequestDto {

    @NotBlank
    private String refreshToken;
}
