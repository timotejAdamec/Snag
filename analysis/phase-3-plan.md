# Phase 3 — Wear OS live experiment (thesis §4.4)

## Context

`analysis/phase-2-plan.md` §L.2 lists four unchecked items that gate the thesis `% TODO` at `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` L3247 for chapter 4.4:

1. Feasibility spike writeup (spike itself done on `experiment/wearos-feasibility-spike`; logged in `analysis/wearos_experiment.md`).
2. One Wear-native screen wired to shared use cases.
3. Ripple decomposition + per-module-tree scaling test.
4. One emulator screenshot (`images/wear-os-*.png` in thesis repo).

The §L.2 rollup says: *"Requires its own plan doc before starting."* — this file is that doc; migrated to `analysis/phase-3-plan.md` on the chore branch (commit 08b4ca037), consistent with §K/§L of `phase-2-plan.md`. This file has since been **revised from its original form** to reflect a Phase 3 pre-registered toolchain finding: AGP 9.0's `com.android.kotlin.multiplatform.library` plugin does not support multiple `androidTarget` per module, so the original source-set-split approach is replaced by a sibling-module split (see D2). The chore-branch `analysis/phase-3-plan.md` will be updated to match before implementation commits land on `experiment/wearos-feasibility-spike`.

§L.4 gating order places Wear OS work BEFORE the Phase 5 prose pass (blocked on §L.2 artefacts). Completing §L.2 unblocks thesis §4.4 prose + the §4.7 cross-case roll-up row for Wear.

### Question under measurement

**What is the actual ripple footprint, across every layer of the Snag architecture, of adding Wear OS as a live platform target with one user-facing feature (read-only project list) reachable end-to-end including auth?**

This is an open measurement, not a confirmation exercise. The experiment has to be designed so any layer can surface a hit and the thesis reports what actually happened — including outcomes that look bad for the architecture.

**Predictions** (stated with invalidation conditions so the measurement can falsify them):

| # | Prediction | What would falsify it |
|---|---|---|
| P1 | Pure-logic layers (`business/*`, `business/model/*`, `app/*`, `app/model/*`, `feat/*/fe/{app,ports,contract,model}`, `core/*`, pure `lib/*`) compile unchanged and are consumed by the new `feat/projects/fe/wear` sibling module + `:wearApp` via existing module dependencies. | Any compile error on the Wear consumer requiring edits to these modules' source files. Every such edit is a finding against the prediction. |
| P2 | Under the revised sibling-module topology, no existing module's source files are edited to accommodate the new platform. | Any edit to files outside `feat/*/fe/wear/`, `:wearApp/`, `build-logic/`, `analysis/` — except visibility-widening to export a VM or helper to the new sibling module, which is a *graded* falsification (logged but less severe than a semantic change). |
| P3 | A new platform target can be authenticated via port substitution alone — one new Wear driven adapter hosted in `:wearApp` implementing the existing `OidcExecutor` port, with no change to `feat/authentication/fe/app/*`, `ports/*`, `contract/*`. | Any edit to the authentication port contract, use cases, or domain types to accommodate `RemoteAuthClient`'s semantics. |
| P4 | Wear Koin composition is contained in `:wearApp` (sibling-module boundary) — `FrontendModulesAggregate` and all other shared Koin modules load unchanged. | Any edit to `FrontendModulesAggregate.kt` or any existing shared Koin module. (Under the original plan this P4 was "aggregate picks variants"; the sibling-module approach sidesteps that question — the finding about the aggregate is recorded either way.) |
| P5 | Database / sync / storage / network driven adapters in `feat/*/fe/driven/impl/androidMain` already compile for the single Android target consumed by both `:composeApp` and `:wearApp`. No second adapter needed beyond the Wear OIDC adapter inside `:wearApp`. | Any driven adapter besides auth either (a) needing a Wear-specific variant duplicated in `:wearApp`, or (b) breaking at runtime on Wear despite compiling. |
| P6 | The phone app (`:composeApp` via `:androidApp`) continues to build and pass all tests after adding the Wear sibling module + expanding `:wearApp`. | Any phone-side regression (test failure, runtime crash on phone emulator smoke-run). |
| P7 | A developer adding a new platform target must perform a codebase-wide semantic review (per §D6b actions A–J), and this review is **required even for modules where the numeric ripple ends up at 0 LOC**. The review is a qualitative cost not captured by intrinsic/collateral/local bucket counts. | The measurement observation list in the critique md must include observations confirming this: every KMP module that ended up with "0 LOC changed" must still be named as having been reviewed as part of the procedure. If the thesis prose implies "0 LOC = 0 cost" anywhere, P7 is treated as falsified and the prose corrected. |

**Failure modes to watch for** (each one, if observed, is the main finding of §4.4, not a footnote):

- **Visibility wall for a shared VM or use case.** `ProjectsViewModel` (or any helper in `feat/projects/fe/driving/impl`) is `internal`, and the new sibling `feat/projects/fe/wear` module cannot reach it without widening the existing code. Expected likely finding — documented as an intrinsic ripple edit, not hidden.
- **Port-contract leak for auth.** `RemoteAuthClient`'s callback semantics force the `OidcExecutor` port interface to change. Falsifies P3 — documents a DVT/AVT weakness in the auth port design.
- **Koin aggregate required for Wear.** `:wearApp`'s own Koin composition turns out to need a binding from inside `FrontendModulesAggregate` that cannot be re-exported without modifying the aggregate. Falsifies P4 — documents an SoC weakness in the aggregator.
- **A shared driven adapter behaves differently on Wear runtime.** E.g., `AndroidSettingsTokenStore` uses SharedPreferences in a way that's fine on phone but breaks on Wear. Falsifies P5.
- **Wear sibling module can't apply an existing convention plugin.** A new `snagWearMultiplatformModule` is required; documents that the existing plugin taxonomy assumes phone-Android-only Android modules.
- **Manifest placeholder mismatch.** The existing `oidcRedirectScheme = "snag"` works on phone but not on Wear (different OIDC provider registration). Documents per-platform config divergence.

