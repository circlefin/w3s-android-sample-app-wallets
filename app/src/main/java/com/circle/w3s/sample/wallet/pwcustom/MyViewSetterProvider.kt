package com.circle.w3s.sample.wallet.pwcustom
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

import android.content.Context
import circle.programmablewallet.sdk.presentation.IImageViewSetter
import circle.programmablewallet.sdk.presentation.IToolbarSetter
import circle.programmablewallet.sdk.presentation.LocalImageSetter
import circle.programmablewallet.sdk.presentation.RemoteImageSetter
import circle.programmablewallet.sdk.presentation.RemoteToolbarImageSetter
import circle.programmablewallet.sdk.presentation.Resource
import circle.programmablewallet.sdk.presentation.Resource.ToolbarIcon
import circle.programmablewallet.sdk.presentation.ViewSetterProvider
import com.circle.w3s.sample.wallet.R

class MyViewSetterProvider(context: Context) : ViewSetterProvider() {
    var context: Context = context


    override fun getToolbarImageSetter(type: ToolbarIcon?): IToolbarSetter? {
        when (type) {
            ToolbarIcon.back -> return RemoteToolbarImageSetter(
                R.drawable.ic_back,
                "https://path/ic_back"
            )

            ToolbarIcon.close -> return RemoteToolbarImageSetter(
                R.drawable.ic_close,
                "https://path/ic_close"
            )

            else -> {}
        }
        return super.getToolbarImageSetter(type)
    }

    override fun getImageSetter(type: Resource.Icon?): IImageViewSetter? {
        when (type) {
            Resource.Icon.securityIntroMain -> return RemoteImageSetter(
                R.drawable.ic_intro_main_icon,
                "https://path/ic_intro_main_icon"
            )

            Resource.Icon.selectCheckMark -> return RemoteImageSetter(
                R.drawable.ic_checkmark,
                "https://path/ic_checkmark"
            )

            Resource.Icon.dropdownArrow -> return RemoteImageSetter(
                R.drawable.ic_dropdown_arrow,
                "https://path/ic_dropdown_arrow"
            )

            Resource.Icon.errorInfo -> return RemoteImageSetter(
                R.drawable.ic_error_info,
                "https://path/ic_error_info"
            )

            Resource.Icon.securityConfirmMain -> return RemoteImageSetter(
                R.drawable.ic_confirm_main_icon,
                "https://path/ic_confirm_main_icon"
            )

            Resource.Icon.biometricsAllowMain -> return LocalImageSetter(R.drawable.ic_biometrics_general)
            Resource.Icon.showPin -> return LocalImageSetter(R.drawable.ic_show_pin)
            Resource.Icon.hidePin -> return LocalImageSetter(R.drawable.ic_hide_pin)


            else -> {}
        }
        return super.getImageSetter(type)
    }
}