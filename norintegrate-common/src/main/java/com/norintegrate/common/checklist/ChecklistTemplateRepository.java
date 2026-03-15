package com.norintegrate.common.checklist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

  List<ChecklistTemplate> findByVisaTypeIdOrderByDisplayOrder(String visaTypeId);
}
