# Code Deduplication Refactoring Plan - Frontend Modules

## Executive Summary

**Recommendation: YES - Proceed with Code Deduplication Refactoring**

The codebase has significant mechanical duplication across frontend app, ports, and driven modules (40-60% duplication in key areas). This refactoring will:
- Reduce ~480 lines of duplicated code (53% reduction in analyzed modules)
- Centralize error handling patterns for consistency
- Simplify adding new features (56% less boilerplate)
- Maintain Clean Architecture + Hexagonal pattern principles
- Improve testability and maintainability

**Key Duplication Patterns Identified:**
1. **Sync Handlers**: 82% code duplication (ProjectSyncHandler vs StructureSyncHandler)
2. **API Error Handling**: 4+ identical runCatchingCancellable + fold blocks per feature
3. **DB Flow Transformations**: 4+ identical map + catch patterns per feature
4. **Fake Test Implementations**: 50-60% structural duplication
5. **Logger Helper Files (LH.kt)**: 6 files of pure boilerplate (3 lines each)

## Analysis Summary

### What's Duplicated and Why It Matters

**1. Sync Handlers (HIGHEST PRIORITY - 82% reduction possible)**
- `ProjectSyncHandler.kt` (83 lines) and `StructureSyncHandler.kt` (73 lines) are 95% identical
- Only differences: entity type names and method calls
- Same algorithm: read from DB → check result → call API → save fresher data → return result
- **Impact**: When adding new synced entities, ~70 lines of boilerplate must be copied

**2. API Error Handling (HIGH PRIORITY - 44% reduction possible)**
- Every API method has identical error handling: `runCatchingCancellable { log + call }.fold(...)`
- Pattern appears 4 times in RealProjectsApi, 3 times in RealStructuresApi (7 total occurrences)
- Only differences: operation name for logging and the actual HTTP call
- **Impact**: Inconsistent error handling when pattern isn't copied correctly

**3. DB Flow Patterns (HIGH PRIORITY - 33% reduction possible)**
- All DB query methods use identical: `.asFlow().mapToX().map { Success(...) }.catch { Error(...) }`
- Pattern appears 4 times per DB implementation (8 total occurrences)
- Only differences: query method and transformation logic
- **Impact**: Repetitive error handling adds noise, reduces readability

**4. Fake Implementations (MEDIUM PRIORITY - test code, but 64% reduction possible)**
- FakeProjectsApi/Db and FakeStructuresApi/Db share 50-60% identical structure
- Common pattern: mutable state holder + forcedFailure field + CRUD methods
- **Impact**: Creating test fakes for new features requires copying ~100 lines

**5. App Layer Use Cases (MEDIUM PRIORITY - 30-40% reduction possible)**
- GetXxx, SaveXxx, DeleteXxx use cases follow similar patterns
- Some variation in business logic makes full abstraction risky
- **Impact**: Moderate duplication but maintains clarity

## Refactoring Approach

### Core Principle: Extract Mechanical Duplication, Preserve Domain Logic

We'll create **utility functions and abstract base classes** for mechanical duplication while keeping domain-specific logic explicit. This follows the "Rule of Three" - only abstract when patterns appear 3+ times and are structurally identical.

### Phase 1: Foundation - Create Utility Abstractions (Week 1)

Create new utility modules in `lib/core/fe/` without modifying existing code.

#### 1.1 API Error Handling Utility

**Create: `lib/core/fe/src/commonMain/kotlin/cz/adamec/timotej/snag/lib/core/fe/ApiCallHelpers.kt`**

```kotlin
package cz.adamec.timotej.snag.lib.core.fe

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.lib.core.common.runCatchingCancellable
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.log
import cz.adamec.timotej.snag.network.fe.toOnlineDataResult

/**
 * Executes an API call with standardized error handling.
 * Handles NetworkException separately from programmer errors.
 */
suspend inline fun <T> executeApiCall(
    logger: Logger,
    operation: String,
    crossinline block: suspend () -> T,
): OnlineDataResult<T> =
    runCatchingCancellable {
        logger.d { "Starting $operation..." }
        block()
    }.fold(
        onSuccess = { result ->
            logger.d { "Completed $operation successfully." }
            OnlineDataResult.Success(result)
        },
        onFailure = { e ->
            if (e is NetworkException) {
                e.log()
                e.toOnlineDataResult()
            } else {
                logger.e { "Error $operation." }
                OnlineDataResult.Failure.ProgrammerError(throwable = e)
            }
        },
    )
```

