package org.yaroslaavl.recruitingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.yaroslaavl.recruitingservice.config.converter.KeyCloakAuthenticationRoleConverter;

import java.util.Collection;

@Configuration
@EnableScheduling
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .jwtAuthenticationConverter(jwtToken -> {
                                    Collection<GrantedAuthority> authorities = new KeyCloakAuthenticationRoleConverter().convert(jwtToken);
                                    return new JwtAuthenticationToken(jwtToken, authorities);
                                })
                        )
                );

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers(
                                        "/error",
                                        "/api/v1/vacancies/search",
                                        "/api/v1/vacancies/*/company",
                                        "/api/v1/vacancies/count",
                                        "/actuator/health").permitAll()
                                .requestMatchers(
                                        "/api/v1/categories/filtered",
                                        "/api/v1/vacancies/create").hasRole("VERIFIED_RECRUITER")
                                .requestMatchers(
                                        "/api/v1/applications/apply").hasRole("VERIFIED_CANDIDATE")
                                .requestMatchers(
                                        "/api/v1/vacancies/create",
                                        "/api/v1/vacancies/*",
                                        "/api/v1/applications/search/*",
                                        "/api/v1/applications/*").hasRole("VERIFIED_RECRUITER")
                                .requestMatchers(
                                        "/api/v1/vacancies/*/info",
                                        "/api/v1/report-system/send",
                                        "/api/v1/applications/*/info").hasAnyRole("VERIFIED_RECRUITER", "VERIFIED_CANDIDATE")
                                .requestMatchers(
                                        "/api/v1/report-system/*").hasRole("MANAGER")
                );

        return http.build();
    }
}
