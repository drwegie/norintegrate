package com.norintegrate.common.municipality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityServiceTest {

  @Mock private SsbKlassClient ssbKlassClient;

  @InjectMocks private MunicipalityService municipalityService;

  private List<MunicipalityInfo> sampleMunicipalities() {
    return List.of(
        new MunicipalityInfo("0301", "Oslo"),
        new MunicipalityInfo("4601", "Bergen"),
        new MunicipalityInfo("5001", "Trondheim"),
        new MunicipalityInfo("1103", "Stavanger"));
  }

  @Test
  @DisplayName("search: filters results by name case-insensitively")
  void search_partialQueryCaseInsensitive_returnsMatchingMunicipalities() {
    when(ssbKlassClient.getMunicipalities()).thenReturn(sampleMunicipalities());

    var result = municipalityService.search("BERG");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().name()).isEqualTo("Bergen");
    assertThat(result.getFirst().code()).isEqualTo("4601");
  }

  @Test
  @DisplayName("search: query matching multiple names returns all matches")
  void search_queryMatchingMultiple_returnsAllMatches() {
    when(ssbKlassClient.getMunicipalities()).thenReturn(sampleMunicipalities());

    // "o" appears in "Oslo", "Trondheim" (not Bergen or Stavanger)
    var result = municipalityService.search("o");

    assertThat(result)
        .extracting(MunicipalityInfo::name)
        .containsExactlyInAnyOrder("Oslo", "Trondheim");
  }

  @Test
  @DisplayName("search: no matches → returns empty list")
  void search_noMatches_returnsEmptyList() {
    when(ssbKlassClient.getMunicipalities()).thenReturn(sampleMunicipalities());

    var result = municipalityService.search("Zzzzz");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("search: empty query matches all municipalities")
  void search_emptyQuery_returnsAllMunicipalities() {
    when(ssbKlassClient.getMunicipalities()).thenReturn(sampleMunicipalities());

    var result = municipalityService.search("");

    assertThat(result).hasSize(4);
  }

  @Test
  @DisplayName("findByCode: code found → returns matching MunicipalityInfo")
  void findByCode_found_returnsMunicipalityInfo() {
    when(ssbKlassClient.getMunicipalities()).thenReturn(sampleMunicipalities());

    var result = municipalityService.findByCode("0301");

    assertThat(result.code()).isEqualTo("0301");
    assertThat(result.name()).isEqualTo("Oslo");
  }

  @Test
  @DisplayName("findByCode: code not found → EntityNotFoundException")
  void findByCode_notFound_throwsEntityNotFoundException() {
    when(ssbKlassClient.getMunicipalities()).thenReturn(sampleMunicipalities());

    assertThatThrownBy(() -> municipalityService.findByCode("9999"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("9999");
  }
}