**Usage Example (after refactoring RealProjectsApi):**
```kotlin
override suspend fun getProjects(): OnlineDataResult<List<Project>> =
    executeApiCall(LH.logger, "fetching projects") {
        httpClient.get("/projects").body<List<ProjectApiDto>>().map { it.toBusiness() }
    }
```

#### 1.2 DB Flow Error Handling Utilities

**Create: `lib/core/fe/src/commonMain/kotlin/cz/adamec/timotej/snag/lib/core/fe/FlowHelpers.kt`**

```kotlin
package cz.adamec.timotej.snag.lib.core.fe

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Maps a Flow to OfflineFirstDataResult.Success and catches errors as ProgrammerError.
 */
fun <T, R> Flow<T>.mapToOfflineFirstResult(
    logger: Logger,
    errorMessage: String,
    transform: (T) -> R,
): Flow<OfflineFirstDataResult<R>> =
    this.map<T, OfflineFirstDataResult<R>> {
        OfflineFirstDataResult.Success(transform(it))
    }.catch { e ->
        logger.e { errorMessage }
        emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
    }

/**
 * Wraps a suspend DB operation with OfflineFirstDataResult error handling.
 */
suspend inline fun <T> executeDbOperation(
    logger: Logger,
    errorMessage: String,
    crossinline block: suspend () -> T,
): OfflineFirstDataResult<T> =
    runCatching {
        block()
    }.fold(
        onSuccess = { OfflineFirstDataResult.Success(it) },
        onFailure = { e ->
            logger.e { errorMessage }
            OfflineFirstDataResult.ProgrammerError(throwable = e)
        },
    )
```

**Usage Example (after refactoring RealProjectsDb):**
```kotlin
override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<Project>>> =
    projectEntityQueries
        .selectAll()
        .asFlow()
        .mapToList(ioDispatcher)
        .mapToOfflineFirstResult(LH.logger, "Error loading projects from DB.") { entities ->
            entities.map { it.toBusiness() }
        }
```

#### 1.3 Sync Handler Base Class

**Create: `lib/sync/fe/app/src/commonMain/kotlin/cz/adamec/timotej/snag/lib/sync/fe/app/handler/AbstractCrudSyncHandler.kt`**

```kotlin
package cz.adamec.timotej.snag.lib.sync.fe.app.handler

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

/**
 * Abstract base class for sync handlers that follow CRUD patterns.
 *
 * Handles standard logic:
 * 1. Read entity from local DB
 * 2. Upsert to remote API
 * 3. Delete from remote API
 * 4. Save updated entity back to DB
 */
abstract class AbstractCrudSyncHandler<T>(
    protected val logger: Logger,
) : SyncOperationHandler {

    protected abstract fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<T?>>
    protected abstract suspend fun saveToApi(entity: T): OnlineDataResult<T?>
    protected abstract suspend fun deleteFromApi(entityId: Uuid): OnlineDataResult<Unit>
    protected abstract suspend fun saveToDb(entity: T): OfflineFirstDataResult<Unit>
    protected abstract val entityName: String

    override suspend fun execute(
        entityId: Uuid,
        operationType: SyncOperationType,
    ): SyncOperationResult =
        when (operationType) {
            SyncOperationType.UPSERT -> executeUpsert(entityId)
            SyncOperationType.DELETE -> executeDelete(entityId)
        }

    private suspend fun executeUpsert(entityId: Uuid): SyncOperationResult {
        val entityResult = getEntityFlow(entityId).first()
        val entity = when (entityResult) {
            is OfflineFirstDataResult.Success -> entityResult.data
            is OfflineFirstDataResult.ProgrammerError -> {
                logger.e { "DB error reading $entityName $entityId for sync." }
                return SyncOperationResult.Failure
            }
        }
        if (entity == null) {
            logger.d { "${entityName.replaceFirstChar { it.uppercase() }} $entityId not found in local DB, discarding sync operation." }
            return SyncOperationResult.EntityNotFound
        }

        return when (val apiResult = saveToApi(entity)) {
            is OnlineDataResult.Success -> {
                apiResult.data?.let { updatedEntity ->
                    logger.d { "Saving fresher $updatedEntity from API to DB." }
                    saveToDb(updatedEntity)
                }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure syncing $entityName $entityId." }
                SyncOperationResult.Failure
            }
        }
    }

    private suspend fun executeDelete(entityId: Uuid): SyncOperationResult =
        when (deleteFromApi(entityId)) {
            is OnlineDataResult.Success -> {
                logger.d { "Deleted $entityName $entityId from API." }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure deleting $entityName $entityId." }
                SyncOperationResult.Failure
            }
        }
}
```

