<!--
Copyright (c) 2026 Timotej Adamec
SPDX-License-Identifier: MIT

Thesis: "Multiplatform snagging system with code sharing maximisation"
Czech Technical University in Prague — Faculty of Information Technology
-->

# `feat/inspections` reverse removal — NS-theorem critique (Case 1)

**Change under review:** experimental branch `experiment/remove-inspections` (HEAD `dbb86f525`) vs base `main` (`e076e89e5`). Entire `feat/inspections/**` subtree deleted (17 Gradle submodules, ~6000 LOC), plus every cross-feature/infra site that referenced it repaired honestly until `./gradlew check` (including `archCheck`) passes end-to-end. `--continue` surfaced all failures before each repair pass per the "fix all before rerun" discipline. Repair cascade documented on the experiment branch (commit `dbb86f525`); experiment branch never merged, retained for reproducibility per `analysis/phase-2-plan.md` §Case 1.

**Method note.** This critique **is** cited as §4.6 correctness evidence — distinct from Case 2's descriptive/non-probative framing. Ripple decomposition measured on the reverse-removal experiment supports the headline claim that feature addition/removal in Snag is not combinatorial — the recurring-intrinsic count is the operationalization of that claim. Per the Part A methodological framing in `phase-2-plan.md`, the claim is *"combinatorial touches are bounded by the enumerated NS-theorem anomaly sites"*, not *"the architecture is optimal"*. The anomaly sites are named and the theorem each exercises is tagged; the critique records what *is*, not what *ought to be*. A ripple site the theory does not predict as recurring is recorded as `collateral` in the yaml even if eliminating it would improve the architecture — no shoehorning to inflate the recurring-intrinsic count.

**Headline numbers** (`python analysis/feature_retro.py --change inspections-reverse-removal --finalize`, recorded for §4.6 and §4.7 prose):

- files:  **local = 89, intrinsic = 7, collateral = 43** (total 139)
- churn:  **local = 5281, intrinsic = 99, collateral = 691** (total 6071 ≡ LOC additions + removals captured via `git diff --numstat -M`)
- **recurring intrinsic units = 6** — the §4.6 headline and the §4.7 cross-case roll-up input for Case 1.

**Source-set annotation.** `commonMain` units are suffixed `[FE]` (frontend-only KMP reach — 5 FE targets) or `[FE+BE]` (full-platform reach, compiles to the backend JVM server too). Other source-set names (`main`, `androidMain`, `iosMain`, `nonWebMain`, `webMain`, etc.) are unambiguous and pass through unannotated. The distinction matters for SoC fan-out reading — a `::commonMain[FE]` ripple never crosses the BE↔FE boundary, whereas a `::commonMain[FE+BE]` one does.

**What "recurring intrinsic units = 6" counts.** The `feature_retro.py` finalize script counts any `(module, source_set)` unit containing ≥1 entry classified as `intrinsic` AND with `recurring = True` at the unit level. Aggregated CSV (`ripple_inspections-reverse-removal_units.csv`): the 6 units are

| Unit | ns_theorem | Site |
|---|---|---|
| `:koinModulesAggregate:be::main` | SoC | `BackendModulesAggregate.kt` imports + `includes` list |
| `:koinModulesAggregate:fe::commonMain[FE]` | SoC | `FrontendModulesAggregate.kt` imports + `includes` list |
| `:root::non-module` | SoC | `koinModulesAggregate/{be,fe}/build.gradle.kts` — same anomaly as the two rows above, counted separately because `build.gradle.kts` files fall outside any `source_set_dir_rel` prefix and resolve to the literal `:root::non-module` fallback. This is a *path→unit* artifact of the longest-prefix resolver, not a distinct anomaly site; §4.6 prose should treat the two Koin-aggregate `.kt` + their `build.gradle.kts` pairs as one SoC site per layer (BE/FE). |
| `:root::settings` | SoC | `settings.gradle.kts:165–181` — 17 `include(":feat:inspections:*")` lines |
| `:server::main` | DVT | `DevDataSeederConfiguration.kt` — constructor param + `seedInspections()` method |
| `:testInfra:fe::commonMain[FE]` | ISP | `FrontendKoinInitializedTest.kt` — `koinModules()` enumeration |

