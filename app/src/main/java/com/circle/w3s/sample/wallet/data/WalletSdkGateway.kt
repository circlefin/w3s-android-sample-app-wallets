// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import android.app.Activity
import android.content.Context
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.api.Callback2
import circle.programmablewallet.sdk.api.LogoutCallback
import circle.programmablewallet.sdk.api.SocialCallback
import circle.programmablewallet.sdk.api.SocialProvider
import circle.programmablewallet.sdk.presentation.EventListener
import circle.programmablewallet.sdk.presentation.LayoutProvider
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.presentation.ViewSetterProvider
import circle.programmablewallet.sdk.result.ExecuteResult
import circle.programmablewallet.sdk.result.LoginResult

/**
 * Testability seam over [WalletSdk] static calls. Production uses
 * [DefaultWalletSdkGateway]; tests inject a fake.
 */
internal interface WalletSdkGateway {
    fun execute(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
        challengeIds: Array<String>,
        callback: Callback<ExecuteResult>,
    )

    fun setBiometricsPin(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
        callback: Callback<ExecuteResult>,
    )

    fun performLogin(
        activity: Activity,
        provider: SocialProvider,
        deviceToken: String,
        deviceEncryptionKey: String,
        callback: SocialCallback<LoginResult>,
    )

    fun performLogout(
        activity: Activity,
        provider: SocialProvider,
        callback: LogoutCallback,
    )

    fun verifyOTP(
        activity: Activity,
        otpToken: String,
        deviceToken: String,
        deviceEncryptionKey: String,
        callback: Callback2<LoginResult>,
    )

    fun getDeviceId(context: Context): String
    fun init(context: Context, configuration: WalletSdk.Configuration)
    fun addEventListener(listener: EventListener)
    fun setLayoutProvider(provider: LayoutProvider)
    fun setViewSetterProvider(provider: ViewSetterProvider)
    fun setCustomUserAgent(agent: String)
    fun setSecurityQuestions(questions: Array<SecurityQuestion>)
    fun moveTaskToFront(activity: Activity)
}
