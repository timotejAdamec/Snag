#!/usr/bin/env python3
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Thesis: "Multiplatform snagging system with code sharing maximisation"
# Czech Technical University in Prague — Faculty of Information Technology
#
# Computes the transitive downstream closure for every (module, source_set) unit in
# `analysis/data/sharing_report_with_loc.csv`, using the edge list from
# `build/reports/dependency_graph/dependency_graph.csv` produced by the Gradle
# DependencyGraphTask.
#
# Two blast-radius numbers per unit:
#   - blast_radius_module: count of transitive downstream modules. Exact.
#   - blast_radius_unit:   count of transitive downstream (module, source_set) units.
#                          Conservative upper bound — Kotlin Multiplatform intermediate
#                          source sets do not expose distinct Gradle configurations, so
#                          this metric treats every source set of every downstream module
#                          as reachable. The §4.3 headline uses blast_radius_module; the
#                          source-set-axis discussion cites blast_radius_unit with this
#                          caveat.
#
# Output:
#   analysis/data/dependency_closure.json — keyed by "f{module}::{source_set}" → dict
#     with blast_radius_module (int), blast_radius_unit (int), downstream_sample
#     (up to 20 downstream unit keys).
#
# Deterministic, idempotent. Pure stdlib — no third-party deps beyond what figures.py
# already uses.
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


def _load_edges(path: Path) -> list[tuple[str, str]]:
    """Returns list of (source_module, target_module) edges. source_configuration and
    scope columns are not needed for module-level closure computation."""
    if not path.is_file():
        sys.stderr.write(
            f"[dependency_closure] ERROR: {path} not found. "
            f"Run ./gradlew dependencyGraphReport first.\n"
        )
        sys.exit(2)

    edges: list[tuple[str, str]] = []
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            src = row["source_module"]
            tgt = row["target_module"]
            if src == tgt:
                continue
            edges.append((src, tgt))
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


def _build_reverse_graph(edges: list[tuple[str, str]]) -> dict[str, set[str]]:
    """Returns target → set of direct upstream consumers. Inverts edges so we can
    walk downstream from a module M by following reverse edges."""
    reverse: dict[str, set[str]] = defaultdict(set)
    for src, tgt in edges:
        reverse[tgt].add(src)
    return reverse


def _transitive_dependents(
    start_module: str,
    reverse_graph: dict[str, set[str]],
    memo: dict[str, frozenset[str]],
) -> frozenset[str]:
    """Transitive closure of dependents. Memoized. Excludes start_module itself."""
    if start_module in memo:
        return memo[start_module]

    visited: set[str] = set()
    stack: list[str] = [start_module]
    while stack:
        current = stack.pop()
        for upstream in reverse_graph.get(current, ()):
            if upstream not in visited and upstream != start_module:
                visited.add(upstream)
                stack.append(upstream)

    result = frozenset(visited)
    memo[start_module] = result
    return result


def compute_closure(
    edges: list[tuple[str, str]],
    units: dict[str, list[str]],
) -> dict[str, dict]:
    reverse = _build_reverse_graph(edges)
    memo: dict[str, frozenset[str]] = {}

    result: dict[str, dict] = {}
    for module, source_sets in sorted(units.items()):
        dependents = _transitive_dependents(module, reverse, memo)
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
