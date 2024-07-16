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
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.LogoutCallback
import circle.programmablewallet.sdk.api.SocialCallback
import circle.programmablewallet.sdk.api.SocialProvider
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.result.LoginResult
import com.circle.w3s.sample.wallet.ExecuteActivity
import com.circle.w3s.sample.wallet.MainActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.PagerSocialBinding
import com.circle.w3s.sample.wallet.ui.NecessaryTextView
import com.circle.w3s.sample.wallet.ui.alert.AlertBar

class TabPageSocial(activity: MainActivity) : ITabPage(activity), View.OnClickListener,
    SocialCallback<LoginResult> {
    private val activity = activity
    private lateinit var binding: PagerSocialBinding
    private lateinit var contentDeviceId: TextView
    override fun initPage(context: Context): View {
        val inflater = LayoutInflater.from(context)
        binding = PagerSocialBinding.inflate(inflater)
        initPagerSocial(activity, binding)
        return binding.root
    }

    private fun initPagerSocial(activity: MainActivity, binding: PagerSocialBinding) {
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

        binding.tvSocialDes.movementMethod = LinkMovementMethod.getInstance()
        val context = binding.tvSocialDes.context
        val text = context.getString(R.string.label_guide)
        val docs = context.getString(R.string.label_doc)
        val startInx = text.indexOf(docs)
        val endInx = startInx + docs.length
        val spannableString = SpannableString(text)
        val url = "https://developers.circle.com/w3s/docs/authentication-methods#create-a-wallet-with-social-logins"
        spannableString.setSpan(MyURLSpan(url), startInx, endInx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvSocialDes.setText(spannableString, TextView.BufferType.SPANNABLE)

        viewModel.executeFormState.observe(activity, Observer {
            if (!TextUtils.isEmpty(viewModel.executeFormState.value?.socialUserToken)
                && !TextUtils.isEmpty(viewModel.executeFormState.value?.socialEncryptionKey)
            ) {
                binding.btnSocialExecute.visibility = View.VISIBLE
            } else {
                binding.btnSocialExecute.visibility = View.GONE
            }
        })

        binding.signInButtonGoogle.setOnClickListener(this)
        binding.signInButtonFacebook.setOnClickListener(this)
        binding.signInButtonApple.setOnClickListener(this)
        binding.btnSocialExecute.setOnClickListener(this)

        binding.signInButtonGoogle.isEnabled = false
        binding.signInButtonFacebook.isEnabled = false
        binding.signInButtonApple.isEnabled = false
        updateUi(binding)

        binding.appId.inputValue.doAfterTextChanged {
            updateUi(binding)
        }
        binding.deviceToken.inputValue.doAfterTextChanged {
            updateUi(binding)
        }
        binding.deviceEncryptionKey.inputValue.doAfterTextChanged {
            updateUi(binding)
        }

    }

    private fun updateUi(binding: PagerSocialBinding) {
        val context = binding.root.context
        var enableBtn: Boolean

        if (TextUtils.isEmpty(binding.deviceToken.inputValue.text.toString()) ||
            TextUtils.isEmpty(binding.deviceEncryptionKey.inputValue.text.toString()) ||
            TextUtils.isEmpty(binding.appId.inputValue.text.toString())
        ) {
            enableBtn = false
        } else {
            enableBtn = true
        }

        binding.signInButtonGoogle.isEnabled = enableBtn
        binding.signInButtonFacebook.isEnabled = enableBtn
        binding.signInButtonApple.isEnabled = enableBtn

        binding.signInButtonGoogleTv.isEnabled = enableBtn
        binding.signInButtonFacebookTv.isEnabled = enableBtn
        binding.signInButtonAppleTv.isEnabled = enableBtn

        executeDataChanged(binding)
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
        context ?: return
        val b = Bundle()
        b.putString(
            ExecuteActivity.ARG_ENCRYPTION_KEY,
            viewModel.executeFormState.value?.socialEncryptionKey
        )
        b.putString(
            ExecuteActivity.ARG_USER_TOKEN,
            viewModel.executeFormState.value?.socialUserToken
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

    private fun signInSocial(socialProvider: SocialProvider) {
        initAndLaunchSdk {
            WalletSdk.performLogout(activity, socialProvider, object : LogoutCallback {
                override fun onComplete() {
                    WalletSdk.performLogin(
                        activity,
                        socialProvider,
                        binding.deviceToken.inputValue.text.toString(),
                        binding.deviceEncryptionKey.inputValue.text.toString(),
                        this@TabPageSocial
                    )
                }

                override fun onError(error: Throwable) {
                    AlertBar.showAlert(
                        activity.findViewById(android.R.id.content),
                        AlertBar.Type.ALERT_FAILED,
                        error.message ?: "performLogout $socialProvider fail"
                    )
                }
            })
        }
    }

    private fun signInGoogle() {
        signInSocial(SocialProvider.Google)
    }

    private fun signInFacebook() {
        signInSocial(SocialProvider.Facebook)
    }

    private fun signInApple() {
        signInSocial(SocialProvider.Apple)
    }

    override fun onError(error: Throwable) {
        error.printStackTrace()
        AlertBar.showAlert(
            binding.root,
            AlertBar.Type.ALERT_FAILED,
            error.message ?: "onError null"
        )
    }

    override fun onResult(result: LoginResult) {
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
            result.userToken,
            result.encryptionKey,
            originEmailUserToken,
            originEmailEncryptionKey,
            null,
        )
    }

    private fun executeDataChanged(binding: PagerSocialBinding) {
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
            ds.setTypeface(Typeface.DEFAULT_BOLD)
            ds.isUnderlineText = false
        }
    }
}