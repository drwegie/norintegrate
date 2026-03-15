package com.norintegrate.mcp.tool;

import com.norintegrate.common.municipality.MunicipalityService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MunicipalitySearchTool {

  private final MunicipalityService municipalityService;

  public MunicipalitySearchTool(MunicipalityService municipalityService) {
    this.municipalityService = municipalityService;
  }

  // TODO: add @Tool annotation when Spring AI 2.x is GA
  public List<MunicipalityResult> searchMunicipality(String query) {
    if (query == null || query.isBlank()) {
      throw new IllegalArgumentException("query must not be blank");
    }
    var results = municipalityService.search(query);
    return results.stream().map(m -> new MunicipalityResult(m.code(), m.name())).toList();
  }
}
