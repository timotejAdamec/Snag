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
# Pipeline:
#   1. Verify tokei is installed and reports the pinned version.
#   2. Read build/reports/sharing/sharing_report.csv.
#   3. For each unique (module_path, source_set) pair, run tokei on the source_set_dir,
#      extract Kotlin `code` and `files` counts.
#   4. Write analysis/data/loc_per_source_set.csv and the joined table.
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

# ---- per-source-set tokei walk ------------------------------------------------------------

# Parse the sharing report. Columns: module_path, category, feature, platform, hex_layer,
# encapsulation, plugin_applied, source_set, source_set_dir, source_set_dir_rel, platform_set.
# All Snag module paths are safe to parse with basic comma splitting — no embedded commas
# or quotes in the current codebase.

log "writing $LOC_PER_SS_CSV"
printf 'module_path,source_set,kotlin_loc,kotlin_files\n' > "$LOC_PER_SS_CSV"

ROW_COUNT=0
while IFS=, read -r module_path category feature platform hex_layer encapsulation plugin_applied source_set source_set_dir source_set_dir_rel platform_set; do
  # Skip header.
  if [[ "$module_path" == "module_path" ]]; then continue; fi

  if [[ ! -d "$source_set_dir" ]]; then
    # Defensive: Gradle task should never emit a nonexistent directory. Skip with warning.
    log "WARN: $source_set_dir does not exist (module $module_path source set $source_set) — skipping"
    continue
  fi

  # tokei JSON output shape (v14):
  #   { "Kotlin": { "code": N, "comments": N, "blanks": N, "reports": [ ... ] }, ... }
  # Extract Kotlin.code and the length of Kotlin.reports. When there are no Kotlin files,
  # the top-level "Kotlin" key is absent.
  tokei_json=$(tokei --output json "$source_set_dir" 2>/dev/null || echo '{}')
  kotlin_loc=$(echo "$tokei_json" | python3 -c '
import json, sys
data = json.load(sys.stdin)
kotlin = data.get("Kotlin", {})
print(kotlin.get("code", 0))
')
  kotlin_files=$(echo "$tokei_json" | python3 -c '
import json, sys
data = json.load(sys.stdin)
kotlin = data.get("Kotlin", {})
reports = kotlin.get("reports", [])
print(len(reports))
')

  printf '%s,%s,%s,%s\n' "$module_path" "$source_set" "$kotlin_loc" "$kotlin_files" >> "$LOC_PER_SS_CSV"
  ROW_COUNT=$((ROW_COUNT + 1))
done < "$SHARING_REPORT_CSV"

log "wrote $ROW_COUNT LOC rows"

# ---- join ---------------------------------------------------------------------------------

log "joining into $JOINED_CSV"
python3 - "$SHARING_REPORT_CSV" "$LOC_PER_SS_CSV" "$JOINED_CSV" <<'PY'
import csv
import sys

sharing_path, loc_path, joined_path = sys.argv[1:4]

with open(loc_path, newline="", encoding="utf-8") as f:
    loc_rows = list(csv.DictReader(f))

loc_index = {
    (row["module_path"], row["source_set"]): (int(row["kotlin_loc"]), int(row["kotlin_files"]))
    for row in loc_rows
}

with open(sharing_path, newline="", encoding="utf-8") as f_in, \
     open(joined_path, "w", newline="", encoding="utf-8") as f_out:
    reader = csv.DictReader(f_in)
    fieldnames = list(reader.fieldnames) + ["kotlin_loc", "kotlin_files"]
    writer = csv.DictWriter(f_out, fieldnames=fieldnames)
    writer.writeheader()
    for row in reader:
        key = (row["module_path"], row["source_set"])
        loc, files = loc_index.get(key, (0, 0))
        row["kotlin_loc"] = str(loc)
        row["kotlin_files"] = str(files)
        writer.writerow(row)
PY

log "done: $JOINED_CSV"
