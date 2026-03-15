package com.norintegrate.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.norintegrate.common.procedure.DocumentRequirement;
import com.norintegrate.common.procedure.Procedure;
import com.norintegrate.common.procedure.ProcedureService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcedureDetailTool")
class ProcedureDetailToolTest {

  @Mock private ProcedureService procedureService;

  @InjectMocks private ProcedureDetailTool procedureDetailTool;

  @Test
  @DisplayName("getProcedureDetail returns result with documents for a valid ID")
  void getProcedureDetail_validId_returnsResultWithDocuments() {
    var procedure = mockProcedure(1L, "Get D-nummer", "Apply for D-nummer", "Tax Office", 14);
    var doc1 = mockDocumentRequirement("Passport", "Valid passport copy", true);
    var doc2 = mockDocumentRequirement("Photo", "Passport-sized photo", false);

    when(procedureService.findById(1L)).thenReturn(procedure);
    when(procedureService.getDocumentRequirements(1L)).thenReturn(List.of(doc1, doc2));

    var result = procedureDetailTool.getProcedureDetail(1L);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.title()).isEqualTo("Get D-nummer");
    assertThat(result.description()).isEqualTo("Apply for D-nummer");
    assertThat(result.authority()).isEqualTo("Tax Office");
    assertThat(result.estimatedDays()).isEqualTo(14);
    assertThat(result.documents()).hasSize(2);

    assertThat(result.documents().get(0).documentName()).isEqualTo("Passport");
    assertThat(result.documents().get(0).description()).isEqualTo("Valid passport copy");
    assertThat(result.documents().get(0).mandatory()).isTrue();

    assertThat(result.documents().get(1).documentName()).isEqualTo("Photo");
    assertThat(result.documents().get(1).mandatory()).isFalse();
  }

  @Test
  @DisplayName("getProcedureDetail with non-existent ID propagates EntityNotFoundException")
  void getProcedureDetail_notFound_propagatesEntityNotFoundException() {
    when(procedureService.findById(999L))
        .thenThrow(new EntityNotFoundException("Procedure not found: 999"));

    assertThatThrownBy(() -> procedureDetailTool.getProcedureDetail(999L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Procedure not found: 999");
  }

  @Test
  @DisplayName("getProcedureDetail with no documents returns empty document list")
  void getProcedureDetail_noDocuments_returnsEmptyDocumentList() {
    var procedure = mockProcedure(2L, "Register address", "Register at local police", "Police", 3);

    when(procedureService.findById(2L)).thenReturn(procedure);
    when(procedureService.getDocumentRequirements(2L)).thenReturn(List.of());

    var result = procedureDetailTool.getProcedureDetail(2L);

    assertThat(result.id()).isEqualTo(2L);
    assertThat(result.title()).isEqualTo("Register address");
    assertThat(result.documents()).isEmpty();
  }

  private Procedure mockProcedure(
      long id, String title, String description, String authority, int estimatedDays) {
    var procedure = mock(Procedure.class);
    when(procedure.getId()).thenReturn(id);
    when(procedure.getTitle()).thenReturn(title);
    when(procedure.getDescription()).thenReturn(description);
    when(procedure.getAuthority()).thenReturn(authority);
    when(procedure.getEstimatedDays()).thenReturn(estimatedDays);
    return procedure;
  }

  private DocumentRequirement mockDocumentRequirement(
      String name, String description, boolean mandatory) {
    var req = mock(DocumentRequirement.class);
    when(req.getDocumentName()).thenReturn(name);
    when(req.getDescription()).thenReturn(description);
    when(req.isMandatory()).thenReturn(mandatory);
    return req;
  }
}
