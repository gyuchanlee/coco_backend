package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.domain.Tour;
import com.eodegano.cocobackend.domain.User;
import com.eodegano.cocobackend.domain.UserPoiLike;
import com.eodegano.cocobackend.dto.PoiLikeResponseDto;
import com.eodegano.cocobackend.repository.TourRepository;
import com.eodegano.cocobackend.repository.UserPoiLikeRepository;
import com.eodegano.cocobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PoiLikeServiceImpl implements PoiLikeService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final UserPoiLikeRepository userPoiLikeRepository;

    @Override
    @Transactional
    public PoiLikeResponseDto toggleLike(Long contentId, String userEmail) {
        Tour tour = tourRepository.findById(contentId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 여행지입니다"));

        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다"));

        Optional<UserPoiLike> existing = userPoiLikeRepository.findByUserIdAndContentId(user.getId(), contentId);

        boolean liked;
        if (existing.isPresent()) {
            userPoiLikeRepository.delete(existing.get());
            tourRepository.decrementLikes(contentId);
            liked = false;
        } else {
            userPoiLikeRepository.save(UserPoiLike.of(user.getId(), contentId));
            tourRepository.incrementLikes(contentId);
            liked = true;
        }

        Tour updated = tourRepository.findById(contentId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 여행지입니다"));

        return new PoiLikeResponseDto(liked, updated.getLikesOrZero());
    }
}
