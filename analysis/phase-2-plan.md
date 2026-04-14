# Phase 2 — Feature-level evolvability case studies

## Context

Thesis evaluation (`analysis/thesis-evaluation-plan.md`) needs Phase 2: ripple analysis on feature-addition axis. Phases 0–1 complete: feasibility spike done, §4.2 sharing quantification tooling shipped in PR #223 (SharingReportTask + loc_report.sh + figures.py heatmap). Phase 2 builds the ripple classifier + dependency closure + case-study data used by §4.3 of Kapitola 4: Vyhodnocení.

The outcome is **measured evidence that adding / extending features is not combinatorial**: for each studied change, partition touched `(module × source set)` units into local / intrinsic / collateral buckets (Normalized Systems ripple definition), annotate each with blast radius, and classify intrinsic-bucket files as recurring vs fixed. The headline finding is *"how many files in the intrinsic bucket recur per feature"* — those are the anomaly sites NS theory predicts.

Phase 2 delivers:
1. `analysis/dependency_closure.py` + a small Gradle task to dump the project dependency graph.
2. `analysis/feature_retro.py` — ripple classifier driven by `ripple_rules.yaml` + `classifications/*.yaml`.
3. `analysis/ripple_rules.yaml` — committed rule set.
4. `analysis/classifications/` — per-change judgment files (inspections reverse, ProjectPhoto forward, DVT synthetic, optional iOS-only).
5. Three executed case studies writing repair logs into `analysis/data/ripple_*.csv`.
6. Figure 4.3 stubs populated in `figures.py` (`figure_ripple_buckets` + source-set heatmap per change).

No thesis prose in Phase 2; §4.3 Czech prose is deferred to Phase 5 per the plan's sequencing principle.

---

## Tooling design

### A. Gradle dependency graph dump + `source_set_dir_rel` column

**New file:** `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/analysis/DependencyGraphTask.kt`

- Root-applied task `dependencyGraphReport` analogous to `SharingReportTask`.
- For every subproject, walk every configuration whose name ends in `Implementation`, `Api`, or `RuntimeOnly` (covers `commonMainImplementation`, `androidMainImplementation`, `mobileMainImplementation`, plain JVM `implementation`, etc.), extract `ProjectDependency` entries, emit one CSV row per edge: `source_module, source_configuration, target_module, scope`.
- Register via a new `SharingReportSetup` sibling `DependencyGraphSetup.kt`; wired into the existing `snagSharingReport` convention plugin so Phase 1 pipeline and Phase 2 pipeline are one invocation.
- Output: `build/reports/dependency_graph/dependency_graph.csv`.
- Reuses `ArchitectureCheckSetup.collectProjectDependencies()` as a reference pattern (broader config sweep, not limited to `commonMain*`).
- Unit tests in `build-logic/src/test/.../DependencyGraphTaskTest.kt` using the same `ProjectBuilder` pattern as `SharingReportTaskTest` (read `ArchitectureRulesTest.kt` for the existing test idiom).

**Edit to existing `SharingReportRowBuilder.kt` + `SharingReportTask.kt`:** add a `source_set_dir_rel` column alongside the existing `source_set_dir` (absolute). Computed as `sourceSetDir.absolutePath.relativeTo(rootDir).invariantSeparatorsPath`. This becomes the canonical repo-relative prefix used by `feature_retro.py` for the path→unit mapping, removing the need to reimplement `ModulePathParser.kt` in Python. A git diff path is mapped to a unit by longest-prefix match against all `source_set_dir_rel` rows in the sharing report.

### B. `analysis/dependency_closure.py`

- Reads `build/reports/dependency_graph/dependency_graph.csv` + `analysis/data/sharing_report_with_loc.csv`.
- For each `(module, source_set)` unit present in the sharing report, computes the transitive set of downstream units that depend on it. Configuration-name → source-set mapping (`commonMainImplementation` → `commonMain`, `androidMainImplementation` → `androidMain`, plain `implementation` → `main`, etc.). Expands the source-set hierarchy documented in `MultiplatformModuleSetup.kt`: a dependency from `nonWebMain` of module X resolves to every platform-specific source set derived from `nonWebMain` (androidMain, iosMain, jvmMain), not `nonWebMain` literally.
- Emits `analysis/data/dependency_closure.json` keyed by `f"{module}::{source_set}"` → `{blast_radius_module: int, blast_radius_unit: int, downstream_sample: [top 20 units]}`. **Two blast-radius numbers:**
  - `blast_radius_module` — downstream *module* count. Exact, no KMP approximation, defensible under attack.
  - `blast_radius_unit` — downstream `(module, source_set)` count. Upper-bounded when KMP intermediate source sets don't expose distinct Gradle configurations; the approximation over-estimates (counts `commonMain` as reaching every downstream source set of every downstream module), which is conservative for the "risky touch" discussion.
  §4.3 headline uses `blast_radius_module`; the source-set-axis discussion cites `blast_radius_unit` with its caveat.
- Deterministic, idempotent. CLI: `python analysis/dependency_closure.py`.

### C. `analysis/ripple_rules.yaml`

Committed rule set for bucket classification. Schema:

```yaml
version: 1
local_rules:
  # A touched unit is `local` iff it's inside a module whose path matches one of these
  # globs AND that module was first created in the change under study.
  - created_in_change: true  # defined per-change in classifications/<change>.yaml
intrinsic_globs:
  # (module_path, source_set) glob pairs that are known structural aggregation points.
  # Every touch of these is `intrinsic` unless overridden in the per-change classification.
  - module_glob: ":koinModulesAggregate:**"
    reason: "DI aggregation point — any feature add/remove edits this module"
    recurring: true
  - module_glob: ":feat:*:fe:app:impl"
    source_set: commonMain
    path_glob: "**/sync/**SyncHandler.kt"
    reason: "Sync handler registry — each feature registers itself"
    recurring: true
  - module_glob: ":server"
    path_glob: "**/DevDataSeederConfiguration.kt"
    reason: "Dev data seeder — enumerates feature entities"
    recurring: false
  # ...etc
collateral_default: true  # anything not matched is collateral by default
```

- Rules are globs, but the source of truth for any individual unit is its entry in the per-change `classifications/<change>.yaml` file. The YAML lets a second reader reproduce the partition, challenge a rule, or override it per change.
- Plan file starts with an empty-ish scaffold; rules are added iteratively as each case study surfaces new aggregation points, and each addition is documented in the yaml's commit history.

### D. `analysis/classifications/<change>.yaml`

One file per studied change. Schema:

```yaml
change_id: inspections-reverse-removal
ref: experiment/remove-inspections
base_ref: main
base_sharing_snapshot: analysis/data/sharing_report_with_loc_base_main_<sha>.csv
repair_log:
  - file: "koinModulesAggregate/fe/src/commonMain/.../FrontendModulesAggregate.kt"
    unit: ":koinModulesAggregate:fe::commonMain"
    status: M
    loc_churn: 12      # added + removed from git diff --numstat
    bucket: intrinsic
    recurring: true
    source: "rule:aggregation_koin"
    reason: "Feature DI registration; every new feature adds a line here"
  - file: "feat/projects/fe/driving/impl/.../ProjectDetailsViewModel.kt"
    unit: ":feat:projects:fe:driving:impl::commonMain"
    status: M
    loc_churn: 8
    bucket: collateral
    recurring: false
    source: "hand"
    reason: "Direct import of inspection use cases instead of a cross-feature port — avoidable"
  # ...
```

- **Classification default is rule-driven, unmatched is unclassified.** `feature_retro.py --stub` runs every changed file through `ripple_rules.yaml`; rule-matched entries get `bucket: <rule-result>`, `source: rule:<rule_id>`, `reason: <rule.reason>` auto-populated. Unmatched entries get `bucket: unclassified` and require a hand-written `reason` + explicit `source: hand`. `--finalize` rejects any entry still `unclassified`.
- Every classification has either a `rule:<id>` trail or a hand-written justification, so git blame on the yaml bisects rule vs judgment for any row.
- Fields present per entry: `file, unit, status (A/M/D/R), loc_churn, bucket, recurring, source, reason`. Renames collapse to a single entry with `file: "old → new"` and inherit the new location's rule match; `file_count: 95` matches `git show --stat` *after* renames are collapsed — verification check 2 accounts for this.

