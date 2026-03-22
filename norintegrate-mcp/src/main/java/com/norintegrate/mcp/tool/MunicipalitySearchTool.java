package com.norintegrate.mcp.tool;

import com.norintegrate.common.municipality.MunicipalityService;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MunicipalitySearchTool {

  private final MunicipalityService municipalityService;

  public MunicipalitySearchTool(MunicipalityService municipalityService) {
    this.municipalityService = municipalityService;
  }

  @Tool(
      name = "searchMunicipality",
      description =
          "Search for Norwegian municipalities by name to find municipality codes and official"
              + " names from Statistics Norway (SSB)")
  public List<MunicipalityResult> searchMunicipality(
      @ToolParam(description = "The municipality name or partial name to search for")
          String query) {
    if (query == null || query.isBlank()) {
      throw new IllegalArgumentException("query must not be blank");
    }
    var results = municipalityService.search(query);
    return results.stream().map(m -> new MunicipalityResult(m.code(), m.name())).toList();
  }
}
