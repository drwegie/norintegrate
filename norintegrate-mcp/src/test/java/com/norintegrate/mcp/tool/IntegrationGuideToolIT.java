package com.norintegrate.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.norintegrate.mcp.AbstractMcpIntegrationTest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("IntegrationGuideTool - Integration Tests")
class IntegrationGuideToolIT extends AbstractMcpIntegrationTest {

  @Autowired private IntegrationGuideTool integrationGuideTool;

  @Test
  @DisplayName("getIntegrationGuide with SKILLED_WORKER returns all 11 steps in dependency order")
  void getIntegrationGuide_skilledWorker_returnsOrderedSteps() {
    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", null);

    assertThat(result.visaTypeId()).isEqualTo("SKILLED_WORKER");
    assertThat(result.steps()).hasSize(11);

    // The first step should be "Receive job offer" (procedure 1) — it has no prerequisites
    var firstStep = result.steps().getFirst();
    assertThat(firstStep.procedureId()).isEqualTo(1L);
    assertThat(firstStep.title()).isEqualTo("Receive job offer from Norwegian employer");
    assertThat(firstStep.isNext()).isTrue();

    // Verify topological ordering: prerequisites appear before dependents
    var stepIds = result.steps().stream().map(ProcedureStep::procedureId).toList();
    // Procedure 1 must come before procedure 2 (1 -> 2)
    assertThat(stepIds.indexOf(1L)).isLessThan(stepIds.indexOf(2L));
    // Procedure 2 must come before procedure 4 (2 -> 4)
    assertThat(stepIds.indexOf(2L)).isLessThan(stepIds.indexOf(4L));
    // Procedure 4 must come before procedure 5 (4 -> 5)
    assertThat(stepIds.indexOf(4L)).isLessThan(stepIds.indexOf(5L));
    // Procedure 9 must come before procedure 10 (9 -> 10)
    assertThat(stepIds.indexOf(9L)).isLessThan(stepIds.indexOf(10L));
    // Procedure 10 must come before procedure 11 (10 -> 11)
    assertThat(stepIds.indexOf(10L)).isLessThan(stepIds.indexOf(11L));
  }

  @Test
  @DisplayName(
      "getIntegrationGuide with completed procedures filters them out and updates next steps")
  void getIntegrationGuide_withCompleted_filtersAndUpdatesNext() {
    // Mark procedure 1 as completed
    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "1");

    assertThat(result.steps()).hasSize(10);
    assertThat(result.steps().stream().map(ProcedureStep::procedureId)).doesNotContain(1L);

    // After completing procedure 1, procedure 2 should now be a next step
    var nextSteps = result.steps().stream().filter(ProcedureStep::isNext).toList();
    assertThat(nextSteps).extracting(ProcedureStep::procedureId).contains(2L);
  }

  @Test
  @DisplayName(
      "getIntegrationGuide with multiple completed procedures correctly computes remaining")
  void getIntegrationGuide_multipleCompleted_correctlyComputesRemaining() {
    // Mark procedures 1 and 2 as completed
    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "1,2");

    assertThat(result.steps()).hasSize(9);
    assertThat(result.steps().stream().map(ProcedureStep::procedureId)).doesNotContain(1L, 2L);

    // Both 3 (biometrics) and 4 (D-number) should be next — they both depend on 2
    var nextStepIds =
        result.steps().stream()
            .filter(ProcedureStep::isNext)
            .map(ProcedureStep::procedureId)
            .toList();
    assertThat(nextStepIds).contains(3L, 4L);
  }

  @Test
  @DisplayName("getIntegrationGuide with FAMILY_REUNIFICATION returns correct steps")
  void getIntegrationGuide_familyReunification_returnsCorrectSteps() {
    var result = integrationGuideTool.getIntegrationGuide("FAMILY_REUNIFICATION", null);

    assertThat(result.visaTypeId()).isEqualTo("FAMILY_REUNIFICATION");
    assertThat(result.steps()).hasSize(11);

    // First step for family reunification should be "Prepare documentation" (procedure 12)
    var firstStep = result.steps().getFirst();
    assertThat(firstStep.procedureId()).isEqualTo(12L);
    assertThat(firstStep.isNext()).isTrue();
  }

  @Test
  @DisplayName("getIntegrationGuide with invalid visa type throws EntityNotFoundException")
  void getIntegrationGuide_invalidVisaType_throwsEntityNotFoundException() {
    assertThatThrownBy(() -> integrationGuideTool.getIntegrationGuide("NONEXISTENT", null))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("NONEXISTENT");
  }

  @Test
  @DisplayName("getIntegrationGuide with blank visaTypeId throws IllegalArgumentException")
  void getIntegrationGuide_blankVisaTypeId_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> integrationGuideTool.getIntegrationGuide("  ", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("visaTypeId must not be blank");
  }

  @Test
  @DisplayName("getIntegrationGuide with null visaTypeId throws IllegalArgumentException")
  void getIntegrationGuide_nullVisaTypeId_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> integrationGuideTool.getIntegrationGuide(null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("visaTypeId must not be blank");
  }

  @Test
  @DisplayName("getIntegrationGuide steps contain correct authority and estimated days")
  void getIntegrationGuide_steps_containCorrectMetadata() {
    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", null);

    // Verify procedure 4 (D-number application) has correct metadata
    var dNumberStep =
        result.steps().stream().filter(s -> s.procedureId() == 4L).findFirst().orElseThrow();
    assertThat(dNumberStep.authority()).isEqualTo("Skatteetaten");
    assertThat(dNumberStep.estimatedDays()).isEqualTo(14);
  }
}
