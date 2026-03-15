package com.norintegrate.common.procedure;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EntityTest {

  @Nested
  @DisplayName("Procedure entity")
  class ProcedureEntityTest {

    @Test
    @DisplayName("constructor and getters return correct values")
    void constructorAndGetters_returnCorrectValues() {
      var proc = new Procedure("Title", "Description", "Authority", 14);

      assertThat(proc.getTitle()).isEqualTo("Title");
      assertThat(proc.getDescription()).isEqualTo("Description");
      assertThat(proc.getAuthority()).isEqualTo("Authority");
      assertThat(proc.getEstimatedDays()).isEqualTo(14);
      assertThat(proc.getId()).isNull();
      assertThat(proc.getCreatedAt()).isNull();
      assertThat(proc.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("setters update fields correctly")
    void setters_updateFieldsCorrectly() {
      var proc = new Procedure("Old", "Old desc", "Old auth", 1);

      proc.setTitle("New Title");
      proc.setDescription("New Description");
      proc.setAuthority("New Authority");
      proc.setEstimatedDays(30);

      assertThat(proc.getTitle()).isEqualTo("New Title");
      assertThat(proc.getDescription()).isEqualTo("New Description");
      assertThat(proc.getAuthority()).isEqualTo("New Authority");
      assertThat(proc.getEstimatedDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("prePersist sets createdAt")
    void prePersist_setsCreatedAt() {
      var proc = new Procedure("Title", "Desc", "Auth", 5);

      assertThat(proc.getCreatedAt()).isNull();

      proc.prePersist();

      assertThat(proc.getCreatedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("DocumentRequirement entity")
  class DocumentRequirementEntityTest {

    private Procedure buildProcedure(Long id) throws Exception {
      var p = new Procedure("Test", "desc", "Auth", 5);
      Field f = Procedure.class.getDeclaredField("id");
      f.setAccessible(true);
      f.set(p, id);
      return p;
    }

    @Test
    @DisplayName("constructor and getters return correct values")
    void constructorAndGetters_returnCorrectValues() throws Exception {
      var proc = buildProcedure(1L);
      var doc = new DocumentRequirement(proc, "Passport", "Valid passport", true);

      assertThat(doc.getProcedure()).isSameAs(proc);
      assertThat(doc.getDocumentName()).isEqualTo("Passport");
      assertThat(doc.getDescription()).isEqualTo("Valid passport");
      assertThat(doc.isMandatory()).isTrue();
      assertThat(doc.getId()).isNull();
      assertThat(doc.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("setters update fields correctly")
    void setters_updateFieldsCorrectly() throws Exception {
      var proc = buildProcedure(1L);
      var doc = new DocumentRequirement(proc, "Old Name", "Old Desc", true);

      doc.setDocumentName("New Name");
      doc.setDescription("New Description");
      doc.setMandatory(false);

      assertThat(doc.getDocumentName()).isEqualTo("New Name");
      assertThat(doc.getDescription()).isEqualTo("New Description");
      assertThat(doc.isMandatory()).isFalse();
    }

    @Test
    @DisplayName("prePersist sets createdAt")
    void prePersist_setsCreatedAt() throws Exception {
      var proc = buildProcedure(1L);
      var doc = new DocumentRequirement(proc, "Passport", "Valid", true);

      doc.prePersist();

      assertThat(doc.getCreatedAt()).isNotNull();
    }
  }
}
