package com.norintegrate.api.checklist;

public record ChecklistItemResponse(
    long procedureId, String title, String authority, Integer estimatedDays, boolean isNext) {}
