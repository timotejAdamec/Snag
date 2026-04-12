#!/usr/bin/env python3
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Thesis: "Multiplatform snagging system with code sharing maximisation"
# Czech Technical University in Prague — Faculty of Information Technology
#
# Computes the scope-aware transitive downstream closure for every (module,
# source_set) unit in `analysis/data/sharing_report_with_loc.csv`, using the
# edge list from `build/reports/dependency_graph/dependency_graph.csv`
# produced by the Gradle DependencyGraphTask.
#
# Semantic model: the closure respects Gradle's api vs implementation scope.
# A reverse-walk from module M enumerates every direct consumer (any scope —
# all of them rebuild when M's ABI changes), but only continues the walk past
# a consumer whose dependency on the reached node is scope=api. An
# implementation edge terminates forward propagation at that node — Gradle's
# api/implementation contract hides the transitive dependency from the
# consumer's own consumers.
#
# This directly implements Normalized Systems Theory's Action Version
# Transparency: wherever a Snag convention plugin chose `implementation` over
# `api`, the closure walk stops. Impl modules, testInfra, koinModulesAggregate,
# and every other AVT seam act as natural sinks — no special-case list is
# required, the api/impl split is honored directly.
#
# Two blast-radius numbers per unit:
#   - blast_radius_module: count of transitive downstream modules reachable
#                          via api-forwarding chains. Exact under the Gradle
#                          api/implementation contract.
#   - blast_radius_unit:   count of transitive downstream (module, source_set)
#                          units. Upper bound — KMP intermediate source sets
#                          do not expose distinct Gradle configurations, so
#                          this metric counts every source set of every
#                          downstream module. §4.3 headline uses
#                          blast_radius_module; source-set-axis discussion
#                          cites blast_radius_unit with this caveat.
#
# Output:
#   analysis/data/dependency_closure.json — keyed by "f{module}::{source_set}"
#     → dict with blast_radius_module (int), blast_radius_unit (int),
#     downstream_sample (up to 20 downstream unit keys).
#
# runtimeOnly edges are dropped at load time (not on the compile classpath).
#
# Deterministic, idempotent. Pure stdlib — no third-party deps beyond what
# figures.py already uses.
#
# Usage: python analysis/dependency_closure.py

from __future__ import annotations

import csv
import json
import sys
from collections import defaultdict
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent
EDGES_CSV = REPO_ROOT / "build" / "reports" / "dependency_graph" / "dependency_graph.csv"
SHARING_CSV = SCRIPT_DIR / "data" / "sharing_report_with_loc.csv"
OUTPUT_JSON = SCRIPT_DIR / "data" / "dependency_closure.json"
DOWNSTREAM_SAMPLE_CAP = 20


def _load_edges(path: Path) -> list[tuple[str, str, str]]:
    """Returns list of (source_module, target_module, scope) edges.

    scope is one of {api, implementation} after filtering. runtimeOnly edges
    are dropped: they aren't on the compile classpath so they don't contribute
    to compile-time blast radius. Test configurations are retained as
    scope=implementation; the scope-aware walk correctly stops propagation at
    those edges.
    """
    if not path.is_file():
        sys.stderr.write(
            f"[dependency_closure] ERROR: {path} not found. "
            f"Run ./gradlew dependencyGraphReport first.\n"
        )
        sys.exit(2)

    edges: list[tuple[str, str, str]] = []
    total = 0
    dropped_runtime = 0
    dropped_blank = 0
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            total += 1
            scope = row["scope"]
            if scope == "runtimeOnly":
                dropped_runtime += 1
                continue
            if scope not in ("api", "implementation"):
                # Defensive: the Gradle task only emits api/implementation/runtimeOnly,
                # but a future config family with an unknown scope would land here.
                dropped_blank += 1
                continue
            src = row["source_module"]
            tgt = row["target_module"]
            if src == tgt:
                continue
            edges.append((src, tgt, scope))
    sys.stderr.write(
        f"[dependency_closure] edges: {len(edges)} compile-classpath "
        f"(dropped {dropped_runtime} runtimeOnly, {dropped_blank} unknown-scope, "
        f"from {total} total)\n"
    )
    return edges


