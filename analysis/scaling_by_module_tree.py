#!/usr/bin/env python3
"""Phase 3 §D6 deliverable: per-module-tree scaling CSV.

For each top-level repo tree (core/, lib/, feat/, app/, business/, composeApp/,
wearApp/, build-logic/, koinModulesAggregate/, settings, ...), count the
production files with any line changed on the experiment branch."""
from __future__ import annotations

import csv
import sys
from collections import Counter
from pathlib import Path

INPUT_CSV = Path("analysis/data/ripple_wearos-project-list_files.csv")
OUTPUT_CSV = Path("analysis/data/ripple_wearos-project-list_by_module_tree.csv")


def tree_for(file: str) -> str:
    """Bucket a path into a top-level tree label."""
    if " -> " in file:
        file = file.split(" -> ", 1)[1]
    if file.startswith("core/"):
        return "core/"
    if file.startswith("lib/"):
        return "lib/"
    if file.startswith("feat/"):
        return "feat/"
    if file.startswith("app/"):
        return "app/"
    if file.startswith("business/"):
        return "business/"
    if file.startswith("composeApp/"):
        return "composeApp/"
    if file.startswith("wearApp/"):
        return "wearApp/"
    if file.startswith("androidApp/"):
        return "androidApp/"
    if file.startswith("build-logic/"):
        return "build-logic/"
    if file.startswith("koinModulesAggregate/"):
        return "koinModulesAggregate/"
    if file.startswith("testInfra/"):
        return "testInfra/"
    if file.startswith("server/"):
        return "server/"
    if file.startswith("gradle/"):
        return "gradle/"
    if file == "settings.gradle.kts" or file.startswith("settings.gradle"):
        return "settings.gradle.kts"
    return "other"


def main() -> int:
    if not INPUT_CSV.is_file():
        print(f"ERROR: {INPUT_CSV} not found", file=sys.stderr)
        return 1
    file_counts: Counter[str] = Counter()
    loc_counts: Counter[str] = Counter()
    intrinsic_files: Counter[str] = Counter()
    collateral_files: Counter[str] = Counter()
    local_files: Counter[str] = Counter()
    with INPUT_CSV.open(encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            tree = tree_for(row["file"])
            file_counts[tree] += 1
            loc_counts[tree] += int(row.get("loc_churn", "0") or 0)
            bucket = row["bucket"]
            if bucket == "intrinsic":
                intrinsic_files[tree] += 1
            elif bucket == "collateral":
                collateral_files[tree] += 1
            elif bucket == "local":
                local_files[tree] += 1
    OUTPUT_CSV.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_CSV.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["tree", "file_count", "loc_churn", "intrinsic_files", "collateral_files", "local_files"])
        # Sort by file_count desc for readability.
        for tree in sorted(file_counts.keys(), key=lambda k: (-file_counts[k], k)):
            writer.writerow([
                tree,
                file_counts[tree],
                loc_counts[tree],
                intrinsic_files[tree],
                collateral_files[tree],
                local_files[tree],
            ])
    print(f"wrote {OUTPUT_CSV}")
    print(f"trees with churn: {len(file_counts)}")
    for tree, count in sorted(file_counts.items(), key=lambda kv: (-kv[1], kv[0])):
        print(f"  {tree}: files={count} loc={loc_counts[tree]}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
