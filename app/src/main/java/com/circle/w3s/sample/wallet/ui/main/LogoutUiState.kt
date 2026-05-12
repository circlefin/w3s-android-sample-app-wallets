// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui.main

sealed interface LogoutUiState {
    data object Idle : LogoutUiState
    data object Loading : LogoutUiState
    data object Success : LogoutUiState
    data class Error(val throwable: Throwable) : LogoutUiState
}