### E. `analysis/feature_retro.py`

**CLI:**
```
python analysis/feature_retro.py --change <change_id> --ref <ref> [--base-ref main] [--base-snapshot <csv>] [--stub | --finalize]
```

**Pipeline:**
1. `git diff-tree --no-commit-id --name-status -r -M <base>..<ref>` → list of changed files with A/M/D/R status; `-M` enables rename detection.
2. For each file, derive its `(module, source_set)` unit by **longest-prefix match against `source_set_dir_rel`** in the base-ref sharing snapshot (`--base-snapshot`, default `analysis/data/sharing_report_with_loc.csv`). No Python grammar reimplementation. Deleted files are resolved against the base snapshot, not the current working tree — so Case 1 (inspections reverse) correctly assigns deleted `feat/inspections/**` files to their original units. Files outside any source-set prefix (`settings.gradle.kts`, `docs/`, `.github/`) fall through a tiny literal fallback table to `:root::non-module` or `:root::settings` etc.
3. For each unit, look up `blast_radius_module` and `blast_radius_unit` from `dependency_closure.json`, and `loc_churn = added + removed` via `git diff --numstat -M`. `loc_churn` (churn) is the reported LOC metric — not net, because reverse-removal would yield huge negative nets and break stacked bars. Documented in the yaml schema docstring.
4. In `--stub` mode: apply `ripple_rules.yaml` auto-classification to every unit; rule-matched entries get their bucket/reason/source populated; unmatched stay `unclassified`. Emit `analysis/classifications/<change_id>.yaml`, ordered by file path.
5. In `--finalize` mode: read the (hand-edited) yaml, reject any `unclassified` entry, emit **two** CSVs:
   - `analysis/data/ripple_<change_id>_files.csv` — one row per file (Příloha A raw log).
   - `analysis/data/ripple_<change_id>_units.csv` — aggregated per `(module, source_set)` with `file_count`, `loc_churn_sum`, and the dominant bucket (hand-resolved if a unit has mixed buckets across files). This is what `figure_ripple_buckets` reads.
6. Summary printed to stdout: bucket counts, churn totals, and explicitly **"N recurring intrinsic units"** — the headline combinatorial-effect number that §4.7 cites.
7. `archCheck` discipline (Case 1 only): `./gradlew archCheck` must pass on the repair. If the repair forces a category-rule violation to stay compiling, the violation itself is a collateral-ripple finding and is classified as such, not worked around.
8. Idempotent; reruns produce byte-identical output.

**Python module layout:**
- `analysis/feature_retro.py` — CLI.
- `analysis/dependency_closure.py` — CLI.
- `analysis/tests/` — pytest directory with `__init__.py` and a `conftest.py` so pytest discovers it. Unit tests cover: longest-prefix unit mapping (fixed `(path, expected_unit)` fixtures), rename collapsing, deleted-file resolution against a base snapshot, rule-driven auto-classification, and the `--finalize` rejection of unclassified entries. Tests run via `python -m pytest analysis/tests/` — author-side only, not wired into `./gradlew check`.
- No `module_path_parser.py`. The grammar lives in Kotlin (`ModulePathParser.kt`) and is reached through the sharing report CSV.

### F. `figures.py` Phase 2 additions

Implement the stub `figure_ripple_buckets()` to consume `analysis/data/ripple_*.csv`:
- Stacked horizontal bar per change — segments = local / intrinsic / collateral, file count (raw) + LOC (companion).
- Companion source-set heatmap per change: rows = buckets, columns = platform_set categories (reusing the Phase 1 ordering), cells = unit count.
- Produces `analysis/figures/fig_4_3_ripple_buckets.pdf` + per-change source-set heatmaps.

---

## Case study execution

**Explicit Phase 2 day budget** (~7 working days). Case 4 is gated — execute only if Cases 1–3 are done and classified by end of day 7.

| Day | Work |
|---|---|
| 1–2 | Tooling: `DependencyGraphTask`, `SharingReportRowBuilder` rel-path edit, `dependency_closure.py`, `feature_retro.py`, `ripple_rules.yaml` scaffold, `figures.py` Phase 2 additions, pytest suite. |
| 3 | **Case 2 — ProjectPhoto retrospective.** Dry run for the tooling — no repair work, pure classification. **Gate:** `feature_retro.py` collapsed-rename file count must equal `git show --stat b5365d611` (95 files, or 95 minus detected renames). Mismatch = tooling bug; fix before Case 1. |
| 4–6 | **Case 1 — inspections reverse removal.** Worktree, deletion, repair, classification. |
| 7 | **Case 3 — DVT synthetic test on Client.** Mechanical: half-day implementation, half-day classification + round-trip test. |
| (gate) | **Case 4 — iOS-only Project extension.** Only if Cases 1–3 are complete by end of day 7. Skip without guilt. |

### Case 1 — Primary: `feat/inspections` reverse removal

**Scope from exploration:** 17 modules under `feat/inspections/*`, ~6000 LOC, ~25–50 cross-feature touchpoints. Known touchpoints from scouting: `koinModulesAggregate/{fe,be}`, `feat/projects/{ProjectDetailsViewModel, ProjectSyncHandler, DeleteProjectUseCaseImpl}`, `feat/reports/be/app/impl`, `server/DevDataSeederConfiguration`, `testInfra/fe/FrontendKoinInitializedTest`.

**Procedure:**
1. **Capture base-ref snapshot before branching.** On `main` at the current SHA, run `./gradlew snagSharingReport && analysis/loc_report.sh` and copy `analysis/data/sharing_report_with_loc.csv` to `analysis/data/sharing_report_with_loc_base_main_<sha>.csv`. Commit this snapshot — `feature_retro.py` needs it to resolve deleted `feat/inspections/**` paths back to their original units.
2. `git worktree add .claude/worktrees/remove-inspections experiment/remove-inspections` off `main`. Wipe `build/` on first invocation (reused build caches from other worktrees can mask repair bugs).
3. Delete entire `feat/inspections/*` subtree + the 17 `include(":feat:inspections:...")` lines from `settings.gradle.kts`.
4. `./gradlew check --continue` to surface **all** errors at once (per the "fix all before rerun" feedback memory). `archCheck` runs as part of `check`; `archCheck` must also pass on the final repair — if a category-rule violation survives the repair, that violation is itself a collateral-ripple finding and is classified, not worked around.
5. Repair file-by-file. **Do not take shortcuts** — if a use case is missing, replace the call with a comment-marker `// REVERSE-REMOVAL: would invoke InspectionsUseCase` or delete the surrounding logic honestly; a shortcut that masks a real touchpoint is a measurement artifact and must be flagged in the yaml.
6. Rerun `./gradlew check --continue` until green.
7. `python analysis/feature_retro.py --change inspections-reverse-removal --ref experiment/remove-inspections --base-ref main --base-snapshot analysis/data/sharing_report_with_loc_base_main_<sha>.csv --stub`
8. Hand-classify every `unclassified` unit in `classifications/inspections-reverse-removal.yaml` into local/intrinsic/collateral + recurring flag + reason. Rule-matched entries are pre-populated and only need review.
9. `python analysis/feature_retro.py --change inspections-reverse-removal --ref experiment/remove-inspections --finalize`
10. **Branch is never merged.** Worktree is kept for reproducibility until Phase 5 writeup completes, then removed.

### Case 2 — Secondary: ProjectPhoto retrospective (commit `b5365d611`)

From scouting: 95 files, 4183 insertions, ~91% inside `feat/projects/*` (intra-feature extension), 6% in `feat/shared/database/*` (schema registration), 2% in `feat/findings/*` (refactor).

