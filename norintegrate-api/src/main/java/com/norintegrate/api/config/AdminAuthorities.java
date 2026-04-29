package com.norintegrate.api.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthorities implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
  private final Set<String> adminEmails;

  public AdminAuthorities(@Value("${norintegrate.security.admin-emails:}") String adminEmailsCsv) {
    this.adminEmails =
        Arrays.stream(adminEmailsCsv.split(","))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var token = delegate.convert(jwt);
    var authorities = new java.util.ArrayList<>(token.getAuthorities());

    var email = jwt.getClaimAsString("email");
    var emailVerified = jwt.getClaimAsBoolean("email_verified");

    if (email != null
        && emailVerified != Boolean.FALSE
        && adminEmails.contains(email.toLowerCase())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    return new org.springframework.security.oauth2.server.resource.authentication
        .JwtAuthenticationToken(jwt, authorities);
  }
}
