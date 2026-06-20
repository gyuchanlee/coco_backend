package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.dto.PoiLikeResponseDto;

public interface PoiLikeService {
    PoiLikeResponseDto toggleLike(Long contentId, String userEmail);
}
