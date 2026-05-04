"""Unit tests for analysis/dependency_closure.py.

Exercises the pure closure math without requiring real Gradle output — builds
a small in-memory fixture of edges and unit lists and checks the computed
blast-radius numbers.

Edges are (source_module, target_module, scope) triples. scope is one of
"api" or "implementation" (runtimeOnly is dropped at load time, not in
compute_closure). The walk respects Gradle's api/implementation contract:
direct consumers are enumerated regardless of scope, but the walk only
propagates past a consumer whose edge is scope=api.

Source-set semantics:
    - Intra-module: a unit T::ss reaches T's other source sets that descend
      from ss in Snag's KMP source-set hierarchy (commonMain → all leaves;
      mobileMain → android+ios; etc.). Always api-equivalent — descendants
      sit on top of ss in the dependsOn graph by construction.
    - Inter-module (KMP consumer C): T::ss reaches C::ss (if C has ss) plus
      C's intra-module descendants of ss in C's hierarchy. Same scope rules
      as the module-level walk: implementation edges still terminate
      forward propagation past C.
    - Inter-module (single-target consumer C, BE or Android-app, with only
      `main`): T::ss reaches C::main iff ss reaches C's target binary
      (e.g., commonMain reaches all targets, jvmMain reaches only jvm,
      iosMain reaches only ios → does NOT propagate to BE consumers).
"""
from __future__ import annotations

import dependency_closure


# ------------------------------- api-scope propagation ------------------------

def test_chain_all_api_propagates_fully():
    # A api-> B api-> C. C's blast = {A, B}; B's blast = {A}; A's blast = {}.
    edges = [
        (":A", ":B", "api"),
        (":B", ":C", "api"),
    ]
    units = {
        ":A": ["commonMain", "androidMain"],
        ":B": ["commonMain"],
        ":C": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)

    assert closure[":C::commonMain"]["blast_radius_module"] == 2
    assert closure[":B::commonMain"]["blast_radius_module"] == 1
    assert closure[":A::commonMain"]["blast_radius_module"] == 0


