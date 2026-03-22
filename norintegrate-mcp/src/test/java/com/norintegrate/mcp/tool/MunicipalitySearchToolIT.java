package com.norintegrate.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.norintegrate.common.municipality.MunicipalityInfo;
import com.norintegrate.mcp.AbstractMcpIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("MunicipalitySearchTool - Integration Tests")
class MunicipalitySearchToolIT extends AbstractMcpIntegrationTest {

  @Autowired private MunicipalitySearchTool municipalitySearchTool;

  @Test
  @DisplayName("searchMunicipality returns matching municipalities from SSB client")
  void searchMunicipality_validQuery_returnsMatchingResults() {
    when(ssbKlassClient.getMunicipalities())
        .thenReturn(
            List.of(
                new MunicipalityInfo("0301", "Oslo"),
                new MunicipalityInfo("4601", "Bergen"),
                new MunicipalityInfo("5001", "Trondheim"),
                new MunicipalityInfo("1103", "Stavanger")));

    var results = municipalitySearchTool.searchMunicipality("Oslo");

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().code()).isEqualTo("0301");
    assertThat(results.getFirst().name()).isEqualTo("Oslo");
  }

  @Test
  @DisplayName("searchMunicipality is case-insensitive")
  void searchMunicipality_caseInsensitive_returnsMatches() {
    when(ssbKlassClient.getMunicipalities())
        .thenReturn(
            List.of(new MunicipalityInfo("0301", "Oslo"), new MunicipalityInfo("4601", "Bergen")));

    var results = municipalitySearchTool.searchMunicipality("oslo");

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().name()).isEqualTo("Oslo");
  }

  @Test
  @DisplayName("searchMunicipality with partial name returns multiple matches")
  void searchMunicipality_partialName_returnsMultipleMatches() {
    when(ssbKlassClient.getMunicipalities())
        .thenReturn(
            List.of(
                new MunicipalityInfo("1505", "Kristiansund"),
                new MunicipalityInfo("4204", "Kristiansand"),
                new MunicipalityInfo("0301", "Oslo")));

    var results = municipalitySearchTool.searchMunicipality("Kristian");

    assertThat(results).hasSize(2);
    assertThat(results)
        .extracting(MunicipalityResult::name)
        .containsExactlyInAnyOrder("Kristiansund", "Kristiansand");
  }

  @Test
  @DisplayName("searchMunicipality with no matches returns empty list")
  void searchMunicipality_noMatches_returnsEmptyList() {
    when(ssbKlassClient.getMunicipalities())
        .thenReturn(
            List.of(new MunicipalityInfo("0301", "Oslo"), new MunicipalityInfo("4601", "Bergen")));

    var results = municipalitySearchTool.searchMunicipality("Nonexistent");

    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("searchMunicipality with blank query throws IllegalArgumentException")
  void searchMunicipality_blankQuery_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> municipalitySearchTool.searchMunicipality("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("query must not be blank");
  }

  @Test
  @DisplayName("searchMunicipality with null query throws IllegalArgumentException")
  void searchMunicipality_nullQuery_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> municipalitySearchTool.searchMunicipality(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("query must not be blank");
  }
}
