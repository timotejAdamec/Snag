# CLAUDE.md

This file describes common mistakes and confusion points that you might encounter
as you work in this project. If you ever encounter something in the project that surprises
you, please alert the developer working with you and indicate that this is the case in the
CLAUDE.md file to help prevent the same issue in the future.

## Changes verification

### Up-to-date tests

For new code, tests should be created.

### Gradle

Before creating a PR, run `check`.

### Following patterns

After implementation, verify if all changed files are in line with existing implementations of other
features/libs and if there are any oddities, tell the developer working with you.

## Database changes

Do not worry about migrations, this is not in production yet.

## Available skills

- `/project-structure` — KMP & JVM module structure with Clean Architecture + Hexagonal layers
- `/gradle-plugins` — Custom Gradle convention plugins reference
- `/thesis-reminder` — Reminds to update the master's thesis when major changes are made

## Keeping thesis up to date

Whenever there are some key changes, decide whether the thesis at ~/Ctu/dp-thesis-timotej-adamec/
should be updated and if yes, ask the developer working with you if and what should be updated there.
After confirmation, update it there. If you find something not matching in the thesis, also ask
the developer to clarify it and if you should make changes about that there too.
