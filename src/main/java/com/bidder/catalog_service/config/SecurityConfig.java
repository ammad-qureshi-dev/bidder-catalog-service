/* (C) 2026 
bidder.app */
package com.bidder.catalog_service.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		// ToDo: [SECURITY CONCERN] -- CSRF protection is disabled with no compensating
		// control (e.g. no auth/CSRF
		// token scheme in place). If any session/cookie-based auth is ever added in
		// front of this stateless config,
		// this reopens CSRF exposure.
		// ToDo: [SECURITY CONCERN] -- authorizeHttpRequests permits every request with
		// no authentication or
		// authorization check whatsoever. All endpoints (including mutating ones like
		// create/update auction and
		// update-highest-bid) are reachable by anyone with network access, with no
		// verification of caller identity.
		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(
						exceptions -> exceptions.authenticationEntryPoint((request, response, authException) -> {
							response.setContentType("application/json");
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						}).accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setContentType("application/json");
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						}));

		return http.build();
	}
}
