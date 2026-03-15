package com.norintegrate.api.procedure;

public record DocumentRequirementResponse(
    long id, String documentName, String description, boolean mandatory) {}
