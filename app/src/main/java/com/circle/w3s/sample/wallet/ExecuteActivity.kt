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

package com.circle.w3s.sample.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.result.ExecuteResult
import com.circle.w3s.sample.wallet.databinding.ActivitySocialExecuteBinding
import com.circle.w3s.sample.wallet.ui.alert.AlertBar


class ExecuteActivity : AppCompatActivity(), View.OnClickListener,
    Callback<ExecuteResult> {
    private val TAG: String = "APP.SocialExecuteActivity"
    private lateinit var binding: ActivitySocialExecuteBinding


    companion object {
        const val ARG_ENCRYPTION_KEY = "encryptionKey"
        const val ARG_USER_TOKEN = "userToken"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySocialExecuteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.close.setOnClickListener(this)

        val extra = intent.extras
        val encryptionKey = extra?.getString(ARG_ENCRYPTION_KEY)
        val userToken = extra?.getString(ARG_USER_TOKEN)

        binding.labelEncryptionKey.setText(R.string.label_encryption_key)
        binding.encryptionKey.text = encryptionKey
        binding.userToken.title.setText(R.string.label_user_token)
        binding.userToken.content.text = userToken
        binding.userToken.content.setOnClickListener(this)
        binding.challengeId.inputTitle.setNecessary(true)
        binding.challengeId.inputTitle.setText(R.string.label_challenge_id)
        binding.challengeId.inputValue.doAfterTextChanged {
            if (!TextUtils.isEmpty(binding.challengeId.inputValue.text)) {
                binding.btMain.isEnabled = true
            } else {
                binding.btMain.isEnabled = false
            }
        }
        binding.btMain.isEnabled = false
        binding.btMain.setOnClickListener(this)

        // FIXME For test convenience.
//        binding.encryptionKey.setOnClickListener(this)
//        binding.challengeId.inputValue.setText(R.string.pw_challengeId)
    }

    private fun executeSocial() {
        WalletSdk.execute(
            this,
            binding.userToken.content.text.toString(),
            binding.encryptionKey.text.toString(),
            arrayOf(binding.challengeId.inputValue.text.toString()),
            this
        )
    }

    override fun onBackPressed() {
        goBackToSdkUi()
        super.onBackPressed()
    }

    /**
     * Bring SDK UI to the front and finish the Activity.
     */
    private fun goBackToSdkUi() {
        WalletSdk.moveTaskToFront(this)
        finish()
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v.id) {
            R.id.close -> finish()
            R.id.encryptionKey -> copyEncryptionKey()
            R.id.content -> copyUserToken()
            R.id.btMain -> executeSocial()
        }
    }

    private fun copyEncryptionKey() {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(binding.encryptionKey.text, binding.encryptionKey.text)
        clipboard.setPrimaryClip(clip)
    }

    private fun copyUserToken() {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(binding.userToken.content.text, binding.userToken.content.text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onError(error: Throwable): Boolean {
        error.printStackTrace()
        if (error !is ApiError) {
            AlertBar.showAlert(
                binding.root,
                AlertBar.Type.ALERT_FAILED,
                error.message ?: "onError null"
            )
            return false // App won't handle next step, SDK will finish the Activity.
        }
        when (error.code) {
            ApiError.ErrorCode.userCanceled,
            ApiError.ErrorCode.networkError -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    error.code.value.toString() + " " + error.message
                )
                return false // App won't handle next step, SDK will finish the Activity.
            }

            else -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    error.message
                )
            }
        }
        return true // App will handle next step, SDK will keep the Activity.
    }

    override fun onWarning(warning: ExecuteWarning, result: ExecuteResult?): Boolean {
        AlertBar.showAlert(
            binding.root,
            AlertBar.Type.ALERT_FAILED,
            "${warning?.warningType}, ${warning?.warningString}, ${result?.resultType?.name}, ${result?.status?.name}, ${result?.data?.signature}"
        )
        //return true, App will handle next step, SDK will keep the Activity.
        //return false, App won't handle next step, SDK will finish the Activity.
        return false
    }

    override fun onResult(result: ExecuteResult) {
        AlertBar.showAlert(
            binding.root,
            AlertBar.Type.ALERT_SUCCESS,
            getString(R.string.execute_successful)
        )
    }
}