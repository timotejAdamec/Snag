"""Unit tests for analysis/dependency_closure.py.

Exercises the pure closure math without requiring real Gradle output — builds
a small in-memory fixture of edges and unit lists and checks the computed
blast-radius numbers.
"""
from __future__ import annotations

import dependency_closure


def test_closure_with_single_chain():
    # A -> B -> C. C has a large blast radius (2 modules: A, B), B has 1 (A),
    # A has 0 (nothing depends on it).
    edges = [(":A", ":B"), (":B", ":C")]
    units = {
        ":A": ["commonMain", "androidMain"],
        ":B": ["commonMain"],
        ":C": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)

    assert closure[":C::commonMain"]["blast_radius_module"] == 2
    assert closure[":B::commonMain"]["blast_radius_module"] == 1
    assert closure[":A::commonMain"]["blast_radius_module"] == 0
    assert closure[":A::androidMain"]["blast_radius_module"] == 0


def test_closure_unit_level_counts_every_source_set_of_every_downstream():
    # A has 2 source sets, B has 1 source set. B depends on A.
    # A's blast_radius_unit should be 1 (only B:commonMain).
    edges = [(":B", ":A")]
    units = {
        ":A": ["commonMain", "iosMain"],
        ":B": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    # Both source sets of A expose the same downstream blast (conservative
    # over-approximation per plan).
    assert closure[":A::commonMain"]["blast_radius_unit"] == 1
    assert closure[":A::iosMain"]["blast_radius_unit"] == 1


def test_closure_fanout_is_deduped():
    # Multiple edges from the same module at different configurations must not
    # double-count the downstream.
    edges = [
        (":App", ":Lib"),          # commonMainImplementation
        (":App", ":Lib"),          # androidMainImplementation to the same target
    ]
    units = {":App": ["commonMain"], ":Lib": ["commonMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":Lib::commonMain"]["blast_radius_module"] == 1


def test_closure_diamond():
    # B and C both depend on A; D depends on both B and C. A's blast should
    # include B, C, D — three modules.
    edges = [
        (":B", ":A"),
        (":C", ":A"),
        (":D", ":B"),
        (":D", ":C"),
    ]
    units = {
        ":A": ["commonMain"],
        ":B": ["commonMain"],
        ":C": ["commonMain"],
        ":D": ["commonMain"],
    }
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":A::commonMain"]["blast_radius_module"] == 3


def test_closure_self_loops_ignored():
    edges = [(":A", ":A"), (":B", ":A")]
    units = {":A": ["commonMain"], ":B": ["commonMain"]}
    closure = dependency_closure.compute_closure(edges, units)
    assert closure[":A::commonMain"]["blast_radius_module"] == 1


def test_closure_downstream_sample_is_capped():
    # Build a fan-in: 30 dependents all pointing at one sink.
    edges = [(f":dep{i}", ":sink") for i in range(30)]
    units = {":sink": ["commonMain"]}
    for i in range(30):
        units[f":dep{i}"] = ["commonMain"]
    closure = dependency_closure.compute_closure(edges, units)
    sample = closure[":sink::commonMain"]["downstream_sample"]
    assert len(sample) <= dependency_closure.DOWNSTREAM_SAMPLE_CAP
    assert closure[":sink::commonMain"]["blast_radius_module"] == 30
