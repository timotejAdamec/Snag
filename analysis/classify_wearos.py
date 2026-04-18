#!/usr/bin/env python3
"""Phase 3 helper: hand-classify the 81 unclassified entries from
analysis/classifications/wearos-project-list.yaml using path patterns.

Run from chore worktree root."""
from __future__ import annotations

import re
import sys
from pathlib import Path

import yaml

CLASSIFICATION = Path("analysis/classifications/wearos-project-list.yaml")


def classify(file: str, status: str) -> tuple[str, bool, str] | None:
    """Return (bucket, recurring, reason) or None to leave unclassified."""
    # Renames carry "old -> new" in `file`; classify by the new (post-rename) path.
    if " -> " in file:
        file = file.split(" -> ", 1)[1]
    # Wear OS new platform driven adapter — AVT (port substitution) for auth.
    if file.startswith("feat/authentication/fe/wear/driven/"):
        return ("intrinsic", True,
                "Per-new-platform driven adapter substituting under existing AuthTokenProvider port (AVT). "
                "Recurs per platform requiring auth.")
    # Wear OS new platform driving (UI) — per-feature per-platform.
    if file.startswith("feat/projects/fe/wear/driving/"):
        return ("intrinsic", True,
                "Per-new-platform driving sibling for the projects feature. "
                "Recurs per (feature, platform) pair shipped on a Wear-class target.")
    # wearApp shell — per-platform-app composition (local, one platform).
    if file.startswith("wearApp/"):
        return ("local", False,
                "Wear OS platform-app composition shell — local to the new-platform module.")
    # nonWear sibling — extracted phone-Android binding from the previously-shared common module.
    # Recurs per Wear-affected feature: each feature must extract the phone-only adapter.
    if file.startswith("feat/authentication/fe/nonWear/driven/"):
        return ("collateral", True,
                "Mobile-extraction sibling: holds AuthTokenProvider binding previously over-shared in common. "
                "Recurs per feature requiring per-platform binding.")
    # common/driven file edits — moved AuthTokenProvider binding out (collateral structural).
    if file.startswith("feat/authentication/fe/common/driven/"):
        # Renames are collateral (file moved, not edited).
        if status == "R":
            return ("collateral", False,
                    "Git rename: feat/authentication/fe/driven/impl → feat/authentication/fe/common/driven "
                    "(D8 variant-first refactor). Structural, no code semantics change.")
        # New file (the empty common module after stripping AuthTokenProvider binding).
        if status == "A":
            return ("collateral", False,
                    "Common Koin module after platform-extraction refactor: AuthTokenProvider binding "
                    "moved out to nonWear/wear siblings. Predates Phase 3; surfaced by Wear extension.")
        # Modified file — the AuthTokenProvider binding removal + visibility widening.
        return ("collateral", False,
                "Common Koin module: AuthTokenProvider binding removed (now per-variant); "
                "Mock/Oidc class visibility widened so siblings can bind them. "
                "Both edits surfaced by Wear extension; tracked as collateral structural cost.")
    # build-logic ArchCheck parser — recognises new platform-variant tokens.
    # One-time per codebase (added 'common' / 'nonWear' / 'wear' as PlatformVariant).
    if file.startswith("build-logic/"):
        return ("collateral", False,
                "ArchCheck parser updates to recognise variant-suffix tokens (common/nonWear/wear). "
                "One-time per codebase (parser concept generalises).")
    # Composer App AppModule — switch from frontendModulesAggregate to frontendModulesCommonAggregate.
    if file == "composeApp/src/commonMain/kotlin/cz/adamec/timotej/snag/di/AppModule.kt":
        return ("collateral", False,
                "Phone-app composition root: aggregate val rename + nonWear aggregate added. "
                "One-time per codebase consumer rewire after D8 refactor.")
    if file == "composeApp/build.gradle.kts":
        return ("collateral", False,
                "composeApp build deps adjusted for variant-suffix module paths.")
    # gradle/libs.versions.toml — added wear-phone-interactions catalog entry.
    if file == "gradle/libs.versions.toml":
        return ("collateral", False,
                "Added androidx.wear:wear-phone-interactions catalog entry (one-time per platform).")
    # testInfra — likely refactor for new package paths.
    if file.startswith("testInfra/"):
        return ("collateral", False,
                "Test infra updated for renamed Koin val (frontendModulesAggregate → frontendModulesCommonAggregate).")
    # Renames from projects driving/impl → projects/nonWear/driving — D8 mobile extraction.
    if "feat/projects/fe/nonWear/driving/" in file or file.startswith("feat/projects/fe/nonWear/driving/"):
        if status == "R":
            return ("collateral", False,
                    "Git rename: feat/projects/fe/driving/impl → feat/projects/fe/nonWear/driving "
                    "(D8 mobile-extraction). Structural, no code semantics change.")
        if status == "A":
            return ("collateral", False,
                    "New nonWear driving file post-extraction. Mobile-only by virtue of variant-suffix module.")
        return ("collateral", False,
                "nonWear driving file modified during D8 mobile-extraction refactor.")
    # Renames into projects/common/driving — D8 mobile-extraction (VMs split into common).
    if file.startswith("feat/projects/fe/common/driving/"):
        if status == "R":
            return ("collateral", False,
                    "Git rename: feat/projects/fe/driving/impl → feat/projects/fe/common/driving "
                    "(D8 split — VMs/UiState moved to common). Structural.")
        return ("collateral", False,
                "Common projects driving file after D8 split.")
    # Anything still in old projects driving/impl path is a deletion (the rename's old side).
    if file.startswith("feat/projects/fe/driving/impl/") or file.startswith("feat/authentication/fe/driven/impl/"):
        if status == "D":
            return ("collateral", False,
                    "Old impl-module path file removed; content moved to common/nonWear/wear sibling.")
    # Aggregates: koinModulesAggregate variant siblings.
    if file.startswith("koinModulesAggregate/"):
        return ("intrinsic", True,
                "Koin aggregate variant siblings (common/nonWear/wear) — each platform variant is wired here.")
    # Anything else: leave unclassified for human review.
    return None


def main() -> int:
    if not CLASSIFICATION.is_file():
        print(f"ERROR: {CLASSIFICATION} not found", file=sys.stderr)
        return 1
    with CLASSIFICATION.open(encoding="utf-8") as f:
        data = yaml.safe_load(f)

    classified = 0
    skipped = 0
    for entry in data["repair_log"]:
        if entry["bucket"] != "unclassified":
            continue
        result = classify(entry["file"], entry.get("status", ""))
        if result is None:
            skipped += 1
            continue
        bucket, recurring, reason = result
        entry["bucket"] = bucket
        entry["recurring"] = recurring
        entry["source"] = "hand:wearos_classifier"
        entry["reason"] = reason
        classified += 1

    with CLASSIFICATION.open("w", encoding="utf-8") as f:
        yaml.safe_dump(data, f, sort_keys=False, width=120, allow_unicode=True)

    print(f"classified={classified} skipped={skipped}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
