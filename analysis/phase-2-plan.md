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
- Fields present per entry: `file, unit, status (A/M/D/R), loc_churn, bucket, recurring, source, reason, ns_theorem (optional)`. The `ns_theorem` field takes one of `SoC | DVT | AVT | SoS | ISP | general` and is populated during hand-classification for every `bucket: intrinsic` entry that has `recurring: true`. It names the NS-theorem anomaly each recurring site represents, so §4.6 can cite the recurring-intrinsic count as theory-grounded evidence rather than a rule-matching tautology. `--finalize` **warns** on missing `ns_theorem` on recurring-intrinsic entries but does not reject — the schema can migrate incrementally without breaking already-finalized case files. A later enforcement pass tightens to reject once all cases have shipped. See Part J of the addendum for the cross-case harmonization that introduced this field.
- Renames collapse to a single entry with `file: "old → new"` and inherit the new location's rule match; `file_count: 95` matches `git show --stat` *after* renames are collapsed — verification check 2 accounts for this.

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

**Revised Phase 2 day budget** (~12 working days, up from the original ~7). The growth reflects (a) Case 1b (counterfactual commonization) added by the addendum, and (b) Case 4 elevation from optional to recommended. Case 4 is now recommended for ship unless Cases 1 + 1b + 2 + 3 are still incomplete at end of day 11.

