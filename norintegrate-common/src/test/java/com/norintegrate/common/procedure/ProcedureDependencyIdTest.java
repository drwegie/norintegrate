package com.norintegrate.common.procedure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProcedureDependencyIdTest {

  @Test
  @DisplayName("equals: same prerequisiteId and dependentId → equal")
  void equals_sameIds_returnsTrue() {
    var id1 = new ProcedureDependencyId(1L, 2L);
    var id2 = new ProcedureDependencyId(1L, 2L);

    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  @DisplayName("equals: different prerequisiteId → not equal")
  void equals_differentPrerequisiteId_returnsFalse() {
    var id1 = new ProcedureDependencyId(1L, 2L);
    var id2 = new ProcedureDependencyId(3L, 2L);

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  @DisplayName("equals: different dependentId → not equal")
  void equals_differentDependentId_returnsFalse() {
    var id1 = new ProcedureDependencyId(1L, 2L);
    var id2 = new ProcedureDependencyId(1L, 3L);

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  @DisplayName("equals: same instance → equal")
  void equals_sameInstance_returnsTrue() {
    var id = new ProcedureDependencyId(1L, 2L);

    assertThat(id).isEqualTo(id);
  }

  @Test
  @DisplayName("equals: compared with different type → not equal")
  void equals_differentType_returnsFalse() {
    var id = new ProcedureDependencyId(1L, 2L);

    assertThat(id).isNotEqualTo("not a ProcedureDependencyId");
  }

  @Test
  @DisplayName("getters return correct values")
  void getters_returnCorrectValues() {
    var id = new ProcedureDependencyId(10L, 20L);

    assertThat(id.getPrerequisiteId()).isEqualTo(10L);
    assertThat(id.getDependentId()).isEqualTo(20L);
  }
}
