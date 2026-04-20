# Wear OS evolvability experiment — measurement log

This file records the Wear OS feasibility spike (Phase 0) and will be extended with the
full Phase 3 measurement once the Wear-native screen is wired through to the shared use cases.

## Phase 0 — Feasibility spike

### Branch

`experiment/wearos-feasibility-spike` (created from `main`).

### Pre-registered hypothesis

> Adding Wear OS as a new platform target should not require modification to any of the
> shared business / app / ports / contract / driven layers. Wear OS is Android at the
> runtime level, so the existing Android-target driven adapters apply unchanged. The only
> platform-specific cost should live inside a new `wearApp` Android-application module.

### Setup

A new top-level `wearApp` module was created mirroring `androidApp`:

- `wearApp/build.gradle.kts` — `com.android.application` plugin + `composeCompiler`. Depends
  on `:composeApp` (transitively pulling every shared layer), `koin-core`,
  `androidx.activity.compose`, and the Wear-specific `androidx.wear.compose:compose-material`
  and `androidx.wear.compose:compose-foundation` libraries.
- `wearApp/src/main/AndroidManifest.xml` — declares `<uses-feature
  android:name="android.hardware.type.watch" />`, the Wear standalone meta-data, and a
  single `MainActivity`.
- `wearApp/src/main/kotlin/cz/adamec/timotej/snag/wear/MainActivity.kt` — minimal Wear-native
  screen rendering "Snag Wear" via `androidx.wear.compose.material.Text` inside a `MaterialTheme`
  Box. Wiring shared use cases through to this screen is deliberately deferred to Phase 3 — the
  spike's purpose is to demonstrate that the dependency graph compiles for the new target, not
  to implement a feature.

Two new entries were added to `gradle/libs.versions.toml`
(`androidx-wear-compose-material` and `androidx-wear-compose-foundation`,
both pinned to `1.5.3`) and one new entry to `settings.gradle.kts` (`include(":wearApp")`).

### Build attempt 1 — failure

`./gradlew :wearApp:assembleDebug`

**Result:** failed at `:wearApp:processDebugMainManifest`.

```
Manifest merger failed : uses-sdk:minSdkVersion 24 cannot be smaller than version 25
declared in library [androidx.wear.compose:compose-material-core:1.5.3]
```

**Diagnosis:** Wear Compose Material requires `minSdk = 25`; the project default in
`libs.versions.toml` is `24`. The constraint is local to Wear and lives entirely inside the
`wearApp` module.

**Importantly:** every preceding compilation task succeeded. The build had already finished
compiling `:core:foundation:common`, `:core:foundation:fe`, every `:feat:*:business:model`,
every `:feat:*:app:model`, every `:feat:*:business:rules`, the relevant `:lib:*`
infrastructure modules, and the supporting KMP intermediate source sets — all for the
Android target — without any modification.

**Repair:** override `wearApp.android.defaultConfig.minSdk` to `25` in
`wearApp/build.gradle.kts`. No shared module touched.

### Build attempt 2 — failure

**Result:** failed at `:wearApp:processDebugMainManifest`.

```
Attribute data@scheme at AndroidManifest.xml requires a placeholder substitution
but no value for <oidcRedirectScheme> is provided.
```

**Diagnosis:** the OIDC library transitively pulled in via `:composeApp` declares an
`<intent-filter>` template that requires the `oidcRedirectScheme` manifest placeholder.
The Android phone app sets it to `"snag"` in `androidApp/build.gradle.kts`.

**Repair:** copy the same placeholder declaration into `wearApp/build.gradle.kts`. No shared
module touched. (The `wearApp` does not perform OAuth flows in the spike, but the placeholder
must be present to satisfy the manifest merger because the OIDC library is transitively
pulled in via `:composeApp`.)

### Build attempt 3 — failure

**Result:** failed at `:wearApp:compileDebugKotlin`. Errors all pointed at unresolved
references inside the spike's own `MainActivity.kt`: `co.touchlab.kermit.Logger`,
`co.touchlab.kermit.koin.KermitKoinLogger`, `org.koin.compose.KoinApplication`,
`koinConfiguration`, etc.

**Diagnosis:** the spike's first-draft `MainActivity.kt` imported Kermit and `koin-compose`
to set up its own Koin graph mirroring `composeApp.App()`. These libraries are declared
`implementation` (not `api`) on `:composeApp`'s commonMain, so they don't leak through to
downstream Android consumers. **This was a bug in the spike's own code, not in any shared
module.**

**Repair:** simplify `MainActivity.kt` to a Wear-Compose-only screen with no Kermit/Koin
imports. The shared-layer compilation success had already been demonstrated by attempts 1
and 2; the Phase 0 spike does not need to wire shared use cases through to the screen
(that is Phase 3 work).

### Build attempt 4 — success

```
> Task :wearApp:assembleDebug
BUILD SUCCESSFUL in 4s
```

APK produced at `wearApp/build/outputs/apk/debug/wearApp-debug.apk`.

### Phase 0 findings