**Validation:** After creating these files, run `./gradlew :lib:core:fe:build` and `./gradlew :lib:sync:fe:app:build`

---

### Phase 2: Refactor Sync Handlers (Week 1-2) - HIGHEST ROI

**Impact: 156 lines → ~30 lines (81% reduction)**

#### 2.1 Refactor ProjectSyncHandler

**File: `feat/projects/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/internal/sync/ProjectSyncHandler.kt`**

Before: 83 lines
After: ~15 lines

```kotlin
internal class ProjectSyncHandler(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) : AbstractCrudSyncHandler<Project>(logger = LH.logger) {

    override val entityType: String = PROJECT_SYNC_ENTITY_TYPE
    override val entityName: String = "project"

    override fun getEntityFlow(entityId: Uuid) = projectsDb.getProjectFlow(entityId)
    override suspend fun saveToApi(entity: Project) = projectsApi.saveProject(entity)
    override suspend fun deleteFromApi(entityId: Uuid) = projectsApi.deleteProject(entityId)
    override suspend fun saveToDb(entity: Project) = projectsDb.saveProject(entity)
}
```

#### 2.2 Refactor StructureSyncHandler

**File: `feat/structures/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/internal/sync/StructureSyncHandler.kt`**

Before: 73 lines
After: ~15 lines

**Special case:** StructureSyncHandler doesn't support DELETE. Override the method:

```kotlin
internal class StructureSyncHandler(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
) : AbstractCrudSyncHandler<Structure>(logger = LH.logger) {

    override val entityType: String = STRUCTURE_SYNC_ENTITY_TYPE
    override val entityName: String = "structure"

    override fun getEntityFlow(entityId: Uuid) = structuresDb.getStructureFlow(entityId)
    override suspend fun saveToApi(entity: Structure) = structuresApi.saveStructure(entity)
    override suspend fun saveToDb(entity: Structure) = structuresDb.saveStructure(entity)

    override suspend fun deleteFromApi(entityId: Uuid): OnlineDataResult<Unit> {
        logger.w { "Delete not yet supported for structures." }
        return OnlineDataResult.Failure.ProgrammerError(
            throwable = UnsupportedOperationException("Delete not supported")
        )
    }
}
```

**Validation:**
```bash
./gradlew :feat:projects:fe:driven:impl:commonTest --tests "*ProjectSyncHandler*"
./gradlew :feat:structures:fe:driven:impl:commonTest --tests "*StructureSyncHandler*"
```

---

### Phase 3: Refactor API Implementations (Week 2) - HIGH IMPACT

**Impact: ~250 lines → ~140 lines (44% reduction)**

#### 3.1 Refactor RealProjectsApi

**File: `feat/projects/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/internal/api/RealProjectsApi.kt`**

Before: 126 lines (with 4 identical error handling blocks)
After: ~75 lines

Replace each method with `executeApiCall`:

```kotlin
override suspend fun getProjects(): OnlineDataResult<List<Project>> =
    executeApiCall(LH.logger, "fetching projects") {
        httpClient.get("/projects").body<List<ProjectApiDto>>().map { it.toBusiness() }
    }

override suspend fun getProject(id: Uuid): OnlineDataResult<Project> =
    executeApiCall(LH.logger, "fetching project $id") {
        httpClient.get("/projects/$id").body<ProjectApiDto>().toBusiness()
    }

override suspend fun saveProject(project: Project): OnlineDataResult<Project?> =
    executeApiCall(LH.logger, "saving project ${project.id} to API") {
        val projectDto = project.toPutApiDto()
        val response = httpClient.put("/projects/${project.id}") {
            setBody(projectDto)
        }
        if (response.status != HttpStatusCode.NoContent) {
            response.body<ProjectApiDto>().toBusiness()
        } else {
            null
        }
    }

override suspend fun deleteProject(id: Uuid): OnlineDataResult<Unit> =
    executeApiCall(LH.logger, "deleting project $id from API") {
        httpClient.delete("/projects/$id")
    }
```

