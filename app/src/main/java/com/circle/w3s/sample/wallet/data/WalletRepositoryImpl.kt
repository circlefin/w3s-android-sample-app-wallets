// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import android.app.Activity
import android.content.Context
import android.util.Log
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.api.Callback2
import circle.programmablewallet.sdk.api.ExecuteEvent
import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.api.LogoutCallback
import circle.programmablewallet.sdk.api.SocialCallback
import circle.programmablewallet.sdk.api.SocialProvider
import circle.programmablewallet.sdk.presentation.EventListener
import circle.programmablewallet.sdk.presentation.LayoutProvider
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.presentation.ViewSetterProvider
import circle.programmablewallet.sdk.result.ExecuteResult
import circle.programmablewallet.sdk.result.LoginResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "WalletRepository"

// Note on `awaitClose { ... }` blocks below:
// awaitClose is required by callbackFlow's contract to keep the flow alive until a terminal
// callback closes it. The SDK's one-shot operation callbacks (Callback, Callback2,
// SocialCallback, LogoutCallback) have no deregister API — distinct from EventListener, which
// does — so we rely on close() being called from each terminal callback path (onError /
// onResult / onWarning). Any second SDK callback that arrives after close() is silently
// dropped by the channel; emitOrLog logs those drops.

/**
 * Emit [value] and log a warning if the channel has already been closed (the send is silently
 * dropped otherwise). Happens when the SDK fires a second callback after a terminal one, since
 * we have no deregister API and close() runs from each terminal branch.
 */
private fun <T> ProducerScope<T>.emitOrLog(value: T, site: String) {
    val result = trySend(value)
    if (result.isFailure) Log.w(TAG, "$site: dropped outcome after close: $value")
}

