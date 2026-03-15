package com.norintegrate.common.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcedureServiceTest {

  @Mock private ProcedureRepository procedureRepository;

  @Mock private ProcedureDependencyRepository procedureDependencyRepository;

  @Mock private DocumentRequirementRepository documentRequirementRepository;

  @InjectMocks private ProcedureService procedureService;

  // --- Helper ---

  private Procedure buildProcedure(Long id, String title) throws Exception {
    var p = new Procedure(title, "desc", "Authority", 5);
    Field f = Procedure.class.getDeclaredField("id");
    f.setAccessible(true);
    f.set(p, id);
    return p;
  }

  // --- findById ---

  @Test
  @DisplayName("findById: id not found → EntityNotFoundException")
  void findById_notFound_throwsEntityNotFoundException() {
    when(procedureRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> procedureService.findById(99L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("99");
  }

  // --- create ---

  @Test
  @DisplayName("create: saves a new Procedure and returns it")
  void create_validArgs_savesAndReturnsProcedure() {
    var procedure = new Procedure("Get D-nummer", "Apply for D-nummer", "Skatteetaten", 14);
    when(procedureRepository.save(any(Procedure.class))).thenReturn(procedure);

    var result = procedureService.create("Get D-nummer", "Apply for D-nummer", "Skatteetaten", 14);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Get D-nummer");
    assertThat(result.getAuthority()).isEqualTo("Skatteetaten");
    assertThat(result.getEstimatedDays()).isEqualTo(14);

    var captor = ArgumentCaptor.forClass(Procedure.class);
    verify(procedureRepository).save(captor.capture());
    assertThat(captor.getValue().getTitle()).isEqualTo("Get D-nummer");
  }

  // --- update ---

  @Test
  @DisplayName("update: updates all fields and returns saved entity")
  void update_existingProcedure_updatesFieldsAndReturnsSaved() throws Exception {
    var existing = buildProcedure(1L, "Old Title");
    when(procedureRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(procedureRepository.save(existing)).thenReturn(existing);

    var result = procedureService.update(1L, "New Title", "New Desc", "New Authority", 7);

    assertThat(result.getTitle()).isEqualTo("New Title");
    assertThat(result.getDescription()).isEqualTo("New Desc");
    assertThat(result.getAuthority()).isEqualTo("New Authority");
    assertThat(result.getEstimatedDays()).isEqualTo(7);
    verify(procedureRepository).save(existing);
  }

  // --- delete ---

  @Test
  @DisplayName("delete: id not found → EntityNotFoundException, no deleteById called")
  void delete_notFound_throwsEntityNotFoundException() {
    when(procedureRepository.existsById(55L)).thenReturn(false);

    assertThatThrownBy(() -> procedureService.delete(55L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("55");

    verify(procedureRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("delete: id exists → calls deleteById")
  void delete_exists_callsDeleteById() {
    when(procedureRepository.existsById(1L)).thenReturn(true);

    procedureService.delete(1L);

    verify(procedureRepository).deleteById(1L);
  }

  // --- addDependency ---

  @Test
  @DisplayName("addDependency: constructs ProcedureDependency with correct procedures and saves it")
  void addDependency_validProcedures_constructsAndSavesDependency() throws Exception {
    var prerequisite = buildProcedure(1L, "Step 1");
    var dependent = buildProcedure(2L, "Step 2");

    when(procedureRepository.findById(1L)).thenReturn(Optional.of(prerequisite));
    when(procedureRepository.findById(2L)).thenReturn(Optional.of(dependent));

    var savedDep = new ProcedureDependency(prerequisite, dependent);
    when(procedureDependencyRepository.save(any(ProcedureDependency.class))).thenReturn(savedDep);

    var result = procedureService.addDependency(1L, 2L);

    assertThat(result).isNotNull();
    assertThat(result.getPrerequisite()).isSameAs(prerequisite);
    assertThat(result.getDependent()).isSameAs(dependent);

    var captor = ArgumentCaptor.forClass(ProcedureDependency.class);
    verify(procedureDependencyRepository).save(captor.capture());
    assertThat(captor.getValue().getId().getPrerequisiteId()).isEqualTo(1L);
    assertThat(captor.getValue().getId().getDependentId()).isEqualTo(2L);
  }

  // --- removeDependency ---

  @Test
  @DisplayName("removeDependency: dependency not found → EntityNotFoundException")
  void removeDependency_notFound_throwsEntityNotFoundException() {
    var depId = new ProcedureDependencyId(1L, 2L);
    when(procedureDependencyRepository.existsById(depId)).thenReturn(false);

    assertThatThrownBy(() -> procedureService.removeDependency(1L, 2L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("1")
        .hasMessageContaining("2");

    verify(procedureDependencyRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("removeDependency: dependency exists → calls deleteById with correct id")
  void removeDependency_exists_callsDeleteById() {
    var depId = new ProcedureDependencyId(1L, 2L);
    when(procedureDependencyRepository.existsById(depId)).thenReturn(true);

    procedureService.removeDependency(1L, 2L);

    verify(procedureDependencyRepository).deleteById(depId);
  }

  // --- getDocumentRequirements ---

  @Test
  @DisplayName("getDocumentRequirements: procedure not found → EntityNotFoundException")
  void getDocumentRequirements_procedureNotFound_throwsEntityNotFoundException() {
    when(procedureRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> procedureService.getDocumentRequirements(99L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("99");

    verify(documentRequirementRepository, never()).findByProcedureId(any());
  }

  @Test
  @DisplayName("getDocumentRequirements: procedure exists → returns list from repository")
  void getDocumentRequirements_procedureExists_returnsList() throws Exception {
    var procedure = buildProcedure(1L, "Step 1");
    var doc1 = new DocumentRequirement(procedure, "Passport", "Valid passport", true);
    var doc2 = new DocumentRequirement(procedure, "Photo", "Recent photo", false);

    when(procedureRepository.findById(1L)).thenReturn(Optional.of(procedure));
    when(documentRequirementRepository.findByProcedureId(1L)).thenReturn(List.of(doc1, doc2));

    var result = procedureService.getDocumentRequirements(1L);

    assertThat(result).containsExactly(doc1, doc2);
  }
}
