package com.norintegrate.common.procedure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureDependencyRepository
    extends JpaRepository<ProcedureDependency, ProcedureDependencyId> {

  List<ProcedureDependency> findByIdPrerequisiteId(Long prerequisiteId);

  List<ProcedureDependency> findByIdDependentId(Long dependentId);
}
