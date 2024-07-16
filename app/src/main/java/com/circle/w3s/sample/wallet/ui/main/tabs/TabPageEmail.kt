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

package com.circle.w3s.sample.wallet.ui.main.tabs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.Callback2
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.result.LoginResult
import com.circle.w3s.sample.wallet.ExecuteActivity
import com.circle.w3s.sample.wallet.MainActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.PagerEmailBinding
import com.circle.w3s.sample.wallet.ui.NecessaryTextView
import com.circle.w3s.sample.wallet.ui.alert.AlertBar

class TabPageEmail(activity: MainActivity) : ITabPage(activity), View.OnClickListener {
    private val activity = activity
    private lateinit var binding: PagerEmailBinding
    private lateinit var contentDeviceId: TextView
    override fun initPage(context: Context): View {
        val inflater = LayoutInflater.from(context)
        binding = PagerEmailBinding.inflate(inflater)
        initPagerEmail(activity, binding)
        return binding.root
    }

    private fun initPagerEmail(activity: MainActivity, binding: PagerEmailBinding) {
        binding.deviceId.title.setText(R.string.label_device_id)
        val deviceId = WalletSdk.getDeviceId(activity.baseContext)
        contentDeviceId = binding.deviceId.content
        contentDeviceId.setText(deviceId)
        contentDeviceId.setOnClickListener(this)

        val appId: NecessaryTextView = binding.appId.inputTitle
        appId.setNecessary(true)
        appId.setText(R.string.label_app_id)
        binding.appId.inputValue.setText(viewModel.executeFormState.value?.appId ?: "")

        val deviceTokenTitle: NecessaryTextView = binding.deviceToken.inputTitle
        deviceTokenTitle.setNecessary(true)
        deviceTokenTitle.setText(R.string.label_device_token)

        val deviceEncryptionKeyTitle: NecessaryTextView = binding.deviceEncryptionKey.inputTitle
        deviceEncryptionKeyTitle.setNecessary(true)
        deviceEncryptionKeyTitle.setText(R.string.label_device_encryption_key)

        val otpTokenTitle: NecessaryTextView = binding.otpToken.inputTitle
        otpTokenTitle.setNecessary(true)
        otpTokenTitle.setText(R.string.label_otp_token)

        binding.btMain.isEnabled = false

        binding.appId.inputValue.doAfterTextChanged {
            updateUi(binding)
        }
        binding.deviceToken.inputValue.doAfterTextChanged {
            updateUi(binding)
        }
        binding.deviceEncryptionKey.inputValue.doAfterTextChanged {
            updateUi(binding)
        }
        binding.otpToken.inputValue.doAfterTextChanged {
            updateUi(binding)
        }

        binding.btMain.setOnClickListener(this)
        binding.btnEmailExecute.setOnClickListener(this)

        viewModel.executeFormState.observe(activity, Observer {
            if (!TextUtils.isEmpty(viewModel.executeFormState.value?.emailUserToken)
                && !TextUtils.isEmpty(viewModel.executeFormState.value?.emailEncryptionKey)
            ) {
                binding.btnEmailExecute.visibility = View.VISIBLE
            } else {
                binding.btnEmailExecute.visibility = View.GONE
            }
        })

    }


    private fun executeDataChanged(binding: PagerEmailBinding) {
        val originEndpoint = viewModel.executeFormState.value?.endpoint
        var appId: String? = binding.appId.inputValue.text.toString()
        if (TextUtils.isEmpty(appId)) {
            appId = viewModel.executeFormState.value?.appId
        }
        val originUserToken = viewModel.executeFormState.value?.userToken
        val originEncryptionKey = viewModel.executeFormState.value?.encryptionKey

        val originSocialUserToken = viewModel.executeFormState.value?.socialUserToken
        val originSocialEncryptionKey = viewModel.executeFormState.value?.socialEncryptionKey

        val originEmailUserToken = viewModel.executeFormState.value?.emailUserToken
        val originEmailEncryptionKey = viewModel.executeFormState.value?.emailEncryptionKey

        val originChallengeId = viewModel.executeFormState.value?.challengeId
        viewModel.executeDataChanged(
            originEndpoint,
            appId,
            originUserToken,
            originEncryptionKey,
            originSocialUserToken,
            originSocialEncryptionKey,
            originEmailUserToken,
            originEmailEncryptionKey,
            originChallengeId,
        )
    }

