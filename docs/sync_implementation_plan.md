# Push Sync Library (`lib/sync/fe`)

## Overview

Replace direct background API calls in feature use cases with a persistent sync queue. The queue stores operations, deduplicates by entity, and retries all pending operations whenever a new one is enqueued. Processing stops on first failure to preserve FIFO dependency ordering.

## Module Structure

```
lib/sync/fe/
├── app/           # SyncEngine, EnqueueSyncOperation, SyncOperationHandler, types (public API)
├── ports/         # SyncQueue (lib-internal storage port)
├── driven/
│   ├── impl/      # SQLite-backed SyncQueue
│   └── test/      # In-memory FakeSyncQueue, FakeEnqueueSyncOperation
```

## Architecture: Keeping sync in adapter layers

`lib/sync/fe/ports` is internal — only used by `lib/sync/fe/app`. Features depend on `lib/sync/fe/app`.

Each feature defines its own sync port in `fe/ports` (no sync lib dependency), implemented in `fe/driven/impl` using the sync lib. The sync handler also lives in `fe/driven/impl`.

```
lib/sync/fe/app  ←  feat/projects/fe/driven/impl  (implements handler + sync port)
                     feat/projects/fe/ports         (defines ProjectsSync, no sync dep)
                     feat/projects/fe/app           (uses ProjectsSync, no sync dep)
```

## Step 1: Add modules to `settings.gradle.kts`

```
include(":lib:sync:fe:ports")
include(":lib:sync:fe:app")
include(":lib:sync:fe:driven:impl")
include(":lib:sync:fe:driven:test")
```

## Step 2: Create `lib/sync/fe/ports` (lib-internal)

**`build.gradle.kts`** — `snagFrontendMultiplatformModule`, no extra deps.

**Files** (package `cz.adamec.timotej.snag.lib.sync.fe.ports`):

- `SyncOperationType` — enum: `UPSERT`, `DELETE`
- `SyncOperation` — data class: `id: Uuid`, `entityType: String`, `entityId: Uuid`, `operationType: SyncOperationType`
- `SyncQueue` — storage interface:
  - `suspend fun enqueue(entityType, entityId, operationType)` — deduplicates by (entityType, entityId)
  - `suspend fun getAllPending(): List<SyncOperation>` — FIFO ordered
  - `suspend fun remove(operationId: Uuid)`

## Step 3: Create `lib/sync/fe/driven/test`

**`build.gradle.kts`** — `snagFrontendMultiplatformModule`, depends on `:lib:sync:fe:ports`.

**File:** `FakeSyncQueue` — in-memory list implementing `SyncQueue`. On enqueue with matching (entityType, entityId), updates operationType in place.

**File:** `FakeEnqueueSyncOperation` — implements `EnqueueSyncOperation` (from `lib/sync/fe/app`), records enqueued operations for test assertions.

**Modify:** `build.gradle.kts` — also depends on `:lib:sync:fe:app`.

## Step 4: Create `lib/sync/fe/driven/impl`

**`build.gradle.kts`** — `snagFrontendMultiplatformModule`, depends on `:lib:sync:fe:ports` and `:feat:shared:database:fe`.

**File:** `RealSyncQueue` — wraps `SyncOperationEntityQueries`, uses `ioDispatcher` and `UuidProvider`.

**Koin module:** `syncDrivenModule` — registers `RealSyncQueue` bound to `SyncQueue`.

## Step 5: Add SQLDelight table

**File:** `feat/shared/database/fe/src/commonMain/sqldelight/.../SyncOperationEntity.sq`

```sql
CREATE TABLE IF NOT EXISTS SyncOperationEntity (
    id TEXT NOT NULL PRIMARY KEY,
    entityType TEXT NOT NULL,
    entityId TEXT NOT NULL,
    operationType TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sync_entity
    ON SyncOperationEntity(entityType, entityId);

enqueue:
INSERT INTO SyncOperationEntity(id, entityType, entityId, operationType)
VALUES (?, ?, ?, ?)
ON CONFLICT(entityType, entityId) DO UPDATE SET
    operationType = excluded.operationType;

selectAllPending:
SELECT * FROM SyncOperationEntity ORDER BY rowid ASC;

deleteById:
DELETE FROM SyncOperationEntity WHERE id = ?;
```

ON CONFLICT keeps existing row's rowid (FIFO position) and id, only updates operationType.

