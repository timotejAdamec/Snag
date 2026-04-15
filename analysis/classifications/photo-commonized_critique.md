<!--
Copyright (c) 2026 Timotej Adamec
SPDX-License-Identifier: MIT

Thesis: "Multiplatform snagging system with code sharing maximisation"
Czech Technical University in Prague — Faculty of Information Technology
-->

# Commonized-branch qualitative critique (Case 1b — experiment/commonize-photo)

**Subject:** `experiment/commonize-photo` (branched off `main` @ `e076e89e5`) — a force-commonized version of the `AddFindingPhoto` and `AddProjectPhoto` flows. This branch is never merged. Before-snapshot: `facts.md`; positive-side critique: `photo-correct_critique.md`.

**Authoring note (per plan §D line 551):** observations were recorded **during** the commonization work, not reverse-rationalized after. The branch compiles; `./gradlew check` (including `archCheck`) passes end-to-end. The archCheck outcome is recorded below as **data**, not as evidence that the commonization is architecturally sound — it is evidence that the current category rules do not detect this specific NS-theorem anomaly shape.

**Honesty claim:** the commonization is a plausible "less-careful-engineer" commonization, not a straw man. Any less-commonized version would leave platform-specific use cases in place and defeat the counterfactual; any more-commonized version (e.g., fusing `LocalFileStorage` and `RemoteFileStorage` into a single port) would have to hide further asymmetries that the current architecture exposes. The chosen shape matches plan §D step 2 literally: `PhotoStoragePort` with `NonWeb*` / `Web*` adapters, common `AddXPhotoUseCase` in commonMain, widened `PhotoUploadResult` sealed type.

---

## SoC (Separation of Concerns) violations

### SoC-1: the widened `when` blocks in both ViewModels now match on variants the platform cannot semantically receive

**Location:**
- `feat/findings/fe/driving/impl/src/nonWebMain/.../NonWebFindingDetailViewModel.kt:62-82` — exhaustive `when (addFindingPhotoUseCase(request))` matches on `PhotoUploadResult.Success`, `ProgrammerError`, `NetworkUnavailable`, `UserMessageError`.
- `feat/findings/fe/driving/impl/src/webMain/.../WebFindingDetailViewModel.kt:72-92` — same four variants.
- `feat/projects/fe/driving/impl/src/nonWebMain/.../NonWebProjectDetailsViewModel.kt:92-112` — same.
- `feat/projects/fe/driving/impl/src/webMain/.../WebProjectDetailsViewModel.kt:104-124` — same.

**Observation:** Two of the four `PhotoUploadResult` variants (`NetworkUnavailable`, `UserMessageError`) are unreachable on the native offline-first path — the native flow never involves a network. The NonWeb VM's `when` block now contains `is PhotoUploadResult.NetworkUnavailable -> { errorEventsChannel.send(UiError.Unknown) }` and `is PhotoUploadResult.UserMessageError -> { errorEventsChannel.send(UiError.Unknown) }` — two branches that cannot fire at runtime on that platform. The source files contain inline comments (`// Unreachable on native offline-first path...`) documenting this — the comments are themselves evidence of the violation: the type system forced them in.

**Theory:** Mannaert et al. §SoC — concerns that the type system previously separated (offline-first failure shape vs online-only failure shape) are structurally fused in the widened sealed interface. The concern of "deciding how to handle each failure variant" now lives in a single place (the common `PhotoUploadResult` sealed type), and every consumer must enumerate every variant even when semantically only a subset can reach them.

**Delta vs correct branch:** On `main`, `NonWebFindingDetailViewModel.onAddPhoto` matches on exactly 2 variants (`OfflineFirstDataResult.Success`, `OfflineFirstDataResult.ProgrammerError`) with zero unreachable cases. Same applies to all four VMs on both flows. The commonized branch adds **2 unreachable cases per VM × 2 VMs (nonWeb findings + nonWeb projects) = 4 unreachable-branch sites**. Web VMs also match on all 4 variants (all reachable on web), but that does not help native.

---

### SoC-2: the common use-case impl contains both Success-after-upload logic AND sealed-variant re-raising — concerns that were implicitly separated before

