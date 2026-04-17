# DVT on Client entity — qualitative critique (Case 3 — experiment/dvt-client-field)

## Subject

- **Experiment branch:** `experiment/dvt-client-field`
- **Comparison ref:** `main` (snapshot `main@e076e89e5`, commit before the experiment)
- **Before/after sharing snapshot:** `analysis/data/sharing_report_with_loc_base_main_e076e89e5.csv`
- **Classification file:** `analysis/classifications/dvt-client-field.yaml`
- **Ripple output:** `analysis/data/ripple_dvt-client-field_files.csv`, `analysis/data/ripple_dvt-client-field_units.csv`
- **§4.6 primary source for Case 3.**

## Authoring note

The anomaly taxonomy for this case was **locked before running `./gradlew check`** on the experiment branch — the table in the classification YAML header was written into a stub and committed on `chore/phase-2-ripple-tooling` before a single Kotlin file was modified. Any touch that the classification records as an anomaly was defined as such before the build ran; the critique below is written against that pre-commitment.

## Honesty claim

The experiment is a paired synthetic change testing both Data Version Transparency mechanisms on the `Client` entity:

1. **Sub-experiment 3a — shared attribute via inheritance** (`ico: String?`, a Czech company tax ID — semantically shared across the `Client` hierarchy). Introduced at `Client` (business/model) with a default of `null`; inherited downward through `AppClient/AppClientData` (app/model) and `BackendClient/BackendClientData` (be/app/model). Tests mechanism (1) downward propagation: intermediate interfaces must **not** need to redeclare `ico`; their concrete `*Data` classes only need to override with a default.
2. **Sub-experiment 3b — level-specific attribute via scoping** (`adminNote: String?`, a BE-only internal annotation). Introduced at `BackendClient` (be/app/model) only. Tests mechanism (1) upward containment: nothing in `business/model`, `app/model`, `contract`, or FE layers should have to change. Also tests that the BE-only concept stays off the wire DTO.
3. **Mechanism (2) default absorption** (asserted across both 3a and 3b). For both fields, every existing construction site of `AppClientData(...)` and `BackendClientData(...)` must compile unchanged because the new fields have defaults. Any forced caller edit is a mechanism-(2) failure.

The claim under test is falsifiable along three independent axes. Any one of the following would have been recorded as an anomaly per the locked taxonomy, and none were observed:

- A forced `abstract val ico` or `abstract val adminNote` declaration on any intermediate interface (3a mechanism-1 failure).
- Any touch in `feat/clients/business/model/**`, `feat/clients/app/model/**`, `feat/clients/contract/**`, or `feat/clients/fe/**` on the 3b axis (3b mechanism-1 failure — upward bleed of a BE-only field).
- Any caller of `AppClientData(...)` or `BackendClientData(...)` forced to pass `ico =` or `adminNote =` explicitly (mechanism-2 failure).

The build succeeded without any such forced edit. All 17 touched files fall inside the locked "not anomaly" set.

---

## DVT — entity axis

### DVT-0 (zero non-essential touches observed)

**Observation.** The synthetic change introduced two new fields on the `Client` hierarchy and propagated them through the data-carrying path:

1. **Essential 3a carriers** (shared `ico` flows through every level that persists or serializes `Client` data): `Client.kt` (1 line added — interface), `AppClient.kt` (1 — `AppClientData` override + default), `BackendClient.kt` (overlaps with 3b, counted once), `ClientApiDto.kt` + `PutClientApiDto.kt` (wire DTO additions), FE + BE API/DB mappers thread `ico` between DTO/entity and model (5 mapper files), FE SQLDelight `ClientEntity.sq` + migration `2.sqm` + regenerated snapshot `3.db`, BE `ClientsTable.kt` + `ClientEntity.kt` Exposed schema, BE `RealClientsDb.kt` save path, BE driving mapper.
2. **Essential 3b carriers** (BE-only `adminNote` flows only through the BE storage path): `BackendClient.kt` (interface + data class), BE `ClientsTable.kt` + `ClientEntity.kt`, BE driven mapper, BE `RealClientsDb.kt`. `adminNote` does **not** appear on the wire DTO or in the FE SQLDelight schema — containment held.
3. **New test** (`ClientApiDtoSerializationTest.kt`) asserts backwards-compatible JSON parsing for the `ico` field on the wire contract.

Zero use cases, ViewModels, DI Koin modules, sync handlers, schema registries, or cross-feature consumer files required modification. `archCheck` passed on the final state. No intermediate interface gained an `abstract val`; all additions were `override val ... = null` on the concrete `*Data` classes.

**Theory.** DVT on the entity axis predicts that adding an attribute to an entity should force touches only at the levels where the attribute must be stored, serialized, or projected. Levels that do not need to know about the attribute — intermediate interfaces, consumers of the interface that do not read the new field, DI aggregators, sync layers that operate on the entity through a port — should remain untouched. The experiment is the ideal positive case for this prediction on the level/layer dimension.

