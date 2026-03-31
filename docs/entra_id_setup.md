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

1. `CurrentUserConfiguration` installs Ktor's `Authentication` plugin with a dual-mode provider:
   - **Production** (`mockAuth=false`): `jwt {}` provider verifies tokens against the EntraID JWKS endpoint (`https://login.microsoftonline.com/{tenantId}/discovery/v2.0/keys`) with key caching. The `validate` callback extracts the `oid` claim, resolves/creates the user, and returns a `SnagPrincipal`.
   - **Mock** (`mockAuth=true`): `MockHeaderAuthProvider` reads the `X-Snag-User-Id` header and resolves the user from the database.
2. All feature routes are wrapped in `authenticate { }`.
3. Route handlers call `currentUser()` which reads from `call.principal<SnagPrincipal>()`. If no principal is present, `UnauthenticatedException` is thrown → HTTP 401.
4. If no user exists with the `oid` from the JWT, one is auto-created with `role=null`.

### Key files

- `lib/configuration/common/api/.../CommonConfiguration.kt` — build-time config (shared BE/FE)
- `feat/authentication/be/driving/impl/.../CurrentUserConfiguration.kt` — Ktor Authentication plugin setup with dual-mode providers
- `feat/authentication/be/driving/impl/.../MockHeaderAuthProvider.kt` — mock auth provider for testing
- `feat/authentication/be/driving/api/.../SnagPrincipal.kt` — principal carrying `CurrentUser`
- `feat/users/be/app/api/.../GetOrCreateUserByAuthProviderIdUseCase.kt` — user lookup/creation

---

## 3. Frontend Configuration

### 3a. Build config

Same as backend — set in `config/release.properties` (see section 2). Values are compiled into `CommonConfiguration` via BuildKonfig and available on all platforms (Android, iOS, JVM, JS, WasmJS).

### How it works

1. On app start, `AuthenticationInitializer` restores the session from persisted tokens. If a valid ID token exists, `authProviderId` is extracted from its `oid` claim and auth state is set to `Authenticated`.
2. If unauthenticated, `AuthenticationViewModel` automatically triggers `LoginUseCase`, which starts the OIDC Authorization Code + PKCE flow via `AuthTokenProvider.login()`. The adapter extracts `authProviderId` from the ID token after successful login.
3. `AuthenticationGate` composable (in `driving/api`) observes auth state and shows app content when authenticated, or a login screen with error/retry on failure.
4. The authentication feature is user-agnostic — it only knows about `authProviderId` (the `oid` from the JWT). The users feature resolves `authProviderId` → app user via `GetCurrentUserFlowUseCaseImpl` querying the local DB by `authProviderId`.
5. Token refresh is handled automatically by `TokenRefreshHandler` + `oidcBearer` Ktor integration — 401 responses trigger transparent refresh and retry.
6. Tokens are persisted via `TokenStore` (Android `EncryptedSharedPreferences`, iOS Keychain).

### Key files

- `feat/authentication/fe/app/api/` — use case interfaces (`GetAuthProviderIdUseCase`, `GetAccessTokenUseCase`, `LoginUseCase`, `LogoutUseCase`, `RestoreSessionUseCase`)
- `feat/authentication/fe/app/impl/` — use case implementations
- `feat/authentication/fe/ports/` — port interfaces (`AuthTokenProvider`, `AuthState`)
- `feat/authentication/fe/driven/impl/` — adapters (`OidcAuthTokenProvider`, `MockAuthTokenProvider`)
- `feat/authentication/fe/driven/test/` — test fakes (`FakeAuthTokenProvider`)
- `feat/authentication/fe/driving/api/` — public composable (`AuthenticationGate`, `AuthenticationGateContent`)
- `feat/authentication/fe/driving/impl/` — internal UI (`LoginScreen`, `AuthenticationViewModel`, `CurrentUserHttpClientConfiguration`, `AuthenticationInitializer`)

---

## 4. Remaining Work for Production

The architecture is fully implemented across all platforms. The following steps are needed for a working `MOCK_AUTH=false` deployment:

1. **Register app in Azure Portal** and obtain tenant/client IDs (section 1 above).
2. **Set config values** in `config/release.properties` (section 2 above).
3. **Provide `TokenStore` and `CodeAuthFlowFactory` in DI** — the `OidcAuthTokenProvider` expects these injected via Koin. Platform-specific implementations (`AndroidEncryptedPreferencesSettingsStore`, `IosKeychainTokenStore`) need to be registered, along with `CodeAuthFlowFactory` for browser flow launching.

---

## 5. First User Bootstrap

In a fresh deployment, no users have roles. Since only an administrator can assign roles, the system automatically assigns the `ADMINISTRATOR` role to the very first user who logs in. This only triggers when the users table is empty — subsequent logins create users with `role = null` as usual.

No configuration is needed — just deploy and the first person to log in becomes admin.

See [#175](https://github.com/timotejAdamec/Snag/issues/175) for implementation status.

---

## 6. Testing

- With `MOCK_AUTH=true` (default): everything works as before — no EntraID dependency.
- All existing tests run in mock mode automatically (`CommonConfiguration.mockAuth` defaults to `true`).
- Backend `MockHeaderAuthProviderTest` tests the mock-auth path.
- For testing the JWT path: set `MOCK_AUTH=false` and provide test JWTs signed with known keys.
- `FakeAuthTokenProvider` in `feat/authentication/fe/driven/test/` is available for FE unit tests.
