#!/usr/bin/env python3
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# Thesis: "Multiplatform snagging system with code sharing maximisation"
# Czech Technical University in Prague — Faculty of Information Technology
#
# Generates the figures for Kapitola 4: Vyhodnocení from the joined sharing-report CSV
# produced by analysis/loc_report.sh. Every thesis figure that references measured data is
# produced by a function in this file so that rerunning `python analysis/figures.py` from a
# fresh clone reproduces the chapter.
#
# Phase 1 scope: the layer × source-set LOC heatmap (fig. 4.2, headline §4.2 figure).
# Everything else is a stub function documenting the input columns and planned output — those
# are filled in during Phase 4 once §4.3/§4.4 data lands.
#
# Usage: python analysis/figures.py
#
# Depends on the joined table at analysis/data/sharing_report_with_loc.csv produced by
# analysis/loc_report.sh. Fails loudly if the input file is missing.

from __future__ import annotations

import sys
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

ANALYSIS_DIR = Path(__file__).resolve().parent
DATA_DIR = ANALYSIS_DIR / "data"
FIGURES_DIR = ANALYSIS_DIR / "figures"
JOINED_CSV = DATA_DIR / "sharing_report_with_loc.csv"


# ---------------------------------------------------------------------------------------------
# Input loading
# ---------------------------------------------------------------------------------------------


def load_joined_table() -> pd.DataFrame:
    if not JOINED_CSV.exists():
        raise SystemExit(
            f"Missing {JOINED_CSV}. Run ./gradlew sharingReport && analysis/loc_report.sh first."
        )
    df = pd.read_csv(JOINED_CSV, dtype=str).fillna("")
    df["kotlin_loc"] = df["kotlin_loc"].astype(int)
    df["kotlin_files"] = df["kotlin_files"].astype(int)
    return df


# ---------------------------------------------------------------------------------------------
# Hex layer derivation
# ---------------------------------------------------------------------------------------------
#
# The `hex_layer` column emitted by SharingReportTask is blank for some rows that still
# conceptually belong to a layer — contract modules carry `encapsulation=contract` but no
# hex layer, model modules carry no hex layer at all, core/lib/infra/app modules carry no
# feature-layer concept. For the §4.2 heatmap we introduce a derived layer label so every
# LOC row lands in exactly one row of the matrix.


LAYER_ORDER = [
    "business",
    "app",
    "ports",
    "driving",
    "driven",
    "contract",
    "other",
]


def derive_layer(row: pd.Series) -> str:
    hex_layer = row["hex_layer"]
    if hex_layer:
        return hex_layer
    if row["encapsulation"] == "contract":
        return "contract"
    return "other"


# ---------------------------------------------------------------------------------------------
# Source-set ordering (ascending platform reach)
# ---------------------------------------------------------------------------------------------
#
# Source sets are ordered left-to-right from "most shared" (commonMain) to "least shared"
# (platform-specific main), then test source sets grouped on the right. The exact ordering
# reflects the plan's narrative: LOC that sits in a more-shared source set contributes to
# more platforms, so placing commonMain on the left gives the reader a left-to-right "reach
# decreasing" reading axis.


SOURCE_SET_ORDER = [
    "commonMain",
    "nonWebMain",
    "mobileMain",
    "nonAndroidMain",
    "nonJvmMain",
    "webMain",
    "androidMain",
    "iosMain",
    "jvmMain",
    "jsMain",
    "wasmJsMain",
    "main",  # plain JVM backend
    "commonTest",
    "nonWebTest",
    "mobileTest",
    "nonAndroidTest",
    "nonJvmTest",
    "webTest",
    "androidUnitTest",
    "androidInstrumentedTest",
    "iosTest",
    "jvmTest",
    "jsTest",
    "wasmJsTest",
    "test",
]


def order_source_sets(present: list[str]) -> list[str]:
    ordered = [s for s in SOURCE_SET_ORDER if s in present]
    extras = sorted(s for s in present if s not in SOURCE_SET_ORDER)
    return ordered + extras


# ---------------------------------------------------------------------------------------------
# Figure 4.2 — layer × source-set LOC heatmap
# ---------------------------------------------------------------------------------------------


