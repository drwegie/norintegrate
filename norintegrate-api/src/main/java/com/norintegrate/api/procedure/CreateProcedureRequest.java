package com.norintegrate.api.procedure;

public record CreateProcedureRequest(
    String title, String description, String authority, Integer estimatedDays) {}
