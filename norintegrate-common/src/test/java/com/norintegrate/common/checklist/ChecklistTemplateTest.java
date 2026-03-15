package com.norintegrate.common.checklist;

import static org.assertj.core.api.Assertions.assertThat;

import com.norintegrate.common.procedure.Procedure;
import com.norintegrate.common.visa.VisaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChecklistTemplateTest {

  @Test
  @DisplayName("constructor and getters return correct values")
  void constructorAndGetters_returnCorrectValues() {
    var visa = new VisaType("SKILLED_WORKER", "Skilled Worker", "For skilled workers");
    var proc = new Procedure("Get D-nummer", "Apply for D-nummer", "Skatteetaten", 14);
    var template = new ChecklistTemplate(visa, proc, 1);

    assertThat(template.getVisaType()).isSameAs(visa);
    assertThat(template.getProcedure()).isSameAs(proc);
    assertThat(template.getDisplayOrder()).isEqualTo(1);
    assertThat(template.getId()).isNull();
    assertThat(template.getCreatedAt()).isNull();
  }

  @Test
  @DisplayName("setDisplayOrder updates display order")
  void setDisplayOrder_updatesDisplayOrder() {
    var visa = new VisaType("SKILLED_WORKER", "Skilled Worker", "For skilled workers");
    var proc = new Procedure("Get D-nummer", "Apply", "Skatteetaten", 14);
    var template = new ChecklistTemplate(visa, proc, 1);

    template.setDisplayOrder(5);

    assertThat(template.getDisplayOrder()).isEqualTo(5);
  }

  @Test
  @DisplayName("prePersist sets createdAt")
  void prePersist_setsCreatedAt() {
    var visa = new VisaType("SKILLED_WORKER", "Skilled Worker", "For skilled workers");
    var proc = new Procedure("Get D-nummer", "Apply", "Skatteetaten", 14);
    var template = new ChecklistTemplate(visa, proc, 1);

    template.prePersist();

    assertThat(template.getCreatedAt()).isNotNull();
  }
}
