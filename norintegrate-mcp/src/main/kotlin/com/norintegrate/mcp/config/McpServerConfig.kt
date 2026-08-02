package com.norintegrate.mcp.config

import com.norintegrate.mcp.tool.IntegrationGuideTool
import com.norintegrate.mcp.tool.MunicipalitySearchTool
import com.norintegrate.mcp.tool.ProcedureDetailTool
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class McpServerConfig {
    @Bean
    fun toolCallbackProvider(
        integrationGuideTool: IntegrationGuideTool,
        procedureDetailTool: ProcedureDetailTool,
        municipalitySearchTool: MunicipalitySearchTool,
    ): ToolCallbackProvider =
        MethodToolCallbackProvider
            .builder()
            .toolObjects(integrationGuideTool, procedureDetailTool, municipalitySearchTool)
            .build()
}
