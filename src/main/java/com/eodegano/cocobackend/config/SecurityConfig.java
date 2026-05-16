package com.eodegano.cocobackend.config;

import com.eodegano.cocobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    // ───────────────────────────────────────────────
    // 1. Password Encoder
    // ───────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ───────────────────────────────────────────────
    // 2. UserDetailsService
    //    - email 기반 로그인
    //    - 탈퇴한 유저(deletedAt != null)는 인증 불가
    // ───────────────────────────────────────────────
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPassword() != null ? user.getPassword() : "")
                        .roles(user.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않거나 탈퇴한 유저입니다: " + email));
    }

    // ───────────────────────────────────────────────
    // 3. AuthenticationProvider
    //    - DaoAuthenticationProvider에 위 두 Bean 연결
    // ───────────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Spring Security 7: UserDetailsService는 setter 제거 → 생성자로 주입
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ───────────────────────────────────────────────
    // 4. SecurityFilterChain
    // ───────────────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // 데이터 마이그레이션 엔드포인트 - 개발 전용
                .requestMatchers("/api/admin/migration/**").permitAll()
                .requestMatchers("/api/v1/user/join").permitAll()
                .requestMatchers("/api/v1/user/{userId}").hasAnyRole("USER", "ADMIN")
                // 나머지는 추후 인증 설정
                .anyRequest().permitAll() // 개발용
//                .anyRequest().authenticated() // 운영용
            );
        return http.build();
    }
}
