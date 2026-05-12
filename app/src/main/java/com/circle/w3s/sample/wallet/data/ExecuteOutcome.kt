// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.result.ExecuteResult
import com.circle.w3s.sample.wallet.ui.main.ExecuteUiState

/**
 * Terminal outcome of a call to [WalletRepository.execute] or
 * [WalletRepository.setBiometricsPin]. Exactly one variant is emitted per call.
 */
sealed interface ExecuteOutcome {
    /** The SDK returned a successful [result]. */
    data class Success(val result: ExecuteResult) : ExecuteOutcome

    /**
     * The SDK reported a non-fatal [warning] that the user may still need to dismiss.
     * [result] may be null when the warning is surfaced before a result is produced.
     */
    data class Warning(val warning: ExecuteWarning, val result: ExecuteResult?) : ExecuteOutcome

    /** The SDK reported a terminal error ([throwable]). */
    data class Error(val throwable: Throwable) : ExecuteOutcome
}

/** Map a data-layer [ExecuteOutcome] to its UI-layer [ExecuteUiState] counterpart. */
internal fun ExecuteOutcome.toExecuteUiState(): ExecuteUiState = when (this) {
    is ExecuteOutcome.Success -> ExecuteUiState.Success(result)
    is ExecuteOutcome.Warning -> ExecuteUiState.Warning(warning, result)
    is ExecuteOutcome.Error -> ExecuteUiState.Error(throwable)
}
