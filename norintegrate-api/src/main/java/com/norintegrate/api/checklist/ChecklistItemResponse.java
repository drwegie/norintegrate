package com.norintegrate.api.checklist;

import java.util.List;

public record ChecklistItemResponse(
    long procedureId,
    String title,
    String description,
    String authority,
    Integer estimatedDays,
    boolean isNext,
    List<ChecklistDocumentResponse> documents) {}
