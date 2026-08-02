package com.norintegrate.mcp.tool

import com.norintegrate.common.checklist.ChecklistService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class IntegrationGuideTool(
    private val checklistService: ChecklistService,
) {
    @Tool(
        name = "getIntegrationGuide",
        description =
            "Get a personalized integration checklist for immigrants based on visa type, showing" +
                " remaining procedures in dependency order with the recommended next steps" +
                " highlighted",
    )
    fun getIntegrationGuide(
        @ToolParam(description = "The visa type identifier, e.g. 'SKILLED_WORKER'")
        visaTypeId: String?,
        @ToolParam(
            description =
                "Comma-separated list of already completed procedure IDs, or empty if none",
            required = false,
        )
        completedIds: String?,
    ): IntegrationGuideResult {
        require(!visaTypeId.isNullOrBlank()) { "visaTypeId must not be blank" }
        val completed = parseCompletedIds(completedIds)
        val steps =
            checklistService.getChecklist(visaTypeId, completed).map { item ->
                val p = item.procedure()
                ProcedureStep(p.id, p.title, p.authority, p.estimatedDays, item.isNext)
            }
        return IntegrationGuideResult(visaTypeId, steps)
    }

    private fun parseCompletedIds(completedIds: String?): Set<Long> {
        if (completedIds.isNullOrBlank()) return emptySet()
        return completedIds
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toLongOrNull() ?: throw IllegalArgumentException("Invalid procedure id: $it") }
            .toSet()
    }
}
