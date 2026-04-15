<!--
Copyright (c) 2026 Timotej Adamec
SPDX-License-Identifier: MIT

Thesis: "Multiplatform snagging system with code sharing maximisation"
Czech Technical University in Prague — Faculty of Information Technology
-->

# Case 1b — Counterfactual photo-progress comparison

**Primary §4.3 / §4.6 / §4.7 source for the paired-branch counterfactual.** Phase 5 thesis prose pulls from this file directly.

---

## Context

Snag's frontend has two structurally distinct photo-upload flows: `AddFindingPhoto` and `AddProjectPhoto`. Both currently exist in two platform-specific variants — `NonWebAdd*PhotoUseCase` (returns `OfflineFirstDataResult`, talks to `LocalFileStorage`) and `WebAdd*PhotoUseCase` (returns `OnlineDataResult`, talks to `RemoteFileStorage`). The split is at the application-layer use-case interface, not at the adapter layer, because the offline-first ↔ online-only divergence is type-level and non-negotiable: `OnlineDataResult.Failure.NetworkUnavailable` has no semantic counterpart in `OfflineFirstDataResult`.

To test the architectural claim that "the existing split is load-bearing," we constructed a **paired-branch counterfactual**:

- `experiment/commonize-photo` force-commonizes both flows into shared `commonMain` use cases, hiding the type-level divergence behind a fused `PhotoUploadResult<T>` sealed type and platform-specific `*PhotoStoragePort` adapters in driven impls. The shape is a "plausible less-careful engineering choice" (plan §D line 496) — it compiles, `archCheck` passes, but a senior reviewer would flag the widened `when` blocks with unreachable branches.
- `experiment/photo-progress-correct` applies a realistic web-only evolution (upload-progress callback) to the current `main` correctly-scoped structure.
- `experiment/photo-progress-commonized` applies the **same** evolution on top of the commonized base, measuring the ripple cost when the use-case contract lives in `commonMain` instead of split per platform.

`feature_retro.py` measured the ripple of each evolution branch against its parent. The numeric delta is the load-bearing comparison.

---

## Numeric comparison

| Metric | Correct (`experiment/photo-progress-correct` vs `main`) | Commonized (`experiment/photo-progress-commonized` vs `experiment/commonize-photo`) | Delta | Ratio (commonized/correct) |
|---|---:|---:|---:|---:|
| Files touched | 8 | 12 | +4 | **1.5×** |
| Modules touched (distinct `module_path`) | 6 | 8 | +2 | 1.33× |
| Source-set units touched (distinct `(module, source_set)`) | 8 | 12 | +4 | 1.5× |
| FE leaves on the touched units' recompile cone | ~2 (web-only: js, wasmJs) | ~6 (commonMain[FE] reaches all FE leaves: android, iosX64, iosSimulatorArm64, jvm-desktop, js, wasmJs) | +4 | **3×** |
| Sum of touched units' `blast_radius_module` | 28 | 56 | +28 | **2.0×** |
| Sum of touched units' `blast_radius_unit` | 99 | 187 | +88 | 1.89× |
| LOC churn | 97 | 100 | +3 | 1.03× |
| Intrinsic-recurring file count | 0 | 0 | 0 | n/a |

**Headline ratio** (per plan §D + §H.7): `B2/B1` for `blast_radius_module` = **2.0×**, exactly at the plan's lower threshold.

**Files-touched ratio:** 1.5× — below the 2× threshold. The reason is the `onProgress: (Float) -> Unit = {}` default-arg form: nonWeb VMs *do not have to source-edit* on the commonized branch because the default value works for them. Source-level ripple under-states the true cost; blast-radius (which counts compile-time recompile fan-out, not source-level edits) tells the more honest story. See "Caveats" below.

**Plausibility checks pass** (plan §H.6):
- L1 ≤ L2 (2 ≤ 6) ✓
- B1 ≤ B2 (28 ≤ 56) ✓
- files_1 ≤ files_2 (8 ≤ 12) ✓

---

## Qualitative observations summary

`photo-commonized_critique.md` records **9 observations** across 5 NS-theorem categories on the commonized branch, plus the loss-of-tests data point.

