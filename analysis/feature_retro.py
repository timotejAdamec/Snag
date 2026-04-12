#!/usr/bin/env python3
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Thesis: "Multiplatform snagging system with code sharing maximisation"
# Czech Technical University in Prague — Faculty of Information Technology
#
# Ripple classifier for thesis evaluation §4.3. Given a git ref (commit SHA or
# branch) and its base ref, derives the list of touched files, maps each to a
# (module, source_set) unit via longest-prefix match against a base-ref sharing
# snapshot, classifies each using ripple_rules.yaml, and emits:
#
#   --stub      Emit classifications/<change>.yaml with rule-matched entries
#               pre-populated and unmatched entries set to bucket: unclassified.
#               Hand-edit the yaml, then re-run with --finalize.
#
#   --finalize  Read the hand-edited yaml, reject any unclassified entry, emit
#               analysis/data/ripple_<change>_files.csv and
#               analysis/data/ripple_<change>_units.csv. Print the headline
#               "recurring intrinsic units = N" count to stdout.
#
# Usage:
#   python analysis/feature_retro.py --change <change_id> --ref <ref> \
#       [--base-ref main] [--base-snapshot <csv>] (--stub | --finalize)
#
# Renames are collapsed via `git diff-tree -M` so the collapsed-rename file count
# matches `git show --stat` after accounting for rename pairs.

from __future__ import annotations

import argparse
import csv
import fnmatch
import json
import re
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

import yaml

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent
DATA_DIR = SCRIPT_DIR / "data"
CLASSIFICATIONS_DIR = SCRIPT_DIR / "classifications"
RULES_YAML = SCRIPT_DIR / "ripple_rules.yaml"
DEFAULT_BASE_SNAPSHOT = DATA_DIR / "sharing_report_with_loc.csv"
DEFAULT_CLOSURE_JSON = DATA_DIR / "dependency_closure.json"

BUCKETS = {"local", "intrinsic", "collateral", "unclassified"}
CHANGE_KINDS = {"feature_remove", "feature_add", "entity_extend"}


# ------------------------------- data types ------------------------------------

@dataclass
class PrefixRow:
    module_path: str
    source_set: str
    source_set_dir_rel: str  # trailing slash stripped for prefix match


@dataclass
class Rule:
    id: str
    bucket: str
    recurring: bool
    reason: str
    path_glob: str
    module_glob: Optional[str] = None
    source_set: Optional[str] = None
    change_kind: Optional[str] = None


@dataclass
class TouchedFile:
    path: str             # repo-relative current path (new location after rename)
    rename_from: Optional[str]  # repo-relative old path if status == R
    status: str           # A / M / D / R / C
    added: int
    removed: int

    @property
    def loc_churn(self) -> int:
        return self.added + self.removed


@dataclass
class ClassifiedEntry:
    file: str
    unit: str
    status: str
    loc_churn: int
    bucket: str
    recurring: bool
    source: str
    reason: str
    blast_radius_module: int = 0
    blast_radius_unit: int = 0


# ------------------------------- loaders ---------------------------------------

def load_base_snapshot(path: Path) -> list[PrefixRow]:
    if not path.is_file():
        sys.exit(
            f"[feature_retro] ERROR: base snapshot {path} not found. Run "
            f"./gradlew sharingReport && analysis/loc_report.sh on the base ref first."
        )
    rows: list[PrefixRow] = []
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            rel = (row.get("source_set_dir_rel") or "").strip()
            if not rel:
                continue
            rows.append(
                PrefixRow(
                    module_path=row["module_path"],
                    source_set=row["source_set"],
                    source_set_dir_rel=rel.rstrip("/"),
                ),
            )
    # Longest prefix first — guarantees we never match `feat/projects/business` when
    # `feat/projects/business/model` is also present.
    rows.sort(key=lambda r: len(r.source_set_dir_rel), reverse=True)
    return rows


def load_rules(path: Path) -> list[Rule]:
    if not path.is_file():
        sys.exit(f"[feature_retro] ERROR: {path} not found.")
    with path.open(encoding="utf-8") as f:
        data = yaml.safe_load(f)
    if not data or "rules" not in data:
        return []
    rules: list[Rule] = []
    for raw in data["rules"]:
        rule = Rule(
            id=raw["id"],
            bucket=raw["bucket"],
            recurring=bool(raw["recurring"]),
            reason=raw["reason"],
            path_glob=raw["path_glob"],
            module_glob=raw.get("module_glob"),
            source_set=raw.get("source_set"),
            change_kind=raw.get("change_kind"),
        )
        if rule.bucket not in BUCKETS or rule.bucket == "unclassified":
            sys.exit(
                f"[feature_retro] ERROR: rule {rule.id} has invalid bucket "
                f"'{rule.bucket}'. Must be one of local/intrinsic/collateral."
            )
        if rule.change_kind is not None and rule.change_kind not in CHANGE_KINDS:
            sys.exit(
                f"[feature_retro] ERROR: rule {rule.id} has invalid change_kind "
                f"'{rule.change_kind}'."
            )
        rules.append(rule)
    return rules


