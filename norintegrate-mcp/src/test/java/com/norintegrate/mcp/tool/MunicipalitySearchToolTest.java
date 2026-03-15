package com.norintegrate.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norintegrate.common.municipality.MunicipalityInfo;
import com.norintegrate.common.municipality.MunicipalityService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MunicipalitySearchTool")
class MunicipalitySearchToolTest {

  @Mock private MunicipalityService municipalityService;

  @InjectMocks private MunicipalitySearchTool municipalitySearchTool;

  @Test
  @DisplayName("searchMunicipality returns correctly mapped results for a valid query")
  void searchMunicipality_validQuery_returnsMappedResults() {
    var municipalities =
        List.of(new MunicipalityInfo("0301", "Oslo"), new MunicipalityInfo("1103", "Stavanger"));

    when(municipalityService.search("os")).thenReturn(municipalities);

    var results = municipalitySearchTool.searchMunicipality("os");

    verify(municipalityService).search("os");
    assertThat(results).hasSize(2);
    assertThat(results.get(0).code()).isEqualTo("0301");
    assertThat(results.get(0).name()).isEqualTo("Oslo");
    assertThat(results.get(1).code()).isEqualTo("1103");
    assertThat(results.get(1).name()).isEqualTo("Stavanger");
  }

  @Test
  @DisplayName("searchMunicipality with null query throws IllegalArgumentException")
  void searchMunicipality_nullQuery_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> municipalitySearchTool.searchMunicipality(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("query must not be blank");
  }

  @Test
  @DisplayName("searchMunicipality with blank query throws IllegalArgumentException")
  void searchMunicipality_blankQuery_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> municipalitySearchTool.searchMunicipality("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("query must not be blank");
  }

  @Test
  @DisplayName("searchMunicipality with no matches returns empty list")
  void searchMunicipality_noMatches_returnsEmptyList() {
    when(municipalityService.search("zzz")).thenReturn(List.of());

    var results = municipalitySearchTool.searchMunicipality("zzz");

    assertThat(results).isEmpty();
  }
}