class WalletRepositoryImpl internal constructor(
    private val sdk: WalletSdkGateway,
) : WalletRepository {

    private val _eventFlow = MutableSharedFlow<ExecuteEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    // ExecuteEvent represents transient one-shot SDK commands (forgotPin, resendOtp) that
    // drive navigation. replay=0 prevents a late subscriber (e.g. a VM constructed after
    // Activity recreation) from receiving an old event and re-navigating; the "cold-start
    // race" is not a real concern in this app because SDK events fire only from user clicks
    // in foreground SDK UI, which the user can only reach after MainFragment/MainViewModel
    // is created and the forwarder is already subscribed.
    //
    // Overflow policy is SUSPEND (not DROP_OLDEST) to signal command-stream intent: every
    // event must land with the current subscribers. DROP_OLDEST would silently discard the
    // oldest queued command to make room for a newer one, which is wrong semantics for
    // navigation. extraBufferCapacity=64 is sized for absurd worst-case bursts at human
    // click rate (~1/s); tryEmit below is explicitly non-blocking and surfaces any overflow
    // as a loud log rather than a hidden drop.
    override val eventFlow: Flow<ExecuteEvent> = _eventFlow.asSharedFlow()

    init {
        sdk.addEventListener(object : EventListener {
            override fun onEvent(event: ExecuteEvent) {
                // Best-effort emission for current subscribers only. tryEmit returns false
                // if the buffer is full (all 64 slots occupied with unread events for at
                // least one subscriber) — unreachable under human-rate events with a prompt
                // VM forwarder, but logged loudly so a regression surfaces immediately.
                val emitted = _eventFlow.tryEmit(event)
                if (!emitted) Log.e(TAG, "eventFlow full; dropped SDK event: $event")
            }
        })
    }

    override fun initSdk(
        context: Context,
        endpoint: String,
        appId: String,
        settings: SettingsManagement,
    ) {
        sdk.init(context, WalletSdk.Configuration(endpoint, appId, settings))
    }

    override fun setLayoutProvider(provider: LayoutProvider) = sdk.setLayoutProvider(provider)
    override fun setViewSetterProvider(provider: ViewSetterProvider) =
        sdk.setViewSetterProvider(provider)
    override fun setCustomUserAgent(agent: String) = sdk.setCustomUserAgent(agent)
    override fun setSecurityQuestions(questions: Array<SecurityQuestion>) =
        sdk.setSecurityQuestions(questions)
    override fun getDeviceId(context: Context): String = sdk.getDeviceId(context)
    override fun moveTaskToFront(activity: Activity) = sdk.moveTaskToFront(activity)

    override fun execute(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
        challengeIds: List<String>,
    ): Flow<ExecuteOutcome> = callbackFlow {
        val callback = object : Callback<ExecuteResult> {
            override fun onError(error: Throwable): Boolean {
                emitOrLog(ExecuteOutcome.Error(error), "execute")
                close()
                return shouldAppHandle(error)
            }

            override fun onWarning(warning: ExecuteWarning, result: ExecuteResult?): Boolean {
                emitOrLog(ExecuteOutcome.Warning(warning, result), "execute")
                close()
                return false
            }

            override fun onResult(result: ExecuteResult) {
                emitOrLog(ExecuteOutcome.Success(result), "execute")
                close()
            }
        }
        sdk.execute(activity, userToken, encryptionKey, challengeIds.toTypedArray(), callback)
        awaitClose { Log.d(TAG, "execute flow closed") } // See file-level awaitClose note.
    }.buffer(Channel.BUFFERED)

    override fun setBiometricsPin(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
    ): Flow<ExecuteOutcome> = callbackFlow {
        val callback = object : Callback<ExecuteResult> {
            override fun onError(error: Throwable): Boolean {
                emitOrLog(ExecuteOutcome.Error(error), "setBiometricsPin")
                close()
                return shouldAppHandle(error)
            }

            override fun onWarning(warning: ExecuteWarning, result: ExecuteResult?): Boolean {
                emitOrLog(ExecuteOutcome.Warning(warning, result), "setBiometricsPin")
                close()
                return false
            }

            override fun onResult(result: ExecuteResult) {
                emitOrLog(ExecuteOutcome.Success(result), "setBiometricsPin")
                close()
            }
        }
        sdk.setBiometricsPin(activity, userToken, encryptionKey, callback)
        awaitClose { Log.d(TAG, "setBiometricsPin flow closed") } // See file-level awaitClose note.
    }.buffer(Channel.BUFFERED)

    override fun performLogin(
        activity: Activity,
        provider: SocialProvider,
        deviceToken: String,
        deviceEncryptionKey: String,
    ): Flow<LoginOutcome> = callbackFlow {
        val callback = object : SocialCallback<LoginResult> {
            override fun onError(error: Throwable) {
                emitOrLog(LoginOutcome.Error(error), "performLogin")
                close()
            }

            override fun onResult(result: LoginResult) {
                emitOrLog(LoginOutcome.Success(result), "performLogin")
                close()
            }
        }
        sdk.performLogin(activity, provider, deviceToken, deviceEncryptionKey, callback)
        awaitClose { Log.d(TAG, "performLogin flow closed") } // See file-level awaitClose note.
    }.buffer(Channel.BUFFERED)

    override fun performLogout(
        activity: Activity,
        provider: SocialProvider,
    ): Flow<LogoutOutcome> = callbackFlow {
        val callback = object : LogoutCallback {
            override fun onError(error: Throwable) {
                emitOrLog(LogoutOutcome.Error(error), "performLogout")
                close()
            }

            override fun onComplete() {
                emitOrLog(LogoutOutcome.Success, "performLogout")
                close()
            }
        }
        sdk.performLogout(activity, provider, callback)
        awaitClose { Log.d(TAG, "performLogout flow closed") } // See file-level awaitClose note.
    }.buffer(Channel.BUFFERED)

    override fun verifyOTP(
        activity: Activity,
        otpToken: String,
        deviceToken: String,
        deviceEncryptionKey: String,
    ): Flow<LoginOutcome> = callbackFlow {
        val callback = object : Callback2<LoginResult> {
            // Always return false from Callback2.onError to preserve the email tab's behavior:
            // the SDK finishes its OTP Activity on error. shouldAppHandle applies only to
            // Callback (execute/setBiometricsPin) where the app handles retries.
            override fun onError(error: Throwable): Boolean {
                emitOrLog(LoginOutcome.Error(error), "verifyOTP")
                close()
                return false
            }

            override fun onResult(result: LoginResult) {
                emitOrLog(LoginOutcome.Success(result), "verifyOTP")
                close()
            }
        }
        sdk.verifyOTP(activity, otpToken, deviceToken, deviceEncryptionKey, callback)
        awaitClose { Log.d(TAG, "verifyOTP flow closed") } // See file-level awaitClose note.
    }.buffer(Channel.BUFFERED)

    /**
     * Returned to [Callback.onError] (execute / setBiometricsPin) to decide what happens next.
     * `true` means the app will handle the next step (the SDK keeps its Activity alive);
     * `false` lets the SDK finish.
     *
     * Rule: `userCanceled`, `networkError`, and non-[ApiError] throwables return `false`;
     * any other [ApiError] returns `true` so the app can surface the failure and retry.
     *
     * Not used for [Callback2.onError] (verifyOTP), which always returns `false` to match
     * the email tab's behavior.
     */
    internal fun shouldAppHandle(error: Throwable): Boolean = when {
        error !is ApiError -> false
        error.code == ApiError.ErrorCode.userCanceled -> false
        error.code == ApiError.ErrorCode.networkError -> false
        else -> true
    }
}
