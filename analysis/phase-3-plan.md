# Phase 3 — Wear OS live experiment (thesis §4.4)

## Context

`analysis/phase-2-plan.md` §L.2 lists four unchecked items that gate the thesis `% TODO` at `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` L3247 for chapter 4.4:

1. Feasibility spike writeup (spike itself done on `experiment/wearos-feasibility-spike`; logged in `analysis/wearos_experiment.md`).
2. One Wear-native screen wired to shared use cases.
3. Ripple decomposition + per-module-tree scaling test.
4. One emulator screenshot (`images/wear-os-*.png` in thesis repo).

The §L.2 rollup says: *"Requires its own plan doc before starting."* — this file is that doc, consistent with §K/§L of `phase-2-plan.md`. Plan doc lives on `chore/phase-2-ripple-tooling`; Phase 3 implementation work lands on `experiment/wearos-feasibility-spike`.

§L.4 gating order places Wear OS work BEFORE the Phase 5 prose pass (blocked on §L.2 artefacts). Completing §L.2 unblocks thesis §4.4 prose + the §4.7 cross-case roll-up row for Wear.

### Question under measurement

**What is the actual ripple footprint, across every layer of the Snag architecture, of adding Wear OS as a live platform target with one user-facing feature (read-only project list) reachable end-to-end including auth?**

This is an open measurement, not a confirmation exercise. The experiment has to be designed so any layer can surface a hit and the thesis reports what actually happened — including outcomes that look bad for the architecture.

**Predictions** (stated with invalidation conditions so the measurement can falsify them):

| # | Prediction | What would falsify it |
|---|---|---|
| P1 | Pure-logic layers (`business/*`, `business/model/*`, `app/*`, `app/model/*`, `feat/*/fe/{app,ports,contract,model}`, `core/*`, pure `lib/*`) compile unchanged for the second Android (Wear) target. | Any compile error on the Wear target originating from these modules that requires editing their source files (not just the Gradle target block). Every such edit is a finding against the prediction. |
| P2 | The per-feature source-set split (commonMain → nonWearMain + commonMain + wearMain) is a pure file-move refactor without semantic edits. | Any file that has to change its implementation (not just path) to survive the split — e.g., because it reached into a Material 3 Foundation API that wasn't obvious until the split forced it out of commonMain. |
| P3 | A new platform target can be authenticated via port substitution alone — one new Wear driven adapter implementing the existing OIDC port, with no change to `feat/authentication/fe/app/*`, `ports/*`, `contract/*`. | Any edit to the authentication port contract, use cases, or domain types to accommodate `RemoteAuthClient`'s semantics. |
| P4 | The `frontendModulesAggregate` Koin graph loads on the Wear Android target without structural change — the only edits are target-variant-aware bindings for the one port that has a Wear-specific adapter. | Any new Koin module required at the aggregate level, any cross-feature rewire, or any shared-module Koin file edited beyond the one-line variant pick. |
| P5 | Database / sync / storage / network adapters currently in `driven/impl/androidMain` either (a) move cleanly to the post-split `commonAndroidMain` so phone + Wear share them, or (b) stay in the post-split `mobileAndroidMain` unchanged — no second Android adapter beyond auth needed. | Any adapter besides auth needing a Wear-specific variant. |
| P6 | The phone app (phone Android target of `:composeApp` + all existing features) continues to build and pass all tests after the split. | Any phone-side regression (test failure, runtime crash on phone emulator smoke-run) introduced by the split. |
| P7 | A developer adding a new platform target must perform a codebase-wide semantic review (per §D6b actions A–J), and this review is **required even for modules where the numeric ripple ends up at 0 LOC**. The review is a qualitative cost not captured by intrinsic/collateral/local bucket counts. | The measurement observation list in the critique md must include observations confirming this: every KMP module that ended up with "0 LOC changed" must still be named as having been reviewed as part of the procedure. If the thesis prose implies "0 LOC = 0 cost" anywhere, P7 is treated as falsified and the prose corrected. |

**Failure modes to watch for** (each one, if observed, is the main finding of §4.4, not a footnote):

- **Hidden phone-only import in a module labelled pure-logic.** E.g., a `feat/*/fe/app/impl` file that imported from `androidx.compose.runtime` in a way that now fails on the second Android target. Unlikely but would falsify P1.
- **Material 3 bleed through a non-Composable helper.** A utility file in the old commonMain that looked Wear-safe but transitively pulls phone Material 3 (e.g., a Color-resolving helper). Falsifies P2 unless the file can be trivially moved to nonWearMain.
- **Port-contract leak for auth.** `RemoteAuthClient`'s callback semantics force the port interface to change (e.g., a new result type, new error category). Falsifies P3 — documents a DVT/AVT weakness in the auth port design.
- **Second Android target confuses the Koin aggregator.** `frontendModulesAggregate` binds a single set of adapters regardless of Android variant and can't naturally pick per-variant implementations. Falsifies P4 — documents a SoC weakness in the aggregator design.
- **A supposedly-shared driven adapter behaves differently on Wear Android runtime.** E.g., `AndroidSettingsTokenStore` uses SharedPreferences in a way that's fine on phone but breaks on Wear (different storage semantics). Falsifies P5.
- **Phone regression from the split.** A file move that the compiler accepted but runtime behaviour broke (resource lookup path changed, Compose preview annotations lost, etc.). Falsifies P6.

The ripple classifier rules and critique doc are designed (D6) to record ANY of these outcomes as first-class findings. No path silently absorbs a surprise hit.

## Design decisions

### D1. Branch & repo layout

