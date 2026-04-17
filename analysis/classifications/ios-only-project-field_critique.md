# iOS-only Project extension — qualitative critique (Case 4 — experiment/ios-only-project-field)

## Subject

- **Experiment branch:** `experiment/ios-only-project-field`
- **Comparison ref:** `main` (snapshot `main@e076e89e5`, commit before the experiment)
- **Before/after sharing snapshot:** `analysis/data/sharing_report_with_loc_base_main_e076e89e5.csv`
- **Classification file:** `analysis/classifications/ios-only-project-field.yaml`
- **Ripple output:** `analysis/data/ripple_ios-only-project-field_files.csv`, `analysis/data/ripple_ios-only-project-field_units.csv`
- **§4.6 primary source for Case 4.**

## Authoring note

The anomaly taxonomy for this case was **locked before running `./gradlew check`** on the experiment branch — the table in the classification YAML was written into a stub and committed on `chore/phase-2-ripple-tooling` before a single Kotlin file was added. Any touch that the classification records as an anomaly was defined as such before the build ran; the critique below is written against that pre-commitment.

## Honesty claim

The experiment branch is a minimal synthetic change: a new FE-specific multiplatform module `feat/projects/fe/app/model/` containing exactly one source file in `iosMain`. The field `widgetPinned: Boolean` is a genuine iOS-exclusive concept (Apple WidgetKit has no cross-platform equivalent) and is placed at a layer that matches its semantic scope (FE app model, not platform-agnostic business model). The claim under test is falsifiable: any Gradle compile failure forcing edits outside the new module's `iosMain` source set (except for the one `settings.gradle.kts` include line) would have been recorded as an anomaly. The build succeeded without such a forced edit.

---

## SoC — platform axis

### SoC-0 (zero forced touches observed)

**Observation.** The synthetic change introduced the iOS-only attribute `widgetPinned: Boolean` on a `Project`-derived interface and it was contained entirely within:

1. A new KMP module `feat/projects/fe/app/model/` (2 new files: `build.gradle.kts`, `src/iosMain/kotlin/.../IosAppProject.kt`).
2. A single `settings.gradle.kts` include line (intrinsic per the existing `settings_gradle` ripple rule — every module add/remove touches this file).

Zero existing files in `feat/projects/business/model/**`, `feat/projects/app/model/**`, `feat/projects/be/**`, any existing `commonMain` source set, or any other feature required modification. `archCheck` passed on the final state.

**Theory.** SoC (Separation of Concerns) on the multiplatform-level dimension predicts that a platform-specific attribute should be added at the platform-specific multiplatform level without forcing changes at platform-agnostic levels above it. The experiment is the ideal case for this prediction: the architecture contained the iOS-only attribute at the correct multiplatform level (iosMain of a new FE-specific app-model module) and produced zero upward or sideways ripple.

**Delta vs existing architecture.** Before Case 4, the `feat/projects/` module taxonomy had no FE-specific app-model module (an asymmetry with `be/app/model` which already existed). The counterfactual reveals the architecture is ready for a FE-specific extension module to be added on demand — the convention-plugin auto-wire fallthrough handled the `:feat:projects:app:model` dependency without any explicit declaration in the new module's `build.gradle.kts`, demonstrating that the multiplatform-level containment extends cleanly to the iOS source set.

### SoC-1 — Convention-plugin-level intrinsic (not an anomaly)

**Location.** `settings.gradle.kts` (1 line added: `include(":feat:projects:fe:app:model")`).

**Observation.** Gradle's `settings.gradle.kts` must list every included module. Adding a new module necessarily adds one line here. This is intrinsic to Gradle's module registration mechanism — not a platform-axis ripple.

**Theory.** Intrinsic recurring touches (registry/enumeration files required by the toolchain) are a separate category from SoC violations on the architecture under test. They are covered by the `settings_gradle` rule in `ripple_rules.yaml`.

---

## Headline numbers (§4.6)

- **commonMain forced touches = 0**
- **non-iOS-source-set forced touches = 0**
- **cross-feature forced touches = 0**
- **recurring-intrinsic units touched = 1** (`:root::settings`)

---

## Conclusion

Zero forced touches observed outside the new module's own files and the intrinsic `settings.gradle.kts` registration line. The architecture contained the iOS-only attribute at the iOS source set as predicted. This is strong positive evidence for platform-axis SoC on the current Snag multiplatform layering.
