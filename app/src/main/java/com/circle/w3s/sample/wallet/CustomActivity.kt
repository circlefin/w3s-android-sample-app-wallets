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
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import circle.programmablewallet.sdk.WalletSdk

class CustomActivity: AppCompatActivity() {
    companion object {
        const val ARG_MSG = "msg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_alert)


        val msgTv = findViewById<TextView>(R.id.msg)
        val btMain = findViewById<TextView>(R.id.btMain)
        btMain.setOnClickListener { v: View? ->
            finish()
        }
        val b = intent.extras ?: return
        val msg = b.getString(ARG_MSG)
        if (msg != null) {
            msgTv.text = msg
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

}