- Continue on `experiment/wearos-feasibility-spike`. Do NOT rebase/rename (per `thesis-evaluation-plan.md` guidance — "the branch is the experiment"). It is the data artefact for the ripple tool.
- All Phase 3 work lands on this branch; the branch is kept long-lived (not merged).
- `feature_retro.py --ref experiment/wearos-feasibility-spike --change-kind platform_extend` runs on the final state.

### D2. Source-set topology — the core refactor

**Problem.** Current `:composeApp` and every `:feat/*/fe/driving/impl` module has a single `commonMain` that pulls Compose Multiplatform Material 3 (phone Material + phone Foundation). Wear OS Compose uses `androidx.wear.compose.material3` / `androidx.wear.compose.foundation` — different library, incompatible API. However, Compose UI + Runtime layers ARE shared between phone and Wear. So the current commonMain has two kinds of content tangled together: (a) genuinely cross-target UI primitives / state / VM logic and (b) phone-Material-3-dependent Composables.

**Solution.** Restructure each affected KMP module's source-set graph as a DAG:

```
commonMain
  — truly cross-target: Kotlin logic, Compose UI + Runtime only (no Material3, no phone Foundation)
  — examples: ViewModels, UiState, navigation routes, pure Composables using only UI layer
  │
  ├── nonWearMain
  │     — current commonMain content that depends on phone Material 3 / phone Foundation
  │     — examples: ProjectsScreen (uses Scaffold + TopAppBar), ProjectsContent (uses LazyVerticalGrid)
  │     — inherited by every non-Wear target below
  │     │
  │     ├── iosMain
  │     ├── webMain         (wasmJs / js)
  │     ├── jvmDesktopMain
  │     └── mobileAndroidMain (phone Android; ALSO inherits commonAndroidMain — see below)
  │
  ├── wearMain
  │     — new: Wear-specific Composables using androidx.wear.compose.material3 / foundation
  │     — examples: WearProjectList (ScalingLazyColumn + Chip)
  │     │
  │     └── wearAndroidMain (Wear Android target; ALSO inherits commonAndroidMain)
  │
  └── commonAndroidMain
        — Android-platform APIs that are identical on phone Android and Wear Android
        — examples: a driven adapter using AndroidSettingsTokenStore, any Context-based helper
        — consumed via multi-parent dependsOn by BOTH mobileAndroidMain and wearAndroidMain
```

Multi-parent `dependsOn` wiring (KMP source-set graphs are DAGs, not trees):
- `mobileAndroidMain : dependsOn(nonWearMain), dependsOn(commonAndroidMain)`
- `wearAndroidMain : dependsOn(wearMain), dependsOn(commonAndroidMain)`

KMP target + source-set wiring (per affected module):
```kotlin
kotlin {
    androidTarget("mobileAndroid")         // phone; produces source set `mobileAndroidMain`
    androidTarget("wearAndroid")           // Wear; produces source set `wearAndroidMain`
    iosX64(); iosArm64(); iosSimulatorArm64()
    wasmJs { browser() }                   // if the module is web-reaching
    jvm("jvmDesktop")                      // if the module is desktop-reaching

    sourceSets {
        val commonMain by getting
        // truly cross-target; Compose UI + Runtime only, no Material 3, no phone Foundation

        val nonWearMain by creating {
            dependsOn(commonMain)
        }
        // all non-Wear platforms: phone Material 3 / phone Foundation allowed

        val wearMain by creating {
            dependsOn(commonMain)
        }
        // Wear Compose Material 3 / Wear Foundation allowed

        val commonAndroidMain by creating {
            dependsOn(commonMain)
        }
        // Android-platform APIs identical on phone Android and Wear Android
        // (Context-based helpers, AndroidSettingsTokenStore, etc.)

        // Phone Android inherits BOTH non-Wear UI layer AND shared Android plumbing
        val mobileAndroidMain by getting {
            dependsOn(nonWearMain)
            dependsOn(commonAndroidMain)
        }

        // Wear Android inherits BOTH Wear UI layer AND shared Android plumbing
        val wearAndroidMain by getting {
            dependsOn(wearMain)
            dependsOn(commonAndroidMain)
        }

        val iosMain by getting { dependsOn(nonWearMain) }
        // (applies identically to iosX64Main / iosArm64Main / iosSimulatorArm64Main
        //  if the target-level hierarchy template is not used)

        val wasmJsMain by getting { dependsOn(nonWearMain) }      // or the module's web source set name
        val jvmDesktopMain by getting { dependsOn(nonWearMain) }
    }
}
```

KMP supports multi-parent `dependsOn` — the source-set graph is a DAG, which is what `mobileAndroidMain` and `wearAndroidMain` both need: each draws from a UI-layer parent (`nonWearMain` vs `wearMain`) AND the shared-Android-platform parent (`commonAndroidMain`).

**Which modules get what.** The refactor has two layers: a universal **Android-target-rename layer** (applies to every KMP module with an Android target, for naming consistency) and a scoped **UI-layer split** (applies only where commonMain pulls phone Material 3 / phone Foundation).

**Android-target-rename layer — universal** (every KMP module with an Android target):
- `androidTarget()` → `androidTarget("mobileAndroid")` — the default-named phone target is renamed explicitly. This renames the source set `androidMain` → `mobileAndroidMain`.
- `androidTarget("wearAndroid")` is added — produces `wearAndroidMain`.
- `commonAndroidMain` source set is created and any content currently in `androidMain` that is Wear-compatible (Android-platform-identical) is moved there. Phone-specific content stays in `mobileAndroidMain`.
- Rationale: post-refactor the entire codebase uses one naming dialect. Without the universal rename, we'd have a split codebase (modules with `androidMain` + modules with `mobileAndroidMain`), which is a worse long-term cost than doing the rename once for all.