**Modify:** `DatabaseModule.kt` — add factory for `SyncOperationEntityQueries`.

## Step 6: Create `lib/sync/fe/app`

**`build.gradle.kts`** — `snagFrontendMultiplatformModule`, depends on `:lib:sync:fe:ports`. Test dep on `:lib:sync:fe:driven:test`.

**Files** (package `cz.adamec.timotej.snag.lib.sync.fe.app`):

- `SyncOperationType` — re-exports or references the enum from ports (or define here and ports uses it). Enum: `UPSERT`, `DELETE`
- `SyncOperationResult` — sealed interface: `Success`, `EntityNotFound`, `Failure`
- `EnqueueSyncOperation` — interface: `suspend fun enqueue(entityType: String, entityId: Uuid, operationType: SyncOperationType)`
- `SyncOperationHandler` — interface: `val entityType: String`, `suspend fun execute(entityId: Uuid, operationType: SyncOperationType): SyncOperationResult`
- `SyncEngine` — implements `EnqueueSyncOperation`. Takes `SyncQueue`, `List<SyncOperationHandler>`, `ApplicationScope`:
  - `enqueue(...)` — delegates to `SyncQueue.enqueue()`, launches `processAll()` in `applicationScope`
  - `processAll()` — guarded by `Mutex`; iterates pending ops in FIFO order; finds handler by entityType (throws if not found — programmer error); calls `execute()`; removes on Success/EntityNotFound; **stops on first Failure** to preserve FIFO dependency ordering
- `di/SyncAppModule.kt` — registers `SyncEngine` as `single` (uses `getAll<SyncOperationHandler>()` for handlers), binds to `EnqueueSyncOperation`

## Step 7: Write SyncEngine tests

**File:** `lib/sync/fe/app/src/commonTest/.../SyncEngineTest.kt`

Test with `FakeSyncQueue` and a `TestSyncHandler` stub:
- Successful op is removed from queue
- Failed op stays in queue and stops processing
- EntityNotFound discards op and continues to next
- Missing handler throws
- Deduplication works (enqueue same entity twice → one op)
- New enqueue triggers retry of previously failed ops

## Step 8: Add `ProjectsSync` to feature ports

**File:** `feat/projects/fe/ports/.../ProjectsSync.kt`

```kotlin
interface ProjectsSync {
    suspend fun enqueueProjectSave(projectId: Uuid)
    suspend fun enqueueProjectDelete(projectId: Uuid)
}
```

No sync lib dependency needed — domain-specific methods avoid referencing `SyncOperationType`.

## Step 9: Add `FakeProjectsSync` to feature driven test

**File:** `feat/projects/fe/driven/test/.../FakeProjectsSync.kt`

Records calls for assertions.

## Step 10: Create `ProjectSyncHandler` and `RealProjectsSync` in feature driven impl

**Modify:** `feat/projects/fe/driven/impl/build.gradle.kts` — add dep on `:lib:sync:fe:app`.

**File:** `feat/projects/fe/driven/impl/.../ProjectSyncHandler.kt`

- `entityType = "project"`
- UPSERT: read from `projectsDb.getProjectFlow(entityId).first()`, if null return EntityNotFound, else call `projectsApi.saveProject()`, if API returns updated DTO save to DB, return Success/Failure
- DELETE: call `projectsApi.deleteProject(entityId)`, return Success/Failure

**File:** `feat/projects/fe/driven/impl/.../RealProjectsSync.kt`

Delegates to `EnqueueSyncOperation.enqueue("project", projectId, operationType)`.

**Modify:** `feat/projects/fe/driven/impl/di/ProjectsDrivenModule.kt` — register `ProjectSyncHandler` bound to `SyncOperationHandler`, register `RealProjectsSync` bound to `ProjectsSync`.

## Step 11: Modify `SaveProjectUseCase`

- Remove `projectsApi` and `applicationScope` constructor params
- Add `projectsSync: ProjectsSync`
- Replace `applicationScope.launch { ... }` with `projectsSync.enqueueProjectSave(project.id)` after successful DB save

## Step 12: Modify `DeleteProjectUseCase`

- Remove `projectsApi` and `applicationScope` constructor params
- Add `projectsSync: ProjectsSync`
- Replace `applicationScope.launch { ... }` with `projectsSync.enqueueProjectDelete(projectId)` after successful DB delete

## Step 13: Update Koin