def load_closure(path: Path) -> dict[str, dict]:
    if not path.is_file():
        sys.stderr.write(
            f"[feature_retro] WARN: closure file {path} missing — blast radius "
            f"will be reported as 0. Run analysis/dependency_closure.py first.\n"
        )
        return {}
    with path.open(encoding="utf-8") as f:
        return json.load(f)


# ------------------------------- git ops ---------------------------------------

def git_diff_files(base_ref: str, ref: str) -> list[TouchedFile]:
    """Collapses renames via -M and computes churn via --numstat. Returns one
    TouchedFile per changed file entry (renames are a single entry)."""
    name_status = subprocess.run(
        [
            "git",
            "diff-tree",
            "--no-commit-id",
            "--name-status",
            "-r",
            "-M",
            f"{base_ref}..{ref}",
        ],
        capture_output=True,
        text=True,
        check=True,
        cwd=REPO_ROOT,
    ).stdout

    numstat = subprocess.run(
        ["git", "diff", "--numstat", "-M", f"{base_ref}..{ref}"],
        capture_output=True,
        text=True,
        check=True,
        cwd=REPO_ROOT,
    ).stdout

    # numstat entries for renames use a "{old => new}" syntax that we normalize.
    churn: dict[str, tuple[int, int]] = {}
    for line in numstat.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        if len(parts) < 3:
            continue
        added_s, removed_s, path_field = parts[0], parts[1], parts[2]
        # Binary files report "-\t-\t<path>"; treat as zero churn.
        added = int(added_s) if added_s.isdigit() else 0
        removed = int(removed_s) if removed_s.isdigit() else 0
        # Normalize rename syntax: "foo/{old => new}/bar" → "foo/new/bar"
        normalized = _normalize_rename_path(path_field)
        churn[normalized] = (added, removed)

    results: list[TouchedFile] = []
    for line in name_status.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        status = parts[0]
        if status.startswith("R"):
            # Rename entry: "R100\told\tnew"
            old_path, new_path = parts[1], parts[2]
            added, removed = churn.get(new_path, (0, 0))
            results.append(
                TouchedFile(
                    path=new_path,
                    rename_from=old_path,
                    status="R",
                    added=added,
                    removed=removed,
                ),
            )
        else:
            path = parts[1]
            added, removed = churn.get(path, (0, 0))
            results.append(
                TouchedFile(
                    path=path,
                    rename_from=None,
                    status=status,
                    added=added,
                    removed=removed,
                ),
            )
    return results


_RENAME_BRACE_RE = re.compile(r"\{[^{}]* => ([^{}]*)\}")


def _normalize_rename_path(path_field: str) -> str:
    """Turns `foo/{old => new}/bar.kt` into `foo/new/bar.kt`. Handles multiple
    brace segments. The `numstat` format uses this encoding for renamed files so
    we can match against the `name_status` new-path values."""
    while True:
        match = _RENAME_BRACE_RE.search(path_field)
        if not match:
            return path_field
        path_field = path_field[: match.start()] + match.group(1) + path_field[match.end():]


# ------------------------------- mapping --------------------------------------

def map_path_to_unit(path: str, snapshot: list[PrefixRow]) -> tuple[str, str]:
    """Longest-prefix match. Returns (module_path, source_set) or a fallback unit."""
    for row in snapshot:
        prefix = row.source_set_dir_rel
        if path == prefix or path.startswith(prefix + "/"):
            return row.module_path, row.source_set

    # Literal fallbacks for repo-root files that don't live under a source set.
    if path == "settings.gradle.kts" or path.startswith("settings.gradle"):
        return ":root", "settings"
    if path.startswith("build-logic/"):
        return ":build-logic", "non-module"
    if path.startswith(".github/") or path.startswith("docs/") or path.startswith("analysis/"):
        return ":root", "non-module"
    return ":root", "non-module"


# ------------------------------- classification --------------------------------

def apply_rules(
    path: str,
    module_path: str,
    source_set: str,
    rules: list[Rule],
    change_kind: Optional[str],
    local_module_globs: list[str],
) -> Optional[tuple[str, bool, str, str]]:
    """Returns (bucket, recurring, source, reason) if a rule matches, else None."""
    # Local takes precedence over the rule file when a module is explicitly
    # listed as local in the per-change classification yaml.
    for glob in local_module_globs:
        if fnmatch.fnmatch(module_path, glob):
            return (
                "local",
                False,
                "hand:local_module_globs",
                f"Unit inside a module declared local to this change ({glob})",
            )

    for rule in rules:
        if rule.change_kind is not None and rule.change_kind != change_kind:
            continue
        if not fnmatch.fnmatch(path, rule.path_glob):
            continue
        if rule.module_glob is not None and not fnmatch.fnmatch(module_path, rule.module_glob):
            continue
        if rule.source_set is not None and rule.source_set != source_set:
            continue
        return (rule.bucket, rule.recurring, f"rule:{rule.id}", rule.reason)
    return None


