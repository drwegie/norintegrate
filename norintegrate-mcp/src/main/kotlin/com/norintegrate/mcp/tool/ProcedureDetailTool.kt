package com.norintegrate.mcp.tool

import com.norintegrate.common.procedure.ProcedureService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class ProcedureDetailTool(
    private val procedureService: ProcedureService,
) {
    @Tool(
        name = "getProcedureDetail",
        description =
            "Get detailed information about a specific settlement procedure including required" +
                " documents, responsible authority, and estimated processing time",
    )
    fun getProcedureDetail(
        @ToolParam(description = "The numeric ID of the procedure to look up") procedureId: Long,
    ): ProcedureDetailResult {
        val procedure = procedureService.findById(procedureId)
        val documents =
            procedureService.getDocumentRequirements(procedureId).map { req ->
                DocumentItem(req.documentName, req.description, req.isMandatory)
            }
        return ProcedureDetailResult(
            procedure.id,
            procedure.title,
            procedure.description,
            procedure.authority,
            procedure.estimatedDays,
            documents,
        )
    }
}
