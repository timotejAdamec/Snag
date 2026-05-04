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
# Semantic model: the closure is unit-level — each (module, source_set) is
# a source-set-level unit of accounting. The walk respects two layers of
# semantics:
#
#   (1) Inter-module Gradle dependency edges with api/implementation scope.
#       Direct consumers always rebuild; the walk only propagates past a
#       consumer whose dependency on the reached node is api. This directly
#       implements Snag's Action Version Transparency: wherever a convention
#       plugin chose `implementation` over `api`, the closure walk stops.
#       Impl modules, testInfra, koinModulesAggregate, and every other AVT
#       seam act as natural sinks — no special-case list is required.
#
#   (2) Intra-module KMP source-set dependsOn hierarchy. Within a single
#       module, a change to `commonMain` propagates to every descendant
#       source set (mobile/nonWeb/nonAndroid/nonJvm intermediates → leaf
#       source sets) because each descendant compiles with its ancestor's
#       klib on its classpath. Cross-module consumers see the same hierarchy
#       on their side: a same-named source set in the consumer plus its own
#       descendants of that source set rebuild.
#
# Single-target consumers (BE modules and Android-app modules expose only a
# `main` source set) are handled by target-binary reachability: a KMP module's
# source set propagates to a single-target consumer's `main` iff the source
# set reaches the consumer's target binary (e.g., `commonMain` reaches all
# targets, `iosMain` reaches only ios — does NOT propagate to a BE consumer).
#
# Two blast-radius numbers per unit:
#   - blast_radius_module: count of transitive downstream Gradle modules
#                          (build-economy view — answers "how many
#                          build.gradle.kts files rebuild").
#   - blast_radius_unit:   count of transitive downstream (module, source_set)
#                          source-set-level units. Headline metric for §4.3 —
#                          exact under the combined api/impl-scope and
#                          source-set-hierarchy semantics described above.
#
# Output:
#   analysis/data/dependency_closure.json — keyed by "{module}::{source_set}"
#     → dict with blast_radius_module (int), blast_radius_unit (int),
#     downstream_sample (up to 20 downstream unit keys).
#
# runtimeOnly edges are dropped at load time (not on the compile classpath).
#
# Deterministic, idempotent. Pure stdlib — no third-party deps.
#
# Usage: python analysis/dependency_closure.py

from __future__ import annotations

import csv
import json
import sys
from collections import defaultdict
from pathlib import Path

# Local import — analysis/ is on sys.path when invoked as a script from the
# repo root, and conftest.py inserts it for tests.
import source_set_hierarchy

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


def _load_units_and_targets(path: Path) -> tuple[dict[str, list[str]], dict[str, str]]:
    """Returns (module_path → sorted list of source_set names, module_path →
    inferred single-target name for non-KMP modules).

    A module is single-target if it does NOT have `commonMain` in its source
    sets. For those modules, the target is inferred from `platform_set`
    column values seen on the module's rows: `backend` → jvm; `android` →
    android. KMP modules (with commonMain) are absent from the target dict.
    """
    if not path.is_file():
        sys.stderr.write(
            f"[dependency_closure] ERROR: {path} not found. "
            f"Run ./gradlew sharingReport && analysis/loc_report.sh first.\n"
        )
        sys.exit(2)

    units: dict[str, set[str]] = defaultdict(set)
    platform_sets_per_module: dict[str, set[str]] = defaultdict(set)
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            module = row["module_path"]
            units[module].add(row["source_set"])
            ps = row.get("platform_set", "")
            if ps:
                platform_sets_per_module[module].add(ps)

    units_sorted = {module: sorted(ss_list) for module, ss_list in units.items()}
    targets = _infer_module_targets(units_sorted, platform_sets_per_module)
    return units_sorted, targets


