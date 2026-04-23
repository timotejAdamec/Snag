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

- files:  **local = 86, intrinsic = 4, collateral = 5** (total 95)
- churn:  **local = 4169, intrinsic = 82, collateral = 177** (total 4428 ≡ 4183 insertions + 245 deletions)
- **recurring intrinsic units = 1** — counted by unit, collapsed by the `:root::non-module` drift fallback (see *Reclassification note r2*). Conceptually the four intrinsic files populate three distinct DVT aggregation modules: the BE schema registry (`AllTables.kt`), the BE schema module (`:feat:shared:database:be:impl` — Exposed DAO + Table), and the FE schema module (`:feat:shared:database:fe:api` — SQLDelight mirror).

**Reclassification note r2 (scope narrowing, 2026-04-24).** The experiment scope for Case 2 was tightened from "modules containing new or directly modified files of commit `b5365d611`" (which implicitly pulled in `:feat:shared:database:*`) to **just `:feat:projects:*`**. Rationale: `:feat:shared:database:*` is a pre-existing, schema-centralizing aggregation module — every new persistent entity in the project must add files there by architectural necessity, so its touches are *intrinsic / recurring DVT*, not *local*. Folding them into the local bucket smeared a recurring combinatorial cost into a one-shot feature-local number and broke cross-experiment comparability (Wear/iOS experiments never touch this module). Under the narrowed scope, 3 schema files (`ProjectPhotoEntity.kt`, `ProjectPhotosTable.kt`, `ProjectPhotoEntity.sq`) moved `local → intrinsic/recurring/DVT`, and `DbConstants.kt` moved `local → collateral` (a one-time AVT cleanup extraction, not a recurring aggregation). `FindingsTable.kt` stayed collateral. `AllTables.kt` stayed intrinsic. Headline shift: `local` lost 96 LOC across 4 files; `intrinsic` gained 81 LOC across 3 files; `collateral` gained 15 LOC across 1 file.

**Source-set annotation.** `commonMain` units are suffixed `[FE]` (frontend-only KMP reach — 5 FE targets) or `[FE+BE]` (full-platform reach, compiles to the backend JVM server too). Other source-set names (`main`, `androidMain`, `iosMain`, `nonWebMain`, `webMain`, etc.) are unambiguous and pass through unannotated. The distinction matters for SoC fan-out reading — a `::commonMain[FE]` ripple never crosses the BE↔FE boundary, whereas a `::commonMain[FE+BE]` one does.

Minimum three observations required. Four are recorded: three named sites plus one positive observation about what the change did *not* touch.

---

## DVT

### Obs 1 — Centralized schema aggregation (registry + schema modules)

**Sites (four files across three DVT aggregation modules):**

1. `feat/shared/database/be/impl/.../AllTables.kt:24` (M, 1 LOC) — single-line addition: `ProjectPhotosTable,`.
2. `feat/shared/database/be/impl/.../ProjectPhotoEntity.kt` (A, 30 LOC) — new Exposed DAO entity.
3. `feat/shared/database/be/impl/.../ProjectPhotosTable.kt` (A, 23 LOC) — new Exposed `Table` with FKs to projects + uploader.
4. `feat/shared/database/fe/api/.../ProjectPhotoEntity.sq` (A, 28 LOC) — new SQLDelight mirror schema on the FE side.

The registry file (site 1) declares `val allTables: Array<Table>` — an eager enumeration of every Exposed `Table` that the BE must create at startup in foreign-key-safe order; ProjectPhoto added its table in the block between `ProjectAssignmentsTable` and `StructuresTable` (line 24). Sites 2–3 are the BE schema counterpart: every new persistent entity in Snag must contribute an `XxxEntity.kt` + `XxxTable.kt` pair into the shared `:feat:shared:database:be:impl` module (not into its home feature). Site 4 is the FE mirror: every persistent entity also contributes a `.sq` schema into the shared `:feat:shared:database:fe:api` module so the FE SQLDelight generator has the matching columns.

**NS-theorem property exercised:** **DVT** — three distinct aggregation points, all recurring.

- *Registry site* (AllTables.kt) — the classic enumeration anomaly; data-version transparency is broken because adding a new entity is not absorbed by a default/inheritance mechanism. The site is combinatorial (O(features) edits) and cannot be eliminated by the current Exposed idiom without either a classpath-scan registry or a Gradle code-generation step.
- *BE and FE schema modules* — Snag centralizes schema declarations (both Exposed and SQLDelight) into two dedicated modules rather than letting each feature own its own tables. Every new persistent entity must land there by architectural necessity — this is a DVT cost paid per-entity, not per-feature-logic, and the ripple classifier correctly attributes it to the *intrinsic/recurring* bucket rather than to the feature's local footprint. It is distinct from a pure registry edit (which is O(features) LOC) because the schema modules absorb O(features × columns) of new code, not a single line each.

