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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.presentation.SettingsManagement
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.PagerPinBinding
import com.circle.w3s.sample.wallet.ui.NecessaryTextView
import com.circle.w3s.sample.wallet.ui.alert.AlertBar
import com.circle.w3s.sample.wallet.ui.main.ExecuteUiState
import com.circle.w3s.sample.wallet.ui.main.MainViewModel
import com.circle.w3s.sample.wallet.util.KeyboardUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

private const val TAG = "TabPagePinFragment"

class TabPagePinFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: PagerPinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PagerPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.version.text = getVersionName(requireContext())

        binding.execute.setOnClickListener {
            initAndLaunchSdk {
                viewModel.executeSdk(
                    requireActivity(),
                    binding.userToken.inputValue.text.toString(),
                    binding.encryptionKey.inputValue.text.toString(),
                    binding.challengeId.inputValue.text.toString(),
                )
            }
        }
        binding.setBiometricsPin.setOnClickListener {
            initAndLaunchSdk {
                viewModel.setBiometricsPin(
                    requireActivity(),
                    binding.userToken.inputValue.text.toString(),
                    binding.encryptionKey.inputValue.text.toString(),
                )
            }
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
        binding.enableBiometrics.toggleBtn.isChecked = viewModel.enableBiometrics.value
        binding.enableBiometrics.toggleBtn.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnableBiometrics(isChecked)
        }

        binding.endpoint.inputValue.setText(viewModel.executeFormState.value?.endpoint ?: "")
        binding.appId.inputValue.setText(viewModel.executeFormState.value?.appId ?: "")

        binding.endpoint.inputValue.doAfterTextChanged { executeDataChanged() }
        binding.appId.inputValue.doAfterTextChanged { executeDataChanged() }
        binding.userToken.inputValue.doAfterTextChanged { executeDataChanged() }
        binding.encryptionKey.inputValue.doAfterTextChanged { executeDataChanged() }
        binding.challengeId.inputValue.doAfterTextChanged { executeDataChanged() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.executeState.collect { state ->
                        handleExecuteState(state) { viewModel.resetExecuteState() }
                    }
                }
                launch {
                    viewModel.setBiometricsPinState.collect { state ->
                        handleExecuteState(state) { viewModel.resetSetBiometricsPinState() }
                    }
                }
                launch {
                    viewModel.executeFormState.collect { state ->
                        state ?: return@collect
                        binding.execute.isEnabled = state.isExecuteDataValid
                    }
                }
                launch {
                    viewModel.isSetBiometricsPinDataValid.collect { valid ->
                        binding.setBiometricsPin.isEnabled = valid
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getVersionName(context: Context): String {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        return packageInfo.versionName
    }

    private inline fun initAndLaunchSdk(launchBlock: () -> Unit) {
        KeyboardUtils.hideKeyboard(binding.challengeId.inputValue)
        binding.ll.requestFocus()
        try {
            val settingsManagement = SettingsManagement()
            settingsManagement.isEnableBiometricsPin = binding.enableBiometrics.toggleBtn.isChecked
            viewModel.initSdk(
                requireContext().applicationContext,
                binding.endpoint.inputValue.text.toString(),
                binding.appId.inputValue.text.toString(),
                settingsManagement,
            )
        } catch (e: Exception) {
            showSnack(e.message ?: "initSdk catch null", binding.main)
            return
        }
        setInProgress(true)
        launchBlock()
    }

    private fun setInProgress(inProgress: Boolean) {
        binding.execute.isClickable = !inProgress
        binding.loading.visibility = if (inProgress) View.VISIBLE else View.GONE
    }

    private fun showSnack(message: String, view: View) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("", null)
        snackbar.view.setBackgroundColor(Color.BLACK)
        val textView =
            snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 10
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    private fun executeDataChanged() {
        viewModel.executeDataChanged(
            binding.endpoint.inputValue.text.toString(),
            binding.appId.inputValue.text.toString(),
            binding.userToken.inputValue.text.toString(),
            binding.encryptionKey.inputValue.text.toString(),
            viewModel.executeFormState.value?.socialUserToken,
            viewModel.executeFormState.value?.socialEncryptionKey,
            viewModel.executeFormState.value?.emailUserToken,
            viewModel.executeFormState.value?.emailEncryptionKey,
            binding.challengeId.inputValue.text.toString(),
        )
    }

    private inline fun handleExecuteState(state: ExecuteUiState, onTerminal: () -> Unit) {
        when (state) {
            is ExecuteUiState.Idle -> Unit
            is ExecuteUiState.Loading -> setInProgress(true)
            is ExecuteUiState.Success -> {
                setInProgress(false)
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_SUCCESS,
                    binding.root.context.getString(R.string.execute_successful)
                )
                onTerminal()
            }
            is ExecuteUiState.Warning -> {
                setInProgress(false)
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    "${state.warning.warningType}, ${state.warning.warningString}, " +
                        "${state.result?.resultType?.name}, ${state.result?.status?.name}, ${state.result?.data?.signature}"
                )
                onTerminal()
            }
            is ExecuteUiState.Error -> {
                setInProgress(false)
                val throwable = state.throwable
                Log.e(TAG, "execute failed", throwable)
                if (throwable !is ApiError) {
                    AlertBar.showAlert(
                        binding.root,
                        AlertBar.Type.ALERT_FAILED,
                        throwable.message ?: "onError null"
                    )
                    onTerminal()
                    return
                }
                when (throwable.code) {
                    ApiError.ErrorCode.userCanceled,
                    ApiError.ErrorCode.networkError -> {
                        AlertBar.showAlert(
                            binding.root,
                            AlertBar.Type.ALERT_FAILED,
                            throwable.code.value.toString() + " " + throwable.message
                        )
                    }
                    else -> {
                        AlertBar.showAlert(
                            binding.root,
                            AlertBar.Type.ALERT_FAILED,
                            throwable.message
                        )
                    }
                }
                onTerminal()
            }
        }
    }
}
