# Release Process

## Versioning Scheme

All version computation is centralized in `build-logic/.../consts/SnagVersioning.kt` and shared across all platforms via `buildkonfig`.

- **Semantic version** (`0.2.0`) — derived from the latest git tag (`v0.2.0`) using `git describe --tags --abbrev=0`. Falls back to `0.0.0` when no tags exist.
- **Version code** (`260324042`) — `YYMMDD * 1000 + (commitCount % 1000)`, auto-computed at build time. Example: 2026-03-24, 42 commits → `260324042`
- **Version name** (`0.2.0.260324042`) — composite of semantic version and version code

All three values are exposed to every platform (Android, iOS, Web, Desktop) as compile-time constants via `buildkonfig` (`SEMANTIC_VERSION`, `VERSION_CODE`, `VERSION_NAME`). Android additionally uses version code and version name natively for `versionCode`/`versionName`. Desktop native distributions use only the semantic version (OS package managers require X.Y.Z format).

## One-Time Setup (Android Signing)

### 1. Generate a release keystore

```bash
./scripts/generate-release-keystore.sh
```

This creates `androidApp/keystore/release.jks`. You will be prompted for a store password and key password.

### 2. Configure local signing

Add the following to `local.properties`:

```properties
snag.release.storeFile=keystore/release.jks
snag.release.storePassword=<your-store-password>
snag.release.keyAlias=snag-release
snag.release.keyPassword=<your-key-password>
```

### 3. Configure GitHub secrets

For the CI release workflow, add these repository secrets:

| Secret                       | Value                                      |
|------------------------------|-------------------------------------------|
| `RELEASE_KEYSTORE_BASE64`   | `base64 -i androidApp/keystore/release.jks \| pbcopy` |
| `RELEASE_STORE_PASSWORD`    | Keystore password                          |
| `RELEASE_KEY_ALIAS`         | `snag-release`                             |
| `RELEASE_KEY_PASSWORD`      | Key password                               |

## Making a Release

1. Tag and push:
   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```
2. GitHub Actions automatically builds all platform artifacts and creates a GitHub Release with them attached
3. Artifacts are downloadable from the [Releases](../../releases) page

The version is derived entirely from the git tag — no manual version bumps needed anywhere.

## Platform Artifacts

| Platform | Gradle task | Output |
|----------|------------|--------|
| Android  | `:androidApp:assembleRelease` | APK |
| Linux    | `:composeApp:packageReleaseDeb` | DEB |
| macOS    | `:composeApp:packageReleaseDmg` | DMG |
| Windows  | `:composeApp:packageReleaseMsi` | MSI |
| Web (JS) | `:composeApp:jsBrowserDistribution` | JS bundle |
| Web (Wasm) | `:composeApp:wasmJsBrowserDistribution` | WasmJS bundle |
| iOS      | `:composeApp:linkReleaseFrameworkIosArm64` | Framework |

## Local Release Builds

```bash
# Android APK
./gradlew :androidApp:assembleRelease

# Desktop (current OS)
./gradlew :composeApp:packageReleaseDistributionForCurrentOS

# Web
./gradlew :composeApp:jsBrowserDistribution
./gradlew :composeApp:wasmJsBrowserDistribution

# iOS framework
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

## Build Types

| Build type | Application ID suffix | Minified | Debuggable | Server target |
|------------|----------------------|----------|------------|---------------|
| `debug`    | `.debug`             | No       | Yes        | `localhost`   |
| `release`  | (none)               | Yes      | No         | `dev`         |

Debug and release builds can coexist on the same device thanks to the `.debug` application ID suffix.

## Server Target Configuration

Server target is determined at compile time via `buildkonfig` and applies to all platforms (Android, iOS, desktop, web).

**Config files** in `config/`:
- `config/debug.properties` — used for debug builds (default: `localhost`)
- `config/release.properties` — used for release builds (default: `dev`)

Detection is automatic based on Gradle task names (e.g., `assembleRelease`, `linkReleaseFramework`).

**Override** with `-P` flag (takes precedence over config files):
```bash
./gradlew :androidApp:assembleRelease -Psnag.serverTarget=demo
```
