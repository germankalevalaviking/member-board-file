package com.example.consult.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 공개 페이지
                        .requestMatchers("/", "/member/login", "/member/new", "/board/new",
                                "/css/**", "/board/list","/js/**", "/images/**"
                        ).permitAll()

                        // 관리자 전용
                        .requestMatchers("/admin/**").hasRole("ADMIN")


                        // 글 저장, 목록, 상세는 로그인 필요
                        .requestMatchers("/member/detail/**","/member/list", "/member/update").authenticated()
                        .requestMatchers("/board/**").authenticated()

                        // 나머지는 일단 공개
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/member/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/member/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
