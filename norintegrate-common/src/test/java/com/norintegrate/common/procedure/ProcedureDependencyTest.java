package com.norintegrate.common.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProcedureDependencyTest {

  private Procedure buildProcedure(Long id, String title) throws Exception {
    var p = new Procedure(title, "desc", "Authority", 5);
    Field f = Procedure.class.getDeclaredField("id");
    f.setAccessible(true);
    f.set(p, id);
    return p;
  }

  @Test
  @DisplayName("constructor: self-dependency throws IllegalArgumentException")
  void constructor_selfDependency_throwsIllegalArgumentException() throws Exception {
    var proc = buildProcedure(1L, "Step 1");

    assertThatThrownBy(() -> new ProcedureDependency(proc, proc))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot depend on itself");
  }

  @Test
  @DisplayName("constructor: valid dependency sets prerequisite, dependent, and composite id")
  void constructor_validDependency_setsFieldsCorrectly() throws Exception {
    var prerequisite = buildProcedure(1L, "Step 1");
    var dependent = buildProcedure(2L, "Step 2");

    var dep = new ProcedureDependency(prerequisite, dependent);

    assertThat(dep.getPrerequisite()).isSameAs(prerequisite);
    assertThat(dep.getDependent()).isSameAs(dependent);
    assertThat(dep.getId().getPrerequisiteId()).isEqualTo(1L);
    assertThat(dep.getId().getDependentId()).isEqualTo(2L);
  }

  @Test
  @DisplayName("prePersist: sets createdAt timestamp")
  void prePersist_setsCreatedAt() throws Exception {
    var prerequisite = buildProcedure(1L, "Step 1");
    var dependent = buildProcedure(2L, "Step 2");
    var dep = new ProcedureDependency(prerequisite, dependent);

    assertThat(dep.getCreatedAt()).isNull();

    dep.prePersist();

    assertThat(dep.getCreatedAt()).isNotNull();
  }
}
