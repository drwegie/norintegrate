package com.norintegrate.api.progress;

import java.time.OffsetDateTime;

public record UserProgressResponse(
    long procedureId, String procedureTitle, boolean completed, OffsetDateTime completedAt) {}
