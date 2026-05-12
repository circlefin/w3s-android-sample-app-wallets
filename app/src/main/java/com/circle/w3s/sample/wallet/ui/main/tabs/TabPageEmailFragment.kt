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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.presentation.SettingsManagement
import com.circle.w3s.sample.wallet.ExecuteActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.PagerEmailBinding
import com.circle.w3s.sample.wallet.ui.NecessaryTextView
import com.circle.w3s.sample.wallet.ui.alert.AlertBar
import com.circle.w3s.sample.wallet.ui.main.LoginUiState
import com.circle.w3s.sample.wallet.ui.main.MainViewModel
import kotlinx.coroutines.launch

private const val TAG = "TabPageEmailFragment"

class TabPageEmailFragment : Fragment(), View.OnClickListener {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: PagerEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PagerEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deviceId.title.setText(R.string.label_device_id)
        binding.deviceId.content.text = viewModel.getDeviceId(requireContext())
        binding.deviceId.content.setOnClickListener(this)

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

        binding.appId.inputValue.doAfterTextChanged { updateUi() }
        binding.deviceToken.inputValue.doAfterTextChanged { updateUi() }
        binding.deviceEncryptionKey.inputValue.doAfterTextChanged { updateUi() }
        binding.otpToken.inputValue.doAfterTextChanged { updateUi() }

        binding.btMain.setOnClickListener(this)
        binding.btnEmailExecute.setOnClickListener(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.verifyOtpState.collect { state -> handleVerifyOtpState(state) }
                }
                launch {
                    viewModel.executeFormState.collect { state ->
                        val visible = !TextUtils.isEmpty(state?.emailUserToken) &&
                            !TextUtils.isEmpty(state?.emailEncryptionKey)
                        binding.btnEmailExecute.visibility = if (visible) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun executeDataChanged() {
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

    private fun updateUi() {
        val enableBtn = !TextUtils.isEmpty(binding.deviceToken.inputValue.text.toString()) &&
            !TextUtils.isEmpty(binding.deviceEncryptionKey.inputValue.text.toString()) &&
            !TextUtils.isEmpty(binding.appId.inputValue.text.toString()) &&
            !TextUtils.isEmpty(binding.otpToken.inputValue.text.toString())

        binding.btMain.isEnabled = enableBtn
        executeDataChanged()
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
            viewModel.verifyOtp(
                requireActivity(),
                binding.otpToken.inputValue.text.toString(),
                binding.deviceToken.inputValue.text.toString(),
                binding.deviceEncryptionKey.inputValue.text.toString(),
            )
        }
    }

    private fun handleVerifyOtpState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> Unit
            is LoginUiState.Loading -> Unit
            is LoginUiState.Success -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_SUCCESS,
                    binding.root.context.getString(R.string.action_result_login_successful)
                )

                viewModel.executeDataChanged(
                    viewModel.executeFormState.value?.endpoint,
                    binding.appId.inputValue.text.toString(),
                    viewModel.executeFormState.value?.userToken,
                    viewModel.executeFormState.value?.encryptionKey,
                    viewModel.executeFormState.value?.socialUserToken,
                    viewModel.executeFormState.value?.socialEncryptionKey,
                    state.result.userToken,
                    state.result.encryptionKey,
                    null,
                )
                viewModel.resetVerifyOtpState()
            }
            is LoginUiState.Error -> {
                val error = state.throwable
                Log.e(TAG, "verifyOtp failed", error)
                if (error !is ApiError) {
                    AlertBar.showAlert(
                        binding.root,
                        AlertBar.Type.ALERT_FAILED,
                        error.message ?: "onError null"
                    )
                    viewModel.resetVerifyOtpState()
                    return
                }
                when (error.code) {
                    ApiError.ErrorCode.userCanceled,
                    ApiError.ErrorCode.networkError -> Unit
                    else -> AlertBar.showAlert(
                        binding.root,
                        AlertBar.Type.ALERT_FAILED,
                        error.message ?: "onError null"
                    )
                }
                viewModel.resetVerifyOtpState()
            }
        }
    }

    private inline fun initAndLaunchSdk(launchBlock: () -> Unit) {
        try {
            val settingsManagement = SettingsManagement()
            viewModel.initSdk(
                requireContext().applicationContext,
                viewModel.executeFormState.value?.endpoint.orEmpty(),
                viewModel.executeFormState.value?.appId.orEmpty(),
                settingsManagement,
            )
        } catch (e: Exception) {
            AlertBar.showAlert(
                binding.root,
                AlertBar.Type.ALERT_FAILED,
                e.message ?: "initSdk catch null"
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
        val intent = Intent(context, ExecuteActivity::class.java).setFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        )
        intent.putExtras(b)
        context.startActivity(intent)
    }

    private fun copyDeviceId() {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val deviceId = binding.deviceId.content.text
        clipboard.setPrimaryClip(ClipData.newPlainText(deviceId, deviceId))
    }
}
