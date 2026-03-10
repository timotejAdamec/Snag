# Implementation Guide

This document is a reference for the implementation team. It describes what the updated analysis specifies and what the implementation must fulfil. It is **not** part of the thesis text.

---

## 1. Roles and Permissions

| Role | Can create project | Can close/reopen project | Access to other projects | Can assign users | Manages clients | Delegates roles | Generates report |
|---|---|---|---|---|---|---|---|
| Uživatel bez role | No | No | No | No | No | No | No |
| Administrátor | No | No | All | No | Yes | All roles | No |
| Vedoucí passportizace | Yes | Own projects | Own + assigned | Yes (technicians + other vedoucí passportizace) | Yes | Technik passportizace | Passportizace report |
| Technik passportizace | No | No | Only assigned | No | Yes | No | Passportizace report |
| Vedoucí servisu | Yes | Own projects | Own + all servisáci + assigned | Yes (servisáci + other vedoucí servisu) | Yes | Servisák | Servisní protokol |
| Servisák | Yes | Own projects | Own + assigned by vedoucí | No | Yes | No | Servisní protokol |

### Notes
- "Tvůrce" = the user who created the project; only the creator can close/reopen their own project.
- "Assigned" = explicitly added to the project via UC1 Scénář F.
- Vedoucí servisu sees all projects belonging to servisáci they manage.

---

## 2. Project Lifecycle

```
[Open] ──close (creator)──> [Closed] ──reopen (creator)──> [Open]
```

### On project close
- The project state is set to "closed".
- Users assigned to the project lose access to it (they no longer see it in their project list).
- All project data is preserved — **no data is deleted**.
- Pending synchronisation queue entries for this project should be discarded / ignored.

### On project reopen
- The project state is set back to "open".
- Previously assigned users regain access.

---

## 3. Project Access Rules

| Role | Which projects are visible |
|---|---|
| Technik passportizace | Only projects to which they were explicitly assigned (UC1 Scénář F) |
| Servisák | Projects they created + projects assigned by vedoucí servisu |
| Vedoucí passportizace | Own projects + projects to which they were assigned |
| Vedoucí servisu | Own projects + all projects of servisáci they manage + projects to which they were assigned |
| Administrátor | All projects |

---

## 4. Authentication

- Login exclusively via **Rilancio Microsoft EntraID**.
- No invite flow, no password management in the system.
- A user without a valid EntraID token has no access whatsoever.
- After successful authentication the system maps the EntraID identity to a role stored in the system.

---

## 5. Two PDF Report Types

| Attribute | Passportizace report | Servisní protokol |
|---|---|---|
| Who generates | Vedoucí passportizace, Technik passportizace | Servisák, Vedoucí servisu |
| Content | Client info, inspections, objects, findings (with coordinates on floor plans) | Description of work performed |
| Signature fields | No | Yes (names + signatures) |
| Requires internet | Yes (generated on server) | Yes (generated on server) |

---

## 6. New Features (Delta vs. Original Analysis)

The following features are new compared to the original analysis (which had no roles):

1. **Project closure and reopening** — projects can be closed (access revoked) and reopened; no deletion of projects.
2. **User assignment to / removal from project** — vedoucí can add/remove technicians or servisáci.
3. **Authentication (EntraID)** — mandatory Microsoft EntraID login.
4. **Role management** — admin manages all roles; vedoucí passportizace delegates technik role; vedoucí servisu delegates servisák role.
5. **Inspection deletion** — UC5 Scénář E.
6. **Servisní protokol** — second PDF export format with signature fields.

---

## 7. New Functional Requirements

| FP | Description | UC |
|---|---|---|
| FP4 | Uzavření projektu — close project, revoke access, preserve data | UC1 |
| FP4b | Znovuotevření projektu — reopen closed project | UC1 |
| FP4c | Přiřazení uživatele do projektu | UC1 |
| FP4d | Odebrání uživatele z projektu | UC1 |
| FP31 | Smazání inspekce | UC5 |
| FP32b | Generace servisního protokolu — PDF with work description and signature fields | UC6 |
| FP34 | Autentizace přes Microsoft EntraID | UC7 |
| FP35 | Zamítnutí přístupu bez autentizace | UC7 |
| FP36 | Správa rolí administrátorem | UC8 |
| FP37 | Delegace role technika passportizace | UC8 |
| FP38 | Delegace role servisáka | UC8 |

---

## 8. Non-Functional Requirements for Implementation

### NP12 — Authentication via EntraID
- **Backend**: validate EntraID JWT on every protected endpoint; reject requests without valid token.
- **Frontend**: implement OAuth2 / OIDC flow with EntraID; store tokens securely; refresh tokens before expiry.

### NP13 — Role-based authorisation
- **Backend**: check user role on every endpoint; return 403 for insufficient permissions.
- **Frontend**: hide/show UI elements based on role; do not rely solely on frontend gating.

### NP4 (updated) — Offline access with 10-second sync
- Synchronisation must complete within **10 seconds** of a change once connectivity is restored.

### NP5 (updated) — LWW conflict resolution
- Conflict resolution strategy: **last-write-wins** based on the **time the change was created** (not the time of synchronisation).
- Each change record must carry a precise creation timestamp.
- On conflict, the record with the later creation timestamp wins.
