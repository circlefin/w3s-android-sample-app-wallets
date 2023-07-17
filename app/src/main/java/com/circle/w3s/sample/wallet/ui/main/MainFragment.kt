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
import android.graphics.Color
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

    private fun init(){
        binding.execute.setOnClickListener { executePwSdk() }
        viewModel.executeFormState.observe(viewLifecycleOwner, Observer {
            val executeState = it ?: return@Observer
            binding.execute.isEnabled = executeState.isDataValid
        })
        binding.endpointEdit.doAfterTextChanged {
            executeDataChanged()
        }
        binding.appIdEdit.doAfterTextChanged {
            executeDataChanged()
        }
        binding.userTokenEdit.doAfterTextChanged {
            executeDataChanged()
        }
        binding.encryptionKeyEdit.doAfterTextChanged {
            executeDataChanged()
        }
        binding.challengeIdEdit.doAfterTextChanged {
            executeDataChanged()
        }
    }
    fun setInProgress(inProgress: Boolean) {
        binding.execute.setClickable(!inProgress)
        binding.loading.visibility = if (inProgress) View.VISIBLE else View.GONE
    }
    private fun executeDataChanged(){
        viewModel.executeDataChanged(
            binding.endpointEdit.text.toString(),
            binding.appIdEdit.text.toString(),
            binding.userTokenEdit.text.toString(),
            binding.encryptionKeyEdit.text.toString(),
            binding.challengeIdEdit.text.toString(),
        )
    }
    private fun executePwSdk(){
        KeyboardUtils.hideKeyboard(binding.challengeIdEdit)
        binding.ll.requestFocus()
        try{
            WalletSdk.init(
                requireContext().applicationContext,
                WalletSdk.Configuration(
                    binding.endpointEdit.text.toString(),
                    binding.appIdEdit.text.toString()
                )
            )
        }catch (t: Throwable){
            showSnack(t.message?: "executePwSdk catch null")
            return
        }
        WalletSdk.addEventListener(this)
        WalletSdk.setSecurityQuestions(
            arrayOf(
                SecurityQuestion("What was your childhood nickname?"),
                SecurityQuestion("What is the name of your favorite childhood friend?"),
                SecurityQuestion("In what city or town did your mother and father meet?"),
                SecurityQuestion("What is the middle name of your oldest child?"),
                SecurityQuestion("When is your birthday?", SecurityQuestion.InputType.datePicker)
            ))

        WalletSdk.setLayoutProvider(context?.let { MyLayoutProvider(it) })
        WalletSdk.setViewSetterProvider(context?.let { MyViewSetterProvider(it) })
        pwExecute(
            activity,
            binding.userTokenEdit.text.toString(),
            binding.encryptionKeyEdit.text.toString(),
            binding.challengeIdEdit.text.toString()
        )
    }
    fun showSnack(message: String){
        val snackbar = Snackbar.make(binding.root, message,
            Snackbar.LENGTH_LONG).setAction("Action", null)
        snackbar.setActionTextColor(Color.BLACK)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.WHITE)
        val textView =
            snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.BLACK)
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