package com.norintegrate.mcp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Security configuration for the MCP server.
 *
 * All three MCP tools (`getIntegrationGuide`, `getProcedureDetail`, `searchMunicipality`) are
 * read-only against public reference data (procedures, visa types, SSB Klass municipalities). No
 * tool path leads to `app_user` or `user_progress` tables, and all calls are idempotent.
 * Application-level authentication is intentionally omitted; access control is enforced at the
 * infrastructure layer (private subnet / API gateway). See ADR-017 for the full threat model and
 * revisit trigger.
 */
@Configuration
class McpSecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
        return http.build()
    }
}
