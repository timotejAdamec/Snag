# EntraID Authentication Setup

This document describes how to configure Microsoft EntraID authentication for production deployment.

---

## Architecture Overview

The system uses a dual-mode authentication architecture:

- **Mock mode** (`MOCK_AUTH=true`, default): Uses `X-User-Id` header on BE and a hardcoded user ID on FE. No external dependencies. Used for local development and testing.
- **Production mode** (`MOCK_AUTH=false`): BE validates EntraID JWT tokens via Ktor's `Authentication` plugin with `jwt {}` provider; FE uses `kotlin-multiplatform-oidc` for OAuth2 Authorization Code + PKCE flow with persistent token storage and automatic token refresh via `oidcBearer` Ktor integration.

---

## 1. Azure Portal — App Registration

1. Go to [Azure Portal → App registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade).
2. Click **New registration**.
3. Set:
   - **Name**: `Snag`
   - **Supported account types**: Accounts in this organizational directory only (single tenant)
   - **Redirect URI**: `snag://auth/callback` (custom URI scheme for mobile/desktop)
4. After creation, note:
   - **Application (client) ID** → this is `ENTRA_ID_CLIENT_ID`
   - **Directory (tenant) ID** → this is `ENTRA_ID_TENANT_ID`
5. Under **API permissions**, ensure `User.Read` (Microsoft Graph) is granted.
6. Under **Expose an API**, set the Application ID URI (e.g., `api://<client-id>`).

### Platform-specific redirect URI setup

- **Android**: `manifestPlaceholders["oidcRedirectScheme"] = "snag"` is set in `androidApp/build.gradle.kts`. The library's manifest merger handles the intent filter automatically.
- **iOS**: URL scheme `snag` is registered in `iosApp/iosApp/Info.plist` via `CFBundleURLSchemes`.
- **Desktop**: The library uses an embedded localhost webserver — no scheme registration needed.
- **Web (WasmJS)**: Redirect to same origin.

---

## 2. Backend Configuration

Auth configuration is set at build time via `CommonConfiguration` (BuildKonfig). Set the following in `config/release.properties`:

```properties
snag.mockAuth=false
snag.entraIdTenantId=<your-tenant-id>
snag.entraIdClientId=<your-client-id>
snag.entraIdRedirectUri=snag://auth/callback
```

### How it works

1. `CallCurrentUserConfiguration` installs Ktor's `Authentication` plugin with a `jwt {}` provider that verifies tokens against the EntraID JWKS endpoint (`https://login.microsoftonline.com/{tenantId}/discovery/v2.0/keys`) with key caching.
2. Every request must include `Authorization: Bearer <jwt-token>`.
3. The JWT's signature (RSA256), issuer, audience, and expiry are verified.
4. `CallCurrentUserPlugin` extracts the `oid` claim from the validated `JWTPrincipal` and resolves the user via `GetOrCreateUserByEntraIdUseCase`.
5. If no user exists with that `oid`, one is auto-created with `role=null`.
6. Requests without a valid token receive no `CallCurrentUser` in the call context — any route calling `currentUser()` throws `UnauthenticatedException` → HTTP 401.

### Key files

- `lib/configuration/common/api/.../CommonConfiguration.kt` — build-time config (shared BE/FE)
- `feat/authentication/be/driving/impl/.../CallCurrentUserConfiguration.kt` — Ktor Authentication plugin setup
- `feat/authentication/be/driving/impl/.../CallCurrentUserPlugin.kt` — dual-mode user resolution
- `feat/users/be/app/api/.../GetOrCreateUserByEntraIdUseCase.kt` — user lookup/creation
- `feat/users/be/driving/impl/.../UsersRoute.kt` — includes `GET /users/me` for post-login user resolution

---

## 3. Frontend Configuration

### 3a. Build config

Same as backend — set in `config/release.properties` (see section 2). Values are compiled into `CommonConfiguration` via BuildKonfig and available on all platforms (Android, iOS, JVM, JS, WasmJS).

### How it works

1. `LoginUseCase` coordinates the sign-in flow:
   a. Triggers OIDC Authorization Code + PKCE flow via `AuthTokenProvider.login()` (browser-based authentication).
   b. After tokens are obtained and stored, calls `GET /users/me` via `AuthenticationApi` to resolve the app user UUID.
   c. Sets `AuthState.Authenticated(userId)` on the token provider.
2. `AuthenticationGate` composable observes auth state and shows `LoginScreen` or app content.
3. Token refresh is handled automatically by `TokenRefreshHandler` + `oidcBearer` Ktor integration — 401 responses trigger transparent refresh and retry.
4. Tokens are persisted via `TokenStore` (Android `EncryptedSharedPreferences`, iOS Keychain).

### Key files

- `feat/authentication/fe/app/api/` — use case interfaces (`GetAuthenticatedUserIdUseCase`, `LoginUseCase`, `LogoutUseCase`)
- `feat/authentication/fe/app/impl/` — use case implementations (login coordination)
- `feat/authentication/fe/ports/` — port interfaces (`AuthTokenProvider`, `AuthState`, `AuthenticationApi`)
- `feat/authentication/fe/driven/impl/` — adapters (`OidcAuthTokenProvider`, `MockAuthTokenProvider`, `CallCurrentUserConfiguration`, `RealAuthenticationApi`)
- `feat/authentication/fe/driven/test/` — test fakes (`FakeAuthTokenProvider`, `FakeAuthenticationApi`)
- `feat/authentication/fe/driving/impl/` — UI (`AuthenticationGate`, `LoginScreen`, `AuthenticationViewModel`)

---

## 4. Remaining Work for Production

The architecture is fully implemented across all platforms. The following steps are needed for a working `MOCK_AUTH=false` deployment:

1. **Register app in Azure Portal** and obtain tenant/client IDs (section 1 above).
2. **Set config values** in `config/release.properties` (section 2 above).
3. **Provide `TokenStore` and `CodeAuthFlowFactory` in DI** — the `OidcAuthTokenProvider` expects these injected via Koin. Platform-specific implementations (`AndroidEncryptedPreferencesSettingsStore`, `IosKeychainTokenStore`) need to be registered, along with `CodeAuthFlowFactory` for browser flow launching.

---

## 5. Testing

- With `MOCK_AUTH=true` (default): everything works as before — no EntraID dependency.
- All existing tests run in mock mode automatically (`CommonConfiguration.mockAuth` defaults to `true`).
- Backend `CallCurrentUserPluginTest` tests the mock-auth path.
- For testing the JWT path: set `MOCK_AUTH=false` and provide test JWTs signed with known keys.
- `FakeAuthTokenProvider` and `FakeAuthenticationApi` in `feat/authentication/fe/driven/test/` are available for FE unit tests.
