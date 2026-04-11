# CLAUDE.md — norintegrate-web

Next.js 15 frontend. Standalone Node.js project (not a Gradle module).

---

## Technology Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Next.js | 15 | App Router, standalone output |
| React | 19 | Server Components + Client Components |
| TypeScript | 5.x | Strict mode |
| Tailwind CSS | 4.x | Utility-first, via `@tailwindcss/postcss` |
| Auth.js | v5 | Google OAuth only (GitHub deferred — opaque tokens) |
| Node.js | 22 LTS | See `.nvmrc` |

---

## Project Structure

```
norintegrate-web/
├── app/                        App Router pages and layouts
│   ├── layout.tsx              Root layout with SessionProvider + Navbar
│   ├── page.tsx                Landing page
│   ├── globals.css             Tailwind import
│   ├── checklist/
│   │   ├── page.tsx            Visa type selector (Server Component)
│   │   └── [visaTypeId]/
│   │       └── page.tsx        Checklist with progress (Client Component)
│   └── api/auth/[...nextauth]/
│       └── route.ts            Auth.js route handler
├── components/
│   ├── Navbar.tsx              Login/logout, user email
│   ├── VisaTypeCard.tsx        Visa type selection card
│   └── ChecklistItem.tsx       Procedure step with checkbox
├── lib/
│   ├── api.ts                  Typed fetch wrappers for NorIntegrate API
│   └── auth.ts                 Auth.js config (Google provider)
├── __tests__/                  Vitest unit + integration tests
└── e2e/                        Playwright E2E tests
```

---

## Auth Flow

1. Auth.js handles Google OAuth authorization code flow
2. Google returns an `id_token` (a JWT)
3. `jwt` callback stores the `id_token` in the session
4. Client sends `Authorization: Bearer <id_token>` to the API
5. API validates the JWT via its configured `issuer-uri`

GitHub OAuth is not supported — GitHub returns opaque access tokens, not JWTs.

---

## API Communication

- Base URL configured via `NEXT_PUBLIC_API_URL` (default: `http://localhost:8080`)
- All API calls go through typed wrappers in `lib/api.ts`
- Public pages use Server Components (no auth needed)
- Progress tracking uses Client Components (requires session)

---

## Testing

Three-layer test strategy:

| Layer | Tool | Location | Command | Needs live API? |
|-------|------|----------|---------|-----------------|
| Unit | Vitest + jsdom | `__tests__/` | `npm test` | No |
| Integration | Vitest + node | `__tests__/integration/` | `npm run test:integration` | Yes |
| E2E | Playwright (Chromium) | `e2e/` | `npm run test:e2e` | Yes |

- **Unit tests** mock `next-auth/react` and global `fetch`
- **Integration tests** call `lib/api.ts` against a running API (`localhost:8080`)
- **E2E tests** run a real browser against Next.js dev server + API
- Playwright auto-starts the Next.js dev server via `webServer` config

---

## What NOT to Do

- Do NOT add GitHub OAuth until the API supports opaque token exchange
- Do NOT import from any Gradle module — this is a standalone Node.js project
- Do NOT use `pages/` router — use App Router only
