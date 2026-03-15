package com.norintegrate.mcp.tool;

import java.util.List;

public record ProcedureDetailResult(
    long id,
    String title,
    String description,
    String authority,
    Integer estimatedDays,
    List<DocumentItem> documents) {}
