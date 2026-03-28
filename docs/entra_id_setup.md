# EntraID Authentication Setup

This document describes how to configure Microsoft EntraID authentication for production deployment.

---

## Architecture Overview

The system uses a dual-mode authentication architecture:

- **Mock mode** (`MOCK_AUTH=true`, default): Uses `X-User-Id` header on BE and a hardcoded user ID on FE. No external dependencies. Used for local development and testing.
- **Production mode** (`MOCK_AUTH=false`): BE validates EntraID JWT tokens; FE uses MSAL for OAuth2/OIDC login flow.

---

## 1. Azure Portal — App Registration

1. Go to [Azure Portal → App registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade).
2. Click **New registration**.
3. Set:
   - **Name**: `Snag`
   - **Supported account types**: Accounts in this organizational directory only (single tenant)
   - **Redirect URI**: Select **Public client/native** and set `msauth://<package-name>/<signature-hash>` (see step 5)
4. After creation, note:
   - **Application (client) ID** → this is `ENTRA_ID_CLIENT_ID`
   - **Directory (tenant) ID** → this is `ENTRA_ID_TENANT_ID`
5. Under **Authentication → Platform configurations → Add a platform → Mobile and desktop applications**, add the redirect URI for Android MSAL.
6. Under **API permissions**, ensure `User.Read` (Microsoft Graph) is granted.
7. Under **Expose an API**, set the Application ID URI (e.g., `api://<client-id>`) — this is used for the MSAL scope (`api://<client-id>/.default`).

---

## 2. Backend Configuration

Set the following environment variables on the server:

```bash
MOCK_AUTH=false
ENTRA_ID_TENANT_ID=<your-tenant-id>
ENTRA_ID_CLIENT_ID=<your-client-id>
```

These are read by `SnagConfig` at startup. When `MOCK_AUTH=false`, the `CallCurrentUserPlugin` validates JWT tokens via `EntraIdJwtVerifier` against the EntraID JWKS endpoint.

### How it works

1. Every request must include `Authorization: Bearer <jwt-token>`.
2. `EntraIdJwtVerifier` validates the token's signature (RSA256 via JWKS), issuer, audience, and expiry.
3. The `oid` claim (EntraID Object ID) is extracted and matched to a user via `UsersDb.getUserByEntraId()`.
4. If no user exists with that `oid`, one is auto-created with `role=null`.
5. Requests without a valid token receive no `CallCurrentUser` in the call context — any route calling `currentUser()` throws `UnauthenticatedException` → HTTP 401.

### Key files

- `lib/configuration/be/api/.../SnagConfig.kt` — reads env vars
- `feat/authentication/be/driving/impl/.../CallCurrentUserPlugin.kt` — dual-mode plugin
- `feat/authentication/be/driving/impl/.../EntraIdJwtVerifier.kt` — JWT validation

---

## 3. Frontend Configuration (Android)

### 3a. Build config

In `config/release.properties`:

```properties
snag.mockAuth=false
snag.entraIdTenantId=<your-tenant-id>
snag.entraIdClientId=<your-client-id>
```

These are compiled into the app via BuildKonfig and available in `CommonConfiguration`.

### 3b. MSAL configuration file

Create `composeApp/src/androidMain/res/raw/auth_config.json`:

```json
{
  "client_id": "<your-client-id>",
  "authorization_user_agent": "DEFAULT",
  "redirect_uri": "msauth://<package-name>/<signature-hash>",
  "broker_redirect_uri_registered": false,
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "AzureADMyOrg",
        "tenant_id": "<your-tenant-id>"
      }
    }
  ]
}
```

### 3c. Android manifest

Add to `androidApp/src/main/AndroidManifest.xml` inside `<application>`:

```xml
<activity android:name="com.microsoft.identity.client.BrowserTabActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:host="<package-name>"
            android:path="/<signature-hash>"
            android:scheme="msauth" />
    </intent-filter>
</activity>
```

### Key files

- `config/debug.properties`, `config/release.properties` — build profiles
- `lib/configuration/common/api/.../CommonConfiguration.kt` — exposes build config
- `feat/authentication/fe/driving/impl/.../AuthTokenProvider.kt` — interface
- `feat/authentication/fe/driving/impl/.../MockAuthTokenProvider.kt` — dev mode
- `feat/authentication/fe/driving/impl/.../MsalAuthTokenProvider.kt` — Android MSAL
- `feat/authentication/fe/driving/impl/.../CallCurrentUserConfiguration.kt` — HTTP header injection
- `core/foundation/fe/.../CurrentUserIdStore.kt` — current user identity
- `composeApp/.../App.kt` — login gate (`AuthenticationGate`)
- `composeApp/.../LoginScreen.kt` — login UI

---

## 4. Remaining Wiring for Production

The architecture and all code paths are implemented. The following deployment tasks remain before `MOCK_AUTH=false` works end-to-end:

1. **Register app in Azure Portal** and obtain tenant/client IDs (section 1 above).
2. **Add `auth_config.json`** to Android resources (section 3b above).
3. **Add MSAL redirect activity** to Android manifest (section 3c above).
4. **Wire `MsalAuthTokenProvider` into Koin DI** — override the `AuthTokenProvider` binding for Android when `mockAuth=false`. This requires either:
   - Adding an Android-specific Koin module in the `androidApp` module, or
   - Making `AuthTokenProvider` public and providing the override in `composeApp`'s platform module.
5. **Populate `CurrentUserIdStore` after MSAL login** — after the user signs in via MSAL and the first sync completes, resolve the current user's UUID from the local DB (by matching the `oid` JWT claim to `entraId` in the users table) and call `currentUserIdStore.set(userId)`.
6. **Connect `LoginScreen.onSignIn`** to `MsalAuthTokenProvider.signIn(activity)` — requires passing the current Android `Activity` to the sign-in flow.

---

## 5. Testing

- With `MOCK_AUTH=true` (default): everything works as before — no EntraID dependency.
- All existing tests run in mock mode automatically (`SnagConfig.mockAuth` defaults to `true` when `MOCK_AUTH` env var is unset).
- Backend `CallCurrentUserPluginTest` tests the mock-auth path.
- For testing the JWT path: set `MOCK_AUTH=false` and provide test JWTs signed with known keys.