| Category | Observation count | Headline shape |
|---|---:|---|
| **SoC** | 2 | Widened `when` blocks with unreachable variants in 4 VMs (SoC-1); nested cross-vocabulary `when`s in common impls (SoC-2). |
| **DVT** | 2 | Canonical fused sealed type `PhotoUploadResult<T>` (DVT-1); `ProgrammerError` conflates file-system vs network programmer exceptions (DVT-2). |
| **AVT** | 2 | Port signature changes propagate to both platform impls even when only one cares (AVT-1); centralized sync-enqueue at common fan-in node (AVT-2). |
| **ISP** | 1 | Port forces a parameter only one platform uses meaningfully (visible already, exacerbated by Phase 4's progress callback). |
| **Compile-time guarantee loss** | 1 | Platform-reachability invariant moved from type system to inline comments. |
| **Parallel-evolvability loss** | 1 | Web-only evolution forces touches in commonMain + nonWeb files on the commonized branch (quantified above). |

`photo-correct_critique.md` records **7 positive observations** on the current correct-scoped structure (3 per flow + 1 cross-flow), grouped under the same NS-theorem categories from the opposite sign.

---

## 2 verbatim observations exemplifying the cost

### From `photo-commonized_critique.md` SoC-1 — unreachable VM branches:

> Two of the four `PhotoUploadResult` variants (`NetworkUnavailable`, `UserMessageError`) are unreachable on the native offline-first path — the native flow never involves a network. The NonWeb VM's `when` block now contains `is PhotoUploadResult.NetworkUnavailable -> { errorEventsChannel.send(UiError.Unknown) }` and `is PhotoUploadResult.UserMessageError -> { errorEventsChannel.send(UiError.Unknown) }` — two branches that cannot fire at runtime on that platform. The source files contain inline comments (`// Unreachable on native offline-first path...`) documenting this — the comments are themselves evidence of the violation: the type system forced them in.
>
> — `feat/findings/fe/driving/impl/src/nonWebMain/.../NonWebFindingDetailViewModel.kt:73-82` (and 3 sibling sites in the projects flow's NonWeb VM and both web VMs).

### From `photo-commonized_critique.md` Compile-1 — type-level → comment-level invariant:

> On `main`, the invariant "native can't get a `NetworkUnavailable` failure" is expressed **in the type system**: `NonWebAddFindingPhotoUseCase` returns `OfflineFirstDataResult<Uuid>`, and `OfflineFirstDataResult` has no `NetworkUnavailable` variant. On the commonized branch, the same invariant is expressed only in inline comments and a comment-suppressed exhaustive `when` branch. The compiler no longer enforces it; a future refactor could silently remove the "unreachable" comment and start treating those branches as meaningful behavior, with no test or compiler error to catch the semantic drift.
>
> A binary regression. Main = "the compiler forbids it"; commonized = "the developer must remember not to".

---

## Caveats and honest reading

1. **The 1.5× files ratio is below the plan's 2× threshold (§H.7).** Three reasons:
   - The progress callback is a single-parameter widening — the smallest realistic web-only evolution. A heavier evolution (e.g., changing the return-type semantics, adding observability) would compound the cost across more sites and push the ratio higher.
   - `onProgress: (Float) -> Unit = {}` default args minimize source-level ripple — nonWeb VMs source-unchanged because the default value covers them. Without defaults the source ripple would be ~16 files (12 + 4 nonWeb VMs forced to pass the param), giving a 2× files ratio.
   - The default arg is a deliberate honesty in the counterfactual (matches what a real engineer would actually write); the "real" cost is the *blast-radius* fan-out, not the source-edit count.

2. **Blast-radius ratio is 2.0× — exactly at the lower bound.** This is the load-bearing number. It says: even for a single-parameter evolution, the commonized branch forces 2× the modules to recompile compared to the correct branch. The leaf-rebuild ratio is 3× (commonMain[FE] reaches all 6 FE leaves; webMain reaches only js + wasmJs). For larger evolutions that ratio would scale further.

3. **The dependency closure JSON used to compute `blast_radius_*` was generated against the `chore/phase-2-ripple-tooling` branch state, not against the `experiment/commonize-photo` HEAD.** Pre-commonization, `:feat:findings:fe:app:api::commonMain[FE]` had no source files (the api module's `commonMain` source set was empty) — so its blast radius in the chore-branch closure reflects its module-level dependents but not its post-commonization runtime fan-in. This *under-states* the commonized branch's true cost; a regenerated closure on the commonize-photo branch would likely show higher numbers for the new commonMain units.

4. **archCheck PASSES on the commonized branch.** Recorded in `photo-commonized_critique.md` "archCheck outcome" section. The repo's category rules currently do not detect "platform-divergent semantics commonized into shared code" — and writing such a rule would require domain knowledge of which sealed-variant vocabularies are platform-specific. archCheck passing is *evidence that mechanical rules cannot substitute for the type-level invariant the correct branch maintains*, not evidence the commonization is sound.

5. **Test loss: 576 LOC across 4 deleted test files** (NonWeb + Web tests for both flows). The commonized branch deliberately did not add common-test replacements because doing so requires test-infrastructure changes (a `FakeFindingPhotoStoragePort`, restructured Koin fixtures) that a less-careful engineer would skip. This absence is part of the counterfactual cost; a more disciplined engineer would have to pay the 4-test-file rewrite tax to reach test-coverage parity.

---

## Closing — why neither half alone is enough

The numeric delta is real but modest at the lower bound (B2/B1 = 2.0×). A skeptical reader could respond "you commonized poorly, a more skilled engineer would have found a cleaner commonization that makes the ratio look like 1×." This is exactly the rhetorical opening plan §E.3 anticipates. The defense is the qualitative critique: the violations recorded in `photo-commonized_critique.md` are **textbook NS-theorem anomaly shapes**, not accidents of the author's execution.

- A forced sealed `PhotoUploadResult` is a textbook DVT-widening regardless of the engineer.
- An `if-else` or sealed-`when` branching on platform-specific variants in commonMain is a textbook SoC violation regardless of the engineer.
- A port parameter only one impl uses meaningfully is a textbook ISP violation regardless of the engineer.
- Comment-enforced invariants instead of type-enforced ones is a textbook compile-time-guarantee loss regardless of the engineer.

These shapes appear because the commonization is fighting against a non-negotiable type-level semantic boundary (`OnlineDataResult` ≠ `OfflineFirstDataResult`). Any commonization that hides this boundary will exhibit some subset of these shapes; the only commonizations that don't exhibit them are the ones that don't actually commonize the divergence (i.e., they keep the platform split at the place where the divergence lives — exactly what `main` does).

**Therefore §4.6 reads:** the numeric delta + the theory-grounded qualitative critique together establish that Snag's `NonWeb*` / `Web*` use-case split at `feat/findings/fe/app/api/src/{nonWebMain,webMain}/` and `feat/projects/fe/app/api/src/{nonWebMain,webMain}/` is **load-bearing** — its removal would extract a measurable architectural cost (concrete: 2× blast-radius fan-out, 3× leaf-rebuild ratio) AND introduce textbook NS-theorem anomalies. The split is not incidental; the duality-of-readings problem (where any static metric is compatible with both correct and overgeneralized placements) is broken by this paired-branch comparison because both branches were applied to the same evolution and both are honest implementations of their respective design choices.

§4.9 (threats to validity) inherits the caveats above: the ratio's modest size is explained by the chosen evolution's small footprint and the default-arg form; a paragraph there should make these honest.

---

## Provenance

- Subject branches:
  - `experiment/commonize-photo` (HEAD `70b7910cb`) — counterfactual base.
  - `experiment/photo-progress-correct` (HEAD `8fe9039f0`) — correct-branch evolution.
  - `experiment/photo-progress-commonized` (HEAD `a373131fb`) — commonized-branch evolution.
- Base snapshots:
  - `analysis/data/sharing_report_with_loc_base_main_e076e89e5.csv` — reused from Case 1, also used by photo-progress-correct.
  - `analysis/data/sharing_report_with_loc_base_commonize-photo_70b7910cb.csv` — generated for Phase 4 via `./gradlew sharingReport` on the commonize-photo worktree, then enriched with `source_set_dir_rel` via Python (the commonize-photo branch's build-logic predates the chore-branch's wider schema).
- Ripple measurements:
  - `analysis/data/ripple_photo-progress-correct_{files,units}.csv` — `feature_retro.py --finalize` output, Phase 2.
  - `analysis/data/ripple_photo-progress-commonized_{files,units}.csv` — Phase 4.
- Critique files:
  - `analysis/classifications/photo-correct_critique.md` — Phase 1, authored before counterfactual.
  - `analysis/classifications/photo-commonized_critique.md` — Phase 3, authored during commonization.
- Before-snapshot:
  - `analysis/classifications/facts.md` — Phase 1, exact paths + LOC for both flows on main.

All experiment branches retained for reproducibility; never merged.
