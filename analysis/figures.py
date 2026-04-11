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
# Row label derivation (architecture layer / module category)
# ---------------------------------------------------------------------------------------------
#
# The heatmap's vertical axis groups each LOC row by a coarse "architectural role" label.
# The `hex_layer` column emitted by SharingReportTask only fills in for hex-shaped modules
# (feat + featShared), so we extend it with module-category fallbacks so every LOC row
# lands in exactly one row of the matrix:
#
# - feat and featShared modules with a hex layer → the hex layer name
#   (business / app / ports / driving / driven). Both categories share the same hex rows —
#   the reader cares about the architectural layer, not whether a feature is per-feature
#   business code or cross-feature infrastructure.
# - feat or lib modules with encapsulation=contract → `contract`
# - feat modules without either signal → `feat (other)`. **After the featShared restructure
#   this row should regenerate empty** — it is kept in LAYER_ORDER as a regression tripwire so
#   any module that drifts back into a non-hex shape is visible in the heatmap instead of being
#   silently dropped by the reindex.
# - core modules → `core`
# - lib modules (non-contract) → `lib`
# - infra modules → `infra`
# - top-level app modules (androidApp, composeApp, server, wearApp) → `app (top-level)`
#
# Note that `app` as a hex layer and `app (top-level)` as a category are different things
# and appear as separate rows. `app` is the application layer inside a feature's hexagonal
# architecture; `app (top-level)` is the single-segment module at the root of the build that
# wires everything into a runnable binary.


LAYER_ORDER = [
    # Feat hex layers — the primary thesis axis
    "business",
    "app",
    "ports",
    "driving",
    "driven",
    # Cross-cutting within features
    "contract",
    "feat (other)",
    # Other top-level categories
    "core",
    "lib",
    "infra",
    "app (top-level)",
]


def derive_layer(row: pd.Series) -> str:
    category = row["category"]
    hex_layer = row["hex_layer"]
    encapsulation = row["encapsulation"]

    if category in ("feat", "featShared"):
        if hex_layer:
            return hex_layer
        if encapsulation == "contract":
            return "contract"
        return "feat (other)"
    if category == "lib":
        if encapsulation == "contract":
            return "contract"
        return "lib"
    if category == "core":
        return "core"
    if category == "infra":
        return "infra"
    if category == "app":
        return "app (top-level)"
    return "other"


# ---------------------------------------------------------------------------------------------
# Platform-set ordering (descending reach)
# ---------------------------------------------------------------------------------------------
#
# The `platform_set` column emitted by SharingReportTask collapses the (plugin family × source
# set) pair into a stable reach label. See build-logic/.../analysis/PlatformReach.kt for the
# derivation. The heatmap columns are ordered left-to-right from "reaches the most platforms"
# to "reaches the fewest" so the reader's eye follows the "shared-first" story axis: most
# shared on the left, platform-specific on the right. The central distinction the thesis
# cares about — `all` (6 platforms, fe+be shared) vs `frontend` (5 platforms, fe only) —
# lives in the first two columns of the heatmap.


PLATFORM_SET_ORDER = [
    # 6-platform
    "all",
    # 5-platform
    "frontend",
    "nonAndroid_shared",
    # 4-platform
    "nonWeb_shared",
    "nonAndroid_fe",
    "nonJvm",
    # 3-platform
    "nonWeb_fe",
    # 2-platform
    "mobile",
    "web",
    "jvm_shared",
    # 1-platform
    "backend",
    "jvm_desktop",
    "android",
    "ios",
    "js",
    "wasmJs",
]


PLATFORM_SET_REACH_COUNT = {
    "all": 6,
    "frontend": 5,
    "nonAndroid_shared": 5,
    "nonWeb_shared": 4,
    "nonAndroid_fe": 4,
    "nonJvm": 4,
    "nonWeb_fe": 3,
    "mobile": 2,
    "web": 2,
    "jvm_shared": 2,
    "backend": 1,
    "jvm_desktop": 1,
    "android": 1,
    "ios": 1,
    "js": 1,
    "wasmJs": 1,
}


def order_platform_sets(present: list[str]) -> list[str]:
    ordered = [s for s in PLATFORM_SET_ORDER if s in present]
    extras = sorted(s for s in present if s not in PLATFORM_SET_ORDER)
    return ordered + extras


# ---------------------------------------------------------------------------------------------
# Figure 4.2 — layer × platform-set LOC heatmap
# ---------------------------------------------------------------------------------------------


