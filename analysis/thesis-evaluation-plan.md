# Thesis Evaluation Methodology — Proving Code Sharing & Evolvability in Snag

## Context

The thesis *"Vývoj multiplatformního snagging systému: Evolvabilní maximalizace sdílení kódu"* (CTU FIT) states its goal as **maximizing code sharing across frontend platforms and the backend while preserving evolvability**. The current thesis text has a Design chapter (kap. 2), an Implementation chapter (kap. 3), and a Publication chapter (kap. 4), but **no formal evaluation chapter and no quantitative evidence** backing the core claims. Figure 1.4 presents a theoretical file-count curve, not measured data. A committee will ask "how do you know the goals are met?" — today there is no defensible answer.

The implementation itself is already strong evidence:
- **190 Gradle modules** across 6 running platform targets (Android, iOS, JVM desktop, JS web, WASM web, JVM backend).
- **Hexagonal architecture is formally enforced by the build** via four rule families (`CATEGORY_DIRECTION`, `PLATFORM_DIRECTION`, `HEXAGONAL_DIRECTION`, `ENCAPSULATION_DIRECTION`) defined in `build-logic/.../architecture/ArchitectureRules.kt` and executed by `archCheck`. The sharing is **structural, not voluntary**.
- **Convention plugins** (`build-logic/.../configuration/MultiplatformModuleSetup.kt`, `FrontendMultiplatformModuleSetup.kt`, `BackendModuleSetup.kt`) define a source-set hierarchy (`commonMain`, `mobileMain`, `nonWebMain`, `nonAndroidMain`, `nonJvmMain`, `webMain`, `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`, `backendMain`) that directly expresses *"platform reach per line of code"*.
- **Code is shared between frontend and backend at multiple layers, not only on the wire.** Modules applying `snagMultiplatformModule` (or its contract variant) compile to every platform including the JVM backend; these span foundational primitives, pure domain interfaces, business rules, application-level model interfaces, sync metadata, and HTTP wire DTOs. The contract modules are one seam of FE↔BE sharing but not the whole story.
- **A recent feature** (ProjectPhoto, commit `b5365d611`) touching ~95 files and ~4200 LOC across all layers is available as a retrospective case study.

This plan proposes a new **Kapitola 4: Vyhodnocení** (the current Publication chapter becomes kap. 5), the tooling to produce its data, and a **live evolvability experiment** adding a **Wear OS** target. Deliverables are in **Czech** to match the rest of the thesis.

---

## Reframing the goal: two measurable outcomes, architecture as the enabler

"Maximization" is unfalsifiable and will be attacked at defense. The evaluation chapter reframes the thesis goal around **two outcomes that the architecture is designed to produce**, and treats the architecture itself as the *mechanism*, not as something to measure in its own right. Measuring architecture separately would be tautological — the build enforces it, so by definition it is there — and quality attributes like "hexagonal seam count" or "layer discipline" are arbitrary proxies for what the architecture is *for*. What is interesting is whether the architecture actually produced the outcomes it was designed to produce.

| # | Outcome | Metrics |
|---|---|---|
| **O1** | **Sharing** — how much of the production code lives in source sets that reach multiple platforms | Share ratio (`LOC(code reaching ≥2 platforms) / LOC(total production)`) broken down by module, layer, category; platform reach histogram; breakdown of LOC by how many platform binaries it reaches (one for every distinct reach count observed, from backend-only through five-frontend through all-six) |
| **O2** | **Evolvability** — what adding a new platform / new feature / new domain entity costs in practice, measured as ripple effect and tested for combinatorial scaling | Wear OS live experiment + `feat/inspections` reverse-removal experiment + ProjectPhoto retrospective + optional synthetic iOS-only entity extension + optional DVT synthetic test: files touched outside the change's own modules, LOC added per layer, ripple bucket decomposition (local / intrinsic / collateral). Combinatorial effect is tested *structurally on the current codebase*, not by observing history, because the architecture itself evolved substantially over the thesis timeline and historical commits would conflate ripple-of-the-change with ripple-of-an-immature-architecture. |

Each outcome is measured, the raw distributions are shown as figures and tables, and the chapter discusses what the numbers mean — including cases where the architecture did not share as well as expected or where the ripple had non-zero collateral bleeding. The discussion is the deliverable, not a pass/fail verdict.

**Architecture is referenced as the *mechanism*, not measured.** The methodology section (§4.1) cites `ArchitectureRules.kt` verbatim to explain *how* "sharing-first" is enforced — the four rule families (`CATEGORY_DIRECTION`, `PLATFORM_DIRECTION`, `HEXAGONAL_DIRECTION`, `ENCAPSULATION_DIRECTION`) and the `archCheck` task that runs them — and that is the last time the architecture is the *subject* of a measurement. Every number in the chapter is an outcome number. If the architecture enables high sharing and low ripple, the architecture is vindicated. If it does not, the architecture is questioned — in both cases the numbers that matter are the outcome numbers, not architecture-internal counts.

**Terminological note.** The label `commonMain` in a Kotlin Multiplatform module does *not* mean "all Snag platforms". The platforms reached by a given `commonMain` source set depend on which convention plugin the module applies:

- **Full-platform KMP modules** (applying `snagMultiplatformModule` or `snagContractDrivingBackendMultiplatformModule`) compile to all six platforms — Android, iOS, JVM desktop, JS web, WASM web, and the JVM backend.
- **Frontend-only KMP modules** (applying `snagFrontendMultiplatformModule` or its driving/driven/network variants) compile to the five frontend platforms — Android, iOS, JVM desktop, JS web, WASM web.
- **Backend-only modules** (applying `snagBackendModule` or its driven/impl-driving variants) use the plain `main` source set on a JVM target and compile to the JVM backend only.

To avoid confusion the chapter uses the phrases "shared across all platforms", "shared across all frontend platforms", and "backend-only" rather than "in `commonMain`". The headline **platform reach per line** metric handles this natively: it counts the concrete target binaries each line is compiled into, using the plugin applied to the module as its source of truth. Measurements based on source-set directory names alone would conflate the three categories and are not used.

**Theoretical grounding for O2 — Normalized Systems Theory (Mannaert & Verelst).** Evolvability in the NS sense is the absence of *combinatorial effects*: a change whose impact scales with the size of the system rather than with the magnitude of the change is an **anomaly**, because any such effect makes the system increasingly expensive to modify as it grows. NS theory prescribes four theorems — *Separation of Concerns*, *Data Version Transparency*, *Action Version Transparency*, and *Separation of States* — as stability conditions that eliminate combinatorial effects. This thesis adopts NS as the vocabulary for evolvability: the chapter frames every evolvability measurement as an attempt to detect or rule out a combinatorial effect, and maps each NS theorem to a concrete architectural mechanism in Snag (hexagonal layering for SoC, `Versioned`/sync contracts for data version transparency, port interfaces for action version transparency, stateless use cases + `StateFlow`-scoped ViewModels for separation of states). The mapping is itself a contribution: NS literature rarely instantiates the theorems in a Kotlin Multiplatform codebase.

