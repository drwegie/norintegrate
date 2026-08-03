package com.norintegrate.mcp.tool

@JvmRecord
data class ProcedureDetailResult(
    val id: Long,
    val title: String,
    val description: String?,
    val authority: String?,
    val estimatedDays: Int?,
    val documents: List<DocumentItem>,
)
