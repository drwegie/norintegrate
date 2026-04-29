package com.norintegrate.mcp.resource;

import com.norintegrate.common.visa.VisaTypeService;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class VisaTypeResource {

  private final VisaTypeService visaTypeService;
  private final ObjectMapper objectMapper;

  public VisaTypeResource(VisaTypeService visaTypeService, ObjectMapper objectMapper) {
    this.visaTypeService = visaTypeService;
    this.objectMapper = objectMapper;
  }

  @McpResource(
      uri = "norintegrate://visa-types",
      name = "visa-types",
      title = "Available Visa Types",
      description =
          "Reference data listing all supported visa categories (e.g. Skilled Worker, Family"
              + " Reunification, Student) with descriptions",
      mimeType = "application/json")
  public String getVisaTypes() throws JacksonException {
    var summaries =
        visaTypeService.findAll().stream()
            .map(vt -> new VisaTypeSummary(vt.getId(), vt.getName(), vt.getDescription()))
            .toList();
    return objectMapper.writeValueAsString(summaries);
  }

  public record VisaTypeSummary(String id, String name, String description) {}
}
