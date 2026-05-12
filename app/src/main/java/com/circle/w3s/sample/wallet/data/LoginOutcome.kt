// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import circle.programmablewallet.sdk.result.LoginResult
import com.circle.w3s.sample.wallet.ui.main.LoginUiState

/**
 * Terminal outcome of a call to [WalletRepository.performLogin] or
 * [WalletRepository.verifyOTP]. Exactly one variant is emitted per call.
 */
sealed interface LoginOutcome {
    /** The SDK returned a successful login [result]. */
    data class Success(val result: LoginResult) : LoginOutcome

    /** The SDK reported a terminal error ([throwable]). */
    data class Error(val throwable: Throwable) : LoginOutcome
}

/** Map a data-layer [LoginOutcome] to its UI-layer [LoginUiState] counterpart. */
internal fun LoginOutcome.toLoginUiState(): LoginUiState = when (this) {
    is LoginOutcome.Success -> LoginUiState.Success(result)
    is LoginOutcome.Error -> LoginUiState.Error(throwable)
}
