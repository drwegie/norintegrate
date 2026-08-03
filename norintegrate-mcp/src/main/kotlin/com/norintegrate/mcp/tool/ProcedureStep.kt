package com.norintegrate.mcp.tool

@JvmRecord
data class ProcedureStep(
    val procedureId: Long,
    val title: String,
    val authority: String?,
    val estimatedDays: Int?,
    val isNext: Boolean,
)
