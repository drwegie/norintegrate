package com.norintegrate.api.checklist;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ChecklistControllerIT extends AbstractIntegrationTest {

  @Test
  void getChecklist_noCompleted_returnsAllItems() throws Exception {
    mockMvc
        .perform(get("/api/v1/checklist/SKILLED_WORKER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.visaTypeId").value("SKILLED_WORKER"))
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(11));
  }

  @Test
  void getChecklist_withCompleted_excludesCompletedItems() throws Exception {
    mockMvc
        .perform(get("/api/v1/checklist/SKILLED_WORKER").param("completed", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(10));
  }

  @Test
  void getChecklist_invalidVisaType_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/checklist/NONEXISTENT")).andExpect(status().isNotFound());
  }
}
