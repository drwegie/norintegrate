# ADR-015: Three-Layer Frontend Testing Strategy with Playwright

**Status:** Accepted

**Date:** 2026-04-05

## Context

The Next.js frontend (`norintegrate-web`) initially had only Vitest unit tests that mocked `fetch` and `next-auth/react` to verify component rendering and API wrapper logic. While these caught regressions in isolated units, they could not verify that the frontend actually worked end-to-end in a real browser — Server Components fetching live data, client-side navigation, and interactive UI flows like expanding checklist items were untested. Vitest-based integration tests were added to validate the API contract via `lib/api.ts` against a running backend, but these still ran in Node.js without a browser. A gap remained: no test verified what the user actually sees and interacts with.

## Decision

We have decided to adopt a three-layer frontend testing strategy:

| Layer | Tool | Environment | What it tests |
|-------|------|-------------|---------------|
| Unit | Vitest + jsdom + React Testing Library | Node.js (jsdom) | Component rendering, API wrappers, mocked dependencies |
| Integration | Vitest + Node.js | Node.js | API contract validation against a live backend |
| E2E | Playwright (Chromium) | Real browser | Full user flows — navigation, data loading, UI interactions |

Playwright was chosen over Cypress because it supports multiple browser engines, has first-class auto-waiting, and integrates well with Next.js via its `webServer` configuration. Each layer serves a distinct purpose: unit tests are fast and isolated, integration tests validate the API contract without browser overhead, and E2E tests prove the application works as a user would experience it.

## Consequences

### Positive
- Full confidence pyramid: fast unit tests catch logic bugs, integration tests catch API contract drift, E2E tests catch rendering and navigation issues
- Playwright auto-starts the Next.js dev server, making E2E tests self-contained
- Each layer can run independently — unit tests need no infrastructure, integration and E2E tests need the API + PostgreSQL
- Chromium-only keeps CI fast; additional browsers can be added later

### Negative
- E2E tests require a running API and PostgreSQL, so they cannot run in the current GitHub Actions pipeline without additional infrastructure (same limitation as integration tests)
- Adding Playwright increases `devDependencies` and requires browser binaries (~260 MB for Chromium)
- Three test configs to maintain (`vitest.config.ts`, `vitest.config.integration.ts`, `playwright.config.ts`)

### Neutral
- The `npm test` command continues to run only unit tests; integration and E2E tests are opt-in via `npm run test:integration` and `npm run test:e2e`
- Playwright artifacts (`test-results/`, `playwright-report/`) are gitignored
