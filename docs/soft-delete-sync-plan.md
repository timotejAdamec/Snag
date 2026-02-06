# Soft-Delete Sync Implementation Plan

## Key Decisions

- **Soft-delete on backend only**: `deletedAt` timestamp added to backend models and API DTOs, not frontend models
- **Client-side cascade**: Backend only marks the directly deleted entity. Client cascade-deletes children locally
- **Incremental pull sync**: `?since={timestamp}` parameter on existing GET endpoints returns entities changed since that time
- **Trash/restore**: Out of scope for this implementation

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              BACKEND                                     │
├─────────────────────────────────────────────────────────────────────────┤
│  DELETE /projects/{id}                                                   │
│    → Sets deletedAt timestamp instead of removing data                   │
│    → Returns 204 (no conflict) or 409 (if client data is stale)         │
│                                                                          │
│  GET /projects?since={timestamp}                                         │
│    → Returns all projects where updatedAt > since OR deletedAt > since   │
│    → Includes soft-deleted entities (client needs deletedAt to know)     │
│                                                                          │
│  PUT /projects/{id}                                                      │
│    → Compares client updatedAt vs max(server.updatedAt, server.deletedAt)│
│    → If client is newer, overwrites (can restore soft-deleted entity)    │
│    → If server is newer, returns 409 conflict                            │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND                                    │
├─────────────────────────────────────────────────────────────────────────┤
│  Push Sync (existing):                                                   │
│    User action → Save to DB → Enqueue sync operation → SyncEngine pushes │
│                                                                          │
│  Pull Sync (new):                                                        │
│    GET use case invoked → Flush push queue → Fetch ?since=lastSyncedAt   │
│      → For each entity:                                                  │
│          deletedAt != null → Hard-delete locally + cascade children      │
│          deletedAt == null → Upsert locally                              │
│      → Store new lastSyncedAt timestamp                                  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow

### 1. User Deletes an Entity (Push)

```
User deletes Project A
    ↓
Frontend: Hard-delete from local DB (immediate UI feedback)
    ↓
Frontend: Enqueue DELETE sync operation
    ↓
SyncEngine: POST delete to backend with deletedAt timestamp
    ↓
Backend: Set deletedAt on Project A (soft-delete, data preserved)
```

### 2. Another Client Syncs (Pull)

```
Client B opens app / navigates to projects list
    ↓
GetProjectsUseCase invoked
    ↓
PullProjectChangesUseCase:
    1. Flush any pending push operations (avoid conflicts)
    2. GET /projects?since=lastSyncedAt
    3. Backend returns Project A with deletedAt set
    4. Client sees deletedAt != null → cascade delete:
       - Delete all structures for Project A
       - For each structure, delete all findings
       - Delete Project A from local DB
    5. Store current timestamp as new lastSyncedAt
    ↓
UI updates from DB flow (project disappears)
```

**Core pull sync logic:**
```kotlin
when (val result = projectsApi.getProjectsModifiedSince(since)) {
    is OnlineDataResult.Failure -> { /* log error, retry later */ }
    is OnlineDataResult.Success -> {
        result.data.forEach { syncResult ->
            when (syncResult) {
                is ProjectSyncResult.Deleted -> {
                    cascadeDeleteLocalStructuresByProjectIdUseCase(syncResult.id)
                    projectsDb.deleteProject(syncResult.id)
                }
                is ProjectSyncResult.Updated -> {
                    projectsDb.saveProject(syncResult.project)
                }
            }
        }
        pullSyncTimestampDataSource.setLastSyncedAt(now)
    }
}
```

### 3. Conflict Resolution

```
Client A: Has Project X with updatedAt=100
Client B: Updates Project X, now updatedAt=200 on server

Client A tries to save Project X:
    ↓
Backend compares: client.updatedAt(100) vs server.updatedAt(200)
    ↓
Server is newer → Return 409 Conflict with server's version
    ↓
Client A receives fresher data, saves to local DB
    ↓
UI updates with server's version
```

---

## Entity Hierarchy & Cascade

```
Project
  └── Structure (many)
        └── Finding (many)
```

**On delete cascade (client-side):**
- Deleting a Project → delete all its Structures → delete all Findings for each Structure
- Deleting a Structure → delete all its Findings
- Deleting a Finding → no cascade needed (leaf entity)

**Why client-side cascade?**
- Backend only soft-deletes the directly deleted entity
- Keeps backend simple; children remain in DB (orphaned but with valid parent reference)
- Client is responsible for cleaning up its local DB when it learns about deletions

---

## Sync Coordination

**Problem**: What if a pull sync happens while push operations are still queued?

**Solution**: `withFlushedQueue` coordination

```
Pull sync starts
    ↓
Acquire mutex lock
    ↓
Process all pending push operations (flush queue)
    ↓
Now safe to pull (no local changes will conflict)
    ↓
Fetch from server, apply changes locally
    ↓
Release mutex lock
```

```kotlin
override suspend fun <T> withFlushedQueue(block: suspend () -> T): T =
    mutex.withLock {
        processAllPending()  // flush push queue first
        block()              // then run pull sync
    }
```

This ensures:
1. Local changes are pushed before pulling
2. No race conditions between push and pull
3. Server always has latest client state before client fetches server state

---

## Timestamp Tracking

Each entity type + scope has its own `lastSyncedAt`:

| Entity Type | Scope | Example |
|-------------|-------|---------|
| Project | Global | `lastSyncedAt` for all projects |
| Structure | Per Project | `lastSyncedAt` for structures in Project X |
| Finding | Per Structure | `lastSyncedAt` for findings in Structure Y |

**Storage (SQLDelight):**
```sql
CREATE TABLE PullSyncTimestampEntity (
    entityType TEXT NOT NULL,    -- "project", "structure", "finding"
    scopeId TEXT NOT NULL,       -- "" for global, or parent UUID
    lastSyncedAt INTEGER NOT NULL,
    PRIMARY KEY (entityType, scopeId)
);
```

**Why per-scope?**
- User may only view one project at a time
- No need to sync all structures across all projects
- Reduces data transfer and improves performance

---

## Backend: Modified Since Query

The key query that powers incremental sync:

```kotlin
// Return entities modified OR deleted since the given timestamp
fun getProjectsModifiedSince(since: Timestamp): List<BackendProject> =
    projects.values.filter { project ->
        project.updatedAt > since ||
        (project.deletedAt != null && project.deletedAt > since)
    }
```

When `since=0`, this returns all entities (initial sync).

---

## API Contract Summary

| Endpoint | Without `?since` | With `?since={ts}` |
|----------|------------------|-------------------|
| `GET /projects` | All non-deleted | All modified since ts (including deleted) |
| `GET /projects/{id}/structures` | All non-deleted for project | All modified since ts (including deleted) |
| `GET /structures/{id}/findings` | All non-deleted for structure | All modified since ts (including deleted) |

| Endpoint | Request | Response |
|----------|---------|----------|
| `DELETE /projects/{id}` | `{deletedAt: timestamp}` | 204 success, 409 conflict |
| `PUT /projects/{id}` | Full entity with `updatedAt` | 200 + entity, 409 conflict |

---

## Edge Cases

1. **Restore after delete**: Client sends PUT with `updatedAt` newer than `deletedAt` → entity restored (deletedAt cleared)

2. **Delete already deleted**: Returns 204 (idempotent)

3. **First sync ever**: `since=0` → returns all entities for that scope

4. **Offline for long time**: Large delta sync, but same logic applies

5. **Concurrent deletes**: Both clients delete same entity → both succeed (soft-delete is idempotent)