**UI-layer split — scoped** to modules whose commonMain depends on phone Material 3 / phone Foundation:
- All `:feat/*/fe/driving/impl` modules (projects, findings, structures, inspections, clients, reports, authentication, …) — every driving-impl gets `commonMain` → `nonWearMain` + smaller `commonMain` + new empty `wearMain`. Only projects ships a `wearMain` Composable in Phase 3; the other driving-impls get an empty `wearMain` and surface the recurring per-feature structural cost in the ripple measurement.
- `:composeApp` — same UI-layer split; its `wearMain` holds the Wear App composition root (see D3).

**Per-module outcomes** by category:

- **Driving-impl modules** — both layers applied. Full topology.
- **`:composeApp`** — both layers applied. Full topology.
- **`:feat/authentication/fe/driven/impl`** — Android-target-rename layer only (no UI-layer split, it has no Composables in commonMain). `wearAndroidMain` holds the new RemoteAuthClient adapter; `mobileAndroidMain` keeps the existing phone adapter; `commonAndroidMain` holds anything shared (e.g., `AndroidSettingsTokenStore`).
- **Other driven-impl modules** — Android-target-rename layer only. Current `androidMain` content moves to either `mobileAndroidMain` (phone-only) or `commonAndroidMain` (platform-identical — the common case; storage, database, etc.). Most will end up in `commonAndroidMain`. Any adapter that turns out to differ on Wear (TBD during implementation) gets a `wearAndroidMain` counterpart.
- **Pure-logic KMP modules** (business / business/model / app / app/model / feat/*/fe/app / feat/*/fe/ports / feat/*/fe/contract / feat/*/fe/model / core / pure-lib) — Android-target-rename layer applied only in a lightweight form: `androidTarget()` → `androidTarget("mobileAndroid")` (renaming any `androidMain` dir to `mobileAndroidMain` if content exists; otherwise just the target rename at the build level) and `androidTarget("wearAndroid")` added. No `commonAndroidMain` / `wearAndroidMain` content unless they actually have Android-specific code (most don't). No UI-layer split.

Convention plugins (`MultiplatformModuleSetup.kt`, `FrontendMultiplatformModulePlugin.kt`, `DrivingFrontendMultiplatformModulePlugin.kt`) implement both layers: the base `snagMultiplatformModule` plugin drives the Android-target-rename layer (universal); `snagDrivingFrontendMultiplatformModule` adds the UI-layer split on top. A single build-logic commit applies both; per-module `build.gradle.kts` edits are avoided where possible (plugin drives the topology).

**Convention plugin change.** `DrivingFrontendMultiplatformModulePlugin` and `FrontendMultiplatformModulePlugin` (in `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/`) are updated to define the topology above. The `MultiplatformModuleSetup.kt` helper gains a `configureWearSplit` function that adds the second `androidTarget("wearAndroid")`, defines `nonWearMain` / `wearMain` / `commonAndroidMain`, and wires the `dependsOn` graph. Applying `snagDrivingFrontendMultiplatformModule` automatically produces the split topology. Rollout is one commit that updates the plugin + touches every affected module's `build.gradle.kts` (likely no per-module `build.gradle.kts` edit needed if the plugin does it automatically — that's the preferred path).

**Expected ripple outcome from the refactor alone** (before any Wear screen is added), in prediction form:
- **Every KMP module**: `androidTarget()` renamed to `androidTarget("mobileAndroid")`; second `androidTarget("wearAndroid")` added. Any existing `src/androidMain/` directory renamed on disk to `src/mobileAndroidMain/` (or content moved to `src/commonAndroidMain/` where Wear-compatible). Tracked as git renames/moves.
- **Driving-impl modules + `:composeApp`**: on top of the rename, the UI-layer split — `commonMain` → `nonWearMain` + new smaller `commonMain` + new empty `wearMain`. Pure Kotlin file moves tracked as git renames.
- **`:feat/authentication/fe/driven/impl`**: universal rename + adapter split (Wear adapter new, phone adapter kept, shared plumbing in `commonAndroidMain`).
- **Other driven-impl + pure-logic modules**: universal rename only. Zero semantic edits predicted.

This is the **structural recurring cost** thesis §4.4 reports for "adding a new platform target." The universal rename is a one-time codebase-wide cost amortized across any future platform-target addition; the UI-layer split is a per-feature cost that recurs when new frontend platforms with incompatible UI toolkits are added (future: e.g., a different watch OS).

### D3. `:composeApp` splitting + Wear App composition

`:composeApp` becomes a dual-Android-target KMP module:

- `commonMain` keeps: `AppModule.kt` (Koin setup pattern), `InitializeInitializers` (sync+async initializer barrier — uses pure Compose Runtime), Koin init helper. These are Wear-reusable as-is because they don't touch Material 3.
- `nonWearMain` (current commonMain content moved here): `App.kt` (phone App() Composable), `MainScreen.kt`, `AppScaffold`, `AuthenticationGate` phone variant, `MainBackStack`, `CollapsableTopAppBarScaffold`. All phone-M3-dependent.
- `wearMain` (new): `WearApp.kt` (Wear App composition root), `SnagWearTheme.kt` (bridges `:lib:design/fe` tokens → `androidx.wear.compose.material3.MaterialTheme`), `WearMainScreen.kt` (Wear-native top-level: boots Koin, runs the initializer barrier, displays the authentication gate → project list). Each file documented in the ripple log as intrinsic (per-platform UI composition cost).
- `mobileAndroidMain` (phone): renamed from the current `androidMain`; the `com.android.application` variant here assembles the phone app using `App()` from `nonWearMain`.
- `wearAndroidMain` (new): `WearMainActivity.kt` that calls `setContent { WearApp() }`. Configures the Wear Android manifest via the same convention plugin-driven path.

