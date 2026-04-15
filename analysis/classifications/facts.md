<!--
Copyright (c) 2026 Timotej Adamec
SPDX-License-Identifier: MIT

Thesis: "Multiplatform snagging system with code sharing maximisation"
Czech Technical University in Prague — Faculty of Information Technology
-->

# Case 1b — Before-snapshot of the counterfactual target flows

**Base commit:** `main` @ `e076e89e51a11bf41e2a791079786f8bb5ee746c`.

**Captured:** 2026-04-15 (same day this file is authored, before `experiment/commonize-photo` or any progress-callback evolution branch exists).

**Purpose (plan §D, line 469):** a precise "before" snapshot of the `NonWeb*` / `Web*` photo-upload flows so the counterfactual has a fixed reference for comparison. Any claim of the shape "the commonized branch ripples through N more files" is resolved against this file.

---

## 1. AddFindingPhoto — current split

The flow exists in two structurally distinct placements. Source set is the discriminant: `nonWebMain` (5 native FE targets: android, iosX64, iosSimulatorArm64, jvm-desktop, wasmJs — wait, let me restate: `nonWebMain` covers all non-JS FE targets, `webMain` covers only js + wasmJs per build-logic path-based inference).

### 1a. Interface files (shape-defining)

| File | LOC | Return type |
|---|---|---|
| `feat/findings/fe/app/api/src/nonWebMain/kotlin/cz/adamec/timotej/snag/findings/fe/app/api/NonWebAddFindingPhotoUseCase.kt` | 20 | `OfflineFirstDataResult<Uuid>` |
| `feat/findings/fe/app/api/src/webMain/kotlin/cz/adamec/timotej/snag/findings/fe/app/api/WebAddFindingPhotoUseCase.kt` | 20 | `OnlineDataResult<Uuid>` |

Body of each interface: a single-method `suspend operator fun invoke(request: AddFindingPhotoRequest): <ReturnType>`. The *only* structural difference between the two files is the return-type import + one declaration line. Request DTO (`AddFindingPhotoRequest`) is shared commonMain.

### 1b. Implementations

| File | LOC | Key dependency | Notes |
|---|---|---|---|
| `feat/findings/fe/app/impl/src/nonWebMain/kotlin/.../NonWebAddFindingPhotoUseCaseImpl.kt` | 83 | `LocalFileStorage` | Saves to disk first; wraps `CancellationException` + generic `Exception` into `OfflineFirstDataResult.ProgrammerError`; enqueues sync save on success. |
| `feat/findings/fe/app/impl/src/webMain/kotlin/.../WebAddFindingPhotoUseCaseImpl.kt` | 82 | `RemoteFileStorage` | Uploads directly; returns `OnlineDataResult.Failure` (which includes `NetworkUnavailable`, `UserMessageError`, `ProgrammerError`) if upload fails; enqueues sync save only after successful upload + DB write. |

### 1c. Tests (independent)

| File | LOC |
|---|---|
| `feat/findings/fe/app/impl/src/nonWebTest/kotlin/.../NonWebAddFindingPhotoUseCaseImplTest.kt` | 176 |
| `feat/findings/fe/app/impl/src/webTest/kotlin/.../WebAddFindingPhotoUseCaseImplTest.kt` | 154 |

Each test file covers only its platform's semantics. No shared commonTest fixture.

### 1d. ViewModels (callers)

| File | LOC | Uses | When-match shape |
|---|---|---|---|
| `feat/findings/fe/driving/impl/src/nonWebMain/kotlin/.../NonWebFindingDetailViewModel.kt` | 72 | `NonWebAddFindingPhotoUseCase` | `when (result) is OfflineFirstDataResult.ProgrammerError → error; is Success → noop`. Exhaustive on 2 variants. |
| `feat/findings/fe/driving/impl/src/webMain/kotlin/.../WebFindingDetailViewModel.kt` | 84 | `WebAddFindingPhotoUseCase` + `CanModifyFindingPhotosUseCase` | `when (result) is Success → noop; is Failure → error`. Exhaustive on 2 variants (Failure is a sealed supertype covering 3 sub-cases). Extra `collectCanModifyPhotos()` Job subscribes to connectivity. |

**Asymmetry:** web VM has an extra `CanModifyFindingPhotosUseCase` dependency the native VM does not need (offline-first always allows modification; online only allows when connected).

### 1e. DI bindings