# ------------------------------- stub / finalize -------------------------------

def cmd_stub(args: argparse.Namespace) -> int:
    snapshot = load_base_snapshot(Path(args.base_snapshot))
    rules = load_rules(RULES_YAML)
    closure = load_closure(Path(args.closure))
    touched = git_diff_files(args.base_ref, args.ref)

    entries: list[ClassifiedEntry] = []
    for touched_file in touched:
        # Deleted and renamed files only exist under their old path in the base
        # snapshot. Added files exist only at the experiment ref and will fall
        # through the prefix match to `:root::non-module` unless the change
        # classification lists their module in `local_module_globs`.
        lookup_path = touched_file.rename_from or touched_file.path
        module_path, source_set = map_path_to_unit(lookup_path, snapshot)

        unit = f"{module_path}::{source_set}"
        blast = closure.get(unit, {})

        display_path = (
            f"{touched_file.rename_from} -> {touched_file.path}"
            if touched_file.rename_from
            else touched_file.path
        )

        match = apply_rules(
            path=lookup_path,
            module_path=module_path,
            source_set=source_set,
            rules=rules,
            change_kind=args.change_kind,
            local_module_globs=args.local_module_globs or [],
        )

        if match is not None:
            bucket, recurring, source, reason = match
        else:
            bucket, recurring, source, reason = "unclassified", False, "", ""

        entries.append(
            ClassifiedEntry(
                file=display_path,
                unit=unit,
                status=touched_file.status,
                loc_churn=touched_file.loc_churn,
                bucket=bucket,
                recurring=recurring,
                source=source,
                reason=reason,
                blast_radius_module=blast.get("blast_radius_module", 0),
                blast_radius_unit=blast.get("blast_radius_unit", 0),
            ),
        )

    entries.sort(key=lambda e: e.file)
    write_stub_yaml(args.change, args.ref, args.base_ref, args.base_snapshot, entries)
    unclassified = sum(1 for e in entries if e.bucket == "unclassified")
    print(
        f"[feature_retro] stub: wrote {len(entries)} entries "
        f"({len(entries) - unclassified} rule-matched, {unclassified} unclassified)",
    )
    return 0