**`AppModule.kt`** — add `syncDrivenModule` and `syncAppModule` to includes.

## Step 14: Update existing tests

**`ProjectDetailsEditViewModelTest.kt`**:
- Replace `projectsApi` + `applicationScope` with `FakeProjectsSync`
- Add test dep on `:lib:sync:fe:driven:test` in `feat/projects/fe/driving/impl/build.gradle.kts` (if needed, may only need `FakeProjectsSync` from `feat/projects/fe/driven/test`)

## Step 15: Write ProjectSyncHandler tests

**File:** `feat/projects/fe/driven/impl/src/commonTest/.../ProjectSyncHandlerTest.kt`

Uses `FakeProjectsDb` and `FakeProjectsApi`:
- UPSERT reads from DB, calls API, returns Success
- UPSERT with API returning fresher DTO saves it to DB
- UPSERT when entity not in DB returns EntityNotFound
- UPSERT when API fails returns Failure
- DELETE calls API, returns Success
- DELETE when API fails returns Failure

## Step 16: Verify

```bash
./gradlew check --no-daemon
```

## Files Modified

| File | Change |
|------|--------|
| `settings.gradle.kts` | Add 4 module includes |
| `feat/shared/database/fe/.../di/DatabaseModule.kt` | Register `SyncOperationEntityQueries` |
| `feat/projects/fe/driven/impl/build.gradle.kts` | Add dep on `:lib:sync:fe:app` |
| `feat/projects/fe/driven/impl/.../di/ProjectsDrivenModule.kt` | Register handler + sync port |
| `feat/projects/fe/app/.../SaveProjectUseCase.kt` | Replace API call with sync port |
| `feat/projects/fe/app/.../DeleteProjectUseCase.kt` | Replace API call with sync port |
| `composeApp/.../di/AppModule.kt` | Include sync modules |
| `feat/projects/fe/driving/impl/build.gradle.kts` | Test dep adjustment |
| `feat/projects/fe/driving/impl/.../ProjectDetailsEditViewModelTest.kt` | Use `FakeProjectsSync` |

## Files Created

| File | Purpose |
|------|---------|
| `lib/sync/fe/ports/build.gradle.kts` | Module config |
| `lib/sync/fe/ports/.../SyncOperationType.kt` | Enum |
| `lib/sync/fe/ports/.../SyncOperation.kt` | Data class |
| `lib/sync/fe/ports/.../SyncQueue.kt` | Storage port interface (lib-internal) |
| `lib/sync/fe/driven/test/build.gradle.kts` | Module config |
| `lib/sync/fe/driven/test/.../FakeSyncQueue.kt` | Test fake |
| `lib/sync/fe/driven/test/.../FakeEnqueueSyncOperationUseCase.kt` | Test fake |
| `lib/sync/fe/driven/impl/build.gradle.kts` | Module config |
| `lib/sync/fe/driven/impl/.../RealSyncQueue.kt` | SQLite implementation |
| `lib/sync/fe/driven/impl/.../di/SyncDrivenModule.kt` | Koin module |
| `feat/shared/database/fe/.../SyncOperationEntity.sq` | SQLDelight table |
| `lib/sync/fe/app/build.gradle.kts` | Module config |
| `lib/sync/fe/app/.../SyncOperationType.kt` | Enum (public API) |
| `lib/sync/fe/app/.../SyncOperationResult.kt` | Result sealed interface (public API) |
| `lib/sync/fe/app/.../EnqueueSyncOperationUseCase.kt` | Enqueue interface (public API) |
| `lib/sync/fe/app/.../SyncOperationHandler.kt` | Handler interface (public API) |
| `lib/sync/fe/app/.../SyncEngine.kt` | Core sync engine |
| `lib/sync/fe/app/.../di/SyncAppModule.kt` | Koin module |
| `lib/sync/fe/app/.../SyncEngineTest.kt` | Engine tests |
| `feat/projects/fe/ports/.../ProjectsSync.kt` | Feature sync port |
| `feat/projects/fe/driven/test/.../FakeProjectsSync.kt` | Test fake |
| `feat/projects/fe/driven/impl/.../ProjectSyncHandler.kt` | Handler impl |
| `feat/projects/fe/driven/impl/.../RealProjectsSync.kt` | Sync port impl |
| `feat/projects/fe/driven/impl/.../ProjectSyncHandlerTest.kt` | Handler tests |