**Location:** `feat/findings/fe/app/impl/src/commonMain/.../AddFindingPhotoUseCaseImpl.kt:35-84` (nested `when` with inner `when (findingPhotosDb.savePhoto(...))` inside the outer `when (findingPhotoStoragePort.uploadPhoto(...))` — similar at `feat/projects/fe/app/impl/src/commonMain/.../AddProjectPhotoUseCaseImpl.kt:38-89`).

**Observation:** The commonized impl must do three things in a single shared function:
1. Call the platform-specific port through the common interface.
2. Map the port's `PhotoUploadResult` into downstream logic.
3. If `Success`, perform the DB save whose return type is `OfflineFirstDataResult<Unit>` (a different sealed shape); if DB save fails with `OfflineFirstDataResult.ProgrammerError`, convert it to `PhotoUploadResult.ProgrammerError`; pass through all non-Success variants from the port unchanged.

On `main`, steps 1 + 2 + 3 are collapsed: each platform's Impl had only one variant-conversion step because the port's return type matched the use-case's return type directly. Now the common impl has a **nested two-level `when`** with cross-type mapping at each level — exactly the "branching I wouldn't have had to write otherwise" shape.

**Theory:** Mannaert et al. §SoC — the commonized impl mixes the "how to route between offline-first and online-only paths" concern (done by Koin binding the platform-specific `FindingPhotoStoragePort` impl) with the "how to map between sealed-variant vocabularies" concern (done inline in the common impl). Both are platform-aware decisions; the commonization hides one behind Koin DI but forces the other into shared code.

**Delta vs correct branch:** On main, `WebAddFindingPhotoUseCaseImpl.invoke` has 1 `when` with 2 cases (Success, Failure); `NonWebAddFindingPhotoUseCaseImpl.invoke` has 0 `when` on the DB result (uses `.also { ... }.map { ... }` because the return type passes through directly). The commonized common impl has 2 nested `when`s totaling 6 cases to consider (4 outer + 2 inner). More cases = more surface for bugs to hide.

---

## DVT (Data-Version Transparency) violations

### DVT-1: the canonical widened sealed type — `PhotoUploadResult<T>` fuses two previously independent sealed vocabularies

**Location:** `core/network/fe/src/commonMain/.../PhotoUploadResult.kt:21-36` (new file on this branch only).

**Observation:** `PhotoUploadResult` exists **solely** to accommodate both platforms' return shapes:
- `OnlineDataResult<T>` variants mapped in: `Success`, `Failure.NetworkUnavailable`, `Failure.ProgrammerError`, `Failure.UserMessageError` → 4 shapes.
- `OfflineFirstDataResult<T>` variants mapped in: `Success`, `ProgrammerError` → 2 shapes.

The fused `PhotoUploadResult` has 4 variants (Success, ProgrammerError, NetworkUnavailable, UserMessageError). The `ProgrammerError` variant is now overloaded — on web it wraps a network/upload programmer exception; on native it wraps a file-system programmer exception. The type system cannot distinguish them; only a reviewer reading the throwable's stack trace can.

**Theory:** Mannaert et al. §DVT — a data version change (adding/removing a failure case) must be transparent to consumers. The fused `PhotoUploadResult` requires **every consumer on every platform** to recompile and handle all 4 cases even though each platform only produces a 2-case subset. Adding a new failure variant later (e.g., `QuotaExceeded` for server-side storage) forces compile-time updates on every platform's VM `when` block, even platforms whose port can't produce the new variant.

**Delta vs correct branch:** On main, `OnlineDataResult` (77 LOC, 4 Failure sub-cases under one sealed Failure supertype) and `OfflineFirstDataResult` (55 LOC, 2 variants) stayed independent. `NetworkUnavailable` existed only in `OnlineDataResult` — the compiler rejected any attempt to use it in a native context. The commonized branch's `PhotoUploadResult` (37 LOC) is smaller in LOC but unifies both vocabularies, creating a single mutation point that every photo-upload site must recompile against.

---

### DVT-2: `ProgrammerError` conflates "file-system write failed" with "network upload failed"

**Location:** `feat/findings/fe/app/impl/src/nonWebMain/.../NonWebFindingPhotoStoragePort.kt:33-37` (catches file-system exceptions, wraps in `PhotoUploadResult.ProgrammerError`) vs `feat/findings/fe/app/impl/src/webMain/.../WebFindingPhotoStoragePort.kt:38-39` (wraps `OnlineDataResult.Failure.ProgrammerError.throwable` in `PhotoUploadResult.ProgrammerError`).

