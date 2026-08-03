package com.norintegrate.mcp.tool

import com.norintegrate.common.procedure.DocumentRequirement
import com.norintegrate.common.procedure.Procedure
import com.norintegrate.common.procedure.ProcedureService
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@DisplayName("ProcedureDetailTool")
class ProcedureDetailToolTest {
    @Mock
    private lateinit var procedureService: ProcedureService

    @InjectMocks
    private lateinit var procedureDetailTool: ProcedureDetailTool

    @Test
    @DisplayName("getProcedureDetail returns result with documents for a valid ID")
    fun getProcedureDetail_validId_returnsResultWithDocuments() {
        val procedure = mockProcedure(1L, "Get D-nummer", "Apply for D-nummer", "Tax Office", 14)
        val doc1 = mockDocumentRequirement("Passport", "Valid passport copy", true)
        val doc2 = mockDocumentRequirement("Photo", "Passport-sized photo", false)

        `when`(procedureService.findById(1L)).thenReturn(procedure)
        `when`(procedureService.getDocumentRequirements(1L)).thenReturn(listOf(doc1, doc2))

        val result = procedureDetailTool.getProcedureDetail(1L)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.title).isEqualTo("Get D-nummer")
        assertThat(result.description).isEqualTo("Apply for D-nummer")
        assertThat(result.authority).isEqualTo("Tax Office")
        assertThat(result.estimatedDays).isEqualTo(14)
        assertThat(result.documents).hasSize(2)

        assertThat(result.documents[0].documentName).isEqualTo("Passport")
        assertThat(result.documents[0].description).isEqualTo("Valid passport copy")
        assertThat(result.documents[0].mandatory).isTrue()

        assertThat(result.documents[1].documentName).isEqualTo("Photo")
        assertThat(result.documents[1].mandatory).isFalse()
    }

    @Test
    @DisplayName("getProcedureDetail with non-existent ID propagates EntityNotFoundException")
    fun getProcedureDetail_notFound_propagatesEntityNotFoundException() {
        `when`(procedureService.findById(999L))
            .thenThrow(EntityNotFoundException("Procedure not found: 999"))

        assertThatThrownBy { procedureDetailTool.getProcedureDetail(999L) }
            .isInstanceOf(EntityNotFoundException::class.java)
            .hasMessage("Procedure not found: 999")
    }

    @Test
    @DisplayName("getProcedureDetail with no documents returns empty document list")
    fun getProcedureDetail_noDocuments_returnsEmptyDocumentList() {
        val procedure = mockProcedure(2L, "Register address", "Register at local police", "Police", 3)

        `when`(procedureService.findById(2L)).thenReturn(procedure)
        `when`(procedureService.getDocumentRequirements(2L)).thenReturn(listOf())

        val result = procedureDetailTool.getProcedureDetail(2L)

        assertThat(result.id).isEqualTo(2L)
        assertThat(result.title).isEqualTo("Register address")
        assertThat(result.documents).isEmpty()
    }

    private fun mockProcedure(
        id: Long,
        title: String,
        description: String,
        authority: String,
        estimatedDays: Int,
    ): Procedure {
        val procedure = mock(Procedure::class.java)
        `when`(procedure.id).thenReturn(id)
        `when`(procedure.title).thenReturn(title)
        `when`(procedure.description).thenReturn(description)
        `when`(procedure.authority).thenReturn(authority)
        `when`(procedure.estimatedDays).thenReturn(estimatedDays)
        return procedure
    }

    private fun mockDocumentRequirement(
        name: String,
        description: String,
        mandatory: Boolean,
    ): DocumentRequirement {
        val req = mock(DocumentRequirement::class.java)
        `when`(req.documentName).thenReturn(name)
        `when`(req.description).thenReturn(description)
        `when`(req.isMandatory).thenReturn(mandatory)
        return req
    }
}
