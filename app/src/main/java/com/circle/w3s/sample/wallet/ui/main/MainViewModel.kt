// Copyright (c) 2024, Circle Internet Financial, LTD. All rights reserved.
//
// SPDX-License-Identifier: Apache-2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.circle.w3s.sample.wallet.ui.main

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.ExecuteEvent
import circle.programmablewallet.sdk.api.SocialProvider
import circle.programmablewallet.sdk.presentation.LayoutProvider
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.presentation.ViewSetterProvider
import com.circle.w3s.sample.wallet.data.LogoutOutcome
import com.circle.w3s.sample.wallet.data.WalletRepository
import com.circle.w3s.sample.wallet.data.isTransient
import com.circle.w3s.sample.wallet.data.toExecuteUiState
import com.circle.w3s.sample.wallet.data.toLoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject internal constructor(
    private val repo: WalletRepository,
) : ViewModel() {

    // ---- Existing form-validation state ----
    private val _executeForm = MutableStateFlow<ExecuteFormState?>(null)
    val executeFormState: StateFlow<ExecuteFormState?> = _executeForm.asStateFlow()
    private val _naviDirections = MutableStateFlow<NavDirections?>(null)
    val naviDirections: StateFlow<NavDirections?> = _naviDirections.asStateFlow()
    private val _enableBiometrics = MutableStateFlow(false)
    val enableBiometrics: StateFlow<Boolean> = _enableBiometrics.asStateFlow()
    private val _isSetBiometricsPinDataValid = MutableStateFlow(false)
    val isSetBiometricsPinDataValid: StateFlow<Boolean> = _isSetBiometricsPinDataValid.asStateFlow()
    private val _disableConfirmationUI = MutableStateFlow(false)
    val disableConfirmationUI: StateFlow<Boolean> = _disableConfirmationUI.asStateFlow()

    fun executeDataChanged(
        endpoint: String?, appId: String?, userToken: String?, encryptionKey: String?,
        socialUserToken: String?, socialEncryptionKey: String?,
        emailUserToken: String?, emailEncryptionKey: String?, challengeId: String?,
    ) {
        val isSetBiometricsPinInputDataValid = (endpoint?.isNotBlank() ?: false) &&
            (appId?.isNotBlank() ?: false) && (userToken?.isNotBlank() ?: false) &&
            (encryptionKey?.isNotBlank() ?: false)
        _isSetBiometricsPinDataValid.value = isSetBiometricsPinInputDataValid &&
            _enableBiometrics.value
        _executeForm.value = ExecuteFormState(
            isSetBiometricsPinInputDataValid = isSetBiometricsPinInputDataValid,
            isExecuteDataValid = isSetBiometricsPinInputDataValid && (challengeId?.isNotBlank() ?: false),
            endpoint = endpoint, appId = appId, userToken = userToken, encryptionKey = encryptionKey,
            socialUserToken = socialUserToken, socialEncryptionKey = socialEncryptionKey,
            emailUserToken = emailUserToken, emailEncryptionKey = emailEncryptionKey,
            challengeId = challengeId,
        )
    }

    fun setNaviDirections(directions: NavDirections?) { _naviDirections.value = directions }
    fun setEnableBiometrics(value: Boolean) {
        _enableBiometrics.value = value
        _isSetBiometricsPinDataValid.value = value &&
            _executeForm.value?.isSetBiometricsPinInputDataValid == true
    }
    fun setDisableConfirmationUI(value: Boolean) { _disableConfirmationUI.value = value }

    // ---- New SDK-call state (one StateFlow per operation) ----
    private val _executeState = MutableStateFlow<ExecuteUiState>(ExecuteUiState.Idle)
    val executeState: StateFlow<ExecuteUiState> = _executeState.asStateFlow()

    private val _setBiometricsPinState = MutableStateFlow<ExecuteUiState>(ExecuteUiState.Idle)
    val setBiometricsPinState: StateFlow<ExecuteUiState> = _setBiometricsPinState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val verifyOtpState: StateFlow<LoginUiState> = _verifyOtpState.asStateFlow()

    // SDK events are delivered via an unbounded Channel so that events fired while no collector
    // is attached (Fragment STOPPED, config change, backgrounding) queue up until the next
    // collector resumes, instead of being dropped by SharedFlow's replay=0 semantics.
    // The Channel is fed by a viewModelScope forwarder (in init) that collects repo.eventFlow
    // for the lifetime of this ViewModel — surviving Fragment STARTED/STOPPED transitions.
    private val _eventChannel = Channel<ExecuteEvent>(Channel.UNLIMITED)
    val eventFlow: Flow<ExecuteEvent> = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Propagate cancellation; log any other Exception so a future refactor that
            // replaces trySend with a suspending call, or an upstream Flow operator change,
            // can't silently kill the forwarder. Catching Exception (not Throwable) so JVM
            // Error subclasses (OutOfMemoryError, StackOverflowError, AssertionError)
            // propagate as-is rather than being swallowed.
            try {
                repo.eventFlow.collect { event ->
                    val result = _eventChannel.trySend(event)
                    // Invariant guard: _eventChannel is Channel(UNLIMITED) and never closed
                    // by this VM, so trySend cannot fail today. Logged loudly so a regression
                    // (bounded channel, explicit close, etc.) is immediately visible.
                    if (result.isFailure) {
                        Log.e(TAG, "INVARIANT VIOLATION: trySend failed; dropped SDK event: $event")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "event forwarder failed", e)
            }
        }
    }

    // ---- SDK entry points ----
    // Config / setup pass-through. Keeps the repository encapsulated — callers use the ViewModel
    // as the single UI-facing API.
    fun initSdk(context: Context, endpoint: String, appId: String, settings: SettingsManagement) =
        repo.initSdk(context, endpoint, appId, settings)

    fun getDeviceId(context: Context): String = repo.getDeviceId(context)
    fun setLayoutProvider(provider: LayoutProvider) = repo.setLayoutProvider(provider)
    fun setViewSetterProvider(provider: ViewSetterProvider) = repo.setViewSetterProvider(provider)
    fun setCustomUserAgent(agent: String) = repo.setCustomUserAgent(agent)
    fun setSecurityQuestions(questions: Array<SecurityQuestion>) = repo.setSecurityQuestions(questions)
    fun moveTaskToFront(activity: Activity) = repo.moveTaskToFront(activity)

    fun executeSdk(
        activity: Activity, userToken: String, encryptionKey: String, challengeId: String,
    ) {
        viewModelScope.launch {
            _executeState.value = ExecuteUiState.Loading
            try {
                repo.execute(activity, userToken, encryptionKey, listOf(challengeId)).collect {
                    _executeState.value = it.toExecuteUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "executeSdk failed", e)
                _executeState.value = ExecuteUiState.Error(e)
            }
        }
    }

    fun setBiometricsPin(activity: Activity, userToken: String, encryptionKey: String) {
        viewModelScope.launch {
            _setBiometricsPinState.value = ExecuteUiState.Loading
            try {
                repo.setBiometricsPin(activity, userToken, encryptionKey).collect {
                    _setBiometricsPinState.value = it.toExecuteUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "setBiometricsPin failed", e)
                _setBiometricsPinState.value = ExecuteUiState.Error(e)
            }
        }
    }

    fun signInSocial(
        activity: Activity, provider: SocialProvider,
        deviceToken: String, deviceEncryptionKey: String,
    ) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            // Logout first to clear any cached SDK session, then login.
            // If logout fails with a non-transient error, short-circuit and surface a login error
            // rather than proceeding against stale SDK state. For userCanceled / networkError
            // the logout likely had no prior session to clear, so proceed to login.
            val proceed = try {
                var ok = true
                repo.performLogout(activity, provider).collect { outcome ->
                    when (outcome) {
                        is LogoutOutcome.Success -> Unit
                        is LogoutOutcome.Error -> {
                            Log.w(TAG, "performLogout before login failed", outcome.throwable)
                            val err = outcome.throwable
                            val isTransientError = err is ApiError && err.code.isTransient
                            if (!isTransientError) {
                                _loginState.value = LoginUiState.Error(err)
                                ok = false
                            }
                        }
                    }
                }
                ok
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "performLogout collect failed", e)
                _loginState.value = LoginUiState.Error(e)
                false
            }
            if (!proceed) return@launch
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

    fun verifyOtp(
        activity: Activity, otpToken: String, deviceToken: String, deviceEncryptionKey: String,
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

    fun resetExecuteState() { _executeState.value = ExecuteUiState.Idle }
    fun resetSetBiometricsPinState() { _setBiometricsPinState.value = ExecuteUiState.Idle }
    fun resetLoginState() { _loginState.value = LoginUiState.Idle }
    fun resetVerifyOtpState() { _verifyOtpState.value = LoginUiState.Idle }

    /**
     * Pure function: map an [ExecuteUiState] to an [ExecuteDirections] describing what
     * UI should do. No side effects, no framework types. Delegates to the shared
     * [toDirections] extension so `ExecuteViewModel` and this VM cannot diverge.
     */
    fun computeExecuteDirections(state: ExecuteUiState): ExecuteDirections =
        state.toDirections()
}
