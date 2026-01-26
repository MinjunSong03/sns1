package com.example.sns1;

import com.example.sns1.jwt.JwtAuthenticationFilter;
import com.example.sns1.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**", "/ws-stomp/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/signup", "/ws-stomp/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                            .requestMatchers("/user/login", "/user/signup", "/ws-stomp-web/**").permitAll()
                            .requestMatchers("/css/**", "/js/**", "/images/**", "/files/**").permitAll()
                            .anyRequest().authenticated())
            .csrf((csrf) -> csrf
                            .ignoringRequestMatchers("/ws-stomp-web/**"))
            .formLogin((formLogin) -> formLogin     
                            .loginPage("/user/login")
                            .loginProcessingUrl("/user/login")
                            .successHandler((request, response, authentication) -> {
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.setContentType("application/json;charset=UTF-8");
                                Map<String, Object> data = new HashMap<>();
                                data.put("status", "success");
                                data.put("message", "로그인 성공");
                                new ObjectMapper().writeValue(response.getWriter(), data);
                            })
                            .failureHandler((request, response, exception) -> {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                Map<String, Object> data = new HashMap<>();
                                data.put("status", "error");
                                data.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
                                new ObjectMapper().writeValue(response.getWriter(), data);
                            })
                            .permitAll()
                        )
            .logout((logout) -> logout
                            .logoutUrl("/user/logout")
                            .logoutSuccessUrl("/")
                            .invalidateHttpSession(true));
        return http.build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
}
