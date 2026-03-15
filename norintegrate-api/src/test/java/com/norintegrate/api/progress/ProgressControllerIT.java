package com.norintegrate.api.progress;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ProgressControllerIT extends AbstractIntegrationTest {

  @Test
  void getProgress_withJwt_returnsOk() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/progress")
                .with(
                    jwt()
                        .jwt(
                            j ->
                                j.claim("sub", "test-user-progress-1")
                                    .claim("iss", "https://accounts.google.com")
                                    .claim("email", "progress1@test.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void getProgress_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/progress")).andExpect(status().isUnauthorized());
  }

  @Test
  void markComplete_withJwt_returnsUserProgress() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/progress/1/complete")
                .with(
                    jwt()
                        .jwt(
                            j ->
                                j.claim("sub", "test-user-progress-2")
                                    .claim("iss", "https://accounts.google.com")
                                    .claim("email", "progress2@test.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.procedureId").value(1))
        .andExpect(jsonPath("$.completed").value(true));
  }

  @Test
  void markComplete_withoutJwt_returns401() throws Exception {
    mockMvc.perform(post("/api/v1/progress/1/complete")).andExpect(status().isUnauthorized());
  }

  @Test
  void markIncomplete_withJwt_returnsNoContent() throws Exception {
    var jwtSpec =
        jwt()
            .jwt(
                j ->
                    j.claim("sub", "test-user-progress-3")
                        .claim("iss", "https://accounts.google.com")
                        .claim("email", "progress3@test.com"));

    mockMvc.perform(post("/api/v1/progress/1/complete").with(jwtSpec));

    mockMvc
        .perform(delete("/api/v1/progress/1/complete").with(jwtSpec))
        .andExpect(status().isNoContent());
  }

  @Test
  void markIncomplete_withoutJwt_returns401() throws Exception {
    mockMvc.perform(delete("/api/v1/progress/1/complete")).andExpect(status().isUnauthorized());
  }
}
