// Copyright (c) 2026, Circle Internet Financial, LTD. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.circle.w3s.sample.wallet.ui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circle.w3s.sample.wallet.data.WalletRepository
import com.circle.w3s.sample.wallet.data.toExecuteUiState
import com.circle.w3s.sample.wallet.ui.main.ExecuteDirections
import com.circle.w3s.sample.wallet.ui.main.ExecuteUiState
import com.circle.w3s.sample.wallet.ui.main.toDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ExecuteViewModel"

@HiltViewModel
class ExecuteViewModel @Inject internal constructor(
    private val repo: WalletRepository,
) : ViewModel() {

    private val _executeState = MutableStateFlow<ExecuteUiState>(ExecuteUiState.Idle)
    val executeState: StateFlow<ExecuteUiState> = _executeState.asStateFlow()

    fun moveTaskToFront(activity: Activity) = repo.moveTaskToFront(activity)

    fun execute(
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
                Log.e(TAG, "execute failed", e)
                _executeState.value = ExecuteUiState.Error(e)
            }
        }
    }

    fun resetExecuteState() { _executeState.value = ExecuteUiState.Idle }

    /**
     * See `MainViewModel.computeExecuteDirections`. Both VMs delegate to the same shared
     * [toDirections] extension so the mapping rule has one authoritative implementation.
     */
    fun computeExecuteDirections(state: ExecuteUiState): ExecuteDirections =
        state.toDirections()
}
