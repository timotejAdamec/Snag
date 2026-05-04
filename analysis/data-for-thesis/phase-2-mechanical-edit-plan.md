# Phase 2 mechanical-edit inventory for thesis §4.1–§4.9

**Purpose.** Drop-in playbook for the bundled mechanical LaTeX commit to `~/Ctu/dp-thesis-timotej-adamec/` described in `analysis/phase-2-plan.md` §K.4. Every T-row from §K.1, plus the §4.6 `\todo{}` fill, is resolved here to a concrete landing target (file + line) and either (a) verbatim drop-in content or (b) a pull-from-file:line pointer. A future session opens this document, opens `text/text.tex`, and executes the edits in one pass — no Snag-repo exploration required.

**Scope.** Mechanical content only: table rows (with real numbers), one-sentence caveats, primary-source bullets, and one quote + comparison table for the §4.6 `\todo{}`. Czech wording proposals are marked "Phase 5 refinement candidate" — Phase 5 prose author is free to rephrase; the sentence *content* is locked by the Phase 2 plan.

**Thesis target.** Worktree `~/Ctu/dp-thesis-timotej-adamec/.worktrees/phase2-vyhodnoceni-draft/` on branch `feat/phase2-vyhodnoceni-draft` (HEAD `a3b7218`). File `text/text.tex`. Line numbers below are relative to that HEAD; rebase if the worktree has moved.

**Out of scope.** Phase 5 prose (T-6, T-13, T-14 expansions beyond the two-sentence source pointer) and the §4.7 cross-case roll-up table prose (T-8 / T-15 write-up). This inventory only decides cell content; prose is Phase 5.

---

## Landing targets — file + line map

| Target                      | File             | Line range (HEAD `a3b7218`) | Nature of edit                        |
|-----------------------------|------------------|------------------------------|---------------------------------------|
| §4.1 op-table rows          | `text/text.tex`  | 3183–3198                    | Row insertions / existing-row edits   |
| §4.2 intro (method note)    | `text/text.tex`  | 3237 (end of first paragraph)| One-sentence append                   |
| §4.3 (evolvabilita-feature) | `text/text.tex`  | 3243–3252                    | No direct edit; reference only        |
| §4.6 NS-theorem section     | `text/text.tex`  | 3264–3278                    | New sub-paragraphs for Cases 1/3/4 + `\todo{}` fill at 3277 |
| §4.7 Výsledky               | `text/text.tex`  | 3279–3288                    | `\todo{}` fill at 3288 with headline numbers |
| §4.9 threats-to-validity    | `text/text.tex`  | 3295–3308                    | One-sentence append at end of duality paragraph (3304) |

---

## Per-edit specifications

### T-1 — §4.1 O1 row (Part A layer-divergence)

**Landing.** Insert new row in `\begin{tabular}` at `text/text.tex` line 3188 (end of O1 block, before `\hline` at 3189).

**Verbatim LaTeX cell content** (adapted from `analysis/phase-2-plan.md` §C lines 433–444, result cell filled from `analysis/data/layer_divergence.csv`):

```latex
O1 & Podíl platformně-specifického LOC podle části architektury (deskriptivní, nevyvracející) & SharingReport CSV $\bowtie$ tokei LOC & \code{figures.py::figure\_layer\_divergence} & viz obr.\ \ref{fig:eval-layer-divergence} \\
```

**Also insert.** Reference to the figure: a `\begin{figure}` block citing `analysis/figures/fig_4_2_layer_divergence.pdf` somewhere in §4.2 (recommended: after line 3241 `\todo{}`, as part of the same edit).

**Source of truth.** `analysis/data/layer_divergence.csv` + `analysis/figures/fig_4_2_layer_divergence.pdf/png` (shipped in commit `6905cca15`).

---

### T-2 — §4.1 O2a row (Case 1b correct branch)

**Landing.** Insert new row at line 3195 (between existing O2 kombinatorický-efekt row and O2 DVT row).

**Verbatim LaTeX cell content:**

```latex
O2 & Ripple dekompozice korektní větve (AddFindingPhoto realistická web-specifická evoluce, paralelní experiment ke~commonizované variantě) & Diff větve \code{experiment/photo-progress-correct} proti \code{main} & \code{feature\_retro.py --ref experiment/photo-progress-correct} & 8 souborů / 97 LOC / všechny \emph{local}, 0 \emph{intrinsic}, 0 \emph{collateral} \\
```

