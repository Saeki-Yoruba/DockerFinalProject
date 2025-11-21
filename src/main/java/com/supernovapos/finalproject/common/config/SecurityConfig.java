package com.supernovapos.finalproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	private static final String[] PUBLIC_ENDPOINTS = {
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/api/auth/**",
			"/api/oauth/**",
			"/api/users/**",
			"/api/store/**",
			"/api/reservations/**",
			"/api/payment/**",
			"/api/points/**",
			"/api/products/**",
			"/api/qr/**",
			"/api/menu/**",
			"/api/temp-user/**",
			"/api/customer/order/**",
			"/api/analytics/**",
			"/api/admin/roles/**",
			"/api/admin/products/**",
            "/api/admin/categories/**",
			"/api/businessHours/**",
			"/api/storeholidays/**",
			"/api/restaurantTable/**",
			"/api/discord/**",
			"/api/email/**",
			"/ws/**"
			
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						// 未登入：回傳 401
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType("application/json;charset=UTF-8");
							response.getWriter().write("""
									{
									  "status":401,
									  "error":"未登入",
									  "message":"請先登入後再操作"
									}
									""");
						})
						// 權限不足：回傳 403
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setStatus(HttpStatus.FORBIDDEN.value());
							response.setContentType("application/json;charset=UTF-8");
							response.getWriter().write("""
									{
									  "status":403,
									  "error":"權限不足",
									  "message":"您沒有權限執行此操作"
									}
									""");
						}));

		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
