"""Unit tests for figures.compute_layer_divergence.

Exercises the pure-function aggregator without file IO — builds small in-memory
DataFrames, asserts the per-hex-layer share / module-count math matches hand
computation. The aggregator is the load-bearing half of Part A (descriptive
per-hex-layer platform-specific LOC share); the plotting function is a thin
wrapper around it and is not tested here.
"""
from __future__ import annotations

import pandas as pd

import figures


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


# ------------------------------- empty input ---------------------------------

def test_empty_input_returns_empty_frame_without_nan():
    result = figures.compute_layer_divergence(_df([]))
    assert list(result.columns) == [
        "hex_layer",
        "total_loc",
        "platform_specific_loc",
        "platform_specific_share",
        "divergent_module_count",
        "total_module_count",
    ]
    # Every numeric cell must be finite (no NaN from zero-division guards).
    for col in ("total_loc", "platform_specific_loc", "platform_specific_share",
                "divergent_module_count", "total_module_count"):
        assert result[col].notna().all()


# ------------------------------- pure-common layer ---------------------------

def test_pure_common_layer_has_zero_share_and_zero_divergent():
    df = _df([
        _row(":feat:x:app:impl", "app", "commonMain", 100),
    ])
    result = figures.compute_layer_divergence(df)
    app_row = result[result["hex_layer"] == "app"].iloc[0]
    assert app_row["total_loc"] == 100
    assert app_row["platform_specific_loc"] == 0
    assert app_row["platform_specific_share"] == 0.0
    assert app_row["divergent_module_count"] == 0
    assert app_row["total_module_count"] == 1


# ------------------------------- pure-platform-specific layer ----------------

def test_pure_platform_specific_layer_has_full_share():
    df = _df([
        _row(":feat:x:fe:driving:impl", "driving", "webMain", 50),
    ])
    result = figures.compute_layer_divergence(df)
    driving_row = result[result["hex_layer"] == "driving"].iloc[0]
    assert driving_row["total_loc"] == 50
    assert driving_row["platform_specific_loc"] == 50
    assert driving_row["platform_specific_share"] == 1.0
    assert driving_row["divergent_module_count"] == 1
    assert driving_row["total_module_count"] == 1


# ------------------------------- mixed layer ---------------------------------

def test_mixed_layer_matches_hand_computed_ratio():
    # Module A: 80 commonMain + 20 webMain → divergent.
    # Module B: 50 commonMain only → NOT divergent (still counts in total).
    df = _df([
        _row(":feat:x:fe:driven:impl", "driven", "commonMain", 80),
        _row(":feat:x:fe:driven:impl", "driven", "webMain", 20),
        _row(":feat:y:fe:driven:impl", "driven", "commonMain", 50),
    ])
    result = figures.compute_layer_divergence(df)
    driven_row = result[result["hex_layer"] == "driven"].iloc[0]
    assert driven_row["total_loc"] == 150
    assert driven_row["platform_specific_loc"] == 20
    # 20/150 = 0.1333...
    assert abs(driven_row["platform_specific_share"] - 20 / 150) < 1e-9
    assert driven_row["divergent_module_count"] == 1
    assert driven_row["total_module_count"] == 2


# ------------------------------- filter discipline --------------------------

def test_test_source_sets_and_empty_platform_set_are_dropped():
    # Everything here should be discarded — result must have all-zero counts
    # for the affected layer.
    df = _df([
        _row(":feat:x:app:impl", "app", "commonTest", 99),
        _row(":feat:x:app:impl", "app", "webTest", 99),
        _row(":feat:x:app:impl", "app", "nonWebTest", 99),
        _row(":feat:x:app:impl", "app", "jvmTest", 99),
        # Empty platform_set — rows that carry no LOC semantics per figures.py heatmap.
        _row(":feat:x:app:impl", "app", "commonMain", 99, platform_set=""),
    ])
    result = figures.compute_layer_divergence(df)
    app_row = result[result["hex_layer"] == "app"].iloc[0]
    assert app_row["total_loc"] == 0
    assert app_row["platform_specific_loc"] == 0
    assert app_row["platform_specific_share"] == 0.0
    assert app_row["total_module_count"] == 0


def test_main_source_set_counts_as_neutral_like_commonMain():
    # Backend modules use "main", which should be treated identically to commonMain.
    df = _df([
        _row(":feat:x:be:app:impl", "app", "main", 200, platform_set="backend"),
    ])
    result = figures.compute_layer_divergence(df)
    app_row = result[result["hex_layer"] == "app"].iloc[0]
    assert app_row["total_loc"] == 200
    assert app_row["platform_specific_loc"] == 0
    assert app_row["platform_specific_share"] == 0.0
    assert app_row["divergent_module_count"] == 0


def test_empty_hex_layer_grouped_as_other():
    df = _df([
        _row(":core:foundation:common", "", "commonMain", 300, category="core"),
    ])
    result = figures.compute_layer_divergence(df)
    other_row = result[result["hex_layer"] == "other"].iloc[0]
    assert other_row["total_loc"] == 300
    assert other_row["total_module_count"] == 1
