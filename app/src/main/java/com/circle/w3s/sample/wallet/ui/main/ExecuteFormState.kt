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

data class ExecuteFormState(
    val endpoint: String? = null,
    val appId: String? = null,
    val userToken: String? = null,
    val encryptionKey: String? = null,
    val socialUserToken: String? = null,
    val socialEncryptionKey: String? = null,
    val emailUserToken: String? = null,
    val emailEncryptionKey: String? = null,
    val challengeId: String? = null,
    val isExecuteDataValid: Boolean = false,
    val isSetBiometricsPinInputDataValid: Boolean = false
)