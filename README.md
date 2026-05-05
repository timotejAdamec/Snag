<p align="center">
  <img src="docs/images/logo.svg" alt="Snag logo" width="160">
</p>

<h1 align="center">Snag</h1>

<p align="center">A Kotlin Multiplatform snagging system — Android, iOS, Web, Desktop (JVM), and a Ktor backend.</p>

---

## Contents

- [Screenshots](#screenshots)
- [Running the apps](#running-the-apps)
- [Running the server locally](#running-the-server-locally)
- [Choosing a server target](#choosing-a-server-target)
- [Releases](#releases)
- [Project structure](#project-structure)
- [Affiliation](#affiliation)

## Screenshots

<table>
  <tr>
    <td align="center" width="50%">
      <img src="docs/images/floor-plan-with-pins.png" alt="Interactive floor plan with finding pins (Android)" height="320"><br>
      <sub>Interactive floor plan with finding pins (Android)</sub>
    </td>
    <td align="center" width="50%">
      <img src="docs/images/report-page-structure.png" alt="Generated PDF report page" height="320"><br>
      <sub>Generated PDF report page</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="docs/images/adaptive-layout-extra-wide.png" alt="Adaptive multi-pane layout on desktop" width="100%"><br>
      <sub>Adaptive multi-pane layout on desktop</sub>
    </td>
  </tr>
</table>

## Running the apps

Frontend clients have no extra prerequisites — by default they talk to `localhost`. To point them at a hosted backend instead, see [Choosing a server target](#choosing-a-server-target).

Each app can be launched from your IDE's run-configuration widget, or from the terminal:

| Target | Command |
| --- | --- |
| Android | `./gradlew :androidApp:assembleDebug` |
| Desktop (JVM) | `./gradlew :composeApp:run` |
| Web (Wasm — modern browsers, faster) | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| Web (JS — older browsers) | `./gradlew :composeApp:jsBrowserDevelopmentRun` |
| iOS | open [`/iosApp`](./iosApp) in Xcode and run, or use the IDE run config |

> On Windows, replace `./gradlew` with `.\gradlew.bat`.

## Running the server locally

The server uses Google Cloud Storage for file storage, so a one-time GCP setup is required **only when running the backend on your machine**. Frontend-only work (or pointing the frontend at the `dev`/`demo` deployment) does not need this.

### 1. Install the Google Cloud CLI

- **macOS** (Homebrew): `brew install --cask google-cloud-sdk`
- **Windows / Linux**: see [cloud.google.com/sdk/docs/install](https://cloud.google.com/sdk/docs/install)

### 2. Authenticate

```shell
gcloud init
```

Sign in with a Google account that has access to the GCP project, and select `snag-487319`.

### 3. Set up Application Default Credentials

```shell
gcloud auth application-default login
```

This writes credentials to `~/.config/gcloud/application_default_credentials.json`, which the server picks up automatically.

> Never commit credential files — they contain sensitive tokens.

### 4. Verify

```shell
gsutil ls
```

### 5. Run the server

```shell
./scripts/run-be.sh
```

The script loads `config/common-debug.properties` and `config/backend-debug.env` into the environment, then launches `:server:run`.

## Choosing a server target

Frontend clients default to `localhost`. To target a Cloud Run deployment instead, set `snag.serverTarget` in `gradle.properties`:

```properties
snag.serverTarget=dev
```

Valid values: `localhost` (default), `dev`, `demo`. Re-sync Gradle in your IDE after changing it. Applies to all frontend targets (Android, desktop, web, iOS).

You can also pass it per-invocation without editing any files:

```shell
./gradlew <task> -Psnag.serverTarget=dev
```

## Releases

Frontend and backend release independently, each with its own tag prefix:

- **Frontend** — Android / iOS / desktop / web — uses `fe-vX.Y.Z` tags.
- **Backend** — Ktor server — uses `be-vX.Y.Z` tags.

To cut a release, tag and push:

```shell
git tag fe-v1.2.0   # or be-v1.2.0
git push origin fe-v1.2.0
```

GitHub Actions then builds the matching artifacts and publishes a GitHub Release. Downloads live on the [Releases](https://github.com/timotejAdamec/Snag/releases) page.

## Project structure

See [Project Structure](docs/project_structure.md).

## Affiliation

<img src="https://fit.cvut.cz/static/images/fit-cvut-logo-en.svg" alt="FIT CTU logo" height="200">

This software was developed with the support of the **Faculty of Information Technology, Czech Technical University in Prague**. More at [fit.cvut.cz](https://fit.cvut.cz).