    private fun updateUi(binding: PagerEmailBinding) {
        var enableBtn: Boolean
        if (TextUtils.isEmpty(binding.deviceToken.inputValue.text.toString()) ||
            TextUtils.isEmpty(binding.deviceEncryptionKey.inputValue.text.toString()) ||
            TextUtils.isEmpty(binding.appId.inputValue.text.toString()) ||
            TextUtils.isEmpty(binding.otpToken.inputValue.text.toString())
        ) {
            enableBtn = false
        } else {
            enableBtn = true
        }

        binding.btMain.isEnabled = enableBtn
        executeDataChanged(binding)
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v.id) {
            R.id.content -> copyDeviceId()
            R.id.btMain -> signInEmail()
            R.id.btn_email_execute -> goExecute(v.context)
        }
    }

    private fun signInEmail() {
        initAndLaunchSdk {
            WalletSdk.verifyOTP(
                activity,
                binding.otpToken.inputValue.text.toString(),
                binding.deviceToken.inputValue.text.toString(),
                binding.deviceEncryptionKey.inputValue.text.toString(),
                object : Callback2<LoginResult> {
                    override fun onError(error: Throwable): Boolean {
                        error.printStackTrace()
                        if (error !is ApiError) {
                            return false // App won't handle next step, SDK will finish the Activity.
                        }
                        when (error.code) {
                            ApiError.ErrorCode.userCanceled,
                            ApiError.ErrorCode.networkError -> {
                                return false // App won't handle next step, SDK will finish the Activity.
                            }

                            else ->
                                AlertBar.showAlert(
                                    binding.root,
                                    AlertBar.Type.ALERT_FAILED,
                                    error.message ?: "onError null"
                                )
                        }
                        return false
                    }

                    override fun onResult(result: LoginResult) {
                        AlertBar.showAlert(
                            binding.root,
                            AlertBar.Type.ALERT_SUCCESS,
                            binding.root.context.getString(R.string.action_result_login_successful)
                        )

                        viewModel.executeDataChanged(
                            viewModel.executeFormState.value?.endpoint ,
                            binding.appId.inputValue.text.toString(),
                            viewModel.executeFormState.value?.userToken ,
                            viewModel.executeFormState.value?.encryptionKey ,
                            viewModel.executeFormState.value?.socialUserToken ,
                            viewModel.executeFormState.value?.socialEncryptionKey ,
                            result.userToken,
                            result.encryptionKey,
                            null,
                        )
                    }
                }
            )
        }
    }

    private inline fun initAndLaunchSdk(launchBlock: () -> Unit) {
        try {
            val settingsManagement = SettingsManagement()

            WalletSdk.init(
                binding.root.context,
                WalletSdk.Configuration(
                    viewModel.executeFormState.value?.endpoint,
                    viewModel.executeFormState.value?.appId,
                    settingsManagement
                )
            )
        } catch (t: Throwable) {
            AlertBar.showAlert(
                binding.root,
                AlertBar.Type.ALERT_FAILED,
                t.message ?: "initSdk catch null"
            )
            return
        }
        launchBlock()
    }

    private fun goExecute(context: Context?) {
        context ?: return
        val b = Bundle()
        b.putString(
            ExecuteActivity.ARG_ENCRYPTION_KEY,
            viewModel.executeFormState.value?.emailEncryptionKey
        )
        b.putString(
            ExecuteActivity.ARG_USER_TOKEN,
            viewModel.executeFormState.value?.emailUserToken
        )
        val intent = Intent(
            context,
            ExecuteActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtras(b)
        context.startActivity(intent)
    }

    private fun copyDeviceId() {
        val clipboard: ClipboardManager =
            activity.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(contentDeviceId.text, contentDeviceId.text)
        clipboard.setPrimaryClip(clip)
    }
}