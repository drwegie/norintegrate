package com.norintegrate.mcp.tool

import com.norintegrate.common.municipality.MunicipalityInfo
import com.norintegrate.common.municipality.MunicipalityService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@DisplayName("MunicipalitySearchTool")
class MunicipalitySearchToolTest {
    @Mock
    private lateinit var municipalityService: MunicipalityService

    @InjectMocks
    private lateinit var municipalitySearchTool: MunicipalitySearchTool

    @Test
    @DisplayName("searchMunicipality returns correctly mapped results for a valid query")
    fun searchMunicipality_validQuery_returnsMappedResults() {
        val municipalities = listOf(MunicipalityInfo("0301", "Oslo"), MunicipalityInfo("1103", "Stavanger"))

        `when`(municipalityService.search("os")).thenReturn(municipalities)

        val results = municipalitySearchTool.searchMunicipality("os")

        verify(municipalityService).search("os")
        assertThat(results).hasSize(2)
        assertThat(results[0].code).isEqualTo("0301")
        assertThat(results[0].name).isEqualTo("Oslo")
        assertThat(results[1].code).isEqualTo("1103")
        assertThat(results[1].name).isEqualTo("Stavanger")
    }

    @Test
    @DisplayName("searchMunicipality with null query throws IllegalArgumentException")
    fun searchMunicipality_nullQuery_throwsIllegalArgumentException() {
        assertThatThrownBy { municipalitySearchTool.searchMunicipality(null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("query must not be blank")
    }

    @Test
    @DisplayName("searchMunicipality with blank query throws IllegalArgumentException")
    fun searchMunicipality_blankQuery_throwsIllegalArgumentException() {
        assertThatThrownBy { municipalitySearchTool.searchMunicipality("   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("query must not be blank")
    }

    @Test
    @DisplayName("searchMunicipality with no matches returns empty list")
    fun searchMunicipality_noMatches_returnsEmptyList() {
        `when`(municipalityService.search("zzz")).thenReturn(emptyList())

        val results = municipalitySearchTool.searchMunicipality("zzz")

        assertThat(results).isEmpty()
    }
}