**Procedure:**
1. `python analysis/feature_retro.py --change projectphoto-forward --ref b5365d611 --base-ref b5365d611~1 --stub`
2. Hand-classify in `classifications/projectphoto-forward.yaml`.
3. `python analysis/feature_retro.py --change projectphoto-forward --ref b5365d611 --base-ref b5365d611~1 --finalize`
4. Expected shape per §4.3 narrative: local ≈ 91%, intrinsic ≈ 8% (schema registry + findings refactor pattern), collateral ≈ 1%. If the numbers come out differently, the discussion in §4.3 covers the surprise honestly.

### Case 3 — Synthetic: DVT test scenario

**Target entity: `Client`.** Full 3-layer model-inheritance chain `Client → AppClient → BackendClient`, contract DTO, FE SQLDelight + BE Exposed storage adapters, sync participation via `Versioned`. Stable — fields were historically added mandatory, so DVT is a latent property never exercised. Runner-up `Structure` skipped due to query-side asymmetry.

**Thesis DVT mechanisms (text.tex §Návrh DVT, lines 1960–1975).** Case 3 must test *both*:

1. **Layered / multiplatform-level model isolation.** Each layer and each multiplatform level declares its own model only when its semantics require attributes the upper layer doesn't provide; otherwise it transparently reuses the upper model. Between layers, mappers absorb the change. Between multiplatform levels, the more specific model *inherits* the more general one — so an attribute relevant only at one level should touch only the extended model, leaving shared levels untouched.
2. **Default attribute values.** New attributes default where semantics allow, so existing construction and consumption sites compile unchanged.

Mechanism (1) is invisible if Case 3 threads a new field through every layer. The faithful test therefore has two sub-experiments, one for each direction of mechanism (1), both asserting mechanism (2) on their own sites.

#### Case 3a — Shared attribute via inheritance (tests mechanism 1, downward propagation)

Declare one new field that semantically belongs to the universal `Client` concept. **Only** at `feat/clients/business/model/.../Client.kt`, with a default. Do NOT redeclare at `AppClient`, `AppClientData`, `BackendClient`, or `BackendClientData` — the inheritance chain should surface it automatically.

**Touches expected to be necessary:**
- `Client.kt` itself (the new property + default).
- Whatever *mappers* must carry the attribute out to wire + storage if the attribute needs to cross those seams. A field that is purely in-memory domain should require **no** mapper edit.
- Contract DTO + FE/BE storage column **only if** the attribute must be persisted or transferred; a pure in-memory field stops at the model.

**Touches that are anomalies** (the §4.6 headline for 3a is the count of these):
- Touching `AppClient` / `BackendClient` or their `Data` classes to redeclare the attribute — means inheritance is not carrying it, which is mechanism (1) failing.
- Touching any use case signature — means stamp coupling at the use case level is not absorbing it.
- Touching any caller construction site — means mechanism (2) defaults are not absorbing it.

#### Case 3b — Level-specific attribute via scoping (tests mechanism 1, upward containment)

Declare one new field that semantically belongs **only to the backend level** (e.g., an internal admin-audit timestamp the frontend has no business knowing about). Only at `feat/clients/be/app/model/.../BackendClient.kt` (or `BackendClientData`), with a default.

**Touches expected to be necessary:**
- `BackendClient.kt` + `BackendClientData` — the new property + default.
- BE Exposed table + BE mapper — persist the new column.
- Nothing else.

**Touches that are anomalies** (the §4.6 headline for 3b):
- ANY touch in `feat/clients/business/model/**` — means the level-specific attribute bled upward into the shared business model.
- ANY touch in `feat/clients/app/model/**` — same, one level up.
- ANY touch in `feat/clients/contract/**` — means the BE-only attribute is leaking onto the wire contract.
- ANY touch in `feat/clients/fe/**` (driven, app, driving, any FE adapter) — means the FE is forced to know about a BE-internal concept.
- ANY touch in FE SQLDelight schema or FE mappers — same.

#### Case 3c — Default absorption (mechanism 2, asserted inside 3a + 3b)

Not a separate experiment. For both 3a and 3b, verify that every existing construction site of the entity data class compiles unchanged because the new field has a default. Grep-style check before and after: `AppClientData(` and `BackendClientData(` caller sites must be identical. Any forced edit to a caller is a mechanism (2) failure and goes into §4.6.

#### DVT anomaly taxonomy — locked before running

Any of the following touches is an anomaly the §4.6 discussion must name. Committed to `classifications/dvt-client-field.yaml` as the stub **before** running `./gradlew check` so the definition cannot be retrofit.

| Touch | Applies to | Severity | Why it's an anomaly |
|---|---|---|---|
| Touching a layer or level *above* where the attribute semantically belongs | 3a (unnecessary `AppClient`/`BackendClient` redeclaration), 3b (any FE / shared-level touch) | **critical** | Mechanism (1) — layer/level model isolation — has failed |
| Any forced edit to an existing caller construction site | 3a + 3b | **critical** | Mechanism (2) — default absorption — has failed |
| Any file in `koinModulesAggregate/**` | 3a + 3b | high | DI wiring enumerates field-level concerns |
| Any file in `feat/sync/**` or a `*SyncHandler.kt` | 3a + 3b | **critical** | Sync layer not field-transparent |
| Any file in a *different* feature's module (`feat/projects/**`, `feat/reports/**`, etc.) | 3a + 3b | high | Client not encapsulated behind its port |
| Any `:contract` module *other than* `feat/clients/contract/**` | 3a + 3b | high | DTO sharing leaks |
| `testInfra/**` | 3a + 3b | low | Test fixtures enumerate entity fields — honest but weaker |
| FE SQLDelight migration `.sqm` or BE schema migration file | 3a (if persisted), 3b (expected for BE column) | **not an anomaly** | Schema evolution legitimately requires a migration |

#### Procedure

1. `git worktree add .claude/worktrees/dvt-synthetic experiment/dvt-client-field` off `main`.
2. Commit the stub `classifications/dvt-client-field.yaml` with the anomaly taxonomy populated for both 3a and 3b, *before* writing any code.
3. **Sub-experiment 3a.** Add the shared attribute at `feat/clients/business/model/.../Client.kt` only, with a default. Run `./gradlew check`. Record every touched file outside `feat/clients/business/model/**` in the yaml. Decide per file whether it was necessary (contract + storage carriers) or an anomaly (redeclaration at a lower layer).
4. **Sub-experiment 3b.** On the same branch, add the BE-only attribute at `feat/clients/be/app/model/.../BackendClient(Data).kt` only, with a default. Run `./gradlew check`. Record every touched file outside `feat/clients/be/**` as an anomaly (everything FE-facing should remain untouched).
5. Round-trip serialization test: author a small Kotlin test (in `feat/clients/contract/src/commonTest/`) that deserializes a JSON payload **without** the new shared-attribute field (3a) and a payload **with** it, asserting both parse and round-trip. Only 3a needs this — the 3b BE-only field never reaches the wire. Committed on the experiment branch only.
6. `python analysis/feature_retro.py --change dvt-client-field --ref experiment/dvt-client-field --stub` → hand-review → `--finalize`. The §4.6 headline numbers printed to stdout are: `3a non-essential touches = N1` and `3b upward-bleed touches = N2`.
7. Branch kept, not merged.

### Case 4 — Optional tertiary: iOS-only Project extension

Only execute if Phase 2 calendar allows. Procedure analogous to Case 3 but on a different branch, targeting `Project` with an `iosCacheLocalPath: String?` field introduced in `feat/projects/business/model/src/iosMain/...` as an `IosProject` interface extending the shared `Project`. The test is whether *zero files in commonMain* need to change — if any commonMain touch is forced, it's a platform-axis anomaly and goes into §4.6 discussion.

Skip without guilt if time-pressed; the primary (Case 1) + secondary (Case 2) + DVT synthetic (Case 3) cover §4.3 + §4.6 sufficiently.

---

## Critical files to reference (read, do not modify)

