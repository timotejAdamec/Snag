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

- files:  **local = 89, intrinsic = 1, collateral = 5** (total 95)
- churn:  **local = 4250, intrinsic = 1, collateral = 177** (total 4428 ≡ 4183 insertions + 245 deletions)
- **recurring intrinsic units = 1** — the single `AllTables.kt` registry edit. No drift-fallback underestimation under r3 classification.

**Reclassification note r2 (scope narrowing, 2026-04-24).** The experiment scope was tightened from "modules containing new or directly modified files of commit `b5365d611`" (which implicitly pulled in `:feat:shared:database:*`) to **just `:feat:projects:*`**. Rationale: `:feat:shared:database:*` is a pre-existing, schema-centralizing aggregation module; touches into it must be evaluated under the recurring/intrinsic rubric rather than folded into the local feature footprint (Wear/iOS experiments never touch these modules, so comparability requires consistent treatment). Under r2 the 3 schema files (`ProjectPhotoEntity.kt`, `ProjectPhotosTable.kt`, `ProjectPhotoEntity.sq`) moved `local → intrinsic/recurring/DVT`, `DbConstants.kt` moved `local → collateral`, and `FindingsTable.kt` + `AllTables.kt` kept their prior buckets.

**Reclassification note r3 (strict intrinsic definition, 2026-04-24).** On review, r2 over-attributed to *intrinsic/recurring*. The methodology defines intrinsic as "intervention into a pre-existing aggregation point that receives a **similar edit** at each future addition". `AllTables.kt` passes cleanly — every new entity adds the same one-line `XxxTable,` to the same array. But `ProjectPhotoEntity.kt`, `ProjectPhotosTable.kt`, and `ProjectPhotoEntity.sq` are **new files**. A future entity (say `Report`) will not re-edit `ProjectPhotoEntity.kt` — it will create `ReportEntity.kt` alongside it. The *module* keeps accreting new files (a real SoC gravity observation: schema declarations are centralized rather than owned by their feature), but no individual file is the target of recurring edits. Under the strict per-file definition those 3 files are **local feature content** whose placement is forced by the centralized schema convention, not recurring DVT sites. Bucket change `intrinsic → local` for all 3; only `AllTables.kt` remains as true recurring DVT. Headline shift r2 → r3: `local` gained 81 LOC across 3 files; `intrinsic` lost the same; `collateral` unchanged. The gravity observation is preserved in Obs 1 as a separate, descriptive note rather than as a bucket attribution.

**Source-set annotation.** `commonMain` units are suffixed `[FE]` (frontend-only KMP reach — 5 FE targets) or `[FE+BE]` (full-platform reach, compiles to the backend JVM server too). Other source-set names (`main`, `androidMain`, `iosMain`, `nonWebMain`, `webMain`, etc.) are unambiguous and pass through unannotated. The distinction matters for SoC fan-out reading — a `::commonMain[FE]` ripple never crosses the BE↔FE boundary, whereas a `::commonMain[FE+BE]` one does.

Minimum three observations required. Four are recorded: three named sites plus one positive observation about what the change did *not* touch.

---

## DVT

### Obs 1 — Centralized schema registry (one recurring DVT site) + schema-module gravity (SoC note)

**Recurring DVT site (1 file, 1 LOC):**

1. `feat/shared/database/be/impl/.../AllTables.kt:24` (M, 1 LOC) — single-line addition: `ProjectPhotosTable,`.

`AllTables.kt` declares `val allTables: Array<Table>` — an eager enumeration of every Exposed `Table` that the BE must create at startup in foreign-key-safe order. ProjectPhoto added its table in the block between `ProjectAssignmentsTable` and `StructuresTable` (line 24). Every new persistent entity adds an analogous one-line entry to this same file, so the site satisfies the strict intrinsic definition: *recurring, similar edit at each future addition*.

**NS-theorem property exercised:** **DVT** — classic enumeration anomaly. Data-version transparency is broken because adding a new entity is not absorbed by a default/inheritance mechanism. The site is combinatorial (O(features) edits) and cannot be eliminated by the current Exposed idiom without either a classpath-scan registry or a Gradle code-generation step. Single recurring unit, `recurring_intrinsic_units = 1`.

**Schema-module gravity (SoC note, 3 local files in shared modules):**

- `feat/shared/database/be/impl/.../ProjectPhotoEntity.kt` (A, 30 LOC) — new Exposed DAO.
- `feat/shared/database/be/impl/.../ProjectPhotosTable.kt` (A, 23 LOC) — new Exposed `Table` with FKs to projects + uploader.
- `feat/shared/database/fe/api/.../ProjectPhotoEntity.sq` (A, 28 LOC) — new SQLDelight mirror schema on the FE side.

