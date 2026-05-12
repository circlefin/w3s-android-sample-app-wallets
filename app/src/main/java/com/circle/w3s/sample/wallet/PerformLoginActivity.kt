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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import circle.programmablewallet.sdk.api.SocialProvider
import com.circle.w3s.sample.wallet.databinding.ActivitySocialBinding
import com.circle.w3s.sample.wallet.ui.AuthErrorClassification
import com.circle.w3s.sample.wallet.ui.PerformLoginViewModel
import com.circle.w3s.sample.wallet.ui.main.LoginUiState
import com.circle.w3s.sample.wallet.ui.main.LogoutUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PerformLoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG: String = "APP.PerformLoginActivity"
    private val RC_SIGN_IN: Int = 111111
    private lateinit var googleSignInButton: TextView
    private lateinit var facebookSignInButton: TextView
    private lateinit var appleSignInButton: TextView
    private lateinit var msgTv: TextView
    private lateinit var contentDeviceId: TextView

    private lateinit var binding: ActivitySocialBinding
    private val viewModel: PerformLoginViewModel by viewModels()


    companion object {
        const val ARG_MSG = "msg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySocialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deviceToken.inputTitle.setText(R.string.label_device_token)
        binding.deviceEncryptionKey.inputTitle.setText(R.string.label_device_encryption_key)
        binding.otpToken.inputTitle.setText(R.string.label_otp_token)

        // FIXME For test convenience.
//        binding.deviceToken.inputValue.setText(R.string.social_deviceToken)
//        binding.deviceEncryptionKey.inputValue.setText(R.string.social_deviceEncryptionKey)
//        binding.otpToken.inputValue.setText(R.string.otp_token)

        msgTv = findViewById(R.id.msg)
        val btMain = findViewById<TextView>(R.id.btMain)
        btMain.setOnClickListener { v: View? ->
            goBackToSdkUi()
        }
        val b = intent.extras ?: return
        val msg = b.getString(ARG_MSG)
        if (msg != null) {
            msgTv.text = msg
        }
        msgTv.setOnClickListener(this)

        googleSignInButton = findViewById(R.id.sign_in_button_google)
        googleSignInButton.setOnClickListener(this)
        binding.logoutButtonGoogle.setOnClickListener(this)

        facebookSignInButton = findViewById(R.id.sign_in_button_facebook)
        facebookSignInButton.setOnClickListener(this)
        binding.logoutButtonFacebook.setOnClickListener(this)

        appleSignInButton = findViewById(R.id.sign_in_button_apple)
        appleSignInButton.setOnClickListener(this)
        binding.logoutButtonApple.setOnClickListener(this)

        binding.verifyOtpButton.setOnClickListener(this)

        val deviceId = viewModel.getDeviceId(applicationContext)
        contentDeviceId = findViewById(R.id.content_device_id)
        contentDeviceId.text = deviceId
        contentDeviceId.setOnClickListener(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginState.collect { state ->
                        handleLoginState(state) { viewModel.resetLoginState() }
                    }
                }
                launch {
                    viewModel.logoutState.collect { state -> handleLogoutState(state) }
                }
                launch {
                    viewModel.verifyOtpState.collect { state ->
                        handleLoginState(state) { viewModel.resetVerifyOtpState() }
                    }
                }
            }
        }
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

    override fun onStart() {
        super.onStart()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sign_in_button_google -> signInGoogle()
            R.id.logout_button_google -> logoutGoogle()
            R.id.sign_in_button_facebook -> signInFacebook()
            R.id.logout_button_facebook -> logoutFacebook()
            R.id.sign_in_button_apple -> signInApple()
            R.id.logout_button_apple -> logoutApple()
            R.id.content_device_id -> copyDeviceId()
            R.id.msg -> copySocialToken()
            R.id.verify_otp_button -> verifyOtp()
        }
    }

    private fun copySocialToken() {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(msgTv.text, msgTv.text)
        clipboard.setPrimaryClip(clip)
    }

    private fun copyDeviceId() {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(contentDeviceId.text, contentDeviceId.text)
        clipboard.setPrimaryClip(clip)
    }

    private fun signInGoogle() {
        binding.msg.text = "Login Google"
        viewModel.login(
            this,
            SocialProvider.Google,
            binding.deviceToken.inputValue.text.toString(),
            binding.deviceEncryptionKey.inputValue.text.toString()
        )
    }

    private fun logoutGoogle() {
        binding.msg.text = "Logout Google"
        viewModel.logout(
            this,
            SocialProvider.Google
        )
    }

    private fun signInFacebook() {
        binding.msg.text = "Login Facebook"
        viewModel.login(
            this,
            SocialProvider.Facebook,
            binding.deviceToken.inputValue.text.toString(),
            binding.deviceEncryptionKey.inputValue.text.toString()
        )
    }

    private fun logoutFacebook() {
        binding.msg.text = "Logout Facebook"
        viewModel.logout(
            this,
            SocialProvider.Facebook
        )
    }

    private fun signInApple() {
        binding.msg.text = "Login Apple"
        viewModel.login(
            this,
            SocialProvider.Apple,
            binding.deviceToken.inputValue.text.toString(),
            binding.deviceEncryptionKey.inputValue.text.toString()
        )
    }

    private fun logoutApple() {
        binding.msg.text = "Logout Apple"
        viewModel.logout(
            this,
            SocialProvider.Apple
        )
    }

    private fun verifyOtp() {
        viewModel.verifyOtp(
            this,
            binding.otpToken.inputValue.text.toString(),
            binding.deviceToken.inputValue.text.toString(),
            binding.deviceEncryptionKey.inputValue.text.toString()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, handle account details
            updateUI(account, null)
        } catch (e: ApiException) {
            // Handle error
            updateUI(null, e)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?, e: ApiException?) {
        if (account != null) {
            // User is signed in, perform your desired actions
            msgTv.text =
                "${account.idToken}" //"${account.displayName}, ${account.email}, ${account.idToken}"
        } else {
            // User is signed out
            msgTv.text = "${e?.statusCode}, ${e?.status}, ${e?.cause}, ${e?.message}"
        }
    }

    private inline fun handleLoginState(state: LoginUiState, onTerminal: () -> Unit) {
        when (state) {
            is LoginUiState.Idle -> Unit
            // No loading UI for this screen
            is LoginUiState.Loading -> Unit
            is LoginUiState.Success -> {
                Log.i(TAG, "APP onResult result.userToken = [REDACTED]")
                Log.i(TAG, "APP onResult result.encryptionKey = [REDACTED]")
                Log.i(TAG, "APP onResult result.refreshToken = [REDACTED]")
                // Email is PII; redact to match token treatment above.
                Log.i(TAG, "APP onResult result.oauthInfo?.socialUserInfo?.email = [REDACTED]")

                val resultMsg = "result.userToken = [REDACTED]\n\n" +
                        "result.encryptionKey = [REDACTED]\n\n" +
                        "result.refreshToken = [REDACTED]\n\n" +
                        "result.oauthInfo.socialUserInfo.email = [REDACTED]"

                binding.msg.text = resultMsg
                onTerminal()
            }
            is LoginUiState.Error -> {
                Log.e(TAG, "login failed", state.throwable)
                routeLoginOrLogoutError(state.throwable)
                onTerminal()
            }
        }
    }

    private fun handleLogoutState(state: LogoutUiState) {
        when (state) {
            is LogoutUiState.Idle -> Unit
            // No loading UI for this screen
            is LogoutUiState.Loading -> Unit
            is LogoutUiState.Success -> {
                binding.msg.text = "Logout complete"
                viewModel.resetLogoutState()
            }
            is LogoutUiState.Error -> {
                Log.e(TAG, "logout failed", state.throwable)
                routeLoginOrLogoutError(state.throwable)
                viewModel.resetLogoutState()
            }
        }
    }

    /**
     * Shared error-dispatch for the Login and Logout Error branches. Uses the VM's pure
     * [PerformLoginViewModel.classifyAuthError] so this Activity never introspects error
     * types directly.
     *
     * Variant mapping preserves master's behavior: non-transient ApiError routes to a
     * standalone CustomActivity; Transient (userCanceled / networkError) and Unknown
     * (non-ApiError) are silent.
     */
    private fun routeLoginOrLogoutError(error: Throwable) {
        // `when` used as expression so a future AuthErrorClassification variant becomes a
        // hard compile error instead of a silent no-op — same idiom as
        // MainFragment.handleExecuteState / ExecuteActivity.handleExecuteState. The
        // `@Suppress` silences the otherwise-unavoidable `UNUSED_VARIABLE` warning.
        @Suppress("UNUSED_VARIABLE")
        val exhaustiveWhen: Unit = when (val c = viewModel.classifyAuthError(error)) {
            is AuthErrorClassification.Other -> goCustom(this.applicationContext, c.message)
            is AuthErrorClassification.Transient -> Unit
            is AuthErrorClassification.Unknown -> Unit
        }
    }

    private fun goCustom(context: Context?, msg: String?) {
        context ?: return
        val b = Bundle()
        b.putString(CustomActivity.ARG_MSG, msg)
        val intent = Intent(
            context,
            CustomActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtras(b)
        context.startActivity(intent)
    }
}