| File | LOC | Binds |
|---|---|---|
| `feat/findings/fe/app/impl/src/nonWebMain/kotlin/.../di/FindingsAppModule.nonWeb.kt` | 28 | `NonWebAddFindingPhotoUseCaseImpl` + `NonWebFindingPhotoSyncHandler` |
| `feat/findings/fe/app/impl/src/webMain/kotlin/.../di/FindingsAppModule.web.kt` | 31 | `WebAddFindingPhotoUseCaseImpl` + `WebFindingPhotoSyncHandler` + `CanModifyFindingPhotosUseCaseImpl` |
| `feat/findings/fe/driving/impl/src/nonWebMain/kotlin/.../di/FindingsDrivingModule.nonWeb.kt` | 43 | `NonWebFindingDetailViewModel` factory |
| `feat/findings/fe/driving/impl/src/webMain/kotlin/.../di/FindingsDrivingModule.web.kt` | 44 | `WebFindingDetailViewModel` factory |

**Sync handler also splits:** `NonWebFindingPhotoSyncHandler` (uploads the local file when online) vs `WebFindingPhotoSyncHandler` (sync-enqueue is a no-op marker because the upload already happened synchronously in the use case). Counted in the DI files because Koin binding lines are the only surface from the point-of-view of this flow.

---

## 2. AddProjectPhoto — current split (replicate target)

Structurally identical to AddFindingPhoto. Table replicates the same shape with project-specific paths.

### 2a. Interface files

| File | LOC | Return type |
|---|---|---|
| `feat/projects/fe/app/api/src/nonWebMain/kotlin/.../NonWebAddProjectPhotoUseCase.kt` | 20 | `OfflineFirstDataResult<Uuid>` |
| `feat/projects/fe/app/api/src/webMain/kotlin/.../WebAddProjectPhotoUseCase.kt` | 20 | `OnlineDataResult<Uuid>` |

### 2b. Implementations

| File | LOC | Key dependency |
|---|---|---|
| `feat/projects/fe/app/impl/src/nonWebMain/kotlin/.../NonWebAddProjectPhotoUseCaseImpl.kt` | 88 | `LocalFileStorage` |
| `feat/projects/fe/app/impl/src/webMain/kotlin/.../WebAddProjectPhotoUseCaseImpl.kt` | 87 | `RemoteFileStorage` |

Body shape matches the findings impls line-for-line except for field names (`AppProjectPhotoData`, `ProjectPhotosDb`, `PROJECT_PHOTO_SYNC_ENTITY_TYPE`, etc.) and the extra `description` field on the request. Project photo also carries `updatedAt` instead of `createdAt` (projects use `Versioned`, findings photos use creation timestamp only).

### 2c. Tests

| File | LOC |
|---|---|
| `feat/projects/fe/app/impl/src/nonWebTest/kotlin/.../NonWebAddProjectPhotoUseCaseImplTest.kt` | 131 |
| `feat/projects/fe/app/impl/src/webTest/kotlin/.../WebAddProjectPhotoUseCaseImplTest.kt` | 115 |

### 2d. ViewModels

| File | LOC | Uses |
|---|---|---|
| `feat/projects/fe/driving/impl/src/nonWebMain/kotlin/.../NonWebProjectDetailsViewModel.kt` | 113 | `NonWebAddProjectPhotoUseCase` (no connectivity gate) |
| `feat/projects/fe/driving/impl/src/webMain/kotlin/.../WebProjectDetailsViewModel.kt` | 125 | `WebAddProjectPhotoUseCase` + `CanModifyProjectFilesUseCase` (extra collectCanModifyPhotos job) |

Same asymmetry as findings: web VM pulls in the `CanModifyProjectFilesUseCase` connectivity gate.

### 2e. DI bindings

| File | LOC | Binds |
|---|---|---|
| `feat/projects/fe/app/impl/src/nonWebMain/kotlin/.../di/ProjectsAppModule.nonWeb.kt` | 31 | `NonWebAddProjectPhotoUseCaseImpl` + `NonWebProjectPhotoSyncHandler` + `NonWebCanModifyProjectFilesUseCaseImpl` |
| `feat/projects/fe/app/impl/src/webMain/kotlin/.../di/ProjectsAppModule.web.kt` | 31 | `WebAddProjectPhotoUseCaseImpl` + `WebProjectPhotoSyncHandler` + `WebCanModifyProjectFilesUseCaseImpl` |
| `feat/projects/fe/driving/impl/src/nonWebMain/kotlin/.../di/ProjectsDrivingModule.nonWeb.kt` | 58 | `NonWebProjectDetailsViewModel` factory |
| `feat/projects/fe/driving/impl/src/webMain/kotlin/.../di/ProjectsDrivingModule.web.kt` | 59 | `WebProjectDetailsViewModel` factory |

