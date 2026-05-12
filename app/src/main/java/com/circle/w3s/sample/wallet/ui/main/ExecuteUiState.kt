// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui.main

import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.result.ExecuteResult

sealed interface ExecuteUiState {
    data object Idle : ExecuteUiState
    data object Loading : ExecuteUiState
    data class Success(val result: ExecuteResult) : ExecuteUiState
    data class Warning(val warning: ExecuteWarning, val result: ExecuteResult?) : ExecuteUiState
    data class Error(val throwable: Throwable) : ExecuteUiState
}
