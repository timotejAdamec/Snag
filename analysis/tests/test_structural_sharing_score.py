"""Unit tests for figures.compute_structural_sharing_score.

Per-module classification: does the module's applied Gradle plugin belong to
the platform-neutral (KMP multiplatform) family? The score is simply
(# modules with a multiplatform plugin) / (# modules with any production LOC).

The function returns a DataFrame of per-hex-layer counts plus a final row
`"(celkem)"` carrying the global ratio, so §4.2 prose can cite both the
headline number and the per-layer decomposition.
"""
from __future__ import annotations

import pandas as pd

import figures


def _row(
    module_path: str,
    plugin_applied: str,
    source_set: str,
    platform_set: str,
    kotlin_loc: int,
    *,
    category: str = "feat",
    hex_layer: str = "",
    encapsulation: str = "",
) -> dict:
    return {
        "module_path": module_path,
        "category": category,
        "feature": "",
        "platform": "",
        "hex_layer": hex_layer,
        "encapsulation": encapsulation,
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


def test_empty_input_returns_header_row_only():
    result = figures.compute_structural_sharing_score(_df([]))
    assert list(result.columns) == [
        "layer",
        "multiplatform_modules",
        "total_modules",
        "share",
    ]
    # Global total row always present.
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["multiplatform_modules"] == 0
    assert totals["total_modules"] == 0
    assert totals["share"] == 0.0


# ------------------------------- classification ------------------------------


def test_multiplatform_module_counted_as_neutral():
    df = _df([_row(
        ":feat:x:app:impl",
        "libs.plugins.snag.multiplatform.module",
        "commonMain", "all", 100,
        hex_layer="app",
    )])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["multiplatform_modules"] == 1
    assert totals["total_modules"] == 1
    assert abs(totals["share"] - 1.0) < 1e-9


def test_backend_only_module_not_counted_as_neutral():
    df = _df([_row(
        ":server",
        "libs.plugins.snag.impl.driving.backend.module",
        "main", "backend", 200,
        category="app",
    )])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["multiplatform_modules"] == 0
    assert totals["total_modules"] == 1
    assert totals["share"] == 0.0


def test_frontend_multiplatform_counts_as_neutral():
    # Frontend KMP reaches 5 platforms — still a multiplatform plugin.
    df = _df([_row(
        ":feat:x:fe:driving:impl",
        "libs.plugins.snag.driving.frontend.multiplatform.module",
        "commonMain", "frontend", 50,
        hex_layer="driving",
    )])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["multiplatform_modules"] == 1
    assert totals["total_modules"] == 1


def test_module_with_empty_plugin_is_not_neutral():
    # :androidApp has no Snag plugin (plugin_applied == ""). It is a platform-
    # specific shell — must not count as multiplatform.
    df = _df([_row(
        ":androidApp",
        "",
        "main", "android", 35,
        category="app",
    )])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["multiplatform_modules"] == 0
    assert totals["total_modules"] == 1


# ------------------------------- module deduplication -----------------------


def test_module_with_multiple_source_sets_counted_once():
    df = _df([
        _row(
            ":feat:x:fe:driving:impl",
            "libs.plugins.snag.driving.frontend.multiplatform.module",
            "commonMain", "frontend", 60,
            hex_layer="driving",
        ),
        _row(
            ":feat:x:fe:driving:impl",
            "libs.plugins.snag.driving.frontend.multiplatform.module",
            "webMain", "web", 20,
            hex_layer="driving",
        ),
    ])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["multiplatform_modules"] == 1
    assert totals["total_modules"] == 1


# ------------------------------- filter discipline ---------------------------


def test_modules_with_only_test_or_zero_production_loc_excluded():
    # A module whose only rows are test sets or zero-LOC production — treat as
    # not-present (matches denominator convention of compute_layer_divergence).
    df = _df([
        _row(
            ":skeleton",
            "libs.plugins.snag.multiplatform.module",
            "commonTest", "", 99,
        ),
        _row(
            ":skeleton",
            "libs.plugins.snag.multiplatform.module",
            "commonMain", "all", 0,
            hex_layer="app",
        ),
        _row(
            ":real",
            "libs.plugins.snag.multiplatform.module",
            "commonMain", "all", 25,
            hex_layer="app",
        ),
    ])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    assert totals["total_modules"] == 1
    assert totals["multiplatform_modules"] == 1


# ------------------------------- per-layer breakdown ------------------------


def test_per_layer_rows_match_global_sum():
    df = _df([
        _row(
            ":feat:x:app:impl",
            "libs.plugins.snag.multiplatform.module",
            "commonMain", "all", 100,
            hex_layer="app",
        ),
        _row(
            ":feat:x:fe:driving:impl",
            "libs.plugins.snag.driving.frontend.multiplatform.module",
            "commonMain", "frontend", 50,
            hex_layer="driving",
        ),
        _row(
            ":server",
            "libs.plugins.snag.impl.driving.backend.module",
            "main", "backend", 200,
            category="app",
        ),
    ])
    result = figures.compute_structural_sharing_score(df)
    per_layer = result[result["layer"] != "(celkem)"]
    assert per_layer["multiplatform_modules"].sum() == 2
    assert per_layer["total_modules"].sum() == 3
    # Per-layer share is local (multiplatform / total in that layer).
    app_row = per_layer[per_layer["layer"] == "app"].iloc[0]
    assert abs(app_row["share"] - 1.0) < 1e-9
    driving_row = per_layer[per_layer["layer"] == "driving"].iloc[0]
    assert abs(driving_row["share"] - 1.0) < 1e-9
    top_row = per_layer[per_layer["layer"] == "app (top-level)"].iloc[0]
    assert top_row["share"] == 0.0


def test_global_share_matches_hand_computed_ratio():
    df = _df([
        _row(
            ":mp1",
            "libs.plugins.snag.multiplatform.module",
            "commonMain", "all", 10, hex_layer="app",
        ),
        _row(
            ":mp2",
            "libs.plugins.snag.frontend.multiplatform.module",
            "commonMain", "frontend", 10, hex_layer="driven",
        ),
        _row(
            ":be1",
            "libs.plugins.snag.backend.module",
            "main", "backend", 10,
        ),
        _row(
            ":be2",
            "libs.plugins.snag.driven.backend.module",
            "main", "backend", 10,
        ),
    ])
    result = figures.compute_structural_sharing_score(df)
    totals = result[result["layer"] == "(celkem)"].iloc[0]
    # 2 multiplatform of 4 total = 0.5.
    assert totals["multiplatform_modules"] == 2
    assert totals["total_modules"] == 4
    assert abs(totals["share"] - 0.5) < 1e-9
