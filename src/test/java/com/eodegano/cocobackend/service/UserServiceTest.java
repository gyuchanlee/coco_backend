package com.eodegano.cocobackend.service;

import com.eodegano.cocobackend.domain.User;
import com.eodegano.cocobackend.dto.*;
import com.eodegano.cocobackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private User mockUser;
    private static final String EMAIL = "test@test.com";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email(EMAIL)
                .nickname("테스터")
                .password(ENCODED_PASSWORD)
                .role("USER")
                .build();
    }

    // ───────────────────────────────────────────────
    // 회원가입
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("회원가입 성공")
    void joinSuccess() {
        given(userRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).willReturn(false);
        given(userRepository.findByEmailAndDeletedAtIsNotNull(EMAIL)).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn(ENCODED_PASSWORD);
        given(userRepository.save(any(User.class))).willReturn(mockUser);

        UserJoinRequestDto request = mock(UserJoinRequestDto.class);
        given(request.getEmail()).willReturn(EMAIL);
        given(request.getNickname()).willReturn("테스터");
        given(request.getPassword()).willReturn("password123!");

        userService.join(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 사용 중인 이메일")
    void joinFailWithDuplicateEmail() {
        given(userRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).willReturn(true);

        UserJoinRequestDto request = mock(UserJoinRequestDto.class);
        given(request.getEmail()).willReturn(EMAIL);

        assertThatThrownBy(() -> userService.join(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("재가입 성공 - 탈퇴한 이력이 있는 이메일")
    void joinSuccessWithRejoin() {
        given(userRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).willReturn(false);
        given(userRepository.findByEmailAndDeletedAtIsNotNull(EMAIL)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.encode(anyString())).willReturn(ENCODED_PASSWORD);

        UserJoinRequestDto request = mock(UserJoinRequestDto.class);
        given(request.getEmail()).willReturn(EMAIL);
        given(request.getNickname()).willReturn("테스터");
        given(request.getPassword()).willReturn("password123!");

        userService.join(request);

        verify(userRepository, never()).save(any()); // 재가입은 save 안 함 (dirty checking)
    }

    // ───────────────────────────────────────────────
    // 비밀번호 변경
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccess() {
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("currentPassword", ENCODED_PASSWORD)).willReturn(true);
        given(passwordEncoder.matches("newPassword123!", ENCODED_PASSWORD)).willReturn(false);
        given(passwordEncoder.encode("newPassword123!")).willReturn("newEncodedPassword");

        UserUpdatePasswordRequestDto request = mock(UserUpdatePasswordRequestDto.class);
        given(request.getCurrentPassword()).willReturn("currentPassword");
        given(request.getNewPassword()).willReturn("newPassword123!");

        userService.updatePassword(1L, request);

        verify(passwordEncoder).encode("newPassword123!");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 소셜 로그인 유저")
    void updatePasswordFailForSocialUser() {
        User socialUser = User.builder()
                .email(EMAIL)
                .nickname("카카오유저")
                .password(null) // 소셜 로그인은 password null
                .role("USER")
                .build();

        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(socialUser));

        UserUpdatePasswordRequestDto request = mock(UserUpdatePasswordRequestDto.class);

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("소셜 로그인 유저는 비밀번호를 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePasswordFailWithWrongCurrentPassword() {
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).willReturn(false);

        UserUpdatePasswordRequestDto request = mock(UserUpdatePasswordRequestDto.class);
        given(request.getCurrentPassword()).willReturn("wrongPassword");

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 현재와 동일")
    void updatePasswordFailWithSamePassword() {
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("currentPassword", ENCODED_PASSWORD)).willReturn(true);
        given(passwordEncoder.matches("currentPassword", ENCODED_PASSWORD)).willReturn(true);

        UserUpdatePasswordRequestDto request = mock(UserUpdatePasswordRequestDto.class);
        given(request.getCurrentPassword()).willReturn("currentPassword");
        given(request.getNewPassword()).willReturn("currentPassword");

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    // ───────────────────────────────────────────────
    // 회원 탈퇴
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("회원 탈퇴 성공 - deletedAt 설정")
    void deleteUserSuccess() {
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(mockUser));

        userService.deleteUser(1L);

        verify(userRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 유저")
    void deleteUserFailWithNotFound() {
        given(userRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 유저입니다.");
    }
}