These three files are **new**, per-entity, feature-specific content. A future feature (e.g. `Report`) will not re-edit them — it will create its own `ReportEntity.kt` / `ReportsTable.kt` / `ReportEntity.sq` alongside. The methodological test "receives a similar edit at each future addition" therefore fails at the **file** level, and under r3 these three are classified as **local** (see the r3 note above). The real observation here is about the *module*, not the file: Snag centralizes schema declarations (Exposed BE, SQLDelight FE) into two dedicated modules rather than letting each feature own its tables, so each new entity's schema content lands in `:feat:shared:database:{be:impl, fe:api}` by architectural convention. This is a **SoC gravity** descriptive finding — the centralized-schema module organization pulls feature content out of `:feat:projects:*` — not a recurring DVT aggregation, and not a combinatorial anomaly in the NS sense (the *module* accretes files O(features × columns), but no single artifact inside absorbs O(features) edits).

Note on the `:root::non-module` unit fallback: all four files collapse to `:root::non-module` in the `unit` field because of a drift artifact — `feat/shared` was moved to `:featuresShared` in commit `dc0d9b8a2` (post-`b5365d611`), so the dependency-closure snapshot no longer resolves the old path. Under r3 this is harmless: only `AllTables.kt` is intrinsic, the other three are local and don't need distinct-unit counting. `recurring_intrinsic_units = 1` is the accurate count, not a drift undercount.

---

## General / AVT

### Obs 2 — In-flight constant extraction forced cross-feature edits

**Sites:**
- `feat/shared/database/be/impl/src/main/kotlin/.../DbConstants.kt` (new file, 15 LOC including copyright header — 1 line of substance: `internal const val URL_MAX_LENGTH = 2048`)
- `feat/shared/database/be/impl/src/main/kotlin/.../FindingsTable.kt:44` (deletion of `private const val URL_MAX_LENGTH = 2048`)

ProjectPhoto's new `ProjectPhotosTable` needed the same 2048-character URL limit that `FindingPhotosTable` already enforced. Rather than duplicating the constant, the review process extracted it into a new shared `DbConstants.kt` file and removed it from `FindingsTable.kt`. The PR description (commit message point 4) lists this explicitly: *"Unify URL_MAX_LENGTH into shared DbConstants"*.

**NS-theorem property exercised:** **General / AVT (action version transparency, weakly)**. The original `private const val` in `FindingsTable.kt` could not be reused across files; making it reusable required a code motion that edited the existing file. AVT is about *versioning* a shared action — here the "action" is the URL-length validation, and promoting the constant is an AVT-adjacent refactor done in-flight. Of interest descriptively: the refactor was noticed and performed during PR review, not pre-planned. The ripple classifier records both entries as **collateral** (one-time cleanup incidental to the feature, not a recurring aggregation site); a stricter discipline might have split the URL-constant extraction into its own commit, but the practice in this repo is to bundle them. Distinction from Obs 1: sites there are recurring (every new entity hits them) — `DbConstants.kt` is a one-shot extraction that no future feature is expected to touch for the same reason.

---

## SoC / SoS

### Obs 3 — Cross-feature delegation: `findings` now depends on a `projects`-exported use case

**Site:** `feat/findings/fe/app/impl/src/webMain/kotlin/.../CanModifyFindingPhotosUseCaseImpl.kt:20–24` (constructor parameter changed from `ConnectionStatusProvider + CanEditProjectEntitiesUseCase` to `CanModifyProjectFilesUseCase`; body collapsed from a 6-line `combine(...).distinctUntilChanged()` to a single-line pass-through).

ProjectPhoto introduced `CanModifyProjectFilesUseCase` in the `projects` feature's `fe:app:api` module as a shared policy for *any* file-modification gate inside a project (applies to both project photos and finding photos). The findings feature's pre-existing `CanModifyFindingPhotosUseCase` implementation was refactored to delegate to it, eliminating the local `combine(isConnected, canEdit) { a && b }` logic. The deleted test file `CanModifyFindingPhotosUseCaseImplTest.kt` (-142 LOC) is the collateral consequence — once the implementation becomes pure delegation, it has no independent logic to test.

**NS-theorem property exercised:** **SoC (cross-feature module coupling) and SoS (system-of-systems, weakly)**. The findings feature now has a compile-time and runtime dependency on the projects feature's `app:api` module. The boundary crossing itself is clean — it uses the API layer, not an internal — but it does mean the two features no longer ship independently: removing `projects` from the build would break `findings`. The repo's current coupling pattern tolerates this (see `core/business/rules/` for an earlier shared-policy site), but the critique notes it because §4.3 prose should acknowledge that feature-level SoC in Snag is not hermetic.

