// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import android.app.Activity
import android.content.Context
import circle.programmablewallet.sdk.api.ExecuteEvent
import circle.programmablewallet.sdk.api.SocialProvider
import circle.programmablewallet.sdk.presentation.LayoutProvider
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.presentation.ViewSetterProvider
import kotlinx.coroutines.flow.Flow

/**
 * Thin repository layer that wraps the Programmable Wallet SDK. The async methods
 * return a one-shot [Flow] that emits exactly one [ExecuteOutcome] / [LoginOutcome] /
 * [LogoutOutcome] and then completes. This lets callers `collect { ... }` without
 * needing to manage SDK callbacks directly.
 */
interface WalletRepository {
    /**
     * Broadcast stream of SDK [ExecuteEvent]s. The underlying SDK listener is registered
     * once per process because [WalletRepositoryImpl] is bound as `@Singleton` in
     * `DataModule`; the registration happens in its `init` block. Subscribers receive only
     * events emitted while they are subscribed (no replay to late subscribers, because these
     * events are one-shot UI commands and replaying them would cause duplicate handling such
     * as stale navigation). Consumers only need `.collect { ... }` / standard Flow operators.
     */
    val eventFlow: Flow<ExecuteEvent>

    // Config / setup

    /** Initialize the underlying SDK with the given endpoint and appId. */
    fun initSdk(
        context: Context,
        endpoint: String = "",
        appId: String = "",
        settings: SettingsManagement,
    )

    /** Install a custom [LayoutProvider] used by the SDK's built-in UI. */
    fun setLayoutProvider(provider: LayoutProvider)

    /** Install a custom [ViewSetterProvider] used by the SDK's built-in UI. */
    fun setViewSetterProvider(provider: ViewSetterProvider)

    /** Set a custom User-Agent string on outbound SDK HTTP requests. */
    fun setCustomUserAgent(agent: String)

    /** Configure the pool of security questions the SDK will present. */
    fun setSecurityQuestions(questions: Array<SecurityQuestion>)

    /** Returns the SDK-managed device identifier for this install. */
    fun getDeviceId(context: Context): String

    /** Bring the SDK's in-progress Activity back to the foreground. */
    fun moveTaskToFront(activity: Activity)

    // Async SDK operations

    /**
     * Execute one or more pending challenges. Returns a one-shot [Flow] that emits exactly
     * one [ExecuteOutcome] (Success / Warning / Error) and then completes.
     */
    fun execute(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
        challengeIds: List<String>,
    ): Flow<ExecuteOutcome>

    /**
     * Set up the user's PIN and (optionally) enable biometrics. Returns a one-shot [Flow]
     * that emits exactly one [ExecuteOutcome] and then completes.
     */
    fun setBiometricsPin(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
    ): Flow<ExecuteOutcome>

    /**
     * Perform a social login via the given [provider]. Returns a one-shot [Flow] that emits
     * exactly one [LoginOutcome] and then completes.
     */
    fun performLogin(
        activity: Activity,
        provider: SocialProvider,
        deviceToken: String,
        deviceEncryptionKey: String,
    ): Flow<LoginOutcome>

    /**
     * Perform a social logout via the given [provider]. Returns a one-shot [Flow] that emits
     * exactly one [LogoutOutcome] and then completes.
     */
    fun performLogout(
        activity: Activity,
        provider: SocialProvider,
    ): Flow<LogoutOutcome>

    /**
     * Verify an email OTP. Returns a one-shot [Flow] that emits exactly one [LoginOutcome]
     * and then completes.
     */
    fun verifyOTP(
        activity: Activity,
        otpToken: String,
        deviceToken: String,
        deviceEncryptionKey: String,
    ): Flow<LoginOutcome>
}