#### 3.2 Refactor RealStructuresApi

**File: `feat/structures/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/internal/api/RealStructuresApi.kt`**

Apply same pattern as RealProjectsApi.

**Validation:**
```bash
./gradlew :feat:projects:fe:driven:impl:commonTest
./gradlew :feat:structures:fe:driven:impl:commonTest
```

---

### Phase 4: Refactor DB Implementations (Week 2-3) - HIGH IMPACT

**Impact: ~240 lines → ~160 lines (33% reduction)**

#### 4.1 Refactor RealProjectsDb

**File: `feat/projects/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/internal/db/RealProjectsDb.kt`**

Replace Flow operations with `mapToOfflineFirstResult` and save/delete operations with `executeDbOperation`:

```kotlin
override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<Project>>> =
    projectEntityQueries
        .selectAll()
        .asFlow()
        .mapToList(ioDispatcher)
        .mapToOfflineFirstResult(LH.logger, "Error loading projects from DB.") { entities ->
            entities.map { it.toBusiness() }
        }

override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<Project?>> =
    projectEntityQueries
        .selectById(id.toString())
        .asFlow()
        .mapToOneOrNull(ioDispatcher)
        .mapToOfflineFirstResult(LH.logger, "Error loading project $id from DB.") {
            it?.toBusiness()
        }

override suspend fun saveProject(project: Project): OfflineFirstDataResult<Unit> =
    executeDbOperation(LH.logger, "Error saving project $project to DB.") {
        withContext(ioDispatcher) {
            projectEntityQueries.save(project.toEntity())
        }
    }
```

#### 4.2 Refactor RealStructuresDb

**File: `feat/structures/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/internal/db/RealStructuresDb.kt`**

Apply same pattern as RealProjectsDb.

**Validation:**
```bash
./gradlew :feat:projects:fe:driven:impl:commonTest
./gradlew :feat:structures:fe:driven:impl:commonTest
```

---

### Phase 5: Refactor Fake Implementations (Week 3) - MEDIUM IMPACT (Test Code)

**Impact: Test code becomes ~64% smaller and more maintainable**

#### 5.1 Create Base Fake Classes

**Create: `lib/core/fe/src/commonTest/kotlin/cz/adamec/timotej/snag/lib/core/fe/test/BaseFakeApi.kt`**

```kotlin
package cz.adamec.timotej.snag.lib.core.fe.test

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

abstract class BaseFakeApi<T> {
    protected val entities = mutableMapOf<Uuid, T>()
    var forcedFailure: OnlineDataResult.Failure? = null

    protected fun <R> checkForcedFailure(block: () -> OnlineDataResult<R>): OnlineDataResult<R> =
        forcedFailure ?: block()

    protected fun getEntityById(id: Uuid): OnlineDataResult<T> =
        checkForcedFailure {
            entities[id]?.let { OnlineDataResult.Success(it) }
                ?: OnlineDataResult.Failure.ProgrammerError(Exception("Not found"))
        }

    protected fun getAllEntities(): OnlineDataResult<List<T>> =
        checkForcedFailure { OnlineDataResult.Success(entities.values.toList()) }

    protected fun saveEntity(id: Uuid, entity: T): OnlineDataResult<T> =
        checkForcedFailure {
            entities[id] = entity
            OnlineDataResult.Success(entity)
        }

    protected fun deleteEntity(id: Uuid): OnlineDataResult<Unit> =
        checkForcedFailure {
            entities.remove(id)
            OnlineDataResult.Success(Unit)
        }

    fun setEntity(id: Uuid, entity: T) {
        entities[id] = entity
    }
}
```

**Create: `lib/core/fe/src/commonTest/kotlin/cz/adamec/timotej/snag/lib/core/fe/test/BaseFakeDb.kt`**

