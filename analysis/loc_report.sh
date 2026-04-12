#!/usr/bin/env bash
#
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Thesis: "Multiplatform snagging system with code sharing maximisation"
# Czech Technical University in Prague — Faculty of Information Technology
#
# Joins the sharingReport CSV (emitted by the Gradle SharingReportTask) with per-source-set
# Kotlin LOC counts from tokei, producing the canonical evaluation table at
# analysis/data/sharing_report_with_loc.csv.
#
# Usage: analysis/loc_report.sh [--quiet]
#
# Run from the repository root. Exit codes: 0 success, 1 bad environment, 2 input missing.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PINNED_VERSION_FILE="$SCRIPT_DIR/tokei_version.txt"
SHARING_REPORT_CSV="$REPO_ROOT/build/reports/sharing/sharing_report.csv"
DATA_DIR="$SCRIPT_DIR/data"
LOC_PER_SS_CSV="$DATA_DIR/loc_per_source_set.csv"
JOINED_CSV="$DATA_DIR/sharing_report_with_loc.csv"

QUIET=0
if [[ "${1:-}" == "--quiet" ]]; then
  QUIET=1
fi

log() {
  if [[ $QUIET -eq 0 ]]; then
    echo "[loc_report] $*" >&2
  fi
}

# ---- tokei availability and version pinning ------------------------------------------------

if ! command -v tokei >/dev/null 2>&1; then
  echo "[loc_report] ERROR: tokei is not installed. Install with: brew install tokei" >&2
  exit 1
fi

PINNED_VERSION=$(tr -d '[:space:]' < "$PINNED_VERSION_FILE")
LOCAL_VERSION=$(tokei --version | awk '{print $2}')

if [[ "$LOCAL_VERSION" != "$PINNED_VERSION" ]]; then
  echo "[loc_report] ERROR: tokei version mismatch." >&2
  echo "[loc_report]        pinned  = $PINNED_VERSION (from $PINNED_VERSION_FILE)" >&2
  echo "[loc_report]        installed = $LOCAL_VERSION" >&2
  echo "[loc_report]        Reproducibility of thesis numbers requires the pinned version." >&2
  exit 1
fi
log "tokei $LOCAL_VERSION (pinned) OK"

# ---- input validation ---------------------------------------------------------------------

if [[ ! -f "$SHARING_REPORT_CSV" ]]; then
  echo "[loc_report] ERROR: $SHARING_REPORT_CSV not found." >&2
  echo "[loc_report]        Run ./gradlew sharingReport first." >&2
  exit 2
fi

mkdir -p "$DATA_DIR"

# ---- tokei walk + join ---------------------------------------------------------------------
#
# One tokei invocation over every unique source_set_dir from the sharing report. Tokei
# aggregates Kotlin reports across all paths; we bucket each report back to its owning
# source set via longest-prefix match on the absolute source_set_dir column, then join
# the per-(module, source_set) LOC totals into the sharing report. Single subprocess
# spawn replaces the previous per-source-set loop (~930 spawns → 1).

python3 - "$SHARING_REPORT_CSV" "$LOC_PER_SS_CSV" "$JOINED_CSV" "$QUIET" <<'PY'
import csv
import json
import subprocess
import sys

sharing_path, loc_path, joined_path, quiet_flag = sys.argv[1:5]
quiet = quiet_flag == "1"


def log(msg: str) -> None:
    if not quiet:
        print(f"[loc_report] {msg}", file=sys.stderr)


with open(sharing_path, newline="", encoding="utf-8") as f:
    sharing_rows = list(csv.DictReader(f))

# Unique absolute source-set directories, skipping any that don't exist (defensive —
# the Gradle task should never emit a nonexistent dir, but filesystem races can happen).
unique_dirs: list[str] = []
seen: set[str] = set()
import os
for row in sharing_rows:
    d = row["source_set_dir"]
    if d in seen:
        continue
    seen.add(d)
    if os.path.isdir(d):
        unique_dirs.append(d)
    else:
        log(f"WARN: {d} does not exist — skipping")

log(f"running tokei over {len(unique_dirs)} source-set directories")
result = subprocess.run(
    ["tokei", "--output", "json", *unique_dirs],
    capture_output=True,
    text=True,
    check=True,
)
tokei_data = json.loads(result.stdout)
kotlin_reports = tokei_data.get("Kotlin", {}).get("reports", [])
log(f"tokei produced {len(kotlin_reports)} Kotlin file reports")

# Sort directories by length descending so the longest-prefix match wins when one
# source-set dir is a parent of another (e.g. `src/commonMain` vs `src/commonMain/kotlin`).
dirs_by_length = sorted(unique_dirs, key=len, reverse=True)

# Aggregate code + file count per source-set dir via longest-prefix on each report's name.
loc_by_dir: dict[str, int] = {d: 0 for d in unique_dirs}
files_by_dir: dict[str, int] = {d: 0 for d in unique_dirs}
for report in kotlin_reports:
    name = report["name"]
    for d in dirs_by_length:
        if name == d or name.startswith(d + "/"):
            loc_by_dir[d] += int(report["stats"]["code"])
            files_by_dir[d] += 1
            break

# Line endings match the previous pipeline byte-for-byte, which keeps committed
# data artifacts stable: loc_per_source_set used to be written by `printf '\n'`
# in bash (LF); the joined file used csv.DictWriter with its RFC 4180 default (CRLF).
with open(loc_path, "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f, lineterminator="\n")
    writer.writerow(["module_path", "source_set", "kotlin_loc", "kotlin_files"])
    for row in sharing_rows:
        d = row["source_set_dir"]
        writer.writerow(
            [
                row["module_path"],
                row["source_set"],
                loc_by_dir.get(d, 0),
                files_by_dir.get(d, 0),
            ],
        )
log(f"wrote {loc_path}")

with open(joined_path, "w", newline="", encoding="utf-8") as f_out:
    fieldnames = list(sharing_rows[0].keys()) + ["kotlin_loc", "kotlin_files"]
    writer = csv.DictWriter(f_out, fieldnames=fieldnames)
    writer.writeheader()
    for row in sharing_rows:
        d = row["source_set_dir"]
        out_row = dict(row)
        out_row["kotlin_loc"] = str(loc_by_dir.get(d, 0))
        out_row["kotlin_files"] = str(files_by_dir.get(d, 0))
        writer.writerow(out_row)
log(f"done: {joined_path}")
PY