1. **Every shared module compiled for the new Android (Wear OS) target without modification.**
   This is the headline result of the spike. The shared-layer build was visible across all three
   failing attempts: `:core:foundation:common:compileAndroidMain`, every
   `:feat:*:business:model:compileAndroidMain`, every `:feat:*:app:model:compileAndroidMain`,
   every relevant `:lib:*:compileAndroidMain`, `:composeApp:compileAndroidMain` — all green
   from attempt 1 onward.

2. **All friction was scoped to the new `wearApp` module.** Three build failures, three repairs,
   all of them confined to files inside `wearApp/`:
   - `wearApp/build.gradle.kts` — minSdk override (Wear requirement)
   - `wearApp/build.gradle.kts` — OIDC manifest placeholder (transitive lib requirement)
   - `wearApp/src/main/kotlin/.../MainActivity.kt` — drop unused Kermit/Koin imports

   Zero modifications to any shared module. Zero modifications to any pre-existing module
   outside `wearApp/`.

3. **Decision gate: Wear OS is the target.** The spike confirms the experiment's pre-registered
   hypothesis. There is no fallback ladder; Phase 3 will proceed by extending the wearApp into
   a Wear-native screen wired through to the existing shared use cases.

### Future-work flags surfaced by the spike

- The OIDC `manifestPlaceholders["oidcRedirectScheme"]` requirement is propagated by the
  transitive dependency on `:composeApp` even when the consumer (here, `wearApp`) does not
  perform OAuth flows. The forward-looking refactor this hints at is: lift the OIDC dependency
  out of `:composeApp`'s shared classpath and into the phone-driving layer specifically, so a
  consumer that does not need it does not have to satisfy its manifest contract. This is
  recorded here as future work; the spike does not perform the refactor.

## Phase 3 — Live experiment

### Summary

Phase 3 extends the spike into a live Wear-native project-list screen wired through to the shared `feat/projects/fe/app/api.GetProjectsUseCase` via the shared `ProjectsViewModel`, with authentication substituted at the `feat/authentication/fe/ports.AuthTokenProvider` boundary by a Wear-specific `RemoteAuthClient`-backed adapter. The full plan is in `analysis/phase-3-plan.md` (chore branch); ripple measurement + qualitative critique live in `analysis/classifications/wearos-project-list_critique.md` + `analysis/data/ripple_wearos-project-list_*.csv` (chore branch). This file records only the facts specific to the `experiment/wearos-feasibility-spike` branch + the measurement addendum.

### Topology landed

Variant-first module layout (see D8 in the plan). For each feature that actually has platform-variant-specific content, the module decomposes into `{common,nonWear,wear}/<hex-layer>/` siblings:

- **Projects (driving):** `feat/projects/fe/{common,nonWear,wear}/driving/` — phone-Material 3 Composables extracted from the pre-Phase-3 `driving/impl` to a new `nonWear/driving` sibling; `WearProjectList.kt` (Wear Compose Material) in a new `wear/driving` sibling; shared `ProjectsViewModel` + `ProjectsUiState` + navigation routes stayed in `common/driving`.
- **Authentication (driven):** `feat/authentication/fe/{common,nonWear,wear}/driven/` — phone OIDC adapter `OidcAuthTokenProvider` stays in `common/driven` (KMP, no Android-specifics in the class itself); `AuthTokenProvider` Koin binding moved from `authenticationDrivenCommonModule` to `authenticationDrivenNonWearModule`; new `wear/driven` sibling hosts `WearAuthTokenProvider` + `WearAuthFlow` + `RealWearAuthFlow` (PKCE token exchange via raw Ktor) + `authenticationDrivenWearModule` binding `AuthTokenProvider` to the Wear implementation.
- **Koin aggregate:** `koinModulesAggregate/fe/{common,nonWear,wear}/` — single pre-Phase-3 aggregate split into three variant-suffixed siblings (`frontendModulesCommonAggregate` + `frontendModulesNonWearAggregate` + `frontendModulesWearAggregate`). The variant aggregates each `includes` their feature driving/driven sibling Koin modules.
- **`:wearApp`:** extended from the spike stub — `WearApp.kt` composition root, `SnagWearTheme.kt` (minimal), `WearAuthenticationGate.kt` observing shared `AuthTokenProvider`, `WearAppKoinModule.kt` composing `frontendModulesCommonAggregate + frontendModulesWearAggregate`. Phone (`:composeApp`) unchanged in Phase 3.
- **Build-logic:** `ModulePathParser.kt` gained `PlatformVariant.COMMON/NON_WEAR/WEAR` token recognition (one-time per codebase; extends to future platform variants). `AutoWiring.kt` driven-ports resolution rewritten to handle variant-first paths (tail-variant suffix stripping). **Zero new convention plugins** — existing `snagDrivingFrontendMultiplatformModule` and `snagDrivenFrontendMultiplatformModule` plugins were Wear-applicable as-is for the new wear/mobile siblings.