def figure_layer_platform_set_heatmap(df: pd.DataFrame) -> None:
    production = df[df["platform_set"] != ""].copy()
    production["layer"] = production.apply(derive_layer, axis=1)

    matrix = production.pivot_table(
        index="layer",
        columns="platform_set",
        values="kotlin_loc",
        aggfunc="sum",
        fill_value=0,
    )

    rows = [layer for layer in LAYER_ORDER if layer in matrix.index]
    cols = order_platform_sets(list(matrix.columns))
    matrix = matrix.reindex(index=rows, columns=cols, fill_value=0)

    fig_height = max(4.5, 0.55 * len(rows) + 2.5)
    fig_width = max(9.0, 0.82 * len(cols) + 2.0)
    fig, ax = plt.subplots(figsize=(fig_width, fig_height))

    data = matrix.values
    im = ax.imshow(data, aspect="auto", cmap="YlOrRd")

    ax.set_xlabel("Platform set (how many platforms the code reaches)")
    ax.set_ylabel("Architecture layer")
    ax.set_title(
        "Production Kotlin LOC by (architecture layer × platform reach)",
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

    col_labels = [
        f"{int(t):,} ({PLATFORM_SET_REACH_COUNT.get(c, 0)}p) {c}"
        for c, t in zip(cols, col_totals)
    ]
    ax.set_xticks(np.arange(len(cols)), labels=col_labels, rotation=45, ha="right")
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
    pdf_path = FIGURES_DIR / "fig_4_2_layer_platform_set_heatmap.pdf"
    png_path = FIGURES_DIR / "fig_4_2_layer_platform_set_heatmap.png"
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


RIPPLE_BUCKET_ORDER = ["local", "intrinsic", "collateral"]
RIPPLE_BUCKET_COLORS = {
    "local": "#4c72b0",       # calm blue — intrinsic cost of the change
    "intrinsic": "#dd8452",   # warm orange — NS-required ripple sites
    "collateral": "#c44e52",  # red — avoidable coupling, the anomaly to discuss
}


def figure_ripple_buckets() -> None:
    """§4.3 stacked bar — one bar per studied change, segments = local / intrinsic /
    collateral.

    Consumes every `analysis/data/ripple_<change>_units.csv` produced by
    feature_retro.py --finalize. Each change contributes one bar with file-count
    segments; LOC-churn variant rendered as a companion plot underneath. Recurring
    intrinsic unit count annotated per bar.

    Skips gracefully when no ripple CSVs exist yet — Phase 2 writeup runs before
    any case study data is committed, so the smoke pass must not fail here.
    """
    ripple_csvs = sorted(DATA_DIR.glob("ripple_*_units.csv"))
    if not ripple_csvs:
        print("figure_ripple_buckets: no ripple_*_units.csv found — skipping")
        return

    per_change_rows = []
    for csv_path in ripple_csvs:
        change_id = csv_path.stem.removeprefix("ripple_").removesuffix("_units")
        df = pd.read_csv(csv_path, dtype=str).fillna("")
        df["file_count"] = df["file_count"].astype(int)
        df["loc_churn_sum"] = df["loc_churn_sum"].astype(int)
        df["recurring_bool"] = df["recurring"].str.lower().isin({"true", "1"})

        bucket_files = {b: 0 for b in RIPPLE_BUCKET_ORDER}
        bucket_churn = {b: 0 for b in RIPPLE_BUCKET_ORDER}
        for _, row in df.iterrows():
            b = row["dominant_bucket"]
            if b not in bucket_files:
                continue
            bucket_files[b] += int(row["file_count"])
            bucket_churn[b] += int(row["loc_churn_sum"])

        recurring_intrinsic_units = int(
            ((df["dominant_bucket"] == "intrinsic") & df["recurring_bool"]).sum(),
        )

        per_change_rows.append(
            {
                "change_id": change_id,
                "bucket_files": bucket_files,
                "bucket_churn": bucket_churn,
                "recurring_intrinsic_units": recurring_intrinsic_units,
            },
        )

    fig, (ax_files, ax_churn) = plt.subplots(
        2,
        1,
        figsize=(10, max(3, 1.2 * len(per_change_rows)) * 2),
        sharey=True,
    )

    def _stacked_barh(ax, key: str, title: str, xlabel: str) -> None:
        change_labels = [r["change_id"] for r in per_change_rows]
        y_pos = np.arange(len(change_labels))
        left = np.zeros(len(change_labels))
        for bucket in RIPPLE_BUCKET_ORDER:
            values = np.array([r[key][bucket] for r in per_change_rows])
            ax.barh(
                y_pos,
                values,
                left=left,
                color=RIPPLE_BUCKET_COLORS[bucket],
                label=bucket,
                edgecolor="white",
                linewidth=0.5,
            )
            for i, v in enumerate(values):
                if v > 0:
                    ax.text(
                        left[i] + v / 2,
                        y_pos[i],
                        str(v),
                        ha="center",
                        va="center",
                        fontsize=8,
                        color="white",
                    )
            left += values

        ax.set_yticks(y_pos)
        ax.set_yticklabels(change_labels)
        ax.invert_yaxis()
        ax.set_xlabel(xlabel)
        ax.set_title(title)
        ax.grid(axis="x", linestyle=":", alpha=0.5)

        # Annotate the recurring intrinsic unit count at the end of each bar.
        for i, row in enumerate(per_change_rows):
            total = sum(row[key].values())
            ax.text(
                total + max(1, total * 0.02),
                y_pos[i],
                f"  recur. intrinsic units = {row['recurring_intrinsic_units']}",
                ha="left",
                va="center",
                fontsize=8,
                color="#333",
            )

    _stacked_barh(ax_files, "bucket_files", "Ripple by file count", "files touched")
    _stacked_barh(ax_churn, "bucket_churn", "Ripple by LOC churn", "LOC churn (added + removed)")

    handles, labels = ax_files.get_legend_handles_labels()
    fig.legend(handles, labels, loc="lower center", ncol=len(RIPPLE_BUCKET_ORDER), frameon=False)
    fig.suptitle("Fig. 4.3 — Ripple decomposition per studied change", fontsize=12)
    fig.tight_layout(rect=(0, 0.05, 1, 0.95))

    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    fig.savefig(FIGURES_DIR / "fig_4_3_ripple_buckets.pdf", bbox_inches="tight")
    fig.savefig(FIGURES_DIR / "fig_4_3_ripple_buckets.png", dpi=200, bbox_inches="tight")
    plt.close(fig)
    print(f"figure_ripple_buckets: wrote {FIGURES_DIR / 'fig_4_3_ripple_buckets.pdf'}")


# ---------------------------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------------------------


def main() -> int:
    df = load_joined_table()
    figure_layer_platform_set_heatmap(df)
    figure_ripple_buckets()
    return 0


if __name__ == "__main__":
    sys.exit(main())
