package com.norintegrate.mcp.resource

import com.norintegrate.common.procedure.ProcedureService
import org.springframework.ai.mcp.annotation.McpResource
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class ProcedureResource(
    private val procedureService: ProcedureService,
    private val objectMapper: ObjectMapper,
) {
    @McpResource(
        uri = "norintegrate://procedures",
        name = "procedures",
        title = "Settlement Procedures",
        description =
            "Complete catalog of Norwegian settlement procedures with responsible authorities," +
                " estimated processing times, and required documents",
        mimeType = "application/json",
    )
    fun getProcedures(): String {
        val summaries =
            procedureService.findAll().map { p ->
                val docs =
                    procedureService.getDocumentRequirements(p.id).map { d ->
                        DocumentSummary(d.documentName, d.isMandatory)
                    }
                ProcedureSummary(p.id, p.title, p.description, p.authority, p.estimatedDays, docs)
            }
        return objectMapper.writeValueAsString(summaries)
    }

    @JvmRecord
    data class ProcedureSummary(
        val id: Long,
        val title: String,
        val description: String?,
        val authority: String?,
        val estimatedDays: Int?,
        val documents: List<DocumentSummary>,
    )

    @JvmRecord
    data class DocumentSummary(
        val documentName: String,
        val mandatory: Boolean,
    )
}
