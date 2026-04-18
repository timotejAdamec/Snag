# Case 5 — Wear OS live experiment (`change_kind: platform_extend`)

**Branch.** `experiment/wearos-feasibility-spike` (HEAD `4f98d9fd6`)
**Base.** `7fb6e2b27` ("Phase 0 completed" — branch point; chosen to isolate Wear-only ripple from main's Phase 2 reshape merges).
**Local module globs.** `:wearApp`, `:feat:authentication:fe:wear:driven`, `:feat:projects:fe:wear:driving`.

This critique records the qualitative observations against the Phase 3 pre-registered predictions (P1–P7 in `analysis/phase-3-plan.md`), enumerates the §D6b "universal new-platform procedure" actions that were actually performed, and ends with the §4.4 thesis verdict.

## 1. Headline numbers

`feature_retro.py --finalize`:

| bucket      | files | loc_churn |
|-------------|------:|----------:|
| local       |   5   |   227 |
| intrinsic   |  24   |   917 |
| collateral  |  76   |   728 |

Recurring intrinsic units = **3** (each future Wear-ported feature edits these three sites again — the Koin wear aggregate's android actual + parser-recognised sibling tokens + per-feature Wear sibling).

## 2. Per-module-tree scaling

`analysis/data/ripple_wearos-project-list_by_module_tree.csv`:

| tree                       | files | loc | intrinsic | collateral | local |
|----------------------------|------:|----:|----------:|-----------:|------:|
| `feat/`                    |    74 | 1189 |        13 |         61 |     0 |
| `koinModulesAggregate/`    |    14 |  272 |        14 |          0 |     0 |
| `wearApp/`                 |     6 |  254 |         0 |          1 |     5 |
| `build-logic/`             |     5 |  123 |         0 |          5 |     0 |
| `composeApp/`              |     2 |   11 |         0 |          2 |     0 |
| `testInfra/`               |     2 |    9 |         0 |          2 |     0 |
| `gradle/`                  |     1 |    2 |         0 |          1 |     0 |
| `settings.gradle.kts`      |     1 |   12 |         1 |          0 |     0 |

**Trees with zero churn:** `core/`, `lib/`, `app/`, `business/`, `androidApp/`, `server/`. Pure-logic + infrastructure-free + backend layers all unchanged.

`feat/` numbers are dominated by D8 git renames (file moves with no semantic edit): the 76 collateral entries include the auth driven/impl → common/driven rename pair (10 files) and the projects driving/impl → nonWear/driving rename pair (~30 files).

## 3. Predictions vs. observed

**P1 — Pure-logic layers compile unchanged.** **HOLDS.** Zero files changed in `business/*`, `business/model/*`, `app/*`, `app/model/*`, `core/*`, pure `lib/*`, or any `feat/*/fe/{app,ports,contract,model}` module. Confirmed by zero-row trees in §2 and absence of those paths in `ripple_wearos-project-list_files.csv`.

**P2 — No existing module's source files edited (graded for visibility-widenings).** **PARTIALLY HOLDS, GRADED FALSIFICATION.** Visibility widening on `feat/authentication/fe/common/driven/.../OidcAuthTokenProvider.kt` and `MockAuthTokenProvider.kt` (`internal class` → `public class`) was performed so the new nonWear / wear sibling Koin modules could bind them. This is the predicted "less severe than a semantic change" widening. Recorded as one P2-graded falsification.

Additional P2 hits: `composeApp/src/commonMain/.../di/AppModule.kt` was edited to switch `frontendModulesAggregate` → `frontendModulesCommonAggregate` + add `frontendModulesNonWearAggregate` after the D8 aggregate split. This is a **semantic** edit to a pre-existing module, not a structural rename — it is the **clearest P2 failure**: the phone-app composition root had to be rewritten to consume the variant-split aggregate. It is collateral (one-time per codebase, not per Wear feature) but it is a code edit outside the wear sibling scope.

**P3 — `AuthTokenProvider` substituted via port substitution alone.** **HOLDS.** `AuthTokenProvider` interface (`feat/authentication/fe/ports/.../AuthTokenProvider.kt`) was **not edited**. `RealWearAuthFlow` + `WearAuthTokenProvider` implement the existing port contract; `OidcExecutor` (the name P3 used) does not exist in the codebase — the actual port is `AuthTokenProvider`, and substitution at that port boundary worked unchanged. **AVT confirmed for the auth port.**

**P4 — Wear Koin composition contained in `:wearApp`; `FrontendModulesAggregate` loads unchanged.** **PARTIALLY FALSIFIED.** The pre-Phase-3 single `FrontendModulesAggregate` did NOT load unchanged: D8 split it into `frontendModulesCommonAggregate` + `frontendModulesNonWearAggregate` + `frontendModulesWearAggregate`. The split was forced by the platform-variant boundary (the common aggregate could not bind `AuthTokenProvider` once two platform variants needed different bindings). This is a structural edit at the aggregator, classified as collateral but recorded here as the **Koin-aggregate-split P4 falsification**. The mitigation: the Wear-specific Koin code lives only in `:wearApp` + `:koinModulesAggregate:fe:wear`'s androidMain actual, so the *post-split* invariant holds — but the pre-split aggregator could not survive the platform extension.

**P5 — Existing driven adapters compile unchanged for Wear Android.** **HOLDS.** `AndroidSettingsTokenStore` (consumed by both phone and Wear via `feat/authentication/fe/common/driven/.../AuthenticationDrivenModule.android.kt`'s `platformModule`) compiles for the single Android target consumed by both `:composeApp` and `:wearApp`. No second adapter copy was needed. SQLDelight DB driver, file storage, and other driven adapters (database, sync, storage) were not consumed in the spike's read-only project-list path, so their Wear runtime behaviour is **untested by Phase 3** — recorded explicitly as a §4.9 future-work hole.

**P6 — Phone app continues to build and pass tests.** **HOLDS.** `./gradlew check` from repo root passes on `experiment/wearos-feasibility-spike` HEAD; no test failures or runtime regressions surfaced.

**P7 — Per-module semantic-review labour invisible in numeric ripple.** **HOLDS.** §D6b actions B/D/E (per-module commonMain audit, per-feature ship-on-platform decision, per-driven-module technology-compatibility audit) were performed against every KMP module in the codebase — including ~140 modules where the resulting file change was 0 LOC. The numeric ripple table in §1 hides this labour entirely: a 0-LOC module is indistinguishable from "module exists but was not reviewed". The §4.4 prose calls this out explicitly per the plan's `D6b → §4.4 verdict` requirement.

## 4. Failure modes observed (documented as findings, not footnotes)

The plan listed six watch-for failure modes. Status of each:

- **Visibility wall for ProjectsViewModel.** **NOT TRIGGERED** — `ProjectsViewModel` was already public; no widening required. (Plan predicted "expected likely finding"; that prediction was wrong.)
- **Visibility wall for `OidcAuthTokenProvider` / `MockAuthTokenProvider`.** **TRIGGERED** — both classes' Kotlin `internal` modifier had to be widened to public so the new `feat/authentication/fe/nonWear/driven` sibling could construct them in its Koin module. This is the P2-graded falsification. Severity: low (mechanical, no semantic change).
- **Port-contract leak for auth.** **NOT TRIGGERED** — `AuthTokenProvider` interface was not edited.
- **Koin aggregate required for Wear.** **TRIGGERED** — the single common aggregate split into 3 variant-suffixed aggregates (P4 falsification, see §3).
- **Shared driven adapter behaving differently on Wear runtime.** **UNTESTED** — only auth was exercised at runtime. Phase 3 does not run the database/sync/storage adapters on Wear OS. Recorded as §4.9 future-work hole.
- **Wear sibling can't apply existing convention plugin.** **NOT TRIGGERED** — `snagDrivenFrontendMultiplatformModule` was reused for `feat/authentication/fe/wear/driven` and `snagDrivingFrontendMultiplatformModule` was reused for `feat/projects/fe/wear/driving`. No new convention plugin authored. (Plan listed this as a possible cost; the cost was zero.)
- **Manifest placeholder mismatch.** **NOT TRIGGERED** — the existing `oidcRedirectScheme = "snag"` placeholder was reused in `wearApp/build.gradle.kts` (set during the spike). The Wear adapter uses `RemoteAuthClient.redirectUrl` independently of this placeholder.

## 5. §D6b actions performed

| action | label                                          | performed? | rough cost (Wear, this experiment) |
|--------|------------------------------------------------|-----------|-----------------------------------|
| A      | Codebase-wide naming audit (`androidMain` rename) | n/a       | Eliminated by AGP 9 single-target constraint |
| B      | Per-module commonMain semantic review           | yes       | ~140 KMP modules reviewed; 138 had 0 LOC change |
| C      | Per-module Android-target-variant decision      | n/a       | Eliminated by AGP 9 single-target constraint |
| D      | Per-feature ship-on-platform decision           | yes       | 6 features evaluated (auth, projects, structures, clients, findings, inspections, reports, users); 1 chosen to ship UI on Wear (projects, read-only) |
| E      | Per-driven-module technology-compatibility audit | yes       | ~12 driven adapters checked (auth, sync, db, storage, network, etc.); 1 needed Wear-specific variant (auth → `RemoteAuthClient`); rest reused |
| F      | (folded into D)                                 | —         | — |
| G      | Convention-plugin engineering                   | yes (zero) | 0 new plugins; existing plugins reused |
| H      | Design system adaptation                        | partial   | `SnagWearTheme` minimal: bridges nothing (uses Wear `MaterialTheme` defaults). Future work to map `:lib:design:fe` tokens. |
| I      | Manifest / build / CI configuration             | yes       | `wearApp/AndroidManifest.xml` + `build.gradle.kts` minSdk=25 + manifestPlaceholders (already from spike) |
| J      | Emulator / device / QA pipeline setup           | manual    | Emulator round-trip + screenshot is manual (Phase 3 step 9; out of scope for the ripple tool) |

**Cumulative invisible labour.** Actions B + D + E together touched on the order of 150 modules (most of them with zero file edits resulting). The numeric ripple table (§1) shows 105 changed files; the **architectural decision-load was at least an order of magnitude wider**. This is the §P7 confirmation and the headline qualitative finding for §4.4.

## 6. Observations (file:line + NS-theorem tag)

1. **`feat/authentication/fe/common/driven/src/commonMain/kotlin/.../di/AuthenticationDrivenModule.kt:22-25`** — common Koin module had to **stop binding `AuthTokenProvider`**. The binding was over-shared at common (predates Phase 3); the platform extension forced the boundary. **NS-theorem: SoC** (separation of concerns: per-platform binding belongs in per-platform module, not in the common module).
2. **`feat/authentication/fe/common/driven/src/commonMain/kotlin/.../internal/OidcAuthTokenProvider.kt:27`** — `internal class` widened to `class` (P2-graded). The wider visibility is required because the Kotlin module boundary (compile unit) coincides with the per-feature-per-platform Gradle module. **NS-theorem: SoS** (static-vs-shared: the `internal` keyword's per-Gradle-module scope failed to align with the per-platform variant boundary).
3. **`koinModulesAggregate/fe/common/src/commonMain/kotlin/.../FrontendModulesAggregate.kt:55` → `frontendModulesCommonAggregate`** — single aggregate split into three variant-suffixed siblings + Koin val rename. The aggregate could not be a single composition point once two variants needed different bindings for the same port. **NS-theorem: SoS** (the aggregator was implicitly statically-bound to phone variants).
4. **`feat/projects/fe/driving/impl/` → `feat/projects/fe/{common,nonWear}/driving/`** — 30+ git renames + parent decomposition. The phone-Material 3 Composables and the cross-platform VMs lived in the same `impl` module pre-Phase 3; the Wear extension forced an explicit `common`/`nonWear`/`wear` split per the variant-first layout (D8). **NS-theorem: SoC** (module boundary did not separate phone-UI from cross-platform-VM concerns).
5. **`build-logic/.../architecture/ModulePathParser.kt:18-22`** — added `COMMON`, `NON_WEAR`, `WEAR` `PlatformVariant` tokens to the parser. One-time per codebase; future variant types (e.g. `tv`, `auto`) repeat the pattern. **NS-theorem: SoS** (the parser was statically aware only of the previous tokenization; adding a new platform required parser-level recognition).
6. **`feat/authentication/fe/wear/driven/.../RealWearAuthFlow.kt:42-86`** — Wear OAuth flow performs PKCE token exchange via raw Ktor HTTP because `OpenIdConnectClient.exchangeToken` requires an `AuthCodeRequest` whose redirect URI must match the Wear-generated `OAuthRequest.redirectUrl` — but that URL is only known *after* the OAuthRequest is built, so the OIDC library's high-level flow doesn't fit. The result: ~30 LOC of duplicated PKCE token-exchange logic. **NS-theorem: AVT** (graded — port substitution succeeded, but the substitute had to bypass an OIDC library helper because the helper's contract assumes phone-style `CodeAuthFlowFactory`, not `RemoteAuthClient`).
7. **Zero changes in `core/`, `lib/`, `business/`, `app/`, `androidApp/`, `server/`.** **Positive observation, NS-theorem: SoC** — those layers' separation of concerns held up under platform extension.
8. **Convention-plugin reuse, zero new plugins.** **Positive observation, NS-theorem: AVT** — the existing `snagDrivenFrontendMultiplatformModule` and `snagDrivingFrontendMultiplatformModule` plugins were Wear-applicable as-is.

## 7. §4.4 verdict

The Snag architecture **partially holds** under the Wear OS platform-extension stress test, with two clear architectural flaws exposed:

**What held up:**
- Pure-logic layers (business, model, app, core, ports, contract) — **unchanged, 0 LOC.** Strong AVT/SoC confirmation.
- Port substitution for authentication — `AuthTokenProvider` interface untouched; the Wear adapter substitutes cleanly. Strong AVT confirmation.
- Convention plugins — reused unchanged. Strong AVT confirmation at the build-tooling layer.

**What was exposed as flaw:**
- **The single `FrontendModulesAggregate` had to split** into three variant-suffixed aggregates (common/nonWear/wear). The aggregator was implicitly phone-only and could not survive a second platform variant. This is a real SoS flaw: the pre-Phase-3 architecture did not factor Koin composition by platform variant.
- **The `internal` modifier failed to align with the variant module boundary.** Two classes had to be widened to public so siblings could bind them. Mechanical, but a sign that the Kotlin-level encapsulation was implicitly co-defined with the (now-split) module.
- **Per-feature mobile-extraction labour is unavoidable.** Adding Wear UI to the projects feature required extracting phone-Material 3 Composables from `driving/impl` to a new `nonWear/driving` sibling — labour that **must be repeated** for every feature later ported to Wear. The numeric ripple shows 30+ collateral renames; the qualitative cost is a per-feature decision-and-refactor cycle.

**What the numbers hide (P7):**
- ~140 KMP modules were semantically reviewed under §D6b actions B/D/E. 138 of them ended up at 0 LOC. The "138 modules: zero change" outcome is **not** zero cost — it is the cost of confirming, per-module, that no change is needed. This labour scales with codebase size and is invisible to LOC-based metrics.

**Cross-references:** §4.6 (NS theorems — AVT and SoC observations above), §4.9 (threats — Wear runtime untested for non-auth driven adapters; per-feature mobile-extraction is unbounded future work; design-system bridge is incomplete).

**Honest framing.** The author's prior was "the result will probably be unflattering". The numeric ripple is more flattering than expected (0 LOC across many critical layers), but the qualitative findings (Koin aggregate split, mobile-extraction per Wear-affected feature, invisible review labour) deliver the unflattering content. The §4.4 prose must report both faithfully — neither overclaiming the clean numeric table nor dismissing the architectural wins.
