// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.SocialProvider
import com.circle.w3s.sample.wallet.data.WalletRepository
import com.circle.w3s.sample.wallet.data.isTransient
import com.circle.w3s.sample.wallet.data.toLoginUiState
import com.circle.w3s.sample.wallet.data.toLogoutUiState
import com.circle.w3s.sample.wallet.ui.main.LoginUiState
import com.circle.w3s.sample.wallet.ui.main.LogoutUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "PerformLoginViewModel"

@HiltViewModel
class PerformLoginViewModel @Inject internal constructor(
    private val repo: WalletRepository,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)
    val logoutState: StateFlow<LogoutUiState> = _logoutState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val verifyOtpState: StateFlow<LoginUiState> = _verifyOtpState.asStateFlow()

    fun getDeviceId(context: Context): String = repo.getDeviceId(context)
    fun moveTaskToFront(activity: Activity) = repo.moveTaskToFront(activity)

    fun login(
        activity: Activity, provider: SocialProvider,
        deviceToken: String, deviceEncryptionKey: String,
    ) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                repo.performLogin(activity, provider, deviceToken, deviceEncryptionKey).collect {
                    _loginState.value = it.toLoginUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "performLogin failed", e)
                _loginState.value = LoginUiState.Error(e)
            }
        }
    }

    fun logout(activity: Activity, provider: SocialProvider) {
        viewModelScope.launch {
            _logoutState.value = LogoutUiState.Loading
            try {
                repo.performLogout(activity, provider).collect {
                    _logoutState.value = it.toLogoutUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "performLogout failed", e)
                _logoutState.value = LogoutUiState.Error(e)
            }
        }
    }

    fun verifyOtp(
        activity: Activity, otpToken: String,
        deviceToken: String, deviceEncryptionKey: String,
    ) {
        viewModelScope.launch {
            _verifyOtpState.value = LoginUiState.Loading
            try {
                repo.verifyOTP(activity, otpToken, deviceToken, deviceEncryptionKey).collect {
                    _verifyOtpState.value = it.toLoginUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "verifyOTP failed", e)
                _verifyOtpState.value = LoginUiState.Error(e)
            }
        }
    }

    fun resetLoginState() { _loginState.value = LoginUiState.Idle }
    fun resetLogoutState() { _logoutState.value = LogoutUiState.Idle }
    fun resetVerifyOtpState() { _verifyOtpState.value = LoginUiState.Idle }

    /**
     * Pure function: classify a login or logout error so the Activity's dispatch is a
     * mechanical `when` on the closed [AuthErrorClassification] sum type. See its KDoc
     * for the variant-to-UI-behavior mapping.
     *
     * Named "Auth" (not "Login") because both the login and logout state-handlers consume
     * this via `PerformLoginActivity.routeLoginOrLogoutError`.
     */
    fun classifyAuthError(err: Throwable): AuthErrorClassification = when {
        err !is ApiError -> AuthErrorClassification.Unknown(err.message)
        err.code.isTransient -> AuthErrorClassification.Transient(err.code, err.message)
        else -> AuthErrorClassification.Other(err.code, err.message)
    }
}