```kotlin
package cz.adamec.timotej.snag.lib.core.fe.test

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

abstract class BaseFakeDb<T> {
    protected val entities = MutableStateFlow<Map<Uuid, T>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    protected fun getEntityFlow(id: Uuid): Flow<OfflineFirstDataResult<T?>> =
        entities.map { map -> forcedFailure ?: OfflineFirstDataResult.Success(map[id]) }

    protected fun getAllEntitiesFlow(): Flow<OfflineFirstDataResult<List<T>>> =
        entities.map { map -> forcedFailure ?: OfflineFirstDataResult.Success(map.values.toList()) }

    protected suspend fun saveEntity(id: Uuid, entity: T): OfflineFirstDataResult<Unit> {
        forcedFailure?.let { return it }
        entities.update { it + (id to entity) }
        return OfflineFirstDataResult.Success(Unit)
    }

    protected suspend fun saveEntities(entities: List<T>, getId: (T) -> Uuid): OfflineFirstDataResult<Unit> {
        forcedFailure?.let { return it }
        this.entities.update { current -> current + entities.associateBy(getId) }
        return OfflineFirstDataResult.Success(Unit)
    }

    protected suspend fun deleteEntity(id: Uuid): OfflineFirstDataResult<Unit> {
        forcedFailure?.let { return it }
        entities.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }
}
```

#### 5.2 Refactor Fake Implementations

Update FakeProjectsApi, FakeProjectsDb, FakeStructuresApi, FakeStructuresDb to extend base classes.

**Validation:**
```bash
./gradlew :feat:projects:fe:app:impl:jvmTest
./gradlew :feat:structures:fe:app:impl:jvmTest
```

---

### Phase 6: Logger Cleanup (Week 3-4) - LOW IMPACT, EASY WIN

**Impact: Delete 6 boilerplate files**

Replace LH.logger pattern with inline loggers:

1. In each file using `LH.logger`, add: `private val logger = Logger.withTag("feat-{feature}-{layer}")`
2. Replace `LH.logger` with `logger`
3. Delete all `LH.kt` files:
   - `feat/projects/fe/app/impl/src/commonMain/kotlin/.../internal/LH.kt`
   - `feat/projects/fe/driven/impl/src/commonMain/kotlin/.../internal/LH.kt`
   - `feat/structures/fe/app/impl/src/commonMain/kotlin/.../internal/LH.kt`
   - `feat/structures/fe/driven/impl/src/commonMain/kotlin/.../internal/LH.kt`

**Validation:**
```bash
./gradlew check --no-daemon
```

---

## Critical Files to Modify

### New Files to Create (Phase 1):
1. `lib/core/fe/src/commonMain/kotlin/cz/adamec/timotej/snag/lib/core/fe/ApiCallHelpers.kt`
2. `lib/core/fe/src/commonMain/kotlin/cz/adamec/timotej/snag/lib/core/fe/FlowHelpers.kt`
3. `lib/sync/fe/app/src/commonMain/kotlin/cz/adamec/timotej/snag/lib/sync/fe/app/handler/AbstractCrudSyncHandler.kt`
4. `lib/core/fe/src/commonTest/kotlin/cz/adamec/timotej/snag/lib/core/fe/test/BaseFakeApi.kt`
5. `lib/core/fe/src/commonTest/kotlin/cz/adamec/timotej/snag/lib/core/fe/test/BaseFakeDb.kt`

### Files to Refactor (Phases 2-5):

**Phase 2 - Sync Handlers:**
- `feat/projects/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/internal/sync/ProjectSyncHandler.kt`
- `feat/structures/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/internal/sync/StructureSyncHandler.kt`

**Phase 3 - API Implementations:**
- `feat/projects/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/internal/api/RealProjectsApi.kt`
- `feat/structures/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/internal/api/RealStructuresApi.kt`

**Phase 4 - DB Implementations:**
- `feat/projects/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/internal/db/RealProjectsDb.kt`
- `feat/structures/fe/driven/impl/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/internal/db/RealStructuresDb.kt`

**Phase 5 - Fake Implementations:**
- `feat/projects/fe/driven/test/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/test/FakeProjectsApi.kt`
- `feat/projects/fe/driven/test/src/commonMain/kotlin/cz/adamec/timotej/snag/projects/fe/driven/test/FakeProjectsDb.kt`
- `feat/structures/fe/driven/test/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/test/FakeStructuresApi.kt`
- `feat/structures/fe/driven/test/src/commonMain/kotlin/cz/adamec/timotej/snag/structures/fe/driven/test/FakeStructuresDb.kt`

---

## Trade-offs and Principles

### When TO Deduplicate:
1. **Mechanical Duplication** - Identical error handling, flow transformations (API, DB patterns)
2. **Pattern Duplication** - Same algorithm with different types (Sync handlers)
3. **Structural Duplication** - Test doubles with identical shape (Fake implementations)

