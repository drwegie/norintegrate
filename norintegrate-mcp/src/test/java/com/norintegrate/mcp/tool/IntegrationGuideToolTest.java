package com.norintegrate.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norintegrate.common.checklist.ChecklistItem;
import com.norintegrate.common.checklist.ChecklistService;
import com.norintegrate.common.procedure.Procedure;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("IntegrationGuideTool")
class IntegrationGuideToolTest {

  @Mock private ChecklistService checklistService;

  @InjectMocks private IntegrationGuideTool integrationGuideTool;

  @Test
  @DisplayName("getIntegrationGuide returns correctly mapped steps for a valid visa type")
  void getIntegrationGuide_validVisaType_returnsMappedSteps() {
    var procedure1 = mockProcedure(1L, "Get D-nummer", "Tax Office", 14);
    var procedure2 = mockProcedure(2L, "Open bank account", "Bank", 7);

    var items = List.of(new ChecklistItem(procedure1, true), new ChecklistItem(procedure2, false));

    when(checklistService.getChecklist(eq("SKILLED_WORKER"), eq(Set.of()))).thenReturn(items);

    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", null);

    assertThat(result.visaTypeId()).isEqualTo("SKILLED_WORKER");
    assertThat(result.steps()).hasSize(2);

    var step1 = result.steps().get(0);
    assertThat(step1.procedureId()).isEqualTo(1L);
    assertThat(step1.title()).isEqualTo("Get D-nummer");
    assertThat(step1.authority()).isEqualTo("Tax Office");
    assertThat(step1.estimatedDays()).isEqualTo(14);
    assertThat(step1.isNext()).isTrue();

    var step2 = result.steps().get(1);
    assertThat(step2.procedureId()).isEqualTo(2L);
    assertThat(step2.title()).isEqualTo("Open bank account");
    assertThat(step2.isNext()).isFalse();
  }

  @Test
  @DisplayName("getIntegrationGuide with null visaTypeId throws IllegalArgumentException")
  void getIntegrationGuide_nullVisaTypeId_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> integrationGuideTool.getIntegrationGuide(null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("visaTypeId must not be blank");
  }

  @Test
  @DisplayName("getIntegrationGuide with blank visaTypeId throws IllegalArgumentException")
  void getIntegrationGuide_blankVisaTypeId_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> integrationGuideTool.getIntegrationGuide("   ", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("visaTypeId must not be blank");
  }

  @Test
  @DisplayName("getIntegrationGuide with non-numeric completedIds throws IllegalArgumentException")
  void getIntegrationGuide_invalidCompletedIds_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "1,abc,3"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid procedure id: abc");
  }

  @Test
  @DisplayName("getIntegrationGuide passes parsed completed IDs to service")
  void getIntegrationGuide_withCompletedIds_passesToService() {
    when(checklistService.getChecklist(eq("SKILLED_WORKER"), eq(Set.of(1L, 3L))))
        .thenReturn(List.of());

    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "1, 3");

    verify(checklistService).getChecklist("SKILLED_WORKER", Set.of(1L, 3L));
    assertThat(result.steps()).isEmpty();
  }

  @Test
  @DisplayName("getIntegrationGuide with empty completedIds string passes empty set")
  void getIntegrationGuide_emptyCompletedIds_passesEmptySet() {
    when(checklistService.getChecklist(eq("SKILLED_WORKER"), eq(Set.of()))).thenReturn(List.of());

    var result = integrationGuideTool.getIntegrationGuide("SKILLED_WORKER", "");

    verify(checklistService).getChecklist("SKILLED_WORKER", Set.of());
    assertThat(result.steps()).isEmpty();
  }

  private Procedure mockProcedure(long id, String title, String authority, int estimatedDays) {
    var procedure = mock(Procedure.class);
    when(procedure.getId()).thenReturn(id);
    when(procedure.getTitle()).thenReturn(title);
    when(procedure.getAuthority()).thenReturn(authority);
    when(procedure.getEstimatedDays()).thenReturn(estimatedDays);
    return procedure;
  }
}
