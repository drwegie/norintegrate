package com.norintegrate.api.procedure;

public record UpdateProcedureRequest(
    String title, String description, String authority, Integer estimatedDays) {}
