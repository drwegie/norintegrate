package com.norintegrate.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.GET, "/api/v1/checklist/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/procedures/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/visa-types/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/municipalities/**")
                    .permitAll()
                    .requestMatchers("/api/v1/admin/**")
                    .authenticated()
                    .requestMatchers("/api/v1/progress/**")
                    .authenticated()
                    .requestMatchers("/api/v1/account/**")
                    .authenticated()
                    .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
    return http.build();
  }
}
