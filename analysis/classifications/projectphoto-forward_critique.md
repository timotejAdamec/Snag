<!--
Copyright (c) 2026 Timotej Adamec
SPDX-License-Identifier: MIT

Thesis: "Multiplatform snagging system with code sharing maximisation"
Czech Technical University in Prague — Faculty of Information Technology
-->

# ProjectPhoto retrospective — descriptive critique (Case 2)

**Change under review:** commit `b5365d611` — *Implement project photodocumentation (US18) (#215)*. 95 files changed, 4183 insertions, 245 deletions. Full-feature addition spanning business/app/backend hexagonal layers, sync handlers, DB schemas, contract DTOs, DI wiring, cascade deletion, and a platform-specific ViewModel split (`NonWeb*` / `Web*` for the photo-add action).

**Method note.** This critique is the §4.3 descriptive illustration source for Case 2 and is **not** cited as correctness evidence. Per the Part A methodological framing in `analysis/phase-2-plan.md`, ripple decomposition is compatible with multiple architectural realities (correct, overgeneralized, fragmented). Case 2 shows what *did* happen on commit `b5365d611`; Case 1b is where correctness is argued via counterfactual. Observations below describe named sites and name which NS-theorem property each site exercises — they do not grade the site.

**Headline numbers** (from `python analysis/feature_retro.py --change projectphoto-forward --finalize`, recorded for §4.7 prose):

- files:  **local = 90, intrinsic = 1, collateral = 4** (total 95)
- churn:  **local = 4265, intrinsic = 1, collateral = 162** (total 4428 ≡ 4183 insertions + 245 deletions)
- **recurring intrinsic units = 1** — the BE schema registry (AllTables.kt)

Minimum three observations required. Four are recorded: three named sites plus one positive observation about what the change did *not* touch.

---

## DVT

### Obs 1 — BE schema registry aggregation

**Site:** `feat/shared/database/be/impl/src/main/kotlin/cz/adamec/timotej/snag/feat/shared/database/be/AllTables.kt:24` (single-line addition: `ProjectPhotosTable,`).

The file declares `val allTables: Array<Table>` — an eager enumeration of every Exposed `Table` that the BE must create at startup in foreign-key-safe order. Each new persistent entity forces exactly one line here. ProjectPhoto added its table in the block between `ProjectAssignmentsTable` and `StructuresTable` (line 24).

**NS-theorem property exercised:** **DVT**. Data-version transparency is broken at the aggregation point: the set of tables is centralized, so adding a new entity is not absorbed by a default or an inheritance mechanism — it requires an explicit line edit. The site is combinatorial (O(features) edits over the life of the project) and cannot be eliminated by the current Exposed idiom without either a classpath-scan registry or a Gradle code-generation step. The ripple decomposition catches this site even when the change is otherwise encapsulated, which makes it the canonical §4.7 example of a recurring intrinsic site Snag tolerates by choice.

---

## General / AVT

### Obs 2 — In-flight constant extraction forced cross-feature edits

**Sites:**
- `feat/shared/database/be/impl/src/main/kotlin/.../DbConstants.kt` (new file, 15 LOC including copyright header — 1 line of substance: `internal const val URL_MAX_LENGTH = 2048`)
- `feat/shared/database/be/impl/src/main/kotlin/.../FindingsTable.kt:44` (deletion of `private const val URL_MAX_LENGTH = 2048`)

ProjectPhoto's new `ProjectPhotosTable` needed the same 2048-character URL limit that `FindingPhotosTable` already enforced. Rather than duplicating the constant, the review process extracted it into a new shared `DbConstants.kt` file and removed it from `FindingsTable.kt`. The PR description (commit message point 4) lists this explicitly: *"Unify URL_MAX_LENGTH into shared DbConstants"*.

**NS-theorem property exercised:** **General / AVT (action version transparency, weakly)**. The original `private const val` in `FindingsTable.kt` could not be reused across files; making it reusable required a code motion that edited the existing file. AVT is about *versioning* a shared action — here the "action" is the URL-length validation, and promoting the constant is an AVT-adjacent refactor done in-flight. Of interest descriptively: the refactor was noticed and performed during PR review, not pre-planned. The ripple classifier records two entries (new file + removal site) both as churn incidental to the feature; a stricter discipline might have split the URL-constant extraction into its own commit, but the practice in this repo is to bundle them.

---

## SoC / SoS

### Obs 3 — Cross-feature delegation: `findings` now depends on a `projects`-exported use case

**Site:** `feat/findings/fe/app/impl/src/webMain/kotlin/.../CanModifyFindingPhotosUseCaseImpl.kt:20–24` (constructor parameter changed from `ConnectionStatusProvider + CanEditProjectEntitiesUseCase` to `CanModifyProjectFilesUseCase`; body collapsed from a 6-line `combine(...).distinctUntilChanged()` to a single-line pass-through).

ProjectPhoto introduced `CanModifyProjectFilesUseCase` in the `projects` feature's `fe:app:api` module as a shared policy for *any* file-modification gate inside a project (applies to both project photos and finding photos). The findings feature's pre-existing `CanModifyFindingPhotosUseCase` implementation was refactored to delegate to it, eliminating the local `combine(isConnected, canEdit) { a && b }` logic. The deleted test file `CanModifyFindingPhotosUseCaseImplTest.kt` (-142 LOC) is the collateral consequence — once the implementation becomes pure delegation, it has no independent logic to test.

**NS-theorem property exercised:** **SoC (cross-feature module coupling) and SoS (system-of-systems, weakly)**. The findings feature now has a compile-time and runtime dependency on the projects feature's `app:api` module. The boundary crossing itself is clean — it uses the API layer, not an internal — but it does mean the two features no longer ship independently: removing `projects` from the build would break `findings`. The repo's current coupling pattern tolerates this (see `core/business/rules/` for an earlier shared-policy site), but the critique notes it because §4.3 prose should acknowledge that feature-level SoC in Snag is not hermetic.

---

## General (positive observation)

### Obs 4 — What ProjectPhoto did *not* touch

Worth recording in a descriptive illustration because the absence is where the encapsulation work is visible. Of the 95 files the commit touched, **90 are local to `feat/projects/**`** and the remaining 5 are the three named sites above (1 DVT registry, 2 AVT-refactor sites, 2 SoC delegation sites — one site was a deletion-only file so a total of 4 collateral entries + 1 intrinsic entry). Notably, **zero** touches in each of:

- `settings.gradle.kts` — ProjectPhoto added no new Gradle submodules. New code went into existing feat/projects modules that were already registered.
- `koinModulesAggregate/fe/**` and `koinModulesAggregate/be/**` — ProjectPhoto's new Koin modules are auto-wired by the repo's convention-plugin aggregation (see `build-logic/` and `docs/gradle_plugins.md`); each feature's DI module is discovered reflectively, so adding a feature does not require editing a central list.
- Any central `SyncEntityType` enum or `SyncHandlerRegistry` file — ProjectPhoto's `ProjectPhotoSyncHandler` is registered via Koin and participates in sync through the handler-discovery mechanism rather than a central enum. The ripple classifier's `sync_handler_registry` and `sync_entity_type_enum` rules did not fire because no central registry was touched.

**NS-theorem property exercised:** **SoC (positive)**. Three of the four classical combinatorial-anomaly sites that Snag could plausibly have (settings, Koin aggregation, sync enum) are absorbed by the repo's convention-plugin + Koin-auto-discovery tooling. This is the observable evidence for the §4.2 claim that Snag's build-time tooling eliminates several NS combinatorial sites that a naïve Gradle/DI setup would re-introduce. The one DVT site that *does* remain (Obs 1 above) is the uneliminated residue — §4.7 prose can frame it as "one recurring intrinsic site out of four possible", which is useful context for the counterfactual comparison in Case 1b.

---

## Cross-observation summary

| Obs | NS theorem | Bucket | Recurring | Site |
|---|---|---|---|---|
| 1 | DVT | intrinsic | yes | `AllTables.kt:24` |
| 2 | AVT / general | collateral + local | no | `FindingsTable.kt:44` + `DbConstants.kt` (new) |
| 3 | SoC / SoS | collateral | no | `CanModifyFindingPhotosUseCaseImpl.kt:20–24` (+ deleted test) |
| 4 | SoC (positive) | — (absence) | — | zero touches: settings.gradle, koinModulesAggregate, sync registry |

**Descriptive reading for §4.3:** A 95-file feature addition in Snag, when classified by ripple bucket, is dominated by feature-local churn (90 files, 96% of touched files, 4265/4428 = 96% of churn). The single recurring intrinsic anomaly is the BE schema registry (1 LOC, well-known DVT site). Three absorbed combinatorial sites (settings, Koin, sync) demonstrate that the repo's build/DI tooling contains what would otherwise be recurring-intrinsic pressure. Two collateral sites (cross-feature delegation refactor, shared-constant extraction) reflect evolutionary cleanup done in-flight and are not predicted by any general rule. This shape is *compatible with* well-factored architecture but does not *prove* it — see Case 1b for the counterfactual comparison.
