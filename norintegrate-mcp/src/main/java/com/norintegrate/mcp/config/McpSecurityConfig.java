package com.norintegrate.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the MCP server.
 *
 * <p>All three MCP tools ({@code getIntegrationGuide}, {@code getProcedureDetail}, {@code
 * searchMunicipality}) are read-only against public reference data (procedures, visa types, SSB
 * Klass municipalities). No tool path leads to {@code app_user} or {@code user_progress} tables,
 * and all calls are idempotent. Application-level authentication is intentionally omitted; access
 * control is enforced at the infrastructure layer (private subnet / API gateway). See ADR-017 for
 * the full threat model and revisit trigger.
 */
@Configuration
public class McpSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }
}
