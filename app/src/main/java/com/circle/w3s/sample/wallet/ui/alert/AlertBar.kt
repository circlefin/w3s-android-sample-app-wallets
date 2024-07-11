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

package com.circle.w3s.sample.wallet.ui.alert

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.circle.w3s.sample.wallet.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout

@SuppressLint("RestrictedApi")
class AlertBar {
    enum class Type {
        ALERT_SUCCESS,
        ALERT_FAILED
    }

    companion object {
        val MAX_DISPLAY_DURATION: Int = 3 * 1000

        fun showAlert(view: View, style: Type, message: String) {
            val snackbar: Snackbar = Snackbar.make(view, message, MAX_DISPLAY_DURATION)
            val snackbarView: View = snackbar.view

            try {
                val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
                params.gravity = Gravity.BOTTOM
                snackbarView.layoutParams = params
            } catch (t: Throwable) {
                val params = snackbarView.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.BOTTOM
                snackbarView.layoutParams = params
            }

            snackbarView.setBackgroundColor(Color.TRANSPARENT)

            snackbarView.setPadding(0, 0, 0, 0)

            val messageTv: TextView =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_text)
            messageTv.visibility = View.INVISIBLE

            val inflater = LayoutInflater.from(view.context)
            val customSnackView: View = inflater.inflate(R.layout.layout_alert_snack_bar, null)
            val mainUi: ConstraintLayout = customSnackView.findViewById(R.id.main)
            val icon: ImageView = customSnackView.findViewById(R.id.icon)
            val msg: TextView = customSnackView.findViewById(R.id.message)
            msg.text = message
            val close: ImageView = customSnackView.findViewById(R.id.close)
            close.setOnClickListener { snackbar.dismiss() }
            val context = view.context
            if (Type.ALERT_FAILED == style) {
                mainUi.setBackgroundResource(R.drawable.background_alert_negative_panel)
                icon.setImageResource(R.drawable.ic_alert_negative)
                msg.setTextColor(context.getColor(R.color.alert_text_negative))
                close.setColorFilter(
                    ContextCompat.getColor(context, R.color.alert_text_negative),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                mainUi.setBackgroundResource(R.drawable.background_alert_positive_panel)
                icon.setImageResource(R.drawable.ic_alert_positive)
                msg.setTextColor(view.context.getColor(R.color.alert_text_positive))
                close.setColorFilter(
                    ContextCompat.getColor(context, R.color.alert_text_positive),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            val layout = snackbar.view as SnackbarLayout
            layout.addView(customSnackView, 0);
            snackbar.show()
        }
    }
}