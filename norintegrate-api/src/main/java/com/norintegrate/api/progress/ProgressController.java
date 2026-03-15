package com.norintegrate.api.progress;

import com.norintegrate.common.progress.ProgressService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/progress")
public class ProgressController {

  private final ProgressService progressService;

  public ProgressController(ProgressService progressService) {
    this.progressService = progressService;
  }

  @GetMapping
  public List<UserProgressResponse> getProgress(@AuthenticationPrincipal Jwt jwt) {
    var user = resolveUser(jwt);
    return progressService.getProgress(user.getId()).stream()
        .map(
            up ->
                new UserProgressResponse(
                    up.getProcedure().getId(),
                    up.getProcedure().getTitle(),
                    up.isCompleted(),
                    up.getCompletedAt()))
        .toList();
  }

  @PostMapping("/{procedureId}/complete")
  public UserProgressResponse markComplete(
      @PathVariable Long procedureId, @AuthenticationPrincipal Jwt jwt) {
    var user = resolveUser(jwt);
    var up = progressService.markComplete(user.getId(), procedureId);
    return new UserProgressResponse(
        up.getProcedure().getId(),
        up.getProcedure().getTitle(),
        up.isCompleted(),
        up.getCompletedAt());
  }

  @DeleteMapping("/{procedureId}/complete")
  public ResponseEntity<Void> markIncomplete(
      @PathVariable Long procedureId, @AuthenticationPrincipal Jwt jwt) {
    var user = resolveUser(jwt);
    progressService.markIncomplete(user.getId(), procedureId);
    return ResponseEntity.noContent().build();
  }

  private com.norintegrate.common.progress.AppUser resolveUser(Jwt jwt) {
    var subject = jwt.getSubject();
    var issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
    var provider = JwtUtils.deriveProvider(issuer);
    var email = jwt.getClaimAsString("email");
    return progressService.findOrCreateUser(provider, subject, email);
  }
}