**Source of truth.** `analysis/data/ripple_photo-progress-correct_units.csv` (8 units all local, churn 97).

---

### T-3 — §4.1 O2b row (Case 1b commonized branch)

**Landing.** Insert new row at line 3195 (immediately after T-2).

**Verbatim LaTeX cell content:**

```latex
O2 & Ripple dekompozice vynuceně sjednocené větve (táž evoluce aplikovaná po~fúzi platformních use cases do~\code{commonMain}) -- kvantifikuje cenu sjednocení jako $B_2/B_1$ & Diff větve \code{experiment/photo-progress-commonized} proti \code{main} & \code{feature\_retro.py --ref experiment/photo-progress-commonized} & 12 souborů / 100 LOC / všechny \emph{local}; $B_2/B_1 = 2{,}0\times$ blast radius na~modul, $3\times$ přeložených FE listů \\
```

**Source of truth.** `analysis/data/ripple_photo-progress-commonized_units.csv` (12 units all local, churn 100) + `analysis/data/counterfactual_photo_progress.md` (B2/B1 ratio).

---

### T-4 — §4.2 introduction method note

**Landing.** Append one sentence to the end of the existing first paragraph at `text/text.tex` line 3237 (before the blank line separating it from the "Share ratio udává…" paragraph).

**Phase 5 refinement candidate — Czech one-sentence placeholder:**

> *"Tyto metriky jsou z~podstaty deskriptivní: stejné hodnoty jsou kompatibilní s~více architektonickými scénáři (korektně rozdělené, nadgeneralizované, fragmentované), a~korektnost rozvahy je argumentována protipříkladem v~\ref{sec:eval-ns-teoremy} a~kaveáty v~\ref{sec:eval-diskuse-hrozby}."*

**Content lock** (from §K.1 T-4 source row). Sentence must state three things: (1) per-layer sharing metrics describe structure only, not correctness; (2) identical values are compatible with multiple scenarios (correctly-scoped, over-generalized, fragmented); (3) the §4.3 / §4.6 paired counterexample is the mechanism by which correctness is argued.

---

### T-5 — §4.9 threats-to-validity duality caveat

**Landing.** Append one sentence at the end of the "Spárovaný protipříklad…" paragraph at `text/text.tex` line 3304 (before the blank line at 3305).

**Phase 5 refinement candidate — Czech placeholder:**

> *"Per-vrstvé share ratios v~\ref{sec:eval-kvantifikace-sdileni} popisují strukturu, nikoliv korektnost rozdělení; argumentem pro korektnost je spárovaný protipříklad v~\ref{sec:eval-ns-teoremy}, který rozbíjí dualitu čtení tím, že měří cenu strukturálně plauzibilní alternativy."*

**Content lock.** Sentence restates the duality framing: descriptive metrics ≠ correctness; paired counterexample is the correctness-argument mechanism.

**Note.** The existing paragraph at line 3304 already discusses the skeptical "šikovnější sjednocení by cenu snížilo" angle. T-5 sentence adds the *duality-of-readings* angle specifically — these are distinct concerns and the T-5 append does not duplicate.

---

### T-6 — §4.6 Case 1 (reverse-removal) prose stub

**Landing.** New paragraph in §4.6 between line 3268 (end of "Toto mapování je interpretační pomůcka" paragraph) and line 3269 (start of "Obě funkcionality obsahují…" Case 1b paragraph). The current §4.6 only covers Case 1b; Cases 1, 3, 4 should be referenced in this same section per §J.5 lines 775–789.

**Phase 5 writes the prose.** This inventory provides the source-pointer and NS-observation count:

- **Primary source.** `analysis/classifications/inspections-reverse-removal_critique.md` (shipped with Case 1 on `chore/phase-2-ripple-tooling`).
- **Headline number (T-7, verbatim for Phase 5).** `N = 6 recurring-intrinsic units (4 distinct NS anomaly sites after collapsing the :root::non-module path→unit artifact for build.gradle.kts files)`. Also: files local/intrinsic/collateral = `89 / 7 / 43`; churn `5281 / 99 / 691 = 6071 total`.
- **NS theorem heading coverage in the critique.** SoC = 3 observations (Koin BE, Koin FE, settings.gradle.kts), DVT = 1 (dev-data seeder), ISP = 1 (test-infra), contrast/general = 1 (89 local files absorb 87% churn). Phase 5 prose groups under these headings when pulling from the critique file.

---

### T-7 — §4.6 headline number (Case 1)

