package com.norintegrate.api.procedure;

import com.norintegrate.common.procedure.ProcedureService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/procedures")
public class ProcedureController {

  private final ProcedureService procedureService;

  public ProcedureController(ProcedureService procedureService) {
    this.procedureService = procedureService;
  }

  @GetMapping
  public List<ProcedureResponse> findAll() {
    return procedureService.findAll().stream()
        .map(
            p ->
                new ProcedureResponse(
                    p.getId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getAuthority(),
                    p.getEstimatedDays()))
        .toList();
  }

  @GetMapping("/{id}")
  public ProcedureResponse findById(@PathVariable Long id) {
    var p = procedureService.findById(id);
    return new ProcedureResponse(
        p.getId(), p.getTitle(), p.getDescription(), p.getAuthority(), p.getEstimatedDays());
  }

  @GetMapping("/{id}/documents")
  public List<DocumentRequirementResponse> getDocuments(@PathVariable Long id) {
    return procedureService.getDocumentRequirements(id).stream()
        .map(
            d ->
                new DocumentRequirementResponse(
                    d.getId(), d.getDocumentName(), d.getDescription(), d.isMandatory()))
        .toList();
  }
}
