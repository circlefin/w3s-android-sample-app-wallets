// Copyright (c) 2023, Circle Technologies, LLC. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.circle.w3s.sample.wallet.ui.main

import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.ApiError.ErrorCode
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.api.ExecuteEvent
import circle.programmablewallet.sdk.api.ExecuteWarning
import circle.programmablewallet.sdk.presentation.EventListener
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.presentation.SettingsManagement
import circle.programmablewallet.sdk.result.ExecuteResult
import circle.programmablewallet.sdk.result.ExecuteResultType
import com.circle.w3s.sample.wallet.CustomActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.FragmentMainBinding
import com.circle.w3s.sample.wallet.pwcustom.MyLayoutProvider
import com.circle.w3s.sample.wallet.pwcustom.MyViewSetterProvider
import com.circle.w3s.sample.wallet.util.KeyboardUtils
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment(), EventListener, Callback<ExecuteResult> {
    private val TAG = MainFragment::class.simpleName

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupSdk()
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

    private fun init() {
        context?.let {
            val versionName = getVersionName(it)
            binding.version.text = versionName
        }
        viewModel.naviDirections.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            findNavController().navigate(it)
            viewModel.setNaviDirections(null)
        })
        binding.execute.setOnClickListener {
            initAndLaunchSdk {
                WalletSdk.execute(
                    activity,
                    binding.userToken.inputValue.text.toString(),
                    binding.encryptionKey.inputValue.text.toString(),
                    arrayOf(binding.challengeId.inputValue.text.toString()),
                    this
                )
            }
        }
        viewModel.executeFormState.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            binding.execute.isEnabled = it.isExecuteDataValid
        })
        viewModel.isSetBiometricsPinDataValid.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            binding.setBiometricsPin.isEnabled = it
        })

        binding.endpoint.inputValue.setText(R.string.pw_endpoint)
        binding.appId.inputValue.setText(R.string.pw_app_id)
        viewModel.executeFormState.value?.let {
            binding.endpoint.inputValue.setText(it.endpoint)
            binding.appId.inputValue.setText(it.appId)
            binding.userToken.inputValue.setText(it.userToken)
            binding.encryptionKey.inputValue.setText(it.encryptionKey)
            binding.challengeId.inputValue.setText(it.challengeId)
        }
        binding.setBiometricsPin.setOnClickListener {
            initAndLaunchSdk {
                WalletSdk.setBiometricsPin(
                    activity,
                    binding.userToken.inputValue.text.toString(),
                    binding.encryptionKey.inputValue.text.toString(),
                    this
                )
            }
        }
        binding.challengeId.inputTitle.setText(R.string.label_challenge_id)
        binding.enableBiometrics.inputTitle.setText(R.string.label_enable_biometrics)
        binding.endpoint.inputTitle.setText(R.string.label_endpoint)
        binding.appId.inputTitle.setText(R.string.label_app_id)
        binding.userToken.inputTitle.setText(R.string.label_user_token)
        binding.encryptionKey.inputTitle.setText(R.string.label_encryption_key)
        binding.enableBiometrics.toggleBtn.isChecked = viewModel.enableBiometrics.value ?: true
        binding.enableBiometrics.toggleBtn.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnableBiometrics(
                isChecked
            )
        }

        binding.endpoint.inputValue.doAfterTextChanged {
            executeDataChanged()
        }
        binding.appId.inputValue.doAfterTextChanged {
            executeDataChanged()
        }
        binding.userToken.inputValue.doAfterTextChanged {
            executeDataChanged()
        }
        binding.encryptionKey.inputValue.doAfterTextChanged {
            executeDataChanged()
        }
        binding.challengeId.inputValue.doAfterTextChanged {
            executeDataChanged()
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        binding.execute.isClickable = !inProgress
        binding.loading.visibility = if (inProgress) View.VISIBLE else View.GONE
    }

    private fun executeDataChanged() {
        viewModel.executeDataChanged(
            binding.endpoint.inputValue.text.toString(),
            binding.appId.inputValue.text.toString(),
            binding.userToken.inputValue.text.toString(),
            binding.encryptionKey.inputValue.text.toString(),
            binding.challengeId.inputValue.text.toString(),
        )
    }

    private fun setupSdk() {
        WalletSdk.addEventListener(this)
        WalletSdk.setLayoutProvider(context?.let { MyLayoutProvider(it) })
        WalletSdk.setViewSetterProvider(context?.let { MyViewSetterProvider(it) })
        WalletSdk.setCustomUserAgent("ANDROID-SAMPLE-APP-WALLETS")
        WalletSdk.setSecurityQuestions(
            arrayOf(
                SecurityQuestion("What is your father’s middle name?"),
                SecurityQuestion("What is your favorite sports team?"),
                SecurityQuestion("What is your mother’s maiden name?"),
                SecurityQuestion("What is the name of your first pet?"),
                SecurityQuestion("What is the name of the city you were born in?"),
                SecurityQuestion("What is the name of the first street you lived on?"),
                SecurityQuestion(
                    "When is your father’s birthday?",
                    SecurityQuestion.InputType.datePicker
                )
            )
        )
    }

    private inline fun initAndLaunchSdk(launchBlock: () -> Unit) {
        KeyboardUtils.hideKeyboard(binding.challengeId.inputValue)
        binding.ll.requestFocus()
        try {
            val settingsManagement = SettingsManagement()
            settingsManagement.isEnableBiometricsPin = binding.enableBiometrics.toggleBtn.isChecked
            WalletSdk.init(
                requireContext().applicationContext,
                WalletSdk.Configuration(
                    binding.endpoint.inputValue.text.toString(),
                    binding.appId.inputValue.text.toString(),
                    settingsManagement
                )
            )
        } catch (t: Throwable) {
            showSnack(t.message ?: "initSdk catch null")
            return
        }
        setInProgress(true)
        launchBlock()
    }

    private fun setDirection(
        challengeId: String? = binding.challengeId.inputValue.text.toString(),
        result: ExecuteResult? = null,
        errorCode: String? = null,
        errorMessage: String? = null,
        warning: ExecuteWarning? = null,
    ) {
        val directions: NavDirections = MainFragmentDirections.actionMainFragmentToResultFragment(
            challengeId = when (result?.resultType) {
                ExecuteResultType.SET_BIOMETRICS_PIN, null -> null
                else -> challengeId
            },
            challengeType = result?.resultType?.name,
            challengeStatus = result?.status?.name,
            errorCode = errorCode,
            errorMessage = errorMessage,
            signature = result?.data?.signature,
            warningType = warning?.name,
            warningMessage = warning?.warningString
        )
        viewModel.setNaviDirections(directions)
    }

    private fun showSnack(
        message: String
    ) {
        Log.i(TAG, message)
        val snackbar = Snackbar.make(
            binding.root, message,
            Snackbar.LENGTH_LONG
        ).setAction("", null)
        snackbar.view.setBackgroundColor(Color.BLACK)
        val textView =
            snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 10
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    private fun goCustom(context: Context?, msg: String?) {
        context ?: return
        val b = Bundle()
        b.putString(CustomActivity.ARG_MSG, msg)
        val intent = Intent(
            context,
            CustomActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtras(b)
        context.startActivity(intent)
    }

    override fun onEvent(event: ExecuteEvent?) {
        event?.let {
            goCustom(context, it.name)
        }
    }

    override fun onError(error: Throwable): Boolean {
        setInProgress(false)
        error.printStackTrace()
        if (error !is ApiError) {
            setDirection(errorMessage = error.message ?: "onError null")
            return false // App won't handle next step, SDK will finish the Activity.
        }
        when (error.code) {
            ErrorCode.userCanceled,
            ErrorCode.networkError -> {
                setDirection(
                    errorCode = error.code.value.toString(),
                    errorMessage = error.message
                )
                return false // App won't handle next step, SDK will finish the Activity.
            }

            ErrorCode.incorrectUserPin, ErrorCode.userPinLocked,
            ErrorCode.incorrectSecurityAnswers, ErrorCode.securityAnswersLocked,
            ErrorCode.insecurePinCode, ErrorCode.pinCodeNotMatched -> {
            }

            else ->
                goCustom(
                    context,
                    error.message
                )
        }
        return true // App will handle next step, SDK will keep the Activity.
    }

    override fun onWarning(warning: ExecuteWarning?, result: ExecuteResult?): Boolean {
        setInProgress(false)
        setDirection(warning = warning, result = result)
        //return true, App will handle next step, SDK will keep the Activity.
        //return false, App won't handle next step, SDK will finish the Activity.
        return false;
    }

    override fun onResult(result: ExecuteResult) {
        setInProgress(false)
        setDirection(result = result)
    }
}