**Landing.** Inline in T-6 prose paragraph (no separate insertion point). Quote verbatim from the critique headline block: *"N = 6 recurring-intrinsic units (4 distinct NS anomaly sites)."*

**Reproducibility command** (record in §4.6 footnote or §4.9):

```bash
python analysis/feature_retro.py \
  --change inspections-reverse-removal \
  --ref experiment/remove-inspections \
  --base-ref main \
  --base-snapshot analysis/data/sharing_report_with_loc_base_main_e076e89e5.csv \
  --change-kind feature_remove \
  --finalize
```

---

### T-8 / T-15 — §4.7 cross-case roll-up table

**Landing.** Replace `\todo{}` at `text/text.tex` line 3288 with a real table. This is the single place where all five cases appear side by side; per §J.5 this is the §4.7 headline artifact.

**Verbatim LaTeX table skeleton** (Phase 5 may tune wording; numbers are locked):

```latex
\begin{table}[h]
\centering
\caption{Ripple dekompozice napříč pěti případovými studiemi -- rekurentní intrinsické jednotky (sites kombinatorického efektu), kolaterální soubory, lokální soubory a~celkový churn.}
\label{tab:eval-cross-case-rollup}
\small
\begin{tabular}{p{0.18\linewidth}p{0.18\linewidth}p{0.12\linewidth}p{0.12\linewidth}p{0.12\linewidth}p{0.14\linewidth}}
\hline
\textbf{Případ} & \textbf{Typ} & \textbf{recurring intrinsic} & \textbf{collateral} & \textbf{local} & \textbf{churn celkem} \\
\hline
Případ 1 (\code{feat/inspections} reverse removal)          & feature\_remove  & 6 (4 distinct) & 43 & 89 & 6071 \\
Případ 1b korektní (\code{photo-progress-correct})           & feature\_add     & 0              & 0  & 8  & 97 \\
Případ 1b commonizovaná (\code{photo-progress-commonized})   & feature\_add     & 0              & 0  & 12 & 100 \\
\quad(kvantifikuje $B_2/B_1 = 2{,}0\times$ na~modul, $3\times$ FE listů) & & & & & \\
Případ 2 (\code{ProjectPhoto} forward, deskriptivní)         & feature\_add     & 1              & 4  & 90 & 4428 \\
Případ 3 (\code{dvt-client-field} DVT syntetický)            & entity\_extend   & 1              & 0  & 16 & 102 \\
Případ 4 (\code{ios-only-project-field} platformní osa)      & platform\_extend & 1              & 0  & 2  & 24 \\
\hline
\end{tabular}
\end{table}
```

**Source of truth per row:**

- Case 1 — `analysis/data/ripple_inspections-reverse-removal_units.csv`.
- Case 1b correct — `analysis/data/ripple_photo-progress-correct_units.csv`.
- Case 1b commonized — `analysis/data/ripple_photo-progress-commonized_units.csv` + B2/B1 from `analysis/data/counterfactual_photo_progress.md`.
- Case 2 — `analysis/data/ripple_projectphoto-forward_units.csv`.
- Case 3 — `analysis/data/ripple_dvt-client-field_units.csv` + `dvt-client-field_critique.md` headline block.
- Case 4 — `analysis/data/ripple_ios-only-project-field_units.csv` + `ios-only-project-field_critique.md` headline block.

**Note on Case 2.** The one recurring-intrinsic unit is the `:root::non-module` unit (loc 101, 7 files — build.gradle.kts collateral per `build_gradle_collateral` rule). The four collateral files are the two Koin aggregate + one settings-registry + one report-module downstream edits. See the Case 2 critique `projectphoto-forward_critique.md` for the site-level mapping.

---

### T-9 — §4.1 O3 row (Case 1 reverse-removal op row)

**Landing.** Insert new row at line 3192 (immediately after existing O2 reverse-removal row; the existing row at 3191 has TBD result and generic wording, this T-row replaces it with a fully-specified entry). **Prefer edit-in-place at line 3191** rather than insert — the row already exists; update the result cell and the dataset cite.

**Verbatim LaTeX cell content** (edit of line 3191):

```latex
O2 & Ripple dekompozice reverzního odstranění funkcionality \code{feat/inspections} do~lokálního, intrinsického a~kolaterálního kbelíku s~počtem rekurentních intrinsických anomálií podle NS teorému & Repair-log větve \code{experiment/remove-inspections} + \code{analysis/data/ripple\_inspections-reverse-removal\_*.csv} & \code{feature\_retro.py --ref experiment/remove-inspections --base-ref main --base-snapshot analysis/data/sharing\_report\_with\_loc\_base\_main\_e076e89e5.csv --change-kind feature\_remove --finalize} & 6 rekurentních intrinsických jednotek (4 distinct sites); local/intrinsic/collateral = 89/7/43; churn 6071 \\
```

