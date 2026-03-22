package com.norintegrate.mcp.tool;

import com.norintegrate.common.procedure.ProcedureService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProcedureDetailTool {

  private final ProcedureService procedureService;

  public ProcedureDetailTool(ProcedureService procedureService) {
    this.procedureService = procedureService;
  }

  @Tool(
      name = "getProcedureDetail",
      description =
          "Get detailed information about a specific settlement procedure including required"
              + " documents, responsible authority, and estimated processing time")
  public ProcedureDetailResult getProcedureDetail(
      @ToolParam(description = "The numeric ID of the procedure to look up") long procedureId) {
    var procedure = procedureService.findById(procedureId);
    var requirements = procedureService.getDocumentRequirements(procedureId);
    var documents =
        requirements.stream()
            .map(
                req ->
                    new DocumentItem(
                        req.getDocumentName(), req.getDescription(), req.isMandatory()))
            .toList();
    return new ProcedureDetailResult(
        procedure.getId(),
        procedure.getTitle(),
        procedure.getDescription(),
        procedure.getAuthority(),
        procedure.getEstimatedDays(),
        documents);
  }
}
