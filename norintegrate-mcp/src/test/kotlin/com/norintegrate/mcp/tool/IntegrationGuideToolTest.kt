package com.norintegrate.mcp.tool

import com.norintegrate.common.checklist.ChecklistItem
import com.norintegrate.common.checklist.ChecklistService
import com.norintegrate.common.procedure.Procedure
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@DisplayName("IntegrationGuideTool")
class IntegrationGuideToolTest {
    @Mock
    private lateinit var checklistService: ChecklistService

    @InjectMocks
    private lateinit var integrationGuideTool: IntegrationGuideTool

    @Test
    @DisplayName("getIntegrationGuide returns correctly mapped steps for a valid visa type")
    fun getIntegrationGuide_validVisaType_returnsMappedSteps() {
        val procedure1 = mockProcedure(1L, "Get D-nummer", "Tax Office", 14)
        val procedure2 = mockProcedure(2L, "Open bank account", "Bank", 7)

        val items = listOf(ChecklistItem(procedure1, true), ChecklistItem(procedure2, false))

        `when`(checklistService.getChecklist(eq("SKILLED_WORKER"), eq(setOf()))).thenReturn(items)

        val result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", null)

        assertThat(result.visaTypeId).isEqualTo("SKILLED_WORKER")
        assertThat(result.steps).hasSize(2)

        val step1 = result.steps[0]
        assertThat(step1.procedureId).isEqualTo(1L)
        assertThat(step1.title).isEqualTo("Get D-nummer")
        assertThat(step1.authority).isEqualTo("Tax Office")
        assertThat(step1.estimatedDays).isEqualTo(14)
        assertThat(step1.isNext).isTrue()

        val step2 = result.steps[1]
        assertThat(step2.procedureId).isEqualTo(2L)
        assertThat(step2.title).isEqualTo("Open bank account")
        assertThat(step2.isNext).isFalse()
    }

    @Test
    @DisplayName("getIntegrationGuide with null visaTypeId throws IllegalArgumentException")
    fun getIntegrationGuide_nullVisaTypeId_throwsIllegalArgumentException() {
        assertThatThrownBy { integrationGuideTool.getIntegrationGuide(null, null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("visaTypeId must not be blank")
    }

    @Test
    @DisplayName("getIntegrationGuide with blank visaTypeId throws IllegalArgumentException")
    fun getIntegrationGuide_blankVisaTypeId_throwsIllegalArgumentException() {
        assertThatThrownBy { integrationGuideTool.getIntegrationGuide("   ", null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("visaTypeId must not be blank")
    }

    @Test
    @DisplayName("getIntegrationGuide with non-numeric completedIds throws IllegalArgumentException")
    fun getIntegrationGuide_invalidCompletedIds_throwsIllegalArgumentException() {
        assertThatThrownBy { integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "1,abc,3") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Invalid procedure id: abc")
    }

    @Test
    @DisplayName("getIntegrationGuide passes parsed completed IDs to service")
    fun getIntegrationGuide_withCompletedIds_passesToService() {
        `when`(checklistService.getChecklist(eq("SKILLED_WORKER"), eq(setOf(1L, 3L))))
            .thenReturn(listOf())

        val result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "1, 3")

        verify(checklistService).getChecklist("SKILLED_WORKER", setOf(1L, 3L))
        assertThat(result.steps).isEmpty()
    }

    @Test
    @DisplayName("getIntegrationGuide with empty completedIds string passes empty set")
    fun getIntegrationGuide_emptyCompletedIds_passesEmptySet() {
        `when`(checklistService.getChecklist(eq("SKILLED_WORKER"), eq(setOf()))).thenReturn(listOf())

        val result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "")

        verify(checklistService).getChecklist("SKILLED_WORKER", setOf())
        assertThat(result.steps).isEmpty()
    }

    private fun mockProcedure(
        id: Long,
        title: String,
        authority: String,
        estimatedDays: Int,
    ): Procedure {
        val procedure = mock(Procedure::class.java)
        `when`(procedure.id).thenReturn(id)
        `when`(procedure.title).thenReturn(title)
        `when`(procedure.authority).thenReturn(authority)
        `when`(procedure.estimatedDays).thenReturn(estimatedDays)
        return procedure
    }
}
