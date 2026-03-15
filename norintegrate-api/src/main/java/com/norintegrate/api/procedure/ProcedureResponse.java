package com.norintegrate.api.procedure;

public record ProcedureResponse(
    long id, String title, String description, String authority, Integer estimatedDays) {}
