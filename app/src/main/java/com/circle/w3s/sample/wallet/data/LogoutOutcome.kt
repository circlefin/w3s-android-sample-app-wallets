// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import com.circle.w3s.sample.wallet.ui.main.LogoutUiState

/**
 * Terminal outcome of a call to [WalletRepository.performLogout]. Exactly one variant is
 * emitted per call.
 */
sealed interface LogoutOutcome {
    /** Logout completed successfully. */
    data object Success : LogoutOutcome

    /** The SDK reported a terminal error ([throwable]). */
    data class Error(val throwable: Throwable) : LogoutOutcome
}

/** Map a data-layer [LogoutOutcome] to its UI-layer [LogoutUiState] counterpart. */
internal fun LogoutOutcome.toLogoutUiState(): LogoutUiState = when (this) {
    is LogoutOutcome.Success -> LogoutUiState.Success
    is LogoutOutcome.Error -> LogoutUiState.Error(throwable)
}
