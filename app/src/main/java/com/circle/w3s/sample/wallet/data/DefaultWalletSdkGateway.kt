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

// Why the UNCHECKED_CAST suppressions below are safe:
// The SDK exposes nullable-element arrays via Java platform types (Array<T?>). We only pass
// non-null arrays, and both types erase to the same JVM array class, so the cast is a no-op
// at runtime. We never write null into the array after casting.
internal object DefaultWalletSdkGateway : WalletSdkGateway {
    @Suppress("UNCHECKED_CAST") // See file-level note.
    override fun execute(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
        challengeIds: Array<String>,
        callback: Callback<ExecuteResult>,
    ) = WalletSdk.execute(
        activity,
        userToken,
        encryptionKey,
        challengeIds as Array<String?>,
        callback,
    )

    override fun setBiometricsPin(
        activity: Activity,
        userToken: String,
        encryptionKey: String,
        callback: Callback<ExecuteResult>,
    ) = WalletSdk.setBiometricsPin(activity, userToken, encryptionKey, callback)

    override fun performLogin(
        activity: Activity,
        provider: SocialProvider,
        deviceToken: String,
        deviceEncryptionKey: String,
        callback: SocialCallback<LoginResult>,
    ) = WalletSdk.performLogin(activity, provider, deviceToken, deviceEncryptionKey, callback)

    override fun performLogout(
        activity: Activity,
        provider: SocialProvider,
        callback: LogoutCallback,
    ) = WalletSdk.performLogout(activity, provider, callback)

    override fun verifyOTP(
        activity: Activity,
        otpToken: String,
        deviceToken: String,
        deviceEncryptionKey: String,
        callback: Callback2<LoginResult>,
    ) = WalletSdk.verifyOTP(activity, otpToken, deviceToken, deviceEncryptionKey, callback)

    override fun getDeviceId(context: Context): String = WalletSdk.getDeviceId(context).orEmpty()
    override fun init(context: Context, configuration: WalletSdk.Configuration) =
        WalletSdk.init(context, configuration)
    override fun addEventListener(listener: EventListener) = WalletSdk.addEventListener(listener)
    override fun setLayoutProvider(provider: LayoutProvider) = WalletSdk.setLayoutProvider(provider)
    override fun setViewSetterProvider(provider: ViewSetterProvider) =
        WalletSdk.setViewSetterProvider(provider)
    override fun setCustomUserAgent(agent: String) = WalletSdk.setCustomUserAgent(agent)

    @Suppress("UNCHECKED_CAST") // See file-level note.
    override fun setSecurityQuestions(questions: Array<SecurityQuestion>) =
        WalletSdk.setSecurityQuestions(questions as Array<SecurityQuestion?>)
    override fun moveTaskToFront(activity: Activity) = WalletSdk.moveTaskToFront(activity)
}
