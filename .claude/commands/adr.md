Scaffold an Architecture Decision Record (ADR) for the NorIntegrate project.

The user will provide an ADR number and title as arguments (e.g. `/adr 001 Java 8 to Java 25 LTS Migration`).

Parse the arguments: first token is the number (zero-pad to 3 digits), the rest is the title.

Create the file at: `docs/adr/ADR-{number}-{title-kebab-case}.md`

Use Michael Nygard's ADR template, pre-filled with NorIntegrate context:

---

# ADR-{number}: {Title}

**Status:** Proposed

**Date:** {today's date}

## Context

[This project migrates from the developer's familiar stack (Java 8, Spring 3, Spring Boot early versions, Oracle DB, Jenkins) to a modern stack. ADRs should explain WHY the new technology was chosen over the familiar one — not just WHAT was chosen. Write 2–4 sentences describing the problem or situation that forced this decision.]

## Decision

[State the decision clearly in one or two sentences. Start with "We will..." or "We have decided to..."]

## Consequences

### Positive
- [List benefits]

### Negative
- [List trade-offs or costs]

### Neutral
- [List things that change but are neither good nor bad]

---

After creating the file, print its path and the first 10 lines so the user can verify it looks right. Then tell the user to fill in the bracketed sections.
