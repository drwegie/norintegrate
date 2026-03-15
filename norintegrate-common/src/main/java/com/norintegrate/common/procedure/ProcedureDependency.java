package com.norintegrate.common.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "procedure_dependency")
public class ProcedureDependency {

  @EmbeddedId private ProcedureDependencyId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("prerequisiteId")
  @JoinColumn(name = "prerequisite_id")
  private Procedure prerequisite;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("dependentId")
  @JoinColumn(name = "dependent_id")
  private Procedure dependent;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected ProcedureDependency() {}

  public ProcedureDependency(Procedure prerequisite, Procedure dependent) {
    if (prerequisite.getId().equals(dependent.getId())) {
      throw new IllegalArgumentException("A procedure cannot depend on itself");
    }
    this.prerequisite = prerequisite;
    this.dependent = dependent;
    this.id = new ProcedureDependencyId(prerequisite.getId(), dependent.getId());
  }

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public ProcedureDependencyId getId() {
    return id;
  }

  public Procedure getPrerequisite() {
    return prerequisite;
  }

  public Procedure getDependent() {
    return dependent;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
