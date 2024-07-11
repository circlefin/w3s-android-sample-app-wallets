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

package com.circle.w3s.sample.wallet.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private val args: ResultFragmentArgs by navArgs()

    private lateinit var binding: FragmentResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btMain.setOnClickListener {
            findNavController().popBackStack()
        }
        args.challengeId?.let {
            binding.challengeId.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_challenge_id)
                value.text = it
            }
        }
        args.challengeType?.let {
            binding.successFail1.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_challenge_type)
                value.text = it
            }
        }
        args.challengeStatus?.let {
            binding.successFail2.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_challenge_status)
                value.text = it
            }
        }
        args.errorCode?.let {
            binding.successFail1.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_error_code)
                value.text = it
            }
        }
        args.errorMessage?.let {
            binding.successFail2.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_error_message)
                value.text = it
            }
        }
        args.signature?.let {
            binding.signature.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_signature)
                value.text = it
            }
        }
        args.warningType?.let {
            binding.warningType.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_warning_type)
                value.text = it
            }
        }
        args.warningMessage?.let {
            binding.warningMessage.apply {
                root.visibility = View.VISIBLE
                title.setText(R.string.label_warning_message)
                value.text = it
            }
        }
    }
}