**Observation:** Both port impls produce `PhotoUploadResult.ProgrammerError(throwable)` — but the semantics of that wrapping are platform-specific. On native, the throwable is a `java.io.IOException` (or similar) from local disk write. On web, it is a network-layer exception from Ktor's HTTP client or a browser `fetch` failure. The consumer — `NonWeb/WebFindingDetailViewModel` — has no way to distinguish them after the fact because the fused `PhotoUploadResult.ProgrammerError` has only one field (`throwable: Throwable`). Error-message rendering, telemetry tagging, retry strategy — all would need to introspect the throwable's class at runtime, undoing the type-safety benefit.

**Theory:** Mannaert et al. §DVT — the merge destroys an entropy distinction: two bits of data (is-online-only vs is-offline-first) collapsed into zero bits at the sealed-variant level. Consumers must recover the distinction downstream.

**Delta vs correct branch:** On main, `OfflineFirstDataResult.ProgrammerError` and `OnlineDataResult.Failure.ProgrammerError` are distinct types. A call site holding a value of either type has platform-level ground truth implicit in the type. On the commonized branch, the distinction is lost at the sealed type boundary.

---

## AVT (Action-Version Transparency) violations

### AVT-1: `PhotoStoragePort` interface compile-forces both platform impls when any parameter changes

**Location:**
- `feat/findings/fe/ports/src/commonMain/.../FindingPhotoStoragePort.kt:18-22` (interface)
- `feat/findings/fe/app/impl/src/nonWebMain/.../NonWebFindingPhotoStoragePort.kt` (impl)
- `feat/findings/fe/app/impl/src/webMain/.../WebFindingPhotoStoragePort.kt` (impl)
- Same 3 files for projects.

**Observation:** Any change to `FindingPhotoStoragePort.uploadPhoto`'s signature (e.g., adding the progress-callback parameter that Phase 4 will add) forces **both** the NonWeb and Web impls to accept the change — even though the progress callback is semantically web-only. The nonWeb impl must accept the parameter and ignore it (trivial pass-through to `LocalFileStorage.saveFile` which has no progress concept). The compiler enforces this symmetry, and a reviewer reading either impl sees a parameter used on one side and ignored on the other.

**Theory:** Mannaert et al. §AVT — an action-version change (progress callback added) propagates across the compile-time interface even when only one platform semantically cares about the change. Phase 4's measurement will quantify this ripple for this specific evolution.

**Delta vs correct branch:** On main, `WebAddFindingPhotoUseCase` and `NonWebAddFindingPhotoUseCase` are independent interfaces. Adding a progress callback to `WebAddFindingPhotoUseCase` compile-forces zero changes in any nonWeb source set — Phase 2's measurement confirms this at 8 web-only files total. On the commonized branch, the AVT cost propagates to at least 6 files before the VMs are even updated (common interface, 2 port impls, 2 VMs, 1 common impl — all forced).

---

### AVT-2: the `enqueueSyncSaveUseCase` / `EnqueueSyncSaveRequest` call now lives in commonMain, which ties its own AVT (if it evolves) to the common `AddXPhotoUseCaseImpl`

**Location:** `AddFindingPhotoUseCaseImpl.kt:61-66` (common impl `enqueueSyncSaveUseCase(EnqueueSyncSaveRequest(entityTypeId = FINDING_PHOTO_SYNC_ENTITY_TYPE, entityId = photoId))`) + same at `AddProjectPhotoUseCaseImpl.kt`.

