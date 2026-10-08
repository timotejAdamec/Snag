---
name: pre-pr
description: Gets the branch PR-ready with ktlint autoformat, then a green `check`. Use when work is finished, or before opening/updating a PR.
---

# Pre-PR

Formatting runs first: `check` includes `ktlintCheck`, which fails on anything `ktlintFormat` would fix.

1. Run `./gradlew ktlintFormat`.
   - It fails → output lists violations ktlint cannot autocorrect (`file:line`). Fix them all, rerun.
   - Done when it exits successfully. Commit any changed files as `style: ktlint format`.
2. Run `./gradlew check --continue`.
   - It fails → collect **all** errors from the full output, fix them in one pass, rerun once.
   - Done when `check` is green. Commit the fixes.

Finished when both steps are done and the working tree is clean.
