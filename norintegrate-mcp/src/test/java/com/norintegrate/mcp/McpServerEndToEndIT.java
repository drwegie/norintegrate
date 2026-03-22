package com.norintegrate.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import com.norintegrate.common.municipality.SsbKlassClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.ai.mcp.server.enabled=true")
@DisplayName("MCP Server End-to-End Tests")
class McpServerEndToEndIT {

  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @MockitoBean private SsbKlassClient ssbKlassClient;

  @LocalServerPort private int port;

  private McpSyncClient mcpClient;

  @BeforeEach
  void setUp() {
    var transport = HttpClientSseClientTransport.builder("http://localhost:" + port).build();
    mcpClient =
        McpClient.sync(transport)
            .requestTimeout(Duration.ofSeconds(10))
            .initializationTimeout(Duration.ofSeconds(10))
            .build();
    mcpClient.initialize();
  }

  @AfterEach
  void tearDown() {
    if (mcpClient != null) {
      mcpClient.close();
    }
  }

  @Test
  @DisplayName("MCP server exposes all three tools")
  void listTools_returnsAllTools() {
    var result = mcpClient.listTools();

    var toolNames = result.tools().stream().map(McpSchema.Tool::name).toList();
    assertThat(toolNames)
        .containsExactlyInAnyOrder(
            "getIntegrationGuide", "getProcedureDetail", "searchMunicipality");
  }

  @Test
  @DisplayName("callTool getIntegrationGuide returns valid response via MCP protocol")
  void callTool_getIntegrationGuide_returnsResponse() {
    var request =
        new McpSchema.CallToolRequest(
            "getIntegrationGuide", Map.of("visaTypeId", "SKILLED_WORKER"));

    var result = mcpClient.callTool(request);

    assertThat(result.isError()).isNotEqualTo(true);
    assertThat(result.content()).isNotEmpty();

    var textContent = (McpSchema.TextContent) result.content().getFirst();
    assertThat(textContent.text()).contains("SKILLED_WORKER");
  }

  @Test
  @DisplayName("callTool getProcedureDetail returns valid response via MCP protocol")
  void callTool_getProcedureDetail_returnsResponse() {
    var request = new McpSchema.CallToolRequest("getProcedureDetail", Map.of("procedureId", 1));

    var result = mcpClient.callTool(request);

    assertThat(result.isError()).isNotEqualTo(true);
    assertThat(result.content()).isNotEmpty();

    var textContent = (McpSchema.TextContent) result.content().getFirst();
    assertThat(textContent.text()).contains("Receive job offer");
  }

  @Test
  @DisplayName("MCP server reports correct server info")
  void serverInfo_returnsCorrectNameAndVersion() {
    var info = mcpClient.getServerInfo();

    assertThat(info.name()).isEqualTo("norintegrate-mcp");
    assertThat(info.version()).isEqualTo("1.0.0");
  }
}