**Source of truth.** `analysis/data/ripple_inspections-reverse-removal_units.csv` + `analysis/classifications/inspections-reverse-removal_critique.md` headline.

---

### T-10 — §4.6 methodological caveat one-liner

**Landing.** Append one sentence at the end of the "Toto mapování je interpretační pomůcka" paragraph at `text/text.tex` line 3268 (before the blank line at 3269).

**Phase 5 refinement candidate — Czech placeholder:**

> *"Případová studie 1 (\ref{sec:eval-evolvabilita-feature}) je citována jako důkaz správnosti v~omezeném smyslu: tvrzení zní ,,kombinatorické dotyky jsou ohraničeny vyjmenovanými NS anomálními místy``, nikoliv ,,architektura je optimální`` -- detail v~\code{analysis/classifications/inspections-reverse-removal\_critique.md}."*

**Content lock.** Sentence must (1) name Case 1 as correctness evidence, (2) state the bounded claim form ("touches bounded by named NS sites"), (3) disclaim the stronger "optimal architecture" claim, (4) cite the critique file.

---

### T-11 — §4.1 DVT row data-fill (Case 3)

**Landing.** Edit-in-place at `text/text.tex` line 3196. Existing row says:

```latex
O2 & DVT syntetický test scénář (volitelné) & Diff větve \code{experiment/dvt-synthetic} & \code{feature\_retro.py --ref experiment/dvt-synthetic} & TBD \\
```

**Verbatim replacement:**

```latex
O2 & Ripple dekompozice DVT syntetického testu sdíleného atributu přes dědičnost a~BE-only atributu (Client entita) -- testuje mechanismy DVT (1) downward propagation, (1) upward containment, (2) default absorption & Diff větve \code{experiment/dvt-client-field} proti \code{main} + \code{analysis/data/ripple\_dvt-client-field\_*.csv} & \code{feature\_retro.py --ref experiment/dvt-client-field --change-kind entity\_extend --finalize} & 0 anomálií (3a non-essential = 0, 3b upward-bleed = 0, forced caller edits = 0); 1 rekurentní intrinsic unit (\code{seedTestClient}); files local/intrinsic/collateral = 16/1/0; churn 102 \\
```

**Corrections made:** (a) `experiment/dvt-synthetic` → `experiment/dvt-client-field`; (b) removed "volitelné" (shipped); (c) added dataset reference; (d) filled TBD with headline numbers.

**Source of truth.** `analysis/classifications/dvt-client-field_critique.md` headline block + `ripple_dvt-client-field_units.csv`.

---

### T-12 — §4.1 iOS-only row data-fill (Case 4)

**Landing.** Edit-in-place at `text/text.tex` line 3197. Existing row says:

```latex
O2 & Syntetické iOS-only rozšíření entity (volitelné) & Diff větve \code{experiment/ios-only-extension} & \code{feature\_retro.py --ref experiment/ios-only-extension} & TBD \\
```

**Verbatim replacement:**

```latex
O2 & Ripple dekompozice platformní osy: přidání atributu \code{widgetPinned: Boolean} do~nového FE-specifického app-model modulu s~obsahem pouze v~\code{iosMain} source setu (mirror \code{be/app/model/} pattern) -- testuje SoC na~multiplatformě-vrstvové dimenzi & Diff větve \code{experiment/ios-only-project-field} proti \code{main} + \code{analysis/data/ripple\_ios-only-project-field\_*.csv} & \code{feature\_retro.py --ref experiment/ios-only-project-field --change-kind platform\_extend --finalize} & 0 anomálií (commonMain = 0, non-iOS source set = 0, cross-feature = 0); 1 rekurentní intrinsic unit (\code{:root::settings}); files local/intrinsic/collateral = 2/1/0; churn 24 \\
```

**Corrections made:** (a) `experiment/ios-only-extension` → `experiment/ios-only-project-field`; (b) removed "volitelné"; (c) added dataset reference; (d) filled TBD with headline numbers.

**Source of truth.** `analysis/classifications/ios-only-project-field_critique.md` headline block + `ripple_ios-only-project-field_units.csv`.

