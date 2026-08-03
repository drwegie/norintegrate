package com.norintegrate.mcp.tool

@JvmRecord
data class IntegrationGuideResult(
    val visaTypeId: String,
    val steps: List<ProcedureStep>,
)
