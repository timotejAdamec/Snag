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
# Figure 4.2 (part A) — per-hex-layer platform-specific LOC share
# ---------------------------------------------------------------------------------------------
#
# Descriptive readout: for each hexagonal layer, what fraction of production Kotlin LOC lives
# in named platform-specific source sets (webMain, nonWebMain, androidMain, iosMain, jvmMain,
# and the rarer nonAndroidMain/nonJvmMain/mobileMain/jsMain/wasmJsMain) versus the neutral
# source sets (commonMain / main).
#
# This metric is strictly descriptive. The same shape is compatible with correctly-scoped,
# over-shared, and over-fragmented codebases — the thesis §4.2 prose must call it out as such
# (see analysis/phase-2-plan.md §A on sharing/evolvability duality). The counterfactual in
# Part D is where correctness is argued; this figure just shows where Snag's divergence lives.
#
# The neutral bucket collapses both `commonMain` (multiplatform) and `main` (BE-only jvmMain)
# because both are the layer's "shared" source set from that module family's perspective.
# A BE module with code in `main` is not platform-divergent — it only has one platform.

NEUTRAL_SOURCE_SETS = frozenset({"commonMain", "main"})

PLATFORM_SPECIFIC_SEGMENT_ORDER = [
    "nonWebMain",
    "webMain",
    "nonAndroidMain",
    "nonJvmMain",
    "mobileMain",
    "androidMain",
    "iosMain",
    "jvmMain",
    "jsMain",
    "wasmJsMain",
]

PLATFORM_SPECIFIC_SEGMENT_COLORS = {
    "nonWebMain": "#dd8452",
    "webMain": "#c44e52",
    "nonAndroidMain": "#937860",
    "nonJvmMain": "#8172b3",
    "mobileMain": "#da8bc3",
    "androidMain": "#55a868",
    "iosMain": "#64b5cd",
    "jvmMain": "#ccb974",
    "jsMain": "#8c8c8c",
    "wasmJsMain": "#4c4c4c",
}

NEUTRAL_SEGMENT_COLOR = "#4c72b0"  # calm blue, same family as ripple_buckets "local"

_DIVERGENCE_COLUMNS = [
    "hex_layer",
    "total_loc",
    "platform_specific_loc",
    "platform_specific_share",
    "divergent_module_count",
    "total_module_count",
]


def compute_layer_divergence(df: pd.DataFrame) -> pd.DataFrame:
    """Per-hex-layer aggregation of platform-specific LOC share.

    Pure function over an already-loaded sharing report. Drops test source sets
    and rows with empty `platform_set` (they carry no reach semantics). Empty
    `hex_layer` rolls up into the `"other"` bucket so every production row lands
    somewhere. Returns a DataFrame ordered by the hex rows of `LAYER_ORDER`
    followed by `"other"` — rows are preserved even when empty so a missing
    layer shows up as a zero row instead of vanishing.
    """
    if df.empty:
        return pd.DataFrame(columns=_DIVERGENCE_COLUMNS)

    prod = df.copy()
    prod = prod[prod["platform_set"] != ""]
    prod = prod[~prod["source_set"].str.endswith("Test")]
    prod["layer"] = prod["hex_layer"].where(prod["hex_layer"] != "", "other")
    prod["kotlin_loc"] = prod["kotlin_loc"].astype(int)
    prod["is_platform_specific"] = ~prod["source_set"].isin(NEUTRAL_SOURCE_SETS)
    prod["platform_specific_loc"] = prod["kotlin_loc"].where(prod["is_platform_specific"], 0)

    layer_rows = [layer for layer in LAYER_ORDER if layer in {
        "business", "app", "ports", "driving", "driven",
    }]
    layer_rows.append("other")

    records: list[dict] = []
    for layer in layer_rows:
        subset = prod[prod["layer"] == layer]
        total_loc = int(subset["kotlin_loc"].sum())
        ps_loc = int(subset["platform_specific_loc"].sum())
        share = ps_loc / total_loc if total_loc > 0 else 0.0
        total_modules = subset["module_path"].nunique()
        divergent_modules = (
            subset[subset["is_platform_specific"] & (subset["kotlin_loc"] > 0)]
            ["module_path"]
            .nunique()
        )
        records.append({
            "hex_layer": layer,
            "total_loc": total_loc,
            "platform_specific_loc": ps_loc,
            "platform_specific_share": share,
            "divergent_module_count": int(divergent_modules),
            "total_module_count": int(total_modules),
        })

    return pd.DataFrame(records, columns=_DIVERGENCE_COLUMNS)


