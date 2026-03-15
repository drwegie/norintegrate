package com.norintegrate.api.checklist;

import java.util.List;

public record ChecklistResponse(String visaTypeId, List<ChecklistItemResponse> items) {}
