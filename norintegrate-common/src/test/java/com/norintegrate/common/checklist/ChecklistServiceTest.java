package com.norintegrate.common.checklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norintegrate.common.visa.VisaType;
import com.norintegrate.common.visa.VisaTypeService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

  @Mock private DependencyResolver dependencyResolver;

  @Mock private VisaTypeService visaTypeService;

  @InjectMocks private ChecklistService checklistService;

  @Test
  @DisplayName("getChecklist: visa type not found → EntityNotFoundException")
  void getChecklist_visaTypeNotFound_throwsEntityNotFoundException() {
    var visaTypeId = "UNKNOWN_VISA";
    var completedIds = Set.<Long>of();
    when(visaTypeService.findById(visaTypeId))
        .thenThrow(new EntityNotFoundException("VisaType not found: " + visaTypeId));

    assertThatThrownBy(() -> checklistService.getChecklist(visaTypeId, completedIds))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(visaTypeId);
  }

  @Test
  @DisplayName("getChecklist: delegates to DependencyResolver with correct args")
  void getChecklist_validVisaType_delegatesToDependencyResolverWithCorrectArgs() {
    var visaTypeId = "SKILLED_WORKER";
    var completedIds = Set.of(1L, 2L);
    var visaType = new VisaType(visaTypeId, "Skilled Worker", "For skilled workers");
    var expectedItems = List.of(new ChecklistItem(null, true), new ChecklistItem(null, false));

    when(visaTypeService.findById(visaTypeId)).thenReturn(visaType);
    when(dependencyResolver.resolve(visaTypeId, completedIds)).thenReturn(expectedItems);

    var result = checklistService.getChecklist(visaTypeId, completedIds);

    assertThat(result).isSameAs(expectedItems);
    verify(visaTypeService).findById(visaTypeId);
    verify(dependencyResolver).resolve(visaTypeId, completedIds);
  }

  @Test
  @DisplayName("getChecklist: DependencyResolver returns empty list → returns empty list")
  void getChecklist_dependencyResolverReturnsEmpty_returnsEmptyList() {
    var visaTypeId = "STUDENT";
    var completedIds = Set.<Long>of();
    var visaType = new VisaType(visaTypeId, "Student", "Student visa");

    when(visaTypeService.findById(visaTypeId)).thenReturn(visaType);
    when(dependencyResolver.resolve(visaTypeId, completedIds)).thenReturn(Collections.emptyList());

    var result = checklistService.getChecklist(visaTypeId, completedIds);

    assertThat(result).isEmpty();
  }
}