def _load_units(path: Path) -> dict[str, list[str]]:
    """Returns module_path → list of source_set names (one per row in the sharing report).
    Preserves duplicates suppressed — each (module, source_set) appears once."""
    if not path.is_file():
        sys.stderr.write(
            f"[dependency_closure] ERROR: {path} not found. "
            f"Run ./gradlew sharingReport && analysis/loc_report.sh first.\n"
        )
        sys.exit(2)

    units: dict[str, set[str]] = defaultdict(set)
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            units[row["module_path"]].add(row["source_set"])

    return {module: sorted(ss_list) for module, ss_list in units.items()}


def _build_reverse_graph(
    edges: list[tuple[str, str, str]],
) -> dict[str, list[tuple[str, str]]]:
    """Returns target → list of (source_module, scope) pairs. Inverts edges so
    we can walk downstream from a module M by following reverse edges, and
    keeps the scope of each reverse edge so the walk can decide whether to
    propagate past each consumer."""
    reverse: dict[str, list[tuple[str, str]]] = defaultdict(list)
    for src, tgt, scope in edges:
        reverse[tgt].append((src, scope))
    return reverse


def _transitive_dependents(
    start_module: str,
    reverse_graph: dict[str, list[tuple[str, str]]],
) -> frozenset[str]:
    """Scope-aware transitive closure of dependents. Excludes start_module.

    The walk enumerates every direct consumer (any scope — all of them
    rebuild when start_module's ABI changes, because they all have it on
    their compile classpath). It then continues the walk past a consumer
    only if the consumer depends on its reached node via scope=api, which
    means the consumer's own ABI includes the reached node's ABI and
    therefore transitively includes start_module's ABI.

    A scope=implementation edge terminates forward propagation at that
    consumer: the consumer rebuilds (it is in the visited set), but the
    consumer's own consumers are NOT reached via this chain, because
    Gradle's api/implementation contract hides the transitive dependency.

    This naturally implements Snag's Action Version Transparency: wherever
    a convention plugin chose `implementation` over `api`, the walk stops.
    No special-case sink list is needed — the api/impl split is honored
    directly.

    Memoization is intentionally NOT used: the per-start-module closure
    depends on which edges are reachable via api-forwarding nodes from
    this particular start, and those edges can change between starts. The
    module count is small enough (~200 modules) that the repeated BFS is
    cheap — a fresh walk per start.
    """
    visited: set[str] = set()
    stack: list[str] = [start_module]
    while stack:
        current = stack.pop()
        for (upstream, scope) in reverse_graph.get(current, ()):
            if upstream == start_module or upstream in visited:
                continue
            visited.add(upstream)
            if scope == "api":
                stack.append(upstream)
            # scope=implementation (and anything else non-api): upstream
            # rebuilds but does not forward the transitive dependency to
            # its own consumers. Do not push onto stack.
    return frozenset(visited)


def compute_closure(
    edges: list[tuple[str, str, str]],
    units: dict[str, list[str]],
) -> dict[str, dict]:
    reverse = _build_reverse_graph(edges)

    result: dict[str, dict] = {}
    for module, source_sets in sorted(units.items()):
        dependents = _transitive_dependents(module, reverse)
        blast_radius_module = len(dependents)

        # Conservative unit-level: count every source set of every downstream module.
        downstream_units: list[str] = []
        for dep_module in sorted(dependents):
            for ss in units.get(dep_module, []):
                downstream_units.append(f"{dep_module}::{ss}")
        blast_radius_unit = len(downstream_units)

        # Each source set of the source module carries the same closure (module-level
        # approximation); we emit per-source-set entries so consumers can look up units
        # by the same key shape feature_retro.py uses.
        for ss in source_sets:
            key = f"{module}::{ss}"
            result[key] = {
                "blast_radius_module": blast_radius_module,
                "blast_radius_unit": blast_radius_unit,
                "downstream_sample": downstream_units[:DOWNSTREAM_SAMPLE_CAP],
            }

    return result


def main() -> None:
    edges = _load_edges(EDGES_CSV)
    units = _load_units(SHARING_CSV)
    closure = compute_closure(edges, units)

    OUTPUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_JSON.open("w", encoding="utf-8") as f:
        json.dump(closure, f, indent=2, sort_keys=True)
        f.write("\n")

    sys.stderr.write(
        f"[dependency_closure] wrote {len(closure)} entries to {OUTPUT_JSON}\n"
    )


if __name__ == "__main__":
    main()