So six unit rows = four distinct NS anomaly sites after collapsing the Koin-aggregate path→unit artifact. §4.6 prose cites **5** observations below (one per NS site, plus a positive/contrast observation on where the ripple did *not* combinatorially propagate), which is above the `phase-2-plan.md:174` minimum of 5.

---

## SoC

### Obs 1 — Backend Koin aggregation registry

**Site:** `koinModulesAggregate/be/src/main/kotlin/cz/adamec/timotej/snag/di/aggregate/be/BackendModulesAggregate.kt:22–24,72–74` (pre-deletion).

`BackendModulesAggregate.kt` declares `val backendModulesAggregate = module { includes(...) }` — an eager enumeration of every BE Koin module the application loads at startup. Each feature registers three modules there (driving + driven + app) through three `import` lines and three `includes(...)` entries. Removing `feat/inspections` required deleting exactly six lines (three imports + three `includes` entries) in this one file.

**NS-theorem property exercised:** **SoC (Separation of Concerns) via central registration aggregation.** The site is combinatorial in the number of features: O(features) edits over the project's lifetime, one feature-sized block per addition/removal. The ripple classifier auto-matches this file via the `aggregation_koin_be` rule and tags it `intrinsic recurring: true`. Eliminating this site would require a classpath-scan DI setup or a Gradle-generated aggregator, neither of which Snag currently uses — Snag tolerates this anomaly by choice. This is the canonical §4.6 example of a recurring intrinsic site in a hexagonal-multiplatform codebase with static DI wiring.

### Obs 2 — Frontend Koin aggregation registry

**Site:** `koinModulesAggregate/fe/src/commonMain/kotlin/cz/adamec/timotej/snag/di/aggregate/fe/FrontendModulesAggregate.kt:25–28,87–90` (pre-deletion).

