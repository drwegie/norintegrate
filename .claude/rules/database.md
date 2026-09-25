---
paths:
  - "**/*.sql"
  - "**/*Repository.java"
  - "**/resources/db/**"
---

# Database (PostgreSQL)

7 tables total. Schema file: `docs/schema.sql`

| Table | Purpose |
|-------|---------|
| visa_type | Master data: visa categories (PK is VARCHAR, e.g. 'SKILLED_WORKER') |
| procedure | Settlement procedures (e.g. 'Get D-nummer') |
| procedure_dependency | DAG edges: prerequisite → dependent |
| document_requirement | Required documents per procedure |
| checklist_template | Visa type × procedure mapping with display order |
| app_user | Minimal OAuth user (UUID, provider, subject, email only) |
| user_progress | Per-user procedure completion tracking |

## Database Rules

- All timestamps are TIMESTAMPTZ
- updated_at uses database trigger (never set in application code)
- procedure_dependency has CHECK constraint preventing self-dependency
- user_progress has UNIQUE(user_id, procedure_id)
- app_user has UNIQUE(oauth_provider, oauth_subject)
- Use GENERATED ALWAYS AS IDENTITY for bigint PKs
- Use gen_random_uuid() for app_user PK
