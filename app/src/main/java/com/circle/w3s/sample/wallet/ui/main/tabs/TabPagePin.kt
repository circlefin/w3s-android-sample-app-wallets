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

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.result.ExecuteResult
import com.circle.w3s.sample.wallet.MainActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.PagerPinBinding
import com.circle.w3s.sample.wallet.ui.NecessaryTextView
import com.circle.w3s.sample.wallet.ui.alert.AlertBar
import com.circle.w3s.sample.wallet.util.KeyboardUtils
import com.google.android.material.snackbar.Snackbar

class TabPagePin(activity: MainActivity) : ITabPage(activity),
    Callback<ExecuteResult> {
    private val activity = activity
    private lateinit var binding: PagerPinBinding
    override fun initPage(context: Context): View {
        val inflater = LayoutInflater.from(context)
        binding = PagerPinBinding.inflate(inflater)
        initPagerPin(activity, binding)
        return binding.root
    }

    private fun initPagerPin(context: Context, binding: PagerPinBinding) {
        context?.let {
            val versionName = getVersionName(it)
            binding.version.text = versionName
        }
        binding.execute.setOnClickListener {
            initAndLaunchSdk({
                WalletSdk.execute(
                    activity,
                    binding.userToken.inputValue.text.toString(),
                    binding.encryptionKey.inputValue.text.toString(),
                    arrayOf(binding.challengeId.inputValue.text.toString()),
                    this
                )
            }, binding)
        }
        viewModel.executeFormState.observe(activity, Observer {
            it ?: return@Observer
            binding.execute.isEnabled = it.isExecuteDataValid
        })
        viewModel.isSetBiometricsPinDataValid.observe(activity, Observer {
            it ?: return@Observer
            binding.setBiometricsPin.isEnabled = it
        })

        binding.setBiometricsPin.setOnClickListener {
            initAndLaunchSdk({
                WalletSdk.setBiometricsPin(
                    activity,
                    binding.userToken.inputValue.text.toString(),
                    binding.encryptionKey.inputValue.text.toString(),
                    this
                )
            }, binding)
        }
        binding.challengeId.inputTitle.setText(R.string.label_challenge_id)

        val epTitle: NecessaryTextView = binding.endpoint.inputTitle
        epTitle.setNecessary(true)
        epTitle.setText(R.string.label_endpoint)

        val appIdTitle: NecessaryTextView = binding.appId.inputTitle
        appIdTitle.setNecessary(true)
        appIdTitle.setText(R.string.label_app_id)

        val userTokenTitle: NecessaryTextView = binding.userToken.inputTitle
        userTokenTitle.setNecessary(true)
        userTokenTitle.setText(R.string.label_user_token)

        val encryptionKeyTitle: NecessaryTextView = binding.encryptionKey.inputTitle
        encryptionKeyTitle.setNecessary(true)
        encryptionKeyTitle.setText(R.string.label_encryption_key)

        binding.enableBiometrics.inputTitle.setText(R.string.label_biometrics_setting)
        binding.enableBiometrics.inputSubtitle.setText(R.string.label_sub_biometrics_setting)
        binding.enableBiometrics.toggleBtn.isChecked =
            viewModel.enableBiometrics.value ?: false
        binding.enableBiometrics.toggleBtn.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnableBiometrics(
                isChecked
            )
        }

        binding.endpoint.inputValue.setText(viewModel.executeFormState.value?.endpoint ?: "")
        binding.appId.inputValue.setText(viewModel.executeFormState.value?.appId ?: "")

        binding.endpoint.inputValue.doAfterTextChanged {
            executeDataChanged(binding)
        }
        binding.appId.inputValue.doAfterTextChanged {
            executeDataChanged(binding)
        }
        binding.userToken.inputValue.doAfterTextChanged {
            executeDataChanged(binding)
        }
        binding.encryptionKey.inputValue.doAfterTextChanged {
            executeDataChanged(binding)
        }
        binding.challengeId.inputValue.doAfterTextChanged {
            executeDataChanged(binding)
        }

    }

    private fun getVersionName(context: Context): String {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        return packageInfo.versionName;
    }

    private inline fun initAndLaunchSdk(launchBlock: () -> Unit, binding: PagerPinBinding) {
        KeyboardUtils.hideKeyboard(binding.challengeId.inputValue)
        binding.ll.requestFocus()
        try {
            val settingsManagement = SettingsManagement()
            settingsManagement.isEnableBiometricsPin = binding.enableBiometrics.toggleBtn.isChecked
            WalletSdk.init(
                binding.main.context,
                WalletSdk.Configuration(
                    binding.endpoint.inputValue.text.toString(),
                    binding.appId.inputValue.text.toString(),
                    settingsManagement
                )
            )
        } catch (t: Throwable) {
            showSnack(t.message ?: "initSdk catch null", binding.main)
            return
        }
        setInProgress(true)
        launchBlock()
    }

    private fun setInProgress(inProgress: Boolean) {
        activity.runOnUiThread({
            binding.execute.isClickable = !inProgress
            binding.loading.visibility = if (inProgress) View.VISIBLE else View.GONE
        })
    }

    private fun showSnack(
        message: String, view: View
    ) {
//            Log.i(TAG, message)
        val snackbar = Snackbar.make(
            view, message,
            Snackbar.LENGTH_LONG
        ).setAction("", null)
        snackbar.view.setBackgroundColor(Color.BLACK)
        val textView =
            snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 10
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    private fun executeDataChanged(binding: PagerPinBinding) {
        viewModel.executeDataChanged(
            binding.endpoint.inputValue.text.toString(),
            binding.appId.inputValue.text.toString(),
            binding.userToken.inputValue.text.toString(),
            binding.encryptionKey.inputValue.text.toString(),
            viewModel.executeFormState.value?.socialUserToken ,
            viewModel.executeFormState.value?.socialEncryptionKey ,
            viewModel.executeFormState.value?.emailUserToken ,
            viewModel.executeFormState.value?.emailEncryptionKey ,
            binding.challengeId.inputValue.text.toString(),
        )
    }

    override fun onError(error: Throwable): Boolean {
        setInProgress(false)
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
        setInProgress(false)
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
        setInProgress(false)
        AlertBar.showAlert(
            binding.root,
            AlertBar.Type.ALERT_SUCCESS,
            binding.root.context.getString(R.string.execute_successful)
        )
    }

}