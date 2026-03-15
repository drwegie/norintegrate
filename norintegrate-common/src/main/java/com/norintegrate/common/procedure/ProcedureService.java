package com.norintegrate.common.procedure;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcedureService {

  private final ProcedureRepository procedureRepository;
  private final ProcedureDependencyRepository procedureDependencyRepository;
  private final DocumentRequirementRepository documentRequirementRepository;

  public ProcedureService(
      ProcedureRepository procedureRepository,
      ProcedureDependencyRepository procedureDependencyRepository,
      DocumentRequirementRepository documentRequirementRepository) {
    this.procedureRepository = procedureRepository;
    this.procedureDependencyRepository = procedureDependencyRepository;
    this.documentRequirementRepository = documentRequirementRepository;
  }

  @Transactional(readOnly = true)
  public List<Procedure> findAll() {
    return procedureRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Procedure findById(Long id) {
    return procedureRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Procedure not found: " + id));
  }

  @Transactional
  public Procedure create(
      String title, String description, String authority, Integer estimatedDays) {
    var procedure = new Procedure(title, description, authority, estimatedDays);
    return procedureRepository.save(procedure);
  }

  @Transactional
  public Procedure update(
      Long id, String title, String description, String authority, Integer estimatedDays) {
    var procedure = findById(id);
    procedure.setTitle(title);
    procedure.setDescription(description);
    procedure.setAuthority(authority);
    procedure.setEstimatedDays(estimatedDays);
    return procedureRepository.save(procedure);
  }

  @Transactional
  public void delete(Long id) {
    if (!procedureRepository.existsById(id)) {
      throw new EntityNotFoundException("Procedure not found: " + id);
    }
    procedureRepository.deleteById(id);
  }

  @Transactional
  public ProcedureDependency addDependency(Long prerequisiteId, Long dependentId) {
    var prerequisite = findById(prerequisiteId);
    var dependent = findById(dependentId);
    var dependency = new ProcedureDependency(prerequisite, dependent);
    return procedureDependencyRepository.save(dependency);
  }

  @Transactional
  public void removeDependency(Long prerequisiteId, Long dependentId) {
    var depId = new ProcedureDependencyId(prerequisiteId, dependentId);
    if (!procedureDependencyRepository.existsById(depId)) {
      throw new EntityNotFoundException(
          "ProcedureDependency not found: prerequisiteId="
              + prerequisiteId
              + ", dependentId="
              + dependentId);
    }
    procedureDependencyRepository.deleteById(depId);
  }

  @Transactional(readOnly = true)
  public List<DocumentRequirement> getDocumentRequirements(Long procedureId) {
    findById(procedureId);
    return documentRequirementRepository.findByProcedureId(procedureId);
  }
}
