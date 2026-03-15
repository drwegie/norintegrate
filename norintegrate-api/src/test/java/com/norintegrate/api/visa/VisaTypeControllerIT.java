package com.norintegrate.api.visa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class VisaTypeControllerIT extends AbstractIntegrationTest {

  @Test
  void getAllVisaTypes_returnsOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/visa-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value("SKILLED_WORKER"));
  }

  @Test
  void getVisaTypeById_hit_returnsOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/visa-types/SKILLED_WORKER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("SKILLED_WORKER"))
        .andExpect(jsonPath("$.name").value("Skilled Worker"));
  }

  @Test
  void getVisaTypeById_miss_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/visa-types/NONEXISTENT")).andExpect(status().isNotFound());
  }
}
