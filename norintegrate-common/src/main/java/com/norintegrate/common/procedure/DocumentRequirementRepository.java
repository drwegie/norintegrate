package com.norintegrate.common.procedure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, Long> {

  List<DocumentRequirement> findByProcedureId(Long procedureId);
}
