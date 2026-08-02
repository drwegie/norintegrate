package com.norintegrate.mcp.tool

@JvmRecord
data class DocumentItem(
    val documentName: String,
    val description: String?,
    val mandatory: Boolean,
)
