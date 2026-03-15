package com.norintegrate.api.progress;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class AccountControllerIT extends AbstractIntegrationTest {

  @Test
  void deleteAccount_withJwt_returnsNoContent() throws Exception {
    var jwtSpec =
        jwt()
            .jwt(
                j ->
                    j.claim("sub", "test-user-account-1")
                        .claim("iss", "https://accounts.google.com")
                        .claim("email", "account1@test.com"));

    // Create the user first by marking progress
    mockMvc.perform(post("/api/v1/progress/1/complete").with(jwtSpec));

    mockMvc.perform(delete("/api/v1/account").with(jwtSpec)).andExpect(status().isNoContent());
  }

  @Test
  void deleteAccount_withoutJwt_returns401() throws Exception {
    mockMvc.perform(delete("/api/v1/account")).andExpect(status().isUnauthorized());
  }
}
