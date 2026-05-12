// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.data

import circle.programmablewallet.sdk.api.ApiError

/**
 * Single source of truth for which SDK error codes are "transient" — user-attributable
 * conditions (cancel / network) that flows handle as a soft failure rather than a
 * navigate-to-error-screen.
 *
 * Used by:
 * - `ExecuteUiState.toDirections` (maps Error → [ExecuteDirections.NavigateTransientError])
 * - `PerformLoginViewModel.classifyAuthError` (maps → [AuthErrorClassification.Transient])
 * - `MainViewModel.signInSocial` (decides whether the pre-login logout error can be
 *   swallowed and the login phase proceed)
 *
 * If the SDK adds / reclassifies a transient code, update this predicate in one place and
 * all three flows follow.
 */
internal val ApiError.ErrorCode.isTransient: Boolean
    get() = this == ApiError.ErrorCode.userCanceled || this == ApiError.ErrorCode.networkError
