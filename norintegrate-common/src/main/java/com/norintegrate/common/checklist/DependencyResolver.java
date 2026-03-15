package com.norintegrate.common.checklist;

import com.norintegrate.common.procedure.Procedure;
import com.norintegrate.common.procedure.ProcedureDependencyRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DependencyResolver {

  private final ChecklistTemplateRepository checklistTemplateRepository;
  private final ProcedureDependencyRepository procedureDependencyRepository;

  public DependencyResolver(
      ChecklistTemplateRepository checklistTemplateRepository,
      ProcedureDependencyRepository procedureDependencyRepository) {
    this.checklistTemplateRepository = checklistTemplateRepository;
    this.procedureDependencyRepository = procedureDependencyRepository;
  }

  public List<ChecklistItem> resolve(String visaTypeId, Set<Long> completedIds) {
    var safeCompleted = completedIds == null ? Set.<Long>of() : completedIds;

    // Step 1: Load all ChecklistTemplate entries for the visa type (ordered by display_order)
    var templates = checklistTemplateRepository.findByVisaTypeIdOrderByDisplayOrder(visaTypeId);
    if (templates.isEmpty()) {
      return Collections.emptyList();
    }

    // Step 2: Extract the set of procedure IDs and build an ordered map of procedure by ID
    var procedureMap = new HashMap<Long, Procedure>();
    for (var template : templates) {
      var proc = template.getProcedure();
      procedureMap.put(proc.getId(), proc);
    }
    var checklistProcedureIds = procedureMap.keySet();

    // Step 3: Load all ProcedureDependency edges where both ends are in the checklist set
    // adjacency: prerequisiteId -> list of dependentIds
    var adjacency = new HashMap<Long, List<Long>>();
    // in-degree counting only incomplete prerequisites
    var inDegree = new HashMap<Long, Integer>();

    // Initialize in-degree for all procedures in the checklist
    for (var id : checklistProcedureIds) {
      inDegree.put(id, 0);
      adjacency.put(id, new ArrayList<>());
    }

    // Load edges and build graph
    for (var id : checklistProcedureIds) {
      var deps = procedureDependencyRepository.findByIdPrerequisiteId(id);
      for (var dep : deps) {
        var dependentId = dep.getId().getDependentId();
        // Only consider edges where both ends are in the checklist
        if (checklistProcedureIds.contains(dependentId)) {
          adjacency.get(id).add(dependentId);
          // Step 4/5: Count only incomplete prerequisites toward in-degree
          if (!safeCompleted.contains(id)) {
            inDegree.merge(dependentId, 1, Integer::sum);
          }
        }
      }
    }

    // Step 4: Identify remaining (incomplete) procedures
    var remainingIds = new HashSet<Long>();
    for (var id : checklistProcedureIds) {
      if (!safeCompleted.contains(id)) {
        remainingIds.add(id);
      }
    }

    // Step 5: Run Kahn's algorithm on the remaining procedures
    var queue = new ArrayDeque<Long>();
    var initialZeroInDegree = new HashSet<Long>();

    // Queue starts with incomplete procedures whose incomplete prerequisites are all done
    // (in-degree = 0)
    for (var id : remainingIds) {
      if (inDegree.get(id) == 0) {
        queue.add(id);
        initialZeroInDegree.add(id);
      }
    }

    var result = new ArrayList<ChecklistItem>();
    int processedCount = 0;

    while (!queue.isEmpty()) {
      var current = queue.poll();
      processedCount++;

      // Step 6: Mark as isNext if it was in the initial zero-in-degree set.
      // isNext=true only applies to procedures that were immediately actionable at the
      // start of the traversal (zero incomplete prerequisites from the beginning).
      // Procedures that become unblocked mid-traversal (as others are processed) are
      // NOT marked isNext, even though their in-degree reaches 0 during the loop.
      var isNext = initialZeroInDegree.contains(current);
      result.add(new ChecklistItem(procedureMap.get(current), isNext));

      // Decrement neighbors' in-degree and enqueue those that reach 0
      for (var neighborId : adjacency.get(current)) {
        if (remainingIds.contains(neighborId)) {
          var newDegree = inDegree.merge(neighborId, -1, Integer::sum);
          if (newDegree == 0) {
            queue.add(neighborId);
          }
        }
      }
    }

    // If processed count < remaining count → cycle detected
    if (processedCount < remainingIds.size()) {
      throw new CyclicDependencyException(visaTypeId);
    }

    return Collections.unmodifiableList(result);
  }
}
