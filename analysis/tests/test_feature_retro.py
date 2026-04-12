"""Unit tests for analysis/feature_retro.py.

These exercise the pure-function parts of the classifier — longest-prefix unit
mapping, rename path normalization, rule matching — without shelling out to git.
"""
from __future__ import annotations

import feature_retro
from feature_retro import PrefixRow, Rule, TouchedFile


# ------------------------------- longest-prefix match --------------------------

def _snapshot() -> list[PrefixRow]:
    # Deliberately unsorted on input — map_path_to_unit uses the sorted copy.
    rows = [
        PrefixRow(":feat:projects:business:model", "commonMain", "feat/projects/business/model/src/commonMain"),
        PrefixRow(":feat:projects:fe:app:impl", "commonMain", "feat/projects/fe/app/impl/src/commonMain"),
        PrefixRow(":feat:projects:fe:app:impl", "androidMain", "feat/projects/fe/app/impl/src/androidMain"),
        PrefixRow(":core:foundation:common", "commonMain", "core/foundation/common/src/commonMain"),
        PrefixRow(":koinModulesAggregate:fe", "commonMain", "koinModulesAggregate/fe/src/commonMain"),
    ]
    rows.sort(key=lambda r: len(r.source_set_dir_rel), reverse=True)
    return rows


def test_longest_prefix_picks_more_specific_module():
    snapshot = _snapshot()
    module, source_set = feature_retro.map_path_to_unit(
        "feat/projects/fe/app/impl/src/commonMain/kotlin/cz/.../ProjectSyncHandler.kt",
        snapshot,
    )
    assert module == ":feat:projects:fe:app:impl"
    assert source_set == "commonMain"


def test_longest_prefix_distinguishes_source_sets_in_same_module():
    snapshot = _snapshot()
    module, source_set = feature_retro.map_path_to_unit(
        "feat/projects/fe/app/impl/src/androidMain/kotlin/.../AndroidThing.kt",
        snapshot,
    )
    assert module == ":feat:projects:fe:app:impl"
    assert source_set == "androidMain"


def test_longest_prefix_for_unrelated_core_module():
    snapshot = _snapshot()
    module, source_set = feature_retro.map_path_to_unit(
        "core/foundation/common/src/commonMain/kotlin/.../Timestamp.kt",
        snapshot,
    )
    assert module == ":core:foundation:common"
    assert source_set == "commonMain"


def test_settings_gradle_falls_back_to_root_settings():
    snapshot = _snapshot()
    module, source_set = feature_retro.map_path_to_unit("settings.gradle.kts", snapshot)
    assert module == ":root"
    assert source_set == "settings"


def test_unknown_path_falls_back_to_root_non_module():
    snapshot = _snapshot()
    module, source_set = feature_retro.map_path_to_unit(
        "feat/brand-new-feature/src/commonMain/kotlin/Thing.kt",
        snapshot,
    )
    # The base snapshot was captured before this module existed, so the longest
    # prefix match fails and we fall back to :root::non-module. Phase 2 case 1
    # handles this via --local-module-globs instead.
    assert module == ":root"
    assert source_set == "non-module"


def test_docs_fall_back_to_root_non_module():
    snapshot = _snapshot()
    module, _ = feature_retro.map_path_to_unit("docs/architecture.md", snapshot)
    assert module == ":root"


# ------------------------------- rename path normalization ---------------------

def test_rename_path_single_brace_segment():
    assert (
        feature_retro._normalize_rename_path("feat/projects/{old => new}/File.kt")
        == "feat/projects/new/File.kt"
    )


def test_rename_path_no_braces_is_passthrough():
    assert feature_retro._normalize_rename_path("foo/bar/Baz.kt") == "foo/bar/Baz.kt"


def test_rename_path_multiple_brace_segments():
    # Git emits one brace per directory difference.
    result = feature_retro._normalize_rename_path("src/{old => new}/nested/{a => b}.kt")
    assert result == "src/new/nested/b.kt"


# ------------------------------- rule matching ---------------------------------

def _rules() -> list[Rule]:
    return [
        Rule(
            id="aggregation_koin_fe",
            bucket="intrinsic",
            recurring=True,
            reason="FE Koin aggregation",
            path_glob="koinModulesAggregate/fe/**",
        ),
        Rule(
            id="sync_handler_registry",
            bucket="intrinsic",
            recurring=True,
            reason="Sync handler registry",
            path_glob="**/sync/**SyncHandler*.kt",
        ),
        Rule(
            id="build_gradle_collateral",
            bucket="collateral",
            recurring=False,
            reason="build.gradle.kts edit",
            path_glob="**/build.gradle.kts",
        ),
    ]


def test_rule_matches_koin_aggregation_intrinsic_recurring():
    match = feature_retro.apply_rules(
        path="koinModulesAggregate/fe/src/commonMain/kotlin/.../FrontendModulesAggregate.kt",
        module_path=":koinModulesAggregate:fe",
        source_set="commonMain",
        rules=_rules(),
        change_kind=None,
        local_module_globs=[],
    )
    assert match is not None
    bucket, recurring, source, _reason = match
    assert bucket == "intrinsic"
    assert recurring is True
    assert source == "rule:aggregation_koin_fe"


def test_rule_matches_build_gradle_collateral():
    match = feature_retro.apply_rules(
        path="feat/projects/fe/app/impl/build.gradle.kts",
        module_path=":feat:projects:fe:app:impl",
        source_set="",
        rules=_rules(),
        change_kind=None,
        local_module_globs=[],
    )
    assert match is not None
    bucket, recurring, _source, _reason = match
    assert bucket == "collateral"
    assert recurring is False


def test_local_module_globs_take_precedence_over_rules():
    # A unit inside a locally-declared module should be tagged local even if a
    # rule would otherwise mark it intrinsic — Case 1 needs this for the deleted
    # :feat:inspections:* modules.
    match = feature_retro.apply_rules(
        path="feat/inspections/fe/app/impl/src/commonMain/kotlin/.../sync/InspectionSyncHandler.kt",
        module_path=":feat:inspections:fe:app:impl",
        source_set="commonMain",
        rules=_rules(),
        change_kind=None,
        local_module_globs=[":feat:inspections:*"],
    )
    assert match is not None
    bucket, _recurring, source, _reason = match
    assert bucket == "local"
    assert source == "hand:local_module_globs"


def test_rule_returns_none_when_nothing_matches():
    match = feature_retro.apply_rules(
        path="docs/random.md",
        module_path=":root",
        source_set="non-module",
        rules=_rules(),
        change_kind=None,
        local_module_globs=[],
    )
    assert match is None


# ------------------------------- touched file properties ----------------------

def test_touched_file_churn_is_added_plus_removed():
    tf = TouchedFile(path="a.kt", rename_from=None, status="M", added=12, removed=3)
    assert tf.loc_churn == 15