def test_chain_last_edge_implementation_truncates():
    # A api-> B impl-> C. From C: B is direct consumer (added) but the edge
    # B → C is implementation, so the walk does NOT continue past B.
    # A is never reached. Blast of C = {B}.
    edges = [
        (":A", ":B", "api"),
        (":B", ":C", "implementation"),
    ]
    units = {":A": ["commonMain"], ":B": ["commonMain"], ":C": ["commonMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":C::commonMain"]["blast_radius_module"] == 1  # only B


def test_chain_middle_edge_implementation_truncates():
    # A api-> B impl-> C api-> D.
    # Reverse from D: D's direct consumer is C (api edge → add C, walk past).
    # From C: C's dep on B is implementation → add B, stop walking past B.
    # A is never reached from D.
    # Blast of D = {C, B}.
    edges = [
        (":A", ":B", "api"),
        (":B", ":C", "implementation"),
        (":C", ":D", "api"),
    ]
    units = {
        ":A": ["commonMain"],
        ":B": ["commonMain"],
        ":C": ["commonMain"],
        ":D": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":D::commonMain"]["blast_radius_module"] == 2  # C and B


def test_direct_consumer_included_even_via_implementation():
    # When a module is a DIRECT consumer via implementation, it still rebuilds.
    # The walk just doesn't continue past it.
    edges = [(":A", ":B", "implementation")]
    units = {":A": ["commonMain"], ":B": ["commonMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":B::commonMain"]["blast_radius_module"] == 1  # A is visited


# ------------------------------- snag-shaped scenarios ------------------------

def test_snag_impl_module_is_automatic_sink():
    # Models the Snag api/impl split: feat:X:fe:app:impl has only one consumer,
    # koinModulesAggregate, via implementation. Changes to the impl's internal
    # deps don't ripple past the aggregator.
    edges = [
        (":feat:x:app:api", ":feat:x:business:model", "api"),
        (":feat:x:fe:app:impl", ":feat:x:app:api", "implementation"),
        (":koinModulesAggregate:fe", ":feat:x:fe:app:impl", "implementation"),
    ]
    units = {
        ":feat:x:business:model": ["commonMain"],
        ":feat:x:app:api": ["commonMain"],
        ":feat:x:fe:app:impl": ["commonMain"],
        ":koinModulesAggregate:fe": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":feat:x:fe:app:impl::commonMain"]["blast_radius_module"] == 1
    assert closure[":feat:x:business:model::commonMain"]["blast_radius_module"] == 2


def test_snag_testinfra_implementation_chain_does_not_contaminate():
    # testInfra:fe consumes feat test fakes via implementation. core has a
    # test-only edge (also implementation scope) to testInfra:fe. From the
    # fakes' perspective, blast = {testInfra:fe} only.
    edges = [
        (":testInfra:fe", ":feat:clients:fe:driven:test", "implementation"),
        (":core:foundation:fe", ":testInfra:fe", "implementation"),
    ]
    units = {
        ":feat:clients:fe:driven:test": ["commonMain"],
        ":testInfra:fe": ["commonMain"],
        ":core:foundation:fe": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":feat:clients:fe:driven:test::commonMain"]["blast_radius_module"] == 1
    sample = closure[":feat:clients:fe:driven:test::commonMain"]["downstream_sample"]
    assert all("core:foundation:fe" not in s for s in sample)


# ------------------------------- intra-module hierarchy -----------------------

def test_intra_module_common_main_propagates_to_own_descendants():
    # T has commonMain + androidMain + iosMain + jvmMain. Walking from
    # T::commonMain reaches T's other source sets via the dependsOn hierarchy
    # — each leaf source set sees commonMain's symbols. No external consumers.
    edges = []
    units = {":T": ["commonMain", "androidMain", "iosMain", "jvmMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    # Module count is 0 (no other modules). Unit count is 3 (T's own descendants).
    assert closure[":T::commonMain"]["blast_radius_module"] == 0
    assert closure[":T::commonMain"]["blast_radius_unit"] == 3
    sample = set(closure[":T::commonMain"]["downstream_sample"])
    assert ":T::androidMain" in sample
    assert ":T::iosMain" in sample
    assert ":T::jvmMain" in sample


def test_intra_module_leaf_source_set_has_no_descendants():
    # T::iosMain is a leaf — nothing in T descends from it. With no external
    # consumers, blast = 0.
    edges = []
    units = {":T": ["commonMain", "androidMain", "iosMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":T::iosMain"]["blast_radius_unit"] == 0


def test_intra_module_intermediate_source_set_propagates_to_its_subtree():
    # T::nonWebMain reaches T::androidMain + T::iosMain + T::jvmMain
    # (its descendants in Snag's hierarchy). Does NOT reach T::commonMain
    # (parent) or T::webMain (sibling-not-in-subtree).
    edges = []
    units = {":T": [
        "commonMain", "nonWebMain",
        "androidMain", "iosMain", "jvmMain",
        "webMain",
    ]}
    closure = dependency_closure.compute_closure(edges, units)
    sample = set(closure[":T::nonWebMain"]["downstream_sample"])
    assert ":T::androidMain" in sample
    assert ":T::iosMain" in sample
    assert ":T::jvmMain" in sample
    assert ":T::commonMain" not in sample  # parent — does not rebuild
    assert ":T::webMain" not in sample  # not in nonWeb subtree
    assert closure[":T::nonWebMain"]["blast_radius_unit"] == 3


# ------------------------------- inter-module same-name match ----------------

def test_inter_module_ios_main_only_reaches_consumer_ios_main():
    # T api-> consumer C. T::iosMain change reaches C::iosMain only.
    # C::commonMain does not rebuild (its source doesn't change).
    # C::androidMain does not rebuild (different target).
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain", "androidMain", "iosMain"],
        ":C": ["commonMain", "androidMain", "iosMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    sample = set(closure[":T::iosMain"]["downstream_sample"])
    assert ":C::iosMain" in sample
    assert ":C::commonMain" not in sample
    assert ":C::androidMain" not in sample
    # Unit count: only C::iosMain (no intra-module descendants of iosMain in T).
    assert closure[":T::iosMain"]["blast_radius_unit"] == 1


def test_inter_module_common_main_reaches_consumer_full_subtree():
    # T::commonMain change reaches consumer C's commonMain + all of C's
    # descendants of commonMain (every C source set).
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain"],
        ":C": ["commonMain", "androidMain", "iosMain", "jvmMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    sample = set(closure[":T::commonMain"]["downstream_sample"])
    assert {":C::commonMain", ":C::androidMain", ":C::iosMain", ":C::jvmMain"} <= sample
    assert closure[":T::commonMain"]["blast_radius_unit"] == 4


def test_inter_module_consumer_without_matching_source_set_does_not_count():
    # Consumer C is FE-only (no jvmMain). Walking from T::jvmMain — even if
    # T → C edge exists — does NOT reach any unit of C, because C does not
    # have jvmMain and jvm is not C's single-target.
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain", "jvmMain"],
        ":C": ["commonMain", "androidMain", "iosMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    # Module-level count still includes C (it rebuilds *something* if T's ABI
    # changes — even if jvmMain change in T may not actually require it,
    # blast_radius_module is the conservative module-level number).
    assert closure[":T::jvmMain"]["blast_radius_module"] == 1
    # Unit-level: no matching/descendant source set in C → 0.
    assert closure[":T::jvmMain"]["blast_radius_unit"] == 0


# ------------------------------- single-target consumers ----------------------

def test_be_consumer_main_rebuilds_on_kmp_common_main_change():
    # T is KMP-FULL. C is BE (only `main`, JVM target). T::commonMain reaches
    # JVM target → C::main rebuilds.
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain", "jvmMain"],
        ":C": ["main"],
    }
    module_targets = {":C": "jvm"}
    closure = dependency_closure.compute_closure(edges, units, module_targets=module_targets)
    sample = set(closure[":T::commonMain"]["downstream_sample"])
    assert ":C::main" in sample


def test_be_consumer_main_does_not_rebuild_on_kmp_ios_main_change():
    # T::iosMain does not reach JVM target → C::main (BE, JVM) does NOT rebuild.
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain", "iosMain", "jvmMain"],
        ":C": ["main"],
    }
    module_targets = {":C": "jvm"}
    closure = dependency_closure.compute_closure(edges, units, module_targets=module_targets)
    sample = set(closure[":T::iosMain"]["downstream_sample"])
    assert ":C::main" not in sample


def test_be_consumer_main_rebuilds_on_kmp_jvm_main_change():
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain", "jvmMain"],
        ":C": ["main"],
    }
    module_targets = {":C": "jvm"}
    closure = dependency_closure.compute_closure(edges, units, module_targets=module_targets)
    sample = set(closure[":T::jvmMain"]["downstream_sample"])
    assert ":C::main" in sample


def test_android_app_consumer_main_rebuilds_on_kmp_android_main_change():
    edges = [(":app", ":T", "api")]
    units = {
        ":T": ["commonMain", "androidMain"],
        ":app": ["main"],
    }
    module_targets = {":app": "android"}
    closure = dependency_closure.compute_closure(edges, units, module_targets=module_targets)
    sample = set(closure[":T::androidMain"]["downstream_sample"])
    assert ":app::main" in sample


def test_android_app_consumer_main_does_not_rebuild_on_kmp_jvm_main_change():
    edges = [(":app", ":T", "api")]
    units = {
        ":T": ["commonMain", "androidMain", "jvmMain"],
        ":app": ["main"],
    }
    module_targets = {":app": "android"}
    closure = dependency_closure.compute_closure(edges, units, module_targets=module_targets)
    sample = set(closure[":T::jvmMain"]["downstream_sample"])
    assert ":app::main" not in sample


def test_be_to_be_main_chain():
    # T (BE) api-> C (BE). Both single-target jvm.
    edges = [(":C", ":T", "api")]
    units = {":T": ["main"], ":C": ["main"]}
    module_targets = {":T": "jvm", ":C": "jvm"}
    closure = dependency_closure.compute_closure(edges, units, module_targets=module_targets)
    sample = set(closure[":T::main"]["downstream_sample"])
    assert ":C::main" in sample
    assert closure[":T::main"]["blast_radius_unit"] == 1


# ------------------------------- web subtree ---------------------------------

def test_web_main_change_propagates_to_js_and_wasm_js_descendants():
    # `applyDefaultHierarchyTemplate()` wires jsMain and wasmJsMain as
    # descendants of webMain via the template's `web` group. A change to
    # T::webMain rebuilds T's own jsMain/wasmJsMain leaves and the same
    # pair in any consumer that has them.
    edges = [(":C", ":T", "api")]
    units = {
        ":T": ["commonMain", "webMain", "jsMain", "wasmJsMain"],
        ":C": ["commonMain", "webMain", "jsMain", "wasmJsMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    sample = set(closure[":T::webMain"]["downstream_sample"])
    # Intra-module: T's own descendants of webMain.
    assert ":T::jsMain" in sample
    assert ":T::wasmJsMain" in sample
    # Inter-module: same-name match plus consumer's own web-side descendants.
    assert ":C::webMain" in sample
    assert ":C::jsMain" in sample
    assert ":C::wasmJsMain" in sample
    # 2 intra + 3 inter (C::webMain, C::jsMain, C::wasmJsMain) = 5.
    assert closure[":T::webMain"]["blast_radius_unit"] == 5


# ------------------------------- diamond, fanout, self-loops ------------------

def test_diamond_all_api_propagates_fully():
    edges = [
        (":B", ":A", "api"),
        (":C", ":A", "api"),
        (":D", ":B", "api"),
        (":D", ":C", "api"),
    ]
    units = {
        ":A": ["commonMain"],
        ":B": ["commonMain"],
        ":C": ["commonMain"],
        ":D": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":A::commonMain"]["blast_radius_module"] == 3


def test_fanout_is_deduped_across_scopes():
    edges = [
        (":App", ":Lib", "implementation"),
        (":App", ":Lib", "implementation"),
    ]
    units = {":App": ["commonMain"], ":Lib": ["commonMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":Lib::commonMain"]["blast_radius_module"] == 1


def test_self_loops_ignored():
    edges = [(":A", ":A", "api"), (":B", ":A", "api")]
    units = {":A": ["commonMain"], ":B": ["commonMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":A::commonMain"]["blast_radius_module"] == 1


def test_downstream_sample_is_capped():
    edges = [(f":dep{i}", ":sink", "api") for i in range(30)]
    units = {":sink": ["commonMain"]}
    for i in range(30):
        units[f":dep{i}"] = ["commonMain"]
    closure = dependency_closure.compute_closure(edges, units)
    sample = closure[":sink::commonMain"]["downstream_sample"]
    assert len(sample) <= dependency_closure.DOWNSTREAM_SAMPLE_CAP
    assert closure[":sink::commonMain"]["blast_radius_module"] == 30


def test_implementation_consumer_still_rebuilds_full_subtree_intra_module():
    # C has full hierarchy + depends on T via implementation. T::commonMain
    # change → C rebuilds (direct consumer). Within C, all source sets that
    # descend from commonMain rebuild.
    edges = [(":C", ":T", "implementation")]
    units = {
        ":T": ["commonMain"],
        ":C": ["commonMain", "androidMain", "iosMain", "jvmMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    sample = set(closure[":T::commonMain"]["downstream_sample"])
    assert {":C::commonMain", ":C::androidMain", ":C::iosMain", ":C::jvmMain"} <= sample
