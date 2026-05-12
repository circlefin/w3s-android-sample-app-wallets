// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui

import circle.programmablewallet.sdk.api.ApiError

/**
 * Classification of a login or logout error produced by
 * `PerformLoginViewModel.classifyAuthError`. Closed so the Activity's dispatch on the
 * Error branch is exhaustive and a future variant forces every call site to decide.
 *
 * Named "Auth" (not "Login") because both the login and logout state-handlers consume
 * this classification via the shared `routeLoginOrLogoutError` helper.
 *
 * Variant-to-UI-behavior mapping in PerformLoginActivity:
 * - [Transient]: silent (userCanceled / networkError do not navigate or surface a dialog)
 * - [Other]: route to CustomActivity with the error message
 * - [Unknown]: silent (matches master's "only ApiError non-transient triggers goCustom")
 */
sealed interface AuthErrorClassification {
    data class Transient(val code: ApiError.ErrorCode, val message: String?) : AuthErrorClassification
    data class Other(val code: ApiError.ErrorCode, val message: String?) : AuthErrorClassification
    data class Unknown(val message: String?) : AuthErrorClassification
}
