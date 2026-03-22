package com.norintegrate.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.norintegrate.mcp.AbstractMcpIntegrationTest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("ProcedureDetailTool - Integration Tests")
class ProcedureDetailToolIT extends AbstractMcpIntegrationTest {

  @Autowired private ProcedureDetailTool procedureDetailTool;

  @Test
  @DisplayName("getProcedureDetail with valid ID returns procedure with document requirements")
  void getProcedureDetail_validId_returnsProcedureWithDocuments() {
    var result = procedureDetailTool.getProcedureDetail(2L);

    assertThat(result.id()).isEqualTo(2L);
    assertThat(result.title()).isEqualTo("Apply for skilled worker residence permit via UDI");
    assertThat(result.authority()).isEqualTo("UDI");
    assertThat(result.estimatedDays()).isEqualTo(30);

    // Procedure 2 has 3 document requirements: Valid passport, Employment contract, Proof of
    // qualifications
    assertThat(result.documents()).hasSize(3);
    assertThat(result.documents())
        .extracting(DocumentItem::documentName)
        .containsExactlyInAnyOrder(
            "Valid passport", "Employment contract", "Proof of qualifications");
    assertThat(result.documents()).allSatisfy(doc -> assertThat(doc.mandatory()).isTrue());
  }

  @Test
  @DisplayName("getProcedureDetail returns both mandatory and optional document requirements")
  void getProcedureDetail_mixedMandatory_returnsBothTypes() {
    // Procedure 13 (family reunification application) has 3 mandatory + 1 optional document
    var result = procedureDetailTool.getProcedureDetail(13L);

    assertThat(result.id()).isEqualTo(13L);
    assertThat(result.documents()).hasSize(4);

    var mandatoryDocs = result.documents().stream().filter(DocumentItem::mandatory).toList();
    var optionalDocs = result.documents().stream().filter(d -> !d.mandatory()).toList();
    assertThat(mandatoryDocs).hasSize(3);
    assertThat(optionalDocs).hasSize(1);
    assertThat(optionalDocs.getFirst().documentName()).isEqualTo("Housing documentation");
  }

  @Test
  @DisplayName("getProcedureDetail with procedure having no description returns null description")
  void getProcedureDetail_noDescription_returnsNullDescription() {
    // Seed data inserts procedures without descriptions
    var result = procedureDetailTool.getProcedureDetail(1L);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.title()).isEqualTo("Receive job offer from Norwegian employer");
    assertThat(result.description()).isNull();
  }

  @Test
  @DisplayName("getProcedureDetail with non-existent ID throws EntityNotFoundException")
  void getProcedureDetail_nonExistentId_throwsEntityNotFoundException() {
    assertThatThrownBy(() -> procedureDetailTool.getProcedureDetail(9999L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("9999");
  }
}
