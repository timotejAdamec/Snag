"""Unit tests for figures.compute_platform_reach_histogram.

Pure-function aggregator that buckets production Kotlin LOC by the number of
platforms a source set reaches. Input is the joined sharing-report DataFrame
(same schema as test_layer_divergence.py _row). Output bins run from reach=1
(platform-specific) to reach=6 (fe+be common). Every reach bucket is always
present in the output — empty bins materialise as 0 rows so reindex/plot code
can rely on stable shape.
"""
from __future__ import annotations

import pandas as pd

import figures


def _row(
    module_path: str,
    source_set: str,
    platform_set: str,
    kotlin_loc: int,
    *,
    category: str = "feat",
    hex_layer: str = "",
    plugin_applied: str = "",
) -> dict:
    return {
        "module_path": module_path,
        "category": category,
        "feature": "",
        "platform": "",
        "hex_layer": hex_layer,
        "encapsulation": "",
        "plugin_applied": plugin_applied,
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


# ------------------------------- schema --------------------------------------


def test_empty_input_returns_all_six_bins_with_zero_loc():
    result = figures.compute_platform_reach_histogram(_df([]))
    assert list(result.columns) == ["reach", "kotlin_loc", "source_set_count", "share"]
    assert list(result["reach"]) == [1, 2, 3, 4, 5, 6]
    assert result["kotlin_loc"].sum() == 0
    assert result["source_set_count"].sum() == 0
    # `share` divides by total LOC — guard against NaN when total is zero.
    assert result["share"].notna().all()
    assert (result["share"] == 0.0).all()


# ------------------------------- reach mapping -------------------------------


def test_commonMain_all_maps_to_reach_6():
    df = _df([_row(":feat:x:app:impl", "commonMain", "all", 100)])
    result = figures.compute_platform_reach_histogram(df)
    six = result[result["reach"] == 6].iloc[0]
    assert six["kotlin_loc"] == 100
    assert six["source_set_count"] == 1
    assert abs(six["share"] - 1.0) < 1e-9


def test_commonMain_frontend_maps_to_reach_5():
    df = _df([_row(":feat:x:fe:app:impl", "commonMain", "frontend", 100)])
    result = figures.compute_platform_reach_histogram(df)
    five = result[result["reach"] == 5].iloc[0]
    assert five["kotlin_loc"] == 100


def test_nonWebMain_frontend_maps_to_reach_3():
    # `nonWeb_fe` = 3-platform frontend intermediate.
    df = _df([_row(":feat:x:fe:app:impl", "nonWebMain", "nonWeb_fe", 42)])
    result = figures.compute_platform_reach_histogram(df)
    three = result[result["reach"] == 3].iloc[0]
    assert three["kotlin_loc"] == 42


def test_backend_main_maps_to_reach_1():
    df = _df([_row(":server", "main", "backend", 200, category="app")])
    result = figures.compute_platform_reach_histogram(df)
    one = result[result["reach"] == 1].iloc[0]
    assert one["kotlin_loc"] == 200


def test_webMain_web_maps_to_reach_2():
    df = _df([_row(":feat:x:fe:driving:impl", "webMain", "web", 33)])
    result = figures.compute_platform_reach_histogram(df)
    two = result[result["reach"] == 2].iloc[0]
    assert two["kotlin_loc"] == 33


# ------------------------------- aggregation ---------------------------------


def test_sum_across_sets_with_same_reach():
    df = _df([
        _row(":a", "commonMain", "all", 50),
        _row(":b", "commonMain", "all", 75),
    ])
    result = figures.compute_platform_reach_histogram(df)
    six = result[result["reach"] == 6].iloc[0]
    assert six["kotlin_loc"] == 125
    assert six["source_set_count"] == 2


def test_share_is_fraction_of_grand_total():
    # 100 LOC at reach 6, 300 at reach 1 → shares 0.25 and 0.75.
    df = _df([
        _row(":shared", "commonMain", "all", 100),
        _row(":server", "main", "backend", 300, category="app"),
    ])
    result = figures.compute_platform_reach_histogram(df)
    six = result[result["reach"] == 6].iloc[0]
    one = result[result["reach"] == 1].iloc[0]
    assert abs(six["share"] - 0.25) < 1e-9
    assert abs(one["share"] - 0.75) < 1e-9
    assert abs(result["share"].sum() - 1.0) < 1e-9


# ------------------------------- filter discipline ---------------------------


def test_empty_platform_set_rows_are_dropped():
    # Test source sets carry platform_set="" in the real CSV; they must not bleed
    # into the histogram.
    df = _df([
        _row(":a", "commonTest", "", 999),
        _row(":b", "commonMain", "all", 50),
    ])
    result = figures.compute_platform_reach_histogram(df)
    assert result["kotlin_loc"].sum() == 50


def test_test_source_sets_excluded_even_with_platform_set():
    # Belt-and-braces: if a Test set ever gets a platform_set label, still drop.
    df = _df([
        _row(":a", "commonTest", "all", 999),
        _row(":b", "commonMain", "all", 10),
    ])
    result = figures.compute_platform_reach_histogram(df)
    assert result["kotlin_loc"].sum() == 10


def test_unknown_platform_set_is_dropped_silently():
    # Future-proofing: an unknown reach label must not crash.
    df = _df([
        _row(":a", "strangeMain", "quantum_realm", 77),
        _row(":b", "commonMain", "all", 10),
    ])
    result = figures.compute_platform_reach_histogram(df)
    assert result["kotlin_loc"].sum() == 10
