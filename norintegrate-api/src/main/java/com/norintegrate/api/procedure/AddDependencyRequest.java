package com.norintegrate.api.procedure;

import jakarta.validation.constraints.Positive;

public record AddDependencyRequest(@Positive long prerequisiteId, @Positive long dependentId) {}
