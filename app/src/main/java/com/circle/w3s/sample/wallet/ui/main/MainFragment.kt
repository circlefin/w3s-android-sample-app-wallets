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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ApiError
import circle.programmablewallet.sdk.api.ApiError.ErrorCode
import circle.programmablewallet.sdk.api.Callback
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import circle.programmablewallet.sdk.result.ExecuteResult
import com.circle.w3s.sample.wallet.databinding.FragmentMainBinding
import com.circle.w3s.sample.wallet.pwcustom.MyLayoutProvider
import com.circle.w3s.sample.wallet.pwcustom.MyViewSetterProvider
import com.circle.w3s.sample.wallet.util.KeyboardUtils

class MainFragment : Fragment() {

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
        binding.secretKeyEdit.doAfterTextChanged {
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
            binding.secretKeyEdit.text.toString(),
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
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
            return
        }
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
            binding.secretKeyEdit.text.toString(),
            binding.challengeIdEdit.text.toString()
        )
    }
    private fun pwExecute(
        activity: Activity?, userToken: String?,
        secretKey: String?, challengeId: String) {
        setInProgress(true)
        WalletSdk.execute(
            activity,
            userToken,
            secretKey,
            arrayOf<String>(challengeId),
            object : Callback<ExecuteResult> {
                override fun onError(error: Throwable): Boolean {
                    setInProgress(false)
                    error.printStackTrace()
                    Toast.makeText(activity, error.message, Toast.LENGTH_LONG).show()
                    if (error is ApiError) {
                        when (error.code) {
                            ErrorCode.incorrectUserPin, ErrorCode.userPinLocked -> return true // App will handle next step, SDK will keep the Activity.
                            ErrorCode.userHasSetPin, ErrorCode.userCanceled -> {}
                            else -> {}
                        }
                    }
                    return false // App won't handle next step, SDK will finish the Activity.
                }

                override fun onResult(result: ExecuteResult) {
                    setInProgress(false)
                    Toast.makeText(activity,String.format(
                        "%s, %s",
                        result.resultType.name,
                        result.status.name
                    ), Toast.LENGTH_LONG).show()
                }
            })
    }
}