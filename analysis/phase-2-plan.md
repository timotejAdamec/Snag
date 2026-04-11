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
