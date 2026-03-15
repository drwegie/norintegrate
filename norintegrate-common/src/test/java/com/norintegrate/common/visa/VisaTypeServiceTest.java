package com.norintegrate.common.visa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VisaTypeServiceTest {

  @Mock private VisaTypeRepository visaTypeRepository;

  @InjectMocks private VisaTypeService visaTypeService;

  @Test
  @DisplayName("findAll: returns list from repository")
  void findAll_always_returnsListFromRepository() {
    var visaType1 = new VisaType("SKILLED_WORKER", "Skilled Worker", "For skilled workers");
    var visaType2 = new VisaType("STUDENT", "Student", "For students");
    when(visaTypeRepository.findAll()).thenReturn(List.of(visaType1, visaType2));

    var result = visaTypeService.findAll();

    assertThat(result).containsExactly(visaType1, visaType2);
  }

  @Test
  @DisplayName("findAll: returns empty list when no visa types exist")
  void findAll_noVisaTypes_returnsEmptyList() {
    when(visaTypeRepository.findAll()).thenReturn(List.of());

    var result = visaTypeService.findAll();

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findById: found → returns VisaType")
  void findById_found_returnsVisaType() {
    var visaType = new VisaType("SKILLED_WORKER", "Skilled Worker", "For skilled workers");
    when(visaTypeRepository.findById("SKILLED_WORKER")).thenReturn(Optional.of(visaType));

    var result = visaTypeService.findById("SKILLED_WORKER");

    assertThat(result).isSameAs(visaType);
    assertThat(result.getId()).isEqualTo("SKILLED_WORKER");
    assertThat(result.getName()).isEqualTo("Skilled Worker");
  }

  @Test
  @DisplayName("findById: not found → EntityNotFoundException")
  void findById_notFound_throwsEntityNotFoundException() {
    when(visaTypeRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> visaTypeService.findById("NONEXISTENT"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("NONEXISTENT");
  }
}
