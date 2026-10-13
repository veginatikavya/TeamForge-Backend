package com.teamforge.config;

import com.teamforge.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .formLogin(form -> form.disable())

            .httpBasic(basic -> basic.disable())

            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    (request, response, exception) -> {
                        response.setStatus(
                            HttpStatus.UNAUTHORIZED.value()
                        );
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"message\":\"Authentication required.\"}"
                        );
                    }
                )
                .accessDeniedHandler(
                    (request, response, exception) -> {
                        response.setStatus(
                            HttpStatus.FORBIDDEN.value()
                        );
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"message\":\"Access denied.\"}"
                        );
                    }
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Allow CORS preflight requests
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // Public endpoints
                .requestMatchers(
                    "/api/auth/login",
                    "/api/health"
                ).permitAll()

                // Public registration disabled
                .requestMatchers(
                    "/api/auth/register"
                ).denyAll()

                // Candidate endpoints
                .requestMatchers(
                    "/api/candidate/**"
                ).hasAnyRole("ADMIN", "CANDIDATE")

                // Admin-only endpoints
                .requestMatchers(
                    "/api/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow localhost and deployed Vercel frontend
        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:5173",
                "https://team-forge-frontend-alpha.vercel.app"
            )
        );

        // Allow Vercel preview deployment URLs
        configuration.setAllowedOriginPatterns(
            List.of(
                "https://team-forge-frontend-*.vercel.app"
            )
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type",
                "Accept"
            )
        );

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/api/**",
            configuration
        );

        return source;
    }
}
