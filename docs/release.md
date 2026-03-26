# Release Process

## Versioning Scheme

- **`versionCode`** = `YYMMDD000 + (commitCount % 1000)` — auto-computed at build time
  - Example: 2026-03-24, 42 commits → `260324042`
- **`versionName`** = `<semantic>.<versionCode>` — e.g., `0.1.0.260324042`
- **Semantic version** stored in `gradle/libs.versions.toml` as `snag-app`

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

1. Bump `snag-app` version in `gradle/libs.versions.toml` (e.g., `0.1.0` → `0.2.0`)
2. Commit: `Release 0.2.0`
3. Tag and push:
   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```
4. GitHub Actions automatically builds all platform artifacts and creates a GitHub Release with them attached
5. Artifacts are downloadable from the [Releases](../../releases) page

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
