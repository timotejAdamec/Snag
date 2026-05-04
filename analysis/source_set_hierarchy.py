#!/usr/bin/env python3
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Thesis: "Multiplatform snagging system with code sharing maximisation"
# Czech Technical University in Prague — Faculty of Information Technology
#
# Encodes Snag's KMP source-set hierarchy as a static lookup.
#
# `DESCENDANTS[ss]` = source-set names that descend from `ss` via
# `dependsOn` chains in `MultiplatformModuleSetup.kt`. When `ss` changes,
# every descendant in the same module sees the change because the
# dependsOn edge means the descendant's compile classpath includes the
# ancestor's klib.
#
# `TARGET_BINS[ss]` = the platform-target binaries that `ss`'s code ends up
# in. Used to decide whether a single-target consumer (BE module's JVM
# `main`, Android-app module's android `main`) rebuilds when a KMP module's
# source-set ABI changes — the consumer rebuilds iff the source set reaches
# its target.
#
# Notable Snag-specific quirk: `webMain` is wired into the hierarchy as a
# CHILD of `nonAndroidMain` and `nonJvmMain` (so `webMain` SEES symbols
# from those parents), but `jsMain` and `wasmJsMain` are NOT declared as
# children of `webMain`. They sit directly under `commonMain` via the
# default hierarchy template. Therefore code authored in `webMain` does
# not reach any platform binary in Snag's current wiring — `webMain` has
# no descendants and reaches no targets. This is encoded faithfully here;
# the implication is logged in CLAUDE.md as a project oddity.

from __future__ import annotations

# Per the dependsOn graph in MultiplatformModuleSetup.kt:
#   mobileMain, nonWebMain, nonAndroidMain, nonJvmMain → commonMain
#   androidMain → mobileMain, nonWebMain, nonJvmMain
#   iosMain → mobileMain, nonWebMain, nonAndroidMain, nonJvmMain
#   jvmMain → nonWebMain, nonAndroidMain
#   webMain → nonAndroidMain, nonJvmMain
#   jsMain, wasmJsMain → commonMain (default hierarchy template only)
DESCENDANTS_MAIN: dict[str, frozenset[str]] = {
    "commonMain": frozenset({
        "mobileMain", "nonWebMain", "nonAndroidMain", "nonJvmMain",
        "androidMain", "iosMain", "jvmMain",
        "webMain", "jsMain", "wasmJsMain",
    }),
    "mobileMain": frozenset({"androidMain", "iosMain"}),
    "nonWebMain": frozenset({"androidMain", "iosMain", "jvmMain"}),
    "nonAndroidMain": frozenset({"iosMain", "jvmMain", "webMain"}),
    "nonJvmMain": frozenset({"androidMain", "iosMain", "webMain"}),
    "webMain": frozenset(),
    "androidMain": frozenset(),
    "iosMain": frozenset(),
    "jvmMain": frozenset(),
    "jsMain": frozenset(),
    "wasmJsMain": frozenset(),
    "main": frozenset(),
}

# Test source sets mirror Main but are parallel — they don't feed production
# binaries, only test compilations. Listed for completeness so an empty lookup
# never silently degrades to "no descendants" when an unfamiliar test source
# set shows up in inputs.
DESCENDANTS_TEST: dict[str, frozenset[str]] = {
    "commonTest": frozenset({
        "mobileTest", "nonWebTest", "nonAndroidTest", "nonJvmTest",
        "androidUnitTest", "iosTest", "jvmTest",
        "webTest", "jsTest", "wasmJsTest",
    }),
    "mobileTest": frozenset({"androidUnitTest", "iosTest"}),
    "nonWebTest": frozenset({"androidUnitTest", "iosTest", "jvmTest"}),
    "nonAndroidTest": frozenset({"iosTest", "jvmTest", "webTest"}),
    "nonJvmTest": frozenset({"androidUnitTest", "iosTest", "webTest"}),
    "webTest": frozenset(),
    "androidUnitTest": frozenset(),
    "iosTest": frozenset(),
    "jvmTest": frozenset(),
    "jsTest": frozenset(),
    "wasmJsTest": frozenset(),
    "test": frozenset(),
}

DESCENDANTS: dict[str, frozenset[str]] = {**DESCENDANTS_MAIN, **DESCENDANTS_TEST}

# Target binaries each source set's code reaches. `webMain` reaches none
# because Snag's wiring does not declare `jsMain`/`wasmJsMain` as
# descendants of `webMain` (see module-level comment).
TARGET_BINS: dict[str, frozenset[str]] = {
    "commonMain": frozenset({"android", "ios", "jvm", "js", "wasmJs"}),
    "mobileMain": frozenset({"android", "ios"}),
    "nonWebMain": frozenset({"android", "ios", "jvm"}),
    "nonAndroidMain": frozenset({"ios", "jvm"}),
    "nonJvmMain": frozenset({"android", "ios"}),
    "webMain": frozenset(),
    "androidMain": frozenset({"android"}),
    "iosMain": frozenset({"ios"}),
    "jvmMain": frozenset({"jvm"}),
    "jsMain": frozenset({"js"}),
    "wasmJsMain": frozenset({"wasmJs"}),
    # `main` is overloaded by plugin: BE (`snagBackendModule`) → jvm target;
    # Android app (`com.android.application`) → android target. Disambiguated
    # at the call site via per-module classification, not at this lookup.
}


def descendants_of(source_set: str) -> frozenset[str]:
    """Returns source sets that descend from `source_set` in Snag's KMP
    hierarchy. Empty for unknown source sets (caller treats unknowns as
    leaves)."""
    return DESCENDANTS.get(source_set, frozenset())


def target_bins_of(source_set: str, single_target: str | None = None) -> frozenset[str]:
    """Returns target-binary names this source set reaches. For overloaded
    `main`, the caller passes `single_target` ('jvm' for BE, 'android' for
    app modules) which is returned as the singleton bin set."""
    if source_set == "main" or source_set == "test":
        return frozenset({single_target}) if single_target else frozenset()
    return TARGET_BINS.get(source_set, frozenset())