def figure_layer_divergence(df: pd.DataFrame) -> None:
    """Emit `layer_divergence.csv` + stacked-bar PDF/PNG for §4.2 Part A."""
    agg = compute_layer_divergence(df)

    DATA_DIR.mkdir(parents=True, exist_ok=True)
    csv_path = DATA_DIR / "layer_divergence.csv"
    agg.to_csv(csv_path, index=False, lineterminator="\n")
    print(f"wrote {csv_path}")

    # Per-layer per-segment LOC for stacked-bar rendering.
    prod = df[df["platform_set"] != ""].copy()
    prod = prod[~prod["source_set"].str.endswith("Test")]
    prod["layer"] = prod["hex_layer"].where(prod["hex_layer"] != "", "other")
    prod["kotlin_loc"] = prod["kotlin_loc"].astype(int)
    prod["segment"] = prod["source_set"].where(
        ~prod["source_set"].isin(NEUTRAL_SOURCE_SETS),
        "neutral",
    )
    seg_matrix = prod.pivot_table(
        index="layer",
        columns="segment",
        values="kotlin_loc",
        aggfunc="sum",
        fill_value=0,
    )

    layers = list(agg["hex_layer"])
    seg_matrix = seg_matrix.reindex(index=layers, fill_value=0)

    segment_order = ["neutral"]
    for seg in PLATFORM_SPECIFIC_SEGMENT_ORDER:
        if seg in seg_matrix.columns and seg_matrix[seg].sum() > 0:
            segment_order.append(seg)
    extras = sorted(
        c for c in seg_matrix.columns
        if c not in segment_order and c != "neutral" and seg_matrix[c].sum() > 0
    )
    segment_order.extend(extras)
    seg_matrix = seg_matrix.reindex(columns=segment_order, fill_value=0)

    fig_height = max(4.5, 0.55 * len(layers) + 2.0)
    fig, ax = plt.subplots(figsize=(11.0, fig_height))

    y_pos = np.arange(len(layers))
    left = np.zeros(len(layers))
    for segment in segment_order:
        values = seg_matrix[segment].to_numpy(dtype=int)
        color = (
            NEUTRAL_SEGMENT_COLOR
            if segment == "neutral"
            else PLATFORM_SPECIFIC_SEGMENT_COLORS.get(segment, "#777777")
        )
        label = "commonMain + main" if segment == "neutral" else segment
        ax.barh(
            y_pos,
            values,
            left=left,
            color=color,
            label=label,
            edgecolor="white",
            linewidth=0.5,
        )
        for i, v in enumerate(values):
            if v >= max(1, int(seg_matrix.values.sum() * 0.01)):
                ax.text(
                    left[i] + v / 2,
                    y_pos[i],
                    f"{int(v):,}",
                    ha="center",
                    va="center",
                    fontsize=8,
                    color="white",
                )
        left += values

    # Annotate each bar with "M/N modules divergent" + share percentage.
    for i, (_, row) in enumerate(agg.iterrows()):
        total = int(row["total_loc"])
        share_pct = row["platform_specific_share"] * 100.0
        annotation = (
            f"  {int(row['divergent_module_count'])}/{int(row['total_module_count'])} modules "
            f"divergent ({share_pct:.1f}% LOC)"
        )
        ax.text(
            total + max(1, int(seg_matrix.values.sum() * 0.005)),
            y_pos[i],
            annotation,
            ha="left",
            va="center",
            fontsize=8,
            color="#333",
        )

    ax.set_yticks(y_pos, labels=layers)
    ax.invert_yaxis()
    ax.set_xlabel("Production Kotlin LOC")
    ax.set_ylabel("Hexagonal layer")
    ax.set_title(
        "Per-hex-layer platform-specific LOC share (descriptive)",
        pad=14,
    )
    ax.legend(loc="lower right", frameon=True, fontsize=8, ncol=2)
    ax.grid(axis="x", linestyle=":", alpha=0.5)

    grand_total = int(seg_matrix.values.sum())
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
    pdf_path = FIGURES_DIR / "fig_4_2_layer_divergence.pdf"
    png_path = FIGURES_DIR / "fig_4_2_layer_divergence.png"
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


