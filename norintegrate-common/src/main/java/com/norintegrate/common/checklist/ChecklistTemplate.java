package com.norintegrate.common.checklist;

import com.norintegrate.common.procedure.Procedure;
import com.norintegrate.common.visa.VisaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "checklist_template",
    uniqueConstraints = @UniqueConstraint(columnNames = {"visa_type_id", "procedure_id"}))
public class ChecklistTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "visa_type_id", nullable = false)
  private VisaType visaType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "procedure_id", nullable = false)
  private Procedure procedure;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected ChecklistTemplate() {}

  public ChecklistTemplate(VisaType visaType, Procedure procedure, Integer displayOrder) {
    this.visaType = visaType;
    this.procedure = procedure;
    this.displayOrder = displayOrder;
  }

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public VisaType getVisaType() {
    return visaType;
  }

  public Procedure getProcedure() {
    return procedure;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
