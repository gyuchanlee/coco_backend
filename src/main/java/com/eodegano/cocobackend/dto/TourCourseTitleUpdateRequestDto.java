package com.eodegano.cocobackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TourCourseTitleUpdateRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자 이내여야 합니다")
    private String title;
}
