# EntraID Authentication Setup

This document describes how to configure Microsoft EntraID authentication for production deployment.

---

## Architecture Overview

The system uses a dual-mode authentication architecture:

- **Mock mode** (`MOCK_AUTH=true`, default): Uses `X-User-Id` header on BE and a hardcoded user ID on FE. No external dependencies. Used for local development and testing.
- **Production mode** (`MOCK_AUTH=false`): BE validates EntraID JWT tokens via Ktor's `Authentication` plugin with `jwt {}` provider; FE uses `ktor-client-auth` Bearer plugin for token injection with `OAuthTokenProvider` for cross-platform OAuth2/OIDC.

---

## 1. Azure Portal — App Registration

1. Go to [Azure Portal → App registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade).
2. Click **New registration**.
3. Set:
   - **Name**: `Snag`
   - **Supported account types**: Accounts in this organizational directory only (single tenant)
   - **Redirect URI**: configure per platform (browser redirect for web/desktop, deep link for mobile)
4. After creation, note:
   - **Application (client) ID** → this is `ENTRA_ID_CLIENT_ID`
   - **Directory (tenant) ID** → this is `ENTRA_ID_TENANT_ID`
5. Under **API permissions**, ensure `User.Read` (Microsoft Graph) is granted.
6. Under **Expose an API**, set the Application ID URI (e.g., `api://<client-id>`).

---

## 2. Backend Configuration

Auth configuration is set at build time via `CommonConfiguration` (BuildKonfig). Set the following in `config/release.properties`:

```properties
snag.mockAuth=false
snag.entraIdTenantId=<your-tenant-id>
snag.entraIdClientId=<your-client-id>
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

---

## 3. Frontend Configuration

### 3a. Build config

Same as backend — set in `config/release.properties` (see section 2). Values are compiled into `CommonConfiguration` via BuildKonfig and available on all platforms (Android, iOS, JVM, JS, WasmJS).

### Key files

- `feat/authentication/fe/app/api/.../AuthenticatedUserProvider.kt` — cross-feature interface for current user identity
- `feat/authentication/fe/driving/impl/.../AuthTokenProvider.kt` — internal interface for token management
- `feat/authentication/fe/driving/impl/.../MockAuthTokenProvider.kt` — dev mode (hardcoded user)
- `feat/authentication/fe/driving/impl/.../OAuthTokenProvider.kt` — production mode (OAuth2 token storage)
- `feat/authentication/fe/driving/impl/.../CallCurrentUserConfiguration.kt` — HTTP client auth (Bearer via `ktor-client-auth` or mock header)
- `feat/authentication/fe/driving/api/.../AuthenticationGate.kt` — login gate composable

---

## 4. Remaining Work for Production

The architecture is fully implemented across all platforms. The following steps are needed for a working `MOCK_AUTH=false` deployment:

1. **Register app in Azure Portal** and obtain tenant/client IDs (section 1 above).
2. **Set config values** in `config/release.properties` (section 2 above).
3. **Implement OAuth2 browser flow in `OAuthTokenProvider`** — open browser to EntraID authorize endpoint, handle redirect with auth code, exchange for tokens. The `OAuthTokenProvider` class is in place with token storage; the interactive sign-in flow needs platform-specific browser launching.
4. **Resolve `AuthenticatedUserProvider` user ID after login** — after OAuth2 login + first sync, resolve the current user's UUID from the local DB by matching the `oid` JWT claim to `entraId` in the users table.
5. **Connect `LoginScreen` sign-in button** to `OAuthTokenProvider`'s sign-in flow.

---

## 5. Testing

- With `MOCK_AUTH=true` (default): everything works as before — no EntraID dependency.
- All existing tests run in mock mode automatically (`CommonConfiguration.mockAuth` defaults to `true`).
- Backend `CallCurrentUserPluginTest` tests the mock-auth path.
- For testing the JWT path: set `MOCK_AUTH=false` and provide test JWTs signed with known keys.
