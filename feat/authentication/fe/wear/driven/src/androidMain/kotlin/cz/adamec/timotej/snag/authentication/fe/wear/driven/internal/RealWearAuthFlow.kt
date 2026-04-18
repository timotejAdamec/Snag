/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.authentication.fe.wear.driven.internal

import android.content.Context
import android.net.Uri
import androidx.wear.phone.interactions.authentication.CodeChallenge
import androidx.wear.phone.interactions.authentication.CodeVerifier
import androidx.wear.phone.interactions.authentication.OAuthRequest
import androidx.wear.phone.interactions.authentication.OAuthResponse
import androidx.wear.phone.interactions.authentication.RemoteAuthClient
import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.configuration.common.RunConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.types.Jwt
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalOpenIdConnect::class)
internal class RealWearAuthFlow(
    private val context: Context,
    private val httpClient: HttpClient,
) : WearAuthFlow {
    override suspend fun runLoginFlow(): WearLoginResult {
        val codeVerifier = CodeVerifier()
        val codeChallenge = CodeChallenge(codeVerifier)

        val authUri =
            Uri
                .parse(
                    "https://login.microsoftonline.com/${RunConfig.entraIdTenantId}/oauth2/v2.0/authorize",
                ).buildUpon()
                .appendQueryParameter(
                    "scope",
                    "openid profile email offline_access api://${RunConfig.entraIdClientId}/access_as_user",
                ).appendQueryParameter("response_type", "code")
                .build()

        val oauthRequest =
            OAuthRequest
                .Builder(context)
                .setAuthProviderUrl(authUri)
                .setClientId(RunConfig.entraIdClientId)
                .setCodeChallenge(codeChallenge)
                .build()
        val redirectUrl = oauthRequest.redirectUrl
        logger.d { "Wear RemoteAuth redirect URL resolved: $redirectUrl" }

        val response = sendAuthorizationRequest(oauthRequest)
        val code =
            response.responseUrl?.getQueryParameter("code")
                ?: error("Missing code in Wear RemoteAuth response")

        val tokenResponse: EntraTokenResponse =
            httpClient
                .submitForm(
                    url = "https://login.microsoftonline.com/${RunConfig.entraIdTenantId}/oauth2/v2.0/token",
                    formParameters =
                        Parameters.build {
                            append("client_id", RunConfig.entraIdClientId)
                            append("grant_type", "authorization_code")
                            append("code", code)
                            append("redirect_uri", redirectUrl)
                            append("code_verifier", codeVerifier.value)
                        },
                ).body()

        val idToken =
            tokenResponse.idToken
                ?: error("Missing id_token in Wear token exchange response")
        val authProviderId =
            extractOidFromIdToken(idToken)
                ?: error("Missing oid claim in Wear id_token")

        return WearLoginResult(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            idToken = tokenResponse.idToken,
            authProviderId = authProviderId,
        )
    }

    private suspend fun sendAuthorizationRequest(request: OAuthRequest): OAuthResponse =
        suspendCancellableCoroutine { cont ->
            val client = RemoteAuthClient.create(context)
            cont.invokeOnCancellation { client.close() }
            client.sendAuthorizationRequest(
                request = request,
                executor = { command -> command.run() },
                clientCallback =
                    object : RemoteAuthClient.Callback() {
                        override fun onAuthorizationResponse(
                            request: OAuthRequest,
                            response: OAuthResponse,
                        ) {
                            client.close()
                            cont.resume(response)
                        }

                        override fun onAuthorizationError(
                            request: OAuthRequest,
                            errorCode: Int,
                        ) {
                            client.close()
                            cont.resumeWithException(
                                IllegalStateException("Wear RemoteAuth failed with errorCode=$errorCode"),
                            )
                        }
                    },
            )
        }

    private fun extractOidFromIdToken(idToken: String): String? {
        val jwt = Jwt.parse(idToken)
        return jwt.payload.additionalClaims["oid"] as? String
    }

    private companion object {
        val logger: Logger = Logger.withTag("RealWearAuthFlow")
    }
}

@Serializable
internal data class EntraTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Int? = null,
    val scope: String? = null,
)
