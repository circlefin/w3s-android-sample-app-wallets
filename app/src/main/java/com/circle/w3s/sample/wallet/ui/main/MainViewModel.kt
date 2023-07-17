// Copyright (c) 2023, Circle Technologies, LLC. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.circle.w3s.sample.wallet.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.circle.w3s.sample.wallet.R

class MainViewModel : ViewModel() {

    private val _executeForm = MutableLiveData<ExecuteFormState>()
    val executeFormState: LiveData<ExecuteFormState> = _executeForm
    fun executeDataChanged(endpoint: String, appId: String, userToken: String, encryptionKey: String, challengeId: String) {
        if (endpoint.isBlank()) {
            _executeForm.value = ExecuteFormState(endpointError = R.string.invalid_endpoint)
        } else if (appId.isBlank()) {
            _executeForm.value = ExecuteFormState(appIdError = R.string.invalid_app_id)
        } else if (userToken.isBlank()) {
            _executeForm.value = ExecuteFormState(userTokenError = R.string.invalid_user_token)
        } else if (encryptionKey.isBlank()) {
            _executeForm.value = ExecuteFormState(encryptionKeyError = R.string.invalid_encryption_key)
        } else if (challengeId.isBlank()) {
            _executeForm.value = ExecuteFormState(challengeIdError = R.string.invalid_challenge_id)
        } else {
            _executeForm.value = ExecuteFormState(isDataValid = true)
        }
    }
}