Full step-by-step ripple analysis with P1–P7 pre-registered prediction outcomes + NS-theorem-tagged observations lives in the critique md on the chore branch; summary numbers here: 105 files changed total (5 local, 24 intrinsic, 76 collateral), 3 recurring intrinsic units that repeat per future Wear-ported feature, and ~140 KMP modules semantically reviewed under §D6b actions B/D/E with 138 of them arriving at 0 LOC (the §P7 invisible-review-labour finding).

### Step 9 — emulator round-trip + screenshot

Goal: install the Wear APK on a Wear OS emulator, sign in end-to-end via `RemoteAuthClient`, capture a screenshot of the populated `WearProjectList` for thesis §4.4.

**Live OIDC flow attempted first.** Install command: `./gradlew :androidApp:installDebug :wearApp:installDebug -Psnag.release=true` (debug build variant + release properties profile so BuildKonfig's `SERVER_TARGET=demo` + demo Entra tenant/client ids are loaded into the debug APK).

Entra ID app registration was extended to include the Wear-specific redirect URIs that `RemoteAuthClient` generates (`https://wear.googleapis.com/3p_auth/cz.adamec.timotej.snag.wear{,.debug}`) under the Mobile-and-desktop-applications platform.

A Pixel Watch emulator paired with a Pixel phone emulator via Android Studio's "Pair Wearable" assistant. Bluetooth-only OS-level pairing was first attempted and rejected by `RemoteAuthClient` with `errorCode=1` (ERROR_UNSUPPORTED) — 3p_auth requires a companion app layer the OS-level Bluetooth pairing does not provide. After the Studio pairing assistant completed, the auth flow advanced far enough for the Google `wear.googleapis.com/3p_auth` consent page to render on the phone emulator, but **tapping ALLOW produced a visible ripple yet did not complete the consent callback** — the companion stub that Studio's assistant installs on the phone emulator does not implement the 3p_auth completion handshake that a real Pixel Watch app (or Galaxy Wearable) provides.

**Root cause:** Android Studio's Wear pairing assistant is sufficient for general Wear-emulator functionality testing (display, inputs, sensor simulation) but does **not** ship the full Google Play Services 3p_auth completion layer on the phone emulator. End-to-end `RemoteAuthClient` flow is reachable only with either (a) real paired hardware — Pixel Watch + Pixel phone with the Pixel Watch companion app, or Galaxy Watch + Samsung phone with Galaxy Wearable — or (b) a Wear-OS-emulator tooling setup that the public Android Studio distribution does not currently provide. Neither (a) nor (b) was feasible within the thesis timeline; the author has neither real watch hardware nor a non-standard companion tooling path.

**Workaround for the screenshot: seed mode.** A debug-only `BuildConfig.SEED_MODE` flag (gated on `-Psnag.wear.seed=true`) was added to `:wearApp` in commit `7b5a179da`. When enabled, `WearApp.kt` loads `wearSeedModule` instead of `wearAppModule` — a minimal Koin graph binding a `FakeAuthTokenProvider` (reports `AuthState.Authenticated` immediately) + `FakeGetProjectsUseCase` (emits 3 hardcoded `AppProjectData`) + `FakeCanCreateProjectUseCase` + the shared `ProjectsViewModel` factory. Install command:

```
./gradlew :wearApp:installDebug -Psnag.release=true -Psnag.wear.seed=true
```

In seed mode the Wear UI composes end-to-end (Koin → shared VM → Wear-Compose `ScalingLazyColumn` of `Chip`s via `WearProjectList`) and produces the screenshot saved to the thesis repo at `images/wear-os-project-list.png`. The seed bindings are opt-in (default `false`); the `wearAppModule` real-flow path is unchanged and remains the primary composition route.

**Finding — emulator tooling limitation as §4.4 data.** The emulator-pair `RemoteAuthClient` failure is recorded as **observation #9** in the critique md on the chore branch with P3 refined to "HOLDS at unit-test level (adapter substitutes under the existing port); not independently validated end-to-end on emulator pair due to Android Studio companion-stub limitation." §4.4 prose will report this honestly: the architectural claim (port substitution for auth adapter across platforms) is verified by the code + unit tests; the demonstration screenshot uses seeded data because the emulator tooling path cannot reach the consent-completion callback without real Wear hardware. The emulator limitation is a Google-tooling issue, not a Snag-architecture issue, and is cross-referenced as a §4.9 future-work hole ("Wear end-to-end live OIDC demonstration deferred to when a real Pixel Watch is available; integration verified at adapter-level + screenshot captured via seed mode").

### Artefacts

- `experiment/wearos-feasibility-spike` branch HEAD `7b5a179da` — code + seed mode + screenshot instructions.
- `chore/phase-2-ripple-tooling` — `analysis/phase-3-plan.md`, `analysis/classifications/wearos-project-list.yaml`, `analysis/classifications/wearos-project-list_critique.md`, `analysis/data/ripple_wearos-project-list_{files,units,by_module_tree}.csv`, `analysis/figures/fig_4_4_wear_ripple_by_module_tree.pdf`.
- Thesis: `~/Ctu/dp-thesis-timotej-adamec/images/wear-os-project-list.png` (staged; commits with Phase 5 §4.4 prose on a Phase 5 thesis branch).
