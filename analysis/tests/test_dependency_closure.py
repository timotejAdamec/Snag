"""Unit tests for analysis/dependency_closure.py.

Exercises the pure closure math without requiring real Gradle output — builds
a small in-memory fixture of edges and unit lists and checks the computed
blast-radius numbers.

Edges are (source_module, target_module, scope) triples. scope is one of
"api" or "implementation" (runtimeOnly is dropped at load time, not in
compute_closure). The walk respects Gradle's api/implementation contract:
direct consumers are enumerated regardless of scope, but the walk only
propagates past a consumer whose edge is scope=api.
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
    # A impl-> B api-> C. C's blast: direct consumer B rebuilds; B rebuilds
    # but B's dep on C is api, so walking past B goes to A. A's dep on B is
    # implementation, so A is added but walk stops. A doesn't forward B to
    # anything. Blast of C = {B, A}.
    #
    # Now flip: A api-> B impl-> C. C's direct consumer is B (blast adds B).
    # B's dep on C is impl, so walk stops at B. A is not reached.
    # Blast of C = {B}.
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
    #
    # Scenario: model -(api)-> api -(api)-> impl <-(implementation)- aggregator
    # (arrow points from consumer to target; reverse closure walks the other
    # direction).
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
    # Walk from :feat:x:fe:app:impl: direct consumer is koinModulesAggregate:fe
    # via implementation → aggregator rebuilds but walk stops. blast = 1.
    assert closure[":feat:x:fe:app:impl::commonMain"]["blast_radius_module"] == 1
    # Walk from :feat:x:business:model: api → app:api (walk past) → impl
    # (implementation, stop) → also the app:api → impl walk stops there.
    # From app:api: reverse[app:api] = [(impl, implementation)]; impl is added
    # but not pushed. From business:model: reached {app:api, impl}. impl's
    # only further reverse is (aggregator, implementation) — not explored
    # because impl was reached via implementation.
    # Blast of business:model = {app:api, impl}.
    assert closure[":feat:x:business:model::commonMain"]["blast_radius_module"] == 2


def test_snag_testinfra_implementation_chain_does_not_contaminate():
    # Models the real Snag testInfra situation: testInfra:fe has production
    # (commonMainImplementation → "implementation" scope) edges to feat test
    # fakes. Core has a test-only edge (commonTestImplementation → also
    # "implementation" scope) to testInfra:fe.
    #
    # Walking reverse from :feat:clients:fe:driven:test: direct consumer is
    # testInfra:fe via implementation → testInfra rebuilds, walk stops there.
    # Core is never reached. blast = 1 (testInfra only).
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


# ------------------------------- unit-level counting --------------------------

def test_unit_level_counts_every_source_set_of_every_downstream():
    # A has 2 source sets, B has 1 source set. B api-> A (B is consumer of A).
    # A's blast_radius_unit should count all source sets of B (= 1).
    edges = [(":B", ":A", "api")]
    units = {
        ":A": ["commonMain", "iosMain"],
        ":B": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":A::commonMain"]["blast_radius_unit"] == 1
    assert closure[":A::iosMain"]["blast_radius_unit"] == 1


# ------------------------------- diamond, fanout, self-loops ------------------

def test_diamond_all_api_propagates_fully():
    # B api-> A, C api-> A, D api-> B, D api-> C. A's blast = {B, C, D}.
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
    # Same consumer declared twice (e.g., commonMainImplementation plus
    # androidMainImplementation) must not be double-counted.
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
    # 30 direct consumers fan-in on one sink, all via api.
    edges = [(f":dep{i}", ":sink", "api") for i in range(30)]
    units = {":sink": ["commonMain"]}
    for i in range(30):
        units[f":dep{i}"] = ["commonMain"]
    closure = dependency_closure.compute_closure(edges, units)
    sample = closure[":sink::commonMain"]["downstream_sample"]
    assert len(sample) <= dependency_closure.DOWNSTREAM_SAMPLE_CAP
    assert closure[":sink::commonMain"]["blast_radius_module"] == 30