**Delta vs existing architecture.** Before Case 3, the shape of the `Client` hierarchy (interface chain + concrete `*Data` classes at each level) had not been stress-tested for field-extension evolvability. The counterfactual reveals:

- **Mechanism (1) — inheritance downward propagation.** `AppClient` and `BackendClient` interfaces inherit `ico: String?` from `Client` without any redeclaration. Only the concrete `AppClientData` and `BackendClientData` add `override val ico: String? = null`. This confirms that intermediate interfaces in Snag's Project-hierarchy pattern (interface + `*Data` at each level) are **field-transparent**: they carry inherited state without enumerating it.
- **Mechanism (1) — upward containment.** `adminNote` stays at `BackendClient` and below (Exposed table, DAO entity, DB impl, driven mapper). It does not bleed upward into `AppClient`, `Client`, `ClientApiDto`, or any FE code. The architecture contains BE-only state at the BE-specific level as predicted.
- **Mechanism (2) — default absorption.** Not a single existing caller of `AppClientData(...)` or `BackendClientData(...)` was forced to pass either new field explicitly. Defaults on both new fields (`= null`) fully absorbed the extension at every existing construction site. Verified by a grep-style check of all `AppClientData(` and `BackendClientData(` call sites: identical signatures before and after.

### DVT-1 — Test-fixture intrinsic (not an anomaly per locked taxonomy)

**Location.** `feat/clients/be/driven/test/src/main/kotlin/cz/adamec/timotej/snag/clients/be/driven/test/ClientsDbTestExtensions.kt` (4 lines modified, unit `:feat:clients:be:driven:test::main`, recurring).

**Observation.** The test fixture `seedTestClient(...)` enumerates every field of `BackendClientData` explicitly (with sensible synthetic values) to make test setup readable. Adding `ico` and `adminNote` to the entity forces one edit to this fixture on every entity extension.

**Theory.** Test fixtures that enumerate entity fields are a field-transparency trade-off: readability at the test site vs. one recurring-intrinsic edit per field extension. Per the locked anomaly taxonomy (severity: low, category: `testInfra/**`), this is **not** a DVT violation of the architecture under test; it is a known recurring-intrinsic cost confined to test infrastructure. It is covered by the `test_fixture` category in the ripple rules and is the single recurring-intrinsic unit in this case.

---

## SoC — derivative observation

### SoC-0 (zero upward bleed of BE-only state)

**Observation.** The `adminNote` sub-experiment (3b) placed a BE-only annotation at `BackendClient`. The predicted upward-containment boundary is the `BackendClient` interface itself: `AppClient`, `Client`, `ClientApiDto`, `PutClientApiDto`, and the entire FE tree (`feat/clients/fe/**`, `featShared/database/fe/**`) must not see the field.

Zero files in `feat/clients/business/model/**`, `feat/clients/app/model/**`, `feat/clients/contract/**`, `feat/clients/fe/**`, or FE SQLDelight required modification for `adminNote`. The BE-only concern stayed BE-only.

**Theory.** SoC on the entity-axis layer dimension predicts that a BE-specific attribute should be added at the BE-specific layer without forcing changes at FE-visible layers above it. The experiment is the ideal positive case: the architecture contained the BE-only attribute at `BackendClient` (be/app/model) and its storage-path dependents, producing zero upward or sideways ripple.

**Delta vs existing architecture.** The `be/app/model` → `app/model` → `business/model` taxonomy is asymmetric by design (BE-only concepts live in `be/app/model`; FE-only concepts would live in a `fe/app/model` that was created for Case 4). Case 3 confirms that this asymmetry is load-bearing for upward containment in addition to its Case 4 role for downward containment.

---

## Headline numbers (§4.6)

- **3a non-essential touches = 0**
- **3b upward-bleed touches = 0**
- **forced caller edits (mechanism 2) = 0**
- **recurring-intrinsic units touched = 1** (`:feat:clients:be:driven:test::main` — `seedTestClient`)
- **files local/intrinsic/collateral = 16 / 1 / 0** (total 17)
- **churn local/intrinsic/collateral = 98 / 4 / 0** (total 102)
- **units local/intrinsic/collateral = 10 / 1 / 0** (total 11)

---

## Conclusion

Zero non-essential touches observed on either the shared-attribute (3a) or level-specific-attribute (3b) mechanism, and zero forced caller edits on the default-absorption (mechanism 2) axis. The architecture carried the shared `ico` field through the `Client` hierarchy via inheritance without redeclaration at intermediate interfaces, contained the BE-only `adminNote` at the `BackendClient` layer with no upward bleed, and absorbed both fields' defaults at every existing construction site with zero caller edits. The single recurring-intrinsic touch is the test fixture `seedTestClient`, which is a known, bounded cost at the `testInfra/**` category per the locked taxonomy. This is strong positive evidence for entity-axis DVT on Snag's inheritance + defaults pattern.
