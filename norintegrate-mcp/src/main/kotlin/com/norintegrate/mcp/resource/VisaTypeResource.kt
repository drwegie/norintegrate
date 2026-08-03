package com.norintegrate.mcp.resource

import com.norintegrate.common.visa.VisaTypeService
import org.springframework.ai.mcp.annotation.McpResource
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class VisaTypeResource(
    private val visaTypeService: VisaTypeService,
    private val objectMapper: ObjectMapper,
) {
    @McpResource(
        uri = "norintegrate://visa-types",
        name = "visa-types",
        title = "Available Visa Types",
        description =
            "Reference data listing all supported visa categories (e.g. Skilled Worker, Family" +
                " Reunification, Student) with descriptions",
        mimeType = "application/json",
    )
    fun getVisaTypes(): String {
        val summaries = visaTypeService.findAll().map { VisaTypeSummary(it.id, it.name, it.description) }
        return objectMapper.writeValueAsString(summaries)
    }

    @JvmRecord
    data class VisaTypeSummary(
        val id: String,
        val name: String,
        val description: String?,
    )
}