---

## General (positive observation)

### Obs 4 — What ProjectPhoto did *not* touch

Worth recording in a descriptive illustration because the absence is where the encapsulation work is visible. Of the 95 files the commit touched, **86 are local to `feat/projects/**`** plus **3 local schema files pulled into `:feat:shared:database:*`** by the centralized-schema convention (see Obs 1 gravity note) — 89 local total. The remaining 6 split as follows: **1 intrinsic/recurring DVT** (`AllTables.kt` registry — Obs 1), **2 collateral AVT** (`DbConstants.kt` extraction + `FindingsTable.kt` constant removal — Obs 2), **2 collateral SoC** (findings-delegation refactor + its now-redundant test file — Obs 3), and **1 collateral `build.gradle.kts`** edit in `feat/projects/fe/app/impl` (flagged by the `build_gradle_collateral` rule — a dependency/plugin line avoidable under stricter convention-plugin autowiring, but common in the repo). Notably, **zero** touches in each of:

- `settings.gradle.kts` — ProjectPhoto added no new Gradle submodules. New code went into existing feat/projects modules that were already registered.
- `koinModulesAggregate/fe/**` and `koinModulesAggregate/be/**` — ProjectPhoto's new Koin modules are auto-wired by the repo's convention-plugin aggregation (see `build-logic/` and `docs/gradle_plugins.md`); each feature's DI module is discovered reflectively, so adding a feature does not require editing a central list.
- Any central `SyncEntityType` enum or `SyncHandlerRegistry` file — ProjectPhoto's `ProjectPhotoSyncHandler` is registered via Koin and participates in sync through the handler-discovery mechanism rather than a central enum. The ripple classifier's `sync_handler_registry` and `sync_entity_type_enum` rules did not fire because no central registry was touched.

**NS-theorem property exercised:** **SoC (positive)**. Three of the four classical combinatorial-anomaly sites that Snag could plausibly have (settings, Koin aggregation, sync enum) are absorbed by the repo's convention-plugin + Koin-auto-discovery tooling. This is the observable evidence for the §4.2 claim that Snag's build-time tooling eliminates several NS combinatorial sites that a naïve Gradle/DI setup would re-introduce. The DVT site that *does* remain (Obs 1 above) is the uneliminated residue — the BE schema registry `AllTables.kt`, a single O(features) enumeration. The separately-noted schema-module gravity (3 files pulled into the centralized schema modules) is a SoC organizational cost, not a recurring DVT site; under strict classification (r3) those files are local, and only the registry remains intrinsic. §4.7 prose can frame it as "one combinatorial anomaly (central schema registry) out of four possible, with an additional SoC gravity cost of per-entity schema content landing in centralized modules" — useful context for the counterfactual comparison in Case 1b.

---

## Cross-observation summary

| Obs | NS theorem | Bucket | Recurring | Site |
|---|---|---|---|---|
| 1a | DVT | intrinsic | yes | `AllTables.kt:24` (+1 LOC registry entry) |
| 1b | SoC (gravity, descriptive) | local | no | 3 schema files in `:feat:shared:database:{be:impl, fe:api}` (per-entity content placed in centralized modules) |
| 2 | AVT / general | collateral | no | `DbConstants.kt` (new) + `FindingsTable.kt:44` (removal) |
| 3 | SoC / SoS | collateral | no | `CanModifyFindingPhotosUseCaseImpl.kt:20–24` (+ deleted test) |
| 4 | SoC (positive) | — (absence) | — | zero touches: settings.gradle, koinModulesAggregate, sync registry |

**Descriptive reading for §4.3:** A 95-file feature addition in Snag, under the r3 classification (scope = `:feat:projects:*`; per-entity new files are local content irrespective of module, recurring intrinsic = "similar edit at each future addition"), breaks down as 89 local / 1 intrinsic / 5 collateral files and 4250 / 1 / 177 LOC of churn. Feature-local churn dominates strongly (≈ 94 % of files, ≈ 96 % of LOC); the single recurring intrinsic site is `AllTables.kt` (1 LOC, DVT enumeration). The centralized schema architecture still shows up, but as a **SoC gravity** observation (Obs 1b — 3 local files that happen to live in `:feat:shared:database:*` rather than `:feat:projects:*`), not as a DVT recurring aggregation. Three absorbed combinatorial sites (settings, Koin, sync) demonstrate that the repo's build/DI tooling contains what would otherwise be recurring-intrinsic pressure. Five collateral entries reflect evolutionary cleanup done in-flight. This shape is *compatible with* well-factored architecture but does not *prove* it — see Case 1b for the counterfactual comparison.
