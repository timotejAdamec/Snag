# Implementation Guide

This document is a reference for the implementation team. It describes what the updated analysis specifies and what the implementation must fulfil. It is **not** part of the thesis text.

---

## Remaining Work

### Must do

1. **FE: User assignment UI** — FE assign/unassign use cases + UI not yet implemented. Local assignment cache, pull sync, cascade delete/restore, and query use case are done. *(§6 #2, FP4c, FP4d)*
2. **FE: Role-based UI gating** — Zero role-based UI gating exists. Current user is a hardcoded UUID. All buttons visible unconditionally. Shared KMP rules exist but are unused on FE driving layer. *(NP13 FE)*
3. **EntraID authentication** — Not started. BE: validate EntraID JWT (currently mock `X-User-Id` header). FE: OAuth2/OIDC flow, token storage, refresh. *(§4, FP34, FP35, NP12)*
4. **Service protocol PDF** — Not started. Second report type with work description and signature fields. *(§5, FP32b)*
5. **FE: Role management UI** — BE API + authorization done; FE UI for managing roles not yet implemented. *(§6 #4)*
6. **Closed-project access gap** — `CanAccessProjectRule` does not check `isClosed`; assigned users still pass access checks on closed projects. Needs design decision on where the check should live. *(NP13 known gap)*

### Partially done (BE complete, FE incomplete)

| Feature | BE | FE |
|---|---|---|
| Project close/reopen | Done (mechanism + authorization + visibility filtering on all routes) | Close/reopen UI exists but no role gating |
| User assignment | Done (API + DB + `CanAssignUserToProjectRule`) | Local cache + sync done; assign/unassign use cases + UI missing |
| Role management (UC7) | Done (`CanSetUserRoleRule`) | UI for role delegation missing |

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

- [ ] Login exclusively via **corporate Microsoft EntraID**.
- [x] No invite flow, no password management in the system.
- [ ] A user without a valid EntraID token has no access whatsoever.
- [x] After successful authentication the system maps the EntraID identity to a role stored in the system. *(infrastructure done: `CallCurrentUserPlugin` resolves user from `X-User-Id` header and maps to `CallCurrentUser` with `UserRole`; EntraID JWT validation not yet implemented)*

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

1. - [ ] **Project closure and reopening** — projects can be closed and reopened; access is determined by project state and roles. *(BE: close/reopen mechanism + `CanCloseProjectRule` + `CanAccessProjectRule` visibility filtering all done on project routes; sub-entity routes wired via `CanAccessProjectUseCase`)*
2. - [ ] **User assignment to / removal from project** — leads can add/remove technicians or service workers. *(BE: API + DB + role-restricted access enforced via `CanAssignUserToProjectRule` — Admin/VP/VS allowed; TP/Ser/None denied. FE: local assignment cache with full-replacement pull sync, cascade delete/restore on project deletion, query use case. FE assign/unassign use cases + UI not yet implemented.)*
3. - [ ] **Authentication (EntraID)** — mandatory Microsoft EntraID login (standalone mechanism, not a UC).
4. - [ ] **Role management (UC7)** — admin manages all roles; passport lead delegates technician role; service lead delegates service worker role. *(BE API done: role set via PUT /users/{id}; authorization enforced via `CanSetUserRoleRule`)*
5. - [x] **Inspection deletion** — UC5 Scenario E. *(full-stack: BE API + DB soft delete + sync, FE use case + local delete + sync enqueue, FE UI delete button + confirmation dialog)*
6. - [x] **Client deletion** — UC2 Scenario F. A client can only be deleted if no project references it. *(full-stack: CanDeleteClientRule in business/rules, FE CanDeleteClientUseCase + delete guard in BE DeleteClientUseCaseImpl, FE UI delete button + confirmation dialog in client edit screen)*
7. - [ ] **Service protocol** — second PDF export format with signature fields.

---

## 7. New Functional Requirements

| FP | Description | UC | Status |
|---|---|---|---|
| FP4 | Close project — restrict access to creator, preserve data | UC1 | - [ ] Close mechanism done; creator-only close authorization enforced on BE; project visibility filtering enforced on all routes including sub-entity routes. |
| FP4b | Reopen project — reopen a closed project | UC1 | - [ ] Reopen mechanism done; creator-only reopen authorization enforced on BE; project visibility filtering enforced on all routes including sub-entity routes. |
| FP4c | Assign user to project | UC1 | - [x] BE API + DB + role-restricted access enforced via `CanAssignUserToProjectRule`. FE: local assignment cache synced from BE (full-replacement pull sync). *(FE assign/unassign use cases + UI not yet implemented)* |
| FP4d | Remove user from project | UC1 | - [x] BE API + DB + role-restricted access enforced via `CanAssignUserToProjectRule`. FE: cascade delete on project deletion. *(FE remove use case + UI not yet implemented)* |
| FP4e | Close project — creator access: only creator retains access to closed project | UC1 | - [ ] Sub-entity editing blocked on closed projects; `creatorId` tracked; creator-only close/reopen enforced; project visibility filtering enforced on all routes including sub-entity routes. |
| FP10 | Delete client — only if not referenced by any project | UC2 | - [x] Done |
| FP31 | Delete inspection | UC5 | - [x] Done |
| FP32b | Generate service protocol — PDF with work description and signature fields | UC6 | - [ ] Not started |
| FP34 | Authentication via Microsoft EntraID | — | - [ ] Not started |
| FP35 | Deny access without authentication | — | - [ ] Not started |
| FP36 | Role management by administrator | UC7 | - [x] BE API done + authorization enforced via `CanSetUserRoleRule` (Admin: any role change) |
| FP37 | Delegate and remove passport technician role | UC7 | - [x] BE API done + authorization enforced via `CanSetUserRoleRule` (Passport Lead: None↔Passport Technician) |
| FP38 | Delegate and remove service worker role | UC7 | - [x] BE API done + authorization enforced via `CanSetUserRoleRule` (Service Lead: None↔Service Worker) |

---

## 8. Non-Functional Requirements for Implementation

### NP12 — Authentication via EntraID
- [ ] **Backend**: validate EntraID JWT on every protected endpoint; reject requests without valid token.
- [ ] **Frontend**: implement OAuth2 / OIDC flow with EntraID; store tokens securely; refresh tokens before expiry.

### NP13 — Role-based authorisation
- [x] **Backend**: authorization fully wired across all routes. Project routes: `CanCreateProjectRule`, `CanCloseProjectRule`, `CanAccessProjectRule`, `CanAssignUserToProjectRule`. Sub-entity routes (structures, findings, inspections, finding photos): `CanAccessProjectUseCase` with project resolution from URL path or entity lookup (three-hop for finding photos: finding → structure → project). Client routes: `CanManageClientsRule` (role-based, denies Passport Technician and None). User routes: `CanSetUserRoleRule` (delegation scoping — Admin: any, Passport Lead: None↔Passport Technician, Service Lead: None↔Service Worker). Report route: `CanAccessProjectUseCase`.
  - **Known gap**: `CanAccessProjectRule` does not check `isClosed` — it grants access to admin/creator/assigned users regardless of project state. Per spec (§3), assigned users should lose access when a project is closed (only creator retains it). This check may belong in `CanAccessProjectRule` itself, in a separate composition (like `CanEditProjectEntitiesUseCaseImpl` does on FE with `AreProjectEntitiesEditableRule`), or in the route-level use case. Decision pending.
- [ ] **Frontend**: hide/show UI elements based on role; do not rely solely on frontend gating.
  - Current state: **zero role-based UI gating**. Current user is a hardcoded UUID (`GetCurrentUserUseCaseImpl`) with TODO to replace with EntraID. FE does not know the current user's role.
  - All buttons (create project, close/reopen, edit, delete, report download) are visible unconditionally — only gated by project state (`isClosed`), not user role or access.
  - Shared KMP rules (`CanCreateProjectRule`, `CanCloseProjectRule`, `CanAccessProjectRule`, `CanAssignUserToProjectRule`) exist. `CanAccessProjectRule` + `AreProjectEntitiesEditableRule` (renamed from `CanEditProjectEntitiesRule`) are now composed in `CanEditProjectEntitiesUseCaseImpl` to gate entity editing by both access and project state. Other rules still unused on FE.
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
