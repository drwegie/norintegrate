package com.norintegrate.mcp.tool;

public record ProcedureStep(
    long procedureId, String title, String authority, Integer estimatedDays, boolean isNext) {}
