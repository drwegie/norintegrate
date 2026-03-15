package com.norintegrate.api.checklist;

import com.norintegrate.common.checklist.ChecklistItem;
import com.norintegrate.common.checklist.ChecklistService;
import java.util.Arrays;
import java.util.List;
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

  public ChecklistController(ChecklistService checklistService) {
    this.checklistService = checklistService;
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

    var responseItems =
        items.stream()
            .map(
                item ->
                    new ChecklistItemResponse(
                        item.procedure().getId(),
                        item.procedure().getTitle(),
                        item.procedure().getAuthority(),
                        item.procedure().getEstimatedDays(),
                        item.isNext()))
            .toList();

    return new ChecklistResponse(visaTypeId, responseItems);
  }
}