**Also update the bash snippet** at lines 3216–3218: change `experiment/ios-only-extension` → `experiment/ios-only-project-field` and `experiment/dvt-synthetic` → `experiment/dvt-client-field`.

---

### T-13 — §4.6 Case 3 primary-source pointer

**Landing.** New paragraph in §4.6 between existing Case 1b prose (ending at line 3275) and the `\todo{}` at line 3277. Alternative: new `\subsection{Případová studie 3 -- DVT na entitě Client}` if Phase 5 author prefers explicit case-heading structure.

**Phase 5 writes the prose.** This inventory provides:

- **Primary source.** `analysis/classifications/dvt-client-field_critique.md` (shipped with Case 3 in the same commit as the Case 3 §K.1 backfill).
- **Headline numbers.** `0 anomalies (3a non-essential = 0, 3b upward-bleed = 0, forced caller edits = 0); 1 recurring-intrinsic unit (seedTestClient test fixture); 17 files classified local/intrinsic/collateral = 16/1/0; churn 98/4/0 = 102 LOC`.
- **NS theorem heading coverage.** DVT = 2 observations (DVT-0 zero non-essential touches, DVT-1 test fixture intrinsic), SoC = 1 derivative (zero upward bleed of BE-only `adminNote`).
- **Anchor quote (one line, for Phase 5).** *"Both DVT mechanisms confirmed: inheritance carries shared `ico` through `Client → AppClient → BackendClient` without redeclaration at intermediate interfaces, and defaults absorb at all existing construction sites with zero caller edits."*
- **Experiment branch.** `experiment/dvt-client-field` (never merged).

---

### T-14 — §4.6 Case 4 primary-source pointer

**Landing.** New paragraph immediately after T-13 (or new `\subsection{Případová studie 4 -- iOS-only rozšíření Project}`).

**Phase 5 writes the prose.** This inventory provides:

- **Primary source.** `analysis/classifications/ios-only-project-field_critique.md` (shipped with Case 4).
- **Headline numbers.** `0 anomalies (commonMain forced touches = 0, non-iOS source set = 0, cross-feature = 0); 1 recurring-intrinsic unit (:root::settings); 3 files classified local/intrinsic/collateral = 2/1/0; churn 23/1/0 = 24 LOC`.
- **NS theorem heading coverage.** SoC (platform axis) = 2 observations (SoC-0 zero forced touches, SoC-1 settings.gradle.kts intrinsic not-an-anomaly).
- **Anchor quote (one line, for Phase 5).** *"Zero forced touches observed outside the new module's own files and the intrinsic `settings.gradle.kts` registration line -- the architecture contained the iOS-only attribute at the iOS source set as predicted."*
- **Experiment branch.** `experiment/ios-only-project-field` (never merged).

---

### §4.6 `\todo{}` fill — Case 1b blast-radius comparison table + commonized-critique quote

**Landing.** Replace the `\todo{}` block at `text/text.tex` line 3277.

**Verbatim LaTeX fill:**

```latex
\begin{table}[h]
\centering
\caption{Cena vynuceného sjednocení platformně-specifického rozdělení -- srovnání blast radius a~zasažených jednotek mezi korektní větví (\code{experiment/photo-progress-correct}) a~sjednocenou větví (\code{experiment/photo-progress-commonized}) na~stejné evoluci (přidání progress callbacku).}
\label{tab:eval-photo-counterfactual}
\small
\begin{tabular}{p{0.30\linewidth}p{0.18\linewidth}p{0.18\linewidth}p{0.18\linewidth}}
\hline
\textbf{Metrika} & \textbf{Korektní} & \textbf{Sjednocená} & \textbf{Poměr $B_2/B_1$} \\
\hline
Zasažené soubory                        & 8    & 12   & $1{,}5\times$ \\
LOC churn                               & 97   & 100  & $\approx 1{,}0\times$ \\
Zasažené jednotky (module $\times$ set) & 8    & 12   & $1{,}5\times$ \\
Blast radius na~úrovni modulů (součet)  & 28   & 56   & $2{,}0\times$ \\
Přeložených FE platformních listů       & 2    & 6    & $3{,}0\times$ \\
\hline
\end{tabular}
\end{table}

Kvalitativně reprezentativní pozorování ze~sjednocené větve (citováno doslovně z~\code{analysis/classifications/photo-commonized\_critique.md}):

\begin{quote}
,,Na \code{main} je invariant ,,native nemůže dostat \code{NetworkUnavailable}`` vyjádřen \emph{v~typovém systému}; na~commonizované větvi je týž invariant vyjádřen pouze v~inline komentářích -- binární regrese. Main = ,,kompilátor to zakáže``; commonized = ,,vývojář si musí pamatovat to nedělat``.``
\end{quote}
```

