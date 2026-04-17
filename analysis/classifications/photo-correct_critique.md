<!--
Copyright (c) 2026 Timotej Adamec
SPDX-License-Identifier: MIT

Thesis: "Multiplatform snagging system with code sharing maximisation"
Czech Technical University in Prague — Faculty of Information Technology
-->

# Correct-branch qualitative critique (Case 1b — main's `AddFindingPhoto` + `AddProjectPhoto`)

**Subject:** `main` @ `e076e89e5` — the `NonWeb*` / `Web*` photo-upload flows as they exist today, *before* any counterfactual commonization. The corresponding before-snapshot (paths + LOC) is in `facts.md` alongside this file.

**Authoring note (per plan §D line 549):** this critique is written **before** `experiment/commonize-photo` is branched so the observations are not reverse-rationalized against the counterfactual's costs. It is a positive critique: it names what the correctly-scoped structure *buys* — what the type system prevents, what cognitive load it avoids, what parallel-evolvability it enables. Each observation cites file:line; theory-category labels reference the NS-theorem site the correct structure protects (same taxonomy §4.6 uses for the commonized branch's violations, but from the opposite sign).

**Observation budget:** ≥3 per flow (plan §H.10). Authored: 7 (3 per flow + 1 cross-flow).

---

## AddFindingPhoto — 3 observations

### Obs 1 (findings) — the return type carries the platform semantic at the type level

**Location:** `feat/findings/fe/app/api/src/nonWebMain/kotlin/.../NonWebAddFindingPhotoUseCase.kt:19` vs `feat/findings/fe/app/api/src/webMain/kotlin/.../WebAddFindingPhotoUseCase.kt:19`.

**Observation:** The only syntactic difference between the two interface files is one token in the return type (`OfflineFirstDataResult<Uuid>` vs `OnlineDataResult<Uuid>`). That single token is the projection of a non-negotiable semantic difference: `OnlineDataResult.Failure.NetworkUnavailable` (at `OnlineDataResult.kt:23`) has no semantic counterpart in `OfflineFirstDataResult` because offline-first save-to-disk cannot fail for network reasons — the network is not involved. A caller that receives `OfflineFirstDataResult` cannot pattern-match on `NetworkUnavailable` because it is not a variant of that type; the compiler rejects the dead case at the `when` exhaustiveness check.

**NS-theorem site protected:** **DVT (data-version transparency)**. The caller's pattern-match is exhaustive over exactly the variant set it can semantically receive — no "variant my platform cannot reach at runtime" pollution. The return type is the smallest honest interface: it encodes no cases that are semantic noise for the platform.

**Parallel-evolvability proof (forward reference):** this critique's paired-branch evolution adds an upload-progress callback only to the web flow. Because the nonWeb interface is a physically different file in a different source set, adding a parameter to `WebAddFindingPhotoUseCase.kt` cannot compile-force a change on `NonWebAddFindingPhotoUseCase.kt`. In a commonized version of this interface, the progress parameter would have to be added to the common signature, and the nonWeb path would have to accept it and ignore it (ISP violation — see `photo-commonized_critique.md` Obs equivalent).

---

### Obs 2 (findings) — the VM `when` block is exhaustive on exactly the reachable variants

**Location:**
- `feat/findings/fe/driving/impl/src/nonWebMain/kotlin/.../NonWebFindingDetailViewModel.kt:60-68` (when-match on `OfflineFirstDataResult` — 2 variants: `ProgrammerError` + `Success`).
- `feat/findings/fe/driving/impl/src/webMain/kotlin/.../WebFindingDetailViewModel.kt:72-80` (when-match on `OnlineDataResult` — 2 variants: `Success` + `Failure`, where `Failure` is a sealed supertype flattening 3 sub-cases).

**Observation:** Neither VM's `when` contains a case that is structurally unreachable. `NonWebFindingDetailViewModel` has no `NetworkUnavailable` branch because it cannot semantically receive one. `WebFindingDetailViewModel` has no `Success` variant from `OfflineFirstDataResult` because it returns a different sealed type. If a reader opens either VM and wants to know "what can go wrong here?", the answer is the literal set of branches in the `when`. There is no implicit "this branch exists because the shared return type requires it but it cannot actually fire" footnote.

**NS-theorem site protected:** **SoC (separation of concerns)**. The connectivity concern (offline-first vs online-only) is separated from the error-handling concern at the VM layer by the type system, not by runtime branching or by documentation. A reviewer reading the non-web VM does not have to build a mental model of the web path to understand the error handling.

---

