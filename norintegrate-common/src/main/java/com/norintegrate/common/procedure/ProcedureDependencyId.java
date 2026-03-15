package com.norintegrate.common.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProcedureDependencyId implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Column(name = "prerequisite_id", nullable = false)
  private Long prerequisiteId;

  @Column(name = "dependent_id", nullable = false)
  private Long dependentId;

  protected ProcedureDependencyId() {}

  public ProcedureDependencyId(Long prerequisiteId, Long dependentId) {
    this.prerequisiteId = prerequisiteId;
    this.dependentId = dependentId;
  }

  public Long getPrerequisiteId() {
    return prerequisiteId;
  }

  public Long getDependentId() {
    return dependentId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProcedureDependencyId that)) return false;
    return Objects.equals(prerequisiteId, that.prerequisiteId)
        && Objects.equals(dependentId, that.dependentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prerequisiteId, dependentId);
  }
}