The ripple classifier rules and critique doc are designed (D6) to record ANY of these outcomes as first-class findings. No path silently absorbs a surprise hit.

## Design decisions

### D1. Branch & repo layout

- Continue on `experiment/wearos-feasibility-spike`. Do NOT rebase/rename (per `thesis-evaluation-plan.md` guidance — "the branch is the experiment"). It is the data artefact for the ripple tool.
- All Phase 3 work lands on this branch; the branch is kept long-lived (not merged).
- `feature_retro.py --ref experiment/wearos-feasibility-spike --change-kind platform_extend` runs on the final state.

### D2. Module topology — parent + mobile/wear sibling split

**Toolchain finding (Phase 3 start, pre-registered).** The original plan drafted dual `androidTarget("mobileAndroid")` + `androidTarget("wearAndroid")` inside each KMP module with `nonWearMain` / `wearMain` / `commonAndroidMain` intermediate source sets. **This topology is infeasible with Snag's current toolchain: AGP 9.0.1 + `com.android.kotlin.multiplatform.library` (applied via `MultiplatformModulePlugin`) replaces `kotlin.androidTarget { }` with a single-target `kotlin.androidLibrary { }` DSL** (verified against [AGP 9.0 migration docs](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html) + [Android KMP library plugin docs](https://developer.android.com/kotlin/multiplatform/plugin)). Multiple Android targets per KMP module are not supported by this plugin.

This finding is itself an architectural observation reported in §4.4 alongside the ripple measurement: the Snag architecture's Android-target setup (inherited from the standard AGP 9 KMP library plugin) rules out the source-set-level split pattern that would be the natural "add a new Frontend platform" story. The cost has to be paid at a coarser (module-level) boundary.

**Revised solution — parent + mobile/wear sibling modules, mirroring the fe/be pattern.** Per user direction: any module currently holding Android-specific code that is platform-variant-specific gets decomposed into:

- **Parent module** — keeps truly cross-platform content + **shared Android code** (code that runs identically on phone-Android and Wear-Android).
- **`-mobile` sibling module** — holds phone-Android-specific content extracted from the current parent.
- **`-wear` sibling module** — holds Wear-Android-specific content (new).

This is the module-level analogue of the original source-set split (`commonAndroidMain` / `mobileAndroidMain` / `wearAndroidMain`), compatible with AGP 9.0's single-Android-target constraint. The precedent is Snag's existing fe/be split: parent + siblings, not intermediate source sets.

**Applied scope (Phase 3):** Applied per-feature to modules that actually have platform-variant-specific content in their Android code. Other modules stay untouched. **Universal application of the mobile extraction across all features is deferred to a follow-up refactor** — Phase 3's scope is Wear-affected features only (projects + authentication), with the pattern established cleanly so it generalises. This is a pragmatic scoping choice; the thesis §4.4 prose honestly reports "the pattern is applied surgically to the two affected features; full codebase adoption is recorded as future work."

**Per-module outcome matrix (Phase 3 scope):**

**Projects feature:**
- `feat/projects/fe/app/impl`, `ports`, `contract`, `model` — **unchanged**.
- `feat/projects/fe/driving/impl` (current phone UI) — **extract phone Material 3 Composables to a new `feat/projects/fe/driving/mobile` sibling**. Parent keeps: ViewModels (Compose Runtime only), UiState, navigation routes, any truly-cross-target code. Mobile sibling: `ProjectsScreen.kt` + other phone-Material 3 Composables. This is a file-move refactor tracked as git renames; any file that has to be edited (not just moved) is a finding.
- `feat/projects/fe/driving/wear` — **new sibling** containing `WearProjectList.kt` (Wear Compose Material 3). Depends on parent `driving/impl` + `app/impl` + ports/model/contract.
- `feat/projects/fe/driven/impl` — **unchanged** (no Wear-specific driven code needed for projects).

**Authentication feature:**
- `feat/authentication/fe/app/impl`, `ports`, `contract`, `model` — **unchanged**.
- `feat/authentication/fe/driven/impl` (current `androidMain` with phone OIDC adapter + `AndroidSettingsTokenStore`) — **extract phone OIDC adapter to a new `feat/authentication/fe/driven/mobile` sibling**. Parent keeps `AndroidSettingsTokenStore` (shared; both phone + Wear persist tokens identically) + any truly-shared Android plumbing.
- `feat/authentication/fe/driven/wear` — **new sibling** containing `WearOidcExecutorAdapter.kt` (uses `RemoteAuthClient`). Depends on parent `driven/impl` for `AndroidSettingsTokenStore` + auth ports.
- `feat/authentication/fe/driving/impl` — **unchanged** for Phase 3 (no Wear auth UI; handled by a small Wear auth gate in `:wearApp` per D3). If full symmetry is wanted later, driving/impl gets the same mobile/wear split.

**Other features (findings, structures, inspections, clients, reports, …):** unchanged in Phase 3. Their current phone-Material 3 commonMain content stays put; not extracted to mobile siblings this round. Recorded in §4.9 as "to extend Wear to feature X, repeat the mobile+wear split on feature X's driving/impl." This honestly measures the per-Wear-feature cost rather than amortising a universal refactor no one has asked for.

**`:composeApp`:**
- If `:composeApp`'s commonMain mixes phone-Material 3 with truly-shared composition (e.g., Koin init), the same split applies: parent keeps shared; a new `:composeApp:mobile` sibling holds phone App composition. In practice `:composeApp` serves only as the phone app's library; since there's no Wear sibling hosting phone composition, the simplest Phase 3 answer is **leave `:composeApp` unchanged** and let `:wearApp` host its own Wear composition directly (see D3). `:composeApp` commonMain is not consumed by `:wearApp`, so no conflict surfaces. Recorded as an asymmetry: phone composition sits in composeApp's commonMain rather than an explicit mobile sibling. Future work.

**`:wearApp`:**
- Becomes a real Android application with Wear UI composition: imports `:feat:projects:fe:driving:wear` + `:feat:authentication:fe:driven:wear` + Wear Koin module. Parent-module driven adapters come through `:feat:authentication:fe:driven:impl` (for `AndroidSettingsTokenStore`). See D3.

