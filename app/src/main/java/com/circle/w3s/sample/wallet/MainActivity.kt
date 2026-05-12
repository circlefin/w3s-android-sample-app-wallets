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

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import com.circle.w3s.sample.wallet.databinding.ActivityMainBinding
import com.circle.w3s.sample.wallet.pwcustom.MyLayoutProvider
import com.circle.w3s.sample.wallet.pwcustom.MyViewSetterProvider
import com.circle.w3s.sample.wallet.ui.main.MainViewModel
import com.circle.w3s.sample.wallet.ui.main.tabs.TabPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val endPoint = getString(R.string.pw_endpoint)
        val appId = getString(R.string.pw_app_id)
        viewModel.executeDataChanged(endPoint, appId, null, null, null, null, null, null, null)

        setupSdk()

        val viewPager: ViewPager2 = binding.viewpager
        viewPager.adapter = TabPagerAdapter(this)
        // Keep all tabs' views resident so transient view state (focus, internal scroll) is
        // preserved on revisit. The default offscreenPageLimit destroys views on off-screen
        // pages, which still preserves ViewModel-backed state but loses local UI state.
        viewPager.offscreenPageLimit = TabPagerAdapter.OFFSCREEN_PAGE_LIMIT_FOR_FULL_RESIDENCY

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = TabPagerAdapter.TAB_TITLES[position]
        }.attach()
    }

    private fun setupSdk() {
        // Runs once per onCreate. The PW SDK setters delegate to a process-wide singleton, so
        // re-invocation across re-creations is idempotent. Providers are handed the
        // applicationContext (not `this`) because the SDK retains them for the lifetime of the
        // process — passing the Activity would leak it across config changes.
        viewModel.setLayoutProvider(MyLayoutProvider(applicationContext))
        viewModel.setViewSetterProvider(MyViewSetterProvider(applicationContext))
        viewModel.setCustomUserAgent("ANDROID-SAMPLE-APP-WALLETS")
        viewModel.setSecurityQuestions(
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

}
