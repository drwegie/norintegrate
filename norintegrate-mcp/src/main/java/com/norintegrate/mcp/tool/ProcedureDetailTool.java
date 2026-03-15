package com.norintegrate.mcp.tool;

import com.norintegrate.common.procedure.ProcedureService;
import org.springframework.stereotype.Component;

@Component
public class ProcedureDetailTool {

  private final ProcedureService procedureService;

  public ProcedureDetailTool(ProcedureService procedureService) {
    this.procedureService = procedureService;
  }

  // TODO: add @Tool annotation when Spring AI 2.x is GA
  public ProcedureDetailResult getProcedureDetail(long procedureId) {
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