### Obs 3 (findings) — extra dependencies appear only where they are needed

**Location:** `WebFindingDetailViewModel.kt:40` (ctor param `canModifyFindingPhotosUseCase: CanModifyFindingPhotosUseCase`) + `WebFindingDetailViewModel.kt:50` (`collectCanModifyPhotos()` job). Absent from `NonWebFindingDetailViewModel` (compare the ctor in `NonWebFindingDetailViewModel.kt:29-37`).

**Observation:** The web VM subscribes to `CanModifyFindingPhotosUseCase` to gate photo-add buttons on connectivity. The nonWeb VM does not, because offline-first save-to-disk is always permitted regardless of connectivity — you can always queue a local write. Because the two VMs are separate classes in separate source sets, the web VM's extra dependency does not inflate the nonWeb VM's constructor. The Koin factory bindings (`FindingsDrivingModule.web.kt:40` vs `FindingsDrivingModule.nonWeb.kt:40`) reflect the asymmetry one-for-one: the web binding lists one extra `get()` call.

**NS-theorem site protected:** **ISP (interface segregation) — constructor narrowing.** The nonWeb VM's constructor is exactly the set of dependencies its methods use; the web VM's constructor is exactly the set its methods use. No dependency is injected to be ignored. A commonized VM would have to accept the union, exposing each platform to dependencies it does not need at construction time (Koin's singleton lifecycle also means the unused dep is still instantiated in memory).

---

## AddProjectPhoto — 3 observations

### Obs 4 (projects) — the return-type split mirrors findings exactly

**Location:** `feat/projects/fe/app/api/src/nonWebMain/kotlin/.../NonWebAddProjectPhotoUseCase.kt:19` vs `feat/projects/fe/app/api/src/webMain/kotlin/.../WebAddProjectPhotoUseCase.kt:19`.

**Observation:** The same one-token divergence as findings: `OfflineFirstDataResult<Uuid>` vs `OnlineDataResult<Uuid>`. Two independent features (`findings` and `projects`) arrived at the same platform-split shape because the underlying semantic — offline-first vs online-only upload — is a property of the *storage strategy*, not the *entity being stored*. The architecture surfaces this: neither feature invented a feature-specific abstraction over the semantics; both used the shared `core/network/fe` DataResult types.

**NS-theorem site protected:** **SoC (cross-feature replication without cross-feature coupling).** Each feature independently applied the platform-split pattern at the same layer (app/api) because the pattern is a property of the hex layer, not of the feature. Centralizing the pattern would require a shared `PhotoUploadUseCase<T>` abstraction — which would then have to be parametrized by the feature-specific request/response types, and the type parameters would leak the asymmetry back out anyway.

**Replication evidence:** two cases are a pattern, not a coincidence (plan §E point 4). The counterfactual commonization below commonizes both flows so the numeric delta is the sum across two realistic feature instances, not a single demonstration.

---

### Obs 5 (projects) — the commonMain `ProjectDetailsViewModel` base class absorbs ~90% of the behavior; only the platform-asymmetric part stays per-platform

**Location:**
- `feat/projects/fe/driving/impl/src/commonMain/kotlin/.../ProjectDetailsViewModel.kt` (abstract base class; not read in preflight but implied by the constructor-inheritance pattern in `WebProjectDetailsViewModel.kt:68-89` and `NonWebProjectDetailsViewModel.kt:65-86`).
- `WebProjectDetailsViewModel.kt:99-124` (override of `onAddPhoto`).
- `NonWebProjectDetailsViewModel.kt:87-112` (override of `onAddPhoto`).

**Observation:** The bulk of the project-details logic (~20 use cases: `getProjectUseCase`, `deleteProjectUseCase`, `getStructuresUseCase`, `getInspectionsUseCase`, …, `updateProjectPhotoDescriptionUseCase`) lives in a shared abstract base class in `commonMain`. Only the `onAddPhoto` method override and its supporting connectivity-gate subscription differ per platform. The current architecture does not over-split: shared behavior stays shared, divergent behavior lives at the divergence point. The Web/NonWeb VM files are *small* (~113–125 LOC each) because they contain only the divergent part.

**NS-theorem site protected:** **SoC + DVT combined.** Shared state and shared transitions stay in commonMain (the base class); platform-divergent transitions (photo add) stay per-platform. The boundary is exactly the semantic divergence, not a conservative "everything related to photos is per-platform" over-scoping.

**Contrast with the counterfactual:** a commonized `AddProjectPhotoUseCase` would either force the base VM to handle a widened `PhotoUploadResult` (pulling the divergence *inward*, into the shared base class) or force both VMs to still override `onAddPhoto` but now pattern-match on more variants (adding noise without removing the per-platform file). Either way, the existing split is Pareto-optimal for the current semantic.

---

### Obs 6 (projects) — parallel-evolvability: the forward evolution step is surgical

**Location:** anticipated changes in `experiment/photo-progress-correct` (to be authored in Phase 2) — restricted to:
- `WebAddProjectPhotoUseCase.kt` (signature widening) + `WebAddProjectPhotoUseCaseImpl.kt` (threading the callback into `remoteFileStorage.uploadFile`).
- `WebProjectDetailsViewModel.kt` (consume + expose `uploadProgressFlow`).
- Symmetric 3-file set for findings.
- Plus zero changes in any `nonWebMain/` source set.
- Plus possibly 1–2 changes in `core/storage/fe` if `RemoteFileStorage.uploadFile` needs a new parameter (counted as the signature-widening cost of the core port, *not* a cost of the correct vs commonized comparison — it appears on both branches).

**Observation (forward-looking, to be confirmed by Phase 2 numbers):** the web-only evolution should touch 3–6 Kotlin files per flow, all in `webMain` source sets plus optionally the `commonMain` port. It should **not** touch `commonMain` use case signatures, `nonWebMain` use case impls, `nonWebMain` VMs, or `commonTest` test sets — because none of those care about upload progress. This prediction is what the counterfactual will contradict: on the commonized branch, the same evolution will ripple into commonMain (widening the common use case signature) and therefore into every downstream FE target's recompile surface.

**NS-theorem site protected:** **AVT (action-version transparency).** The progress-callback is a new action-version; it propagates only along the path where the action is relevant. On main, that path is web-only. On a commonized structure, the common interface becomes a fan-in node and the action-version propagates across the fan-in.

---

## Cross-flow observation

### Obs 7 (cross-flow) — the split is not an isolated quirk of `addPhoto`; it reflects the broader offline-first ↔ online-only axis in Snag's FE architecture

**Location:** Recurring pattern also visible at:
- `feat/projects/fe/app/impl/src/{nonWebMain,webMain}/.../CanModifyProjectFilesUseCaseImpl.kt` (connectivity-gated edit permission, split per platform).
- Sync handler split: `{NonWeb,Web}FindingPhotoSyncHandler.kt`, `{NonWeb,Web}ProjectPhotoSyncHandler.kt`.
- The `nonWebMain` / `webMain` source sets themselves (part of the KMP hierarchy, not specific to photo flows).

**Observation:** The `NonWeb*` / `Web*` pattern is present in at least 6 independent sites in the codebase (2 photo add flows × findings/projects + 2 sync handlers + 2 can-modify checks). A single-site commonization counterfactual would not be representative. Replication across 2 photo-add flows captures a pattern instance; the other 4 instances are out of scope for Case 1b but confirm the pattern is load-bearing across the codebase.

**NS-theorem site protected:** **SoC at the architectural-pattern level.** The split is not ad-hoc per feature; it is a consistent boundary that appears wherever the offline-first vs online-only semantic reaches the application layer. A commonization would not just affect one pair of files; it would either set a precedent inconsistent with the rest of the codebase (local erosion) or propagate back out to those other sites as a consistency pressure (global cost).

---

## Expected counterfactual cost (hypothesis, to be verified)

Prediction for `experiment/commonize-photo` (authored before Phase 3 starts):

- A new sealed `PhotoUploadResult<T>` type in `core/network/fe` unifying `OnlineDataResult` + `OfflineFirstDataResult` variants — canonical DVT-violation artifact.
- A `PhotoStoragePort` with `nonWebMain` + `webMain` impls, whose purpose is to route the platform-conditional storage call from a shared commonMain use-case impl — SoC violation because the routing logic now lives in shared code.
- VM `when` blocks must now match on `PhotoUploadResult` variants including at least one their platform cannot reach at runtime.
- The progress-callback evolution ripples into commonMain and into both platforms' ports.

Expected numeric signature per plan §H.6-H.7:
- `files_commonized / files_correct ≥ 2×` (hypothesis; plan's lower bound is 2×, straw-man threshold is 20×).
- `B2/B1 ≥ 3×` — commonMain interface changes have fan-out to every FE target.

If the measured numbers fall below `2×` or above `20×`, the counterfactual's honesty must be reviewed (too tame or straw-manned respectively). These predictions are recorded here, *before* the measurement, so the analysis is pre-registered.