- `analysis/thesis-evaluation-plan.md` — §4.3 spec, Pillar 2 methodology, work order Phase 2, risks (4), (5), (6).
- `analysis/loc_report.sh` + `analysis/figures.py` + `analysis/data/sharing_report_with_loc.csv` — Phase 1 canonical outputs, upstream of Phase 2.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/architecture/ModulePathParser.kt` + `ModuleIdentity.kt` — authoritative module path grammar; reached from Python via `SharingReportTask`'s `source_set_dir_rel` column, not reimplemented.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/architecture/ArchitectureCheckSetup.kt` — pattern for collecting project dependencies in Gradle.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/analysis/SharingReportTask.kt` + `SharingReportRowBuilder.kt` + `SharingReportSetup.kt` — Phase 1 pattern that `DependencyGraphTask` mirrors.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/MultiplatformModuleSetup.kt` — source-set hierarchy for closure expansion.
- `koinModulesAggregate/{fe,be}/.../ModulesAggregate.kt` — confirmed aggregation points.
- `settings.gradle.kts` — 17 `:feat:inspections:*` lines to delete on Case 1 branch.
- Memory file `feedback_fix_all_before_rerun.md` — `check --continue` discipline during Case 1 repair.

---

## Verification

1. **Tooling self-test on Phase 1 data.** Run `python analysis/dependency_closure.py` on current `main`, confirm it emits `analysis/data/dependency_closure.json` with one entry per unit in `sharing_report_with_loc.csv`. Spot-check: `:core:foundation:common::commonMain` should have a large `blast_radius_module`; `:composeApp::androidMain` and `:androidApp::main` should have near-zero blast radius (top of the graph).
2. **Sharing report rel-path column.** Verify `sharing_report_with_loc.csv` now has a `source_set_dir_rel` column, relative to repo root. Unit test: `SharingReportRowBuilder` fixture assertion for a known module.
3. **ProjectPhoto retrospective reproducibility.** `python analysis/feature_retro.py --change projectphoto-forward --ref b5365d611 --base-ref b5365d611~1 --stub` must produce a file count that, after collapsing renames, equals `git show --stat b5365d611` (95 or 95 − rename-pair count). Any mismatch = longest-prefix mapping bug.
4. **Inspections reverse repair compiles.** `./gradlew check` (including `archCheck`) passes on `experiment/remove-inspections` before the classifier runs. Repair log entries match the diff, and the yaml contains zero `unclassified` entries when `--finalize` succeeds.
5. **DVT round-trip.** Kotlin test on `experiment/dvt-client-field` asserts both payload shapes parse; `./gradlew :feat:clients:contract:allTests` passes.
6. **Ripple CSVs match figures.py expectations.** `figure_ripple_buckets()` renders from `analysis/data/ripple_*_units.csv` without column errors; generated PDF has one bar per change with recurring-intrinsic count annotated.
7. **Headline recurring count printed.** `feature_retro.py --finalize` prints `recurring intrinsic units = N` to stdout for each change; this is the §4.7 headline number.
8. **Idempotence.** Running the full pipeline twice produces byte-identical CSVs, JSON, and PDFs.
9. **PR gate.** Phase 2 ships as one or two PRs against `main` analogous to Phase 1's PR #223:
   - PR A: tooling (`DependencyGraphTask`, `SharingReportRowBuilder` rel-path edit, `dependency_closure.py`, `feature_retro.py`, `ripple_rules.yaml` scaffold, `figures.py` Phase 2 additions, Kotlin + pytest unit tests).
   - PR B: case-study data (`classifications/*.yaml`, `analysis/data/ripple_*_{files,units}.csv`, `analysis/data/sharing_report_with_loc_base_main_<sha>.csv`, regenerated `analysis/figures/fig_4_3_*.pdf`).
   - The `experiment/*` branches themselves are not merged — only the CSV/YAML artifacts they produced.

---

## Out of scope for Phase 2

- §4.3 Czech prose — deferred to Phase 5 per the plan's sequencing principle.
- Phase 3 (Wear OS live experiment), Phase 4 (§4.2 writeup), Phase 5 (§4.5–4.9 prose). Each has its own plan.
- Refactoring any production code to *reduce* ripple — the measurement must reflect the architecture as it stands, not an idealized version.
- A new `cloc`-based LOC counter — tokei is pinned and authoritative.
- Porting `ripple_rules.yaml` into the Gradle build — it's author tooling, not part of `./gradlew check`.

---
---

# Addendum (2026-04-14): Sharing-vs-evolvability duality + counterfactual commonization case study

The sections below are appended after reviewing the Phase 1 + Phase 2 outputs against the thesis §Maximalizace sdílení kódu argument. They introduce one additional descriptive metric for §4.2 and one additional case study for §4.3/§4.6/§4.7 that together address a methodological gap the original Phase 2 plan did not foreground: *static metrics alone cannot prove correctness of scoping decisions, because sharing and evolvability show up as the same numbers read from opposite sides*. The addendum is the durable artifact of that design discussion and is intended to feed Phase 5 prose directly.

## A. Methodological framing — why static metrics alone cannot make the thesis argument

The thesis §Maximalizace sdílení kódu (lines 1714–1900 of `~/Ctu/dp-thesis-timotej-adamec/text/text.tex`) argues that **code must be placed at the multiplatform level that matches its semantic scope**. Over-sharing leaks higher-level specifics downward and forces all platforms to rebuild on changes that concern only one. Under-sharing duplicates common logic and invites drift. The "correct" placement is the level where the semantics of the code align with the platforms it reaches.

The subtlety this addendum addresses is that **sharing and evolvability, measured statically, are the same numbers read from opposite sides of the same coin**.

Consider any per-layer share ratio (LOC in platform-neutral source sets ÷ total LOC in the layer). The sharing view says "high share ratio in this layer is the architectural goal realized." The evolvability view says "high share ratio in this layer is a ripple surface — any change here is forced to ripple into every platform that pulls from this layer." The *same number* supports both readings simultaneously. Which reading is correct depends on whether the share matches the semantic boundary of the code in that layer, and **semantic fit is not visible from the filesystem**.

Consequences for Kapitola 4:

- A sharing-by-layer heatmap / share-ratio table (shipped in Phase 1) legitimately describes *what Snag looks like*. It cannot *by itself* prove that Snag's placement is correct, because the same table is compatible with both a correctly-scoped architecture and an overgeneralized one where the layer simply happens to have a lot of code-like logic crammed into `commonMain`.
- The scope-aware blast radius (shipped in Phase 2 as `blast_radius_module` via `dependency_closure.py`) annotates each touched file with how many downstream modules would rebuild. This is useful for §4.3 repair-log annotations but suffers from the same symmetry: a high blast is bad if the sharing was wrong, necessary if the sharing was right.
- Static numbers describe structure; they do not adjudicate whether that structure matches semantics. **Any thesis claim about correctness of scoping must come from a source other than the static per-layer share ratio.**

This is not a weakness of the metrics — it is a structural feature of any purely-descriptive evaluation. The fix is methodological: the argument the thesis needs is **comparative**, not absolute. The comparison must fix the semantics (what code we are placing) and vary the placement (correct vs. overgeneralized), then measure the delta in a realistic evolution step.

### Correction to an earlier framing

