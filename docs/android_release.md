# Android Release Process

## Versioning Scheme

- **`versionCode`** = `YYMMDD000 + (commitCount % 1000)` — auto-computed at build time
  - Example: 2026-03-24, 42 commits → `260324042`
- **`versionName`** = `<semantic>.<versionCode>` — e.g., `0.1.0.260324042`
- **Semantic version** stored in `gradle/libs.versions.toml` as `snag-app`

## One-Time Setup

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
|------------------------------|--------------------------------------------|
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
4. GitHub Actions automatically builds a signed release APK and creates a GitHub Release with the APK attached
5. The APK is downloadable from the [Releases](../../releases) page

## Local Release Build

For testing a release build locally:

```bash
./gradlew :androidApp:assembleRelease
```

The APK will be at `androidApp/build/outputs/apk/release/`.

## Build Types

| Build type | Application ID suffix | Minified | Debuggable |
|------------|----------------------|----------|------------|
| `debug`    | `.debug`             | No       | Yes        |
| `release`  | (none)               | Yes      | No         |

Debug and release builds can coexist on the same device thanks to the `.debug` application ID suffix.