| Day | Work | Status |
|---|---|---|
| 1–2 | Tooling: `DependencyGraphTask`, `SharingReportRowBuilder` rel-path edit, `dependency_closure.py`, `feature_retro.py`, `ripple_rules.yaml` scaffold, `figures.py` Phase 2 additions, pytest suite. **(Shipped in PR #228.)** | DONE |
| 2.5 | **Addendum Part A (layer-divergence descriptive metric).** `figure_layer_divergence()` + pytest + thesis op row. | DONE |
| 3 | **Case 2 — ProjectPhoto retrospective.** Tooling gate + descriptive illustration (non-probative per reframing). **Gate:** collapsed-rename file count ≡ `git show --stat b5365d611`. | DONE |
| 4–6 | **Case 1 — inspections reverse removal.** Worktree, deletion, repair, classification + critique file. | DONE |
| 7–10 | **Case 1b — counterfactual commonization of AddFindingPhoto.** Preflight + correct-branch evolution + commonization + commonized-branch evolution + critique files + comparison table. (See addendum Part F for the detailed schedule inside this 4-day slot.) | DONE |
| 11 | **Case 3 — DVT (Data Version Transparency) synthetic test on Client.** Half-day implementation, half-day classification + round-trip test. | DONE |
| 12 | **Case 4 — iOS-only Project extension (recommended).** Analogous to Case 3. Skip only if days 3–11 overran. | DONE |

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
8. Hand-classify every `unclassified` unit in `classifications/inspections-reverse-removal.yaml` into local/intrinsic/collateral + recurring flag + reason. Rule-matched entries are pre-populated and only need review. **For every entry where `bucket: intrinsic` and `recurring: true`, set `ns_theorem` to the NS anomaly category the site represents (e.g. `koinModulesAggregate` → SoC, `*SyncHandler.kt` registry → SoC, `DevDataSeeder` → DVT, test-infrastructure enumeration → ISP). See the Part J cross-case guide in the addendum for the labeling conventions.**
9. `python analysis/feature_retro.py --change inspections-reverse-removal --ref experiment/remove-inspections --finalize`
10. **Author the critique file.** Write `analysis/classifications/inspections-reverse-removal_critique.md` by synthesizing every `recurring: true, bucket: intrinsic` entry from the finalized yaml into a prose observation grouped under NS-theorem headings (SoC / DVT / AVT / SoS / ISP / general). Same format as Case 1b's `photo-commonized_critique.md` in the addendum: each entry cites file:line, names the theorem, gives a one-sentence description. Minimum 5 observations; if the yaml has fewer than 5 recurring-intrinsic entries, include non-recurring intrinsic entries that are nevertheless theory-named (e.g. an `archCheck`-surface violation the repair created). This file is §4.6's primary source for Case 1.
11. **Branch is never merged.** Worktree is kept for reproducibility until Phase 5 writeup completes, then removed.

### Case 2 — Secondary: ProjectPhoto retrospective (commit `b5365d611`) — DESCRIPTIVE, NON-PROBATIVE

From scouting: 95 files, 4183 insertions. The rough shape is dominated by intra-feature extension within `feat/projects/*`, with schema-registry touches in `feat/shared/database/*` and a minor refactor in `feat/findings/*`.

**Purpose of Case 2 is strictly two-fold** and the thesis prose in §4.3 must say this out loud so Case 2 is not misread as a correctness claim:

1. **Tooling gate / dry run.** Before Case 1 runs against a worktree with active repair work, `feature_retro.py` is validated end-to-end on a historical commit where the diff is already known. The gate is collapsed-rename file count ≡ `git show --stat b5365d611`. Mismatch = longest-prefix unit mapping bug; fix before Case 1.
2. **Descriptive illustration.** §4.3 cites the resulting ripple decomposition as a concrete example of what a real feature addition in Snag actually touched — useful for the reader's intuition about the shape of evolvability in practice. **It is not probative.** Per the Part A methodological note in the addendum, any distribution is compatible with multiple architectural realities; Case 2 shows what *did* happen, not whether what happened was correct. The counterfactual Case 1b is where correctness is argued.

No expected-shape prediction is committed before running, to avoid confirmation bias. Whatever distribution falls out is reported honestly.

**Procedure:**
1. `python analysis/feature_retro.py --change projectphoto-forward --ref b5365d611 --base-ref b5365d611~1 --stub`
2. Hand-classify in `classifications/projectphoto-forward.yaml`. For every `bucket: intrinsic` entry with `recurring: true`, set `ns_theorem` per Part J conventions (schema registry sites → DVT, feature-registration aggregation → SoC, etc.).
3. `python analysis/feature_retro.py --change projectphoto-forward --ref b5365d611 --base-ref b5365d611~1 --finalize`
4. **Author the critique file.** Write `analysis/classifications/projectphoto-forward_critique.md` with a prose observation list grouped under NS-theorem headings, same format as Case 1b's `photo-commonized_critique.md`. Minimum 3 observations (Case 2 is descriptive; 3 named anomaly sites is enough to illustrate the shape). Each cites file:line on commit `b5365d611`. This file is §4.3's descriptive-illustration primary source.
5. **Explicit method note in the yaml commit message:** "Case 2 is a tooling gate + descriptive illustration; it is not cited as correctness evidence. See Part A of the phase-2-plan.md addendum for the methodological framing."

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
6. `python analysis/feature_retro.py --change dvt-client-field --ref experiment/dvt-client-field --stub` → hand-review → `--finalize`. The §4.6 headline numbers printed to stdout are: `3a non-essential touches = N1` and `3b upward-bleed touches = N2`. During hand-review, set `ns_theorem: DVT` on every recurring-intrinsic entry (Case 3 is DVT-specific by construction); any non-DVT anomaly observed in passing (e.g. an ISP violation in a test fixture) should be labeled with its actual theorem.
7. **Author the critique file.** Translate the locked anomaly taxonomy table into `analysis/classifications/dvt-client-field_critique.md` as a prose observation list, one entry per observed anomaly from 3a and 3b, grouped under DVT sub-headings (mechanism 1 / mechanism 2 / cross-theorem notes) for internal organization and then aligned to the top-level SoC/DVT/AVT/SoS/ISP/general heading structure used across the other critique files. Each entry cites the specific file:line of the actual touch, names the DVT mechanism that failed, and gives a one-sentence description. Same format as Case 1b's `photo-commonized_critique.md`. §4.6 primary source for Case 3.
8. Branch kept, not merged.

### Case 4 — Recommended tertiary: iOS-only Project extension (platform-axis counterfactual)

**Elevated from optional to recommended ship** after the addendum's methodological discussion. Rationale: the thesis evidence for §4.6 comes from three distinct counterfactual axes, and each axis tests a different aspect of "correct scoping" that the others cannot substitute for:

| Counterfactual axis | Case | What it tests |
|---|---|---|
| Hex-layer axis (correct vs commonized placement at the application layer) | Case 1b (addendum) | SoC / DVT / AVT / ISP via forced commonization of AddFindingPhoto |
| Data-version axis (field added, DVT mechanism absorbs or doesn't) | Case 3 (3a + 3b) | DVT mechanisms 1 and 2 |
| Platform axis (platform-specific code added, multiplatform-level containment absorbs or doesn't) | **Case 4** | SoC / DVT on the multiplatform-level dimension |

Skipping Case 4 leaves the platform axis uncovered. It is cheap (~1 day) and methodologically analogous to Case 3, so the cost of shipping it is proportionate to the thesis value it adds. Skip only if Cases 1 + 1b + 2 + 3 are all still incomplete at end of day 7 — otherwise ship.

**Target:** `Project` entity. Introduce an iOS-only field `iosCacheLocalPath: String?` via a new `IosProject` interface in `feat/projects/business/model/src/iosMain/.../` that extends the shared `Project` interface. The field semantically belongs only at the iOS level (iOS file system cache path has no meaning on other platforms).

**Claim under test:** *Zero files in `commonMain` (of any `feat/projects/**` module) should require modification. Any forced `commonMain` touch is a platform-axis anomaly.*

**Procedure:**
1. `git worktree add .claude/worktrees/ios-only-project experiment/ios-only-project-field` off `main`.
2. Commit the stub `classifications/ios-only-project-field.yaml` with the anomaly taxonomy populated **before** writing any code — same discipline as Case 3. Taxonomy:
   - ANY touch in `feat/projects/**/src/commonMain/**` → SoC violation (platform concern leaking into platform-agnostic code). **Critical.**
   - ANY touch in `feat/projects/**/src/nonWebMain/**` or any non-iOS-specific source set → SoC violation (iOS concern leaking into broader multiplatform level). **Critical.**
   - ANY touch outside `feat/projects/**` (e.g. in another feature or in `koinModulesAggregate`) → SoC violation (cross-feature platform coupling). **High.**
   - FE SQLDelight migration for a native-only column on iOS target → **not an anomaly** (schema evolution legitimately requires a migration).
3. Add the iOS-only field: new `IosProject` interface + `IosProjectData` data class in `feat/projects/business/model/src/iosMain/.../`, with a default for the new field. Do not touch the shared `Project.kt` or any other source set initially.
4. Run `./gradlew check --continue`. Fix every failure in one pass. Every fix outside `feat/projects/**/src/iosMain/**` is recorded as an anomaly in the yaml against the locked taxonomy. `archCheck` must pass on the final state or its violation is itself recorded as data.
5. `python analysis/feature_retro.py --change ios-only-project-field --ref experiment/ios-only-project-field --stub` → hand-review → `--finalize`. §4.6 headline number printed to stdout: `commonMain forced touches = N` and `non-iOS-source-set forced touches = M`.
6. During hand-review, set `ns_theorem` on every anomaly entry — primarily SoC (platform concern leak) with DVT as secondary for any field-enumeration sites.
7. **Author the critique file.** Write `analysis/classifications/ios-only-project-field_critique.md` with the observation list grouped under NS-theorem headings (same format as Case 1b's). Each entry cites the file:line forced to change and names why it is an anomaly on the platform axis. Minimum observation count: **all** anomaly touches must be listed — if the count is zero (ideal case), the file still exists with a one-paragraph "zero forced touches observed; architecture contained the iOS-only attribute at the iOS source set as predicted" note. A zero-count critique file is strong evidence; an empty or missing critique file is not.
8. Branch kept, not merged.

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

## J. Cross-case harmonization (verification of original Phase 2 cases against addendum principles)

This section captures the findings of verifying the original Phase 2 cases (1, 2, 3, 4 from the sections above) against the addendum's methodological principles (duality of static metrics, necessity of comparative evidence, requirement for numeric + theory-grounded qualitative observation halves, correction that divergence is legitimate at any hex layer, NS-theorem labeling vocabulary). The verification produced a set of edits to each original case — those edits are already applied in the sections above; this subsection is the durable record of *why* the edits were made and *what* they accomplish.

### J.1 Alignment summary per case

| Case | Duality addressed | Comparative structure | Numeric + qualitative | NS theorem labels | Verdict |
|---|---|---|---|---|---|
| 1 — inspections reverse removal | Weakly (implicit baseline only) | Weak (before/after = coupling, not scoping) | Was pure numeric → **now + critique file** | Was missing → **now required via `ns_theorem` field + critique file** | Aligned after edits; keep |
| 2 — ProjectPhoto retrospective | No (by design, descriptive) | None (by design) | Was pure numeric → **now + critique file** | Was missing → **now required** | Aligned as *explicitly descriptive* after edits; keep, reframe applied |
| 3 — DVT synthetic Client | Yes (theoretical baseline: DVT predicts zero anomalies) | Present (locked anomaly taxonomy) | Already had both (taxonomy table) → **now + critique file for format uniformity** | Implicit → **now explicit DVT labels + cross-theorem notes** | Strongly aligned; minor format harmonization applied |
| 1b — counterfactual commonization | Yes (empirical baseline: correct vs commonized branch) | Present (two branches) | Both halves from the start | Explicit from the start | Addendum-native; no edit needed |
| 4 — iOS-only Project extension | Yes (theoretical baseline: zero commonMain touches) | Present (locked taxonomy) | Was light → **now + critique file** | Was missing → **now required** | **Elevated from optional to recommended; ship** |

All five cases (1, 1b, 2, 3, 4) now satisfy the addendum's methodological requirements. No case was dropped. Cases 1 and 2 needed the most adjustment (qualitative halves were missing); Case 1b was addendum-native; Case 3 was already strongly aligned and needed only format harmonization; Case 4 was well-aligned in spirit but undersold — elevating it provides the platform-axis counterfactual without which §4.6 would be missing one leg of the triangulated evidence.

### J.2 `ns_theorem` field — schema extension

The `classifications/<change>.yaml` schema gains an optional `ns_theorem` field on repair-log entries, documented in the Schema D section above. This subsection expands the labeling conventions.

**When the field is required.** Every repair-log entry with `bucket: intrinsic` and `recurring: true` must have `ns_theorem` set during hand-classification. For non-recurring intrinsic entries and for collateral/local entries, the field is optional but encouraged where the entry names a known NS anomaly.

**Allowed values.** Exactly one of:

- `SoC` — Separation of Concerns. The touched site fuses two or more concerns that the theory says should be separated. Common Snag shapes: `koinModulesAggregate/**` (module identity fused with DI registration), `*SyncHandler.kt` registry (feature identity fused with sync routing), `settings.gradle.kts` (build-graph membership fused with module identity).
- `DVT` — Data Version Transparency. The touched site enumerates field-level knowledge of a data structure that the theory says should be version-transparent. Common Snag shapes: `DevDataSeeder` (seeder enumerates entity fields), FE SQLDelight column migrations that mirror backend fields, `testInfra/**` fixtures that construct entity `Data` classes positionally.
- `AVT` — Action Version Transparency. The touched site forwards a dependency version it should hide. Common Snag shapes: an `api`-scope module dependency that should have been `implementation`; a module that re-exports an internal type because a downstream commonMain consumer needs it; any Gradle edge promoted from `implementation` to `api` to make a commonization compile (expected only in Case 1b).
- `SoS` — Separation of State. The touched site fuses platform-specific state with shared state. Common Snag shapes: a `commonMain` holder lifted from platform-specific scope, a Koin single-scoped provider that hides platform divergence.
- `ISP` — Interface Segregation Principle (not one of the four NS theorems, but a related anomaly category). The touched site is a port or interface that carries methods only one implementation uses meaningfully, or uses default arguments to paper over a "method only web needs" shape. Common Snag shapes: any port that ended up with `fun onProgress(...) = Unit` default implementations.
- `general` — reviewer-judgment anomalies that don't fit the above. Use sparingly; fewer generic labels is better. If more than ~20% of a critique file's observations land in `general`, the labeling discipline is too loose and needs review.

**Labeling in the yaml.** One-line per entry:

```yaml
repair_log:
  - file: "koinModulesAggregate/fe/src/commonMain/.../FrontendModulesAggregate.kt"
    unit: ":koinModulesAggregate:fe::commonMain"
    status: M
    loc_churn: 12
    bucket: intrinsic
    recurring: true
    source: "rule:aggregation_koin"
    reason: "Feature DI registration; every new feature adds a line here"
    ns_theorem: SoC
```

The `reason` field stays free-form; `ns_theorem` is the controlled-vocabulary category. Both are read by the critique-file author during step "Author the critique file" of each case.

**Enforcement.** `feature_retro.py --finalize` emits a warning on every recurring-intrinsic entry missing `ns_theorem`, but does not fail the run. This lets the schema migrate incrementally — finalized yaml files from earlier runs keep working until their next re-finalize pass. After all Phase 2 cases have shipped and are labeled, a follow-up tightening changes the warning to a reject; that tightening is out of scope for Phase 2 itself.

### J.3 Critique file matrix

Every case produces one `<change_id>_critique.md` file. Format is the Part D observation list from the addendum (file:line citation + NS-theorem heading + one-sentence description). Case 1b produces two critique files (one per branch) because it is the only case with a paired correct/counterfactual comparison.

| Case | Critique file path | Authored when | Content source | Minimum observation count |
|---|---|---|---|---|
| 1 | `analysis/classifications/inspections-reverse-removal_critique.md` | After `--finalize` of Case 1 yaml | Synthesis of recurring-intrinsic entries from the yaml | 5 |
| 2 | `analysis/classifications/projectphoto-forward_critique.md` | After `--finalize` of Case 2 yaml | Synthesis of intrinsic entries from the yaml | 3 (descriptive illustration) |
| 1b correct | `analysis/classifications/photo-correct_critique.md` | Before counterfactual starts | Read of `feat/findings/fe/driving/impl/src/{nonWebMain,webMain}/` on `main` | 3 (positive observations) |
| 1b commonized | `analysis/classifications/photo-commonized_critique.md` | During commonization (not after) | Observations as they arise during the forced commonization | 5 |
| 3 | `analysis/classifications/dvt-client-field_critique.md` | After `--finalize` of Case 3 yaml | Translation of locked anomaly taxonomy into prose observations | All observed anomalies; zero-anomaly case states "zero forced touches observed" explicitly |
| 4 | `analysis/classifications/ios-only-project-field_critique.md` | After `--finalize` of Case 4 yaml | All observed commonMain / non-iOS-source-set touches | All observed; zero-count case states "zero forced touches observed" explicitly |

Six critique files total. Format convergence across §4.6 sources is the point: Phase 5 prose can pull from any of them with the same heading structure and citation style, without needing per-case adapters.

**Format anchor.** The Part D format block in the addendum (`### SoC-1: platform branching in commonMain use-case implementation`) is the canonical template. All six critique files use it.

### J.4 §4.7 four-headline coordination

The original Phase 2 plan had one §4.7 headline number: "N recurring intrinsic units." The addendum added another: "delta ratio B2/B1 from the counterfactual." This verification adds two more, producing **four complementary headlines** that together constitute the §4.7 synthesis:

1. **Recurring intrinsic units across Cases 1+2** (combined). *Descriptive combinatorial-effect count.* Shows that the aggregate number of "every feature has to touch this spot" sites in Snag is bounded, not combinatorial. Drawn from the intrinsic-recurring entries in both cases' yaml + critique files.
2. **Case 1b delta ratio (B2/B1, files_2/files_1)** across the counterfactual commonization. *Empirical counterfactual evidence on the hex-layer axis.* Shows that the correct scoping is load-bearing: forcing commonization at the application layer produces a measurable blast-radius multiplier for a realistic evolution step.
3. **Case 3 anomaly counts (3a: N1, 3b: N2)**. *Theoretical counterfactual evidence on the data-version axis.* Shows that DVT mechanisms (layered/level model isolation + default absorption) absorb field additions as the theory predicts, or, if they don't, names the specific absorption failures.
4. **Case 4 forced-commonMain-touches count**. *Theoretical counterfactual evidence on the platform axis.* Shows that the multiplatform-level hierarchy contains iOS-only concerns at the iOS source set as the theory predicts, or names the specific containment failures.

**Triangulation, not redundancy.** Each headline tests a distinct aspect of "correct scoping":

- #1 is descriptive; it answers "is the combinatorial effect bounded in practice?"
- #2 is empirical-comparative on the hex-layer axis; it answers "does the placement at the application layer materially affect evolvability cost?"
- #3 is theory-comparative on the data-version axis; it answers "do DVT mechanisms absorb data changes as predicted?"
- #4 is theory-comparative on the platform axis; it answers "does the multiplatform-level hierarchy contain platform-specific changes as predicted?"

Collapsing any one of these into the others would lose a distinct claim. §4.7 prose presents them in that order — descriptive first to set the scene, then the three counterfactual axes — and closes with a synthesis paragraph noting that agreement across the four headlines is the thesis's strongest available evidence that correct scoping is not incidental.

### J.5 Prose role assignment per §

The verification tightens the §-assignments originally sketched in the addendum. All original cases and Case 1b feed specific sections; nothing is used twice for the same purpose.

**§4.2 — Sharing quantification:**
- Phase 1 sharing heatmap (existing)
- Part A layer-divergence metric (addendum) — descriptive per-layer platform-specific LOC share
- Method note: explicitly label both as *descriptive, non-probative*, with forward reference to §4.3 / §4.9 for correctness argument

**§4.3 — Feature-level evolvability case studies:**
- Case 1 (inspections reverse removal) — primary combinatorial-effect evidence + NS-labeled intrinsic-recurring findings
- Case 2 (ProjectPhoto retrospective) — *descriptive illustration*, explicitly non-probative
- Case 1b (counterfactual commonization) — empirical counterfactual on the hex-layer axis
- Case 4 (iOS-only Project) — theoretical counterfactual on the platform axis
- Ripple decomposition (local/intrinsic/collateral) as the shared format across all four cases

**§4.6 — NS theorems mapped to measured data:**
- **SoC:** Case 1b critique (commonMain branching introduced by commonization), Case 1 critique (koinModulesAggregate, SyncHandler registry), Case 4 critique (any forced commonMain touch)
- **DVT:** Case 3 (primary), Case 1b critique (forced sealed `PhotoUploadResult`), Case 2 critique (schema registry)
- **AVT:** Case 1b critique (any `implementation` → `api` promotion forced by commonization). If Case 1b produces zero AVT observations, that is a finding — the existing architecture already honored AVT, and the counterfactual could not escape it.
- **SoS:** any state-lifting observations from Case 1b critique. Likely small or zero; if so, note that Case 3's mechanism-1 framing already covered the state axis indirectly.
- **ISP:** Case 1b critique (any port method only one implementation uses meaningfully)
- Critique files are primary sources; yaml classifications are supporting evidence

**§4.7 — Results synthesis:**
- Four headlines from §J.4 above
- Closing synthesis paragraph on triangulation

**§4.9 — Threats to validity:**
- **Duality caveat**, applied explicitly to Cases 1 and 2: their static numbers are compatible with multiple architectural realities and do not by themselves adjudicate correctness
- Cases 1b, 3, 4 named as the counterfactual mechanisms that break the duality — and the reader is told that the thesis rests on these three comparative structures agreeing with each other, not on any single one
- Honesty-of-counterfactual caveats: the commonized branch in Case 1b must be a plausible less-careful engineering choice, not a straw man; Cases 3 and 4 use theoretical rather than empirical baselines and the precision of the "zero anomalies expected" claim rests on the thesis's statement of what DVT and multiplatform-level containment guarantee

### J.6 Verification additions for the cross-case work

The addendum's verification section §H already covers Case 1b. This subsection adds the verification items that cover the rest of the cross-case harmonization.

1. **`ns_theorem` field present on every recurring-intrinsic entry.** Across the finalized yaml files for Cases 1, 2, 3, 4, every entry with `bucket: intrinsic` and `recurring: true` has a non-empty `ns_theorem` value from the allowed set. `feature_retro.py --finalize` prints a warning line per missing label; verification passes iff every recurring-intrinsic entry across all cases is labeled.
2. **Six critique files committed.** The matrix in §J.3 lists six file paths. All six must exist on their respective experiment branches (or on the corresponding `experiment/*` worktree for Cases 1, 1b, 3, 4; or on the feature branch that commits Case 2's retrospective classification). Each file has the minimum observation count from the §J.3 matrix and uses the Part D format anchor.
3. **Case 4 ships unless explicitly cancelled.** Elevation from "optional" to "recommended" means the default is ship. Cancellation requires an explicit note in the PR B description naming which of Cases 1 + 1b + 2 + 3 overran and why. Silent omission of Case 4 is not allowed by the revised plan.
4. **Case 2 commit message carries the non-probative note.** The git commit that lands `classifications/projectphoto-forward.yaml` and its critique file must include the sentence "Case 2 is a tooling gate + descriptive illustration; it is not cited as correctness evidence." This forces the reframing into the repo's durable record so a future reader cannot accidentally cite Case 2 as probative.
5. **§J.4 headline numbers printed to stdout.** Each case's `feature_retro.py --finalize` run prints its respective headline line to stdout; the comparison script for Case 1b prints the delta ratio. A Phase 5 prose author can copy the four headlines verbatim from four terminal outputs without re-deriving them.
6. **Critique file SoC/DVT/AVT/SoS/ISP heading coverage.** Across the six critique files taken together, every NS theorem heading (SoC, DVT, AVT, SoS, ISP) appears at least once. If a heading is entirely absent, §4.6 has a gap: either the theorem was not exercised by any case (honest finding to note in §4.9) or the critique files under-reported. Determine which, and document.

### J.7 What this verification did NOT change

For future-me reading this: some things were considered and intentionally kept as-is.

- **Case 1's "recurring intrinsic" methodology stays.** The addendum could have pushed Case 1 to be converted into a full before/after counterfactual, but that would have required a second Case 1 on a different feature (for empirical replication) and doubled its cost. Instead, Case 1 stays a single observational experiment with the recurring-intrinsic headline, and its §4.6 evidence is strengthened by critique-file NS labels rather than by structural replication.
- **Case 2 is not dropped.** Descriptive evidence still has value when it is honest about being descriptive. Case 2 serves both as a tooling gate (catches `feature_retro.py` bugs before they reach Case 1's active repair work) and as §4.3 intuition-builder ("here is what a real feature addition in Snag actually touched").
- **Case 3 stays DVT-focused.** Broadening Case 3 to test SoC/AVT/SoS/ISP in addition to DVT would dilute it. DVT has a specific theoretical baseline (layered/level model isolation + default absorption mechanisms from §Návrh DVT); adding broader theorem tests would require separate experiments with separate baselines.
- **`blast_radius_module` stays the file-annotation metric in §4.3.** Kept as-is for its original purpose. The addendum and this verification did not replace it.
- **Phase 5 Czech prose remains out of scope for Phase 2.** All the §4.3 / §4.6 / §4.7 / §4.9 assignments in this addendum + verification are structural (which case feeds which section), not prose. Writing the Czech is the Phase 5 job and has its own plan.

---

## K. Pending thesis-repo edits (rollup checklist)

**Scope.** This section is a single-page index of every edit Phase 2 work has accumulated against `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` that has not yet been made. Phase 5 prose is **not** in this list (out of scope). What is in this list: mechanical LaTeX edits — operationalization-table rows and one-sentence caveat inserts — whose content is fully decided in Phase 2 but whose landing is deferred so thesis-repo commits stay bundled and small.

Whenever a Phase 2 part ships in this repo, this section should be updated: either the item moves to "done" (if the thesis edit was made in the same session) or it stays pending with the source-of-truth pointer updated. The point is that a Phase 5 author opening this plan can find every pending thesis edit in one place without grepping across §A–§J.

### K.1 Pending edits

All fifteen T-rows shipped in thesis-repo commit `99afdce` on branch `feat/phase2-vyhodnoceni-draft` (MR !65). Kept here as a ledger — per §K.3 protocol no row is silently dropped.

| ID | §-target | What was inserted | Source of truth | Status |
|---|---|---|---|---|
| **T-1** | §4.1 `tab:eval-operacionalizace` | O1 row: "Podíl platformně-specifického LOC podle vrstvy hexagonální architektury (deskriptivní, nevyvracející)" — tooling cite `figures.py::figure_layer_divergence` — result cell references `analysis/data/layer_divergence.csv` + `fig_4_2_layer_divergence.pdf`. | `phase-2-plan.md` §C lines 433–444 | **Done** in `99afdce`. |
| **T-2** | §4.1 `tab:eval-operacionalizace` | O2a row: Part B correct-branch ripple bucket counts — `8 souborů / 97 LOC / vše local`. | `phase-2-plan.md` §F + §G + `ripple_photo-progress-correct_units.csv` | **Done** in `99afdce`. |
| **T-3** | §4.1 `tab:eval-operacionalizace` | O2b row: Part B commonized-branch ripple bucket counts — `12 souborů / 100 LOC / vše local`; $B_2/B_1 = 2{,}0\times$ blast radius na modul, $3\times$ přeložených FE listů. | `ripple_photo-progress-commonized_units.csv` + `counterfactual_photo_progress.md` | **Done** in `99afdce`. |
| **T-4** | §4.2 introduction | One-sentence method note appended to first paragraph (structure descriptive; correctness argued by §4.6 + §4.9). | `phase-2-plan.md` §E + §J.5 | **Done** in `99afdce`. Czech phrasing landed as placeholder; Phase 5 prose author may refine. |
| **T-5** | §4.9 threats-to-validity | One-sentence duality caveat appended to paired-counterexample paragraph (share ratios describe structure; §4.6 counterexample argues correctness). | `phase-2-plan.md` §J.5 + §E | **Done** in `99afdce`. Czech placeholder. |
| **T-6** | §4.6 "Případová studie 1 — reverzní odstranění feat/inspections" (primary source) | New `\subsection{}` in §4.6 summarising the critique file: local/intrinsic/collateral = 89/7/43, churn 6071, six NS-theorem-grouped observations (3 SoC + 1 DVT + 1 ISP + 1 contrast). | `inspections-reverse-removal_critique.md` | **Done** in `99afdce`. Full Phase 5 prose refinement remains a Phase 5 task. |
| **T-7** | §4.6 headline number | `N = 6 rekurentních intrinsických jednotek (4 distinct NS anomálních míst)` inline in T-6 subsection. | `--finalize` stdout + `ripple_inspections-reverse-removal_units.csv` | **Done** in `99afdce`. |
| **T-8** | §4.7 cross-case roll-up table | `tab:eval-cross-case-rollup` now lists all 5 cases. See T-15 for per-case cell data. Superseded by T-15. | See T-15 | **Done** in `99afdce`. |
| **T-9** | §4.1 `tab:eval-operacionalizace` | Existing reverse-removal row rewritten with headline result cell: `6 rek. intrinsic (4 distinct sites); L/I/C = 89/7/43; churn 6071`. | `ripple_inspections-reverse-removal_units.csv` + Case 1 critique | **Done** in `99afdce`. |
| **T-10** | §4.6 methodological caveat one-liner | Sentence appended to "Toto mapování je interpretační pomůcka" paragraph (Case 1 claim bounded to named NS sites, not architectural optimality). | `inspections-reverse-removal_critique.md` method note | **Done** in `99afdce`. |
| **T-11** | §4.1 `tab:eval-operacionalizace` | DVT row rewritten: branch `experiment/dvt-synthetic` → `experiment/dvt-client-field`, result cell `0 anomálií (3a/3b/mech. 2 = 0); 1 rek. intrinsic (seedTestClient); L/I/C = 16/1/0; churn 102`. Also corrected in reproducibility lstlisting. | `dvt-client-field_critique.md` + `ripple_dvt-client-field_units.csv` | **Done** in `99afdce`. |
| **T-12** | §4.1 `tab:eval-operacionalizace` | iOS row rewritten: branch `experiment/ios-only-extension` → `experiment/ios-only-project-field`, result cell `0 anomálií (commonMain/non-iOS/cross-feature = 0); 1 rek. intrinsic (:root::settings); L/I/C = 2/1/0; churn 24`. Also corrected in reproducibility lstlisting. | `ios-only-project-field_critique.md` + `ripple_ios-only-project-field_units.csv` | **Done** in `99afdce`. |
| **T-13** | §4.6 "Případová studie 3 — DVT na entitě Client" (primary source) | New `\subsection{}` summarising Case 3: both DVT mechanisms confirmed (inheritance downward propagation + defaults absorption; zero upward bleed of BE-only `adminNote`). | `dvt-client-field_critique.md` | **Done** in `99afdce`. Phase 5 may refine Czech prose. |
| **T-14** | §4.6 "Případová studie 4 — iOS-only rozšíření entity Project" (primary source) | New `\subsection{}` summarising Case 4: iOS-only `widgetPinned` contained entirely at `iosMain` of new `feat/projects/fe/app/model/` module; zero forced touches elsewhere. | `ios-only-project-field_critique.md` | **Done** in `99afdce`. Phase 5 may refine Czech prose. |
| **T-15** | §4.7 cross-case roll-up table | `tab:eval-cross-case-rollup` landed with all five cases: P1 `6/43/89/6071`, P1b correct `0/0/8/97`, P1b commonized `0/0/12/100` ($B_2/B_1 = 2{,}0\times$), P2 `1/4/90/4428`, P3 `1/0/16/102`, P4 `1/0/2/24`. | Five `ripple_*_units.csv` + `counterfactual_photo_progress.md` | **Done** in `99afdce`. |

### K.2 Already captured (no thesis-repo action needed)

These items are permanently captured in Phase 2 repo artifacts and Phase 5 prose can pull from them directly without any intermediate thesis-repo edit:

- **Part A data + figure.** `analysis/data/layer_divergence.csv`, `analysis/figures/fig_4_2_layer_divergence.pdf/png` — shipped in commit `6905cca15`. §4.2 table + figure pull from here.
- **Case 2 §4.3 primary source.** `analysis/classifications/projectphoto-forward_critique.md` — shipped in commit `550fd426b`. Four observations, NS-theorem-headed.
- **Case 2 §4.6 DVT citation.** Inside the Case 2 critique file, Obs 1 cites `AllTables.kt:24` as the schema-registry DVT site. §4.6 DVT bullet (per §J.5 line 777) uses this observation.
- **Case 2 §4.7 headline** (`recurring intrinsic units = 1`). Captured in commit `550fd426b` message, critique file, and reproducible any time via `python analysis/feature_retro.py --change projectphoto-forward --base-ref b5365d611~1 --ref b5365d611 --finalize` stdout. §4.7 prose copies the number verbatim.
- **Case 2 non-probative audit sentence.** Commit `550fd426b` message contains the exact sentence required by §J.6 item 4: "Case 2 is a tooling gate + descriptive illustration; it is not cited as correctness evidence." The repo's durable record carries the reframing so a future reader cannot accidentally cite Case 2 as probative.
- **§4.3 / §4.6 / §4.7 / §4.9 artifact-to-section map.** `phase-2-plan.md` §J.5 lines 763–790 is the definitive map of which repo artifact feeds which thesis section. Phase 5 author reads §J.5 first, then pulls from the listed artifacts.
- **Case 1 §4.6 primary source.** `analysis/classifications/inspections-reverse-removal_critique.md` — shipped alongside Case 1 yaml + CSVs on branch `chore/phase-2-ripple-tooling`. Six NS-theorem-grouped observations, correctness-evidence method note, headline numbers block. §4.6 prose (T-6 above) pulls directly from this file.
- **Case 1 §4.6 headline.** `recurring intrinsic units = 6` (4 distinct NS anomaly sites after collapsing the build.gradle.kts path→unit artifact). Captured in the critique file, in the Case 1 commit message, and reproducible via `python analysis/feature_retro.py --change inspections-reverse-removal --ref experiment/remove-inspections --base-ref main --base-snapshot analysis/data/sharing_report_with_loc_base_main_e076e89e5.csv --change-kind feature_remove --finalize` stdout.
- **Case 1 experiment branch.** `experiment/remove-inspections` on the main Snag repo — never merged, retained for reproducibility until Phase 5 writeup completes. Worktree at `.claude/worktrees/remove-inspections`. HEAD `dbb86f525`.
- **Case 1 base-ref snapshot.** `analysis/data/sharing_report_with_loc_base_main_e076e89e5.csv` — captured once (copy of the pre-Case-1 state of `sharing_report_with_loc.csv`) so `feature_retro.py` can resolve deleted `feat/inspections/**` paths against it on every future rerun.
- **commonMain platform-reach disambiguation in ripple artifacts.** `feature_retro.py` annotates `commonMain` as `commonMain[FE+BE]` (full-platform reach, compiles to the backend JVM server) or `commonMain[FE]` (frontend-only KMP reach) based on the sharing report's `platform_set` column; Case 1 + Case 2 yamls, CSVs, and critiques are backfilled. §4.7 cross-case table cites the annotated unit IDs so BE↔FE boundary crossings are explicit in the evidence.
- **Case 1b §4.3 / §4.6 / §4.7 primary source.** `analysis/data/counterfactual_photo_progress.md` — paired-branch counterfactual on the `AddFindingPhoto` + `AddProjectPhoto` flows. Numeric comparison table (correct vs commonized) + observation-count summary + 2 verbatim quotes + closing paragraph on duality-of-readings. Headline ratio: `B2/B1 = 2.0×` for `blast_radius_module` (at the plan §H.7 lower bound), `3×` for FE leaves rebuilt. Caveats section explains the modest ratio honestly (single-param evolution + default-arg form).
- **Case 1b qualitative critiques.** `analysis/classifications/photo-correct_critique.md` (7 positive observations on main's correct-scoped split, authored before counterfactual) + `analysis/classifications/photo-commonized_critique.md` (9 NS-theorem-grouped violation observations on the commonized branch, authored during commonization). archCheck-passes-but-reviewer-would-flag outcome explicitly recorded (plan §D.4 + §H.4).
- **Case 1b before-snapshot.** `analysis/classifications/facts.md` — exact paths + LOC for both flows on `main` @ `e076e89e5` (24 split files across nonWebMain/webMain source sets, 1715 LOC total). Reference for any future "before commonization" claim.
- **Case 1b experiment branches.** Three branches retained for reproducibility, never merged: `experiment/commonize-photo` (HEAD `70b7910cb`), `experiment/photo-progress-correct` (HEAD `8fe9039f0`), `experiment/photo-progress-commonized` (HEAD `a373131fb`). Worktrees under `.claude/worktrees/`.
- **Case 1b ripple artifacts.** `analysis/data/ripple_photo-progress-correct_{files,units}.csv` (8 files / 97 LOC, all local) + `analysis/data/ripple_photo-progress-commonized_{files,units}.csv` (12 files / 100 LOC, all local). Reproducible via `python analysis/feature_retro.py --change <name> --ref <branch> --base-ref <parent> --base-snapshot <snapshot> --change-kind feature_add --finalize`.
- **Case 1b base snapshot.** `analysis/data/sharing_report_with_loc_base_commonize-photo_70b7910cb.csv` — generated via `./gradlew sharingReport` on the commonize-photo worktree, then enriched with `source_set_dir_rel` + zero-padded LOC columns via inline Python (the commonize-photo branch's build-logic predates the chore-branch's wider schema; enrichment script is one-shot, not committed).

- **Case 3 DVT synthetic test.** `analysis/classifications/dvt-client-field.yaml` — anomaly taxonomy locked before experiment, 17 entries hand-classified. `analysis/classifications/dvt-client-field_critique.md` — zero-count critique file with DVT-0 + DVT-1 + derivative SoC-0 observations, honesty claim covering both DVT mechanisms. `analysis/data/ripple_dvt-client-field_{files,units}.csv`. Headline: **0 anomalies** (3a non-essential touches = 0, 3b upward-bleed touches = 0, forced caller edits = 0), 1 recurring intrinsic unit (test fixture `seedTestClient`), files local/intrinsic/collateral = 16/1/0, churn 98/4/0 = 102 LOC. Experiment branch `experiment/dvt-client-field`. Both DVT mechanisms confirmed: (1) inheritance carries shared `ico` field through `Client → AppClient → BackendClient` without redeclaration at intermediate interfaces; (2) defaults absorb at all existing construction sites (zero caller edits). BE-only `adminNote` has zero upward bleed into business/model, app/model, contract, or FE layers.

- **Case 4 iOS-only platform extension.** `analysis/classifications/ios-only-project-field.yaml` — anomaly taxonomy locked before experiment, 3 entries hand-classified. `analysis/classifications/ios-only-project-field_critique.md` — zero-count critique file with SoC-0 observation + honesty claim. `analysis/data/ripple_ios-only-project-field_{files,units}.csv`. Headline: **0 anomalies** (commonMain forced touches = 0, non-iOS-source-set forced touches = 0, cross-feature forced touches = 0), 1 recurring intrinsic unit (`:root::settings` — settings.gradle.kts include line). Experiment branch `experiment/ios-only-project-field`. Architecture contained the iOS-only `widgetPinned: Boolean` attribute entirely in a new FE-specific `feat/projects/fe/app/model/` module (mirroring the existing `be/app/model/` pattern) with one file in `iosMain`. Convention-plugin auto-wire fallthrough handled the `AppProject` dependency without explicit declaration. `platform_extend` added to `feature_retro.py` CHANGE_KINDS for semantic correctness.

- **Phase 2 mechanical thesis commit.** `analysis/data-for-thesis/phase-2-mechanical-edit-plan.md` — drop-in playbook describing every §K.1 T-row's landing target + verbatim content. Actually executed in thesis-repo commit `99afdce` on `feat/phase2-vyhodnoceni-draft` (MR !65): §4.1 operationalization table gained O1 layer-divergence row, O2a/O2b photo-progress rows, and headline-number fills for Case 1 / Case 2 / Case 3 / Case 4 O2 rows (branch-name corrections included); §4.2 intro gained duality method-note; §4.6 gained T-10 caveat, three new `\subsection{}` blocks for Cases 1 / 3 / 4, and a Case 1b comparison table + commonized-critique anchor quote replacing the existing `\todo{}`; §4.7 gained cross-case roll-up table (`tab:eval-cross-case-rollup`) covering all five cases; §4.9 gained duality caveat. Thesis `lualatex` build verified: 176 pages clean, no errors. Czech one-sentence caveats (T-4/T-5/T-10) landed as placeholders; Phase 5 prose author owns the final wording.

### K.3 Update protocol

Every time a Phase 2 part ships in this repo:

1. Check §K.1: does this part unblock any pending thesis edit? If yes, either (a) make the edit now in one thesis-repo commit and move the row to §K.2, or (b) leave it pending and update the "Source of truth" column if the spec has moved.
2. Check §K.2: does this part produce a new already-captured artifact Phase 5 will need? Add a row.
3. Never silently drop a §K.1 item. If a pending edit is no longer wanted (e.g. the op row becomes obsolete), delete the row with a one-line reason on the same commit.

### K.4 Planned thesis-repo commits

Current plan is **one bundled commit** in `~/Ctu/dp-thesis-timotej-adamec/` once Part B ships:

```
[J:MEP-0] §4.1 Phase 2 operationalization rows + §4.2/§4.6/§4.9 caveats

- §4.1 tab:eval-operacionalizace: add O1 (Part A), O2a (Part B correct),
  O2b (Part B commonized), O3 (Case 1 ripple) rows with tooling + dataset
  citations.
- §4.2 introduction: add one-sentence method note framing per-layer
  metrics as descriptive, forward-ref to §4.3 / §4.9.
- §4.6 introduction: add one-sentence method note pointing to the
  Case 1 critique file as the correctness-evidence source and stating
  the "combinatorial touches are bounded" framing verbatim.
- §4.9 threats-to-validity: add one-sentence duality caveat.

Content decided in Phase 2 (see analysis/phase-2-plan.md §K of the
Snag repo on branch chore/phase-2-ripple-tooling). This commit is
mechanical LaTeX insertion; Czech phrasing for the one-liners is the
Phase 5 prose author's final judgment call.
```

This commit runs after Part B completes and before Phase 2 ships its final PR. If Part B gets delayed past Phase 2 PR merge, split: land T-1, T-4, T-5, T-9, T-10 as one commit (Part A + Case 1 + caveats), and T-2, T-3 as a second commit when Part B data exists. T-6, T-7, T-8 are Phase 5 prose edits (not mechanical inserts) and stay out of this bundle.
