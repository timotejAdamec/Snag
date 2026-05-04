"""Unit tests for figures.compute_layer_divergence.

Exercises the pure-function aggregator without file IO — builds small in-memory
DataFrames, asserts the per-hex-layer share math matches hand computation. The
aggregator is the load-bearing half of Part A (descriptive per-hex-layer
platform-specific LOC share); the plotting function is a thin wrapper around
it and is not tested here.

Platform-specific = LOC outside `commonMain`. Backend-only `main` (reach 1)
counts as platform-specific, consistent with the reach-histogram convention
in §4.2 part B (`reach == 1` is "platformně specifické").
"""
from __future__ import annotations

import pandas as pd

import figures


SCHEMA_COLUMNS = [
    "hex_layer",
    "total_loc",
    "platform_specific_loc",
    "platform_specific_share",
]


def _row(
    module_path: str,
    hex_layer: str,
    source_set: str,
    kotlin_loc: int,
    *,
    category: str = "feat",
    platform_set: str = "frontend",
) -> dict:
    return {
        "module_path": module_path,
        "category": category,
        "feature": "",
        "platform": "",
        "hex_layer": hex_layer,
        "encapsulation": "",
        "plugin_applied": "",
        "source_set": source_set,
        "source_set_dir": "",
        "source_set_dir_rel": "",
        "platform_set": platform_set,
        "kotlin_loc": kotlin_loc,
        "kotlin_files": 1,
    }


def _df(rows: list[dict]) -> pd.DataFrame:
    if not rows:
        return pd.DataFrame(
            columns=[
                "module_path", "category", "feature", "platform", "hex_layer",
                "encapsulation", "plugin_applied", "source_set", "source_set_dir",
                "source_set_dir_rel", "platform_set", "kotlin_loc", "kotlin_files",
            ],
        )
    return pd.DataFrame(rows)


def test_empty_input_returns_empty_frame_without_nan():
    result = figures.compute_layer_divergence(_df([]))
    assert list(result.columns) == SCHEMA_COLUMNS
    for col in ("total_loc", "platform_specific_loc", "platform_specific_share"):
        assert result[col].notna().all()


def test_pure_common_layer_has_zero_share():
    df = _df([
        _row(":feat:x:app:impl", "app", "commonMain", 100),
    ])
    result = figures.compute_layer_divergence(df)
    app_row = result[result["hex_layer"] == "app"].iloc[0]
    assert app_row["total_loc"] == 100
    assert app_row["platform_specific_loc"] == 0
    assert app_row["platform_specific_share"] == 0.0


def test_pure_platform_specific_layer_has_full_share():
    df = _df([
        _row(":feat:x:fe:driving:impl", "driving", "webMain", 50),
    ])
    result = figures.compute_layer_divergence(df)
    driving_row = result[result["hex_layer"] == "driving"].iloc[0]
    assert driving_row["total_loc"] == 50
    assert driving_row["platform_specific_loc"] == 50
    assert driving_row["platform_specific_share"] == 1.0


def test_mixed_layer_matches_hand_computed_ratio():
    # 80 commonMain + 20 webMain + 50 commonMain → share = 20/150.
    df = _df([
        _row(":feat:x:fe:driven:impl", "driven", "commonMain", 80),
        _row(":feat:x:fe:driven:impl", "driven", "webMain", 20),
        _row(":feat:y:fe:driven:impl", "driven", "commonMain", 50),
    ])
    result = figures.compute_layer_divergence(df)
    driven_row = result[result["hex_layer"] == "driven"].iloc[0]
    assert driven_row["total_loc"] == 150
    assert driven_row["platform_specific_loc"] == 20
    assert abs(driven_row["platform_specific_share"] - 20 / 150) < 1e-9


def test_test_source_sets_and_empty_platform_set_are_dropped():
    # All rows are filtered out (test source sets + empty platform_set), so the
    # affected layer never appears in the result.
    df = _df([
        _row(":feat:x:app:impl", "app", "commonTest", 99),
        _row(":feat:x:app:impl", "app", "webTest", 99),
        _row(":feat:x:app:impl", "app", "nonWebTest", 99),
        _row(":feat:x:app:impl", "app", "jvmTest", 99),
        _row(":feat:x:app:impl", "app", "commonMain", 99, platform_set=""),
    ])
    result = figures.compute_layer_divergence(df)
    assert "app" not in set(result["hex_layer"])


def test_backend_main_source_set_counts_as_platform_specific():
    # Backend modules use `main` with platform_set=backend (reach 1). Under the
    # reach-based metric, this is platform-specific — same as a single-target
    # webMain or androidMain on the frontend.
    df = _df([
        _row(":feat:x:be:app:impl", "app", "main", 200, platform_set="backend"),
    ])
    result = figures.compute_layer_divergence(df)
    app_row = result[result["hex_layer"] == "app"].iloc[0]
    assert app_row["total_loc"] == 200
    assert app_row["platform_specific_loc"] == 200
    assert app_row["platform_specific_share"] == 1.0


def test_feat_module_without_hex_layer_or_contract_falls_back_to_feat_other():
    # `feat` category + empty hex_layer + no contract encapsulation → bucket
    # `feat (other)` (regression tripwire — the row should not be dropped).
    df = _df([
        _row(":feat:x:misc", "", "commonMain", 300, category="feat"),
    ])
    result = figures.compute_layer_divergence(df)
    fallback_row = result[result["hex_layer"] == "feat (other)"].iloc[0]
    assert fallback_row["total_loc"] == 300
