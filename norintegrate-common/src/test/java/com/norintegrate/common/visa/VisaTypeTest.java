package com.norintegrate.common.visa;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VisaTypeTest {

  @Test
  @DisplayName("constructor and getters return correct values")
  void constructorAndGetters_returnCorrectValues() {
    var vt = new VisaType("SKILLED_WORKER", "Skilled Worker", "For skilled workers");

    assertThat(vt.getId()).isEqualTo("SKILLED_WORKER");
    assertThat(vt.getName()).isEqualTo("Skilled Worker");
    assertThat(vt.getDescription()).isEqualTo("For skilled workers");
    assertThat(vt.getCreatedAt()).isNull();
  }

  @Test
  @DisplayName("setters update fields correctly")
  void setters_updateFieldsCorrectly() {
    var vt = new VisaType("ID", "Old Name", "Old Desc");

    vt.setName("New Name");
    vt.setDescription("New Description");

    assertThat(vt.getName()).isEqualTo("New Name");
    assertThat(vt.getDescription()).isEqualTo("New Description");
  }

  @Test
  @DisplayName("prePersist sets createdAt")
  void prePersist_setsCreatedAt() {
    var vt = new VisaType("ID", "Name", "Description");

    assertThat(vt.getCreatedAt()).isNull();

    vt.prePersist();

    assertThat(vt.getCreatedAt()).isNotNull();
  }
}
