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

    ax.set_xlabel("Sada platforem (počet platforem, na které zdrojová sada zasahuje)")
    ax.set_ylabel("Vrstva architektury")
    ax.set_title(
        "Produkční Kotlin LOC podle vrstvy architektury a sady platforem",
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
# Descriptive readout: for each hexagonal layer, what fraction of production Kotlin LOC reaches
# only a single platform — i.e. lives outside the multi-platform-shared `commonMain`. The
# definition matches the reach histogram (§4.2 part B) where `reach == 1` is labelled
# "platformně specifické": named frontend-platform source sets (webMain, androidMain, iosMain,
# jsMain, wasmJsMain, jvmMain) AND the backend-module `main` source set (which only runs on
# JVM backend) all count as platform-specific. Intermediate source sets (nonWebMain,
# nonAndroidMain, mobileMain, …) are also outside commonMain and thus count too — they cover
# 2–4 platforms but not the full reach of commonMain.
#
# This metric is strictly descriptive. The same shape is compatible with correctly-scoped,
# over-shared, and over-fragmented codebases — the thesis §4.2 prose must call it out as such
# (see analysis/phase-2-plan.md §A on sharing/evolvability duality). The counterfactual in
# Part D is where correctness is argued; this figure just shows where Snag's divergence lives.
#
# The *visual breakdown* further splits commonMain into FE+BE-shared (reach 6) vs FE-only
# (reach 5) so the reader sees the 6p-vs-5p reach difference. Everything outside commonMain
# (including BE `main`) renders in the warm/platform-specific palette so the bar's warm
# fraction reads directly as the per-layer platform-specific share.

NEUTRAL_SOURCE_SETS = frozenset({"commonMain"})

# (source_set, platform_set) -> (segment id, legend label, colour) for the cool/neutral
# palette segments. Only commonMain rows land here — BE `main` is now a platform-specific
# segment (see SEGMENT_SPEC below) consistent with the reach-histogram convention.
NEUTRAL_SEGMENT_SPEC: dict[tuple[str, str], tuple[str, str, str]] = {
    ("commonMain", "all"):      ("commonMain_all", "commonMain · vše (6p fe+be)",    "#2d3e5e"),
    ("commonMain", "frontend"): ("commonMain_fe",  "commonMain · frontend (5p fe)",  "#4c72b0"),
}
NEUTRAL_SEGMENT_ORDER = ["commonMain_all", "commonMain_fe"]
NEUTRAL_SEGMENT_COLORS = {seg_id: colour for (seg_id, _, colour) in NEUTRAL_SEGMENT_SPEC.values()}
NEUTRAL_SEGMENT_LABELS = {seg_id: label for (seg_id, label, _) in NEUTRAL_SEGMENT_SPEC.values()}

# (source_set, platform_set) -> (segment id, legend label, colour) for single-target `main`
# source sets. Both backend modules (JVM) and the top-level :androidApp shell (Android target)
# use `main`; both are reach 1, so they join the warm/platform-specific palette.
SINGLE_TARGET_MAIN_SPEC: dict[tuple[str, str], tuple[str, str, str]] = {
    ("main", "backend"): ("main_be",      "main · backend (1p be)",      "#a37b50"),
    ("main", "android"): ("main_android", "main · android (1p android)", "#3f7f4a"),
}
SINGLE_TARGET_MAIN_COLORS = {seg_id: colour for (seg_id, _, colour) in SINGLE_TARGET_MAIN_SPEC.values()}
SINGLE_TARGET_MAIN_LABELS = {seg_id: label for (seg_id, label, _) in SINGLE_TARGET_MAIN_SPEC.values()}

PLATFORM_SPECIFIC_SEGMENT_ORDER = [
    "main_be",
    "main_android",
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
    **SINGLE_TARGET_MAIN_COLORS,
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

PLATFORM_SPECIFIC_SEGMENT_LABELS = {
    **SINGLE_TARGET_MAIN_LABELS,
}

_DIVERGENCE_COLUMNS = [
    "hex_layer",
    "total_loc",
    "platform_specific_loc",
    "platform_specific_share",
]


def compute_layer_divergence(df: pd.DataFrame) -> pd.DataFrame:
    """Per-hex-layer aggregation of platform-specific LOC share.

    Platform-specific = LOC outside `commonMain` (the only multi-platform-shared
    source set). Includes BE-only `main` (reach 1) — consistent with the reach
    histogram in §4.2 part B where `reach == 1` is labelled "platformně
    specifické".

    Pure function over an already-loaded sharing report. Drops test source sets
    and rows with empty `platform_set` (they carry no reach semantics). Returns
    a DataFrame ordered by `LAYER_ORDER`; rows with no production LOC are not
    emitted.
    """
    if df.empty:
        return pd.DataFrame(columns=_DIVERGENCE_COLUMNS)

    prod = df.copy()
    prod = prod[prod["platform_set"] != ""]
    prod = prod[~prod["source_set"].str.endswith("Test")]
    prod["layer"] = prod.apply(derive_layer, axis=1)
    prod["kotlin_loc"] = prod["kotlin_loc"].astype(int)
    prod["is_platform_specific"] = ~prod["source_set"].isin(NEUTRAL_SOURCE_SETS)
    prod["platform_specific_loc"] = prod["kotlin_loc"].where(prod["is_platform_specific"], 0)

    present_layers = set(prod["layer"].unique())
    layer_rows = [layer for layer in LAYER_ORDER if layer in present_layers]

    records: list[dict] = []
    for layer in layer_rows:
        subset = prod[prod["layer"] == layer]
        total_loc = int(subset["kotlin_loc"].sum())
        ps_loc = int(subset["platform_specific_loc"].sum())
        share = ps_loc / total_loc if total_loc > 0 else 0.0
        records.append({
            "hex_layer": layer,
            "total_loc": total_loc,
            "platform_specific_loc": ps_loc,
            "platform_specific_share": share,
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
    prod["layer"] = prod.apply(derive_layer, axis=1)
    prod["kotlin_loc"] = prod["kotlin_loc"].astype(int)
    def _classify_segment(row: pd.Series) -> str:
        key = (row["source_set"], row["platform_set"])
        spec = NEUTRAL_SEGMENT_SPEC.get(key) or SINGLE_TARGET_MAIN_SPEC.get(key)
        return spec[0] if spec is not None else row["source_set"]

    prod["segment"] = prod.apply(_classify_segment, axis=1)
    seg_matrix = prod.pivot_table(
        index="layer",
        columns="segment",
        values="kotlin_loc",
        aggfunc="sum",
        fill_value=0,
    )

    layers = list(agg["hex_layer"])
    seg_matrix = seg_matrix.reindex(index=layers, fill_value=0)

    segment_order: list[str] = []
    for seg in NEUTRAL_SEGMENT_ORDER:
        if seg in seg_matrix.columns and seg_matrix[seg].sum() > 0:
            segment_order.append(seg)
    for seg in PLATFORM_SPECIFIC_SEGMENT_ORDER:
        if seg in seg_matrix.columns and seg_matrix[seg].sum() > 0:
            segment_order.append(seg)
    extras = sorted(
        c for c in seg_matrix.columns
        if c not in segment_order and seg_matrix[c].sum() > 0
    )
    segment_order.extend(extras)
    seg_matrix = seg_matrix.reindex(columns=segment_order, fill_value=0)

    fig_height = max(4.5, 0.55 * len(layers) + 2.0)
    fig, ax = plt.subplots(figsize=(11.0, fig_height))

    y_pos = np.arange(len(layers))
    left = np.zeros(len(layers))
    for segment in segment_order:
        values = seg_matrix[segment].to_numpy(dtype=int)
        color = NEUTRAL_SEGMENT_COLORS.get(
            segment,
            PLATFORM_SPECIFIC_SEGMENT_COLORS.get(segment, "#777777"),
        )
        label = (
            NEUTRAL_SEGMENT_LABELS.get(segment)
            or PLATFORM_SPECIFIC_SEGMENT_LABELS.get(segment)
            or segment
        )
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

    ax.set_yticks(y_pos, labels=layers)
    ax.invert_yaxis()
    ax.set_xlabel("Produkční Kotlin LOC")
    ax.set_ylabel("Hexagonální vrstva")
    ax.set_title(
        "Rozložení produkčního LOC po vrstvě architektury a zdrojové sadě",
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


# ---------------------------------------------------------------------------------------------
# Structural sharing score — per-module classification by applied plugin family
# ---------------------------------------------------------------------------------------------
#
# A "platform-neutral plugin" is one of the KMP multiplatform plugins in PlatformReach.kt:
# their applied-id contains the substring `multiplatform.module`. Single-target plugins
# (backend JVM, Android application) and modules that apply no Snag plugin (e.g. :androidApp
# top-level shell) are classified as platform-specific.
#
# A module is counted in the denominator only when it has at least one production row with
# non-zero LOC — consistent with compute_layer_divergence's filter discipline.


def _is_multiplatform_plugin(plugin_applied: str) -> bool:
    return "multiplatform.module" in plugin_applied


_STRUCTURAL_SCORE_COLUMNS = ["layer", "multiplatform_modules", "total_modules", "share"]


def compute_structural_sharing_score(df: pd.DataFrame) -> pd.DataFrame:
    """Per-layer and global fraction of modules whose plugin is KMP multiplatform.

    Returns one row per hex layer that has any production LOC, plus a final
    `"(celkem)"` row carrying the global ratio. The per-layer `share` is local
    (layer's multiplatform count / layer's total count); the global row uses
    the grand totals.
    """
    empty_result = pd.DataFrame(
        [{"layer": "(celkem)", "multiplatform_modules": 0, "total_modules": 0, "share": 0.0}],
        columns=_STRUCTURAL_SCORE_COLUMNS,
    )
    if df.empty:
        return empty_result

    prod = df.copy()
    prod = prod[prod["platform_set"] != ""]
    prod = prod[~prod["source_set"].str.endswith("Test")]
    prod["kotlin_loc"] = prod["kotlin_loc"].astype(int)
    prod = prod[prod["kotlin_loc"] > 0]
    if prod.empty:
        return empty_result

    prod["layer"] = prod.apply(derive_layer, axis=1)
    prod["is_multiplatform"] = prod["plugin_applied"].map(_is_multiplatform_plugin)

    per_module = (
        prod.groupby("module_path", as_index=False)
        .agg(
            layer=("layer", "first"),
            is_multiplatform=("is_multiplatform", "max"),
        )
    )

    records: list[dict] = []
    present_layers = set(per_module["layer"].unique())
    for layer in [*LAYER_ORDER, "other"]:
        if layer not in present_layers:
            continue
        subset = per_module[per_module["layer"] == layer]
        total = int(len(subset))
        mp = int(subset["is_multiplatform"].sum())
        share = mp / total if total > 0 else 0.0
        records.append({
            "layer": layer,
            "multiplatform_modules": mp,
            "total_modules": total,
            "share": share,
        })

    grand_total = int(len(per_module))
    grand_mp = int(per_module["is_multiplatform"].sum())
    grand_share = grand_mp / grand_total if grand_total > 0 else 0.0
    records.append({
        "layer": "(celkem)",
        "multiplatform_modules": grand_mp,
        "total_modules": grand_total,
        "share": grand_share,
    })

    return pd.DataFrame(records, columns=_STRUCTURAL_SCORE_COLUMNS)


def write_structural_sharing_score(df: pd.DataFrame) -> None:
    """Write `data/structural_sharing_score.csv` for §4.2 Part C.

    This metric is a single headline number plus a per-layer decomposition; the
    §4.2 prose cites it inline. No dedicated figure is produced — the per-layer
    breakdown would duplicate the divergent-module-count annotation already on
    `fig_4_2_layer_divergence`. The CSV is the canonical artefact; §A.2 of the
    appendix mentions it alongside the full sharing report.
    """
    agg = compute_structural_sharing_score(df)
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    csv_path = DATA_DIR / "structural_sharing_score.csv"
    agg.to_csv(csv_path, index=False, lineterminator="\n")
    print(f"wrote {csv_path}")


_REACH_HISTOGRAM_COLUMNS = ["reach", "kotlin_loc", "source_set_count", "share"]


def compute_platform_reach_histogram(df: pd.DataFrame) -> pd.DataFrame:
    """Bucket production Kotlin LOC by the number of platforms a source set reaches.

    The `platform_set` column emitted by SharingReportTask already carries the
    (plugin family × source set) reach label — `PLATFORM_SET_REACH_COUNT` maps
    it back to an integer 1..6. Rows with empty `platform_set`, test source
    sets, or unknown reach labels are dropped (they carry no reach semantics).

    The output is stable-shaped: reach buckets 1..6 are always present, so a
    bucket with zero LOC materialises as a 0 row instead of vanishing. `share`
    is each bucket's fraction of the grand total (sums to 1.0 when there is
    any LOC, and is uniformly 0 for empty input).
    """
    bins = [1, 2, 3, 4, 5, 6]
    if df.empty:
        return pd.DataFrame(
            [{"reach": r, "kotlin_loc": 0, "source_set_count": 0, "share": 0.0} for r in bins],
            columns=_REACH_HISTOGRAM_COLUMNS,
        )

    prod = df.copy()
    prod = prod[prod["platform_set"] != ""]
    prod = prod[~prod["source_set"].str.endswith("Test")]
    prod["kotlin_loc"] = prod["kotlin_loc"].astype(int)
    prod["reach"] = prod["platform_set"].map(PLATFORM_SET_REACH_COUNT)
    prod = prod[prod["reach"].notna()]
    prod["reach"] = prod["reach"].astype(int)

    total = int(prod["kotlin_loc"].sum())
    records: list[dict] = []
    for r in bins:
        subset = prod[prod["reach"] == r]
        loc = int(subset["kotlin_loc"].sum())
        share = loc / total if total > 0 else 0.0
        records.append({
            "reach": r,
            "kotlin_loc": loc,
            "source_set_count": int(len(subset)),
            "share": share,
        })
    return pd.DataFrame(records, columns=_REACH_HISTOGRAM_COLUMNS)


# ---------------------------------------------------------------------------------------------
# Figure 4.2 (part B) — LOC distribution by platform reach (1..6)
# ---------------------------------------------------------------------------------------------
#
# Reach-count histogram: x = 6..1 (most-shared on the left, platform-specific on the right),
# y = Kotlin LOC in that bucket. The 6-bucket is the fe+be shared `commonMain`, the 5-bucket is
# frontend-only `commonMain`, the 1-bucket is platform-specific tail (backend `main`, webMain,
# iosMain, androidMain …). The share metric (percentage of production LOC) is annotated on
# each bar to give §4.2 a headline "N % of the code reaches K platforms" sentence per row.


REACH_BUCKET_LABELS: dict[int, str] = {
    6: "6 (vše: fe+be)",
    5: "5 (frontend)",
    4: "4 (nonWeb fe+be, nonAndroid fe, nonJvm)",
    3: "3 (nonWeb fe)",
    2: "2 (mobile, web, jvm shared)",
    1: "1 (platformně specifické)",
}

REACH_BUCKET_COLORS: dict[int, str] = {
    6: "#2d3e5e",
    5: "#4c72b0",
    4: "#5aa4d4",
    3: "#8fbdd9",
    2: "#dd8452",
    1: "#c44e52",
}


def figure_platform_reach_histogram(df: pd.DataFrame) -> None:
    """Emit `fig_4_2_platform_reach_histogram.{pdf,png}` for §4.2 Part B."""
    agg = compute_platform_reach_histogram(df)
    total = int(agg["kotlin_loc"].sum())

    # Display in descending reach (most-shared at top) so the eye follows the
    # same "shared-first" axis as the §4.2 heatmap.
    agg_desc = agg.sort_values("reach", ascending=False).reset_index(drop=True)

    fig, ax = plt.subplots(figsize=(9.5, 4.2))
    y_pos = np.arange(len(agg_desc))

    colors = [REACH_BUCKET_COLORS[int(r)] for r in agg_desc["reach"]]
    ax.barh(y_pos, agg_desc["kotlin_loc"], color=colors, edgecolor="white", linewidth=0.6)

    labels = [REACH_BUCKET_LABELS[int(r)] for r in agg_desc["reach"]]
    ax.set_yticks(y_pos, labels=labels)
    ax.invert_yaxis()
    ax.set_xlabel("Produkční Kotlin LOC")
    ax.set_ylabel("Dosah (počet platforem)")
    ax.set_title("Rozdělení produkčního LOC podle počtu platforem, na které zdrojová sada zasahuje", pad=14)
    ax.grid(axis="x", linestyle=":", alpha=0.5)

    max_loc = int(agg_desc["kotlin_loc"].max()) if total > 0 else 0
    for i, (_, row) in enumerate(agg_desc.iterrows()):
        loc = int(row["kotlin_loc"])
        share_pct = row["share"] * 100.0
        if loc == 0:
            annotation = "0 LOC"
        else:
            annotation = f"{loc:,} LOC ({share_pct:.1f}%)"
        ax.text(
            loc + max(1, int(max_loc * 0.01)) if max_loc > 0 else 0.1,
            y_pos[i],
            annotation,
            ha="left",
            va="center",
            fontsize=9,
            color="#333",
        )
    if max_loc > 0:
        ax.set_xlim(right=max_loc * 1.32)

    fig.text(
        0.99, 0.01,
        f"Σ LOC = {total:,}",
        ha="right", va="bottom", fontsize=9, style="italic",
    )

    fig.tight_layout()
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    pdf_path = FIGURES_DIR / "fig_4_2_platform_reach_histogram.pdf"
    png_path = FIGURES_DIR / "fig_4_2_platform_reach_histogram.png"
    fig.savefig(pdf_path)
    fig.savefig(png_path, dpi=200)
    plt.close(fig)
    print(f"wrote {pdf_path}")
    print(f"wrote {png_path}")


RIPPLE_BUCKET_ORDER = ["local", "intrinsic", "collateral"]
RIPPLE_BUCKET_COLORS = {
    "local": "#4c72b0",       # calm blue — intrinsic cost of the change
    "intrinsic": "#dd8452",   # warm orange — NS-required ripple sites
    "collateral": "#c44e52",  # red — avoidable coupling, the anomaly to discuss
}
RIPPLE_BUCKET_LABELS_CS = {
    "local": "lokální",
    "intrinsic": "vlastní",
    "collateral": "vedlejší",
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
                label=RIPPLE_BUCKET_LABELS_CS[bucket],
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
        totals = [sum(r[key].values()) for r in per_change_rows]
        max_total = max(totals) if totals else 0
        for i, row in enumerate(per_change_rows):
            total = totals[i]
            ax.text(
                total + max(1, total * 0.02),
                y_pos[i],
                f"  opak. vlastní místa = {row['recurring_intrinsic_units']}",
                ha="left",
                va="center",
                fontsize=8,
                color="#333",
            )

        # Reserve headroom on the right so the "opak. vlastní místa = N"
        # annotation stays inside the axes frame for the widest bars.
        if max_total > 0:
            ax.set_xlim(right=max_total * 1.30)

    _stacked_barh(ax_files, "bucket_files", "Kategorie dopadu podle počtu souborů", "zasaženo souborů")
    _stacked_barh(ax_churn, "bucket_churn", "Kategorie dopadu podle změny řádků", "Změny řádků (přidáno + odebráno)")

    handles, labels = ax_files.get_legend_handles_labels()
    fig.legend(handles, labels, loc="lower center", ncol=len(RIPPLE_BUCKET_ORDER), frameon=False)
    fig.suptitle("Obr. 4.3 — Rozdělení dopadu mezi kategorie napříč případovými studiemi", fontsize=12)
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

    fig, (ax_files, ax_loc) = plt.subplots(2, 1, figsize=(9, 7.5), sharex=True)

    trees = df["tree"].tolist()
    x_pos = np.arange(len(trees))
    bar_width = 0.7

    intrinsic = df["intrinsic_files"].to_numpy()
    collateral = df["collateral_files"].to_numpy()
    local = df["local_files"].to_numpy()

    ax_files.bar(x_pos, intrinsic, bar_width, color=RIPPLE_BUCKET_COLORS["intrinsic"],
                 label=RIPPLE_BUCKET_LABELS_CS["intrinsic"])
    ax_files.bar(x_pos, collateral, bar_width, bottom=intrinsic,
                 color=RIPPLE_BUCKET_COLORS["collateral"], label=RIPPLE_BUCKET_LABELS_CS["collateral"])
    ax_files.bar(x_pos, local, bar_width, bottom=intrinsic + collateral,
                 color=RIPPLE_BUCKET_COLORS["local"], label=RIPPLE_BUCKET_LABELS_CS["local"])
    ax_files.set_ylabel("zasaženo souborů")
    ax_files.set_title("Obr. 4.4 — Rozložení dopadu Wear OS podle stromu nejvyšší úrovně v repozitáři")
    ax_files.grid(axis="y", linestyle=":", alpha=0.5)
    ax_files.legend(loc="upper right", frameon=False, fontsize=9)

    # Headroom above the tallest bar so value labels stay inside the axes
    # frame (matplotlib's bar autoscale does not reserve space for text).
    max_files = int(df["file_count"].max())
    for i, total in enumerate(df["file_count"]):
        ax_files.text(x_pos[i], total + max_files * 0.02, str(total),
                      ha="center", va="bottom", fontsize=8)
    ax_files.set_ylim(top=max_files * 1.15)

    ax_loc.bar(x_pos, df["loc_churn"], bar_width, color="#888")
    ax_loc.set_ylabel("Změny řádků (přidáno + odebráno)")
    ax_loc.set_xticks(x_pos)
    ax_loc.set_xticklabels(trees, rotation=30, ha="right")
    ax_loc.grid(axis="y", linestyle=":", alpha=0.5)

    max_loc = int(df["loc_churn"].max())
    for i, loc in enumerate(df["loc_churn"]):
        ax_loc.text(x_pos[i], loc + max_loc * 0.02, str(loc),
                    ha="center", va="bottom", fontsize=8)
    ax_loc.set_ylim(top=max_loc * 1.12)

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
    figure_platform_reach_histogram(df)
    write_structural_sharing_score(df)
    figure_ripple_buckets()
    figure_wearos_ripple_by_module_tree()
    return 0


if __name__ == "__main__":
    sys.exit(main())
