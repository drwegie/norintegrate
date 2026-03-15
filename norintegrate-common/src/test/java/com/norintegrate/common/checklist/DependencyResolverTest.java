package com.norintegrate.common.checklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.norintegrate.common.procedure.Procedure;
import com.norintegrate.common.procedure.ProcedureDependency;
import com.norintegrate.common.procedure.ProcedureDependencyRepository;
import com.norintegrate.common.visa.VisaType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DependencyResolverTest {

  @Mock private ChecklistTemplateRepository checklistTemplateRepository;

  @Mock private ProcedureDependencyRepository procedureDependencyRepository;

  private DependencyResolver resolver;

  private static final String VISA_TYPE_ID = "SKILLED_WORKER";

  @BeforeEach
  void setUp() {
    resolver = new DependencyResolver(checklistTemplateRepository, procedureDependencyRepository);
  }

  // -------------------------------------------------------------------------
  // Helper: create a Procedure with its id set via reflection
  // -------------------------------------------------------------------------

  private Procedure procedure(long id, String title) {
    var proc = new Procedure(title, "desc", "authority", 5);
    try {
      var idField = Procedure.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(proc, id);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Could not set id on Procedure", e);
    }
    return proc;
  }

  private VisaType visaType() {
    return new VisaType(VISA_TYPE_ID, "Skilled Worker", "Skilled worker visa");
  }

  private ChecklistTemplate template(VisaType visa, Procedure proc, int displayOrder) {
    return new ChecklistTemplate(visa, proc, displayOrder);
  }

  // -------------------------------------------------------------------------
  // Scenario 1: Empty checklist — no templates found → returns empty list
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_emptyChecklist_returnsEmptyList")
  void resolve_emptyChecklist_returnsEmptyList() {
    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(Collections.emptyList());

    var result = resolver.resolve(VISA_TYPE_ID, Set.of());

    assertThat(result).isEmpty();
  }

  // -------------------------------------------------------------------------
  // Scenario 2: Single procedure, no dependencies, not completed → isNext=true
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_singleProcedureNoDependenciesNotCompleted_returnsOneItemIsNextTrue")
  void resolve_singleProcedureNoDependenciesNotCompleted_returnsOneItemIsNextTrue() {
    var visa = visaType();
    var procA = procedure(1L, "A");

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(List.of(template(visa, procA, 1)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());

    var result = resolver.resolve(VISA_TYPE_ID, Set.of());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).procedure()).isEqualTo(procA);
    assertThat(result.get(0).isNext()).isTrue();
  }

  // -------------------------------------------------------------------------
  // Scenario 3: Single procedure, already completed → returns empty list
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_singleProcedureAlreadyCompleted_returnsEmptyList")
  void resolve_singleProcedureAlreadyCompleted_returnsEmptyList() {
    var visa = visaType();
    var procA = procedure(1L, "A");

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(List.of(template(visa, procA, 1)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());

    var result = resolver.resolve(VISA_TYPE_ID, Set.of(1L));

    assertThat(result).isEmpty();
  }

  // -------------------------------------------------------------------------
  // Scenario 4: Linear chain A→B→C, none completed
  //   A has isNext=true; B and C have isNext=false
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_linearChainNoneCompleted_aIsNextBCNotNext")
  void resolve_linearChainNoneCompleted_aIsNextBCNotNext() {
    var visa = visaType();
    var procA = procedure(1L, "A");
    var procB = procedure(2L, "B");
    var procC = procedure(3L, "C");

    var depAtoB = new ProcedureDependency(procA, procB);
    var depBtoC = new ProcedureDependency(procB, procC);

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(
            List.of(template(visa, procA, 1), template(visa, procB, 2), template(visa, procC, 3)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(procedureDependencyRepository.findByIdPrerequisiteId(1L)).thenReturn(List.of(depAtoB));
    when(procedureDependencyRepository.findByIdPrerequisiteId(2L)).thenReturn(List.of(depBtoC));

    var result = resolver.resolve(VISA_TYPE_ID, Set.of());

    assertThat(result).hasSize(3);

    // A must be first and isNext=true
    var itemA = result.stream().filter(i -> i.procedure().getId() == 1L).findFirst().orElseThrow();
    var itemB = result.stream().filter(i -> i.procedure().getId() == 2L).findFirst().orElseThrow();
    var itemC = result.stream().filter(i -> i.procedure().getId() == 3L).findFirst().orElseThrow();

    assertThat(itemA.isNext()).isTrue();
    assertThat(itemB.isNext()).isFalse();
    assertThat(itemC.isNext()).isFalse();

    // Topological order: A before B, B before C
    assertThat(result.indexOf(itemA)).isLessThan(result.indexOf(itemB));
    assertThat(result.indexOf(itemB)).isLessThan(result.indexOf(itemC));
  }

  // -------------------------------------------------------------------------
  // Scenario 5: Linear chain A→B→C, A completed → B isNext=true, C isNext=false
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_linearChainACompleted_bIsNextCNotNext")
  void resolve_linearChainACompleted_bIsNextCNotNext() {
    var visa = visaType();
    var procA = procedure(1L, "A");
    var procB = procedure(2L, "B");
    var procC = procedure(3L, "C");

    var depAtoB = new ProcedureDependency(procA, procB);
    var depBtoC = new ProcedureDependency(procB, procC);

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(
            List.of(template(visa, procA, 1), template(visa, procB, 2), template(visa, procC, 3)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(procedureDependencyRepository.findByIdPrerequisiteId(1L)).thenReturn(List.of(depAtoB));
    when(procedureDependencyRepository.findByIdPrerequisiteId(2L)).thenReturn(List.of(depBtoC));

    var result = resolver.resolve(VISA_TYPE_ID, Set.of(1L));

    assertThat(result).hasSize(2);

    var itemB = result.stream().filter(i -> i.procedure().getId() == 2L).findFirst().orElseThrow();
    var itemC = result.stream().filter(i -> i.procedure().getId() == 3L).findFirst().orElseThrow();

    assertThat(itemB.isNext()).isTrue();
    assertThat(itemC.isNext()).isFalse();

    assertThat(result.indexOf(itemB)).isLessThan(result.indexOf(itemC));
  }

  // -------------------------------------------------------------------------
  // Scenario 6: Linear chain A→B→C, A and B completed → C isNext=true
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_linearChainAandBCompleted_cIsNext")
  void resolve_linearChainAandBCompleted_cIsNext() {
    var visa = visaType();
    var procA = procedure(1L, "A");
    var procB = procedure(2L, "B");
    var procC = procedure(3L, "C");

    var depAtoB = new ProcedureDependency(procA, procB);
    var depBtoC = new ProcedureDependency(procB, procC);

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(
            List.of(template(visa, procA, 1), template(visa, procB, 2), template(visa, procC, 3)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(procedureDependencyRepository.findByIdPrerequisiteId(1L)).thenReturn(List.of(depAtoB));
    when(procedureDependencyRepository.findByIdPrerequisiteId(2L)).thenReturn(List.of(depBtoC));

    var result = resolver.resolve(VISA_TYPE_ID, Set.of(1L, 2L));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).procedure()).isEqualTo(procC);
    assertThat(result.get(0).isNext()).isTrue();
  }

  // -------------------------------------------------------------------------
  // Scenario 7: Diamond A→B, A→C, B→D, C→D, none completed
  //   A is next; B and C follow (isNext=false); D is last (isNext=false)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_diamondNoneCompleted_aIsNextBCDNotNext")
  void resolve_diamondNoneCompleted_aIsNextBCDNotNext() {
    var visa = visaType();
    var procA = procedure(1L, "A");
    var procB = procedure(2L, "B");
    var procC = procedure(3L, "C");
    var procD = procedure(4L, "D");

    var depAtoB = new ProcedureDependency(procA, procB);
    var depAtoC = new ProcedureDependency(procA, procC);
    var depBtoD = new ProcedureDependency(procB, procD);
    var depCtoD = new ProcedureDependency(procC, procD);

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(
            List.of(
                template(visa, procA, 1),
                template(visa, procB, 2),
                template(visa, procC, 3),
                template(visa, procD, 4)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(procedureDependencyRepository.findByIdPrerequisiteId(1L))
        .thenReturn(List.of(depAtoB, depAtoC));
    when(procedureDependencyRepository.findByIdPrerequisiteId(2L)).thenReturn(List.of(depBtoD));
    when(procedureDependencyRepository.findByIdPrerequisiteId(3L)).thenReturn(List.of(depCtoD));

    var result = resolver.resolve(VISA_TYPE_ID, Set.of());

    assertThat(result).hasSize(4);

    var itemA = result.stream().filter(i -> i.procedure().getId() == 1L).findFirst().orElseThrow();
    var itemB = result.stream().filter(i -> i.procedure().getId() == 2L).findFirst().orElseThrow();
    var itemC = result.stream().filter(i -> i.procedure().getId() == 3L).findFirst().orElseThrow();
    var itemD = result.stream().filter(i -> i.procedure().getId() == 4L).findFirst().orElseThrow();

    // Only A was in the initial zero-in-degree set
    assertThat(itemA.isNext()).isTrue();
    assertThat(itemB.isNext()).isFalse();
    assertThat(itemC.isNext()).isFalse();
    assertThat(itemD.isNext()).isFalse();

    // A must precede B and C; both B and C must precede D
    assertThat(result.indexOf(itemA)).isLessThan(result.indexOf(itemB));
    assertThat(result.indexOf(itemA)).isLessThan(result.indexOf(itemC));
    assertThat(result.indexOf(itemB)).isLessThan(result.indexOf(itemD));
    assertThat(result.indexOf(itemC)).isLessThan(result.indexOf(itemD));
  }

  // -------------------------------------------------------------------------
  // Scenario 8: Parallel independent procedures (no edges) → all isNext=true
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_parallelIndependentProcedures_allAreNext")
  void resolve_parallelIndependentProcedures_allAreNext() {
    var visa = visaType();
    var procA = procedure(1L, "A");
    var procB = procedure(2L, "B");
    var procC = procedure(3L, "C");

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(
            List.of(template(visa, procA, 1), template(visa, procB, 2), template(visa, procC, 3)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());

    var result = resolver.resolve(VISA_TYPE_ID, Set.of());

    assertThat(result).hasSize(3);
    assertThat(result).allMatch(ChecklistItem::isNext);
    assertThat(result.stream().map(i -> i.procedure().getId()))
        .containsExactlyInAnyOrder(1L, 2L, 3L);
  }

  // -------------------------------------------------------------------------
  // Scenario 9: Null completedIds → treated same as empty set (no NPE)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_nullCompletedIds_treatedAsEmptyNoNpe")
  void resolve_nullCompletedIds_treatedAsEmptyNoNpe() {
    var visa = visaType();
    var procA = procedure(1L, "A");

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(List.of(template(visa, procA, 1)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());

    var result = resolver.resolve(VISA_TYPE_ID, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).procedure()).isEqualTo(procA);
    assertThat(result.get(0).isNext()).isTrue();
  }

  // -------------------------------------------------------------------------
  // Scenario 10: Cycle detection → throws CyclicDependencyException
  //   A→B, B→C, C→A (all incomplete)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("resolve_cyclicDependency_throwsCyclicDependencyException")
  void resolve_cyclicDependency_throwsCyclicDependencyException() {
    var visa = visaType();
    var procA = procedure(1L, "A");
    var procB = procedure(2L, "B");
    var procC = procedure(3L, "C");

    var depAtoB = new ProcedureDependency(procA, procB);
    var depBtoC = new ProcedureDependency(procB, procC);
    var depCtoA = new ProcedureDependency(procC, procA);

    when(checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(VISA_TYPE_ID))
        .thenReturn(
            List.of(template(visa, procA, 1), template(visa, procB, 2), template(visa, procC, 3)));
    when(procedureDependencyRepository.findByIdPrerequisiteId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(procedureDependencyRepository.findByIdPrerequisiteId(1L)).thenReturn(List.of(depAtoB));
    when(procedureDependencyRepository.findByIdPrerequisiteId(2L)).thenReturn(List.of(depBtoC));
    when(procedureDependencyRepository.findByIdPrerequisiteId(3L)).thenReturn(List.of(depCtoA));

    assertThatThrownBy(() -> resolver.resolve(VISA_TYPE_ID, Set.of()))
        .isInstanceOf(CyclicDependencyException.class)
        .hasMessageContaining(VISA_TYPE_ID);
  }
}