**Note:** projects split `CanModifyProjectFilesUseCase` itself across Web/NonWeb (`NonWebCanModifyProjectFilesUseCaseImpl` vs `WebCanModifyProjectFilesUseCaseImpl`) — findings does *not* (`CanModifyFindingPhotosUseCase` has a single commonMain impl). Projects went further with the split pattern; this asymmetry is captured here for completeness and does not affect the counterfactual target (we only commonize the `AddX*PhotoUseCase` flow).

---

## 3. Shared (commonMain) infrastructure consumed by both flows

The counterfactual must not touch these; they are the upstream dependency surface.

| File | LOC | Role |
|---|---|---|
| `core/network/fe/src/commonMain/kotlin/.../OnlineDataResult.kt` | 77 | `sealed interface OnlineDataResult<out T>` with `Success` + 3 `Failure` sub-cases (`NetworkUnavailable`, `UserMessageError`, `ProgrammerError`). Web-only return shape. |
| `core/network/fe/src/commonMain/kotlin/.../OfflineFirstDataResult.kt` | 55 | `sealed interface OfflineFirstDataResult<out T>` with `Success` + `ProgrammerError` only. NonWeb-only return shape. |
| `core/storage/fe/src/commonMain/kotlin/.../RemoteFileStorage.kt` | 26 | Port interface; `uploadFile(bytes, fileName, directory): OnlineDataResult<String>`. Web impl lives in `core/storage/fe` per-platform; JS and Android both implement — but only the Web flow invokes it via `WebAdd*PhotoUseCaseImpl`. |
| `core/storage/fe/src/commonMain/kotlin/.../LocalFileStorage.kt` | (not read; commonMain port) | `saveFile(bytes, fileName, subdirectory): String` (throws on failure — not wrapped in DataResult). NonWeb flow invokes. |

The two DataResult sealed types are the **non-negotiable type-level semantic boundary** the counterfactual must commit against: `OnlineDataResult.Failure.NetworkUnavailable` has no semantic counterpart in `OfflineFirstDataResult` (offline-first save-to-disk cannot fail due to network because there is no network involved), and `OfflineFirstDataResult.ProgrammerError` maps only *approximately* to `OnlineDataResult.Failure.ProgrammerError` (they wrap different classes of exception).

---

## 4. Summary totals

### 4a. Files split by source set (subject of the counterfactual)

- AddFindingPhoto: 12 files across `nonWebMain` + `webMain` source sets (2 interfaces + 2 impls + 2 tests + 2 VMs + 4 DI modules).
- AddProjectPhoto: 12 files across `nonWebMain` + `webMain` source sets (same decomposition).
- **Combined: 24 files, 1715 LOC** (the 1715 total from the `wc -l` preflight — matches this table row-for-row).

### 4b. Shape-level observations

- Both flows duplicate the same 6-row decomposition (interface / impl / test / VM / app-module-DI / driving-module-DI) across the `nonWebMain` ↔ `webMain` split.
- The return-type divergence is the *only* structurally forced split — everything else (VM, DI, tests, sync handlers) follows from the return-type divergence because the callers have to pattern-match on different sealed variant sets.
- No file currently exists in `commonMain` for either flow at the use-case layer. The `AddFindingPhotoRequest` / `AddProjectPhotoRequest` DTOs *are* in `commonMain`, but they carry no return-type information.

### 4c. What the counterfactual changes

Phase 3 (commonize-photo) will:
- Add at minimum 2 new commonMain interface files (`AddFindingPhotoUseCase`, `AddProjectPhotoUseCase` at `feat/*/fe/app/api/src/commonMain/`).
- Add 2 new commonMain impl files (`AddFindingPhotoUseCaseImpl`, `AddProjectPhotoUseCaseImpl` at `feat/*/fe/app/impl/src/commonMain/`).
- Add a new shared `PhotoUploadResult<T>` sealed type in `core/network/fe/src/commonMain/` — canonical DVT-widening artifact.
- Add a `PhotoStoragePort` shape in `feat/*/fe/ports/src/commonMain/` with platform-specific impls under `nonWebMain/` + `webMain/`.
- Delete the 4 interface files + 4 impl files currently at the Web/NonWeb split.
- Rewrite 4 VM files to consume the common use case + match on widened `PhotoUploadResult`.
- Rewrite 4 AppModule DI files + potentially 4 DrivingModule DI files.
- Delete 4 test files (nonWebTest + webTest) and add at least 2 new commonTest files (possibly 4 if impl + port both need testing in common).

The expected delta is the *subject* of Phase 4's `feature_retro.py` measurement on `experiment/photo-progress-commonized` vs `experiment/photo-progress-correct`.
