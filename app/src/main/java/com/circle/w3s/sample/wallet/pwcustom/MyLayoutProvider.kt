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
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
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

    private fun getGradientColors(): IntArray? {
        return intArrayOf(
            Color.parseColor("#00B14F"),
            Color.parseColor("#23B64B"),
            Color.parseColor("#35BA47"),
            Color.parseColor("#43BE43"),
            Color.parseColor("#4FC23F"),
            Color.parseColor("#59C53C"),
            Color.parseColor("#62C838"),
            Color.parseColor("#6ACA34"),
            Color.parseColor("#71CD31"),
            Color.parseColor("#77CF2D"),
            Color.parseColor("#7DD02A"),
            Color.parseColor("#82D228"),
            Color.parseColor("#85D325"),
            Color.parseColor("#88D424"),
            Color.parseColor("#8AD522"),
            Color.parseColor("#8CD521"),
            Color.parseColor("#8CD521")
        )
    }

    private fun getGradientTypeface(): Typeface? {
        return ResourcesCompat.getFont(context!!, R.font.en_medium)
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
                TextConfig("Web3 PIN", getGradientColors(), getGradientTypeface())
            )

            TextsKey.securityIntroHeadline -> return arrayOf(
                TextConfig("Set up your "),
                TextConfig("Recovery Method", getGradientColors(), getGradientTypeface())
            )

            TextsKey.newPinCodeHeadline -> return arrayOf(
                TextConfig("Enter your new "),
                TextConfig("Web3 PIN", getGradientColors(), getGradientTypeface())
            )

            TextsKey.securityIntroLink -> return arrayOf(
                TextConfig("Learn more"),
                TextConfig("https://path/terms-policies/privacy-notice/")
            )

            TextsKey.recoverPinCodeHeadline -> return arrayOf(
                TextConfig("Recover your "),
                TextConfig("Web3 PIN", getGradientColors(), getGradientTypeface())
            )

            else -> {}
        }
        return super.getTextConfigs(key)
    }

    override fun getIconTextConfigs(key: IconTextsKey?): Array<IconTextConfig>? {
        val url = arrayOf(
            "https://path/intro_item1",
            "https://path/intro_item0",
            "https://path/intro_item2"
        )
        when (key) {
            IconTextsKey.securityConfirmationItems -> return arrayOf<IconTextConfig>(
                IconTextConfig(
                    RemoteImageSetter(R.drawable.ic_intro_item2_icon, url[2]),
                    TextConfig("This is the only way to recover my account access. ")
                ),
                IconTextConfig(
                    RemoteImageSetter(R.drawable.ic_intro_item0_icon, url[0]),
                    TextConfig("Circle won’t store my answers so it’s my responsibility to remember them.")
                ),
                IconTextConfig(
                    RemoteImageSetter(R.drawable.ic_intro_item0_icon, url[0]),
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