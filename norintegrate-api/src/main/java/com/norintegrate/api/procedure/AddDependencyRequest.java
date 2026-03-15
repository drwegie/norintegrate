package com.norintegrate.api.procedure;

public record AddDependencyRequest(long prerequisiteId, long dependentId) {}
