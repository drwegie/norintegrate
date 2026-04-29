# norintegrate-web

Next.js 15 frontend for NorIntegrate — helps immigrants navigate the settlement process in Norway.

## Quick Start

```bash
npm install
npm run dev
```

The dev server runs at `http://localhost:3000` and expects the API at `http://localhost:8080`.

## Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Production build (standalone output) |
| `npm start` | Start production server |
| `npm test` | Run Vitest unit tests |
| `npm run test:integration` | Run integration tests (requires running API) |
| `npm run test:e2e` | Run Playwright E2E tests |
| `npm run lint` | ESLint check |

## Internationalization (i18n)

Uses [`next-intl`](https://next-intl.dev/) with a cookie-based locale strategy. Supported locales: English (`en`) and Norwegian Bokmal (`nb`).

- **Message files**: `messages/en.json`, `messages/nb.json`
- **Server config**: `i18n/request.ts` (reads `NEXT_LOCALE` cookie)
- **Client constants**: `i18n/locales.ts` (safe to import from client components)
- **Locale switcher**: `components/LocaleSwitcher.tsx`

To add a new locale, create `messages/<code>.json` and add the code to `SUPPORTED_LOCALES` in `i18n/locales.ts`.

## Accessibility

WCAG 2.0 AA compliance is verified via `@axe-core/playwright` in `e2e/a11y.spec.ts`. Run with `npm run test:e2e`.

## Authentication

Uses [Auth.js v5](https://authjs.dev/) with Google OAuth 2.0. See [ADR-012](../docs/adr/ADR-012-frontend-technology-choice.md) for the technology choice.

Required environment variables:
- `GOOGLE_CLIENT_ID` — Google OAuth client ID
- `GOOGLE_CLIENT_SECRET` — Google OAuth client secret
- `NEXTAUTH_SECRET` — Session encryption key (`openssl rand -base64 32`)

Cookie security is hardened in production with `__Secure-` and `__Host-` prefixes.

## Testing Strategy

Three-layer approach per [ADR-015](../docs/adr/ADR-015-three-layer-frontend-testing-strategy.md):

1. **Unit** (Vitest + jsdom): Component rendering, mock auth state
2. **Integration** (Vitest + node): API client against live backend
3. **E2E** (Playwright): Full browser flows including a11y checks