def cmd_finalize(args: argparse.Namespace) -> int:
    classification_path = CLASSIFICATIONS_DIR / f"{args.change}.yaml"
    if not classification_path.is_file():
        sys.exit(
            f"[feature_retro] ERROR: {classification_path} not found. "
            f"Run --stub first and hand-classify unclassified entries.",
        )

    closure = load_closure(Path(args.closure))

    with classification_path.open(encoding="utf-8") as f:
        data = yaml.safe_load(f)

    repair_log = data.get("repair_log", [])
    unclassified = [e for e in repair_log if e.get("bucket") == "unclassified"]
    if unclassified:
        sys.exit(
            f"[feature_retro] ERROR: {len(unclassified)} entries remain "
            f"'unclassified' in {classification_path}. Hand-classify them first.",
        )

    # Per-file CSV
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    files_csv = DATA_DIR / f"ripple_{args.change}_files.csv"
    with files_csv.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(
            [
                "file",
                "unit",
                "status",
                "loc_churn",
                "bucket",
                "recurring",
                "source",
                "reason",
                "blast_radius_module",
                "blast_radius_unit",
            ],
        )
        for entry in repair_log:
            unit = entry["unit"]
            blast = closure.get(unit, {})
            writer.writerow(
                [
                    entry["file"],
                    unit,
                    entry.get("status", ""),
                    entry.get("loc_churn", 0),
                    entry["bucket"],
                    bool(entry.get("recurring", False)),
                    entry.get("source", ""),
                    entry.get("reason", ""),
                    blast.get("blast_radius_module", 0),
                    blast.get("blast_radius_unit", 0),
                ],
            )

    # Per-unit aggregated CSV — file_count + loc_churn_sum + dominant bucket.
    units_csv = DATA_DIR / f"ripple_{args.change}_units.csv"
    aggregated: dict[str, dict] = {}
    for entry in repair_log:
        unit = entry["unit"]
        bucket = entry["bucket"]
        agg = aggregated.setdefault(
            unit,
            {
                "unit": unit,
                "file_count": 0,
                "loc_churn_sum": 0,
                "bucket_counts": {"local": 0, "intrinsic": 0, "collateral": 0},
                "recurring": False,
            },
        )
        agg["file_count"] += 1
        agg["loc_churn_sum"] += int(entry.get("loc_churn", 0))
        agg["bucket_counts"][bucket] = agg["bucket_counts"].get(bucket, 0) + 1
        if entry.get("recurring"):
            agg["recurring"] = True

    with units_csv.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(
            [
                "unit",
                "module_path",
                "source_set",
                "file_count",
                "loc_churn_sum",
                "dominant_bucket",
                "recurring",
                "blast_radius_module",
                "blast_radius_unit",
            ],
        )
        for unit in sorted(aggregated.keys()):
            agg = aggregated[unit]
            dominant = max(agg["bucket_counts"].items(), key=lambda kv: kv[1])[0]
            module_path, _, source_set = unit.partition("::")
            blast = closure.get(unit, {})
            writer.writerow(
                [
                    unit,
                    module_path,
                    source_set,
                    agg["file_count"],
                    agg["loc_churn_sum"],
                    dominant,
                    agg["recurring"],
                    blast.get("blast_radius_module", 0),
                    blast.get("blast_radius_unit", 0),
                ],
            )

    # Summary
    bucket_counts: dict[str, int] = {"local": 0, "intrinsic": 0, "collateral": 0}
    churn_counts: dict[str, int] = {"local": 0, "intrinsic": 0, "collateral": 0}
    for entry in repair_log:
        b = entry["bucket"]
        bucket_counts[b] = bucket_counts.get(b, 0) + 1
        churn_counts[b] = churn_counts.get(b, 0) + int(entry.get("loc_churn", 0))

    recurring_intrinsic_units = sum(
        1
        for agg in aggregated.values()
        if agg["bucket_counts"].get("intrinsic", 0) > 0 and agg["recurring"]
    )

    print(f"[feature_retro] finalized {args.change}")
    print(f"  files:  local={bucket_counts['local']}  intrinsic={bucket_counts['intrinsic']}  collateral={bucket_counts['collateral']}")
    print(f"  churn:  local={churn_counts['local']}  intrinsic={churn_counts['intrinsic']}  collateral={churn_counts['collateral']}")
    print(f"  recurring intrinsic units = {recurring_intrinsic_units}")
    print(f"  wrote {files_csv}")
    print(f"  wrote {units_csv}")
    return 0


def write_stub_yaml(
    change: str,
    ref: str,
    base_ref: str,
    base_snapshot: str,
    entries: list[ClassifiedEntry],
) -> None:
    CLASSIFICATIONS_DIR.mkdir(parents=True, exist_ok=True)
    path = CLASSIFICATIONS_DIR / f"{change}.yaml"
    payload = {
        "change_id": change,
        "ref": ref,
        "base_ref": base_ref,
        "base_sharing_snapshot": base_snapshot,
        "repair_log": [
            {
                "file": e.file,
                "unit": e.unit,
                "status": e.status,
                "loc_churn": e.loc_churn,
                "bucket": e.bucket,
                "recurring": e.recurring,
                "source": e.source,
                "reason": e.reason,
            }
            for e in entries
        ],
    }
    with path.open("w", encoding="utf-8") as f:
        yaml.safe_dump(payload, f, sort_keys=False, width=120)


# ------------------------------- cli -------------------------------------------

def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Ripple classifier for thesis §4.3")
    parser.add_argument("--change", required=True, help="Change identifier, e.g. inspections-reverse-removal")
    parser.add_argument("--ref", required=True, help="Git ref (SHA or branch) for the change")
    parser.add_argument("--base-ref", default="main", help="Git base ref (default: main)")
    parser.add_argument(
        "--base-snapshot",
        default=str(DEFAULT_BASE_SNAPSHOT),
        help="Sharing snapshot CSV taken on the base ref (default: analysis/data/sharing_report_with_loc.csv)",
    )
    parser.add_argument(
        "--closure",
        default=str(DEFAULT_CLOSURE_JSON),
        help="Dependency closure JSON (default: analysis/data/dependency_closure.json)",
    )
    parser.add_argument(
        "--change-kind",
        default=None,
        choices=sorted(CHANGE_KINDS),
        help="Change kind for rule filtering",
    )
    parser.add_argument(
        "--local-module-globs",
        nargs="*",
        default=None,
        help="Module path globs to treat as local to this change (e.g. ':feat:inspections:*')",
    )
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--stub", action="store_true", help="Emit stub classifications/<change>.yaml")
    mode.add_argument("--finalize", action="store_true", help="Emit ripple CSVs from hand-edited yaml")
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    if args.stub:
        return cmd_stub(args)
    if args.finalize:
        return cmd_finalize(args)
    parser.error("specify --stub or --finalize")
    return 2


if __name__ == "__main__":
    sys.exit(main())
