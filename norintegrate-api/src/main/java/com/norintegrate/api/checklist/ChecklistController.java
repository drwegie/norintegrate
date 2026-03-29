package com.norintegrate.api.checklist;

import com.norintegrate.common.checklist.ChecklistItem;
import com.norintegrate.common.checklist.ChecklistService;
import com.norintegrate.common.procedure.DocumentRequirement;
import com.norintegrate.common.procedure.DocumentRequirementRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checklist")
public class ChecklistController {

  private final ChecklistService checklistService;
  private final DocumentRequirementRepository documentRequirementRepository;

  public ChecklistController(
      ChecklistService checklistService,
      DocumentRequirementRepository documentRequirementRepository) {
    this.checklistService = checklistService;
    this.documentRequirementRepository = documentRequirementRepository;
  }

  @GetMapping("/{visaTypeId}")
  public ChecklistResponse getChecklist(
      @PathVariable String visaTypeId,
      @RequestParam(required = false, defaultValue = "") String completed) {

    Set<Long> completedIds =
        completed.isBlank()
            ? Set.of()
            : Arrays.stream(completed.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(
                    s -> {
                      try {
                        return Long.parseLong(s);
                      } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid completed id: " + s);
                      }
                    })
                .collect(Collectors.toSet());

    List<ChecklistItem> items = checklistService.getChecklist(visaTypeId, completedIds);

    var procedureIds = items.stream().map(item -> item.procedure().getId()).toList();

    Map<Long, List<DocumentRequirement>> docsByProcedure =
        documentRequirementRepository.findByProcedureIdIn(procedureIds).stream()
            .collect(Collectors.groupingBy(doc -> doc.getProcedure().getId()));

    var responseItems =
        items.stream()
            .map(
                item -> {
                  var procId = item.procedure().getId();
                  var documents =
                      docsByProcedure.getOrDefault(procId, List.of()).stream()
                          .map(
                              doc ->
                                  new ChecklistDocumentResponse(
                                      doc.getDocumentName(), doc.isMandatory()))
                          .toList();
                  return new ChecklistItemResponse(
                      procId,
                      item.procedure().getTitle(),
                      item.procedure().getDescription(),
                      item.procedure().getAuthority(),
                      item.procedure().getEstimatedDays(),
                      item.isNext(),
                      documents);
                })
            .toList();

    return new ChecklistResponse(visaTypeId, responseItems);
  }
}
