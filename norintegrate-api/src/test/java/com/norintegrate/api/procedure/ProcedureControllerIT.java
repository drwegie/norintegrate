package com.norintegrate.api.procedure;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ProcedureControllerIT extends AbstractIntegrationTest {

  @Test
  void getAllProcedures_returnsAll() throws Exception {
    mockMvc
        .perform(get("/api/v1/procedures"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(17));
  }

  @Test
  void getProcedureById_hit_returnsOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/procedures/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Receive job offer from Norwegian employer"));
  }

  @Test
  void getProcedureById_miss_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/procedures/9999")).andExpect(status().isNotFound());
  }

  @Test
  void getDocuments_returnsDocumentRequirements() throws Exception {
    mockMvc
        .perform(get("/api/v1/procedures/2/documents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3));
  }

  @Test
  void getDocuments_missingProcedure_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/procedures/9999/documents")).andExpect(status().isNotFound());
  }
}
