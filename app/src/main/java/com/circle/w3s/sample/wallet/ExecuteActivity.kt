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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.circle.w3s.sample.wallet.databinding.ActivitySocialExecuteBinding
import com.circle.w3s.sample.wallet.ui.ExecuteViewModel
import com.circle.w3s.sample.wallet.ui.alert.AlertBar
import com.circle.w3s.sample.wallet.ui.main.ExecuteDirections
import com.circle.w3s.sample.wallet.ui.main.ExecuteUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExecuteActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG: String = "APP.SocialExecuteActivity"
    private lateinit var binding: ActivitySocialExecuteBinding
    private val viewModel: ExecuteViewModel by viewModels()


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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.executeState.collect { state -> handleExecuteState(state) }
            }
        }
    }

    private fun executeSocial() {
        viewModel.execute(
            this,
            binding.userToken.content.text.toString(),
            binding.encryptionKey.text.toString(),
            binding.challengeId.inputValue.text.toString()
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
        viewModel.moveTaskToFront(this)
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

    private fun handleExecuteState(state: ExecuteUiState) {
        // This screen has no loading UI, so Idle/Loading both collapse to no-op.
        //
        // `when` used as expression + per-branch resetExecuteState so a future variant is
        // forced to either decide its own reset policy or trip a hard compile error — avoids
        // a fall-through path where a new non-terminal variant silently auto-resets. The
        // `@Suppress` silences the otherwise-unavoidable `UNUSED_VARIABLE` warning this
        // idiom produces.
        if (state is ExecuteUiState.Error) Log.e(TAG, "execute failed", state.throwable)
        @Suppress("UNUSED_VARIABLE")
        val exhaustiveWhen: Unit = when (val d = viewModel.computeExecuteDirections(state)) {
            is ExecuteDirections.None -> Unit
            is ExecuteDirections.ShowLoading -> Unit
            is ExecuteDirections.NavigateSuccess -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_SUCCESS,
                    getString(R.string.execute_successful),
                )
                viewModel.resetExecuteState()
            }
            is ExecuteDirections.NavigateWarning -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    "${d.warning.warningType}, ${d.warning.warningString}, " +
                        "${d.result?.resultType?.name}, ${d.result?.status?.name}, ${d.result?.data?.signature}",
                )
                viewModel.resetExecuteState()
            }
            is ExecuteDirections.NavigateTransientError -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    "${d.code} ${d.message}",
                )
                viewModel.resetExecuteState()
            }
            is ExecuteDirections.GoCustom -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    d.message ?: "onError null",
                )
                viewModel.resetExecuteState()
            }
            is ExecuteDirections.NavigateGenericError -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    d.message,
                )
                viewModel.resetExecuteState()
            }
        }
    }
}
