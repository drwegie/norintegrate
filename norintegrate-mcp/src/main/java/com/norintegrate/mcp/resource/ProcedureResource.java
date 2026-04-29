package com.norintegrate.mcp.resource;

import com.norintegrate.common.procedure.ProcedureService;
import java.util.List;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ProcedureResource {

  private final ProcedureService procedureService;
  private final ObjectMapper objectMapper;

  public ProcedureResource(ProcedureService procedureService, ObjectMapper objectMapper) {
    this.procedureService = procedureService;
    this.objectMapper = objectMapper;
  }

  @McpResource(
      uri = "norintegrate://procedures",
      name = "procedures",
      title = "Settlement Procedures",
      description =
          "Complete catalog of Norwegian settlement procedures with responsible authorities,"
              + " estimated processing times, and required documents",
      mimeType = "application/json")
  public String getProcedures() throws JacksonException {
    var summaries =
        procedureService.findAll().stream()
            .map(
                p -> {
                  var docs =
                      procedureService.getDocumentRequirements(p.getId()).stream()
                          .map(d -> new DocumentSummary(d.getDocumentName(), d.isMandatory()))
                          .toList();
                  return new ProcedureSummary(
                      p.getId(),
                      p.getTitle(),
                      p.getDescription(),
                      p.getAuthority(),
                      p.getEstimatedDays(),
                      docs);
                })
            .toList();
    return objectMapper.writeValueAsString(summaries);
  }

  public record ProcedureSummary(
      long id,
      String title,
      String description,
      String authority,
      Integer estimatedDays,
      List<DocumentSummary> documents) {}

  public record DocumentSummary(String documentName, boolean mandatory) {}
}
