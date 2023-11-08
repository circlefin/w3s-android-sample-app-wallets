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

package com.circle.w3s.sample.wallet.pwcustom

import android.content.Context
import android.graphics.Color
import circle.programmablewallet.sdk.api.ApiError.ErrorCode
import circle.programmablewallet.sdk.presentation.IconTextConfig
import circle.programmablewallet.sdk.presentation.LayoutProvider
import circle.programmablewallet.sdk.presentation.RemoteImageSetter
import circle.programmablewallet.sdk.presentation.Resource.IconTextsKey
import circle.programmablewallet.sdk.presentation.Resource.TextsKey
import circle.programmablewallet.sdk.presentation.TextConfig
import com.circle.w3s.sample.wallet.R

class MyLayoutProvider(context: Context) : LayoutProvider() {
    private var context: Context = context

    override fun getTextConfig(key: String?): TextConfig? {
        return super.getTextConfig(key)
    }

    private fun getHeadingColors(): Int {
        return Color.parseColor("#0073C3")
    }

    override fun getTextConfigs(key: TextsKey?): Array<TextConfig>? {
        when (key) {
            TextsKey.securityQuestionHeaders -> return arrayOf(
                TextConfig("Choose your 1st question"),
                TextConfig("Choose your 2nd question")
            )

            TextsKey.securitySummaryQuestionHeaders -> return arrayOf(
                TextConfig("1st Question"),
                TextConfig("2nd Question")
            )

            TextsKey.enterPinCodeHeadline -> return arrayOf(
                TextConfig("Enter your "),
                TextConfig("PIN", getHeadingColors(), null)
            )

            TextsKey.securityIntroHeadline -> return arrayOf(
                TextConfig("Set up your "),
                TextConfig("Recovery Method", getHeadingColors(), null)
            )

            TextsKey.newPinCodeHeadline -> return arrayOf(
                TextConfig("Enter your "),
                TextConfig("PIN", getHeadingColors(), null)
            )

            TextsKey.securityIntroLink -> return arrayOf(
                TextConfig("Learn more"),
                TextConfig("https://path/terms-policies/privacy-notice/")
            )

            TextsKey.recoverPinCodeHeadline -> return arrayOf(
                TextConfig("Recover your "),
                TextConfig("PIN", getHeadingColors(), null)
            )

            else -> {}
        }
        return super.getTextConfigs(key)
    }

    override fun getIconTextConfigs(key: IconTextsKey?): Array<IconTextConfig>? {
        val url = arrayOf(
            "https://path/intro_item0",
            "https://path/intro_item1",
            "https://path/intro_item2"
        )
        when (key) {
            IconTextsKey.securityConfirmationItems -> return arrayOf<IconTextConfig>(
                IconTextConfig(
                    RemoteImageSetter(R.drawable.ic_intro_item0_icon, url[0]),
                    TextConfig("This is the only way to recover my account access. ")
                ),
                IconTextConfig(
                    RemoteImageSetter(R.drawable.ic_intro_item1_icon, url[1]),
                    TextConfig("Circle won’t store my answers so it’s my responsibility to remember them.")
                ),
                IconTextConfig(
                    RemoteImageSetter(R.drawable.ic_intro_item2_icon, url[2]),
                    TextConfig("I will lose access to my wallet and my digital assets if I forget my answers. ")
                )
            )

            else -> {}
        }
        return super.getIconTextConfigs(key)
    }

    override fun getErrorString(code: ErrorCode?): String? {
        return super.getErrorString(code)
    }
}