Symmetric to Obs 1 but for the frontend side. `frontendModulesAggregate` enumerates every FE Koin module. ProjectPhoto contributed *four* modules per feature (driving API + driving impl + driven + app), so removing `feat/inspections` forced eight line deletions (four imports + four `includes` entries). Also removed: the `koinModulesAggregate/{be,fe}/build.gradle.kts` `projects.feat.inspections.*` dependency declarations — these `build.gradle.kts` edits resolve to the `:root::non-module` fallback unit in the classifier output but are the same SoC anomaly as the `.kt` edits above (they're the physical manifestation of feature enumeration at the Gradle-module level, not a separate site).

**NS-theorem property exercised:** **SoC**. Same rationale as Obs 1. The FE aggregate has four-per-feature fan-in instead of three, so its churn sensitivity to feature count is slightly higher than the BE aggregate; §4.6 prose can note this asymmetry if the word count permits.

### Obs 3 — `settings.gradle.kts` module registration

**Site:** `settings.gradle.kts:165–181` (pre-deletion) — 17 contiguous `include(":feat:inspections:*")` lines.

Every Gradle submodule in the project must be listed in `settings.gradle.kts` by Gradle's inclusion rules — there is no classpath-scan alternative at the Gradle level. `feat/inspections` shipped 17 submodules (one per `feat/inspections/<layer>/<impl-or-api>/...`), so reverse removal deleted exactly 17 lines here. This is the highest raw LOC recurring-intrinsic delta in Case 1 (17 lines), dwarfing the 6–8-line Koin aggregate edits.

**NS-theorem property exercised:** **SoC — module-registration aggregation at the build-tool layer.** Rule: `settings_gradle`, auto-matched, intrinsic recurring: true. The count scales linearly with the number of Gradle submodules per feature, not features themselves — so a feature that adopts the repo's modular-by-layer convention pays a higher `settings.gradle.kts` cost than a feature shipped as a single module. Case 2's ProjectPhoto comparison shows 0 touches here because ProjectPhoto extended existing modules rather than creating new ones; Case 1 shows the opposite extreme for a feature of comparable size that does ship its own module tree.

---

## DVT

### Obs 4 — Dev-data seeder enumeration

**Site:** `server/src/main/kotlin/cz/adamec/timotej/snag/impl/internal/DevDataSeederConfiguration.kt:26–27,50,63,352–396,428–431` (pre-deletion).

`DevDataSeederConfiguration` is a server-only class that seeds a fresh dev database with example entities so a just-started local backend is immediately usable. Every feature that defines a persistent entity registers a constructor-injected `*Db` port and adds a `seed<Entity>()` call to the `Application.setup()` block inside `runBlocking`. `feat/inspections` contributed: one import (`BackendInspectionData`), one import (`InspectionsDb`), one constructor parameter, one `seedInspections()` function invocation inside `setup()`, the entire `seedInspections()` function body (44 LOC), and four private companion-object UUID constants. Total churn: 55 lines across one file.

**NS-theorem property exercised:** **DVT (Data-Version Transparency), recurring.** Every new persistent entity type forces a fan-in at the seeder: one `*Db` dependency, one `seed*()` method, one call site in `setup()`. This is not the same anomaly as the BE schema registry `AllTables.kt` discussed in Case 2 — there, Exposed requires an explicit table list for foreign-key-safe creation order at startup; here, the seeder is a dev-tool convenience, and its sensitivity to entity count comes from a *different* DVT axis (the seed data's shape must version with the entity's fields, so a DVT defect in the model propagates to seeder churn).

**Note on rule mismatch.** The ripple classifier's `dev_data_seeder` rule pre-populates `bucket: intrinsic, recurring: false` — because the rule author, when writing `ripple_rules.yaml` at tooling time, treated the seeder as non-recurring (on the theory that not every feature must be seeded). Case 1 reverse-removal evidence contradicts that judgement for the inspections case: the entity *was* seeded, the seeder *was* touched, and every feature currently in the seeder adheres to the same pattern. The yaml for Case 1 explicitly overrides this entry to `recurring: true, ns_theorem: DVT, source: hand` with the rationale recorded in the `reason` field. Follow-up: a separate PR should tighten `ripple_rules.yaml` to match `recurring: true` by default (tracked as out-of-scope in this case per `phase-2-plan.md` §Out-of-scope for Case 1).

---

## ISP

### Obs 5 — Test-infrastructure Koin enumeration

**Site:** `testInfra/fe/src/commonMain/kotlin/cz/adamec/timotej/snag/testinfra/fe/FrontendKoinInitializedTest.kt:18,39` (pre-deletion) — one import, one `koinModules()` list entry.

`FrontendKoinInitializedTest` is an abstract test base class that every feature-level FE test inherits from. Its `koinModules()` override enumerates every feature's `fe:driven:test` module so the Koin graph is fully satisfiable during tests. `feat/inspections` added itself as one import + one list entry. Removing the feature required deleting exactly those two lines. The touch is small (2 LOC) but it is a *hard* recurring site: a new feature whose FE tests need a driven-test module cannot compile without editing this file.

**NS-theorem property exercised:** **ISP (Interface Segregation Principle), recurring — applied to test infrastructure fan-in.** The base class acts as a single point of test-side dependency injection for the entire FE test suite. Every new feature's tests implicitly depend on this base, and every feature adds exactly one entry. ISP would call for per-feature test bases that each include only that feature's driven-test module plus the shared infra — Snag does not adopt that pattern, so this site remains combinatorial. The classifier has no rule targeting this file (test-infra aggregation is not a rule category in `ripple_rules.yaml` at this time); the classification is hand-applied with `source: hand, ns_theorem: ISP` in the yaml, with rationale recorded. The unit `:testInfra:fe::commonMain[FE]` also has the largest downstream blast radius among the Case 1 recurring-intrinsic sites: `blast_radius_module = 74`, `blast_radius_unit = 160` — editing this file triggers recompilation of every feature's FE test set.

---

## General (contrast observation)

### Obs 6 — Where the ripple did *not* combinatorially propagate: the 89 feature-local files

Worth recording in a correctness-evidence critique because the absence is what the headline claim rests on. Of the 139 files the reverse-removal touched, **89 are inside `feat/inspections/**` itself** — the 17 submodules of the feature being deleted, 64% of the file count, and 87% (5281 / 6071) of the raw churn. These are classified `local` via the `--local-module-globs ':feat:inspections:*'` flag — pure feature-internal files that would vanish along with the feature in any honest accounting. The remaining 50 files split into 7 intrinsic (6 recurring, 1 non-recurring) and 43 collateral.

**NS-theorem property exercised:** **SoC (positive).** The measured ratio — 89 local : 7 intrinsic : 43 collateral, and within that 6 recurring-intrinsic units mapped to 4 distinct anomaly sites — is what §4.6 operationalizes as "feature removal is not combinatorial". Collateral 43 is non-trivial and is *not* absorbed by the architecture, but it is concentrated in two places: `feat/projects/fe/driving/impl` (16 files) and `feat/reports/be/*` (3 files). Each is a direct cross-feature use case import or UI embed (InspectionCard on the projects details screen, inspections section in the PDF report). This is an observable SoC weakness in the codebase — a proper cross-feature port at the app/api layer would have kept these files out of the collateral bucket — but it is *not* a recurring anomaly: every new feature does not force an edit to `feat/projects` or `feat/reports`. The collateral bucket is bounded by the concrete cross-feature dependencies the inspections feature accumulated, not by any structural combinatorial fan-out. §4.6 prose should state this contrast explicitly to avoid over-claiming: Snag's architecture makes cross-feature coupling *possible* (collateral) but not *mandatory* (intrinsic recurring).

---

## Cross-observation summary

| Obs | NS theorem | Unit(s) | Site | Churn | Recurring |
|---|---|---|---|---|---|
| 1 | SoC | `:koinModulesAggregate:be::main` | `BackendModulesAggregate.kt:22–24,72–74` | 6 | yes |
| 2 | SoC | `:koinModulesAggregate:fe::commonMain[FE]` + `:root::non-module` (build.gradle.kts artifact) | `FrontendModulesAggregate.kt:25–28,87–90` | 8 | yes |
| 3 | SoC | `:root::settings` | `settings.gradle.kts:165–181` | 17 | yes |
| 4 | DVT | `:server::main` | `DevDataSeederConfiguration.kt:26–27,50,63,352–396,428–431` (hand-overridden to recurring:true) | 55 | yes |
| 5 | ISP | `:testInfra:fe::commonMain[FE]` | `FrontendKoinInitializedTest.kt:18,39` (hand-classified, no matching rule) | 2 | yes |
| 6 | SoC (contrast) | 89 local files | `feat/inspections/**` — 87% of total churn is absorbed by the feature's own module tree | 5281 | — |

**Correctness reading for §4.6:** Reverse-removing a 17-module ~6000-LOC feature from Snag touches 139 files, of which 89 (64%) are local to the deleted feature itself, 43 (31%) are collateral cross-feature coupling concentrated in two feature consumers, and 7 (5%) are intrinsic — aggregating to 6 unit-level recurring-intrinsic sites, which after collapsing one path→unit artifact corresponds to **4 distinct NS anomaly sites** (2× SoC Koin aggregation, 1× SoC settings.gradle.kts, 1× DVT dev-data seeder, 1× ISP test infra). All four sites are well-known structural aggregation points that NS theory predicts *a priori*; none of them is a surprise site specific to the inspections feature. The 43 collateral files, while non-trivial, are bounded by the specific cross-feature responsibilities inspections accumulated (cascade deletion policy, PDF report section, project details UI embed) — they do not grow combinatorially with feature count. Taken together, this measured shape is consistent with "feature addition/removal is not combinatorial": the combinatorial sites exist, they are enumerable, and they are what theory predicts. This does not prove the architecture is optimal — a stricter cross-feature port convention would empty the collateral bucket — but it does operationalize the non-combinatorial claim the thesis §4.6 makes.

**Follow-up items for `ripple_rules.yaml`** (not executed in Case 1 per §Out-of-scope; noted for a separate PR):
1. Upgrade `dev_data_seeder` rule to `recurring: true` by default.
2. Tighten `sync_handler_registry` rule so it matches only the central sync registry/aggregation sites, not feature-owned sync handlers that happen to have cross-feature cascade calls (the Case 1 stub auto-matched `ProjectSyncHandler.kt` and `ProjectPullSyncHandler.kt` as intrinsic-recurring, which required hand-override to collateral — see yaml rationale).
3. Consider adding a `test_infra_aggregation` rule targeting `testInfra/fe/**/FrontendKoinInitializedTest*.kt` and `testInfra/be/**/BackendKoinInitializedTest*.kt` with `ns_theorem: ISP, recurring: true`, so Case 1's Obs 5 would be rule-matched rather than hand-classified.
