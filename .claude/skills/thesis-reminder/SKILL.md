---
name: thesis-reminder
description: Reminds to update the master's thesis when a major update or addition is done in the Snag repository that is worth writing about. Suggests which chapters to update.
user-invocable: false
trigger: after_completion
allowed-tools: Read, Glob, Grep
---

# Thesis Update Reminder

After completing a major task in this repository, evaluate whether the work is worth documenting in the thesis. If it is, remind the user to update their thesis.

## When to trigger

Remind the user when the completed work involves any of these:
- New architectural decisions or significant refactors
- New features or major feature changes
- New technology integrations or library adoptions
- New patterns, conventions, or abstractions introduced
- Performance improvements with measurable results
- Cross-platform code sharing strategies or changes
- Database schema changes or sync mechanism updates
- UI/design system changes
- Build system or CI/CD changes of architectural significance

Do NOT remind for:
- Minor bug fixes, typos, formatting
- Routine dependency updates
- Small refactors with no architectural significance
- Test-only changes (unless introducing a new testing pattern)

## Thesis location

`~/Ctu/dp-thesis-timotej-adamec/` — the main content is in `text/text.tex`.

## Thesis chapters

The thesis is written in Czech. These are the available chapters:

- **Analýza (Analysis)** — domain intro, existing solutions comparison, framework comparison, code sharing maximization and evolvability analysis, KMP/CMP specifics, AI possibilities, user stories, requirements, domain model, wireframes
- **Návrh (Design)** — architecture (LC/HC, DVT, AVT, SOS, SOC), modularization, Clean Architecture layering, encapsulation (API/impl/test), shared FE+BE architecture, module/package structure, UI design, ER model, offline-first design
- **Implementace (Implementation)** — technology choices (SQLDelight, Ktor, Navigation 3), feature implementations, offline-first sync (UUID, ops queue, conflict resolution), code sharing evaluation, AI-assisted development, deployment, monitoring, testing

Suggest one or more chapters. Read `~/Ctu/dp-thesis-timotej-adamec/text/text.tex` to determine the specific section(s) within the chapter to update.

## Reminder format

When reminding, use this format:

```
📝 **Thesis update suggested**

The work you just completed ([brief description]) is worth documenting in your thesis (`~/Ctu/dp-thesis-timotej-adamec/text/text.tex`).

**Suggested chapter(s) to update:**
- **[Chapter name]** — [what to write about]

Would you like me to help draft the content?
```

Suggest one or more sections. Be specific about what aspect of the work should be documented.
