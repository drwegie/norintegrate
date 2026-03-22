package com.norintegrate.mcp.config;

import com.norintegrate.mcp.tool.IntegrationGuideTool;
import com.norintegrate.mcp.tool.MunicipalitySearchTool;
import com.norintegrate.mcp.tool.ProcedureDetailTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

  @Bean
  public ToolCallbackProvider toolCallbackProvider(
      IntegrationGuideTool integrationGuideTool,
      ProcedureDetailTool procedureDetailTool,
      MunicipalitySearchTool municipalitySearchTool) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(integrationGuideTool, procedureDetailTool, municipalitySearchTool)
        .build();
  }
}
