package com.norintegrate.api.municipality;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.norintegrate.api.AbstractIntegrationTest;
import com.norintegrate.common.municipality.MunicipalityInfo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MunicipalityControllerIT extends AbstractIntegrationTest {

  @BeforeEach
  void setUp() {
    when(ssbKlassClient.getMunicipalities())
        .thenReturn(
            List.of(
                new MunicipalityInfo("0301", "Oslo"),
                new MunicipalityInfo("4601", "Bergen"),
                new MunicipalityInfo("5001", "Trondheim")));
  }

  @Test
  void search_matchingQuery_returnsMunicipalities() throws Exception {
    mockMvc
        .perform(get("/api/v1/municipalities").param("query", "Osl"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].code").value("0301"))
        .andExpect(jsonPath("$[0].name").value("Oslo"));
  }

  @Test
  void search_noMatch_returnsEmptyList() throws Exception {
    mockMvc
        .perform(get("/api/v1/municipalities").param("query", "zzz"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void findByCode_hit_returnsMunicipality() throws Exception {
    mockMvc
        .perform(get("/api/v1/municipalities/0301"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0301"))
        .andExpect(jsonPath("$.name").value("Oslo"));
  }

  @Test
  void findByCode_miss_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/municipalities/9999")).andExpect(status().isNotFound());
  }
}
