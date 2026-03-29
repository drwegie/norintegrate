# ADR-014: Decision Not to Integrate ID-porten/BankID Authentication

**Status:** Accepted
**Date:** 2026-03-29

## Context

Norway's central authentication system, ID-porten, is managed by Digitaliseringsdirektoratet (Digdir) and provides OIDC-based login via BankID, MinID, and other Norwegian eID methods. All Norwegian residents with a D-nummer or fødselsnummer can use it. For a platform targeting immigrants in Norway, ID-porten login would be a natural fit alongside Google OAuth.

We evaluated the technical and administrative feasibility:

**Technical feasibility — confirmed:**
- Auth.js v5 has a built-in `BankIDNorway` provider (`@auth/core/providers/bankid-no`) that handles the OIDC flow out of the box.
- The `app_user` table already uses generic `oauth_provider` / `oauth_subject` columns, so no schema changes would be needed.
- Spring Security's `JwtIssuerAuthenticationManagerResolver` supports multi-issuer JWT validation, allowing Google and ID-porten tokens to coexist.
- The code changes would be modest: add the provider in `auth.ts`, configure multi-issuer JWT validation in `SecurityConfig.java`, and add the ID-porten issuer URI to application properties.

**Administrative feasibility — blocked:**
- Integrating with ID-porten requires registration as a service provider through Digdir's Samarbeidsportalen (https://samarbeid.digdir.no).
- Registration is restricted to Norwegian organizations (companies, municipalities, government agencies). Individual developers cannot register.
- The approval process involves organizational verification and can take weeks.
- Test credentials are also only available through the same registration process.

## Decision

ID-porten/BankID authentication is not implemented. The blocking constraint is administrative (organizational registration with Digdir), not technical. Google OAuth remains the sole authentication provider.

## Consequences

- Users who prefer government ID login must use Google OAuth instead. This is acceptable for a portfolio/demonstration project.
- If this project is ever adopted by a Norwegian organization, adding ID-porten is straightforward — the technical path is documented above and the architecture already supports it.
- The `app_user` table design remains provider-agnostic, so no migration would be needed when adding a second provider.
- This ADR serves as a reference for the technical implementation steps if the administrative barrier is removed in the future.
