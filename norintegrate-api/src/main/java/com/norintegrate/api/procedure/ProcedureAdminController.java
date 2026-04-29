package com.norintegrate.api.procedure;

import com.norintegrate.common.procedure.ProcedureService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/procedures")
public class ProcedureAdminController {

  private final ProcedureService procedureService;

  public ProcedureAdminController(ProcedureService procedureService) {
    this.procedureService = procedureService;
  }

  @PostMapping
  public ResponseEntity<ProcedureResponse> create(
      @Valid @RequestBody CreateProcedureRequest request) {
    var p =
        procedureService.create(
            request.title(), request.description(), request.authority(), request.estimatedDays());
    var response =
        new ProcedureResponse(
            p.getId(), p.getTitle(), p.getDescription(), p.getAuthority(), p.getEstimatedDays());
    return ResponseEntity.created(URI.create("/api/v1/procedures/" + p.getId())).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProcedureResponse> update(
      @PathVariable Long id, @Valid @RequestBody UpdateProcedureRequest request) {
    var p =
        procedureService.update(
            id,
            request.title(),
            request.description(),
            request.authority(),
            request.estimatedDays());
    return ResponseEntity.ok(
        new ProcedureResponse(
            p.getId(), p.getTitle(), p.getDescription(), p.getAuthority(), p.getEstimatedDays()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    procedureService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/dependencies")
  public ResponseEntity<Void> addDependency(
      @PathVariable Long id, @Valid @RequestBody AddDependencyRequest request) {
    procedureService.addDependency(request.prerequisiteId(), request.dependentId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{prerequisiteId}/dependencies/{dependentId}")
  public ResponseEntity<Void> removeDependency(
      @PathVariable Long prerequisiteId, @PathVariable Long dependentId) {
    procedureService.removeDependency(prerequisiteId, dependentId);
    return ResponseEntity.noContent().build();
  }
}
