package com.norintegrate.api.procedure;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

class ProcedureAdminControllerIT extends AbstractIntegrationTest {

  @Test
  void createProcedure_withAuth_returnsCreated() throws Exception {
    var body =
        """
                {"title":"Test Procedure","description":"Test desc","authority":"Test Authority","estimatedDays":5}
                """;

    mockMvc
        .perform(
            post("/api/v1/admin/procedures")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Test Procedure"))
        .andExpect(jsonPath("$.authority").value("Test Authority"));
  }

  @Test
  void createProcedure_withoutAuth_returns401() throws Exception {
    var body =
        """
                {"title":"Test Procedure","description":"Test desc","authority":"Test","estimatedDays":5}
                """;

    mockMvc
        .perform(
            post("/api/v1/admin/procedures").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateProcedure_withAuth_returnsOk() throws Exception {
    var body =
        """
                {"title":"Updated Title","description":"Updated","authority":"Updated Auth","estimatedDays":10}
                """;

    mockMvc
        .perform(
            put("/api/v1/admin/procedures/1")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"));
  }

  @Test
  void updateProcedure_withoutAuth_returns401() throws Exception {
    var body =
        """
                {"title":"Updated Title","description":"Updated","authority":"Updated Auth","estimatedDays":10}
                """;

    mockMvc
        .perform(
            put("/api/v1/admin/procedures/1").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deleteProcedure_withAuth_returnsNoContent() throws Exception {
    var body =
        """
                {"title":"To Delete","description":null,"authority":null,"estimatedDays":null}
                """;

    var result =
        mockMvc
            .perform(
                post("/api/v1/admin/procedures")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andReturn();

    var id =
        new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("id").asLong();

    mockMvc
        .perform(delete("/api/v1/admin/procedures/" + id).with(jwt()))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteProcedure_withoutAuth_returns401() throws Exception {
    mockMvc.perform(delete("/api/v1/admin/procedures/1")).andExpect(status().isUnauthorized());
  }
}
