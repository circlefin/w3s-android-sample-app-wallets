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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.ApiError.ErrorCode
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.api.ExecuteEvent
import circle.programmablewallet.sdk.presentation.EventListener
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.result.ExecuteResult
import com.circle.w3s.sample.wallet.CustomActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.FragmentMainBinding
import com.circle.w3s.sample.wallet.pwcustom.MyLayoutProvider
import com.circle.w3s.sample.wallet.pwcustom.MyViewSetterProvider
import com.circle.w3s.sample.wallet.util.KeyboardUtils
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment(), EventListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
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
    }
    private fun getVersionName(context: Context): String{
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        return packageInfo.versionName;
    }
    private fun init(){
        context?.let {
            val versionName = getVersionName(it)
            binding.version.text = versionName
        }
        binding.execute.setOnClickListener { executePwSdk() }
        viewModel.executeFormState.observe(viewLifecycleOwner, Observer {
            val executeState = it ?: return@Observer
            binding.execute.isEnabled = executeState.isDataValid
        })
        binding.endpoint.inputTitle.setText(R.string.label_endpoint)
        binding.addId.inputTitle.setText(R.string.label_app_id)
        binding.userToken.inputTitle.setText(R.string.label_user_token)
        binding.encryptionKey.inputTitle.setText(R.string.label_encryption_key)
        binding.challengeId.inputTitle.setText(R.string.label_challenge_id)
        binding.endpoint.inputValue.setText(R.string.pw_endpoint)
        binding.endpoint.inputValue.doAfterTextChanged {
            executeDataChanged()
        }
        binding.addId.inputValue.doAfterTextChanged {
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
    fun setInProgress(inProgress: Boolean) {
        binding.execute.setClickable(!inProgress)
        binding.loading.visibility = if (inProgress) View.VISIBLE else View.GONE
    }
    private fun executeDataChanged(){
        viewModel.executeDataChanged(
            binding.endpoint.inputValue.text.toString(),
            binding.addId.inputValue.text.toString(),
            binding.userToken.inputValue.text.toString(),
            binding.encryptionKey.inputValue.text.toString(),
            binding.challengeId.inputValue.text.toString(),
        )
    }
    private fun executePwSdk(){
        KeyboardUtils.hideKeyboard(binding.challengeId.inputValue)
        binding.ll.requestFocus()
        try{
            WalletSdk.init(
                requireContext().applicationContext,
                WalletSdk.Configuration(
                    binding.endpoint.inputValue.text.toString(),
                    binding.addId.inputValue.text.toString()
                )
            )

            WalletSdk.setCustomUserAgent("ANDROID-SAMPLE-APP-WALLETS")

            WalletSdk.setSecurityQuestions(
                arrayOf(
                    SecurityQuestion("What is your father’s middle name?"),
                    SecurityQuestion("What is your favorite sports team?"),
                    SecurityQuestion("What is your mother’s maiden name?"),
                    SecurityQuestion("What is the name of your first pet?"),
                    SecurityQuestion("What is the name of the city you were born in?"),
                    SecurityQuestion("What is the name of the first street you lived on?"),
                    SecurityQuestion("When is your father’s birthday?", SecurityQuestion.InputType.datePicker)
                ))
        }catch (t: Throwable){
            showSnack(t.message?: "executePwSdk catch null")
            return
        }
        WalletSdk.addEventListener(this)

        WalletSdk.setLayoutProvider(context?.let { MyLayoutProvider(it) })
        WalletSdk.setViewSetterProvider(context?.let { MyViewSetterProvider(it) })
        pwExecute(
            activity,
            binding.userToken.inputValue.text.toString(),
            binding.encryptionKey.inputValue.text.toString(),
            binding.challengeId.inputValue.text.toString()
        )
    }
    fun showSnack(message: String){
        val snackbar = Snackbar.make(binding.root, message,
            Snackbar.LENGTH_LONG).setAction("Action", null)
        snackbar.setActionTextColor(Color.BLACK)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.BLACK)
        val textView =
            snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }
    fun goCustom(context: Context, msg: String?) {
        val b = Bundle()
        b.putString(CustomActivity.ARG_MSG, msg)
        val intent = Intent(
            context,
            CustomActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtras(b)
        context.startActivity(intent)
    }
    private fun pwExecute(
        activity: Activity?, userToken: String?,
        encryptionKey: String?, challengeId: String) {
        setInProgress(true)
        WalletSdk.execute(
            activity,
            userToken,
            encryptionKey,
            arrayOf<String>(challengeId),
            object : Callback<ExecuteResult> {
                override fun onError(error: Throwable): Boolean {
                    setInProgress(false)
                    error.printStackTrace()
                    showSnack(error.message ?: "onError null")
                    if (error is ApiError) {
                        when (error.code) {
                            ErrorCode.userCanceled -> return false // App won't handle next step, SDK will finish the Activity.
                            ErrorCode.incorrectUserPin, ErrorCode.userPinLocked,
                            ErrorCode.incorrectSecurityAnswers, ErrorCode.securityAnswersLocked,
                            ErrorCode.insecurePinCode, ErrorCode.pinCodeNotMatched-> {}
                            ErrorCode.networkError -> {
                                context?.let {
                                    goCustom(it, error.message)
                                    return false
                                }
                            }
                            else -> context?.let { goCustom(it, error.message) }
                        }
                        return true // App will handle next step, SDK will keep the Activity.
                    }
                    return false // App won't handle next step, SDK will finish the Activity.
                }

                override fun onResult(result: ExecuteResult) {
                    setInProgress(false)
                    showSnack(String.format(
                        "%s, %s",
                        result.resultType.name,
                        result.status.name))
                }
            })
    }

    override fun onEvent(event: ExecuteEvent?) {
        context?.let {
            if (event != null) {
                goCustom(it, event.name)
            }
        }
    }

}