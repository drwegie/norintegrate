package com.norintegrate.mcp.tool;

import com.norintegrate.common.checklist.ChecklistService;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class IntegrationGuideTool {

  private final ChecklistService checklistService;

  public IntegrationGuideTool(ChecklistService checklistService) {
    this.checklistService = checklistService;
  }

  // TODO: add @Tool annotation when Spring AI 2.x is GA
  public IntegrationGuideResult getIntegrationGuide(String visaTypeId, String completedIds) {
    if (visaTypeId == null || visaTypeId.isBlank()) {
      throw new IllegalArgumentException("visaTypeId must not be blank");
    }
    var completed = parseCompletedIds(completedIds);
    var items = checklistService.getChecklist(visaTypeId, completed);
    var steps =
        items.stream()
            .map(
                item -> {
                  var p = item.procedure();
                  return new ProcedureStep(
                      p.getId(),
                      p.getTitle(),
                      p.getAuthority(),
                      p.getEstimatedDays(),
                      item.isNext());
                })
            .toList();
    return new IntegrationGuideResult(visaTypeId, steps);
  }

  private Set<Long> parseCompletedIds(String completedIds) {
    if (completedIds == null || completedIds.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(completedIds.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(
            s -> {
              try {
                return Long.parseLong(s);
              } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid procedure id: " + s);
              }
            })
        .collect(Collectors.toSet());
  }
}
