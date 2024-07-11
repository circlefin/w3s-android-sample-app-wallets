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

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import circle.programmablewallet.sdk.WalletSdk
import circle.programmablewallet.sdk.api.ExecuteEvent
import circle.programmablewallet.sdk.presentation.EventListener
import circle.programmablewallet.sdk.presentation.SecurityQuestion
import com.circle.w3s.sample.wallet.CustomActivity
import com.circle.w3s.sample.wallet.MainActivity
import com.circle.w3s.sample.wallet.R
import com.circle.w3s.sample.wallet.pwcustom.MyLayoutProvider
import com.circle.w3s.sample.wallet.pwcustom.MyViewSetterProvider
import com.circle.w3s.sample.wallet.ui.alert.AlertBar
import com.circle.w3s.sample.wallet.ui.main.MainViewModel

class TabPagerAdapter(mainActivity: MainActivity) : PagerAdapter(), EventListener {
    private val activity = mainActivity
    override fun getCount(): Int {
        return 3
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return o === view
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Page: Item${position + 1}"
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View
        val page: ITabPage
        when (position) {
            0 -> {
                page = TabPageSocial(activity)
            }

            1 -> {
                page = TabPageEmail(activity)
            }

            2 -> {
                page = TabPagePin(activity)
            }

            else -> {
                page = TabPagePin(activity)
            }
        }

        page.let {
            view = page.initPage(activity.applicationContext)
            container.addView(view)
        }
        setupSdk(activity.applicationContext)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }


    private fun setupSdk(context: Context) {
        WalletSdk.addEventListener(this)
        context?.let {
            WalletSdk.setLayoutProvider(MyLayoutProvider(it))
            WalletSdk.setViewSetterProvider(MyViewSetterProvider(it))
            WalletSdk.setCustomUserAgent("ANDROID-SAMPLE-APP-WALLETS")
        }
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


    override fun onEvent(event: ExecuteEvent) {
        if (event == ExecuteEvent.forgotPin || event == ExecuteEvent.resendOtp) {
            goCustom(activity, activity.getString(R.string.register_callback))
        }
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
        activity.overridePendingTransition(circle.programmablewallet.sdk.R.anim.no_anim, circle.programmablewallet.sdk.R.anim.no_anim)
    }
}