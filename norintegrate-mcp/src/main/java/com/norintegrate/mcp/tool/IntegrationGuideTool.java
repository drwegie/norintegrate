package com.norintegrate.mcp.tool;

import com.norintegrate.common.checklist.ChecklistService;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class IntegrationGuideTool {

  private final ChecklistService checklistService;

  public IntegrationGuideTool(ChecklistService checklistService) {
    this.checklistService = checklistService;
  }

  @Tool(
      name = "getIntegrationGuide",
      description =
          "Get a personalized integration checklist for immigrants based on visa type, showing"
              + " remaining procedures in dependency order with the recommended next steps"
              + " highlighted")
  public IntegrationGuideResult getIntegrationGuide(
      @ToolParam(description = "The visa type identifier, e.g. 'SKILLED_WORKER'") String visaTypeId,
      @ToolParam(
              description =
                  "Comma-separated list of already completed procedure IDs, or empty if none",
              required = false)
          String completedIds) {
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
