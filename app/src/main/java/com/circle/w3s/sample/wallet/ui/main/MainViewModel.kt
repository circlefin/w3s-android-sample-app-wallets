// Copyright (c) 2024, Circle Internet Financial, LTD. All rights reserved.
//
// SPDX-License-Identifier: Apache-2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
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
import androidx.navigation.NavDirections

class MainViewModel : ViewModel() {

    private val _executeForm = MutableLiveData<ExecuteFormState>()
    val executeFormState: LiveData<ExecuteFormState> = _executeForm
    private val _naviDirections = MutableLiveData<NavDirections?>()
    val naviDirections: LiveData<NavDirections?> = _naviDirections
    private val _enableBiometrics = MutableLiveData(false)
    val enableBiometrics: LiveData<Boolean> = _enableBiometrics
    private val _isSetBiometricsPinDataValid = MutableLiveData(false)
    val isSetBiometricsPinDataValid: LiveData<Boolean> = _isSetBiometricsPinDataValid
    private val _disableConfirmationUI = MutableLiveData(false)
    val disableConfirmationUI: LiveData<Boolean> = _disableConfirmationUI


    fun executeDataChanged(endpoint: String?, appId: String?, userToken: String?, encryptionKey: String?, socialUserToken: String?, socialEncryptionKey: String?, emailUserToken: String?, emailEncryptionKey: String?, challengeId: String?) {
        val isSetBiometricsPinInputDataValid = endpoint?.isNotBlank() ?: false && appId?.isNotBlank() ?: false && userToken?.isNotBlank() ?: false && encryptionKey?.isNotBlank() ?: false
        _isSetBiometricsPinDataValid.value = isSetBiometricsPinInputDataValid && _enableBiometrics.value == true
        _executeForm.value =  ExecuteFormState(
            isSetBiometricsPinInputDataValid = isSetBiometricsPinInputDataValid,
            isExecuteDataValid = isSetBiometricsPinInputDataValid && challengeId?.isNotBlank() ?: false,
            endpoint =  endpoint,
            appId =  appId,
            userToken =  userToken,
            encryptionKey =  encryptionKey,
            socialUserToken =  socialUserToken,
            socialEncryptionKey =  socialEncryptionKey,
            emailUserToken = emailUserToken,
            emailEncryptionKey =  emailEncryptionKey,
            challengeId =  challengeId,
        )
    }

    fun setNaviDirections(directions: NavDirections?) {
        _naviDirections.value = directions
    }

    fun setEnableBiometrics(value: Boolean){
        _enableBiometrics.value = value
        _isSetBiometricsPinDataValid.value =  value && _executeForm.value?.isSetBiometricsPinInputDataValid == true
    }

    fun setDisableConfirmationUI(value: Boolean){
        _disableConfirmationUI.value = value
    }
}