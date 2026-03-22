# ADR-012: Frontend Technology Choice — Next.js with Auth.js

## Status

Accepted

## Context

NorIntegrate has a complete REST API and MCP server but no user interface. The API supports OAuth 2.0 JWT authentication (Google provider), and the domain logic is fully encapsulated in the backend. A frontend is needed to demonstrate the full user flow: browsing visa checklists, signing in via Google OAuth, and tracking progress.

The developer's background is Java-centric (15 years). The frontend choice must balance modern capability with approachability:

- **Server-rendered HTML (Thymeleaf/HTMX):** Familiar to Spring developers but produces a monolithic deployment and limits interactivity.
- **React SPA (Vite + React Router):** Lightweight, but requires manual handling of SSR, routing, and auth.
- **Next.js (App Router):** Full-stack React framework with built-in SSR, file-based routing, and the Auth.js ecosystem for OAuth.

## Decision

Use **Next.js 15** with the App Router, **TypeScript**, **Tailwind CSS v4**, and **Auth.js v5** (Google provider only for MVP).

Key design choices:

1. **Separate deployment** — `norintegrate-web/` is a standalone Node.js project, not a Gradle module. It communicates with the API over HTTP, maintaining the clean separation established in ADR-003.

2. **Auth.js v5 with Google OAuth** — Auth.js handles the authorization code flow and obtains a Google `id_token`. This JWT is forwarded to the API as a Bearer token. The API already validates Google JWTs via its configured `issuer-uri`. GitHub OAuth is deferred because GitHub returns opaque access tokens that cannot be validated as JWTs.

3. **Server Components + Client Components** — Public pages (landing, visa type listing) use Server Components for fast initial load. The checklist detail page uses Client Components for interactive progress toggling.

4. **Standalone output** — `output: "standalone"` in Next.js config produces a minimal Node.js server suitable for Docker deployment.

## Consequences

### Positive

- Full OAuth flow demonstrated end-to-end without backend changes
- File-based routing eliminates boilerplate
- Server Components reduce client-side JavaScript for public pages
- Auth.js handles token management, CSRF, and session lifecycle
- TypeScript catches API contract mismatches at build time
- Tailwind CSS v4 provides utility-first styling with zero configuration

### Negative

- Adds Node.js to the project's technology stack (previously Java-only)
- Auth.js v5 is still in beta — API surface may change
- GitHub OAuth requires a future token-exchange endpoint on the API side
- Frontend developers need familiarity with both React Server Components and Client Components