**Convention plugins:**
- A new `-mobile` sibling is a KMP module with an Android target (phone-Material 3 Compose). The existing `snagDrivingFrontendMultiplatformModule` plugin fits this (it's what the current `driving/impl` uses). Prefer reuse.
- A `-wear` sibling is an Android-only module using Wear Compose. May need a new `snagWearMultiplatformModule` plugin if existing plugins don't fit (current plugins assume full KMP target set). First preference: reuse an Android-only existing plugin; fallback: new plugin.

**Expected ripple (revised):**
- **File moves (parent → mobile siblings):** ~1-2 files from `feat/projects/fe/driving/impl` commonMain → new `feat/projects/fe/driving/mobile` commonMain. ~1 file from `feat/authentication/fe/driven/impl` androidMain → new `feat/authentication/fe/driven/mobile` androidMain. Tracked as git renames; semantic edits flagged.
- **New modules:** `feat/projects/fe/driving/mobile`, `feat/projects/fe/driving/wear`, `feat/authentication/fe/driven/mobile`, `feat/authentication/fe/driven/wear`. Each: 1 `build.gradle.kts` + 1-3 content files + `settings.gradle.kts` entry.
- **Parent-module edits:** `feat/projects/fe/driving/impl/build.gradle.kts` may need scope adjustment (add `api` exports of VM types if mobile/wear siblings need them). `feat/authentication/fe/driven/impl/build.gradle.kts` similar for `AndroidSettingsTokenStore`. Each such edit is predicted structural (intrinsic-but-trivial); any non-build-gradle edit is a finding.
- **Consumer-side rewires:** `:composeApp` / `:androidApp` depend on `:feat:projects:fe:driving:mobile` instead of (or in addition to) `:feat:projects:fe:driving:impl`. Similarly for auth. Plus `settings.gradle.kts` entries.
- **`:wearApp`**: expanded from spike stub — 4-6 new files (Wear App composition root, Wear auth gate, Wear Koin module, theme bridge) + build.gradle.kts deps + manifest stays as-is.
- **`build-logic/`**: 0 or 1 new convention plugin.

**Why this is the right structural answer for thesis measurement.** The parent + mobile/wear sibling split cleanly attributes cost to platform variants. Every mobile sibling is the phone cost that WAS hidden inside the parent before Wear was considered — making the thesis prose honest about the "phone-specific code was always platform-variant code, just not labelled." Every wear sibling is the new Wear cost. Parent keeps truly-shared code. §4.4 can report: "adding Wear OS required, per Wear-affected feature, (a) extracting phone-specific code from the parent into a mobile sibling + (b) authoring a wear sibling. The mobile extraction is per-feature labor that should have happened at architecture design time; it surfaced during the Wear experiment because the existing architecture was implicitly phone-centric."

**Relation to §D6b universal new-platform procedure** — the revision surfaces a new per-feature action: **parent-module decomposition audit**. For every feature shipping on the new platform, inspect the parent's commonMain/androidMain for phone-specific content, extract to a mobile sibling, add the wear sibling. The mobile-extraction cost is paid once per feature (on first new-platform extension; reused for future platforms).

### D3. `:composeApp` (unchanged) + `:wearApp` composition

`:composeApp` stays phone-only and unchanged in Phase 3. The asymmetry is recorded as §4.9 future work: ideally `:composeApp` would also split into `-mobile` sibling + parent (consistent with the feature-level pattern), but Phase 3 scope does not require it (Wear does not consume `:composeApp`).

`:wearApp` (Android application, spike stub today) is extended to become a real Wear app. New files, all local to `:wearApp/`:

- `MainActivity.kt` (existing stub extended) — `setContent { WearApp() }` entry.
- `WearApp.kt` — top-level Wear composition root. Wraps `WearMaterialTheme` + boots the Wear Koin graph + routes to `WearAuthenticationGate` → `WearProjectList`.
- `SnagWearTheme.kt` — bridges `:lib:design/fe` color tokens → `androidx.wear.compose.material3.MaterialTheme`. Minimal; picks primary / onPrimary / background from Snag tokens only.
- `WearAuthenticationGate.kt` — observes shared auth state (via existing `feat/authentication/fe/ports` + `feat/authentication/fe/app/impl`) and shows either the sign-in button (invokes the `OidcExecutor` port, bound to the Wear adapter from `:feat:authentication:fe:driven:wear`) or `WearProjectList` (from `:feat:projects:fe:driving:wear`).
- `WearAppKoinModule.kt` — assembles the Wear Koin graph. Imports shared modules (business / app / ports / parent driven KoinModule for `AndroidSettingsTokenStore`) + the Wear adapter module from `:feat:authentication:fe:driven:wear`.

Wear-specific driven adapters (notably the OIDC adapter) now live in proper sibling modules (`feat/authentication/fe/driven/wear`), not inside `:wearApp`. `:wearApp` only hosts app-level composition + Koin wiring. This is cleaner than my earlier draft which put the adapter in `:wearApp` itself.

**Why `:wearApp` still hosts the Wear Koin graph and not `FrontendModulesAggregate`:** the aggregate currently binds the phone OIDC adapter at its composition point. Reusing it for Wear would either require a runtime conditional (falsifies P4's "one-line variant pick") or would force splitting the aggregate. Hosting the Wear Koin composition inside `:wearApp` is the cleanest answer: the Wear platform-app module knows it is Wear and assembles its own Koin graph from shared modules + the new `-wear` sibling adapter modules. This is a revision of P4's phrasing, not a falsification — the split is at module boundary, which is the whole point of the sibling-module approach.

Phone (`:composeApp` via `:androidApp`) and Wear (`:wearApp`) share: the entire `feat/*/fe/{app,ports,contract,model}` layer + the unchanged parts of each feature's `driving/impl` parent + `driven/impl` parent. They do NOT share UI composition and do NOT share adapters that are platform-variant-specific (phone OIDC in `-mobile` sibling, Wear OIDC in `-wear` sibling). They have separate Koin assembly by design.

### D4. Auth — OAuth 2.0 PKCE via `RemoteAuthClient`

Per [Google's Wear OS auth guidance](https://developer.android.com/training/wearables/apps/auth-wear) and the [2025 evolution-of-Wear-OS-authentication post](https://android-developers.googleblog.com/2025/08/the-evolution-of-wear-os-authentication.html), the three accepted paths for standalone Wear OS are Credential Manager + passkeys (primary; requires server passkey support Snag doesn't have → out of scope), **OAuth 2.0 PKCE via `RemoteAuthClient`** (fallback; chosen), and Mobile Auth Token Data Layer Sharing (requires companion Wear Data Layer infra Snag doesn't have → out of scope).

Implementation — parent + mobile + wear sibling split for `feat/authentication/fe/driven/impl`:

- **Parent `feat/authentication/fe/driven/impl`** — keeps truly-shared content: `AndroidSettingsTokenStore` (both phone and Wear persist tokens via SharedPreferences identically), any truly-shared auth-driven plumbing. Current Koin registration moves to `-mobile` or stays as pure `api` exports — decided during implementation based on what the current code actually holds.
- **New `feat/authentication/fe/driven/mobile`** — phone OIDC executor adapter extracted from the current parent's `androidMain`. Its Koin `authenticationDrivenModule` (phone-variant) moves here. Existing consumer `FrontendModulesAggregate` now depends on this sibling for the phone binding. Tracked as git renames where possible; any file needing edit (not just move) is a finding.
- **New `feat/authentication/fe/driven/wear`** — new Wear OIDC executor adapter: `WearOidcExecutorAdapter.kt` implementing the existing `OidcExecutor` port using `RemoteAuthClient.sendAuthorizationRequest(OAuthRequest(url=…, codeChallenge=…))`. Callback delivers the authorization code; the adapter completes the PKCE exchange and writes the resulting token via the parent module's `AndroidSettingsTokenStore`. Includes `AuthenticationDrivenWearModule.kt` (Koin) binding `OidcExecutor` → Wear adapter.
- **Port contract untouched** — this is the **AVT (Action Version Transparency) demonstration** for §4.5. A new adapter substitutes under an existing port; no consumer — phone or Wear — sees the contract change.
- **Unit test** — `WearOidcExecutorAdapterTest.kt` in `feat/authentication/fe/driven/wear/src/test/...` using a fake `RemoteAuthClient` → asserts token-store write on success + error propagation on failure.

**Flow.** User taps "Sign in" on watch → `WearAuthenticationGate` (in `:wearApp`) invokes `OidcExecutor` (bound via `:feat:authentication:fe:driven:wear`'s Koin module) → `RemoteAuthClient` shows "Open phone to continue" → phone browser opens Snag OIDC → PKCE completes → callback returns to watch → token persisted via parent module's `AndroidSettingsTokenStore` → the shared `AuthenticationState` observer flips → Wear UI routes to `WearProjectList`.

**Expected ripple.**
- Parent `feat/authentication/fe/driven/impl`: phone adapter files git-moved out to mobile sibling (tracked as renames). Parent Koin / plumbing may need small edits (widen visibility of `AndroidSettingsTokenStore` or similar). Any non-rename edit is a finding.
- New mobile sibling: 1-2 moved files + new `build.gradle.kts` + settings entry.
- New wear sibling: 2 new intrinsic files (`WearOidcExecutorAdapter.kt` + `AuthenticationDrivenWearModule.kt`) + 1 test + `build.gradle.kts` + settings entry.
- `FrontendModulesAggregate` (in `koinModulesAggregate/fe/`): dependency update only (points at mobile sibling instead of parent's android adapter binding). If anything more than a dependency-list edit is needed, finding.
- Port contract and use cases: 0 touches.

**Manifest placeholder.** `:wearApp` already declares `manifestPlaceholders["oidcRedirectScheme"] = "snag"` (set during the spike). No additional manifest work required for auth in Phase 3.

### D5. Screen — `WearProjectList` inside the new `feat/projects/fe/driving/wear` sibling

`WearProjectList` lives in the new wear sibling under `driving/`:
```
feat/projects/fe/driving/
  impl/           (parent; keeps VMs, UiState, truly-shared composables)
  mobile/         (NEW: phone-Material 3 Composables — extracted from parent's commonMain)
  wear/           (NEW: Wear-Material 3 Composables)
    build.gradle.kts
    src/main/
      AndroidManifest.xml                (minimal, if required by convention plugin)
      kotlin/cz/adamec/timotej/snag/projects/fe/driving/wear/
        WearProjectList.kt
```

Rationale: platform-specific UI is owned by the feature's driving/`<variant>` sibling. Phone UI and Wear UI are parallel at the same level, with truly-shared UI (VMs, UiState, navigation routes — anything Compose-Runtime-only) in the parent `driving/impl`.

**Parent `driving/impl` decomposition** — files move out of the current tangled commonMain:
- **Stays** in parent commonMain: `ProjectsViewModel.kt`, any `ProjectsUiState.kt`, any navigation/route types, any Compose-Runtime-only primitives.
- **Moves** to `driving/mobile` commonMain: `ProjectsScreen.kt` + any other phone-Material 3 Composables (files using Scaffold, TopAppBar, LazyVerticalGrid, Material 3 adaptive navigation, etc.).
- **New** in `driving/wear` commonMain: `WearProjectList.kt`.

Each file move is a git rename; any rename that can't be purely mechanical (i.e., needs a code edit to survive the move) is a finding.

**Wear sibling module specifics:**
- **Android-only KMP module** (Wear is single-platform). Convention plugin: prefer reusing `snagDrivingFrontendMultiplatformModule` applied with a narrow target set; fall back to new `snagWearMultiplatformModule` in `build-logic/` if existing plugins force unwanted non-Android targets.
- **Dependencies**: `:feat:projects:fe:{driving:impl, app:impl, ports, model, contract}` + `androidx.wear.compose.material3` + `androidx.wear.compose.foundation` + koin-compose.
- **Composable**: `ScalingLazyColumn` with one `Chip` per project (name + status). Tapping a chip: out of scope. Loading state: Wear `CircularProgressIndicator`. Error state: text + retry `Chip`.
- **VM consumption**: `ProjectsViewModel` is re-exported from parent `driving/impl` (now decoupled from phone Material 3 so it's safely consumed by either sibling). If the VM is `internal`, widen to public via `api` on parent's dependencies (parent-build-gradle edit is the minimal intrinsic ripple) or explicitly widen the declaration. Whichever, record as finding per plan's prediction failure mode.

**Mobile sibling module specifics:**
- `feat/projects/fe/driving/mobile` — holds `ProjectsScreen.kt` + other phone-Material 3 Composables from the parent's previous commonMain.
- Convention plugin: `snagDrivingFrontendMultiplatformModule` (same as current parent). Full KMP target set retained (iOS, web, desktop, phone Android) since Compose Multiplatform Material 3 compiles to all these.
- Dependencies: same as current parent `driving/impl` (Compose Multiplatform material3 etc.) + depends on `:feat:projects:fe:driving:impl` (parent) for shared VMs.

**Consumers updated:**
- `:composeApp` — previously depended on `:feat:projects:fe:driving:impl`; now also depends on (or switches to) `:feat:projects:fe:driving:mobile` because that's where `ProjectsScreen` lives. Precise dep-list delta decided during implementation; expected: add `:feat:projects:fe:driving:mobile`, keep `:feat:projects:fe:driving:impl` (parent is transitively needed for VMs).
- `:wearApp` — depends on `:feat:projects:fe:driving:wear` (+ transitive parent).

**Expected ripple (projects feature, driving layer):**
- Parent `driving/impl`: git renames moving phone Composables out (collateral, structural). `build.gradle.kts` may change `api`/`implementation` scopes for shared types (intrinsic-but-trivial).
- New mobile sibling: 1 `build.gradle.kts` + ~5-10 moved files + `settings.gradle.kts` entry.
- New wear sibling: 1 `build.gradle.kts` + 1 `WearProjectList.kt` + optional manifest + `settings.gradle.kts` entry.
- `:composeApp` / `:androidApp`: dependency list update (~1-3 lines).
- Zero touches to `feat/projects/fe/{app,ports,contract,model,driven/impl}`.
- Zero touches to any other feature.

### D6. Measurement — ripple decomposition + per-module-tree scaling

Deliverables (unchanged):
- `analysis/data/ripple_wearos-project-list_files.csv`
- `analysis/data/ripple_wearos-project-list_units.csv`
- `analysis/data/ripple_wearos-project-list_by_module_tree.csv` (new scaling chart input)
- `analysis/classifications/wearos-project-list.yaml` (rule-based classifier)
- `analysis/classifications/wearos-project-list_critique.md` (qualitative ≥ 5 observations, file:line + NS-theorem tag per §J.2 schema)
- `analysis/figures/fig_4_4_wear_ripple_by_module_tree.pdf` (new bar chart in `figures.py`)

Ripple tool invocation (unchanged):
```
analysis/feature_retro.py \
  --ref experiment/wearos-feasibility-spike \
  --base main \
  --change-kind platform_extend \
  --rules analysis/ripple_rules.yaml \
  --classifications analysis/classifications/wearos-project-list.yaml \
  --out analysis/data/
```

`ripple_rules.yaml` already supports `change_kind: platform_extend`. Rules for this revised case classify (updated for parent + mobile/wear sibling topology):
- `wearApp/**` → **local** (the platform-app shell — expected per-new-platform cost).
- `feat/**/fe/driving/wear/**` new files → **intrinsic** (per-feature Wear UI sibling; recurs per Wear-ported feature).
- `feat/**/fe/driven/wear/**` new files → **intrinsic, ns_theorem: AVT** (Wear-specific driven adapter substituting under existing port — auth OIDC primary example).
- `feat/**/fe/driving/mobile/**` new files (content moved from parent) → **collateral** (structural phone-extraction; one-time per feature × new-platform-extension, tracks the cost of architecture-didn't-pre-split).
- `feat/**/fe/driven/mobile/**` new files (content moved from parent) → **collateral** (same reason as above but for driven).
- Git renames from `feat/**/fe/driving/impl/src/commonMain/**` to `feat/**/fe/driving/mobile/src/commonMain/**` → **collateral** (file moves, structural).
- Git renames from `feat/**/fe/driven/impl/src/androidMain/**` to `feat/**/fe/driven/mobile/src/androidMain/**` → **collateral**.
- Parent `feat/**/fe/driving/impl` or `feat/**/fe/driven/impl` build.gradle.kts edits (scope adjustment, `api` exports) → **collateral, with finding annotation** if the edit is anything beyond a scope adjustment.
- `settings.gradle.kts` additions for new siblings → **collateral**.
- Convention-plugin additions/edits in `build-logic/` → **collateral**.
- `:wearApp/*` new files (WearApp, theme, gate, Koin module) → **intrinsic, ns_theorem: SoC** (per-new-platform app composition; structural but platform-specific).
- `:wearApp/build.gradle.kts` dependency additions → **collateral**.
- **Any edit to files NOT in the above categories** (e.g., shared `feat/*/fe/{app,ports,contract,model}`, other features, `:composeApp`, `koinModulesAggregate`, `lib/*`) → red flag. Each such edit is a prediction falsification logged in critique md with file:line + P# reference.
- `:composeApp` / `:androidApp` / `koinModulesAggregate` build.gradle.kts dependency-list edits (to switch to `-mobile` siblings) → **collateral**. Code edits in these modules are findings.

Per-module-tree scaling test: for each top-level tree (`core/`, `lib/`, `feat/`, `app/`, `business/`, `composeApp/`, `wearApp/`, `build-logic/`), count production files with any line changed on the experiment branch. Emit `analysis/data/ripple_wearos-project-list_by_module_tree.csv`. `figures.py` adds a bar chart `fig_4_4_wear_ripple_by_module_tree.pdf`.

**Shape predictions** (revised for parent + mobile/wear sibling topology):
- `business/`, `business/model/`, `app/model/`, `core/`, pure `lib/*`, contract modules → **predicted 0 LOC changed** (tests P1).
- `feat/*/fe/{app,ports,contract,model}` (all features) → **predicted 0 LOC changed** (tests P1).
- `feat/*/fe/driven/impl` (other features) → **predicted 0 LOC changed**.
- `feat/*/fe/driving/impl` (other features) → **predicted 0 LOC changed**.
- `feat/projects/fe/driving/impl` (parent) → predicted: phone Composables git-renamed out to mobile sibling; build.gradle.kts scope adjustments (api exports). Any source-file semantic edit is a finding.
- `feat/projects/fe/driving/mobile/` → **new module** (moved content + build.gradle.kts).
- `feat/projects/fe/driving/wear/` → **new module** (`WearProjectList.kt` + build.gradle.kts + optional manifest).
- `feat/authentication/fe/driven/impl` (parent) → predicted: phone OIDC adapter git-renamed out; `AndroidSettingsTokenStore` stays; build.gradle.kts scope adjustments.
- `feat/authentication/fe/driven/mobile/` → **new module** (moved phone OIDC adapter + Koin module + build.gradle.kts).
- `feat/authentication/fe/driven/wear/` → **new module** (WearOidcExecutorAdapter + AuthenticationDrivenWearModule + test + build.gradle.kts).
- `:composeApp/` source files → **predicted 0 LOC changed** (phone composition unchanged).
- `:composeApp/build.gradle.kts` → predicted dependency-list edit (~3-5 lines) to depend on new mobile siblings.
- `:wearApp/` → predicted 4-5 new Kotlin files (MainActivity extended, WearApp, SnagWearTheme, WearAuthenticationGate, WearAppKoinModule) + build.gradle.kts deps + manifest tweaks. The original spike's MainActivity stub is extended; that diff is **intrinsic**.
- `build-logic/` → predicted 0 files or 1 new convention plugin.
- `koinModulesAggregate/fe/build.gradle.kts` → predicted dep-list edit if it needs to point at mobile sibling (otherwise 0). No code-file edits.
- `settings.gradle.kts` → predicted +4 lines (projects/driving/mobile, projects/driving/wear, authentication/driven/mobile, authentication/driven/wear).

Deviations from these predictions are findings recorded in the critique md with file:line + P# references + NS-theorem category.

### D6b. Universal new-platform procedure — qualitative findings for the thesis

Thesis §4.4 must document not only ripple numbers but the **procedure a developer has to follow** to add any new platform target (not just Wear OS). This is an architectural-cost story the numbers alone can paint falsely rosy (e.g., "99 % of modules: 0 LOC changed" hides the review labour that produced the 0).

The following actions are **universally required for any new platform target** of Snag's KMP architecture (Android TV, Android Automotive, KaiOS, a future OS, etc.). The Wear OS experiment is where they are first observed and documented, but they recur per platform.

**A. Codebase-wide naming audit.** *Eliminated by the D2 toolchain finding.* The original plan assumed a codebase-wide rename of `androidMain` → `mobileAndroidMain` to distinguish phone and Wear Android source sets. AGP 9's `com.android.kotlin.multiplatform.library` plugin has a single Android target per module, so no rename is needed. This is a positive side-effect of the sibling-module split: one ecosystem-wide review load the original approach carried, the revised approach avoids.

**B. Per-module semantic review of commonMain.** Retained. For every KMP module whose commonMain contains platform-facing code (Compose UI, Compose Multiplatform helpers, JVM std libs, serialization), a developer still mentally validates: "the new platform consumes this module via a sibling — does the module's commonMain compile for the Android target? are any APIs it uses Wear-incompatible?" Classification outcomes: (i) no change (expected common case), (ii) a finding that forces a split or widening. Every module with shared-layer content must be reviewed even when the outcome is "no change". **This cognitive cost scales with codebase size and is invisible in the ripple numbers.** Thesis §4.4 names it as an architectural cost the sharing-first architecture does not eliminate.

**C. Per-module Android-target-variant decision.** *Eliminated by D2.* Single Android target per module, no variant split to decide.

**D. Per-feature product decision: does this feature ship on the new platform?** Transformed from "Composable audit" to "product decision." For every feature, the team decides whether to ship UI on the new platform. If yes, author a sibling module. This is a faster/cleaner decision point than the original plan's per-Composable audit: the feature is a unit, the Composables inside it are the feature author's problem. This is outside the architecture but inside the "adding a platform" workstream.

**E. Per-driven-module technology-compatibility audit.** For every driven-impl adapter in the codebase (auth, storage, database, network, files, biometrics, notifications, sync, …), ask: "does this technology have a different API or recommended pattern on the new platform?" Examples: OIDC on Wear → `RemoteAuthClient` (different API); notifications on Wear → different channel semantics; file picker on Wear → unavailable. Most adapters survive unchanged, but EVERY adapter must be reviewed. Another cognitive cost.

**F. Product-axis decision per feature.** Folded into action D above.

**G. Convention-plugin engineering.** One-time engineering cost in `build-logic/`. Under the revised sibling-module approach this cost is smaller: either an existing plugin is reused or a lightweight new `snagWearMultiplatformModule` is added. Amortized across all Wear sibling modules.

**H. Design system adaptation.** For Compose-based new targets, the design tokens in `:lib:design/fe` need to be re-wrapped into the new platform's theme library. One-time per design-system-incompatible platform.

**I. Manifest / build / CI configuration.** Each new-target-specific app module (`wearApp`, `tvApp`, …) needs a manifest, resource qualifiers, app ID, minSdk override, CI job. Per-new-platform, not per-feature.

**J. Emulator / device / QA pipeline setup.** New emulator images in CI; QA device coverage matrix extended. Per-new-platform.

**Thesis writeup structure for §4.4.** Prose covers (in this order):

1. **Numeric ripple table** (intrinsic / collateral / local) from `feature_retro.py`.
2. **Per-module-tree scaling figure** `fig_4_4_wear_ripple_by_module_tree.pdf`.
3. **Procedure narrative** (A–J above, adapted from this plan to Czech) — the qualitative cost of the experiment.
4. **Architectural flaw identification** — explicit paragraph naming:
   - "per-module semantic review load" (action B) — invisible cognitive cost the sharing-first architecture does not eliminate (cross-ref §4.9 threats).
   - "AGP 9.0 single-Android-target constraint" — the toolchain-imposed module-level split is a coarser structural attribution than the source-set split the architecture's intent would suggest. Recurs per Wear-ported feature (sibling module each time), vs the original plan's one-time-per-codebase source-set refactor. Named as an observation that the production build tool constrains the ripple locus, not as a criticism of the architecture itself.
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

1. **This plan approved.** Plan doc already migrated to `analysis/phase-3-plan.md` on chore branch (commit 08b4ca037). This plan file tracks the revised parent + mobile/wear sibling approach; the chore-branch copy is updated to match before implementation starts on the experiment branch.
2. **Split authentication driven layer** (do this first — smallest, pure refactor, validates the pattern):
   - Create `feat/authentication/fe/driven/mobile` sibling. Move phone OIDC executor adapter from parent androidMain to mobile sibling androidMain. Move phone Koin module to mobile. Leave `AndroidSettingsTokenStore` in parent (shared).
   - Create `feat/authentication/fe/driven/wear` sibling empty (content in step 5).
   - Register both in `settings.gradle.kts`.
   - Update `FrontendModulesAggregate` build.gradle.kts to include new mobile sibling.
   - Compile check: `./gradlew :androidApp:assembleDebug` succeeds.
3. **Split projects driving layer** (bigger refactor; validates that the phone UI can live in a sibling):
   - Create `feat/projects/fe/driving/mobile` sibling. Identify phone-Material 3 Composables in parent commonMain. Move them to mobile sibling commonMain (git rename). Keep VM + UiState + routes in parent commonMain.
   - Create `feat/projects/fe/driving/wear` sibling empty (content in step 6).
   - Update parent build.gradle.kts to expose VM as `api` if needed.
   - Register siblings in `settings.gradle.kts`.
   - Update `:composeApp` or `:androidApp` build.gradle.kts to depend on mobile sibling.
   - Deal with `ProjectsViewModel` visibility — widen if internal (record as finding).
   - Compile check: `./gradlew :androidApp:assembleDebug` succeeds (phone still works).
4. **Run `./gradlew check`** — phone build passes end to end. Fix ALL errors in one pass (CLAUDE.md rule).
5. **Populate `feat/authentication/fe/driven/wear`** — add `WearOidcExecutorAdapter.kt` + `AuthenticationDrivenWearModule.kt`. Add test `WearOidcExecutorAdapterTest.kt` with fake `RemoteAuthClient` (TDD).
6. **Populate `feat/projects/fe/driving/wear`** — add `WearProjectList.kt` using Wear Compose Material 3 + `ScalingLazyColumn` + `Chip`. Consumes `ProjectsViewModel` from parent.
7. **Wire `:wearApp`** — extend the spike `MainActivity` + new files: `WearApp.kt`, `SnagWearTheme.kt`, `WearAuthenticationGate.kt`, `WearAppKoinModule.kt`. `:wearApp/build.gradle.kts` adds deps on `:feat:projects:fe:driving:wear` + `:feat:authentication:fe:driven:wear` + `:feat:authentication:fe:{app:impl,ports}` + `androidx.wear.compose.material3`/`foundation`.
8. **Full `./gradlew check` from repo root** — every error addressed in one pass.
9. **Emulator round-trip** — pair phone emulator + Wear emulator, sign in via `RemoteAuthClient`, verify project list with ≥ 3 seeded projects. Capture screenshot → `images/wear-os-project-list.png` (thesis repo).
10. **Ripple tool run** — update `ripple_rules.yaml` to match the new path patterns from D6. Run `feature_retro.py --ref experiment/wearos-feasibility-spike --change-kind platform_extend`. Write `analysis/classifications/wearos-project-list_critique.md` (≥ 5 observations; P1-P7 held/failed statement; D6b actions B, D, E, G, H, I, J performed with module-count figures).
11. **Scaling figure** — add Wear bar to `figures.py`; regenerate `fig_4_4_wear_ripple_by_module_tree.pdf`.
12. **Thesis edits** (new branch on thesis repo, e.g. `feat/phase3-wearos-draft`) — fill `% TODO` at `text/text.tex` L3247 with all six writeup elements. Appendix CSV `\input{}` linked.
13. **Update `analysis/phase-2-plan.md`** — §L.2 checkboxes flipped; §K.1 row for §4.4 landing; §L.4 gating step 1 struck through; §L.3 updates.
14. **Phase 3 addendum** appended to `analysis/wearos_experiment.md`.
15. **Update `analysis/phase-3-plan.md` on chore branch** — sync with this plan file's final content.
16. **Draft PR** — ask user; if yes, on `experiment/wearos-feasibility-spike`, titled per CLAUDE.md, description per the PR template.

## Critical files

### Existing (read; structural edits expected per D2; source-code edits flagged as findings)

**Parent modules being decomposed (file moves expected, scope adjustments in build.gradle.kts):**
- `feat/projects/fe/driving/impl/build.gradle.kts` — scope adjustments (api exports for VM).
- `feat/projects/fe/driving/impl/src/commonMain/kotlin/.../projects/ProjectsScreen.kt` + other phone-Material 3 Composables — git-renamed to `feat/projects/fe/driving/mobile/src/commonMain/...`.
- `feat/projects/fe/driving/impl/src/commonMain/kotlin/.../vm/ProjectsViewModel.kt` — STAYS in parent. If internal-scoped, widen (finding).
- `feat/authentication/fe/driven/impl/build.gradle.kts` — scope adjustments.
- `feat/authentication/fe/driven/impl/src/androidMain/kotlin/.../AuthenticationDrivenModule.android.kt` + phone OIDC adapter files — git-renamed to `feat/authentication/fe/driven/mobile/src/androidMain/...`.
- `feat/authentication/fe/driven/impl/src/androidMain/kotlin/.../AndroidSettingsTokenStore.kt` — STAYS in parent (shared).

**Consumers updated (build.gradle.kts dep-list edits only):**
- `composeApp/build.gradle.kts` OR `androidApp/build.gradle.kts` — add `:feat:projects:fe:driving:mobile` + `:feat:authentication:fe:driven:mobile` to deps; parent modules stay as transitive.
- `koinModulesAggregate/fe/build.gradle.kts` — dep-list edit if aggregate binding path requires mobile sibling.

**`:wearApp` spike stubs extended:**
- `wearApp/build.gradle.kts` — new deps (`:feat:projects:fe:driving:wear`, `:feat:authentication:fe:driven:wear`, `:feat:authentication:fe:{app:impl,ports}`, `androidx.wear.compose.material3`/`foundation`).
- `wearApp/src/main/AndroidManifest.xml` — inspected; keep placeholder.
- `wearApp/src/main/java/.../MainActivity.kt` — extended to `setContent { WearApp() }`.

**Shared-layer (predicted no-edits; any edit is a finding):**
- `feat/authentication/fe/app/impl/...`, `feat/authentication/fe/ports/...`, `feat/authentication/fe/contract/...`, `feat/authentication/fe/model/...`.
- `feat/projects/fe/app/impl/...`, `feat/projects/fe/ports/...`, `feat/projects/fe/contract/...`, `feat/projects/fe/model/...`, `feat/projects/fe/driven/impl/...`.
- `koinModulesAggregate/fe/src/commonMain/...` source files (only build.gradle.kts is allowed).
- All other `feat/*/fe/...` modules.

**Settings / plan-doc / tooling:**
- `settings.gradle.kts` — add `include(":feat:projects:fe:driving:mobile")`, `:feat:projects:fe:driving:wear`, `:feat:authentication:fe:driven:mobile`, `:feat:authentication:fe:driven:wear`.
- `analysis/ripple_rules.yaml` — update `platform_extend` rules per D6.
- `analysis/feature_retro.py` — confirm `--by-module-tree` output or add.
- `analysis/figures.py` — add `fig_4_4_wear_ripple_by_module_tree.pdf` function.
- `analysis/wearos_experiment.md` — Phase 3 addendum.
- `analysis/phase-3-plan.md` (chore branch) — keep in sync with this plan file.

### Build-logic

- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/MultiplatformModuleSetup.kt` — read only.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/*.kt` — read; one new `SnagWearMultiplatformModulePlugin.kt` may be added if existing plugins don't fit the wear siblings.

### New (on `experiment/wearos-feasibility-spike`)

**Projects driving siblings:**
- `feat/projects/fe/driving/mobile/build.gradle.kts`
- `feat/projects/fe/driving/mobile/src/commonMain/kotlin/.../` (moved phone-Material 3 Composables; git renames)
- `feat/projects/fe/driving/wear/build.gradle.kts`
- `feat/projects/fe/driving/wear/src/main/AndroidManifest.xml` (if required)
- `feat/projects/fe/driving/wear/src/main/kotlin/cz/adamec/timotej/snag/projects/fe/driving/wear/WearProjectList.kt`

**Authentication driven siblings:**
- `feat/authentication/fe/driven/mobile/build.gradle.kts`
- `feat/authentication/fe/driven/mobile/src/androidMain/kotlin/.../` (moved phone OIDC adapter files; git renames)
- `feat/authentication/fe/driven/wear/build.gradle.kts`
- `feat/authentication/fe/driven/wear/src/androidMain/kotlin/cz/adamec/timotej/snag/authentication/fe/driven/wear/WearOidcExecutorAdapter.kt`
- `feat/authentication/fe/driven/wear/src/androidMain/kotlin/.../AuthenticationDrivenWearModule.kt`
- `feat/authentication/fe/driven/wear/src/androidUnitTest/kotlin/.../WearOidcExecutorAdapterTest.kt`

**`:wearApp` new files (inside spike module):**
- `wearApp/src/main/java/cz/adamec/timotej/snag/wear/WearApp.kt`
- `wearApp/src/main/java/cz/adamec/timotej/snag/wear/SnagWearTheme.kt`
- `wearApp/src/main/java/cz/adamec/timotej/snag/wear/WearAuthenticationGate.kt`
- `wearApp/src/main/java/cz/adamec/timotej/snag/wear/WearAppKoinModule.kt`

**Build-logic (possibly):**
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/SnagWearMultiplatformModulePlugin.kt` (if existing plugins don't fit wear siblings).

**Analysis + thesis:**
- `analysis/classifications/wearos-project-list.yaml`
- `analysis/classifications/wearos-project-list_critique.md`
- `analysis/data/ripple_wearos-project-list_files.csv` + `_units.csv` + `_by_module_tree.csv`
- `analysis/figures/fig_4_4_wear_ripple_by_module_tree.pdf`
- Thesis: `images/wear-os-project-list.png`
- Thesis: `text/text.tex` §4.4 prose (replaces `% TODO` at L3247)

## Verification

Phase 3 is **measurable and reportable** (not "successful") when ALL of the following hold. Note: verification is about completeness of measurement, not about predictions being confirmed. Negative findings against P1–P6 satisfy verification equally — they just change what the thesis prose says.

1. `./gradlew check` passes on `experiment/wearos-feasibility-spike` HEAD, for phone + Wear targets. If it does NOT pass because a prediction was falsified in a way that required unfixable layer edits, the branch is still captured with the failing state documented in the critique md; the thesis then reports "the experiment revealed unresolvable coupling at X" as the main finding and `check` is run with the minimum viable scope that produces the branch's measurement.
2. Full diff vs `main` is captured; every change is one of: (a) a new-file addition in `feat/projects/fe/wear/**`, `wearApp/**`, `analysis/**`, or `build-logic/**` (if a new plugin); (b) a `settings.gradle.kts` addition for `:feat:projects:fe:wear`; (c) `:wearApp` stub extensions (build.gradle.kts dep additions, manifest tweaks, MainActivity body); or (d) a semantic edit to an existing non-wearApp module — any semantic edit flagged in the critique md with P#-reference + NS-theorem category regardless of location. Visibility widenings (e.g., `ProjectsViewModel` internal → public) are always flagged as findings.
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