---

## Methodology pillars

### Pillar 1 — Sharing quantification (primary metrics)

Drop the "effective reuse multiplier" framing (marketing, not science). Use:

1. **Share ratio** — single number per module / per feature / per layer / project-wide. Primary headline metric.
2. **Platform reach per line** — histogram: for each production LOC, how many platform binaries it is compiled into (1–6). Weighted mean as a scalar. Honest, no counterfactual.
3. **Structural sharing score** — `# modules in platform-neutral plugin / # modules total`, broken down by category. Module-level counterpart to the line-level share ratio.
4. **Architecture-layer × source-set LOC matrix** — a 2D cross-tabulation: rows = architecture layers (business/model, app/model, ports, app/impl, driving/api, driving/impl, driven/impl, contract), columns = source sets ordered by platform reach (commonMain → nonWeb → mobile → ios/android/jvm → web → backend main). Each cell is the total LOC in that (layer, source set) combination across all modules; row and column totals in the margins. Rendered as a heatmap with cell shading by LOC and a numeric annotation per cell. This is the single most informative view of where the project's code actually lives: it shows simultaneously which architecture layers dominate the codebase, which source sets those layers concentrate in, and where any unexpected "bleeding" into platform-specific source sets occurs (e.g., an app/impl layer with surprisingly non-trivial iosMain content). Produced by `analysis/figures.py` from the joined `SharingReportTask` + tokei data, no manual aggregation.

Metrics that sound attractive but are **tautological, dominated by a structural pattern, or measure the mechanism instead of the outcome** are dropped:

- *Per-feature uniformity / coefficient of variation across features.* The architecture rules already force every feature into the same layer and platform partitioning; uniformity across features is structurally guaranteed, so measuring it just reports the rules back to the reader.
- *Contract round-trip coverage* (DTOs referenced from both FE and BE). Contracts live in KMP modules depended on by both sides — the dependency direction is enforced by the category rules. No empirical "proof of use" is needed.
- *Expect/actual count & ratio.* In practice almost every Gradle module in Snag declares an `expect`/`actual` Koin DI module pair, so the count is dominated by a uniform DI pattern and says nothing about whether *domain logic* needs to branch per platform. The share-ratio and platform-reach metrics already capture the shape of platform branching without this noise.
- *Auto-wiring coverage.* Auto-wiring is a convenience mechanism that makes implementing the architecture ergonomic; it is not itself an outcome of the architecture. Like `ArchitectureRules.kt`, it is referenced in the methodology paragraph as part of *how* sharing-first is enforced, but it is not separately measured.

Per-feature numbers are still shown in the **headline stacked-bar figure** for visual scale, but they are not framed as a variance analysis.

An **"avoidance multiplier"** (commonMain LOC × N platforms) is reported *separately and explicitly labelled as an illustrative upper bound*, not a measurement.

### Pillar 2 — Evolvability: feature-level case study + ripple analysis (current state, reverse-removal methodology)

The codebase architecture itself evolved substantially over the thesis timeline — early modules were not yet organized under the current hexagonal + category + encapsulation rules, and convention plugins, auto-wiring, and the `archCheck` task were introduced iteratively. Historical commits would therefore conflate *the cost of the change* with *the immaturity of the architecture at the time of the change*. The evaluation only uses the current, stable architecture.

**Primary case study — reverse removal of `feat/inspections`.** The ideal case study for feature-addition evolvability is a feature whose existence touches *other parts of the system*, not a feature whose footprint is contained entirely within its own module tree. ProjectPhoto is a poor fit here: photos are coupled to projects and live inside `feat/projects`, so the measured "ripple" is dominated by intra-feature edits rather than cross-feature impact. `feat/inspections` is a much better candidate — it is its own top-level feature with a many-to-one relationship to `projects` and genuine cross-feature touchpoints (authorization, sync registries, navigation, reporting, cross-feature queries). There is, however, no clean *forward* commit that reflects the current architecture, because inspections was added during the architectural evolution.

The methodology is therefore a **reverse experiment**: on a throwaway branch, delete the entire `feat/inspections/*` subtree and then repair the codebase until `./gradlew check` passes again. Every file that needs to change during repair is a file that the existence of the feature *currently requires* — by symmetry with adding the feature from scratch under the current architecture, the reverse ripple is the forward cost, in the same files and approximately the same magnitude. This sidesteps the "no clean forward commit" problem without fabricating history.

Measurement procedure:
1. Create branch `experiment/remove-inspections`.
2. Delete all modules under `feat/inspections/*`; delete `include(":feat:inspections:...")` entries in `settings.gradle.kts`.
3. Run `./gradlew check` and collect the full list of compilation/test failures.
4. Repair the codebase file-by-file, tracking every touched `(module × source set)` unit as an entry in the repair log. Stop when `check` passes.
5. Classify each repair-log entry into the local / intrinsic / collateral ripple buckets:
   - *Local* is empty by definition (the local files were deleted).
   - *Intrinsic ripple* = pre-existing modules that referenced inspections for structural reasons that any new feature would replicate (DI aggregators, navigation registries, sync entity enums, cross-feature query composition).
   - *Collateral ripple* = references that were not structural — e.g., hard-coded imports that could have been behind an interface.
6. Report the reverse-ripple table (count, source-set distribution, blast-radius annotation) as the forward-cost estimate for adding a standalone feature of comparable scope.
7. Restore the branch at the end; the repair is never merged.

**Secondary case study — forward ProjectPhoto retrospective.** Commit `b5365d611` is still analyzed as a *complementary* data point: it represents the cost of extending an *existing* feature with a new sub-entity (photos inside projects), which is a different and simpler evolution vector than adding a standalone feature. Reporting both side-by-side lets the chapter distinguish the two scenarios instead of conflating them. Classification is the same as Pillar 2's ripple methodology.

**Tertiary data point (optional but highly recommended) — synthetic platform-specific extension.** Pick one platform (say iOS) and introduce a requirement that is meaningful only on that platform — for example, an iOS-only property on a `Project`-family type. Implement it by adding a new `IosProject` interface that extends the existing lowest-level `Project` model with the extra field, plus the corresponding platform-specific port and adapter, without touching any shared model. Measure:
1. Whether the extension can be introduced entirely inside iOS-only source sets (ideal: no `commonMain` or cross-platform module modified).
2. Where the architecture forces a touch anyway — e.g., a shared factory, a sync handler, an aggregation point that enumerates entity kinds.
3. Whether other platforms remain oblivious: do Android/JVM/web builds compile unchanged, and do their use cases continue to operate on the base `Project` model as if nothing happened?

