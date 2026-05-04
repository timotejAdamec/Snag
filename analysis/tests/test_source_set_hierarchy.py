"""Unit tests for analysis/source_set_hierarchy.py.

Encodes the dependsOn graph from
build-logic/.../MultiplatformModuleSetup.kt as a static lookup. Tests pin the
shape so that an accidental hierarchy change in build-logic without a matching
update here is caught immediately.
"""
from __future__ import annotations

import source_set_hierarchy as ssh


# ------------------------------- DESCENDANTS_MAIN -----------------------------

def test_common_main_descends_to_every_other_main_source_set():
    expected = {
        "mobileMain", "nonWebMain", "nonAndroidMain", "nonJvmMain",
        "androidMain", "iosMain", "jvmMain",
        "webMain", "jsMain", "wasmJsMain",
    }
    assert ssh.DESCENDANTS_MAIN["commonMain"] == frozenset(expected)


def test_mobile_main_descends_only_to_mobile_targets():
    assert ssh.DESCENDANTS_MAIN["mobileMain"] == frozenset({"androidMain", "iosMain"})


def test_non_web_main_descends_to_all_non_web_targets():
    assert ssh.DESCENDANTS_MAIN["nonWebMain"] == frozenset({
        "androidMain", "iosMain", "jvmMain",
    })


def test_non_android_main_descends_to_ios_jvm_and_web():
    # The wiring in MultiplatformModuleSetup.kt nests webMain under
    # nonAndroidMain, so webMain DOES descend from nonAndroidMain.
    assert ssh.DESCENDANTS_MAIN["nonAndroidMain"] == frozenset({
        "iosMain", "jvmMain", "webMain",
    })


def test_non_jvm_main_descends_to_android_ios_and_web():
    assert ssh.DESCENDANTS_MAIN["nonJvmMain"] == frozenset({
        "androidMain", "iosMain", "webMain",
    })


def test_web_main_has_no_descendants_in_snag_wiring():
    # Snag-specific quirk: jsMain and wasmJsMain are NOT wired as descendants
    # of webMain (only the default hierarchy template is applied for them, and
    # it does not nest js/wasmJs under web). Code authored in webMain therefore
    # reaches no platform binary in Snag's current wiring. The closure model
    # must reflect this faithfully.
    assert ssh.DESCENDANTS_MAIN["webMain"] == frozenset()


def test_leaf_main_source_sets_have_no_descendants():
    for leaf in ("androidMain", "iosMain", "jvmMain", "jsMain", "wasmJsMain", "main"):
        assert ssh.DESCENDANTS_MAIN[leaf] == frozenset(), (
            f"{leaf} should be a leaf in Snag's hierarchy"
        )


# ------------------------------- TARGET_BINS ----------------------------------

def test_common_main_reaches_all_five_kmp_targets():
    assert ssh.TARGET_BINS["commonMain"] == frozenset({
        "android", "ios", "jvm", "js", "wasmJs",
    })


def test_mobile_main_reaches_only_android_and_ios():
    assert ssh.TARGET_BINS["mobileMain"] == frozenset({"android", "ios"})


def test_non_web_main_reaches_android_ios_jvm():
    # Crucially includes 'jvm' — this is what makes a BE consumer's `main`
    # rebuild when a KMP module's nonWebMain ABI changes.
    assert ssh.TARGET_BINS["nonWebMain"] == frozenset({"android", "ios", "jvm"})


def test_non_android_main_reaches_ios_and_jvm_only():
    # NOT 'js'/'wasmJs' — webMain has no descendants so the web targets are
    # not reached transitively from nonAndroidMain either.
    assert ssh.TARGET_BINS["nonAndroidMain"] == frozenset({"ios", "jvm"})


def test_non_jvm_main_reaches_android_and_ios_only():
    # Same reasoning as nonAndroidMain — no path to js/wasmJs through webMain.
    assert ssh.TARGET_BINS["nonJvmMain"] == frozenset({"android", "ios"})


def test_web_main_reaches_no_targets_in_snag_wiring():
    assert ssh.TARGET_BINS["webMain"] == frozenset()


def test_target_bins_of_main_resolves_via_single_target_argument():
    assert ssh.target_bins_of("main", single_target="jvm") == frozenset({"jvm"})
    assert ssh.target_bins_of("main", single_target="android") == frozenset({"android"})


def test_target_bins_of_main_with_no_single_target_returns_empty():
    # Caller MUST pass single_target for `main` — without it, no propagation.
    assert ssh.target_bins_of("main") == frozenset()


def test_target_bins_of_unknown_source_set_returns_empty():
    assert ssh.target_bins_of("zzMain") == frozenset()


# ------------------------------- helpers --------------------------------------

def test_descendants_of_unknown_source_set_returns_empty():
    assert ssh.descendants_of("zzMain") == frozenset()


def test_descendants_includes_test_variants_for_completeness():
    # Test source sets are not used by the production closure walk but are
    # listed so an unfamiliar input does not silently degrade.
    assert "androidUnitTest" in ssh.DESCENDANTS["commonTest"]
    assert "jvmTest" in ssh.DESCENDANTS["commonTest"]
