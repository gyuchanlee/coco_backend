package com.eodegano.cocobackend.config;

import com.eodegano.cocobackend.security.JwtAccessDeniedHandler;
import com.eodegano.cocobackend.security.JwtAuthenticationEntryPoint;
import com.eodegano.cocobackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // ───────────────────────────────────────────────
    // 1. PasswordEncoder
    // ───────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ───────────────────────────────────────────────
    // 2. AuthenticationProvider
    // ───────────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ───────────────────────────────────────────────
    // 3. AuthenticationManager
    //    - AuthService에서 직접 인증 처리 시 필요
    // ───────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
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

            // JWT → Stateless 세션 사용 안 함
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 인증/인가 실패 핸들러 등록
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401
                .accessDeniedHandler(jwtAccessDeniedHandler)             // 403
            )

            .authenticationProvider(authenticationProvider())

            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/migration/**").permitAll()  // 개발 전용
                .requestMatchers("/api/v1/auth/**").permitAll()          // 로그인, 재발급
                .requestMatchers("/api/v1/user/join").permitAll()        // 회원가입
                .requestMatchers("/api/v1/tour-course/**").permitAll()   // 여행 코스 생성 (비로그인 허용)
                .requestMatchers("/api/v1/user/{userId}").hasAnyRole("USER", "ADMIN")
                .anyRequest().permitAll()                                // 개발용
//              .anyRequest().authenticated()                            // 운영용
            );

        return http.build();
    }
}