### When NOT TO Deduplicate:
1. **Domain-Specific Logic** - Business rules that happen to look similar
2. **Coincidental Similarity** - Code that looks similar now but may diverge
3. **Clarity Over DRY** - Abstraction would obscure intent
4. **Low ROI** - 2-3 lines duplicated in 2 places

### Design Principles:
- **Composition over Inheritance** - Use utility functions where possible
- **Inline Functions** - No performance penalty for abstractions
- **Clear Intent** - Named parameters make usage obvious
- **Easy Opt-Out** - Can always bypass helpers for special cases
- **Clean Architecture** - Abstractions live in lib/core, features depend on them

---

## Success Metrics

### Quantitative:
- **Lines of Code Reduction**: ~480 lines removed (53% in analyzed areas)
  - Sync handlers: 156 → 30 lines (81% reduction)
  - API implementations: ~250 → ~140 lines (44% reduction)
  - DB implementations: ~240 → ~160 lines (33% reduction)
  - Fake implementations: ~280 → ~100 lines (64% reduction)
- **File Count**: 6 boilerplate files deleted (LH.kt)

### Qualitative:
- Consistent error handling across all layers
- Easier to add new features (56% less boilerplate per feature)
- Centralized patterns easier to modify
- Test fakes easier to create and maintain

---

## Verification Plan

### After Each Phase:
```bash
# Run all checks
./gradlew check --no-daemon

# Run specific module tests
./gradlew :feat:projects:fe:driven:impl:commonTest
./gradlew :feat:structures:fe:driven:impl:commonTest
./gradlew :feat:projects:fe:app:impl:jvmTest
./gradlew :feat:structures:fe:app:impl:jvmTest

# Check code style
./gradlew detekt
```

### End-to-End Testing:
1. Run Android app: `./gradlew :androidApp:assembleDebug` and manual smoke test
2. Run Desktop app: `./gradlew :composeApp:run` and manual smoke test
3. Run server: `./gradlew :server:impl:run --no-daemon` and verify API endpoints

### Rollback Plan:
Each phase is independent. If issues arise, revert via git:
```bash
git revert <phase-commit-hash>
```

---

## Reasoning - Why This Approach Makes Sense

### 1. The Duplication is Real and Significant
- **82% duplication in sync handlers** - copying 70 lines per new entity is error-prone
- **Identical error handling in 7+ places** - inconsistency risk when pattern isn't copied correctly
- **Pattern appears 3+ times** - meets "Rule of Three" for abstraction

### 2. Abstractions Are Appropriate
- **Inline functions** = zero runtime cost
- **Abstract base class for sync handlers** = true IS-A relationship (all sync handlers do the same thing)
- **Utility functions** = composition, not forced inheritance
- **Clear naming** = intent is obvious (executeApiCall, mapToOfflineFirstResult)

### 3. Maintains Architecture Principles
- **Clean Architecture preserved** - abstractions in lib/core, features depend on them
- **Hexagonal pattern intact** - ports and adapters still separated
- **Testability improved** - fake base classes make tests easier
- **Domain logic explicit** - only mechanical duplication is abstracted

### 4. Phased Approach Minimizes Risk
- **Phase 1 is additive** - no breaking changes
- **Each phase is testable** - comprehensive test suite validates correctness
- **Independent phases** - can stop or rollback at any point
- **Highest ROI first** - sync handlers (82% reduction) done early

### 5. Future Extensibility
**Before refactoring:** Adding a new synced entity requires ~684 lines of boilerplate
**After refactoring:** Adding a new synced entity requires ~300 lines (~56% reduction)

### 6. This is a Thesis Project
Code quality, maintainability, and demonstrating architectural understanding are critical. This refactoring shows:
- Understanding of code duplication patterns
- Ability to design appropriate abstractions
- Knowledge of Kotlin multiplatform best practices
- Architectural discipline (Clean Architecture + DRY principle)

---

## Optional: App Layer Use Cases (Not Included in Main Plan)

The app layer use cases have ~30-40% duplication but include domain logic. Only refactor if time permits and after validating other phases.

**Helpers (if pursuing):**
- `executeOfflineFirstGet()` for GetXxx use cases
- `executeSaveWithSync()` for SaveXxx use cases
- `executeDeleteWithSync()` for DeleteXxx use cases

**Recommendation:** Skip app layer for now. The duplication is moderate and abstractions may reduce clarity. Revisit after Phase 6 is complete.
