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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Suppress("ReturnCount")
internal class MsalAuthTokenProvider(
    private val context: Context,
    private val clientId: String,
) : AuthTokenProvider {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState

    private val scopeDefault: String
        get() = "api://$clientId/.default"

    @Volatile
    private var msalApp: ISingleAccountPublicClientApplication? = null

    suspend fun initialize() {
        msalApp =
            suspendCancellableCoroutine { continuation ->
                PublicClientApplication.createSingleAccountPublicClientApplication(
                    context,
                    getAuthConfigResourceId(),
                    object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                        override fun onCreated(application: ISingleAccountPublicClientApplication) {
                            continuation.resume(application)
                        }

                        override fun onError(exception: MsalException) {
                            continuation.resume(null)
                        }
                    },
                )
            }

        val account = msalApp?.currentAccount?.currentAccount
        if (account != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    override suspend fun getAccessToken(): String? {
        val app = msalApp ?: return null
        val account = app.currentAccount?.currentAccount ?: return null

        return runCatching {
            val params =
                AcquireTokenSilentParameters
                    .Builder()
                    .forAccount(account)
                    .fromAuthority(account.authority)
                    .withScopes(listOf(scopeDefault))
                    .build()
            app.acquireTokenSilent(params).accessToken
        }.getOrNull()
    }

    @Suppress("LabeledExpression")
    suspend fun signIn(activity: Activity): Boolean =
        suspendCancellableCoroutine { continuation ->
            val app = msalApp
            if (app == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            val params =
                SignInParameters
                    .builder()
                    .withActivity(activity)
                    .withScopes(listOf(scopeDefault))
                    .withCallback(
                        object : AuthenticationCallback {
                            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                                _authState.value = AuthState.Authenticated
                                continuation.resume(true)
                            }

                            override fun onError(exception: MsalException) {
                                continuation.resume(false)
                            }

                            override fun onCancel() {
                                continuation.resume(false)
                            }
                        },
                    ).build()

            app.signIn(params)
        }

    override suspend fun signOut() {
        msalApp?.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    private fun getAuthConfigResourceId(): Int =
        context.resources.getIdentifier(
            "auth_config",
            "raw",
            context.packageName,
        )
}