def figure_wearos_ripple_by_module_tree() -> None:
    """§4.4 bar chart — top-level repo trees on x, files+LOC churn on y, for the
    Wear OS platform-extension experiment.

    Consumes analysis/data/ripple_wearos-project-list_by_module_tree.csv produced
    by analysis/scaling_by_module_tree.py. Skips gracefully when missing.
    """
    csv_path = DATA_DIR / "ripple_wearos-project-list_by_module_tree.csv"
    if not csv_path.exists():
        print(f"figure_wearos_ripple_by_module_tree: {csv_path.name} not found — skipping")
        return

    df = pd.read_csv(csv_path)
    df = df.sort_values("file_count", ascending=False).reset_index(drop=True)

    fig, (ax_files, ax_loc) = plt.subplots(2, 1, figsize=(9, 6), sharex=True)

    trees = df["tree"].tolist()
    x_pos = np.arange(len(trees))
    bar_width = 0.7

    intrinsic = df["intrinsic_files"].to_numpy()
    collateral = df["collateral_files"].to_numpy()
    local = df["local_files"].to_numpy()

    ax_files.bar(x_pos, intrinsic, bar_width, color=RIPPLE_BUCKET_COLORS["intrinsic"], label="intrinsic")
    ax_files.bar(x_pos, collateral, bar_width, bottom=intrinsic,
                 color=RIPPLE_BUCKET_COLORS["collateral"], label="collateral")
    ax_files.bar(x_pos, local, bar_width, bottom=intrinsic + collateral,
                 color=RIPPLE_BUCKET_COLORS["local"], label="local")
    ax_files.set_ylabel("files touched")
    ax_files.set_title("Fig. 4.4 — Wear OS extension ripple by repo tree")
    ax_files.grid(axis="y", linestyle=":", alpha=0.5)
    ax_files.legend(loc="upper right", frameon=False, fontsize=9)

    for i, total in enumerate(df["file_count"]):
        ax_files.text(x_pos[i], total + 1, str(total), ha="center", va="bottom", fontsize=8)

    ax_loc.bar(x_pos, df["loc_churn"], bar_width, color="#888")
    ax_loc.set_ylabel("LOC churn (added + removed)")
    ax_loc.set_xticks(x_pos)
    ax_loc.set_xticklabels(trees, rotation=30, ha="right")
    ax_loc.grid(axis="y", linestyle=":", alpha=0.5)

    for i, loc in enumerate(df["loc_churn"]):
        ax_loc.text(x_pos[i], loc + max(df["loc_churn"]) * 0.02, str(loc),
                    ha="center", va="bottom", fontsize=8)

    fig.tight_layout()
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    fig.savefig(FIGURES_DIR / "fig_4_4_wear_ripple_by_module_tree.pdf", bbox_inches="tight")
    fig.savefig(FIGURES_DIR / "fig_4_4_wear_ripple_by_module_tree.png", dpi=200, bbox_inches="tight")
    plt.close(fig)
    print(f"figure_wearos_ripple_by_module_tree: wrote {FIGURES_DIR / 'fig_4_4_wear_ripple_by_module_tree.pdf'}")


def main() -> int:
    df = load_joined_table()
    figure_layer_platform_set_heatmap(df)
    figure_layer_divergence(df)
    figure_ripple_buckets()
    figure_wearos_ripple_by_module_tree()
    return 0


if __name__ == "__main__":
    sys.exit(main())
