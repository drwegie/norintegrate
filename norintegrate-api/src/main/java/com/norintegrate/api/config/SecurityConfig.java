package com.norintegrate.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String issuerUri;

  @Bean
  @ConditionalOnMissingBean
  public JwtDecoder jwtDecoder() {
    var decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
    // Google id_tokens set 'aud' to the OAuth client ID, not a resource server identifier.
    // Use only the issuer and timestamp validators — skip the default audience check.
    var issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
    decoder.setJwtValidator(issuerValidator);
    return decoder;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> {})
        .csrf(csrf -> csrf.disable())
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
                    .requestMatchers(
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
    return http.build();
  }
}
