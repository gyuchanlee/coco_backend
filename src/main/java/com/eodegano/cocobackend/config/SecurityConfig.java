package com.eodegano.cocobackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    // ───────────────────────────────────────────────
    // 1. Password Encoder
    // ───────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ───────────────────────────────────────────────
    // 2. AuthenticationProvider
    //    - DaoAuthenticationProvider에 UserDetailsService, PasswordEncoder 연결
    // ───────────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Spring Security 7: UserDetailsService는 setter 제거 → 생성자로 주입
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ───────────────────────────────────────────────
    // 3. SecurityFilterChain
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
