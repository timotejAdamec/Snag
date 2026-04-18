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

### Outcome (steps 5–8 complete, 9 manual)

Plan: `analysis/phase-3-plan.md`. Implementation lands on `experiment/wearos-feasibility-spike` (HEAD `4f98d9fd6`). `./gradlew check` green from repo root.

#### What was built

- **Variant-first module layout (D8).** `feat/authentication/fe/{common,nonWear,wear}/driven/` and `feat/projects/fe/{common,nonWear,wear}/driving/`. Aggregates analogously: `koinModulesAggregate/fe/{common,nonWear,wear}`. Three `PlatformVariant` tokens recognised by the `ModulePathParser` ArchCheck rule.
- **Wear OAuth via `RemoteAuthClient` (PKCE).** `feat/authentication/fe/wear/driven` hosts `WearAuthTokenProvider` + `WearAuthFlow` interface (commonMain, KMP-pure, fully unit-tested) and `RealWearAuthFlow` (androidMain, wraps `RemoteAuthClient` + raw Ktor token-exchange POST). The existing `AuthTokenProvider` port is **not edited**.
- **Wear-native `WearProjectList`.** `feat/projects/fe/wear/driving` Composable using Wear Compose Material 1 (`ScalingLazyColumn` + `Chip` per project, `CircularProgressIndicator` while loading). Consumes shared `ProjectsViewModel` from `:feat:projects:fe:common:driving` (no widening required — VM was already public).
- **`:wearApp` real composition.** Replaces the Phase 0 `implementation(projects.composeApp)` shortcut with explicit deps. Boots Koin via `KoinApplication { modules(wearAppModule) }` where `wearAppModule = frontendModulesCommonAggregate + frontendModulesWearAggregate`. `WearAuthenticationGate` observes `AuthTokenProvider.authState` and routes Unauthenticated → sign-in chip; Authenticated → `WearProjectList`.

#### Architectural findings (full critique in `analysis/classifications/wearos-project-list_critique.md`)

- **P1 (pure-logic layers compile unchanged) — HOLDS.** `business/`, `app/`, `core/`, pure `lib/`, all `feat/*/fe/{app,ports,contract,model}` — zero LOC churn.
- **P3 (port substitution alone for auth) — HOLDS.** `AuthTokenProvider` interface untouched. AVT confirmed for the auth port.
- **P4 (Koin aggregate loads unchanged) — PARTIALLY FALSIFIED.** Single `FrontendModulesAggregate` had to split into common/nonWear/wear variants (D8). Pre-Phase-3 aggregator was implicitly phone-only.
- **P2 (no edits to existing modules) — graded falsification.** Kotlin `internal` classes (`OidcAuthTokenProvider`, `MockAuthTokenProvider`) widened to `public` so per-variant Koin modules can bind them; `composeApp/src/commonMain/.../AppModule.kt` rewritten to consume the variant-split aggregate.
- **P7 (per-module review labour invisible in numeric ripple) — HOLDS.** ~140 KMP modules semantically reviewed under §D6b actions B/D/E; 138 ended at 0 LOC. The "0 LOC = 0 cost" framing must be refused in §4.4 prose.

#### Numeric headlines

- L/I/C files = **5 / 24 / 76**, LOC churn = **227 / 917 / 728**.
- 3 recurring intrinsic units (`koinModulesAggregate/fe/wear/.../platformWearModule.android.kt`, `feat/projects/fe/wear/driving/...`, `feat/authentication/fe/wear/driven/...`) — each repeats per future Wear-ported feature.
- Per-tree scaling: `feat/` 74 files, `koinModulesAggregate/` 14, `wearApp/` 6, `build-logic/` 5, `composeApp/` 2, `testInfra/` 2, `gradle/` 1, `settings.gradle.kts` 1. **Zero in `core/`, `lib/`, `app/`, `business/`, `androidApp/`, `server/`.**

#### Pending

- **Step 9 (manual):** paired phone+Wear emulator round-trip, sign in via `RemoteAuthClient`, capture `images/wear-os-project-list.png` for thesis §4.4.
- **Step 12 (manual sign-off):** thesis §4.4 prose fill at `text/text.tex` L3247.
- **Future-work hole:** Wear runtime not exercised for non-auth driven adapters (database, sync, storage). P5 confirmed only at compile time.
