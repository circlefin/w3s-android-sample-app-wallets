// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui.main

import circle.programmablewallet.sdk.result.LoginResult

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val result: LoginResult) : LoginUiState
    data class Error(val throwable: Throwable) : LoginUiState
}