Note on the `:root::non-module` unit fallback: all four files collapse to `:root::non-module` in the `unit` field because of a drift artifact — `feat/shared` was moved to `:featuresShared` in commit `dc0d9b8a2` (post-`b5365d611`), so the dependency-closure snapshot no longer resolves the old path. This undercounts the distinct-unit metric (`recurring intrinsic units = 1`) which conceptually should be `3`. The file-level bucket attribution is still correct; only the unit-count aggregate is affected. For §4.7 prose, cite the file- and churn-level numbers (4 intrinsic files, 82 LOC) rather than the unit count.

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

Worth recording in a descriptive illustration because the absence is where the encapsulation work is visible. Of the 95 files the commit touched, **86 are local to `feat/projects/**`** and the remaining 9 split as follows: **4 intrinsic/recurring DVT** (AllTables registry + 3 schema files in the centralized BE/FE schema modules — see Obs 1), **2 collateral AVT** (DbConstants extraction + FindingsTable constant removal — Obs 2), **2 collateral SoC** (findings-delegation refactor + its now-redundant test file — Obs 3), and **1 collateral `build.gradle.kts`** edit in `feat/projects/fe/app/impl` (flagged by the `build_gradle_collateral` rule — a dependency/plugin line that would be avoidable under stricter convention-plugin autowiring, but is common in the repo). Notably, **zero** touches in each of:

- `settings.gradle.kts` — ProjectPhoto added no new Gradle submodules. New code went into existing feat/projects modules that were already registered.
- `koinModulesAggregate/fe/**` and `koinModulesAggregate/be/**` — ProjectPhoto's new Koin modules are auto-wired by the repo's convention-plugin aggregation (see `build-logic/` and `docs/gradle_plugins.md`); each feature's DI module is discovered reflectively, so adding a feature does not require editing a central list.
- Any central `SyncEntityType` enum or `SyncHandlerRegistry` file — ProjectPhoto's `ProjectPhotoSyncHandler` is registered via Koin and participates in sync through the handler-discovery mechanism rather than a central enum. The ripple classifier's `sync_handler_registry` and `sync_entity_type_enum` rules did not fire because no central registry was touched.

**NS-theorem property exercised:** **SoC (positive)**. Three of the four classical combinatorial-anomaly sites that Snag could plausibly have (settings, Koin aggregation, sync enum) are absorbed by the repo's convention-plugin + Koin-auto-discovery tooling. This is the observable evidence for the §4.2 claim that Snag's build-time tooling eliminates several NS combinatorial sites that a naïve Gradle/DI setup would re-introduce. The DVT sites that *do* remain (Obs 1 above) are the uneliminated residue — the BE schema registry plus the two centralized schema modules (BE Exposed + FE SQLDelight) that absorb per-entity schema code. §4.7 prose can frame it as "one combinatorial anomaly (central schema) out of four possible, affecting three distinct aggregation modules", useful context for the counterfactual comparison in Case 1b.

---

## Cross-observation summary

| Obs | NS theorem | Bucket | Recurring | Site |
|---|---|---|---|---|
| 1 | DVT | intrinsic | yes | `AllTables.kt:24` + 3 schema files in `:feat:shared:database:{be:impl, fe:api}` |
| 2 | AVT / general | collateral | no | `DbConstants.kt` (new) + `FindingsTable.kt:44` (removal) |
| 3 | SoC / SoS | collateral | no | `CanModifyFindingPhotosUseCaseImpl.kt:20–24` (+ deleted test) |
| 4 | SoC (positive) | — (absence) | — | zero touches: settings.gradle, koinModulesAggregate, sync registry |

**Descriptive reading for §4.3:** A 95-file feature addition in Snag, when classified by ripple bucket under the *scoped* definition (scope = `:feat:projects:*` only), breaks down as 86 local / 4 intrinsic / 5 collateral files and 4169 / 82 / 177 LOC of churn. Feature-local churn still dominates (≈ 91 % of files, ≈ 94 % of LOC), but the recurring intrinsic cost of Snag's centralized schema architecture is now visible as a 4-file / 82-LOC block rather than hidden in the local bucket — the correct reading, since every future persistent entity will pay the same per-entity cost at the same three aggregation modules. Three absorbed combinatorial sites (settings, Koin, sync) demonstrate that the repo's build/DI tooling contains what would otherwise be recurring-intrinsic pressure. Five collateral entries (cross-feature delegation refactor, shared-constant extraction, dependency declaration) reflect evolutionary cleanup done in-flight and are not predicted by any general rule. This shape is *compatible with* well-factored architecture but does not *prove* it — see Case 1b for the counterfactual comparison.
