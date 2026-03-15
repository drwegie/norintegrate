package com.norintegrate.api.progress;

import com.norintegrate.common.progress.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

  private final ProgressService progressService;

  public AccountController(ProgressService progressService) {
    this.progressService = progressService;
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Jwt jwt) {
    var subject = jwt.getSubject();
    var issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
    var provider = JwtUtils.deriveProvider(issuer);
    var user = progressService.findUser(provider, subject);
    progressService.deleteAccount(user.getId());
    return ResponseEntity.noContent().build();
  }
}