**Observation:** On main, the sync-enqueue call existed in both platform-specific impls (2 Web + 2 NonWeb = 4 call sites across both flows, each in its platform's source set). On the commonized branch, the call exists in 2 common call sites. Reducing call-site count sounds like a win for sharing, but it has an AVT consequence: any future change to `EnqueueSyncSaveRequest`'s shape (e.g., adding a priority field) recompiles both flows simultaneously because the common impls are the fan-in node. On main, the same change could propagate platform-by-platform or flow-by-flow if needed (more flexibility for staged rollout).

**Theory:** Mannaert et al. §AVT — centralizing the action call at a fan-in point exchanges local flexibility for propagation symmetry. This is a weaker AVT observation than AVT-1; still recorded for completeness.

**Delta vs correct branch:** 4 call sites → 2 call sites (quantitatively). But 2 common call sites × 5 FE target platforms = 10 recompile targets per call-site change, vs 4 platform-specific call sites × 1 target each = 4 recompile targets on main.

---

## ISP (Interface Segregation Principle) violations — visible in the shape even though archCheck allows it

### ISP-1: `FindingPhotoStoragePort.uploadPhoto` will soon accept a progress parameter only web needs

**Location:** `feat/findings/fe/ports/src/commonMain/.../FindingPhotoStoragePort.kt:16-22`.

**Observation:** The current interface has three params (`bytes`, `fileName`, `directory`). Phase 4 adds a fourth (`onProgress`) that only the Web impl uses meaningfully. The nonWeb impl must accept it, ignore it, and lose no information. ISP says an interface should contain only what every implementer needs — here, the progress parameter violates that once added.

**Theory:** Mannaert et al. §ISP (applied to ports in hex architecture). A per-platform port interface (`NonWebFindingPhotoStoragePort` with 3 params, `WebFindingPhotoStoragePort` with 4 params) would keep each impl honest. The commonization rules this out by design.

**Delta vs correct branch:** On main, there is no shared `FindingPhotoStoragePort` interface at all — each platform talks directly to `LocalFileStorage` or `RemoteFileStorage` via its own use-case impl. ISP is satisfied trivially because there is no shared interface for an ISP violation to live in.

---

## Loss of compile-time guarantees

### Compile-1: the platform-level reachability invariant is now a runtime comment, not a type

**Location:** `NonWebFindingDetailViewModel.kt:73-82` (new inline comments: `// Unreachable on native offline-first path (no network involved), but the sealed when must be exhaustive because the common AddFindingPhotoUseCase return type fuses both platforms.`) — comments visible at equivalent lines of all 4 VMs.

**Observation:** On `main`, the invariant "native can't get a `NetworkUnavailable` failure" is expressed **in the type system**: `NonWebAddFindingPhotoUseCase` returns `OfflineFirstDataResult<Uuid>`, and `OfflineFirstDataResult` has no `NetworkUnavailable` variant. On the commonized branch, the same invariant is expressed only in **inline comments and a comment-suppressed exhaustive `when` branch**. The compiler no longer enforces it; a future refactor could silently remove the "unreachable" comment and start treating those branches as meaningful behavior, with no test or compiler error to catch the semantic drift.

**Quote — correct branch signature:**
```kotlin
suspend operator fun invoke(request: AddFindingPhotoRequest): OfflineFirstDataResult<Uuid>
```
**Quote — commonized branch signature:**
```kotlin
suspend operator fun invoke(request: AddFindingPhotoRequest): PhotoUploadResult<Uuid>
```

**Theory:** NS theorems imply that type-level invariants are the cheapest enforcement of semantic boundaries. The commonization trades a type-level invariant for a comment-level one — a strictly weaker enforcement mechanism.

**Delta vs correct branch:** A binary regression. Main = "the compiler forbids it"; commonized = "the developer must remember not to".

---

## Parallel-evolvability loss

### Parallel-1: any cross-cutting change to photo-upload semantics now forces coordinated Web+NonWeb updates

**Location:** anticipated for Phase 4's progress-callback evolution — the same change that stays web-only on `experiment/photo-progress-correct` will touch both web and non-web code paths here.

**Observation:** On `main`, web and native photo-upload use cases can evolve independently. The web team can add a progress callback without touching native code; the native team can change the offline-first queueing strategy without touching web. On the commonized branch, a change to the common `AddFindingPhotoUseCase` signature compile-forces both VMs (web and native) to handle the new shape even if the change is semantically one-sided. This is the canonical parallel-evolvability loss NS theorems predict.

**Theory:** Mannaert et al. §evolvability — the ability to change one subsystem without rippling to others is a load-bearing architectural property. A commonized interface at a layer where the semantics diverge destroys this property at that layer.

**Delta vs correct branch:** Quantified in Phase 4's `feature_retro.py` measurement (see `counterfactual_photo_progress.md` once Phase 5 is authored). Expected ratio `B2/B1 ≥ 3×`.

---

## archCheck outcome

**`./gradlew archCheck` passes on `experiment/commonize-photo` HEAD** (verified via `./gradlew check --continue` — 2m 53s end-to-end after a cached run, BUILD SUCCESSFUL across all 7279 actionable tasks).

**Interpretation:** the archCheck category rules on Snag's build-logic currently enforce concerns like "driven ports live in `ports`", "app-layer use cases don't import infrastructure", etc. They do **not** enforce "don't commonize a platform-divergent semantic into commonMain" — and it would be difficult to write a rule that does, because detecting the divergence at build-rule level requires domain-knowledge of which sealed-variant vocabularies are platform-specific.

A senior Kotlin/KMP reviewer would still flag this commit in code review: the widened sealed type, the unreachable `when` branches with explanatory comments, and the asymmetric port-impls ignoring each other's parameters are all visible code smells that archCheck does not see. Plan §D.4 and §E.5 require recording this explicitly — **archCheck passed; a reviewer would not**.

---

## Loss of tests

**Deleted:**
- `feat/findings/fe/app/impl/src/nonWebTest/.../NonWebAddFindingPhotoUseCaseImplTest.kt` (176 LOC)
- `feat/findings/fe/app/impl/src/webTest/.../WebAddFindingPhotoUseCaseImplTest.kt` (154 LOC)
- `feat/projects/fe/app/impl/src/nonWebTest/.../NonWebAddProjectPhotoUseCaseImplTest.kt` (131 LOC)
- `feat/projects/fe/app/impl/src/webTest/.../WebAddProjectPhotoUseCaseImplTest.kt` (115 LOC)

**Total deleted test LOC: 576 across 4 files.**

**Not added on this branch:** `commonTest` unit tests for the new common `AddXPhotoUseCaseImpl`. A less-careful engineer might reasonably not add them (the existing Koin test infra is Web/NonWeb-specific, bound via `FrontendKoinInitializedTest`; writing a common test that mocks `FindingPhotoStoragePort` directly requires test-infrastructure changes). This absence is **evidence**, not a shortcut: the commonization deliberately matches the "plausible less-careful engineering choice" framing from plan §D line 496 + §E.5.

**Delta vs correct branch:** On `main`, 4 platform-specific test files cover exactly the 2 sealed-variant sets each platform can receive. On commonized, those files are gone; writing common equivalents means introducing a `FakeFindingPhotoStoragePort` and restructuring the Koin test fixture. The counterfactual leaves this cost visible rather than fixing it.

---

## Cross-observation summary

| Obs | NS-theorem | Violation count | Site examples |
|---|---|---|---|
| SoC-1 | SoC | 4 unreachable-branch sites | NonWeb+Web VMs for both flows |
| SoC-2 | SoC | 2 nested-when impl sites | AddFindingPhotoUseCaseImpl, AddProjectPhotoUseCaseImpl |
| DVT-1 | DVT | 1 widened sealed type | PhotoUploadResult.kt |
| DVT-2 | DVT | 2 conflated-semantics impl sites | NonWeb+WebFindingPhotoStoragePort (and project mirrors) |
| AVT-1 | AVT | 2 symmetric-propagation port-interface sites | FindingPhotoStoragePort, ProjectPhotoStoragePort |
| AVT-2 | AVT | 2 fan-in centralization sites | Common enqueueSyncSaveUseCase call in both common impls |
| ISP-1 | ISP | 2 impending-violation port sites | Same ports, once Phase 4's progress callback is added |
| Compile-1 | compile-time guarantees | 4 comment-enforced invariant sites | 4 VMs |
| Parallel-1 | evolvability | quantified in Phase 4 | (forward reference) |

**Observation totals: 9 observations** across 4 NS categories (SoC×2, DVT×2, AVT×2, ISP×1) + compile-time-guarantee loss (×1) + parallel-evolvability loss (×1). Plan §H.9 minimum = 5 → met with overhead.

**archCheck:** passes (recorded, not worked around). A senior reviewer would still flag the commit.

**Test loss:** 576 LOC across 4 deleted test files; no common replacement.

**Honesty check:** the commonization is a minimum-honest shape (plan §D.496: "plausible less-careful engineering choice"). It uses the same sealed-type-widening pattern a real engineer would reach for; it does not invent a cartoonishly bad port. Archcheck passing is the mechanical evidence that the shape is not impossible under the repo's current category rules.