Phone App() + Wear App() share (via `commonMain`): Koin init, initializer barrier, BackStack state type. They do NOT share Composable render code because Material 3 incompatibility forces re-authoring at the UI layer.

### D4. Auth — OAuth 2.0 PKCE via `RemoteAuthClient`

Per [Google's Wear OS auth guidance](https://developer.android.com/training/wearables/apps/auth-wear) and the [2025 evolution-of-Wear-OS-authentication post](https://android-developers.googleblog.com/2025/08/the-evolution-of-wear-os-authentication.html), the three accepted paths for standalone Wear OS are Credential Manager + passkeys (primary; requires server passkey support Snag doesn't have → out of scope), **OAuth 2.0 PKCE via `RemoteAuthClient`** (fallback; chosen), and Mobile Auth Token Data Layer Sharing (requires companion Wear Data Layer infra Snag doesn't have → out of scope).

Implementation inside the source-set split of `:feat/authentication/fe/driven/impl`:
- **`mobileAndroidMain`** (renamed from current `androidMain`) — keeps existing phone OIDC executor adapter.
- **`wearAndroidMain`** — new Wear OIDC executor adapter using `RemoteAuthClient.sendAuthorizationRequest(OAuthRequest(url=…, codeChallenge=…))`. Callback delivers the code; the adapter completes the PKCE exchange and writes the token to `AndroidSettingsTokenStore` (each Android process has its own SharedPreferences, which is correct: watch auth persists independently of phone).
- **`commonAndroidMain`** — `AndroidSettingsTokenStore` moves here if both phone and Wear adapters use it (likely they do). Shared Android plumbing, platform-variant-agnostic.
- **Koin wiring** — a new `authenticationDrivenWearModule` inside `wearAndroidMain` binds `OidcExecutor` → Wear variant; the phone `authenticationDrivenModule` stays in `mobileAndroidMain`. `FrontendModulesAggregate` picks the right variant per target.
- **Port contract untouched** — this is the **AVT (Action Version Transparency) demonstration** for §4.5. A new adapter substitutes under an existing port; no consumer sees the change.

**Flow.** User taps "Sign in" on watch → `WearAuthenticationGate` (in `:composeApp/wearMain`) invokes the Wear OIDC executor → `RemoteAuthClient` shows "Open phone to continue" → phone browser opens Snag OIDC → PKCE completes → callback returns to watch → token persisted → authentication gate flips → project list displayed.

**Expected ripple.** 1 new intrinsic file (`WearOidcExecutorAdapter.kt`) + 1 new Koin module file (`AuthenticationDrivenWearModule.kt`) + potentially 1 file move (`AndroidSettingsTokenStore.kt` from current `androidMain` to the new `commonAndroidMain`). Port contract files: 0 touches.

### D5. Screen — `WearProjectList` inside the projects feature

`WearProjectList` lives at:
```
feat/projects/fe/driving/impl/src/wearMain/kotlin/
  cz/adamec/timotej/snag/projects/fe/driving/impl/internal/projects/
    WearProjectList.kt
```

NOT in `wearApp/` (per user feedback — platform-specific UI is owned by the feature so the ripple analysis attributes the cost to the feature).

- Consumes the existing shared `ProjectsViewModel` from `commonMain` (it's in the same module, now post-split in the smaller `commonMain` — its file doesn't move; its code depends only on Kotlin + coroutines + Compose Runtime `@Composable state`, not Material 3).
- Body: `ScalingLazyColumn` with one `Chip` per project (name + status). Tapping a chip: out of scope.
- Loading state: Wear `CircularProgressIndicator`. Error state: text + retry `Chip`.
- Scope: read-only list only. No detail, no create, no write. Matches `thesis-evaluation-plan.md` §Phase 3.

**Expected ripple for Phase 3 per-feature count.** `feat/projects/fe/driving/impl/wearMain` = 1 new file (`WearProjectList.kt`). `feat/projects/fe/driving/impl/commonMain` = 0 net new files (ProjectsViewModel already here; it may need to be moved from the new nonWearMain back up to the new commonMain if it's currently in the tangled commonMain — this is a file move, not a semantic change). Zero shared-logic-layer touches.

### D6. Measurement — ripple decomposition + per-module-tree scaling

Deliverables:
- `analysis/data/ripple_wearos-project-list_files.csv`
- `analysis/data/ripple_wearos-project-list_units.csv`
- `analysis/data/ripple_wearos-project-list_by_module_tree.csv` (new scaling chart input)
- `analysis/classifications/wearos-project-list.yaml` (rule-based classifier)
- `analysis/classifications/wearos-project-list_critique.md` (qualitative ≥ 5 observations, each file:line + NS-theorem tag per §J.2 schema)
- `analysis/figures/fig_4_4_wear_ripple_by_module_tree.pdf` (new bar chart in `figures.py`)

Ripple tool invocation:
```
analysis/feature_retro.py \
  --ref experiment/wearos-feasibility-spike \
  --base main \
  --change-kind platform_extend \
  --rules analysis/ripple_rules.yaml \
  --classifications analysis/classifications/wearos-project-list.yaml \
  --out analysis/data/
```

`ripple_rules.yaml` already supports `change_kind: platform_extend` (`feature_retro.py:53` + `apply_rules` at `:316–346`). Rules for this case classify:
- `wearApp/**` → **local** (the platform-app shell — expected per-new-platform cost).
- `**/wearMain/**` new files → **intrinsic** (per-screen Wear UI; recurs per feature × Wear-ported-screen).
- `**/commonAndroidMain/**` moved files (Android code that was single-Android) → **collateral** (one-time refactor cost, does not recur per feature).
- Source-set renames in every driving-impl module (commonMain → nonWearMain) → **collateral** if it's a pure rename with no semantic change, **intrinsic** if the file had to be edited to preserve semantics after the split.
- Convention-plugin changes in `build-logic/` → **collateral** (one-time structural cost).
- Any edit to `commonMain` pure-logic files (VMs, use-cases, ports) → red flag, falsifies the hypothesis → logged explicitly in critique md.
- OIDC manifest placeholder and auth adapter additions → **intrinsic** with `ns_theorem: AVT` per §J.2.

Per-module-tree scaling test: for each top-level tree (`core/`, `lib/`, `feat/`, `app/`, `business/`, `composeApp/`, `wearApp/`, `build-logic/`), count production files with any line changed on the experiment branch. Emit `analysis/data/ripple_wearos-project-list_by_module_tree.csv`. `figures.py` adds a bar chart `fig_4_4_wear_ripple_by_module_tree.pdf` showing the distribution.

**Shape predictions** (bound to the P1–P6 predictions above; any deviation is the finding):
- `business/`, `business/model/`, `app/model/`, `core/`, pure `lib/*`, contract modules → **predicted 0 LOC changed** (tests P1).
- `feat/*/fe/app/`, `feat/*/fe/ports/`, `feat/*/fe/contract/`, `feat/*/fe/model/` → **predicted 0 LOC changed** (tests P1).
- `feat/*/fe/driving/impl/` — directory reorganization + per-module `build.gradle.kts` if the convention plugin doesn't fully drive it. Predicted structural cost only; any semantic edit is a P2 falsifier.
- `feat/projects/fe/driving/impl/wearMain/` → predicted 1 file (`WearProjectList.kt`). Predicted 0 files in other features' `wearMain` (empty Wear dirs after the split).
- `feat/authentication/fe/driven/impl/wearAndroidMain/` → predicted 1 Wear OIDC adapter + 1 Koin module. Port-contract modules predicted 0 LOC (tests P3).
- `:composeApp/wearMain/` → predicted ~3 files (`WearApp`, `SnagWearTheme`, `WearMainScreen`).
- `:composeApp/wearAndroidMain/` → predicted 1 file (`WearMainActivity`).
- `:composeApp/commonMain/` + `nonWearMain/` → predicted pure file moves (0 semantic edits). Any edit is a P2/P6 falsifier.
- `wearApp/` → predicted minimal shell (entry activity, manifest).
- `build-logic/` → predicted convention-plugin edits (MultiplatformModuleSetup, DrivingFrontendMultiplatformModulePlugin, FrontendMultiplatformModulePlugin).
- `koinModulesAggregate/fe/commonMain/` → predicted ≤ 1 file edited for variant-aware binding pick (tests P4); structural additions falsify P4.
- All other `feat/*/fe/driven/impl/` modules → predicted 0 LOC changed OR pure file moves to `commonAndroidMain` (tests P5).

If the measurement deviates from any prediction, §4.4 prose reports the deviation as the headline rather than the prediction. The critique md (D6) is where each deviation is recorded with file:line evidence and the relevant NS-theorem category.

### D6b. Universal new-platform procedure — qualitative findings for the thesis

Thesis §4.4 must document not only ripple numbers but the **procedure a developer has to follow** to add any new platform target (not just Wear OS). This is an architectural-cost story the numbers alone can paint falsely rosy (e.g., "99 % of modules: 0 LOC changed" hides the review labour that produced the 0).

The following actions are **universally required for any new platform target** of Snag's KMP architecture (Android TV, Android Automotive, KaiOS, a future OS, etc.). The Wear OS experiment is where they are first observed and documented, but they recur per platform.

**A. Codebase-wide naming audit.** Every KMP module's Android-target source set gets a semantic name (`mobileAndroidMain`) and a second `wearAndroidMain` target added. Universal rename affects ~N KMP modules (N ≈ order of 100 for Snag). Developer action: confirm the rename compiles module-by-module; no per-module decisions needed. Flaw captured: the naming dialect change is an *ecosystem-wide refactor*, not a per-feature cost, and is invisible in intrinsic/collateral ripple buckets beyond the file-rename count.

**B. Per-module semantic review of commonMain.** For every KMP module whose commonMain contains platform-facing code (Compose UI, Compose Multiplatform helpers, JVM std libs, serialization), a developer must classify each file: "can this compile on the new target?" Classification outcomes: (i) stays in commonMain, (ii) moves to a `nonNewPlatformMain`-style source set, (iii) splits into common + new-platform variants, (iv) uncovered dependency found → falsifies the "this module is pure-logic" assumption. Every module in the repo must be reviewed even when the outcome is "no change". **This is a cognitive cost scaling with codebase size and is invisible in the ripple numbers.** Thesis §4.4 must name this as an architectural cost that the sharing-first architecture does not eliminate.

**C. Per-module Android-target-variant decision.** For every KMP module with Android-specific code, decide per file: does it belong in `mobileAndroidMain`, `commonAndroidMain`, or `wearAndroidMain`? (Driven adapters, manifest entries, resource qualifiers, SDK-version gates.) For new non-Android platforms, the equivalent decision is on platform-target source sets. This is a semantic review with the same cognitive cost as B, scoped to Android-specific code rather than commonMain.

**D. Per-feature driving-impl UI audit.** For every feature's driving-impl module, a developer must audit all Composables in commonMain to classify against the new platform's Compose toolkit (shared UI+Runtime but different Material / Foundation). Any Composable using phone-only Material 3 calls (Scaffold, TopAppBar, LazyVerticalGrid, Navigation components) moves to `nonNewPlatformMain`. This is per-Composable, per-feature — another cognitive cost scaling with feature count × Composable density.

**E. Per-driven-module technology-compatibility audit.** For every driven-impl adapter in the codebase (auth, storage, database, network, files, biometrics, notifications, sync, …), ask: "does this technology have a different API or recommended pattern on the new platform?" Examples: OIDC on Wear → `RemoteAuthClient` (different API); notifications on Wear → different channel semantics; file picker on Wear → unavailable. Most adapters survive unchanged, but EVERY adapter must be reviewed. Another cognitive cost.

**F. Product-axis decision per feature.** After the structural split, every feature's driving-impl has a (possibly empty) new-platform source set. A product decision has to be made per feature whether to ship UI on the new platform. This is outside the architecture but inside the "adding a platform" workstream cost.

**G. Convention-plugin engineering.** One-time engineering cost in `build-logic/` to extend `MultiplatformModuleSetup.kt` + plugins to support the new platform's source-set topology. Amortized across all modules. Recurs per **new platform target class** (e.g., once for Wear, once for TV if TV's topology differs).

**H. Design system adaptation.** For Compose-based new targets, the design tokens in `:lib:design/fe` need to be re-wrapped into the new platform's theme library. One-time per design-system-incompatible platform.

**I. Manifest / build / CI configuration.** Each new-target-specific app module (`wearApp`, `tvApp`, …) needs a manifest, resource qualifiers, app ID, minSdk override, CI job. Per-new-platform, not per-feature.

**J. Emulator / device / QA pipeline setup.** New emulator images in CI; QA device coverage matrix extended. Per-new-platform.

**Thesis writeup structure for §4.4.** Prose covers (in this order):

1. **Numeric ripple table** (intrinsic / collateral / local) from `feature_retro.py`.
2. **Per-module-tree scaling figure** `fig_4_4_wear_ripple_by_module_tree.pdf`.
3. **Procedure narrative** (A–J above, adapted from this plan to Czech) — the qualitative cost of the experiment.
4. **Architectural flaw identification** — explicit paragraph naming "per-module semantic review load" as a cost the sharing-first architecture does not eliminate (cross-refs to §4.9 threats). Related: the naming-dialect refactor (A) would not have been required if the convention plugins had been designed from day 1 to use semantic target names (`mobileAndroidMain`) rather than the KMP default (`androidMain`) — another retrospective architectural observation.
5. **Generalisability claim** — explicit statement that the procedure A–J generalizes to any new platform target; per-target cost factors into "the kind of cost that recurs per new platform" vs "the kind that's amortized once per codebase."
6. **Verdict — explicit result statement.** §4.4 must end with an unambiguous judgement on whether the architecture held up well or not when subjected to the "add a new platform" stress test. The prior going in (author's expectation, recorded here) is **"the result will probably be unflattering to the architecture"** — the numeric ripple may look clean (business/app/ports/contract untouched) but the qualitative costs (A–J) and any prediction failures among P1–P7 expose real architectural weaknesses the sharing-first claim did not address. The verdict section:
   - States the verdict in one sentence (e.g., "the architecture handled X well but failed Y"; or "the refactor exposed a fundamental limitation at Z").
   - Lists concrete architectural flaws the experiment surfaced (invisible-review load per B / C / D / E; naming dialect per A; any prediction falsifications).
   - Lists what the architecture DID handle well (e.g., pure-logic layers compiling unchanged, port substitutability for auth adapter) as a balance.
   - Explicitly refuses to over-claim: avoids phrases like "the architecture proved its sharing-first claim" unless the balance of evidence actually supports that unqualified statement.
   - Cross-references §4.6 (NS theorems) and §4.9 (threats) so the flaws aren't orphaned.

   Writing the verdict honestly — including an unflattering one — is the thesis's defensibility guarantee. A §4.4 that reports only numbers without a verdict is incomplete.

### D7. Emulator screenshot

One Wear OS emulator screenshot: project list populated with ≥ 3 seeded test projects, signed in via the real `RemoteAuthClient` flow with a paired phone emulator. Saved to thesis repo `images/wear-os-project-list.png`; referenced from §4.4 `\includegraphics{...}` replacing `% TODO` at `text/text.tex` L3247.

## Execution order

1. **This plan approved.**
2. **Convention plugin update** — extend `MultiplatformModuleSetup.kt` + `DrivingFrontendMultiplatformModulePlugin.kt` + `FrontendMultiplatformModulePlugin.kt` in `build-logic/` to produce the source-set topology in D2. Unit tests for the new plugin behaviour (plugin tests already exist for `SharingReportRowBuilder` etc.; follow that pattern).
3. **Roll out split to all frontend KMP modules upfront** — single commit; the plugin drives the topology. Move files from the tangled `commonMain` into `nonWearMain` / `commonMain` / `wearMain` per rules: (a) uses phone Material 3 or phone Foundation → `nonWearMain`; (b) uses only Compose UI + Runtime (or pure Kotlin) → stays in `commonMain`; (c) Wear-specific → `wearMain` (empty for most modules in this step). `:composeApp` same.
4. **Verify split with `./gradlew check`** — phone build (all non-Wear targets) still compiles and tests pass. No semantic edits yet. Any compile error indicates a file classified as commonMain that actually used nonWear deps → re-bucket.
5. **Wear OIDC driven adapter** — add `WearOidcExecutorAdapter.kt` + `AuthenticationDrivenWearModule.kt` in `feat/authentication/fe/driven/impl/wearAndroidMain`. Move `AndroidSettingsTokenStore` to `commonAndroidMain` if it's used by both adapters. Unit test the Wear adapter (fake `RemoteAuthClient` → token-store write assertion, runs on JVM).
6. **`:composeApp/wearMain` composition** — `WearApp.kt`, `SnagWearTheme.kt`, `WearMainScreen.kt`. `:composeApp/wearAndroidMain/WearMainActivity.kt`. Manual verification: Wear app launches, shows Authentication gate, runs initializer barrier.
7. **`WearProjectList` in projects feature** — `feat/projects/fe/driving/impl/wearMain/.../WearProjectList.kt`. Wires to `ProjectsViewModel` (already in commonMain after Step 3). Manual verification with seeded data on emulator.
8. **wearApp module wiring** — update `wearApp/build.gradle.kts` to depend on the Wear Android variant of `:composeApp`. Remove any ad-hoc declarations from the spike that are now provided by the new topology. OIDC manifest placeholder handling: whatever the authentication driven module's Wear variant needs (likely same placeholder, since the OIDC provider is the same; placeholder is set either in `wearAndroidMain` manifest or propagated via plugin).
9. **Full `./gradlew check` from repo root** — every error addressed in one pass (CLAUDE.md rule).
10. **Emulator round-trip** — pair phone emulator + Wear emulator, sign in via `RemoteAuthClient`, verify project list. Capture screenshot → `images/wear-os-project-list.png` (thesis repo).
11. **Ripple tool run** — `feature_retro.py --ref experiment/wearos-feasibility-spike --change-kind platform_extend`. Classify files per D6. Write critique md (≥ 5 observations).
12. **Scaling figure** — add Wear bar to `figures.py`; regenerate `fig_4_4_wear_ripple_by_module_tree.pdf`.
13. **Thesis edits** (`feat/phase3-wearos-draft` branch in thesis repo) — fill `% TODO` at `text/text.tex` L3247 with prose + ripple table + scaling figure + screenshot. Appendix CSV `\input{}` linked.
14. **Update `analysis/phase-2-plan.md`** — §L.2 checkboxes flipped; §K.1 row for the §4.4 landing; §L.4 gating step 1 struck through; §L.3 updates so Phase 5 prose knows §4.4 is done.
15. **Phase 3 addendum section** appended to `analysis/wearos_experiment.md` — spike was Phase 0, Phase 3 continuation kept in the same file for audit trail.
16. **Draft PR** — ask user; if yes, on `experiment/wearos-feasibility-spike`, titled per CLAUDE.md (no Jira ticket unless user provides one), description per the PR template.

## Critical files

### Existing (read; modify only as part of the split)

- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/MultiplatformModuleSetup.kt` — extend with `configureWearSplit`.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/FrontendMultiplatformModulePlugin.kt` — apply split.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/DrivingFrontendMultiplatformModulePlugin.kt` — apply split + driving-specific content.
- `composeApp/src/commonMain/kotlin/cz/adamec/timotej/snag/App.kt:36–57` — moves to `:composeApp/nonWearMain`.
- `composeApp/src/commonMain/kotlin/cz/adamec/timotej/snag/AppModule.kt:20–28` — stays in `commonMain` (Koin setup, no Material 3).
- `feat/projects/fe/driving/impl/src/commonMain/kotlin/.../vm/ProjectsViewModel.kt:33–71` — stays in `commonMain` (no Material 3).
- `feat/projects/fe/driving/impl/src/commonMain/kotlin/.../projects/ProjectsScreen.kt` — moves to `nonWearMain`.
- `feat/authentication/fe/driven/impl/src/androidMain/kotlin/.../AuthenticationDrivenModule.android.kt:19–30` — existing path; post-split splits between `mobileAndroidMain` (phone binding) and `commonAndroidMain` (shared plumbing).
- `lib/design/fe/src/commonMain/kotlin/.../theme/SnagTheme.kt:25–37` — tokens stay in commonMain; Wear theme wrapper new in `:composeApp/wearMain`.
- `analysis/ripple_rules.yaml` — add `change_kind: platform_extend` rules for the new path patterns (wearMain, wearAndroidMain, commonAndroidMain).
- `analysis/feature_retro.py` — already supports `--ref` + `--change-kind`; confirm per-module-tree scaling emit or add a `--by-module-tree` flag.
- `analysis/figures.py` — add the new fig_4_4 bar chart function.
- `analysis/wearos_experiment.md` — Phase 3 addendum section.
- `koinModulesAggregate/fe/src/commonMain/kotlin/.../FrontendModulesAggregate.kt:56–99` — may need small edits so the right authentication driven module variant is picked on each Android target; no new aggregate per user's answer.

### New (on `experiment/wearos-feasibility-spike`)

- `:composeApp/wearMain/.../WearApp.kt`, `WearMainScreen.kt`, `SnagWearTheme.kt`
- `:composeApp/wearAndroidMain/.../WearMainActivity.kt`
- `:feat/projects/fe/driving/impl/wearMain/.../WearProjectList.kt`
- `:feat/authentication/fe/driven/impl/wearAndroidMain/.../WearOidcExecutorAdapter.kt`, `AuthenticationDrivenWearModule.kt`
- `:feat/authentication/fe/driven/impl/wearAndroidMain/test/.../WearOidcExecutorAdapterTest.kt`
- `analysis/classifications/wearos-project-list.yaml`
- `analysis/classifications/wearos-project-list_critique.md`
- `analysis/data/ripple_wearos-project-list_files.csv` + `_units.csv` + `_by_module_tree.csv`
- `analysis/figures/fig_4_4_wear_ripple_by_module_tree.pdf`
- Thesis: `images/wear-os-project-list.png`
- Thesis: `text/text.tex` §4.4 prose (replaces `% TODO` at L3247)

## Verification

Phase 3 is **measurable and reportable** (not "successful") when ALL of the following hold. Note: verification is about completeness of measurement, not about predictions being confirmed. Negative findings against P1–P6 satisfy verification equally — they just change what the thesis prose says.

1. `./gradlew check` passes on `experiment/wearos-feasibility-spike` HEAD, for phone + Wear targets. If it does NOT pass because a prediction was falsified in a way that required unfixable layer edits, the branch is still captured with the failing state documented in the critique md; the thesis then reports "the experiment revealed unresolvable coupling at X" as the main finding and `check` is run with the minimum viable scope that produces the branch's measurement.
2. Full diff vs `main` is captured; every change is one of: (a) a pure git rename with no semantic change (expected for `commonMain/*` → `nonWearMain/*` moves, `androidMain/*` → `mobileAndroidMain/*` renames, and `androidMain/*` subset → `commonAndroidMain/*` moves); (b) a new-file addition in an expected new-content directory (`*/wearMain/`, `*/wearAndroidMain/`, `wearApp/`, `build-logic/`, `analysis/`); or (c) a semantic edit — any semantic edit is flagged in the critique md with a P#-reference (P1–P6) and NS-theorem category, regardless of location. No silent semantic edits; `*/commonAndroidMain/` and `*/mobileAndroidMain/` are NOT allow-listed and any semantic edits there are flagged too (since those are predicted to be rename-only).
3. Wear emulator launches `wearApp`, `RemoteAuthClient` sign-in completes on paired phone emulator, project list populates with ≥ 3 seeded projects. Screenshot captured. If any step fails structurally (e.g., RemoteAuthClient cannot bind due to a P3 failure), the failure state is captured as the main finding and the screenshot shows the furthest-reached UI state.
4. `feature_retro.py --ref experiment/wearos-feasibility-spike --change-kind platform_extend` emits the three CSVs. The `wearos-project-list` row is reported whatever its shape — predicted values are NOT required to match; they are the comparison baseline.
5. `analysis/classifications/wearos-project-list_critique.md` exists with ≥ 5 observations, each file:line + NS-theorem category (SoC / DVT / AVT / SoS per §J.2). AT LEAST ONE observation must explicitly address whether each of P1–P7 held or failed on the branch. A separate subsection documents the §D6b actions A–J actually performed (with time estimate or at least a "reviewed-N-modules-for-M-min" figure) so the qualitative-cost evidence is preserved alongside the numeric CSVs.
6. `fig_4_4_wear_ripple_by_module_tree.pdf` regenerated from actual data. The bar-chart interpretation in §4.4 prose matches what the chart actually shows, not what was predicted.
7. Thesis §4.4 `% TODO` at L3247 replaced with prose that covers all six elements of the D6b writeup structure (numeric table, scaling figure, procedure narrative A–J, architectural-flaw identification, generalisability claim, and — critically — the §4.4 **verdict** paragraph stating whether the architecture held up well or failed, listing concrete flaws exposed by the experiment, and cross-referencing §4.6 / §4.9), plus the screenshot. Prose leads with the actual findings, framed honestly. An unflattering verdict is the expected outcome per the author's prior and must be written honestly if that is where the evidence points. `latexmk` builds clean.
8. `analysis/phase-2-plan.md` §L.2 checkboxes flipped; §K.1/§K.2 ledger updated; §L.4 gating order step 1 struck through.
9. `analysis/wearos_experiment.md` gets a Phase 3 addendum appended.
10. `experiment/wearos-feasibility-spike` branch NOT merged into `main` — it remains the data artefact. Kept alive indefinitely.
11. (Optional) Draft PR opened on the branch at user's request.

## Out of scope (explicit)

- Credential Manager / passkey auth (backend doesn't ship passkeys).
- Wearable Data Layer token sharing (separate infra project).
- Project detail / create / edit screens on Wear — scope is the one read-only list screen.
- Adding Wear UI to other features in Phase 3 (only projects is ported; other features just get the structural split, empty `wearMain`). Future Wear ports are per-feature intrinsic cost.
- Refactoring `:lib:app-core/fe`-style shared-scaffolding extraction beyond what the source-set split itself unlocks. Deferred; recorded as §4.9 future work.
- Taking shortcuts that hide ripple — if Wear requires a shared-layer edit, the edit is made AND recorded as a prediction falsification, not avoided. "Commenting out a reference" or similar shortcuts are explicitly forbidden per `thesis-evaluation-plan.md` §Risk 5 (symmetry rule).

Sources:
- [Authentication on wearables: Credential Manager | Wear OS | Android Developers](https://developer.android.com/training/wearables/apps/auth-wear)
- [Android Developers Blog: The evolution of Wear OS authentication (2025)](https://android-developers.googleblog.com/2025/08/the-evolution-of-wear-os-authentication.html)
