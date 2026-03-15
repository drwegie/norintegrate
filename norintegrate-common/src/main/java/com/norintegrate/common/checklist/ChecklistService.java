package com.norintegrate.common.checklist;

import com.norintegrate.common.visa.VisaTypeService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChecklistService {

  private final DependencyResolver dependencyResolver;
  private final VisaTypeService visaTypeService;

  public ChecklistService(DependencyResolver dependencyResolver, VisaTypeService visaTypeService) {
    this.dependencyResolver = dependencyResolver;
    this.visaTypeService = visaTypeService;
  }

  @Transactional(readOnly = true)
  public List<ChecklistItem> getChecklist(String visaTypeId, Set<Long> completedIds) {
    visaTypeService.findById(visaTypeId);
    return dependencyResolver.resolve(visaTypeId, completedIds);
  }
}
