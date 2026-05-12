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
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
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
import circle.programmablewallet.sdk.api.SocialProvider
import circle.programmablewallet.sdk.presentation.SettingsManagement
import com.circle.w3s.sample.wallet.ExecuteActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.PagerSocialBinding
import com.circle.w3s.sample.wallet.ui.NecessaryTextView
import com.circle.w3s.sample.wallet.ui.alert.AlertBar
import com.circle.w3s.sample.wallet.ui.main.LoginUiState
import com.circle.w3s.sample.wallet.ui.main.MainViewModel
import kotlinx.coroutines.launch

private const val TAG = "TabPageSocialFragment"

class TabPageSocialFragment : Fragment(), View.OnClickListener {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: PagerSocialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PagerSocialBinding.inflate(inflater, container, false)
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

        binding.tvSocialDes.movementMethod = LinkMovementMethod.getInstance()
        val text = requireContext().getString(R.string.label_guide)
        val docs = requireContext().getString(R.string.label_doc)
        val startInx = text.indexOf(docs)
        val endInx = startInx + docs.length
        val spannableString = SpannableString(text)
        val url = "https://developers.circle.com/w3s/docs/authentication-methods#create-a-wallet-with-social-logins"
        spannableString.setSpan(MyURLSpan(url), startInx, endInx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvSocialDes.setText(spannableString, TextView.BufferType.SPANNABLE)

        binding.signInButtonGoogle.setOnClickListener(this)
        binding.signInButtonFacebook.setOnClickListener(this)
        binding.signInButtonApple.setOnClickListener(this)
        binding.btnSocialExecute.setOnClickListener(this)

        binding.signInButtonGoogle.isEnabled = false
        binding.signInButtonFacebook.isEnabled = false
        binding.signInButtonApple.isEnabled = false
        updateUi()

        binding.appId.inputValue.doAfterTextChanged { updateUi() }
        binding.deviceToken.inputValue.doAfterTextChanged { updateUi() }
        binding.deviceEncryptionKey.inputValue.doAfterTextChanged { updateUi() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginState.collect { state -> handleLoginState(state) }
                }
                launch {
                    viewModel.executeFormState.collect { state ->
                        val visible = !TextUtils.isEmpty(state?.socialUserToken) &&
                            !TextUtils.isEmpty(state?.socialEncryptionKey)
                        binding.btnSocialExecute.visibility = if (visible) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi() {
        val enableBtn = !TextUtils.isEmpty(binding.deviceToken.inputValue.text.toString()) &&
            !TextUtils.isEmpty(binding.deviceEncryptionKey.inputValue.text.toString()) &&
            !TextUtils.isEmpty(binding.appId.inputValue.text.toString())

        binding.signInButtonGoogle.isEnabled = enableBtn
        binding.signInButtonFacebook.isEnabled = enableBtn
        binding.signInButtonApple.isEnabled = enableBtn

        binding.signInButtonGoogleTv.isEnabled = enableBtn
        binding.signInButtonFacebookTv.isEnabled = enableBtn
        binding.signInButtonAppleTv.isEnabled = enableBtn

        executeDataChanged()
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v.id) {
            R.id.sign_in_button_google -> signInGoogle()
            R.id.sign_in_button_facebook -> signInFacebook()
            R.id.sign_in_button_apple -> signInApple()
            R.id.content -> copyDeviceId()
            R.id.btn_social_execute -> goToExecute(v.context)
        }
    }

    private fun goToExecute(context: Context) {
        val b = Bundle()
        b.putString(
            ExecuteActivity.ARG_ENCRYPTION_KEY,
            viewModel.executeFormState.value?.socialEncryptionKey
        )
        b.putString(
            ExecuteActivity.ARG_USER_TOKEN,
            viewModel.executeFormState.value?.socialUserToken
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

    private fun signInSocial(socialProvider: SocialProvider) {
        initAndLaunchSdk {
            viewModel.signInSocial(
                requireActivity(),
                socialProvider,
                binding.deviceToken.inputValue.text.toString(),
                binding.deviceEncryptionKey.inputValue.text.toString(),
            )
        }
    }

    private fun signInGoogle() = signInSocial(SocialProvider.Google)
    private fun signInFacebook() = signInSocial(SocialProvider.Facebook)
    private fun signInApple() = signInSocial(SocialProvider.Apple)

    private fun handleLoginState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> Unit
            is LoginUiState.Loading -> Unit
            is LoginUiState.Success -> {
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_SUCCESS,
                    binding.root.context.getString(R.string.action_result_login_successful)
                )
                val originUserToken = viewModel.executeFormState.value?.userToken
                val originEncryptionKey = viewModel.executeFormState.value?.encryptionKey
                val originEmailUserToken = viewModel.executeFormState.value?.emailUserToken
                val originEmailEncryptionKey = viewModel.executeFormState.value?.emailEncryptionKey

                viewModel.executeDataChanged(
                    viewModel.executeFormState.value?.endpoint,
                    binding.appId.inputValue.text.toString(),
                    originUserToken,
                    originEncryptionKey,
                    state.result.userToken,
                    state.result.encryptionKey,
                    originEmailUserToken,
                    originEmailEncryptionKey,
                    null,
                )
                viewModel.resetLoginState()
            }
            is LoginUiState.Error -> {
                Log.e(TAG, "social login failed", state.throwable)
                AlertBar.showAlert(
                    binding.root,
                    AlertBar.Type.ALERT_FAILED,
                    state.throwable.message ?: "onError null"
                )
                viewModel.resetLoginState()
            }
        }
    }

    private fun executeDataChanged() {
        val originEndpoint = viewModel.executeFormState.value?.endpoint ?: ""
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

    private class MyURLSpan(url: String?) : URLSpan(url) {
        override fun onClick(widget: View) {
            val uri = Uri.parse(url)
            val context = widget.context
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
            context.startActivity(intent)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.typeface = Typeface.DEFAULT_BOLD
            ds.isUnderlineText = false
        }
    }
}
