// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui.main

import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.result.ExecuteResult
import com.circle.w3s.sample.wallet.data.isTransient

/**
 * The UI-facing decision that results from observing an [ExecuteUiState].
 * `ViewModel.computeExecuteDirections(state)` returns one of these;
 * UI code mechanically dispatches on the variant to perform side effects.
 *
 * Variants are closed so `when (direction)` in UI is exhaustive and any new branch
 * forces every call site to decide how to handle it.
 */
sealed interface ExecuteDirections {
    data object None : ExecuteDirections
    data object ShowLoading : ExecuteDirections
    data class NavigateSuccess(val result: ExecuteResult) : ExecuteDirections
    data class NavigateWarning(val warning: ExecuteWarning, val result: ExecuteResult?) : ExecuteDirections

    /**
     * Transient, user-attributable failure (userCanceled / networkError). UI typically
     * surfaces the error code + message without navigating to a custom-error screen.
     * `code` is already stringified here to match master's `setDirection(errorCode = String)`
     * signature and keep UI framework-free.
     */
    data class NavigateTransientError(val code: String, val message: String?) : ExecuteDirections

    /**
     * Non-transient [ApiError] — MainFragment's original behavior routes this to a
     * standalone CustomActivity; ExecuteActivity just shows an AlertBar with the message.
     * Both semantics share this variant; each UI decides what "custom" means for its flow.
     */
    data class GoCustom(val message: String?) : ExecuteDirections

    /**
     * Non-[ApiError] Throwable. UI surfaces the message verbatim with a null-safe default.
     */
    data class NavigateGenericError(val message: String) : ExecuteDirections
}

/**
 * Map a data-layer [ExecuteUiState] to an [ExecuteDirections] decision. Pure function
 * with no framework types; shared by `MainViewModel` and `ExecuteViewModel` so the
 * mapping rule has one authoritative implementation.
 *
 * Transient-error classification (userCanceled / networkError) is delegated to the shared
 * [isTransient] predicate in the data layer so changes to the SDK's transient set apply
 * everywhere in one edit.
 */
internal fun ExecuteUiState.toDirections(): ExecuteDirections = when (this) {
    is ExecuteUiState.Idle -> ExecuteDirections.None
    is ExecuteUiState.Loading -> ExecuteDirections.ShowLoading
    is ExecuteUiState.Success -> ExecuteDirections.NavigateSuccess(result)
    is ExecuteUiState.Warning -> ExecuteDirections.NavigateWarning(warning, result)
    is ExecuteUiState.Error -> {
        val t = throwable
        if (t is ApiError) {
            if (t.code.isTransient) {
                ExecuteDirections.NavigateTransientError(t.code.value.toString(), t.message)
            } else {
                ExecuteDirections.GoCustom(t.message)
            }
        } else {
            ExecuteDirections.NavigateGenericError(t.message ?: "onError null")
        }
    }
}