An earlier draft of the Phase 2 plan's reasoning implicitly suggested that platform divergence lives in driving/driven adapter layers. That understates Snag's architecture. Snag explicitly places divergence at the **application layer** where the semantics demand it: `WebAddFindingPhotoUseCase` and `NonWebAddFindingPhotoUseCase` (thesis §2857) are use cases — application-layer contracts — with different return types (`OnlineDataResult` vs `OfflineFirstDataResult`). The semantic boundary is at the use-case contract itself, not the adapter that implements it. Platform divergence is legitimate wherever in the hexagonal stack the semantics diverge; the architecture does not stipulate a "lowest-layer-only" rule. The only rule is *match the divergence to the layer where the semantic difference appears* — which can be anywhere from driven/impl up through the application layer (and, in rare cases, the domain model itself, though Snag's design explicitly targets domain purity as the aspirational case).

### What this addendum adds

Two things:

1. **One descriptive §4.2 metric: per-hexagonal-layer platform-specific LOC share.** A factual readout — "X% of `driving/impl` LOC lives in non-common source sets, Y% of `business/model` LOC does." It describes Snag's structure without claiming the structure is correct. Combined with the existing sharing heatmap and blast-radius table, it completes the Phase 1+2 descriptive picture of §4.2.

2. **One counterfactual case study: commonize `AddFindingPhoto` (and, if possible as a replicate, `AddProjectPhoto`).** Take an existing correctly-scoped flow, force it into a single `commonMain` use case, adapt everything that breaks, then apply the same realistic web-specific evolution (adding an upload-progress callback) to both the correct branch and the commonized branch. Measure the ripple delta with the existing `feature_retro.py` + `dependency_closure.py` tooling AND author two theory-grounded qualitative critique files — one per branch. Numbers + critique together are the evidence; neither alone suffices because of the sharing/evolvability duality above.

The descriptive metric supports §4.2; the counterfactual supports §4.3, §4.6, and the §4.7 headline. The methodological framing (duality of readings + necessity of counterfactual) lands in §4.2 introduction and §4.9 threats-to-validity.

## B. Reasoning narrative — the discussion that produced this addendum

This subsection preserves the key reasoning steps that led to the design below, in the order they emerged. It is written in narrative form so Phase 5 prose can draw from it directly. Bullet points and code references are intentional; the prose should not be stripped when translated to Czech.

1. **Initial observation.** Reviewing the Phase 2 `dependency_closure.json` against the thesis §4.2 sharing claim, it was not obvious that the module-count blast radius answered the right question. Blast radius says "how many modules rebuild when X changes"; the thesis says "is X placed at the right multiplatform level." These are related but not the same question.

2. **First attempt: LOC per leaf platform + reach coefficient.** Considered introducing a metric where `reach coefficient = sum of LOC projected onto leaf platforms / total unique LOC`. For a perfectly-sharing codebase, coefficient ≈ 6 (every line compiles for all 6 Snag leaves). For a fully-fragmented codebase, coefficient ≈ 1. Initially this seemed like the right headline number.

3. **The duality emerges.** The reach coefficient fails because it conflates over-generalization and correct-sharing. A line that sits in `commonMain` of a FULL plugin contributes 6 to the sum regardless of whether it *should* be in commonMain. A codebase that force-shared everything would score higher than a codebase that correctly scopes — and "higher reach coefficient" is superficially "more sharing," but the thesis argues the forced-sharing codebase is *worse*. One number, two interpretations. Dropped the metric.

4. **Generalization.** Any per-layer static sharing metric has the same problem. The sharing view ("high share = good") and the evolvability view ("high share = ripple surface") produce identical raw numbers. Which reading is correct is a function of semantic fit, and semantic fit is not observable from code alone. This is a structural property of descriptive measurement, not a flaw in any specific metric.

5. **Concession to descriptiveness.** Per-layer sharing tables are still useful for §4.2, but they must be presented honestly as *descriptive, not probative*. The thesis prose for §4.2 should explicitly state that these tables show where Snag's code sits, not whether the sitting is correct. This is Part A below.

6. **Searching for a probative metric.** The only way to break the duality is to fix the semantics and vary the placement. Fixing the semantics means picking a code region whose correct multiplatform level is not in dispute — ideally one where the type system already enforces the distinction. For Snag, `AddFindingPhoto` is perfect: native uses `OfflineFirstDataResult` and web uses `OnlineDataResult`; these are different Kotlin types the compiler enforces. Any common abstraction has to widen the return type, which is a type-level act the author cannot hide.

7. **The counterfactual is a second case study.** Vary the placement by actually commonizing `AddFindingPhoto` on a branch. Apply the same realistic web-specific evolution (upload progress callback, because browsers support it via fetch/XHR and native offline-first doesn't need it) to both branches. Measure the ripple delta. The delta between branches is the claim's evidence. Numbers only — the critique concern is addressed in step 9.

8. **Replication improves the counterfactual.** One counterfactual is a demonstration; two is a pattern. `AddProjectPhoto` is the natural replicate because it (likely) has the same offline/online asymmetry. If ProjectPhoto is currently not multiplatform-split, skip rather than fake a replicate.

9. **Numbers alone are still dismissible.** A skeptical reader can say "you just commonized it badly; a better engineer would find a cleaner commonization." The defense is not more numbers; it is *theory-grounded qualitative observations*. If the commonization forces a sealed `PhotoUploadResult` return type, that is a textbook DVT violation regardless of the author's skill. If it forces an `if (isWeb)` branch in `commonMain`, that is a textbook SoC violation regardless of the author's skill. Normalized Systems Theory specifies the *shapes* of these anomalies; observing them in the counterfactual is evidence the violations are intrinsic to forcing commonization against a type-level semantic boundary, not accidental to the author's execution. Numbers + critique together break the rhetorical opening.

10. **Honesty requirements.** The counterfactual must be "minimum honest forced commonization," not a straw man. The commonized version must be a plausible less-careful engineering choice a reviewer would recognize from real PRs. An `archCheck` failure on the commonized branch is data, not a problem to fix. Qualitative observations must cite file + line and a theory category, not vague reviewer preferences.

11. **Framing for thesis prose.** §4.2 introduces the duality as a method note. §4.3 executes the counterfactual as a case study alongside Case 1/2/3. §4.6 maps NS theorems to the observed anomalies. §4.7 reads the numeric delta ratio as the headline. §4.9 threats-to-validity restates the duality and notes that the counterfactual is how the evaluation argues past it.

## C. Part A — descriptive metric: per-hex-layer platform-specific LOC share

### What it measures

For each hexagonal layer (derived from the `hex_layer` column in `sharing_report_with_loc.csv`), partition its rows by whether the source set is `commonMain` / `main` (neutral) or one of the named platform-specific source sets (`nonWebMain`, `webMain`, `nonAndroidMain`, `nonJvmMain`, `mobileMain`, `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`). Sum `kotlin_loc` in each partition. Report:

- per-layer total LOC (numerator)
- per-layer platform-specific LOC (subset)
- per-layer platform-specific share (subset / numerator, as a %)
- per-layer number of modules that have at least one platform-specific source set with non-zero LOC (the "divergence inventory" — how many modules in this layer actually exercised the multiplatform-level capability)

Optionally broken down by platform-set label (`web` vs `mobile` vs `android` etc.) for a stacked bar.

### Why it is descriptive only (and the thesis must say this)

This metric does not prove correctness. It is compatible with at least three distinct architectural realities:
- A correctly-scoped codebase where divergence lives in the layers where semantics diverge.
- An overgeneralized codebase where everything sits in `commonMain` and the metric reads "~0% platform-specific" everywhere, which looks like perfect sharing but is actually hiding forced commonization.
- An over-fragmented codebase where everything is duplicated across `androidMain`/`iosMain`/`jvmMain` and the metric reads "~100% platform-specific" everywhere, which looks like maximum scoping but is actually hiding combinatorial drift.

The thesis §4.2 prose, when Phase 5 writes it, must explicitly state that this table is descriptive — it tells the reader where Snag's divergence lives, not whether it should live there. The counterfactual in Part D is what provides the "should" argument.

### Implementation

File: `analysis/figures.py`

- Add `figure_layer_divergence()` that consumes `analysis/data/sharing_report_with_loc.csv`.
- Aggregation key is the `hex_layer` column (already emitted by `SharingReportRowBuilder`). Rows with empty `hex_layer` (app modules, some libs) are grouped as `"other"`.
- Two outputs:
  - CSV `analysis/data/layer_divergence.csv` with columns `hex_layer, total_loc, platform_specific_loc, platform_specific_share, divergent_module_count, total_module_count`.
  - PDF `analysis/figures/fig_4_2_layer_divergence.pdf` — stacked horizontal bar per layer, segments = commonMain / nonWebMain / webMain / other platform-specific; annotated with divergent_module_count.
- Add unit test `analysis/tests/test_layer_divergence.py` with ~4 fixture-based cases: empty input, pure-common layer (share = 0%), pure-platform-specific layer (share = 100%), mixed layer (share matches hand-computed ratio).

### Thesis §4.1 operationalization row

Single new row under O1 in `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` table `tab:eval-operacionalizace`:

```latex
O1 & Podíl platformně-specifického LOC podle vrstvy hexagonální architektury (deskriptivní, nevyvracející)
   & SharingReport CSV $\bowtie$ tokei LOC
   & \code{figures.py::figure\_layer\_divergence}
   & TBD \\
```

Label explicitly says "deskriptivní, nevyvracející" so the reader knows the table is a readout, not a correctness claim.

## D. Part B — counterfactual commonization case study (the actual argument)

### Claim under test

*If the `AddFindingPhoto` flow were commonized into a single `commonMain` use case — hiding the `OnlineDataResult` vs `OfflineFirstDataResult` semantic difference behind a forced common abstraction — then a realistic web-specific evolution would ripple into substantially more files, more modules, and more leaf platforms than it does with the existing correctly-scoped structure, AND the commonized branch would exhibit textbook NS-theorem anomalies (SoC, DVT, AVT, ISP violations) that the correctly-scoped branch does not.*

The numeric delta between the correct and commonized branches, measured on the same evolution step, is the first half of the evidence. The qualitative observation list on the commonized branch, grouped under NS-theorem headings, is the second half. Neither alone is sufficient.

### Target flow: `AddFindingPhoto`

Confirmed structure from thesis §2857 (line 2857 of `text.tex`) and Snag source:

- `NonWebAddFindingPhotoUseCase` lives in `feat/findings/fe/driving/impl/src/nonWebMain/.../` with return type `OfflineFirstDataResult`.
- `WebAddFindingPhotoUseCase` lives in `feat/findings/fe/driving/impl/src/webMain/.../` with return type `OnlineDataResult`.
- Platform-specific ViewModels (also in the respective non-common source sets) call the matching use case.
- Domain model `Finding` and its photo associations live in `commonMain` — only the *upload action* has divergent semantics, not the data.

Before starting the experiment, confirm this structure on the current main branch by:

```
grep -rn "AddFindingPhotoUseCase" feat/findings/ --include="*.kt"
```

Record the exact file paths + LOC on each side in a `facts.md` pinned to the experiment branch so the counterfactual has a precise "before" snapshot to compare against.

### Replicate target: `AddProjectPhoto` (if structurally analogous)

Check whether Snag's ProjectPhoto flow (introduced in commit `b5365d611`) has an analogous `NonWeb*` / `Web*` split. If yes, commonize it on the same experiment branch and report a second set of numbers. If no (e.g. if ProjectPhoto currently has only a non-web implementation and is not multiplatform at the same granularity), skip it and proceed with only the AddFindingPhoto counterfactual. Do not manufacture a replicate that does not exist.

Pre-check command:

```
find feat/projects -name "*ProjectPhoto*" -type f
```

### Counterfactual branch design

Branch: `experiment/commonize-photo` off current `main`.

Steps:

1. Create `AddFindingPhotoUseCase` interface in `feat/findings/app/model/src/commonMain/.../` (or wherever the app layer currently lives for findings). Return type must be a single shape that can accommodate both online and offline flows — the honest choice is a new sealed type `PhotoUploadResult` that wraps either variant, because the alternatives (picking one return type and papering over the other) lose information the upper layer needs. **Note in the commit message that this abstraction is deliberately forced — it exists only to test the counterfactual, not as a design proposal.**
2. Remove `NonWebAddFindingPhotoUseCase` and `WebAddFindingPhotoUseCase`. Move their bodies into the implementation of the common use case, which now needs to internally select between the offline-first and online-only paths. Likely via a new `PhotoStoragePort` interface (commonMain) with `NonWebPhotoStoragePort` and `WebPhotoStoragePort` adapters in the respective driven source sets. Wire via Koin.
3. Update both ViewModels (web and non-web) to call the new common use case. They now share more code but each must handle the sealed `PhotoUploadResult` variants appropriately.
4. Run `./gradlew check --continue`. Fix every failure in one pass (per the `feedback_fix_all_before_rerun` memory). `archCheck` must pass on the final state — if the forced commonization triggers an `archCheck` violation, that violation itself is part of the counterfactual's cost and must be recorded, not worked around.
5. Commit as one clean commit `refactor: commonize AddFindingPhoto (counterfactual)` on `experiment/commonize-photo`. The branch is never merged.

Pitfalls the counterfactual must be honest about:
- If commonization forces a `PhotoStoragePort` port split, count the port interface, its impls, and their Koin wiring as cost. They exist only because the upper layer was forced into `commonMain`.
- If commonization forces the `Result` type to widen (sealed `PhotoUploadResult`), count the new type + every `when` exhaustive-match site on both branches.
- Do not commonize too aggressively — the goal is "minimum honest forced commonization," not "worst possible commonization." A reader should agree that the commonized version is a plausible way a less-careful engineer would have written it, not a straw man.

### Evolution step to apply to both branches

**Chosen: add an upload-progress callback to the web photo upload path.** Realistic because (a) browsers support progress events via `XMLHttpRequest` / `fetch` streams, (b) native platforms currently don't need it because offline-first saves to disk immediately and syncs in the background — there is no foreground progress to report, and (c) it is a concrete per-platform semantic that over-commonized designs handle awkwardly.

Apply on each branch:
- On `main` → create `experiment/photo-progress-correct` containing only the progress-callback addition.
- On `experiment/commonize-photo` → create `experiment/photo-progress-commonized` containing the equivalent change on top of the commonized structure.

Both evolution branches must deliver the same user-visible behavior: web shows upload progress, native is unaffected.

### Measurement — numeric

For each of the two evolution branches:

```
python analysis/feature_retro.py \
  --change photo-progress-<correct|commonized> \
  --ref experiment/photo-progress-<correct|commonized> \
  --base-ref <parent branch> \
  --base-snapshot analysis/data/sharing_report_with_loc_base_<ref>_<sha>.csv \
  --stub
```

Hand-classify the resulting YAML (expected to be small — a handful of files on the correct branch, larger on the commonized branch), then:

```
python analysis/feature_retro.py --change <same> --ref <same> --finalize
```

Which emits `analysis/data/ripple_photo-progress-<correct|commonized>_{files,units}.csv`.

Numbers extracted from each CSV into a final comparison table:

| Metric | Correct (current Snag) | Commonized (counterfactual) | Delta |
| --- | --- | --- | --- |
| Files touched | N1 | N2 | N2 − N1 |
| Modules touched | M1 | M2 | M2 − M1 |
| Source-set units touched | U1 | U2 | U2 − U1 |
| Leaves rebuilt | L1 | L2 | L2 − L1 |
| Sum of touched units' `blast_radius_module` | B1 | B2 | B2 − B1 |
| LOC churn | C1 | C2 | C2 − C1 |
| Intrinsic-recurring file count | I1 | I2 | I2 − I1 |

The delta column is the numeric evidence. `L1` is expected to be 2 (js + wasmJs); `L2` is expected to be 6 (the change touches the commonMain use case contract, which every FE leaf + BE tests recompile against). A ratio `B2/B1` larger than ~5 would be a strong finding; smaller than ~2 would be a surprise and would need §4.3 prose to explain why.

### Measurement — qualitative observations (the other half of the evidence)

A skeptical reader can respond to any numeric delta with "maybe you commonized it poorly; a more skilled engineer would have found a cleaner commonization." The counterfactual has to defend against this critique. The defense is qualitative: name the specific NS-theorem anomaly shapes the commonization introduced. Anomaly shapes are specified in the theory (Mannaert, Verelst & De Bruyn, cited throughout §2) — they are not subjective reviewer preferences. If the commonization exhibits a textbook SoC, DVT, or AVT violation, that is evidence the violation is intrinsic to the forced commonization, not accidental to the author's execution.

Two committed artifacts on the experiment branch, authored during the experiment:

1. **`analysis/classifications/photo-correct_critique.md`** — short structured observation list on the *current* (correctly-scoped) Snag code, written by reading `feat/findings/fe/driving/impl/src/{nonWebMain,webMain}/` on `main` **before** the counterfactual starts. Focus: what the type system prevents, what cognitive load the current structure avoids, what parallel-evolvability it enables. Each observation cites file + line. Aim for ≥3 observations.

2. **`analysis/classifications/photo-commonized_critique.md`** — structured observation list on the `experiment/commonize-photo` branch, **written while the commonization is being done**, not reverse-rationalized after measurement. Grouped under NS-theorem headings:

   - **SoC violations** — any `if (isWeb)` / `when (platform)` branching introduced in `commonMain` to route between offline-first and online-only paths; any Koin module that binds platform-conditional implementations at the `commonMain` use-case level; any comment that starts with "TODO: native will never hit this" or equivalent.
   - **DVT violations** — any forced widening of a return type (the sealed `PhotoUploadResult` is the expected canonical example); any callsite that handles sealed variants only to match the "other platform's" case; any type the web ViewModel must pattern-match on that it can never semantically receive.
   - **AVT violations** — any dependency that had to be promoted from `implementation` to `api` to make the commonization compile; any module that had to re-export an internal type because a downstream commonMain consumer needs the widened shape.
   - **SoS violations** — any platform-specific state that had to be lifted into shared state for the commonization to work.
   - **Interface pollution (ISP)** — any port that ended up with methods only one implementation uses meaningfully; any default-argument workaround for "method only web needs."
   - **Loss of compile-time guarantees** — what specific invariants the compiler enforced in the correct branch and no longer enforces in the commonized branch. Quote the type signatures before and after.
   - **Parallel-evolvability loss** — for each use case, one sentence: "in the correct branch, web and native contracts can evolve independently; in the commonized branch, every contract change requires coordinating web-side and native-side handlers." Trivial to state; cite the specific ViewModel code that now has cross-platform coupling.
   - **archCheck outcome** — if `./gradlew archCheck` passes on the commonized branch, state that explicitly and discuss what it implies ("the category rules allow the commonization, but a reviewer still would not"). If it fails, quote the exact rule and violation — the failure is data, not a problem to fix.
   - **General smells (reviewer-judgment)** — anything a senior Kotlin reviewer would flag that doesn't fit the categories above. Fewer is better; this bucket exists so the critique doesn't overclaim with textbook labels.

   Each entry cites: file path + line range on the counterfactual branch, the theory anomaly category, and a one-sentence description. Aim for 5–15 observations — enough to demonstrate a pattern, few enough to read.

Format each entry:

```markdown
### SoC-1: platform branching in commonMain use-case implementation

**Location:** `feat/findings/app/impl/src/commonMain/.../AddFindingPhotoUseCaseImpl.kt:47-52`

**Observation:** The use case delegates to the injected `PhotoStoragePort`, but the Koin module at `feat/findings/fe/driving/impl/src/commonMain/.../PhotoStorageModule.kt:18` binds a `PlatformAwarePhotoStoragePort` that internally branches on `Platform.current`. The branch exists in commonMain, so every platform compiles code for cases it cannot reach at runtime.

**Theory:** Mannaert et al. §SoC — concerns (native offline-first, web online-only) are structurally fused where they should be separated.

**Delta vs correct branch:** in `main`, the platform decision is made at module-level (which source set contains the use case), not at runtime. The commonMain code on `main` contains zero references to the other platform's path.
```

3. **Cross-cutting synthesis — `analysis/data/counterfactual_photo_progress.md`** — the top-level comparison document. Sections:
   - One-paragraph context (what was counterfactualized, why AddFindingPhoto, what evolution step was applied).
   - The numeric comparison table (N1 vs N2, etc.).
   - Summary counts: "K observations in `photo-commonized_critique.md` across {SoC: A, DVT: B, AVT: C, SoS: D, ISP: E, compile-time loss: F, parallel-evolvability loss: G}."
   - 2–3 verbatim-quoted observations from the critique that best exemplify the cost, with file:line references.
   - Short closing paragraph: the numbers + the critique together are the evidence. Neither alone would be sufficient; the numbers because of the sharing/evolvability duality, the critique because a reader could dismiss isolated observations as author bias.

This file is the §4.3 / §4.6 / §4.7 primary source. Phase 5 prose pulls from it directly.

### Thesis placement

- **§4.3** (feature-level evolvability case studies): the counterfactual is a fourth case study alongside Case 1 (inspections reverse removal), Case 2 (ProjectPhoto retrospective), Case 3 (DVT synthetic). Ripple dekompozice for both branches + the comparison table.
- **§4.6** (NS theorems mapped to measured data): the counterfactual is the direct SoC/DVT evidence — SoC because the commonized version violates it (web semantics contaminate common code), DVT because the forced sealed `PhotoUploadResult` is the canonical DVT-violation shape. The ripple delta is the concrete price paid for each violation; the `photo-commonized_critique.md` observation list, grouped under NS-theorem headings, provides the *shape* of each violation. Numbers quantify cost, critique names the cost — both are needed because numbers alone are susceptible to the "you just commonized it badly" critique, while the theory-grounded observations show the violations are textbook anomalies intrinsic to forcing commonization against a type-level semantic boundary.
- **§4.2** (sharing quantification): descriptive per-layer metric from Part A. Must explicitly state "this describes structure, not correctness — the counterfactual in §4.3 is where correctness is argued."
- **§4.7** (synthesis): the headline number is the delta ratio (`B2/B1` or `files_2/files_1`). If it is ≥3×, it directly supports the thesis; if it is <2×, §4.7 must say so honestly and discuss what it implies about Snag's sharing-vs-evolvability tradeoffs.
- **§4.9** (threats to validity): the duality-of-readings framing lands here. "Per-layer sharing ratios describe structure; they are compatible with both correct and overgeneralized placements, and therefore cannot by themselves adjudicate correctness. The §4.3 counterfactual is the mechanism by which this evaluation argues that Snag's chosen splits are load-bearing rather than incidental."

## E. Durable reasoning points for Phase 5 prose

The following conceptual points emerged in the discussion that led to this addendum. They are captured here because this file is the durable project artifact between sessions.

1. **Sharing ↔ evolvability duality.** Any per-layer LOC sharing metric is symmetric: the same number is "good" from the sharing view and "a ripple surface" from the evolvability view. Which reading is correct depends on semantic fit. Static metrics cannot assess semantic fit. The thesis must state this in §4.2 or §4.9 and treat the static numbers as descriptive.

2. **Divergence is legitimate at any hex layer, not just adapters.** The place where platform semantics diverge is the place where the architecture must allow a split. For Snag's `AddFindingPhoto`, that place is the application-layer use case contract itself — native's return type is `OfflineFirstDataResult`, web's is `OnlineDataResult`, and these are type-level semantic differences the compiler enforces. The thesis should not carry any language implying divergence must live "at the edges" or "in driven/impl only" — that understates the architecture. The rule is "match divergence to the layer where the semantic difference appears."

3. **Counterfactual is the only form of evidence that breaks the duality, and even the counterfactual needs two kinds of evidence — numeric *and* qualitative.** If you don't fix the semantics, any number you collect is compatible with multiple architectural realities. If you fix the semantics (by selecting a flow whose divergence is type-level and non-negotiable, like the `OnlineDataResult`/`OfflineFirstDataResult` split) and then vary the placement experimentally, the ripple delta is a clean comparison that the duality cannot swallow. But numbers alone leave a rhetorical opening: "you just commonized it badly." The defense is theory-grounded qualitative observations naming each NS-theorem anomaly the commonization introduced. A forced sealed `PhotoUploadResult` is a textbook DVT violation regardless of the author's skill; an `if (isWeb)` branch in `commonMain` is a textbook SoC violation regardless of the author's skill. Numbers + critique together are the load-bearing argument for §4.7; either alone is dismissible.

4. **Replication strengthens the counterfactual.** One commonization case is a demonstration; two is a pattern. `AddProjectPhoto` is the natural replicate because it has (likely) the same offline/online semantic asymmetry as `AddFindingPhoto`. Check before committing; if ProjectPhoto is not currently multiplatform-split, skip it rather than manufacturing a replicate.

5. **The counterfactual must be honest.** The commonized branch must be a "plausible less-careful engineering choice," not a straw man. If an archCheck violation surfaces on the commonized branch, that violation is a genuine cost of the overgeneralization and belongs in the measurement; do not work around it. If widening the return type requires exhaustive `when`-match updates across the codebase, count those touches. The delta only matters if both branches are honest implementations of their respective design choices. Honesty also applies to the qualitative critique: each observation must cite a specific file + line and a specific theory category, not vague reviewer preferences. Five precise observations beat fifteen vague ones.

6. **Other metrics remain useful but are not centerpiece.** The module-count `blast_radius_module` still annotates ripple files in §4.3. The per-layer fragmentation metric (Part A / Part C of this addendum) still describes structure in §4.2. The sharing heatmap still visualizes the overall shape. None of them by themselves prove correctness; the counterfactual does, and only when its numeric and qualitative halves are read together.

7. **Why LOC-per-leaf-platform was considered and rejected.** An earlier draft of this addendum proposed a `reach coefficient = sum of leaf-projected LOC / total unique LOC` as a single headline sharing number. Dropped because it conflates over-generalization and correct-sharing: a forced-commonized codebase scores higher (closer to 6, the number of leaves) than a correctly-scoped codebase, and "higher is better" reads as the sharing view while "higher is worse" reads as the evolvability view — the duality again. The reach coefficient was the clearest illustration that the thesis needed a non-static source of evidence.

## F. Implementation order for the addendum work

1. **Part A (descriptive metric).** ~1 day. `figures.py` addition + pytest + thesis operationalization row. No branch, no case study, no experiment — pure offline tooling.
2. **Part B preflight.** ~0.5 day. Confirm `AddFindingPhoto` structure on main; check `AddProjectPhoto` for replicate eligibility; write the `facts.md` before-snapshot; pick the evolution step; sanity-check that the realistic evolution really is web-specific and doesn't require native changes.
3. **Part B — correct branch evolution.** ~0.5 day. Branch `experiment/photo-progress-correct` off `main`, add progress callback, run `archCheck`, run `feature_retro.py --stub`, hand-classify, `--finalize`. Also author `photo-correct_critique.md`.
4. **Part B — commonization.** ~1 day. Branch `experiment/commonize-photo` off `main`, force-commonize AddFindingPhoto (and optionally AddProjectPhoto), fix all breakage in one pass, verify archCheck passes or record the violation. Author `photo-commonized_critique.md` **during** the commonization, not after.
5. **Part B — commonized evolution.** ~0.5 day. Branch `experiment/photo-progress-commonized` off `experiment/commonize-photo`, add the same progress callback, run `feature_retro.py` pipeline.
6. **Comparison table + thesis §4.1 operationalization rows.** ~0.5 day. Final CSVs committed; comparison table inserted into `analysis/data/counterfactual_photo_progress.md`; two new O2 rows in the operationalization table.

Total budget: ~4 days. Slots into the existing Phase 2 schedule as effectively a Case 1b, executed between Cases 1–3. Part A first, then Part B (confirmed direction from the design discussion).

## G. Critical files for the addendum work

Read-only references:
- `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` lines 1714–1900 (§Maximalizace sdílení kódu), line 2857 (AddFindingPhoto semantics), lines 3130–3270 (§4.1 operationalization, §4.2 preview).
- `analysis/dependency_closure.py` — scope-aware blast tooling, already shipped in PR #228.
- `analysis/feature_retro.py` — ripple classifier, already shipped in PR #228.
- `analysis/data/sharing_report_with_loc.csv` — Part A input.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/analysis/SharingReportRowBuilder.kt` — `hex_layer` column definition.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/analysis/PlatformReach.kt` — `platform_set` label derivation (read-only source of truth for any future leaf-axis metrics).
- `feat/findings/fe/driving/impl/src/{nonWebMain,webMain}/.../` — AddFindingPhoto current source.

To be created / modified:
- `analysis/figures.py` — add `figure_layer_divergence()`.
- `analysis/tests/test_layer_divergence.py` — new pytest file.
- `analysis/data/layer_divergence.csv` + `analysis/figures/fig_4_2_layer_divergence.pdf` — outputs.
- `experiment/commonize-photo` worktree — counterfactual branch.
- `experiment/photo-progress-correct` worktree — correct-branch evolution.
- `experiment/photo-progress-commonized` worktree — commonized-branch evolution.
- `analysis/classifications/photo-progress-correct.yaml` + `photo-progress-commonized.yaml` — ripple classifications for `feature_retro.py`.
- `analysis/classifications/photo-correct_critique.md` — qualitative observations on the current correct-scoped code, authored before the counterfactual starts.
- `analysis/classifications/photo-commonized_critique.md` — qualitative observations on the commonized branch, authored during the commonization, grouped under NS-theorem categories.
- `analysis/data/ripple_photo-progress-correct_{files,units}.csv` + commonized counterparts.
- `analysis/data/counterfactual_photo_progress.md` — top-level comparison document: numeric table + critique-observation summary counts + 2–3 verbatim-quoted observations + closing paragraph. §4.3 / §4.6 / §4.7 primary source.
- `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` — 3 new operationalization rows (1 for Part A, 2 for Part B), plus the §4.2/§4.9 duality caveat one-liner inserts.

## H. Verification for the addendum work

1. **Part A tests green**: `python -m pytest analysis/tests/test_layer_divergence.py -v` (4 tests).
2. **Part A figure renders**: `python analysis/figures.py` produces `layer_divergence.csv` + PDF without errors; stacked bar reads sensibly (no layer > 100% platform-specific, no layer < 0%, divergent module counts are non-negative).
3. **Part A idempotence**: two runs produce byte-identical CSV + PDF.
4. **Counterfactual compiles**: `./gradlew check` passes on `experiment/commonize-photo` (or, if `archCheck` fails, the failure is documented as part of the measurement, not worked around).
5. **Evolution branches compile**: `./gradlew check` passes on both `experiment/photo-progress-correct` and `experiment/photo-progress-commonized`.
6. **Comparison table plausibility**: `L1 ≤ L2` (correct branch rebuilds ≤ commonized branch); `B1 ≤ B2`; `files_1 ≤ files_2`. A violation of any of these would be a strong signal that the counterfactual is measuring something different from what it claims.
7. **Ratio sanity**: `B2/B1` is in the range `[2×, 20×]`. Below 2×, the thesis argument is weaker than expected and §4.7 must discuss why. Above 20×, verify the commonized branch wasn't straw-manned.
8. **Headline number printed**: `feature_retro.py --finalize` on the commonized evolution prints the ripple-bucket summary; the comparison script prints the delta ratio; both go into §4.7.
9. **Critique checklist present**: `analysis/classifications/photo-commonized_critique.md` exists on the counterfactual branch with ≥5 observations, each citing a file:line and an NS-theorem category. Empty or single-category critique = insufficient — the counterfactual should not be able to "pass" without the qualitative half committed.
10. **Critique asymmetry honest**: `photo-correct_critique.md` exists on the correct branch with ≥3 positive observations (compile-time guarantees, parallel evolvability, cognitive simplicity). A critique file for one branch without the other would be biased; both must exist.
11. **Thesis compiles**: `latexmk -pdf text.tex` in `~/Ctu/dp-thesis-timotej-adamec/text/` after the operationalization table edits.

## I. Out of scope for this addendum

- LOC-per-leaf-platform / reach-coefficient metric — discussed and rejected; see §E point 7.
- Consumer source-set distribution per producer (an earlier alternative metric considered) — would require KMP source-set hierarchy parsing in Python and only supports the same descriptive claim as Part A with more plumbing. Not worth the complexity if the counterfactual is doing the load-bearing work.
- Replacing `blast_radius_module` — kept as-is for §4.3 annotation.
- §4.2 / §4.3 / §4.6 / §4.7 / §4.9 Czech prose — Phase 5.
- Merging the counterfactual or evolution branches into main — they are experimental artifacts.
- A third replicate flow beyond AddFindingPhoto and AddProjectPhoto — two is sufficient for a pattern; three becomes disproportionate effort.
- Updating the `feat/findings` production architecture to match any lesson drawn from the counterfactual — the counterfactual tests the current architecture, not a proposal to change it. Any improvements are a separate conversation, post-thesis.
