# Implementation Guide

This document is a reference for the implementation team. It describes what the updated analysis specifies and what the implementation must fulfil. It is **not** part of the thesis text.

---

## Remaining Work

### Must do

1. **FE: User assignment UI** — FE assign/unassign use cases + UI not yet implemented. Local assignment cache, pull sync, cascade delete/restore, and query use case are done. *(§6 #2, FP4c, FP4d)*
2. ~~**FE: Role-based UI gating**~~ — **Done.** FE use cases (`CanCreateProjectUseCase`, `CanCloseProjectUseCase`, `CanManageClientsUseCase`, `GetAllowedRoleOptionsUseCase`) wrap shared KMP rules with `GetCurrentUserFlowUseCase`, emitting reactive `Flow<Boolean>`. All ViewModels collect these into state booleans (deny-first defaults: `false` until confirmed). UI buttons hidden/disabled based on authorization. *(NP13 FE)*
3. ~~**EntraID authentication**~~ — **Done.** BE: dual-mode `CallCurrentUserPlugin` with Ktor `Authentication` plugin (`jwt {}` provider) for production, `X-User-Id` header for dev. `GetOrCreateUserByEntraIdUseCase` auto-creates users on first login. `GET /users/me` endpoint returns current user's `UserApiDto`. FE: hexagonal architecture in `feat/authentication/fe/` — use cases (`GetAuthenticatedUserIdUseCase`, `LoginUseCase`, `LogoutUseCase`) in `app/{api,impl}`, port interfaces (`AuthTokenProvider`, `AuthState`, `AuthenticationApi`) in `ports`, adapters (`OidcAuthTokenProvider` via kotlin-multiplatform-oidc v0.16.5, `MockAuthTokenProvider`, `RealAuthenticationApi`) in `driven/impl`, test fakes in `driven/test`, UI (`AuthenticationGate`, `LoginScreen`, `AuthenticationViewModel`) in `driving/impl`. `LoginUseCase` coordinates OIDC login → `/users/me` call → authenticated user ID resolution. Token refresh via `TokenRefreshHandler` + `oidcBearer` Ktor integration. Mock/production dual-mode via `CommonConfiguration.mockAuth` DI switching. Build-time config via BuildKonfig (`MOCK_AUTH`, `ENTRA_ID_TENANT_ID`, `ENTRA_ID_CLIENT_ID`, `ENTRA_ID_REDIRECT_URI`). *(§4, FP34, FP35, NP12)*
4. **Service protocol PDF** — Not started. Second report type with work description and signature fields. *(§5, FP32b)*
5. ~~**FE: Role management UI**~~ — **Done.** `GetAllowedRoleOptionsUseCase` computes allowed role transitions per target user; `UserManagementViewModel` collects into `allowedRoleOptions` per `UserItem`; `RoleDropdown` filters options accordingly. *(§6 #4)*
6. ~~**Closed-project access gap**~~ — **Not a gap.** `CanAccessProjectRule` intentionally does not check `isClosed`; `AreProjectEntitiesEditableRule` handles it separately. Both rules are composed at the use case layer: FE via `CanEditProjectEntitiesUseCaseImpl`, BE via sub-entity route use cases. Separation by design — access and editability are orthogonal concerns.

### Partially done (BE complete, FE incomplete)

| Feature | BE | FE |
|---|---|---|
| Project close/reopen | Done (mechanism + authorization + visibility filtering on all routes) | Close/reopen UI exists; role gating done (`CanCloseProjectUseCase` gates button to admin/creator) |
| User assignment | Done (API + DB + `CanAssignUserToProjectRule`) | Local cache + sync done; assign/unassign use cases + UI missing |
| Role management (UC7) | Done (`CanSetUserRoleRule`) | **Done** — `GetAllowedRoleOptionsUseCase` + filtered `RoleDropdown` |

---

## 1. Roles and Permissions

| Role | Can create project | Can close/reopen project | Access to other projects | Can assign users | Manages clients | Delegates/removes roles | Generates report |
|---|---|---|---|---|---|---|---|
| User without role | No | No | No | No | No | No | No |
| Administrator | Yes | Creator only | All | Yes (all users) | Yes | All roles | Both |
| Passport lead | Yes | Creator only | Own + assigned | Yes (TP + other VP) | Yes | Passport technician | Passport report |
| Passport technician | No | No | Only assigned | No | No | No | Passport report |
| Service lead | Yes | Creator only | Own + all service workers + assigned | Yes (Ser. + other VS) | Yes | Service worker | Service protocol |
| Service worker | Yes | Creator only | Own + assigned by lead | No | Yes | No | Service protocol |

### Notes
- "Creator" = the user who created the project; only the creator can close/reopen their own project.
- "Assigned" = explicitly added to the project via UC1 Scenario F.
- Service lead sees all projects belonging to service workers they manage.
- Passport technician **cannot** manage clients (create, edit, assign) — only VP, VS, and Ser. can.
- Leads can both **delegate and remove** roles (UC7 Scenario B/C/D): assigning a role to a user without role, or removing a delegated role (setting user back to "without role").

---

## 2. Project Lifecycle

```
[Open] ──close (creator)──> [Closed] ──reopen (creator)──> [Open]
```

### On project close
- [x] The project state is set to "closed".
- [x] Only the creator retains access; other users no longer see the project in their list. *(BE: `CanCloseProjectRule` enforces creator-only close/reopen; `GET /projects` filters by `CanAccessProjectRule` — creator/assigned/admin; sub-entity routes wired via `CanAccessProjectUseCase`)*
- [x] All project data is preserved — **no data is deleted**.
- [x] Pending synchronisation queue entries for this project should be discarded / ignored. *(backend returns server entity instead of 403, frontend treats as success — queue unblocks)*

### On project reopen
- [x] The project state is set back to "open".
- [x] Previously assigned users can access the project again. *(BE: creator-only reopen enforced via `CanCloseProjectRule`; `GET /projects` filters by access — reopened project visible again to assigned users)*

---

## 3. Project Access Rules

| Role | Which projects are visible |
|---|---|
| Passport technician | Only projects to which they were explicitly assigned (UC1 Scenario F) |
| Service worker | Projects they created + projects assigned by service lead |
| Passport lead | Own projects + projects to which they were assigned |
| Service lead | Own projects + all projects of service workers they manage + projects to which they were assigned |
| Administrator | All projects |

### CRUD Access to Project Sub-Entities

Both the **creator** and any **assigned user** of an open project have **full CRUD access** (create, read, edit, delete) to all entities within that project:
- Inspections
- Objects
- Findings
- Coordinates
- Photo documentation (of objects and findings)

When a project is **closed**, only the creator retains access. Assigned users lose all access immediately.

- [x] Backend rejects sub-entity save/delete on closed projects (returns server entity for sync compatibility).
- [x] Creator-only access enforcement — `creatorId` tracked on projects; close/reopen authorization enforced; project visibility filtering enforced on all project routes via `CanAccessProjectRule`; sub-entity routes wired via `CanAccessProjectUseCase`.

---

## 4. Authentication

- [x] Login exclusively via **corporate Microsoft EntraID**. *(BE: `CallCurrentUserPlugin` dual-mode — validates EntraID JWT via Ktor `Authentication` plugin with `jwt {}` provider in production, retains `X-User-Id` header mock mode for dev/testing via `CommonConfiguration.mockAuth`. FE: hexagonal architecture — `AuthTokenProvider` port with `OidcAuthTokenProvider` (kotlin-multiplatform-oidc v0.16.5, Auth Code + PKCE, token refresh, persistent secure storage) and `MockAuthTokenProvider` (hardcoded UUID). `LoginUseCase` coordinates OIDC login → `GET /users/me` → set authenticated user ID. `CallCurrentUserConfiguration` uses `oidcBearer` Ktor integration with `TokenRefreshHandler` in production or custom mock plugin in dev. `AuthenticationGate` composable wraps `MainScreen` as a precondition gate. Build-time config: `MOCK_AUTH`, `ENTRA_ID_TENANT_ID`, `ENTRA_ID_CLIENT_ID`, `ENTRA_ID_REDIRECT_URI` in BuildKonfig.)*
- [x] No invite flow, no password management in the system.
- [x] A user without a valid EntraID token has no access whatsoever. *(BE: `CallCurrentUserPlugin` only sets `CallCurrentUser` attribute on valid JWT or valid mock header; routes call `currentUser()` which throws `UnauthenticatedException` → 401 when attribute is missing. FE: `AuthenticationGate` with `AuthenticationViewModel` observes `GetAuthenticatedUserIdUseCase` (maps `AuthState` to `StateFlow<Uuid?>`) — `LoginScreen` shown until authenticated.)*
- [x] After successful authentication the system maps the EntraID identity to a role stored in the system. *(BE: `CallCurrentUserPlugin` extracts `oid` claim from `JWTPrincipal`, resolves user via `GetOrCreateUserByEntraIdUseCase`. Auto-creates user with `role=null` on first login. FE: `LoginUseCase` calls `AuthTokenProvider.login()` for OIDC flow, then `AuthenticationApi.getCurrentUser()` (GET /users/me → `OnlineDataResult<Uuid>`) to resolve user ID, then sets `AuthState.Authenticated(userId)`. User's role is stored in DB and synced to FE.)*

---

## 5. Two PDF Report Types

| Attribute | Passport report | Service protocol |
|---|---|---|
| Who generates | Passport lead, Passport technician | Service worker, Service lead |
| Content | Client info, inspections, objects, findings (with coordinates on floor plans) | Description of work performed |
| Signature fields | No | Yes (names + signatures) |
| Requires internet | Yes (generated on server) | Yes (generated on server) |

---

## 6. New Features (Delta vs. Original Analysis)

The following features are new compared to the original analysis (which had no roles):

1. - [x] **Project closure and reopening** — projects can be closed and reopened; access is determined by project state and roles. *(BE: close/reopen mechanism + `CanCloseProjectRule` + `CanAccessProjectRule` visibility filtering all done on project routes; sub-entity routes wired via `CanAccessProjectUseCase`. FE: close/reopen button gated via `CanCloseProjectUseCase`; entity editing gated via `CanEditProjectEntitiesUseCase`.)*
2. - [ ] **User assignment to / removal from project** — leads can add/remove technicians or service workers. *(BE: API + DB + role-restricted access enforced via `CanAssignUserToProjectRule` — Admin/VP/VS allowed; TP/Ser/None denied. FE: local assignment cache with full-replacement pull sync, cascade delete/restore on project deletion, query use case. FE assign/unassign use cases + UI not yet implemented.)*
3. - [x] **Authentication (EntraID)** — mandatory Microsoft EntraID login (standalone mechanism, not a UC). *(BE: dual-mode `CallCurrentUserPlugin` with JWT validation via `EntraIdJwtVerifier` + mock header fallback; `GET /users/me` endpoint. FE: hexagonal `feat/authentication/fe/` — `AuthTokenProvider` port with `OidcAuthTokenProvider` (kotlin-multiplatform-oidc) / `MockAuthTokenProvider`, `LoginUseCase` (OIDC → /users/me → user ID), `AuthenticationGate` precondition gate. Build-time config via BuildKonfig.)*
4. - [x] **Role management (UC7)** — admin manages all roles; passport lead delegates technician role; service lead delegates service worker role. *(BE API done: role set via PUT /users/{id}; authorization enforced via `CanSetUserRoleRule`. FE: `GetAllowedRoleOptionsUseCase` computes allowed transitions; `UserManagementViewModel` populates `allowedRoleOptions` per user; `RoleDropdown` filters visible options.)*
5. - [x] **Inspection deletion** — UC5 Scenario E. *(full-stack: BE API + DB soft delete + sync, FE use case + local delete + sync enqueue, FE UI delete button + confirmation dialog)*
6. - [x] **Client deletion** — UC2 Scenario F. A client can only be deleted if no project references it. *(full-stack: CanDeleteClientRule in business/rules, FE CanDeleteClientUseCase + delete guard in BE DeleteClientUseCaseImpl, FE UI delete button + confirmation dialog in client edit screen)*
7. - [ ] **Service protocol** — second PDF export format with signature fields.

---

## 7. New Functional Requirements

| FP | Description | UC | Status |
|---|---|---|---|
| FP4 | Close project — restrict access to creator, preserve data | UC1 | - [x] Close mechanism done; creator-only close authorization enforced on BE; project visibility filtering enforced on all routes including sub-entity routes. FE: close button gated via `CanCloseProjectUseCase` (admin/creator only). |
| FP4b | Reopen project — reopen a closed project | UC1 | - [x] Reopen mechanism done; creator-only reopen authorization enforced on BE; project visibility filtering enforced on all routes including sub-entity routes. FE: reopen button gated via `CanCloseProjectUseCase` (admin/creator only). |
| FP4c | Assign user to project | UC1 | - [x] BE API + DB + role-restricted access enforced via `CanAssignUserToProjectRule`. FE: local assignment cache synced from BE (full-replacement pull sync). *(FE assign/unassign use cases + UI not yet implemented)* |
| FP4d | Remove user from project | UC1 | - [x] BE API + DB + role-restricted access enforced via `CanAssignUserToProjectRule`. FE: cascade delete on project deletion. *(FE remove use case + UI not yet implemented)* |
| FP4e | Close project — creator access: only creator retains access to closed project | UC1 | - [x] Sub-entity editing blocked on closed projects; `creatorId` tracked; creator-only close/reopen enforced; project visibility filtering enforced on all routes including sub-entity routes. FE: `CanEditProjectEntitiesUseCase` composes access + editability checks. |
| FP10 | Delete client — only if not referenced by any project | UC2 | - [x] Done |
| FP31 | Delete inspection | UC5 | - [x] Done |
| FP32b | Generate service protocol — PDF with work description and signature fields | UC6 | - [ ] Not started |
| FP34 | Authentication via Microsoft EntraID | — | - [x] BE: dual-mode `CallCurrentUserPlugin` (Ktor `jwt {}` provider + mock header); `GET /users/me` endpoint. FE: `OidcAuthTokenProvider` (kotlin-multiplatform-oidc, Auth Code + PKCE), `LoginUseCase` (OIDC → /users/me → user ID), `AuthenticationGate` precondition gate. |
| FP35 | Deny access without authentication | — | - [x] BE: unauthenticated requests get no `CallCurrentUser` → `currentUser()` throws → 401. FE: `AuthenticationGate` shows `LoginScreen` until `GetAuthenticatedUserIdUseCase` emits non-null user ID. |
| FP36 | Role management by administrator | UC7 | - [x] BE API done + authorization enforced via `CanSetUserRoleRule` (Admin: any role change) |
| FP37 | Delegate and remove passport technician role | UC7 | - [x] BE API done + authorization enforced via `CanSetUserRoleRule` (Passport Lead: None↔Passport Technician) |
| FP38 | Delegate and remove service worker role | UC7 | - [x] BE API done + authorization enforced via `CanSetUserRoleRule` (Service Lead: None↔Service Worker) |

---

## 8. Non-Functional Requirements for Implementation

### NP12 — Authentication via EntraID
- [x] **Backend**: validate EntraID JWT on every protected endpoint; reject requests without valid token. *(Dual-mode `CallCurrentUserPlugin`: production validates JWT via Ktor `Authentication` plugin with `jwt {}` provider against EntraID JWKS; dev uses `X-User-Id` header via `CommonConfiguration.mockAuth`. `GetOrCreateUserByEntraIdUseCase` auto-creates user on first login. `GET /users/me` returns current user's `UserApiDto`.)*
- [x] **Frontend**: implement OAuth2 / OIDC flow with EntraID; store tokens securely; refresh tokens before expiry. *(Hexagonal architecture in `feat/authentication/fe/`: use cases in `app/{api,impl}` (`GetAuthenticatedUserIdUseCase` maps `AuthState` to `StateFlow<Uuid?>`, `LoginUseCase` coordinates OIDC login + `/users/me` user resolution, `LogoutUseCase`); port interfaces in `ports` (`AuthTokenProvider` with `AuthState` sealed — `Unauthenticated`/`Authenticated(userId)`, `AuthenticationApi` with `getCurrentUser() → OnlineDataResult<Uuid>`); adapters in `driven/impl` (`OidcAuthTokenProvider` via kotlin-multiplatform-oidc v0.16.5 — Auth Code + PKCE, persistent secure storage, token refresh via `TokenRefreshHandler` + `oidcBearer` Ktor integration; `MockAuthTokenProvider` with hardcoded UUID; `RealAuthenticationApi` calls `GET /users/me`); test fakes in `driven/test` (`FakeAuthTokenProvider`, `FakeAuthenticationApi`); UI in `driving/impl` (`AuthenticationGate` precondition composable wrapping `MainScreen`, `LoginScreen` with loading state, `AuthenticationViewModel`). Mock/production switching via `CommonConfiguration.mockAuth` DI.)*

### NP13 — Role-based authorisation
- [x] **Backend**: authorization fully wired across all routes. Project routes: `CanCreateProjectRule`, `CanCloseProjectRule`, `CanAccessProjectRule`, `CanAssignUserToProjectRule`. Sub-entity routes (structures, findings, inspections, finding photos): `CanAccessProjectUseCase` with project resolution from URL path or entity lookup (three-hop for finding photos: finding → structure → project). Client routes: `CanManageClientsRule` (role-based, denies Passport Technician and None). User routes: `CanSetUserRoleRule` (delegation scoping — Admin: any, Passport Lead: None↔Passport Technician, Service Lead: None↔Service Worker). Report route: `CanAccessProjectUseCase`.
  - **Note**: `CanAccessProjectRule` intentionally does not check `isClosed` — access and editability are orthogonal concerns. Both are composed at the use case layer: FE via `CanEditProjectEntitiesUseCaseImpl` (combines `CanAccessProjectRule` + `AreProjectEntitiesEditableRule`), BE via sub-entity route use cases that check both.
- [x] **Frontend**: FE authorization use cases wrap shared KMP rules with `GetCurrentUserFlowUseCase` and reactive DB queries, emitting `Flow<Boolean>` (deny-first: all default to `false`). ViewModels collect these into state booleans that gate UI elements.
  - `CanCreateProjectUseCase` — gates "new project" button (role-based via `CanCreateProjectRule`)
  - `CanCloseProjectUseCase` — gates close/reopen button (admin or creator via `CanCloseProjectRule`)
  - `CanEditProjectEntitiesUseCase` — gates entity editing (access + project-not-closed via `CanAccessProjectRule` + `AreProjectEntitiesEditableRule`); feeds into `ProjectDetailsUiState.isProjectEditable` which cascades to edit, delete, new structure/inspection buttons
  - `CanManageClientsUseCase` — gates client create/edit/delete (role-based via `CanManageClientsRule`)
  - `GetAllowedRoleOptionsUseCase` — returns `Flow<Set<UserRole?>>` of assignable roles per target; filters `RoleDropdown` options
  - Current user is resolved via `GetAuthenticatedUserIdUseCase` (from `feat/authentication/fe/app/api`), which maps `AuthState` to `StateFlow<Uuid?>`. `GetCurrentUserUseCaseImpl` delegates to this use case; `GetCurrentUserFlowUseCaseImpl` uses the resulting UUID to query user data (including role) from local DB. In mock mode, `MockAuthTokenProvider` provides a hardcoded UUID eagerly. In production, `LoginUseCase` coordinates OIDC login → `/users/me` call → `AuthState.Authenticated(userId)` resolution.
  - **Stale data issue**: when a user's role changes or they are unassigned, the local DB retains all previously synced data (projects, structures, findings, photos). Backend stops serving new updates, but old data remains readable offline. Backend blocks unauthorized writes (403), so no data corruption, but user can still read data they should no longer access. Inherent to offline-first architecture — possible fix: detect projects no longer returned by backend sync and purge local copies.

### Detailed Permission Matrix

This matrix mirrors the thesis permission matrix table. Legend: ✓ = unrestricted, T = creator only, P = creator or assigned user, — = denied.

| Action | Adm. | VP | TP | VS | Ser. | None |
|---|---|---|---|---|---|---|
| **Project management** | | | | | | |
| Create project | ✓ | ✓ | — | ✓ | ✓ | — |
| Edit project | ✓ | P | — | P* | P | — |
| Close project | ✓ | T | — | T | T | — |
| Reopen project | ✓ | T | — | T | T | — |
| Assign user to project | ✓ | ✓† | — | ✓‡ | — | — |
| Remove user from project | ✓ | ✓† | — | ✓‡ | — | — |
| **Client management** | | | | | | |
| Create/edit client | ✓ | ✓ | — | ✓ | ✓ | — |
| Delete client | ✓ | ✓ | — | ✓ | ✓ | — |
| Assign client to project | ✓ | P | — | P | P | — |
| **CRUD project entities** (objects, inspections, findings, coordinates, photos) | | | | | | |
| Create, read, edit, delete | ✓ | P | P | P | P | — |
| **Report generation** | | | | | | |
| Passport report | ✓ | P | P | — | — | — |
| Service protocol | ✓ | — | — | P | P | — |
| **Access management** | | | | | | |
| Manage all roles | ✓ | — | — | — | — | — |
| Delegate/remove TP role | ✓ | ✓ | — | — | — | — |
| Delegate/remove Ser. role | ✓ | — | — | ✓ | — | — |

\* VS also has access to projects created by service workers.
† VP assigns/removes only TP and VP roles.
‡ VS assigns/removes only Ser. and VS roles; also has access to service worker projects.