**Source of truth.** `analysis/data/counterfactual_photo_progress.md` (comparison table numbers) + `analysis/classifications/photo-commonized_critique.md` (verbatim anchor quote).

---

### §4.7 `\todo{}` fill — headline sharing numbers

**Landing.** Replace the `\todo{}` at `text/text.tex` line 3288. This is separate from T-8 / T-15 (cross-case roll-up) — the §4.7 `\todo{}` asks for O1 headline share ratios across the three paragraphs at 3282–3286, not ripple numbers.

**Status.** Blocked on Part A headline numbers from `figures.py::figure_layer_divergence` output + overall share ratio per category. Not a Phase 2 T-row; Phase 5 pulls directly from `analysis/data/layer_divergence.csv` + `sharing_report_with_loc.csv` when authoring prose.

---

## NS-theorem heading coverage audit (§J.6 item 6)

Per Agent 3 digest across all six shipped critique files:

| NS theorem | Cases where it appears as a heading                | Observation count total |
|------------|-----------------------------------------------------|-------------------------|
| SoC        | Case 1 (3), Case 1b-correct (2), Case 1b-commonized (2), Case 2 (folded under SoC/SoS, 1), Case 3 (1 derivative), Case 4 (2) | 11 |
| DVT        | Case 1 (1), Case 1b-correct (2), Case 1b-commonized (2), Case 2 (1), Case 3 (2)                                            | 8 |
| AVT        | Case 1b-correct (1), Case 1b-commonized (2), Case 2 (1)                                                                     | 4 |
| ISP        | Case 1 (1), Case 1b-correct (1), Case 1b-commonized (1), Case 2 (implicit)                                                  | 3–4 |
| SoS        | Case 2 (folded under SoC/SoS)                                                                                               | 1 |

**Verdict.** Every NS heading appears at least once across the corpus. No §4.6 gap. Phase 5 prose can pull from any critique file for any of the five NS theorems without a missing-source problem.

---

## Execution order (when the bundled thesis commit is made)

1. On `feat/phase2-vyhodnoceni-draft` in the thesis worktree. No rebase; HEAD is `a3b7218`.
2. Apply edits top-to-bottom by file line number to avoid drift:
   - Lines 3183–3198 (§4.1 op-table): T-1 (new O1 row), T-2 + T-3 (new O2a/O2b rows), T-9 (edit line 3191), T-11 (edit line 3196), T-12 (edit line 3197 + bash snippet at 3216–3218).
   - Line 3237 (§4.2): T-4 one-sentence append.
   - Lines 3268, 3277 (§4.6): T-10 one-sentence append at 3268, then T-6/T-13/T-14 new sub-paragraphs (or subsections) between 3268 and 3277, then §4.6 `\todo{}` fill at 3277.
   - Line 3288 (§4.7): T-8/T-15 cross-case roll-up table replaces `\todo{}`.
   - Line 3304 (§4.9): T-5 one-sentence append.
3. Build: `cd ~/Ctu/dp-thesis-timotej-adamec/.worktrees/phase2-vyhodnoceni-draft && latexmk -pdf text/text.tex` (or project-local build cmd). Verify no LaTeX errors introduced; verify table captions + `\ref{tab:...}` + `\ref{sec:...}` cross-refs resolve.
4. Commit with the message block from `analysis/phase-2-plan.md` §K.4.
5. Push to MR !65 branch.

---

## Cross-reference with Phase 2 plan

- §K.1 rows covered: T-1, T-2, T-3, T-4, T-5, T-6, T-7, T-8, T-9, T-10, T-11, T-12, T-13, T-14, T-15 (all fifteen).
- §K.2 artifacts cited: layer_divergence.csv + fig, counterfactual_photo_progress.md, all six critique files, all five/six ripple CSVs.
- §J.5 artifact-to-section map respected: §4.3 gets ripple-decomp tables (T-9 source is also §4.3's primary evidence), §4.6 gets NS-theorem grouping (T-6, T-10, T-13, T-14 + `\todo{}` fill), §4.7 gets O1/O2 headline numbers + cross-case roll-up (T-8 / T-15), §4.9 gets the duality caveat (T-5).
- §J.6 NS-theorem heading coverage audited; no gap found.
