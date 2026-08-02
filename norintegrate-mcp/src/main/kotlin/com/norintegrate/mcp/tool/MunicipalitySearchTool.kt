package com.norintegrate.mcp.tool

import com.norintegrate.common.municipality.MunicipalityService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@JvmRecord
data class MunicipalityResult(
    val code: String,
    val name: String,
)

@Component
class MunicipalitySearchTool(
    private val municipalityService: MunicipalityService,
) {
    @Tool(
        name = "searchMunicipality",
        description =
            "Search for Norwegian municipalities by name to find municipality codes and official" +
                " names from Statistics Norway (SSB)",
    )
    fun searchMunicipality(
        @ToolParam(description = "The municipality name or partial name to search for") query: String?,
    ): List<MunicipalityResult> {
        require(!query.isNullOrBlank()) { "query must not be blank" }
        return municipalityService.search(query).map { MunicipalityResult(it.code(), it.name()) }
    }
}