def figure_layer_source_set_heatmap(df: pd.DataFrame) -> None:
    production = df[~df["source_set"].str.contains("Test") & (df["source_set"] != "test")].copy()
    production["layer"] = production.apply(derive_layer, axis=1)

    matrix = production.pivot_table(
        index="layer",
        columns="source_set",
        values="kotlin_loc",
        aggfunc="sum",
        fill_value=0,
    )

    rows = [layer for layer in LAYER_ORDER if layer in matrix.index]
    cols = order_source_sets(list(matrix.columns))
    matrix = matrix.reindex(index=rows, columns=cols, fill_value=0)

    fig_height = max(4.5, 0.55 * len(rows) + 2.5)
    fig_width = max(9.0, 0.72 * len(cols) + 2.0)
    fig, ax = plt.subplots(figsize=(fig_width, fig_height))

    data = matrix.values
    im = ax.imshow(data, aspect="auto", cmap="YlOrRd")

    ax.set_xticks(np.arange(len(cols)))
    ax.set_xticklabels(cols, rotation=45, ha="right")
    ax.set_yticks(np.arange(len(rows)))
    ax.set_yticklabels(rows)
    ax.set_xlabel("Source set")
    ax.set_ylabel("Architecture layer")
    ax.set_title(
        "LOC per (architecture layer × source set) — production code",
        pad=14,
    )

    max_val = data.max() if data.size else 0
    threshold = max_val * 0.55
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            val = int(data[i, j])
            if val == 0:
                continue
            color = "white" if val >= threshold else "black"
            ax.text(j, i, f"{val:,}", ha="center", va="center", fontsize=8, color=color)

    col_totals = data.sum(axis=0)
    row_totals = data.sum(axis=1)
    grand_total = int(data.sum())

    ax.set_xticks(
        np.arange(len(cols)),
        labels=[f"{c}\n{int(t):,}" for c, t in zip(cols, col_totals)],
        rotation=45,
        ha="right",
    )
    ax.set_yticks(
        np.arange(len(rows)),
        labels=[f"{r}  ({int(t):,})" for r, t in zip(rows, row_totals)],
    )

    cbar = fig.colorbar(im, ax=ax, shrink=0.75)
    cbar.set_label("Kotlin LOC", rotation=270, labelpad=14)

    fig.text(
        0.99,
        0.01,
        f"Σ LOC = {grand_total:,}",
        ha="right",
        va="bottom",
        fontsize=9,
        style="italic",
    )

    fig.tight_layout()
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    pdf_path = FIGURES_DIR / "fig_4_2_layer_sourceset_heatmap.pdf"
    png_path = FIGURES_DIR / "fig_4_2_layer_sourceset_heatmap.png"
    fig.savefig(pdf_path)
    fig.savefig(png_path, dpi=200)
    plt.close(fig)
    print(f"wrote {pdf_path}")
    print(f"wrote {png_path}")


# ---------------------------------------------------------------------------------------------
# Phase 4 figure stubs
# ---------------------------------------------------------------------------------------------


def figure_feature_stacked_bars(df: pd.DataFrame) -> None:
    """
    Phase 4: one horizontal bar per feature (11 bars), segments colored by source set.
    Consumes: module_path, category, feature, source_set, kotlin_loc.
    Filter: category == "feat", source set is a production (non-test) set.
    """


def figure_module_treemap(df: pd.DataFrame) -> None:
    """
    Phase 4: treemap of 190 modules, area = total kotlin_loc, color = plugin_applied.
    Consumes: module_path, category, plugin_applied, kotlin_loc (summed per module).
    """


def figure_source_set_sankey(df: pd.DataFrame) -> None:
    """
    Phase 4: Sankey diagram flowing source-set categories (common → intermediate → platform)
    to platform binaries (android, ios, jvm desktop, js, wasmJs, jvm backend).
    Consumes: source_set, kotlin_loc; requires a source-set → platform reach mapping.
    """


def figure_feature_dependency_dag(df: pd.DataFrame) -> None:
    """
    Phase 4: dependency DAG for :feat:projects, colored by hex_layer.
    Requires a separate dependency dump (not available in Phase 1).
    """


def figure_wearos_before_after(df: pd.DataFrame) -> None:
    """
    Phase 4: LaTeX table showing which layers the Wear OS experiment reached unchanged
    and what the one new Wear-native screen cost. Populated after Phase 3 measurements.
    """


def figure_platform_reach_histogram(df: pd.DataFrame) -> None:
    """
    Phase 4: histogram — x = number of platforms a line reaches (1..6), y = LOC.
    Requires a source-set → platform reach mapping parameterized by plugin_applied:
    full-platform plugins map commonMain → 6, frontend-only plugins map commonMain → 5,
    backend plugins map main → 1 (jvm backend only).
    """


def figure_ripple_buckets(df: pd.DataFrame) -> None:
    """
    Phase 4: stacked bar per studied change (inspections reverse removal, ProjectPhoto,
    optional iOS-only extension, Wear OS experiment). Segments: local / intrinsic / collateral.
    Consumes: per-change repair logs produced by feature_retro.py (Phase 2), not the sharing
    report.
    """


# ---------------------------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------------------------


def main() -> int:
    df = load_joined_table()
    figure_layer_source_set_heatmap(df)
    return 0


if __name__ == "__main__":
    sys.exit(main())