This is a stronger test than a plain field addition because it exercises the *extension point* that enables per-platform domain branching — the case where platform specificity is genuinely required by the business, not merely incidental to the UI framework. It also directly evaluates NS's Separation of Concerns at the *platform* axis, which is distinct from the feature axis tested by the inspections reverse experiment.

**Ripple effect measurement (per change, NS-theoretic, KMP-adjusted).** Classical NS theory measures ripple at the module level. In a Kotlin Multiplatform codebase a single module contains multiple source sets that can each reach a different set of platforms — `commonMain` of a frontend module reaches five platforms, `iosMain` of the same module reaches only one. A naïve module-level count obscures this: "one module touched" is not informative if we do not know whether the touch was in a shared source set or a platform-specific one. The measurement unit in this thesis is therefore **(module × source set)** — each touched source-set directory of a touched module is a separate ripple unit, and each unit records which source set directory the touch was in.

For every change studied, partition the touched units into three buckets on the NS axis:

| Bucket | Definition | Interpretation |
|---|---|---|
| **Local** | Units inside modules that were *created for* the change (the new entity's own modules) | Intrinsic cost — cannot be smaller than "the change itself" |
| **Intrinsic ripple** | Modifications to *pre-existing* modules that must happen by definition of what the change is (e.g., an aggregation point, a shared enum of entity types) | Unavoidable given the current architecture; interesting because each instance is a candidate "anomaly site" for NS refactoring |
| **Collateral ripple** | Modifications to pre-existing modules that are *not* intrinsic — coupling the change did not semantically require | The quantity NS theory says should be zero; every nonzero case is named and discussed |

**The direct cost of a touch is the same regardless of source set** — one changed file is one changed file. What differs is the *potential secondary ripple*: a change to a more general source set exposes more downstream consumers to the modification, so it is a *riskier* touch, not a more expensive one. To separate this out cleanly the chapter reports three quantities per change:

1. **Direct ripple** — count and LOC of (module × source set) units actually modified, partitioned into the local / intrinsic / collateral buckets. This is the raw NS ripple, no weighting.
2. **Source-set distribution of the direct ripple** — within each bucket, which source sets were touched (how many units in `commonMain`, in `nonWebMain`, in `iosMain`, in `backendMain`, etc.). This answers "*where* in the platform hierarchy did the ripple land" without inflating the raw count.
3. **Potential blast radius per touched unit** — for each touched unit, the size of its downstream dependency closure (how many other `(module × source set)` units transitively consume it). This is computed statically from the module dependency graph and the source-set hierarchy; it is *not* added to the ripple count but is reported as a per-unit annotation. The intuition: a touch in `commonMain` of `feat/projects/business/model` has a larger blast radius than a touch in `iosMain` of `feat/projects/fe/driven/impl` because more downstream source sets depend on it. Whether that blast radius *actually* materialized as a secondary ripple is already captured in quantity (1) — if consumers had to change, they show up as more touched units.

Together (1), (2), and (3) describe the change without conflating "how much code was touched" with "how broadly the touch could have propagated". A clean change has small direct ripple *and* a concentration of the ripple in source sets whose blast radius is bounded; a risky change is one where the direct ripple landed in high-blast-radius source sets, even if small in raw count. The chapter reports all three, names every unit in the intrinsic and collateral buckets with its source set and blast radius, and discusses whether the intrinsic bucket could be eliminated by a further refactor.

**Combinatorial effect test (structural, current state only).** Rather than plotting ripple-vs-project-size over git history, combinatorial scaling is tested statically against the current architecture. For each file in an intrinsic ripple bucket the question is: **does this touch recur once per *future* change of the same kind, or is it a one-time cost that is not paid again?**

1. For each file in the inspections reverse-removal intrinsic bucket (and, for comparison, the ProjectPhoto intrinsic bucket), classify it as either:
   - **Recurring** — the file would have to be touched *again* for every next feature/entity added to the system (for example, an aggregation point that enumerates feature modules, or a sync-layer type switch). Each such file is a combinatorial anomaly: the Nth feature pays the same cost as the first, so total lifetime cost grows with the product (features × recurring files).
   - **Fixed** — the file was touched because this particular feature required it, but adding a *different* feature later would not touch it again. Not combinatorial.
   
   Name every file in both sub-buckets in the thesis. The count of recurring files is the combinatorial-effect metric on the feature-addition axis.
2. For the Wear OS experiment (Pillar 3), the **per-module-tree ripple scaling test** gives the complementary measurement on the platform-addition axis: adding the platform, does each existing `feat/*`, `lib/*`, and `core/*` module tree contribute its own mandatory touch (combinatorial — Nth platform pays N × trees), or does the addition land on a fixed set of central files regardless of tree count (not combinatorial)?

Together these two tests cover the two combinatorial axes the assignment cares about — adding entities and adding platforms — without requiring the historical observation that the codebase's own evolution would contaminate.

**NS theorem mapping (discussion layer).** Each of the four NS theorems is connected to the *mechanism* in Snag that is expected to prevent a particular combinatorial effect, with the mapping evaluated against measured data where possible and by explicit test scenarios where history is not informative:

- **Separation of Concerns** → category rules + hexagonal layer rules in `ArchitectureRules.kt` + per-feature module trees. Evaluated by: does a change to feature X ripple into feature Y? (Should be zero except through well-defined cross-feature contracts; measured via the ripple classifier on the inspections reverse-removal experiment and the ProjectPhoto commit.)
- **Data Version Transparency** → `Versioned.updatedAt`, `SoftDeletable.deletedAt`, sync push/pull handlers, kotlinx.serialization contracts. **Cannot be evaluated from history** — field additions in the codebase were all written as mandatory (no default value), so observing them would only tell us that DVT was not *practiced*, not whether it is *architecturally supported*. Evaluated instead by a **synthetic test scenario** on a throwaway branch: (1) add a new field to an existing entity with a default value at every layer from `business/model` through `contract` to the storage adapters; (2) attempt to compile and test-run the rest of the codebase unchanged; (3) exercise the contract serializers against a payload that lacks the new field and one that includes it, to verify kotlinx.serialization handles both; (4) verify the `Versioned` sync mechanism still converges. The chapter reports: "DVT is architecturally available but requires disciplined use of default values at the wire and model layers; in the current codebase fields were added mandatory, so DVT is a latent property demonstrated by the synthetic scenario rather than an observed one." This is a meaningful distinction the thesis should highlight because it separates *what the architecture enables* from *what the developer practice has exercised so far*.
- **Action Version Transparency** → use-case-interface + impl split, port interfaces, convention-plugin auto-wiring. Evaluated structurally: callers depend on the interface, not the impl, so adding a new implementation of an existing use case cannot force callers to change. If time allows, complement with a synthetic test scenario: add a second `impl` for an existing use case interface on a throwaway branch, wire it, confirm callers compile unchanged.
- **Separation of States** → NS defines this theorem as *"the calling of an action entity by another action entity needs to exhibit state keeping of the return result"*, with heuristic manifestations including asynchronous communication and asynchronous processing. Snag satisfies this through its pervasive asynchronicity and reactive design: coroutine-based use cases, `StateFlow`/`Flow` for reactive state propagation, and the offline-first sync layer that decouples client actions from server acknowledgements. The chapter briefly describes these mechanisms as the Snag-side correspondence to the NS theorem, without drilling into component-level detail.

### Pillar 3 — Evolvability: live platform addition (Wear OS)

**The target is Wear OS (Android Wear), matching the thesis assignment's wording ("hodinky").** Wear OS is structurally close to the existing Android target — same JVM/ART runtime, same Kotlin, and Compose for Wear OS shares core concepts with Compose Multiplatform — so library feasibility risk is low while the narrative remains a genuine new platform addition. **There is no fallback ladder.** Alternatives like watchOS with SwiftUI would run into the exact same "new UI toolkit" problem without buying any extra evidence, so the plan commits to Wear OS and handles any obstacles by refactoring inside Snag rather than pivoting to a different platform.

**Narrative:** *"We added a Wear OS target. Business, application, ports, contract, and driven layers were reached unchanged — Wear OS is Android at the runtime level, so the existing Android-target driven adapters (database, storage, network) apply as-is. The driving layer is where the platform-specific work lives. We attempt to reuse as much of the existing `composeApp` driving code as possible and only write Wear-specific code where the form factor or Wear component set forces it. Where the current driving layer is too monolithic to share only parts of, we take that as a finding: the driving layer's source-set split would benefit from a finer-grained refactor that enables partial sharing with Wear OS, and we document this as future work without executing the refactor in the experiment itself."*

**Scope cap:** one Wear-native screen wiring through to the existing shared business logic, reusing as much of the `composeApp` driving layer as feasible without refactoring. The point is to demonstrate that the shared stack runs end-to-end on Wear OS; fully optimal UI reuse is out of scope.

**Feasibility spike (Phase 0, days 1–2)** — short, not a blocker:
- Add a new `wearApp` Android application module configured for Wear OS.
- Pull in `androidx.wear.compose:compose-material`, `compose-navigation` for Wear OS, and any other Wear-specific libraries.
- Wire it to depend on the existing shared KMP feature modules plus, as much as possible, on parts of `composeApp` for the driving layer.
- **Spike outputs:** (a) confirmation that the shared layers compile for the new target unchanged, (b) an inventory of which `composeApp` driving components can be reused as-is on Wear OS and which would require either a Wear-specific implementation or a project-wide driving-layer source-set split that the experiment does not perform.

**Measurements collected during the experiment** (descriptive, no thresholds):

| Dimension | What is measured |
|---|---|
| LOC added in the new `wearApp` module **plus LOC added in any newly created Wear-OS-specific source sets of existing feature, lib, core, or `composeApp` modules** (e.g., `wearMain` source sets introduced for platform-specific drivers, ViewModels, or UI fragments) | Total platform-specific cost of the new target, regardless of which module hosts it |
| LOC added/modified in pre-existing *shared* source sets | "Bleed" into shared code — expected to be near zero |
| Files modified outside the new target's own modules and own source sets | Architectural leak indicator |
| Amount of existing shared LOC that now additionally runs on Wear OS via the new driving adapter, unchanged | The headline win — business, app, ports, contract, and driven layers all reached by the new platform without modification |
| **Ripple decomposition** (local / intrinsic ripple / collateral ripple — same buckets as Pillar 2) | Per-file attribution so every modified pre-existing file is accounted for |
| **Per-module-tree ripple scaling** — does adding the Wear OS target touch something inside each existing feature, lib, and core module tree? | Detects combinatorial effect at the *platform* axis across every top-level category, not only `feat/*`: a well-normalized system should touch $O(1)$ central points when a platform is added, not $O(\text{feat} + \text{lib} + \text{core})$. |

The chapter reports these numbers and discusses their interpretation; it does not declare the experiment a success or failure against a prior target. The ripple decomposition specifically tests whether the *platform addition* axis is combinatorial in the same sense that the *feature addition* axis was tested in Pillar 2 — together the two give NS-theoretic coverage of the two most common evolution vectors in the assignment ("adding platforms" and "expanding the domain").

**Comparative framing:** state the hypothetical pure-native Wear OS implementation baseline as "business + data + sync logic would all need reimplementation even though Wear OS is Android-based" and report the shared LOC as an **upper bound** on the delta that was avoided — explicitly labelled as illustrative, not an estimate.

### Pillar 4 — Evolvability: technology swap analysis (structural, not empirical)

Enumerate swap seams without actually swapping:
- HTTP client — `SnagNetworkHttpClient` in `lib/network/fe/api` (Ktor adapter).
- Frontend database — SQLDelight behind per-feature `Db` ports.
- Backend database — Exposed behind per-feature `Db` ports.
- File storage — `LocalFileStorage` with platform-specific adapters.
- Auth provider — `AuthTokenProvider` (OIDC/Mock adapters).

For each seam: port file path, list of current adapters, count of call sites that would *not* need to change if swapped. Framed explicitly as a **structural argument**, not empirical proof. Acknowledged in threats-to-validity.

*(Quality section dropped. Test coverage and static-analysis metrics do not bear on the two outcomes the chapter evaluates — sharing and evolvability — and mixing them into the chapter dilutes the argument. The existing Implementation chapter already covers the project's testing and static-analysis setup; re-reporting those numbers under "evaluation" would suggest they are evidence for the thesis goals, which they are not.)*

---

## Evaluation chapter structure (Kapitola 4: Vyhodnocení)

In Czech. Placeholder figures/tables created **before** measurements to lock scope.

1. **4.1 Metodologie** — the two outcomes (sharing, evolvability) and their metrics; operationalization table (outcome → metric → data source → **script that produces the value** → measured value); the enabling mechanism, citing `ArchitectureRules.kt` verbatim and explaining briefly that the build rejects any dependency that violates the four rule families plus the `archCheck` task output for the submission SHA (one short paragraph, not a section); definitions of ripple effect and combinatorial effect (Mannaert & Verelst's NS theory, cited); tool choices (tokei pinned version); indexing scope (absolute source-set paths emitted by the `sharingReport` Gradle task → implicit absence of generated code / `build/` / `.gradle/` / library binaries, with no explicit filter) — LOC is reported as raw production Kotlin line count including Koin DI scaffolding; expect/actual declarations are not reported as a separate metric because the construct covers both uniform DI modules and legitimate platform abstractions in driving/adapter layers (e.g., `SaveReportEffect`, `SqlDriverFactory`) and share-ratio + platform-reach already capture the shape of platform branching; **reproducibility statement: every number printed in the chapter is produced by a committed script or a committed Gradle task — no hand-counted numbers, no manual LOC tallies, no screenshot measurements. Judgment-based classifications (ripple buckets, recurring-vs-fixed anomaly classification) are captured in committed YAML/markdown files and referenced by path from the tables that consume them, so a second reader can reproduce the figures exactly and challenge the classifications explicitly.** Threats to validity up front.
2. **4.2 Kvantifikace sdílení** — share ratio per module/layer/category, platform reach histogram, structural sharing score, **layer × source-set LOC heatmap matrix** (headline "where does the code live" figure).
3. **4.3 Případová studie rozšiřitelnosti na úrovni funkcionality + ripple analýza** — primary: reverse-removal experiment on `feat/inspections` (delete the feature on a branch and repair the codebase to measure the cross-feature touch surface); secondary: forward ProjectPhoto retrospective as a complementary "extending an existing feature" data point; optional tertiary: synthetic iOS-only entity extension as a platform-axis test. **Ripple decomposition (local / intrinsic / collateral) with source-set annotation and blast-radius per touched unit**; **structural combinatorial-effect test** — naming every file in the intrinsic bucket and asking which of them would recur for future feature additions. No historical commit batch — the codebase architecture itself evolved during the thesis and historical ripples would measure the past architecture, not the present one.
4. **4.4 Případová studie přidání platformy (Wear OS) + ripple analýza** — feasibility spike results, what compiled unchanged across business/app/ports/contract/driven layers, a single Wear-native screen wired end-to-end as the proof of execution, ripple decomposition, **per-feature ripple scaling test** (is the platform-addition axis combinatorial?), one Wear OS emulator screenshot.
5. **4.5 Analýza technologických švů** — enumeration of ports that enable technology swaps, tied to Action Version Transparency.
6. **4.6 NS teorémy a naměřená data** — short discussion section mapping the four NS theorems (SoC, DVT, AVT, SoS) to the Snag mechanisms that are supposed to enforce them, interpreted against the ripple numbers from §4.3 and §4.4 (does the data support the claim that each theorem holds in practice?).
7. **4.7 Výsledky (synthesis)** — the central "what did we find" section of the chapter. Pulls the numbers from §4.2–§4.6 into a single synthesized picture and states, explicitly and in order of importance, what the measurements say about each of the two outcomes: (i) the headline sharing numbers (share ratio, platform reach distribution, structural sharing score), what surprised the author, and where the shape of the distributions reveals low-sharing outliers and why; (ii) the headline evolvability numbers (inspections reverse-removal ripple, ProjectPhoto forward ripple, Wear OS platform-addition ripple, optional synthetic extensions), the combinatorial-effect findings (which intrinsic-ripple files recur per-feature and therefore represent anomaly sites), and whether the architecture mechanisms from §4.6 held in practice. This section is where the thesis states its bottom line — a reader who opens the chapter and reads only §4.7 should come away knowing the answer.
8. **4.8 Komparativní kontext** — literature review (Cash App, JetBrains customer stories, Touchlab reports, published EMSE papers on multiplatform sharing, plus NS-theory empirical case studies where available), positioning of Snag's numbers against external benchmarks.
9. **4.9 Diskuse a hrozby pro validitu** — LOC metric limitations, single-developer bias, tool bias (tokei vs cloc), generated code, intermediate source set attribution, selection bias on the ProjectPhoto retro commit, the Wear OS experiment reflects the author's ability, reverse-removal ≈ forward-addition symmetry assumption, **NS theorem mapping is an interpretation by the author and not a formal NS audit**, ripple bucket classification is judgment-based and should be reproducible by a second reader.

**Příloha A: Naměřená data (appendix at the end of the thesis).** Reprints the larger raw CSVs that the chapter references but does not display in-place: the full per-module sharing report (one row per `(module × source set)` unit with LOC, plugin, layer, encapsulation, platform reach), the full per-feature LOC breakdown, and the full ripple repair logs from each evolvability case study. Every appendix table is generated from `analysis/data/*.csv` by `figures.py` (or a sibling `tables.py`) into a LaTeX-ready file under `analysis/data-for-thesis/`, and the thesis includes those files via `\input{}`. No manual transcription. Smaller datasets (the layer × source-set heatmap numeric data, the ripple bucket counts per change) appear in-place in their respective sections rather than in the appendix.

---

## Tooling to build

All tooling lives in-repo so the measurements are reproducible. Nothing mutates production code.

### A. Gradle sharing-report task

**Files to create:**
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/analysis/SharingReportTask.kt` — walks `project.rootProject.allprojects`, uses the **existing** `ModulePathParser.kt` and `ModuleIdentity.kt` (do not reinvent), detects the convention plugin applied per module via `pluginManager.hasPlugin(...)`, enumerates source set directories, emits CSV + JSON to `build/reports/sharing/`.
- `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/analysis/SharingReportPlugin.kt` — registers the task on the root project. Applied to root only.
- `build-logic/src/test/kotlin/cz/adamec/timotej/snag/buildsrc/analysis/SharingReportTaskTest.kt` — unit tests following the existing pattern in `ArchitectureRulesTest.kt`.

**Output schema (CSV columns):** `module_path, category, platform, hex_layer, encapsulation, feature, plugin_applied, targets_compiled, source_set, source_set_loc_kotlin, source_set_file_count`.

### B. LOC counting wrapper

**Files to create:**
- `analysis/loc_report.sh` — shell wrapper invoking **tokei** (pinned to a specific version, documented in the chapter) per source-set directory. Joins with the `SharingReportTask` output via module path. Excludes `build/`, `.gradle/`, `generated/`.
- `analysis/tokei_version.txt` — pinned tool version.

### C. Single-commit ripple classifier

**Files to create:**
- `analysis/feature_retro.py` — takes a commit SHA *or* a pair of refs (so it works on real commits like `b5365d611` and on the `experiment/remove-inspections` reverse branch alike), runs `git diff-tree --no-commit-id --name-status -r <ref>`, and for each changed file derives a **(module × source set)** unit from its path. Classifies each unit by (feature, hex layer, encapsulation, source-set directory) by mirroring the parse logic from `ModulePathParser.kt` (Python reimplementation is fine — it's a deterministic path grammar). Joins with tokei LOC diffs. **Partitions modifications into the three NS ripple buckets (local / intrinsic / collateral)** using a rule set defined in a small YAML config (e.g., "files in a module whose path was first created in this commit → local; files matching known aggregation points like koin aggregation or sync entity-type registries → intrinsic; everything else → collateral — subject to author review"). For every touched unit, looks up its **potential blast radius** (count of downstream `(module × source set)` units that transitively consume it) from a precomputed dependency closure produced by task (D) below. Emits: (i) a raw direct-ripple count table grouped by bucket, (ii) a source-set distribution within each bucket, (iii) a per-unit annotation with blast radius.
- `analysis/dependency_closure.py` — builds the static dependency closure for every `(module × source set)` pair in the current codebase: given a unit U, compute the set of other units that transitively depend on U, accounting for the KMP source-set hierarchy (e.g., `nonWebMain` → `androidMain` + `iosMain` + `jvmMain`). Dumps the closure as a JSON index consumed by `feature_retro.py`. Runs once per measurement session on the current `main` SHA.
- `analysis/ripple_rules.yaml` — human-readable, reviewable rule set for bucket classification. Committed to the thesis repo so a second reader can reproduce or challenge the classification.

No historical batch tool, no scatter plot over git history — the combinatorial-effect test is structural against the current codebase, per Pillar 2.

### D. Figure generation

**Files to create:**
- `analysis/figures.py` — Python + matplotlib, consumes the CSV from (A) and (B), produces PDF/PNG for the thesis. Figures ranked by communication value:
  1. **Layer × source-set LOC heatmap matrix** — rows = architecture layers, columns = source sets ordered by platform reach, cells = LOC (color-shaded + numeric annotation), row and column totals in the margins. The "where does the code live" headline figure for §4.2. Rendered via `matplotlib.pyplot.imshow` with an annotation pass.
  2. **Stacked horizontal bar per feature** — 11 bars, segments = LOC by source set. Companion to figure (1), showing the same data sliced by feature instead of by layer.
  3. **Module treemap** — 190 modules, color = plugin type, grouping by category → platform → layer.
  4. **Sankey diagram** — source-set categories flowing to platform binaries.
  5. **Feature dependency DAG** for `:feat:projects`, colored by hex layer.
  6. **Before/after table** for the Wear OS experiment (rendered as a LaTeX table in the thesis), showing which layers were reached unchanged and what the one new Wear-native screen cost.
  7. **Platform reach histogram** — x = # of platforms a line reaches, y = LOC.
  8. **Ripple bucket stacked bar** per studied change (inspections reverse removal, ProjectPhoto, optional iOS-only entity extension, Wear OS experiment) — segments = local / intrinsic / collateral, raw file/LOC counts only. Companion **source-set heatmap** per change showing where in the platform hierarchy (commonMain / intermediate / platform-specific / backend) the ripple landed within each bucket.
- `analysis/data/` — generated CSV/JSON committed for reproducibility.
- `analysis/figures/` — generated PDF/PNG committed for thesis inclusion.
- `analysis/data-for-thesis/` — a typeset-ready snapshot of every CSV that the thesis text references, formatted for direct inclusion (e.g., as LaTeX `tabular` or `longtable` files generated by `figures.py` from the same CSVs). Every numeric claim in the chapter must trace to a row in one of these files. The thesis-side wiring is: in-place tables for small datasets that fit on a page (the layer × source-set heatmap data, the ripple bucket counts), and a **dedicated appendix at the end of the thesis ("Příloha A: Naměřená data")** that reprints the larger CSVs verbatim (per-module sharing report, full per-feature LOC breakdown, per-change ripple repair logs). Both in-place tables and the appendix are generated from the same `analysis/data/*.csv` source — no manual transcription, no risk of the thesis showing different numbers from the figures.

### E. Wear OS experiment

**Files to create on branch `experiment/wearos-target` only:**
- `wearApp/` — new Android application module targeting Wear OS, pulling in `androidx.wear.compose:*` and depending on the existing shared KMP feature modules (and on `composeApp` where verbatim UI reuse is possible).
- `wearApp/build.gradle.kts` — Wear OS build configuration.
- `wearApp/README.md` — how to build and run on a Wear OS emulator.
- **Measurement note file** — `analysis/wearos_experiment.md` — pre-registered hypotheses and observed results, layer-by-layer reach log.

---

## Critical files to reference (existing, do not modify outside the experiment branch)

- `/Users/timotej.adamec/StudioProjects/Snag/build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/architecture/ArchitectureRules.kt` — the **four formal architecture invariants**. Cited verbatim in §4.1 as the mechanism that enforces sharing-first; not the subject of a standalone section because the outcomes are what matter.
- `/Users/timotej.adamec/StudioProjects/Snag/build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/architecture/ModulePathParser.kt` — canonical module path grammar. The SharingReportTask and feature_retro.py must mirror its logic.
- `/Users/timotej.adamec/StudioProjects/Snag/build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/architecture/ModuleIdentity.kt` — canonical `ModuleIdentity` sealed hierarchy.
- `/Users/timotej.adamec/StudioProjects/Snag/build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/MultiplatformModuleSetup.kt` — source set hierarchy (mobile/nonWeb/nonAndroid/nonJvm/web), current targets (iosArm64, iosSimulatorArm64, jvm, js, wasmJs, android via plugin). Not expected to change for the Wear OS experiment because Wear OS is an Android application, not a new KMP target — but relevant context for the chapter's platform-reach discussion.
- `/Users/timotej.adamec/StudioProjects/Snag/build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/architecture/ArchitectureCheckSetup.kt` — registers the `archCheck` task.
- `/Users/timotej.adamec/StudioProjects/Snag/settings.gradle.kts` — authoritative list of 190 modules.
- `/Users/timotej.adamec/StudioProjects/Snag/composeApp/build.gradle.kts` — current frontend target inventory; useful for documenting which shared screens the `wearApp` can reuse verbatim and which must be re-authored with Compose for Wear OS components.
- Thesis source directory `/Users/timotej.adamec/Ctu/dp-thesis-timotej-adamec/` — new chapter goes here in Czech.

---

## Work order (phased, scope-locked)

**Phase 0 — Feasibility and chapter skeleton (days 1–2, blocking) — COMPLETE**
1. ✅ Wear OS feasibility spike. Executed on branch `experiment/wearos-feasibility-spike`; succeeded after three iterations, all repairs scoped to files inside the new `wearApp/` module. Full log in `analysis/wearos_experiment.md`. Target confirmed as Wear OS.
2. ✅ `Kapitola 4: Vyhodnocení` skeleton inserted into `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` ahead of the existing "Vydání aplikace" chapter (which becomes ch. 5). All nine sections 4.1–4.9 stubbed.
3. ✅ §4.1 operationalization table populated with the O1/O2 metrics, data sources, producing scripts, and TBD measured-value column. Reproducibility statement with the exact reproduction command sequence included. Four `ArchitectureRules.kt` invariants cited verbatim. Appendix "Naměřená data vyhodnocení" added to `text/appendix.tex` with `\input{}` placeholders for the raw CSV dumps.

**Phase 1 — Measurement tooling (week 1) — COMPLETE**
1. ✅ `SharingReportTask` + `SharingReportPlugin` + `SharingReportRowBuilder` (pure row-building logic) + 60 `kotlin.test` cases covering every `ModuleIdentity` subtype and every distinct shape in `settings.gradle.kts`. Reuses `ModulePathParser` — no reinvention. Registered as the `snagSharingReport` convention plugin applied to the root project, walks `rootProject.subprojects` via `gradle.projectsEvaluated`, emits `build/reports/sharing/sharing_report.csv`.
2. ✅ `analysis/loc_report.sh` with tokei pinned to `14.0.0` via `analysis/tokei_version.txt`. Runs tokei per source-set directory, extracts Kotlin `code` LOC and file counts, joins with the sharing report into `analysis/data/sharing_report_with_loc.csv` — the canonical table downstream consumers read.
3. ✅ `analysis/figures.py` with `pandas` + `matplotlib` (pinned in `requirements.txt`). Implements the headline **§4.2 layer × platform-set LOC heatmap** (`analysis/figures/fig_4_2_layer_platform_set_heatmap.pdf`). Other figures stubbed for Phase 4.
4. ✅ **Platform-reach derivation.** The naïve `source_set` name is ambiguous — `commonMain` of a full-platform KMP module reaches all 6 platforms, but `commonMain` of a frontend-only KMP module reaches only 5 (no backend). The Gradle task derives a stable `platform_set` label from `(plugin family, module platform classification, source set)` encoded in `PlatformReach.kt`. The classifier also respects the architectural platform-direction rule: a module with `platform=fe` in its path is architecturally scoped to frontend *even if* it applies the full-platform base plugin, because the category rules forbid backend modules from depending on it. Marker plugin probing (`com.android.application`) handles the `:androidApp` case. Regression tests cover every distinct plugin-family × source-set combination plus the stacked-plugin trap.
5. ✅ **Smoke-run on `main`.** 190/190 modules covered, 310 `(module × source set)` rows, 48,381 total Kotlin LOC (31,773 production). Pipeline is idempotent (byte-identical output on rerun). `./gradlew check` still passes.
6. ✅ **Headline §4.2 finding (preview, for the eventual Phase 4 writeup).** Of 31,773 production LOC:
   - **980 LOC (3.1 %)** is truly shared across all 6 platforms including the JVM backend — the `all` bucket. Concentrated in `contract`, `app/model`, `business/model`, and `ports` layers.
   - **20,838 LOC (65.6 %)** is shared across the 5 frontend platforms but **not** the backend — the `frontend` bucket. Concentrated in `driving`, `driven`, and `app` layers (i.e. the UI composition, use-case orchestration, and frontend adapters).
   - **6,988 LOC (22.0 %)** is backend-only.
   - **2,967 LOC (9.3 %)** lives in platform-specific or intermediate source sets (web, nonWeb_fe, android, ios, jvm_desktop, etc.).
   
   The previous naïve view (aggregating by raw source-set name) would have reported `commonMain ≈ 21,818 LOC`, conflating the 980 LOC of true FE↔BE sharing with the 20,838 LOC of frontend-only sharing and overstating cross-tier reuse by roughly **19×**. The `platform_set` column is the fix.
7. ✅ Phase 1 tooling shipped in PR #223 against `main`. No thesis prose written — §4.2 Czech prose is deferred to Phase 4 so numbers stabilize before being written up.

**Phase 2 — Feature-level evolvability case studies (week 2)**
1. `feature_retro.py` + `dependency_closure.py` + `ripple_rules.yaml`.
2. **Primary:** create branch `experiment/remove-inspections`, delete the feature subtree, repair until `./gradlew check` passes, record every touched file and the reason in a repair log. Run `feature_retro.py` on the branch diff. Restore branch without merging.
3. **Secondary:** run `feature_retro.py` on commit `b5365d611` (ProjectPhoto) for the "extending an existing feature" data point.
4. **Optional tertiary:** create a synthetic iOS-only entity-extension branch (a platform-specific subtype of an existing entity) for the platform-axis evolvability test.
5. **DVT synthetic test scenario** on a throwaway branch: add a new field with a default value to an existing entity through the full layer stack, compile and test-run the rest of the codebase, verify the contract serializers handle payloads with and without the new field. Record whether consumer code had to change (should be zero if DVT holds).
6. For each change, walk the intrinsic ripple bucket file-by-file and classify each as "would recur per feature" vs "fixed cost". Draft section 4.3.

**Phase 3 — Live Wear OS experiment (weeks 3–4)**

Phase 0 status: **complete**. Feasibility spike succeeded on branch `experiment/wearos-feasibility-spike` (kept alive as the long-lived Wear OS experiment branch). The `wearApp` Android application module already exists on that branch with a minimal "Snag Wear" Wear-native screen, and the pre-registered hypothesis was confirmed: every shared business / app / ports / contract / driven module compiled for the new Android (Wear OS) target without modification, and all three build-repair iterations were scoped to files inside `wearApp/`. Full repair log, three-attempt failure/diagnosis/fix chain, and the "OIDC manifest contract leaks through `:composeApp`" future-work flag are recorded in `analysis/wearos_experiment.md`.

Phase 3 proceeds on the same branch:
1. Continue on branch `experiment/wearos-feasibility-spike`. Do not rename or rebase — the branch is the experiment.
2. Extend the existing minimal `wearApp` into a single Wear-native screen wired through to the existing shared use cases (e.g., a read-only project list). Reuse `composeApp` driving components where feasible and author Wear-specific UI only where reuse would force a project-wide refactor.
3. Record an inventory of `composeApp` components that could/could not be reused, and why. Flag any case where partial reuse was blocked by the current driving-layer source-set split as future work.
4. Measure per the measurement table; take one emulator screenshot. Draft section 4.4.

**Phase 4 — Sharing quantification writeup (week 5)**
1. Re-run `SharingReportTask` + `loc_report.sh` on `main`.
2. Generate all figures.
3. Draft section 4.2.

**Phase 5 — Comparative context, quality, threats, discussion (week 6)**
1. Literature search (Cash App, JetBrains, Touchlab, academic papers on multiplatform sharing).
2. Draft sections 4.5, 4.6, **4.7 (results synthesis — the headline section)**, 4.8, 4.9.

**Phase 6 — Defense preparation (week 7)**
1. Self-attack: write the 10 hardest committee questions and answer them in the threats-to-validity section.
2. Advisor review cycle.

**Sequencing principle:** tooling (phase 1) precedes retro (phase 2) precedes live experiment (phase 3), because the live experiment is the riskiest item and the retro validates the tooling on known data first. Prose is drafted **in parallel** with each phase to avoid a writeup explosion at the end.

---

## Risks, ranked

1. **(LOW) Wear OS is low-risk and there is no fallback.** Wear OS is Android at runtime, so business/app/ports/contract/driven layers all apply unchanged; the experiment is one Wear-native screen on top, reusing as much of `composeApp` as feasible. **If reuse of a driving component is blocked by the current driving-layer source-set granularity, the blocker is documented as future work** (a project-wide refactor of the driving source-set split), and the experiment proceeds by authoring the component Wear-specifically — not by pivoting to a different platform. watchOS/SwiftUI would have the same "new UI toolkit" problem without adding evidence, so fallback is not part of the plan.
2. **(HIGH) Measurement tooling scope creep.** A Gradle task that walks modules, joins tokei output, and computes metrics can balloon. **Mitigation:** scope `SharingReportTask` to "emit CSV, nothing else"; do all charting offline in Python; aggressively defer any feature not appearing on a chart.
3. **(MEDIUM) LOC metrics attacked at defense.** **Mitigation:** report multiple metrics (LOC plus module counts plus ripple file counts), cite tool versions, explicit exclusions, ratio-within-project framing rather than savings-vs-native.
4. **(MEDIUM) Feature-level evolvability rests on a single primary case (inspections reverse removal) plus complementary data points.** **Mitigation:** acknowledge explicitly in §4.3 and §4.9 that the primary evidence is one reverse experiment (chosen because inspections is cross-feature while ProjectPhoto is intra-feature) supplemented by the ProjectPhoto forward case and an optional synthetic platform-axis extension; compensate with the structural combinatorial test (naming every file in the intrinsic bucket and asking whether it recurs per feature), which does not depend on having multiple commits; cross-reference with the Wear OS experiment's own ripple data in the discussion. Historical batch is explicitly avoided because the codebase architecture itself evolved over time and historical ripples would measure the past architecture.
5. **(MEDIUM) The reverse-removal methodology assumes symmetry between deletion and addition.** Deleting a feature and measuring the repair ripple is only a reliable forward-cost estimate if the current architecture would require the same touchpoints for a fresh feature addition. **Mitigation:** justify the symmetry argument in §4.3 from the structural rules — the architecture enforces the same hexagonal / category / encapsulation layout for any feature, so the set of aggregation points and cross-feature seams that inspections currently touches is the same set a new feature would touch; acknowledge in §4.9 that asymmetries are possible if repair took a shortcut the forward addition would not (e.g., temporarily commenting out a reference instead of implementing a replacement), and state that the repair log is kept truthful by treating any such shortcut as a measurement artifact to be flagged.
6. **(MEDIUM) Ripple bucket classification is subjective.** The local/intrinsic/collateral partition requires author judgment about *why* a file was touched. **Mitigation:** encode the classification rules in `ripple_rules.yaml`, commit it alongside the data so a second reader can reproduce or challenge the partition, name every file in the intrinsic and collateral buckets in the thesis prose, and acknowledge the subjectivity in §4.9.
7. **(MEDIUM) Single-developer bias.** **Mitigation:** center the argument on `ArchitectureRules.kt` (the build enforces the rules, the developer cannot bypass them) rather than on developer discipline; explicit acknowledgement in the threats section.
8. **(MEDIUM) NS theorem mapping may be challenged as interpretive rather than formal.** NS theory's theorems are stated at a level of abstraction that requires translation to any concrete codebase. **Mitigation:** frame the mapping in §4.6 as *"the Snag architectural mechanisms we identify as instantiations of each theorem"*, not as a formal NS audit; cite Mannaert & Verelst (2016) for the theorem definitions; explicitly state in §4.9 that a formal NS audit would require independent review.
9. **(LOW) Figure 1.4 already in the thesis is theoretical.** **Mitigation:** replace it or forward-reference the new figure from chapter 4. Simpler to leave the theoretical figure in the introduction and cite the measured headline figure from chapter 1 once chapter 4 data is ready.
10. **(LOW) Committee reader unfamiliar with KMP or NS theory.** **Mitigation:** define terms in 4.1; write for a general software engineering audience; cite primary NS sources for any reader wanting the full theoretical background.

---

## Verification — how to know this plan worked

1. **Reproducibility (hard requirement).** A fresh clone of the repo plus a single documented command sequence reproduces every number and figure in the chapter without manual intervention:
   ```
   ./gradlew sharingReport archCheck
   analysis/loc_report.sh
   analysis/dependency_closure.py
   analysis/feature_retro.py --ref <inspections-reverse-branch>
   analysis/feature_retro.py --sha b5365d611
   analysis/feature_retro.py --ref <ios-entity-extension-branch>   # optional
   analysis/feature_retro.py --ref <dvt-synthetic-branch>          # optional
   analysis/feature_retro.py --ref <wearos-branch>
   python analysis/figures.py
   ```
   This must produce an identical `analysis/data/*.csv` and `analysis/figures/*.pdf` set to what the thesis prints. **Every hard number in the chapter is the output of one of these commands** — no hand-counted LOC, no manual module enumeration, no spreadsheet tally, no screenshot-based measurement. The ripple bucket classification and the recurring-vs-fixed anomaly classification are judgment-based, but both are captured in committed files (`ripple_rules.yaml` and a per-change markdown classification file under `analysis/classifications/`) so a second reader can reproduce the figures using those exact inputs and then challenge the classifications explicitly by editing them and rerunning.

2. **Audit the reproducibility before submission.** The author runs the full command sequence on a fresh clone in a clean working directory, diffs the outputs against the committed `data/` and `figures/` folders, and confirms they are identical. Any drift is a blocking bug.

3. **Scope lock:** the operationalization table (outcome → metric → data source → producing script) in §4.1 is committed to the thesis repo before measurement results are entered, so the chapter's scope is fixed in advance even though its conclusions are not.

4. **Every outcome has a measured value** — every row of the operationalization table is populated with an observed number or distribution produced by a named script, plus discussion of what it means.

5. **Live experiment builds a running Wear OS binary.** A screenshot of a single Wear-native screen wired to the shared stack, running on a Wear OS emulator, is in §4.4.

6. **`archCheck` output on the submission SHA** is included verbatim in §4.1 as part of the enabling-mechanism paragraph, produced by a single `./gradlew archCheck` invocation.

7. **Every claim in the chapter cites a data source** (CSV row, figure ID, commit SHA, file path, or classification file path).

8. **Every threat in §4.9 has an acknowledgement or mitigation** — no threat is listed without a response.

9. **The chapter discusses the shape of the distributions, not just the headline numbers** — outliers and low-sharing modules are named and explained, because those cases carry more information than the modules that shared "as expected".

---

## Non-goals (explicitly out of scope)

- CK metrics (LCOM, CBO) — overkill and poorly suited to multiplatform code.
- Test coverage, static analysis, and other quality metrics — not bearing on the two outcomes (sharing, evolvability); the Implementation chapter already covers the project's testing and static-analysis setup and this chapter does not duplicate it.
- Empirical technology swap (e.g., swapping Ktor for OkHttp) — structural argument only; empirical swap is a separate thesis.
- Porting the full Snag app to Wear OS — the experiment is login + project list read only.
- Building a brand-new measurement tool from scratch — everything reuses existing infrastructure (`ModulePathParser`, `archCheck`, tokei).