def _infer_module_targets(
    units: dict[str, list[str]],
    platform_sets_per_module: dict[str, set[str]],
) -> dict[str, str]:
    """Returns module_path → 'jvm' or 'android' for single-target modules.

    Heuristic: a module without `commonMain` is single-target. Pick the
    target from its `platform_set` values: backend → jvm, android → android.
    Falls back to 'jvm' when ambiguous (the conservative default — Snag's
    backend modules are the dominant single-target case)."""
    targets: dict[str, str] = {}
    for module, source_sets in units.items():
        if "commonMain" in source_sets:
            continue  # KMP module — not single-target
        platform_sets = platform_sets_per_module.get(module, set())
        if "android" in platform_sets:
            targets[module] = "android"
        elif "backend" in platform_sets or "jvm_shared" in platform_sets or "jvm_desktop" in platform_sets:
            targets[module] = "jvm"
        else:
            targets[module] = "jvm"
    return targets


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


def _downstream_units_for(
    start_module: str,
    start_ss: str,
    consumer_modules: frozenset[str],
    units: dict[str, list[str]],
    module_targets: dict[str, str],
) -> list[str]:
    """Computes the unit-level downstream of (start_module, start_ss).

    Combines intra-module hierarchy propagation (within start_module, which
    descendants of start_ss in Snag's KMP hierarchy rebuild) with inter-module
    propagation through each consumer module reached by the scope-aware
    module walk. The consumer's matching/descendant source sets, or its
    single-target `main` if the source set reaches the consumer's target
    binary, are added to the downstream set."""
    descendants = source_set_hierarchy.descendants_of(start_ss)
    start_target_bins = source_set_hierarchy.target_bins_of(
        start_ss,
        single_target=module_targets.get(start_module),
    )

    downstream: list[str] = []
    seen: set[str] = set()

    def _add(unit: str) -> None:
        if unit not in seen:
            seen.add(unit)
            downstream.append(unit)

    own_sets = units.get(start_module, [])
    for ss in own_sets:
        if ss == start_ss:
            continue
        if ss in descendants:
            _add(f"{start_module}::{ss}")

    for consumer in sorted(consumer_modules):
        consumer_sets = units.get(consumer, [])
        consumer_target = module_targets.get(consumer)
        if consumer_target is not None:
            if consumer_target in start_target_bins:
                for ss in consumer_sets:
                    _add(f"{consumer}::{ss}")
        else:
            if start_ss in consumer_sets:
                _add(f"{consumer}::{start_ss}")
                for ss in consumer_sets:
                    if ss != start_ss and ss in descendants:
                        _add(f"{consumer}::{ss}")

    return downstream


def compute_closure(
    edges: list[tuple[str, str, str]],
    units: dict[str, list[str]],
    module_targets: dict[str, str] | None = None,
) -> dict[str, dict]:
    """Computes blast radius per unit.

    `module_targets` maps single-target modules (BE, Android-app) to their
    target binary name ('jvm' or 'android'). Modules absent from this dict
    AND lacking 'commonMain' default to 'jvm' (Snag's dominant single-target
    case is backend). KMP modules (those with 'commonMain') are always
    treated as multi-target regardless of their entry."""
    reverse = _build_reverse_graph(edges)
    targets = dict(module_targets) if module_targets else {}
    for module, source_sets in units.items():
        if "commonMain" in source_sets:
            continue
        targets.setdefault(module, "jvm")

    result: dict[str, dict] = {}
    for module, source_sets in sorted(units.items()):
        dependents = _transitive_dependents(module, reverse)
        blast_radius_module = len(dependents)

        for ss in source_sets:
            downstream_units = _downstream_units_for(
                start_module=module,
                start_ss=ss,
                consumer_modules=dependents,
                units=units,
                module_targets=targets,
            )
            key = f"{module}::{ss}"
            result[key] = {
                "blast_radius_module": blast_radius_module,
                "blast_radius_unit": len(downstream_units),
                "downstream_sample": downstream_units[:DOWNSTREAM_SAMPLE_CAP],
            }

    return result


def main() -> None:
    edges = _load_edges(EDGES_CSV)
    units, module_targets = _load_units_and_targets(SHARING_CSV)
    closure = compute_closure(edges, units, module_targets=module_targets)

    OUTPUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_JSON.open("w", encoding="utf-8") as f:
        json.dump(closure, f, indent=2, sort_keys=True)
        f.write("\n")

    sys.stderr.write(
        f"[dependency_closure] wrote {len(closure)} entries to {OUTPUT_JSON}\n"
    )


if __name__ == "__main__":
    main()
