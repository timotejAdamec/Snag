#!/usr/bin/env python3
"""Phase 3 helper: classify entries in
analysis/classifications/wearos-project-list.yaml using path patterns.

By default processes only entries with bucket == "unclassified" (backfill mode
for freshly stubbed YAML). Pass --reclassify to also override already-classified
entries where the path rule now emits a different bucket -- used when the
scope_module_set rule changes (e.g. promoting wear-scope modules from
intrinsic/collateral to local).

Run from chore worktree root."""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import yaml

CLASSIFICATION = Path("analysis/classifications/wearos-project-list.yaml")


def classify(file: str, status: str) -> tuple[str, bool, str] | None:
    """Return (bucket, recurring, reason) or None to leave unclassified.

    scope_module_set(Wear) = {:wearApp, :feat:*:fe:wear:*,
                              :koinModulesAggregate:fe:wear:*}
    Files whose module is inside this set are classified as local (per
    §4.1 Dekompozice ripple).
    """
    # Renames carry "old -> new" in `file`; classify by the new (post-rename) path.
    if " -> " in file:
        file = file.split(" -> ", 1)[1]

    # -- scope_module_set(Wear): all three module groups are local -----------

    # wearApp/ shell -- top-level platform-app module.
    if file.startswith("wearApp/"):
        return ("local", False,
                "Kompoziční shell Wear OS platformy — uvnitř scope_module_set(Wear).")
    # feat/<feature>/fe/wear/* -- wear-specific driving/driven siblings.
    if file.startswith("feat/authentication/fe/wear/driven/"):
        return ("local", True,
                "Wear-specifický driven adaptér (AuthTokenProvider substituce pro Wear OIDC). "
                "Uvnitř scope_module_set(Wear); opakuje se per funkcionalita vyžadující Wear adaptér.")
    if file.startswith("feat/projects/fe/wear/driving/"):
        return ("local", True,
                "Wear-specifický driving (UI) modul. "
                "Uvnitř scope_module_set(Wear); opakuje se per funkcionalita portovaná na Wear.")
    # koinModulesAggregate/fe/wear/* -- wear-specific Koin aggregator.
    if file.startswith("koinModulesAggregate/fe/wear/"):
        return ("local", True,
                "Wear-specifický Koin agregátor. "
                "Uvnitř scope_module_set(Wear); opakuje se per funkcionalita s Wear Koin modulem.")

    # -- Outside scope: nonWear siblings (P4 Koin split collateral) ----------

    # nonWear sibling — extracted phone-Android binding from the previously-shared common module.
    # Recurs per Wear-affected feature: each feature must extract the phone-only adapter.
    if file.startswith("feat/authentication/fe/nonWear/driven/"):
        return ("collateral", True,
                "Paralelní nonWear modul (drží AuthTokenProvider vazbu dříve nadměrně sdílenou v common). "
                "Strukturální důsledek Koin rozdělení P4 vynuceného Wear extenzí; opakuje se "
                "u každé funkcionality vyžadující per-platform vazbu.")
    # common/driven file edits — moved AuthTokenProvider binding out (collateral structural).
    if file.startswith("feat/authentication/fe/common/driven/"):
        if status == "R":
            return ("collateral", False,
                    "Git rename: feat/authentication/fe/driven/impl → feat/authentication/fe/common/driven "
                    "(D8 variant-first refaktoring). Strukturální, beze změny sémantiky kódu.")
        if status == "A":
            return ("collateral", False,
                    "Společný Koin modul po platform-extraction refaktoringu: vazba AuthTokenProvider "
                    "přesunuta do paralelních modulů nonWear/wear. Předchází Phase 3; vyvolané rozšířením Wear.")
        return ("collateral", False,
                "Společný Koin modul: vazba AuthTokenProvider odstraněna (nyní per-varianta); "
                "viditelnost Mock/Oidc tříd rozšířena, aby je paralelní moduly mohly bind. "
                "Obě úpravy vyvolané rozšířením Wear; sledováno jako collateral strukturální náklad.")
    # build-logic ArchCheck parser — recognises new platform-variant tokens.
    if file.startswith("build-logic/"):
        return ("collateral", False,
                "Aktualizace parseru archCheck pro rozpoznání variant-suffix tokenů (common/nonWear/wear). "
                "Jednorázová úprava per kódová základna (koncept parseru se generalizuje).")
    # Composer App AppModule — switch from frontendModulesAggregate to frontendModulesCommonAggregate.
    if file == "composeApp/src/commonMain/kotlin/cz/adamec/timotej/snag/di/AppModule.kt":
        return ("collateral", False,
                "Kompoziční kořen telefonní aplikace: přejmenování agregátního valu + přidán nonWear agregát. "
                "Jednorázová úprava spotřebitele po D8 refaktoringu.")
    if file == "composeApp/build.gradle.kts":
        return ("collateral", False,
                "composeApp build závislosti upravené pro variant-suffix cesty modulů.")
    # gradle/libs.versions.toml — added wear-phone-interactions catalog entry.
    if file == "gradle/libs.versions.toml":
        return ("collateral", False,
                "Přidán katalogový záznam androidx.wear:wear-phone-interactions (jednorázově per platforma).")
    # testInfra — likely refactor for new package paths.
    if file.startswith("testInfra/"):
        return ("collateral", False,
                "Test infra aktualizována pro přejmenovaný Koin val "
                "(frontendModulesAggregate → frontendModulesCommonAggregate).")
    # Renames from projects driving/impl → projects/nonWear/driving — D8 mobile extraction.
    if "feat/projects/fe/nonWear/driving/" in file or file.startswith("feat/projects/fe/nonWear/driving/"):
        if status == "R":
            return ("collateral", False,
                    "Git rename: feat/projects/fe/driving/impl → feat/projects/fe/nonWear/driving "
                    "(D8 mobile-extraction). Strukturální, beze změny sémantiky kódu.")
        if status == "A":
            return ("collateral", False,
                    "Nový nonWear driving soubor po extraction. Mobile-only díky variant-suffix modulu.")
        return ("collateral", False,
                "nonWear driving soubor upravený během D8 mobile-extraction refaktoringu.")
    # Renames into projects/common/driving — D8 mobile-extraction (VMs split into common).
    if file.startswith("feat/projects/fe/common/driving/"):
        if status == "R":
            return ("collateral", False,
                    "Git rename: feat/projects/fe/driving/impl → feat/projects/fe/common/driving "
                    "(D8 split — VMs/UiState přesunuty do common). Strukturální.")
        return ("collateral", False,
                "Společný projects driving soubor po D8 splitu.")
    # Anything still in old projects driving/impl path is a deletion (the rename's old side).
    if file.startswith("feat/projects/fe/driving/impl/") or file.startswith("feat/authentication/fe/driven/impl/"):
        if status == "D":
            return ("collateral", False,
                    "Soubor ze staré impl-module cesty odstraněn; obsah přesunut do paralelního common/nonWear/wear modulu.")
    # Aggregates: koinModulesAggregate NON-wear variant siblings (common, nonWear).
    if file.startswith("koinModulesAggregate/"):
        return ("intrinsic", True,
                "Koin agregátor (common/nonWear varianty) — každá platformní varianta se zde wires. "
                "Přijímá edit na každý budoucí inkrement.")
    # Anything else: leave unclassified for human review.
    return None


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Classify wearos-project-list.yaml entries by path pattern."
    )
    parser.add_argument(
        "--reclassify",
        action="store_true",
        help=(
            "Override already-classified entries where the path rule now emits "
            "a different bucket. Use when scope_module_set rule changes."
        ),
    )
    args = parser.parse_args()

    if not CLASSIFICATION.is_file():
        print(f"ERROR: {CLASSIFICATION} not found", file=sys.stderr)
        return 1
    with CLASSIFICATION.open(encoding="utf-8") as f:
        data = yaml.safe_load(f)

    classified = 0
    reclassified = 0
    skipped = 0
    for entry in data["repair_log"]:
        current_bucket = entry["bucket"]
        result = classify(entry["file"], entry.get("status", ""))
        if result is None:
            skipped += 1
            continue
        bucket, recurring, reason = result
        if current_bucket == "unclassified":
            entry["bucket"] = bucket
            entry["recurring"] = recurring
            entry["source"] = "hand:wearos_classifier"
            entry["reason"] = reason
            classified += 1
        elif args.reclassify and current_bucket != bucket:
            print(
                f"RECLASS {entry['file']}: {current_bucket} -> {bucket}",
                file=sys.stderr,
            )
            entry["bucket"] = bucket
            entry["recurring"] = recurring
            entry["source"] = "hand:wearos_classifier"
            entry["reason"] = reason
            reclassified += 1

    with CLASSIFICATION.open("w", encoding="utf-8") as f:
        yaml.safe_dump(data, f, sort_keys=False, width=120, allow_unicode=True)

    print(f"classified={classified} reclassified={reclassified} skipped={skipped}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
