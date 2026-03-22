# CLAUDE.md — norintegrate-common

Shared domain library. Not deployed independently.

---

## Package Design (Domain-Based)

Package-by-feature, NOT package-by-layer.

```
com.norintegrate.common
├── checklist/       ← ChecklistTemplate, ChecklistService, DependencyResolver
├── procedure/       ← Procedure, ProcedureDependency, DocumentRequirement, ProcedureService
├── visa/            ← VisaType, VisaTypeService
├── progress/        ← AppUser, UserProgress, ProgressService
└── municipality/    ← MunicipalityInfo (record), SsbKlassClient, MunicipalityService
```

---

## Code Style

- DTOs are ALWAYS Java records (never classes with getters/setters)
- Entities are JPA @Entity classes (records cannot be JPA entities)
- Use `var` when the type is obvious from the right side
- Use pattern matching in switch where applicable
- Use text blocks for multi-line strings
- No Lombok — Java records and IDE generation are sufficient

---

## Key Algorithm: DependencyResolver

Implements Kahn's algorithm for topological sort on the procedure DAG.

Input: visa type, optional set of completed procedure IDs
Output: ordered list of remaining procedures, with next recommended step(s)

Must detect and reject cyclic dependencies with CyclicDependencyException.

---

## Testing

- Unit tests for all services
- Test packages mirror main source packages
- Target: ≥ 80% coverage (enforced by JaCoCo)

---

## External API Calls

- SSB Klass API: call directly via RestClient, NO caching, NO local storage
- Never store data that is available from a stable external API
