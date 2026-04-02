# CLAUDE.md

This file describes common mistakes and confusion points that you might encounter
as you work in this project. If you ever encounter something in the project that surprises
you, please alert the developer working with you and indicate that this is the case in the
CLAUDE.md file to help prevent the same issue in the future.

## UI guidelines

Follow Material 3 Expressive (adaptive). For any component (https://m3.material.io/components) to be
used, see the 4 subsections:
- https://m3.material.io/components/{component}/overview
- https://m3.material.io/components/{component}/specs
- https://m3.material.io/components/{component}/guidelines
- https://m3.material.io/components/{component}/accessibility

## Code policy

Use named arguments for invocations. The only exception is when there is only a single parameter
which's meaning is obvious.

Use trailing comma.

## New modules

Try implementing the `build.gradle.kts` just with the convention plugin described in `docs/gradle_plugins.md`
without any explicit dependencies. The convention plugin should autowire the necessary dependencies.
Only add if the autowiring does not wire them.

## Changes verification

### Up-to-date tests

For new code, tests should be created.

### Gradle check

After finishing making changes run `check`. If it fails, analyze **all** errors from the full output,
fix them all in one pass, then re-run `check` once to confirm. Do not fix-and-rerun iteratively one
error at a time – see *ALL* the errors at once.

### Following patterns

After implementation, verify if all changed files are in line with existing implementations of other
features/libs and if there are any oddities, tell the developer working with you.

## Database changes

Do not worry about migrations, this is not in production yet.

## Available skills

- `/project-structure`
- `/gradle-plugins`
- `/thesis-reminder`

## Keeping thesis up to date

After each change, analyze the contents of the thesis ~/Ctu/dp-thesis-timotej-adamec/ decide whether
it should be updated and if yes, ask the developer working with you if and what should be updated there.
After confirmation, update it there. If you find something not matching in the thesis, also ask
the developer to clarify it and if you should make changes about that there too.
